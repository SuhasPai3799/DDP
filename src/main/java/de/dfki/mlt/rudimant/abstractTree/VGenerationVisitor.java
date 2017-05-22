/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.abstractTree;

import static de.dfki.mlt.rudimant.Constants.DIALOGUE_ACT_TYPE;
import static de.dfki.mlt.rudimant.Utils.capitalize;

import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.rudimant.Mem;
import de.dfki.mlt.rudimant.RudimantCompiler;
import de.dfki.mlt.rudimant.Type;

/**
 * this visitor generates the java code
 *
 * @author Anna Welker, anna.welker@dfki.de
 */
public class VGenerationVisitor implements RTStringVisitor, RTStatementVisitor {

  public static Logger logger = LoggerFactory.getLogger(RudimantCompiler.class);

  RudimantCompiler out;
  private RudimantCompiler rudi;
  private Mem mem;
  private VConditionLogVisitor condV;
  LinkedList<Token> collectedTokens;

  // activate this bool to get double escaped String literals
  private boolean escape = false;

  // flag to tell the if if is a real rule if (contains the condition that was calculated)
  private String ruleIf = null;

  public VGenerationVisitor(RudimantCompiler r, LinkedList<Token> collectedTokens) {
    this.rudi = r;
    this.out = r;
    this.mem = rudi.getMem();
    condV = new VConditionLogVisitor(this);
    this.collectedTokens = collectedTokens;
  }

  @Override
  public void visitNode(RudiTree node) {
    node.visitWithComments(this);
  }

  @Override
  public String visitNode(RTExpression node) {
    return node.visitWithSComments(this);
  }

  @Override
  public String visitNode(ExpArithmetic node) {
    String ret = "";
    if (node.right == null) {
      // unary operator
      // TODO: ENCAPSULATE THIS INTO TWO FUNCTIONS: isPrefixOperator() and
      // isPostFixOperator()
      if ("-".equals(node.operator) || "!".equals(node.operator)) {
        ret += node.operator;
      }
      ret += '(' + node.left.visitWithSComments(this);
      // something like .isEmpty(), which is a postfix operator
      if (node.operator.endsWith(")")) {
        ret += node.operator;
      }
    } else {
      ret += '(' + node.left.visitWithSComments(this);
      ret += node.operator;
      ret += node.right.visitWithSComments(this);
      if (node.operator.endsWith("(")) {
        ret += ')';
      }
    }
    ret += ')';
    return ret;
  }

  boolean replaceLastWithFuncall = false;

  @Override
  public String visitNode(ExpAssignment node) {
    String ret = "";
    if (node.declaration) {
      ret += (Type.convertRdfType(node.type));
    }
    ret += (' ');
    UPropertyAccess pa = null;
    if (node.left instanceof UFieldAccess) {
      UFieldAccess acc = (UFieldAccess) node.left;
      RudiTree lastPart = acc.parts.get(acc.parts.size() - 1);
      if (lastPart instanceof UPropertyAccess) {
        pa = (UPropertyAccess) lastPart;
      }
      // don't print the last field since is will be replaced by a set...(a, b)
      replaceLastWithFuncall = pa != null;
      ret += node.left.visitWithSComments(this);
      if (replaceLastWithFuncall) {
        if (node.right instanceof USingleValue &&
            ((USingleValue)node.right).content.equals("null")) {
          replaceLastWithFuncall = false;
          return ret + ".clearValue(" + pa.getPropertyName() + ")";
        }
        //out.append(functional ? ".setSingleValue(" : ".setValue(");
        ret += ".setValue(";  // always right!
        ret += pa.getPropertyName();
        ret += ", ";
      } else {
        ret += " = ";
      }
      replaceLastWithFuncall = false;
    } else {
      ret += node.left.visitWithSComments(this);
      ret += " = ";
    }
    if (node.type != null
            && !node.type.equals(node.right.getType())
            && !(node.right instanceof ExpNew)) {
      // then there is either sth wrong here, what would at least have resulted
      // in warnings in type testing, or it is possible to cast the right part
      ret += "(" + Type.convertRdfType(node.type) + ") ";
    }
    ret += node.right.visitWithSComments(this);
    if (pa != null) {
      ret += ")"; // close call to set(Single)Value()
    }
    return ret;
  }

  @Override
  public String visitNode(ExpBoolean node) {
    String ret = "";
    if (node.operator != null && node.operator.contains("(")) {
      // other operator, is it sth. like "exists("
      if (! mem.getToplevelInstance().equalsIgnoreCase(rudi.getClassName())) {
        ret += mem.getToplevelInstance() + ".";
      }
      ret += node.operator;
      ret += node.left.visitWithSComments(this);
      if (node.right != null) {
        ret += ", ";
        ret += node.right.visitWithSComments(this);
      }
      ret += ")";
    } else {
      if (node.right != null) {
        ret += "(";
        ret += node.left.visitWithSComments(this);
        ret += " " + node.operator + " ";
        ret += node.right.visitWithSComments(this);
        ret += ")";
      } else {
        if (null != node.operator) {
          ret += node.operator;
        }
        ret += node.left.visitWithSComments(this);
      }
    }
    return ret;
  }

  @Override
  public String visitNode(ExpCast node) {
    return "((" + Type.convertRdfType(node.type) + ")"
        + this.visitNode(node.construct) + ")";
  }

  public String visitDaToken(RTExpression exp) {
    String ret = "";
    if (exp instanceof USingleValue
        && ((USingleValue) exp).type.equals("String")) {
      String s = ((USingleValue) exp).visitStringV(this);
      if (! s.startsWith("\"")) {
        ret += "\"" + s + "\"";
      } else
        ret += s;
    } else {
      ret +=  this.visitNode(exp);
    }
    return ret;
  }

  @Override
  public String visitNode(ExpDialogueAct node) {
    String ret = "new DialogueAct(";
    ret += visitDaToken(node.daType);
    ret += ", ";
    ret += visitDaToken(node.proposition);
    for (int i = 0; i < node.exps.size(); i += 2) {
      ret += ", ";
      ret += visitDaToken(node.exps.get(i));
      ret += ", ";
      ret += visitDaToken(node.exps.get(i + 1));
    }
    ret += ")";
    return ret;
  }

  @Override
  public String visitNode(ExpConditional node) {
    String ret = "(";
    ret += node.boolexp.visitWithSComments(this);
    ret += " ? ";
    ret += node.thenexp.visitWithSComments(this);
    ret += " : ";
    ret += node.elseexp.visitWithSComments(this);
    ret += ')';
    return ret;
  }

  @Override
  public String visitNode(ExpLambda node) {
    String ret = "(" + node.parameters.get(0);
    for(int i = 1; i < node.parameters.size(); i++){
      ret += ", " + node.parameters.get(i);
    }
    ret += ") -> ";
    // this is the rare occasion where sth of class statement is allowed to
    // be inside an expression, prevent it from printing directly to out
    Writer old = out.out;
    out.out = new StringWriter();
    node.body.visitVoidV(this);
    ret += out.out.toString();
    out.out = old;
    return ret;
  }

  @Override
  public String visitNode(ExpNew node) {
    String ret = "";
    if (node.construct != null) {
      ret += "new ";
      ret += node.construct.visitStringV(this);
    } else {
      if(!mem.getClassName().toLowerCase().equals(
          mem.getToplevelInstance().toLowerCase())){
        ret += mem.getToplevelInstance() + ".";
      }
      ret += "_proxy.getClass(\""
              + mem.getProxy().getClass(node.type)
              + "\").getNewInstance(";
      if(!mem.getClassName().toLowerCase().equals(
              mem.getToplevelInstance().toLowerCase())){
        ret += mem.getToplevelInstance() + ".";
      }
      ret += "DEFNS)";
    }
    return ret;
  }

  @Override
  public void visitNode(GrammarRule node) {
    if (node.toplevel) {
      // this is a toplevel rule and will be converted to a method
      out.append("public void " + node.label + "(");
      out.append("){\n");
      this.ruleIf = this.printRuleLogger(node.label, node.ifstat.condition);
      out.append(node.label + ":\n");
      node.ifstat.visitWithComments(this);
      out.append("}\n");
    } else {
      // this is a sublevel rule and will get an if to determine whether it
      // should be executed
      out.append("//Rule " + node.label + "\n");
      this.ruleIf = this.printRuleLogger(node.label, node.ifstat.condition);
      out.append(node.label + ":\n");
      node.ifstat.visitWithComments(this);
    }
  }

  public void visitStatementOrExpression(RudiTree rt) {
    rt.visitWithComments(this);
    if (rt instanceof RTExpression)
      out.append(";\n");
  }

  @Override
  public void visitNode(StatAbstractBlock node) {
    if (node.braces) {
      // when entering a statement block, we need to create a new local
      // environment
      out.append("{");
    }
    for (RudiTree stat : node.statblock) {
      visitStatementOrExpression(stat);
    }
    if (node.braces) {
      out.append("}");
    }
  }

  @Override
  public void visitNode(StatFor1 node) {
    out.append("for ( ");
    node.assignment.visitWithComments(this);
    out.append("; ");
    node.condition.visitWithComments(this);
    out.append(";");
    if (node.arithmetic != null) {
      node.arithmetic.visitWithComments(this);
    }
    out.append(")");
    visitStatementOrExpression(node.statblock);
  }

  @Override
  public void visitNode(StatFor2 node) {
    out.append("for (Object ");
    String var = node.var.visitWithSComments(this);
    out.append(var).append("_outer : ");
    node.exp.visitWithComments(this);
    out.append(") { ")
       .append(Type.convertRdfType(node.varType))
       .append(" ").append(var);
    out.append(" = (").append(Type.convertRdfType(node.varType)).append(")")
       .append(var).append("_outer;\n");
    visitStatementOrExpression(node.statblock);
    out.append("}");
  }

  @Override
  public void visitNode(StatFor3 node) {
    out.append("for (Object[] o : ");
    node.exp.visitWithComments(this);
    out.append(") {");
    int count = 0;
    for (String s : node.variables) {
      out.append("\nObject " + s + " = o[" + count++ + "]");
    }
    visitStatementOrExpression(node.statblock);
    out.append("}");
  }

  @Override
  public void visitNode(StatIf node) {
    if (this.ruleIf != null) {
      out.append("if (" + ruleIf + ") ");
//      out.append("if (shouldLog(\"" + node.currentRule + "\") ? wholeCondition : ");
      ruleIf = null;
    } else {
      out.append("if (");
      node.condition.visitWithComments(this);
      out.append(") ");
    }
    visitStatementOrExpression(node.statblockIf);
    out.append("\n");
    if (node.statblockElse != null) {
      out.append("else ");
      visitStatementOrExpression(node.statblockElse);
    }
  }

  @Override
  public void visitNode(StatListCreation node) {
    out.append(Type.convertRdfType(node.listType)).append(' ')
       .append(node.variableName);
    if (node.listType.startsWith("List")) {
      out.append(" = new ArrayList<>();");
    } else if (node.listType.startsWith("Set")) {
      out.append(" = new HashSet<>();");
    }
    if(node.objects.isEmpty()) {
      return;
    }
    for (RTExpression e : node.objects) {
      out.append(node.variableName + ".add(");
      out.append(this.visitNode(e));
      out.append(");\n");
    }
  }

  @Override
  public void visitNode(StatMethodDeclaration node) {
    if (node.block == null) {
      return;
    }
    out.append(node.visibility + " ");
    out.append(Type.convertRdfType(node.return_type) + " ");
    out.append(node.name + "(");
    for (int i = 0; i < node.parameters.size(); i++) {
      if (i != 0) {
        out.append(", ");
      }
      out.append(Type.convertRdfType(node.partypes.get(i))
              + " " + node.parameters.get(i));
    }
    out.append(")\n");
    node.block.visitWithComments(this);
  }

  @Override
  public void visitNode(StatPropose node) {
    if(!mem.getClassName().toLowerCase().equals(
            mem.getToplevelInstance().toLowerCase())){
      out.append(mem.getToplevelInstance()).append(".");
    }
    out.append("propose(");
    node.arg.visitWithComments(this);
    out.append(",");
    if(!mem.getClassName().toLowerCase().equals(
            mem.getToplevelInstance().toLowerCase())){
      out.append(mem.getToplevelInstance()).append(".");
    }
    out.append("new Proposal() {public void run()\n");
    node.block.visitWithComments(this);
    out.append("});\n");
  }

  @Override
  public void visitNode(StatReturn node) {
    if (mem.isExistingRule(node.lit)) {
      if (mem.getTopLevelRules(mem.getClassName()).contains(node.curRuleLabel)) {
        out.append("return;\n");
        return;
      }
      //out.append("returnTo = returnTo | return_" + node.lit + ";\n");
      out.append("break " + node.lit + ";\n");
      return;

    } else if (node.toRet == null) {
      if (mem.getCurrentRule().equals(mem.getClassName())) {
        out.append("return;\n");
        return;
      }
      out.append("break " + mem.getCurrentRule() + ";\n");
      return;
    }
    out.append("return ");
    node.toRet.visitWithComments(this);
    out.append(";\n");
  }

  @Override
  public void visitNode(StatSetOperation node) {
    node.left.visitWithComments(this);
    if (node.add) {
      out.append(".add(");
    } else {
      out.append(".remove(");
    }
    node.right.visitWithComments(this);
    out.append(");");
  }

  @Override
  public void visitNode(StatVarDef node) {
    // no generation here
  }

  @Override
  public void visitNode(StatWhile node) {
    if (node.isWhileDo()) {
      out.append("while (");
      node.condition.visitWithComments(this);
      out.append(")");
      visitStatementOrExpression(node.block);
    } else {
      out.append("do");
      visitStatementOrExpression(node.block);
      out.append("while (");
      node.condition.visitWithComments(this);
      out.append(");");
    }
  }

  @Override
  public void visitNode(StatSwitch node) {
    out.append("switch (");
    node.condition.visitWithComments(this);
    out.append(")");
    node.block.visitWithComments(this);
  }

  @Override
  public String visitNode(UFieldAccess node) {
    String ret = "";
    int to = node.parts.size();
    // don't print the last field if this is in an assignment rather than an
    // access, which means that a set method is generated.
    if (replaceLastWithFuncall) {
      --to;
    }

    // changed the direction of the for loop; should be enough
    for (int i = to - 1; i > 0; i--) {
      if (node.parts.get(i) instanceof UPropertyAccess) {
        UPropertyAccess pa = (UPropertyAccess) node.parts.get(i);
        String cast = Type.convertRdfType(pa.getType());
        if ("int".equals(cast))
          cast = "Integer";
        else
          cast = capitalize(cast);
        //ret += "((" + cast + ")";
        ret += "((";
        ret += (!pa.functional) ? "Set<Object>" : cast;
        ret += ")";
      }
    }
    ret += node.parts.get(0).visitWithSComments(this);
    String currentType = ((RTExpression) node.parts.get(0)).type;
    for (int i = 1; i < to; i++) {
      RudiTree currentPart = node.parts.get(i);
      if (currentPart instanceof UPropertyAccess) {
        UPropertyAccess pa = (UPropertyAccess) currentPart;
        // then we are in the case that this is actually an rdf operation
        if (DIALOGUE_ACT_TYPE.equals(currentType)) {
          ret += ".getValue(";
        } else {
          ret += pa.functional ? ".getSingleValue(" : ".getValue(";
        }
        ret += pa.getPropertyName();
        ret += "))";
        currentType = pa.type;
      } else {
        ret += ".";
        ret += currentPart.visitStringV(this);
        if (currentPart instanceof RTExpression) {
          currentType = ((RTExpression) currentPart).type;
        } else {
          currentType = null;
        }
      }
    }
    return ret;
  }

  @Override
  public String visitNode(UFuncCall node) {
    String ret = "";
    if (node.realOrigin != null) {
      String t = node.realOrigin;
      ret += t.substring(0, 1).toLowerCase() + t.substring(1) + ".";
    }
    if(node.newexp){
      ret += Type.convertRdfType(node.type) + "(";
    } else {
      ret += node.content + "(";
    }
    for (int i = 0; i < node.exps.size(); i++) {
      ret += node.exps.get(i).visitWithSComments(this);
      if (i != node.exps.size() - 1) {
        ret += ", ";
      }
    }
    ret += ")";
    return ret;
  }

  @Override
  public String visitNode(USingleValue node) {
    if ("String".equals(node.type) && this.escape) {
      // properly escape if needed
      return "\\" + node.content.substring(0, node.content.length() - 1) + "\\\" ";
    }
    return node.content;
  }

  @Override
  public String visitNode(UVariable node) {
    if (node.realOrigin != null) {
      String t = node.realOrigin;
      return t.substring(0, 1).toLowerCase() + t.substring(1) + "." + node.content;
    } else {
      return node.content;
    }
  }

  boolean collectingCondition = false;

  String stringEscape(String in) {
    return in.replaceAll("\\\"", "\\\\\"");
  }
  /**
   * creates and prints the logging method of the given rule
   *
   * @param rule
   */
  private String printRuleLogger(String rule, RTExpression bool_exp) {

    // TODO BK: bool_exp can be a simple expression, in which case it
    // has to be turned into a comparison with zero, null or a call to
    // the has(...) method
    if (bool_exp instanceof USingleValue && bool_exp.getType().equals("boolean")) {
      return ((USingleValue) bool_exp).content;
    }
    collectingCondition = true;

    // remembers how the expressions looked (for logging)
    LinkedHashMap<String, String> realLook = new LinkedHashMap<>();
    LinkedHashMap<String, String> rudiLook = new LinkedHashMap<>();

    condV.newInit(rule, realLook, rudiLook);
    String result = condV.visitNode(bool_exp);
    for (String s : realLook.keySet()) {
      out.append("boolean " + s + " = false;\n");
    }
    out.append(result);

    out.append("if (");
    if(!mem.getClassName().toLowerCase().equals(
            mem.getToplevelInstance().toLowerCase())){
      out.append(mem.getToplevelInstance()).append(".");
    }
    out.append("shouldLog(\"" + rule + "\")){\n");
    // do all that logging
    out.append("Map<String, Boolean> " + rule + " = new LinkedHashMap<>();\n");

    LinkedHashMap<String, String> logging;
      out.append(rule + ".put(\"" + stringEscape(bool_exp.fullexp) + "\", "
          + condV.getLastBool() + ");\n");
    if(out.logRudi()){
      logging = rudiLook;
    } else {
      logging = realLook;
    }
    for (String var : logging.keySet()) {
      out.append(rule + ".put(\"" + stringEscape(logging.get(var)) + "\", " + var + ");\n");
    }
    if(!mem.getClassName().toLowerCase().equals(
            mem.getToplevelInstance().toLowerCase())){
      out.append(mem.getToplevelInstance()).append(".");
    }
    out.append("logRule(" + rule + ", \"" + rule + "\", \""
            + mem.getClassName() + "\");\n");

    out.append("}\n");
    collectingCondition = false;
    //return (String) expnames[expnames.length - 1];
    return condV.getLastBool();
  }
}
