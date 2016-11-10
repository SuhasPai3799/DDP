/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.abstractTree;

import de.dfki.lt.hfc.db.rdfProxy.RdfClass;
import de.dfki.lt.hfc.db.rdfProxy.RdfProxy;
import de.dfki.mlt.rudimant.Mem;
import de.dfki.mlt.rudimant.RudimantCompiler;
import java.util.ArrayList;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this visitor calculates the types of nodes and checks whether the types are
 * okay
 *
 * @author Anna Welker, anna.welker@dfki.de
 */
public class VTestTypeVisitor implements RudiVisitor {

  public static Logger logger = LoggerFactory.getLogger(RudimantCompiler.class);

  private RudimantCompiler rudi;
  private Mem mem;

  public VTestTypeVisitor(RudimantCompiler rudi) {
    this.rudi = rudi;
    this.mem = rudi.getMem();
  }

  @Override
  public void visitNode(RudiTree node) {
    node.visit(this);
  }

  /** If that is a binary expression, the resulting type should be the more
   *  specific of both. If they are incompatible there should be a warning.
   *  Maybe the type could be pushed down if there is only a non-empty type
   *  on one branch
   */
  @Override
  public void visitNode(ExpArithmetic node) {
    node.left.visit(this);
    if (node.right != null) {
      node.right.visit(this);
    }
    // ?
    // assert (node.right.getType().equals(node.left.getType()));
  }

  /** This has various aspects.
   *  a) if there is a declaration, and the variable is already defined, that
   *     should be either a warning or an error.
   *  b) if the right side has a non-empty type
   *     1) if the variable is defined, has it the right type? If it has no
   *        type, set it to the type of the right side.
   *     2) if it is not defined, define it and set type
   *  c) if the variable has a type, and the right side has not, push it down
   *     the right branch.
   */
  @Override
  public void visitNode(ExpAssignment node) {
    logger.trace("Testing an assignment");
    node.right.visit(this);
    if (node.declaration || (node.left instanceof UVariable
            && !mem.variableExists(node.left.toString()))) {
      if (node.actualType == null) {
        node.declaration = true;
        String t = node.right.getType();
        boolean worked = mem.addElement(node.left.toString(),
                t, node.position);
        if (!worked) {
          rudi.handleTypeError("You are trying to re-declare the variable "
                  + node.left.toString() + ", you really shouldn't do this");
        }
        // do not forget to tell the variable what type we find out it is
        ((UVariable) node.left).type = t;
        node.actualType = t;
        node.typeRight = t;
        node.testTypeDecl(rudi);
        node.position = mem.getCurrentTopRule();
        return;
      }
      boolean worked = mem.addElement(node.left.toString(),
              node.actualType, node.position);
      if (!worked) {
        rudi.handleTypeError("You are trying to re-declare the variable "
                + node.left.toString() + ", you really shouldn't do this");
      }
      // do not forget to tell the variable what type we find out it is
      ((UVariable) node.left).type = node.actualType;
      node.testTypeDecl(rudi);
      node.position = mem.getCurrentTopRule();
//      node.left.visit(this);
    } else {
      // we have to visit the left part, too, because if this is no declaration
      // that visit method will find out the variable type for us
      node.left.visit(this);
      node.testType(rudi);
    }
  }

  /** In principle the same as ExpArithmetic, with boolean only.
   *  The one difference is that there are unary expressions which serve as
   *  boolean expressions and later have to be turned into proper boolean
   *  expressions, either by calling the right 0-ary method, or comparing with
   *  zero or null.
   */
  @Override
  public void visitNode(ExpBoolean node) {
    // System.out.println(node.fullexp);
    node.rule = mem.getCurrentRule();
    node.left.visit(this);
    if (node.right == null) {
      this.conditionHandling(node);
      return;
    }
    if (node.left.getType() == null) {
      rudi.handleTypeError("expression " + node.fullexp + " could not be resolved to a type");
      node.left.setType("Object");
    }
    node.right.visit(this);
    if (node.right.getType() == null) {
      rudi.handleTypeError("expression " + node.fullexp + " could not be resolved to a type");
      node.right.setType("Object");
    }
    try {
      if (node.operator != null && (this.rudi.getProxy().fetchRdfClass(node.left.getType()) != null)) {
        // TODO: then this should always produce a subsumes, shouldn't it?
//      if (node.left.getType().equals(node.right.getType())) {
        if (node.operator.equals("<=")) {
          node.isSubsumed = true;
        } else if (node.operator.equals("=>")) {
          node.doesSubsume = true;
        }
        if (node.left.getType().contains("Rdf")) {
          node.rdf = true;
        }
//      }
      } else if (node.operator != null) {
        if (!node.left.getType().equals(node.right.getType())) {
          rudi.handleTypeError(node.fullexp + " s a boolean expression with type "
                  + node.left.getType() + " on the one and type " + node.right.getType()
                  + " on the other hand");
        } else {
          node.type = "boolean";
        }
      }
    } catch (TException ex) {
      logger.error(ex.toString());
    }
  }

  /** This should have type "DialogueAct" already, which should be a constant */
  @Override
  public void visitNode(ExpDialogueAct node) {
    // no type testing needed (?)
  }

  /** This should get the return type of the method */
  @Override
  public void visitNode(ExpFuncOnObject node) {
    node.on.visit(this);
    node.funccall.visit(this);
  }

  /** This might push the boolean type downwards for the boolexp, but maybe
   *  that's not necessary.
   */
  @Override
  public void visitNode(ExpIf node) {
    node.boolexp.visit(this);
    node.thenexp.visit(this);
    node.elseexp.visit(this);
//      rudi.handleTypeError(node.fullexp + " is an if expression where the condition does not "
//              + "resolve to boolean!");
    if (!node.thenexp.getType().equals(node.elseexp.getType())) {
      rudi.handleTypeError(node.fullexp + " is an if expression where the else expression "
              + "does not have the same type as the right expression!\n("
              + "comparing types " + node.thenexp.getType() + " on left and "
              + node.elseexp.getType() + " on right)");
    }
    node.type = node.thenexp.getType();
  }

  @Override
  public void visitNode(ExpLambda node) {
    // nothing to do
  }

  @Override
  public void visitNode(GrammarFile node) {
    mem.enterEnvironment();
    String oldname = mem.getClassName();
    String oldrule = mem.getCurrentRule();
    String oldTrule = mem.getCurrentTopRule();
    mem.enterClass(rudi.className);
    for (RudiTree t : node.rules) {
      t.visit(this);
    }
    for (String s : mem.getTopLevelRules(rudi.className)) {
      for (String n : mem.getNeededClasses(s)) {
        mem.needsClass(rudi.className, n);
      }
    }
    // do not leave the environment, we are still in it! (but remember to leave it
    // once the generation is done)
//    mem.leaveEnvironment();
    mem.leaveClass(oldname, oldrule, oldTrule);
  }

  @Override
  public void visitNode(GrammarRule node) {
    mem.addRule(node.label, node.toplevel);
    if (node.toplevel) {
      mem.enterEnvironment();
    }
    node.ifstat.visit(this);
    if (node.toplevel) {
      mem.leaveEnvironment();
    }
  }

  @Override
  public void visitNode(StatAbstractBlock node) {
    if (node.braces) {
      mem.enterEnvironment();
    }
    for (RudiTree t : node.statblock) {
      t.visit(this);
    }
    if (node.braces) {
      mem.leaveEnvironment();
    }
  }

  /** Make sure the boolean exp is really boolean, or transformed into one */
  @Override
  public void visitNode(StatDoWhile node) {
    node.condition.visit(this);
    /*if (!node.condition.getType().equals("boolean")) {
     rudi.handleTypeError("This is a while statement where the condition does not "
     + "resolve to boolean!");
     }*/
    node.statblock.visit(this);
  }

  /** Same for the boolean expression here (is that the right for?) */
  @Override
  public void visitNode(StatFor1 node) {
    // TODO: this is a bit more complicated; remember the types of the variables
    // that were declared in the condition
    // the assignment will add the variable to the memory
  }

  @Override
  public void visitNode(StatFor2 node) {
    // TODO: this is a bit more complicated; remember the types of the variables
    // that were declared in the condition
    if (node.varType == null) {
      String et = node.exp.getType();
      if (et.contains("<")) {
        node.varType = et.substring(et.indexOf("<"), et.indexOf(">"));
      }
    }
    mem.addElement(node.var.toString(), node.varType, node.position);
  }

  @Override
  public void visitNode(StatFor3 node) {
    // TODO: this is a bit more complicated; remember the types of the variables
    // that were declared in the condition
    for (String s : node.variables) {
      mem.addElement(s, "Object", node.position);
    }
  }

  @Override
  public void visitNode(StatFunDef node) {
    // these are not tested, just added to the memory
    mem.addFunction(node.funcname, node.type,
            node.parameterTypes, node.position);
  }

  @Override
  public void visitNode(StatIf node) {
    node.currentRule = mem.getCurrentRule();
    node.condition.visit(this);
    /*if (!node.condition.getType().equals("boolean")) {
     rudi.handleTypeError("This is an if statement where the condition: "
     + node.conditionString + ", does not resolve to boolean!");
     }*/
    node.statblockIf.visit(this);
    if (node.statblockElse != null) {
      node.statblockElse.visit(this);
    }
  }

  @Override
  public void visitNode(StatImport node) {
    String conargs = "";
    if (null != rudi.getConstructorArgs()
            && !rudi.getConstructorArgs().isEmpty()) {
      int i = 0;
      for (String a : rudi.getConstructorArgs().split(",")) {
        if (i > 0) {
          conargs += ", ";
        }
        conargs += a.trim().split(" ")[1];
        i++;
      }
    }
    mem.addImport(node.name, conargs);
  }

  @Override
  public void visitNode(StatListCreation node) {
    if (!(node.objects == null)) {
      for (RTExpression e : node.objects) {
        this.visitNode(e);
      }
      if (node.listType != null) {
        if (!(node.listType.substring(node.listType.indexOf("<"),
                node.listType.indexOf(">")).equals(node.objects.get(0)))) {
          rudi.handleTypeError("Found a list creation where the list type doesn't fit"
                  + " its objects' type");
        }
        mem.addElement(node.variableName, node.listType, node.origin);
        return;
      }
      node.listType = "List<" + node.objects.get(0).getType() + ">";
      mem.addElement(node.variableName, node.listType, node.origin);
    } else if (node.listType == null) {
      node.listType = "Object";
    }
  }

  @Override
  public void visitNode(StatMethodDeclaration node) {
    mem.addFunction(node.name, node.return_type, node.partypes, node.position);
    mem.enterEnvironment();
    if (!node.parameters.isEmpty()) {
      for (int i = 0; i < node.parameters.size(); i++) {
        // add parameters to environment
        mem.addElement(node.parameters.get(i), node.partypes.get(i), node.position);
      }
    }
    node.block.visit(this);
    mem.leaveEnvironment();
  }

  @Override
  public void visitNode(StatPropose node) {
    node.arg.visit(this);
    node.block.visit(this);
  }

  @Override
  public void visitNode(StatReturn node) {
    // nothing to do (?)
  }

  @Override
  public void visitNode(StatSetOperation node) {
    // TODO: test whether the set accepts variables of this type??
  }

  @Override
  public void visitNode(StatVarDef node) {
    mem.addElement(node.variable, node.type, node.position);
  }

  /** push the boolean type down on the boolean exp, if it is not already */
  @Override
  public void visitNode(StatWhile node) {
    node.condition.visit(this);
    node.statblock.visit(this);
  }

  @Override
  public void visitNode(StatSwitch node) {
    node.condition.visit(this);
    node.switchBlock.visit(this);
  }

  @Override
  public void visitNode(UComment node) {
    // everything okay
  }

  @Override
  public void visitNode(UCommentBlock node) {
    // everything okay
  }

  @Override
  public void visitNode(UFieldAccess node) {
    try {
      node.type = node.getPredicateType(rudi.getProxy(), mem, node.representation);
    } catch (TException ex) {
      logger.error(ex.toString());
    }
    /*if(node.type == null){
     // then this is not an rdf node, but some composed funccall
     node.parts.get(node.parts.size() - 1).visit(this);
     node.type = ((RTExpression)node.parts.get(node.parts.size() - 1)).getType();
     return;
     }*/
    for (int i = 0; i < node.representation.size(); i++) {
      node.parts.get(i).visit(this);
//      if (node.representation.get(i).contains("(")) {
//        continue;
//      } else if (!mem.variableExists(node.representation.get(i))) {
//        node.representation.set(i, "\"" + node.representation.get(i) + "\"");
//      }
    }
  }

  /** Do the parameter types of the function are checked against the actual
   *  types? Is it necessary?
   */
  @Override
  public void visitNode(UFuncCall node) {
    if (node.type == null) {
      node.type = mem.getFunctionRetType(node.representation);
    }
    // test whether the given parameters are of the correct type
    ArrayList<String> partypes = new ArrayList<String>();
    for (RTExpression e : node.exps) {
      partypes.add(e.getType());
    }
    if (!mem.existsFunction(node.representation, partypes)) {
      rudi.handleTypeError("The function call to " + node.representation + " referrs"
              + " to a function that wasn't declared");
    }
  }

  @Override
  public void visitNode(USingleValue node) {
    // nothing to test here
  }

  @Override
  public void visitNode(UVariable node) {
    node.type = mem.getVariableType(node.representation);
    String o = mem.getVariableOriginClass(node.representation);
    if (o == null) {
      // the variable does not originate in another file
//      if (node.type == null) {
      // is the variable an rdf type?
      try {
        if (rudi.getProxy().fetchRdfClass(node.type) != null) {
          node.isRdfClass = true;
          return;
        } else // TODO: is this correct????
          // it could still be sth like Introduction
        if (rudi.getProxy().fetchRdfClass(node.representation) != null) {
          node.isRdfClass = true;
          return;
        }
      } catch (TException e) {
        logger.error("Problem accessing database : {}", e.getMessage());
        throw new RuntimeException(e);
      }
//      }
      // if not, mem either found a type or this variable wasn't declared
      if (node.type == null) {
        rudi.handleTypeError("The variable " + node.representation
                + " is used but was not declared");
        node.type = "Object";
        return;
      }
    }
    if (!node.originClass.equals(o)) {
      mem.needsClass(mem.getCurrentTopRule(), o);
      node.realOrigin = o;
    }
  }

  @Override
  public void visitNode(UWildcard node) {
    // nothing to do
  }

  private void conditionHandling(ExpBoolean node) {
    String t = node.left.getType();
    if (t == null) {
      // lets assume it is an unrecognized rdf object
      node.isTrue = " != null";
      return;
    }
    if (!t.equals("boolean")) {
      // tell the expression how it should handle its condition
      if (t.equals("int") || t.equals("float")) {
        node.isTrue = " != 0";
      } else if (mem.isRdf(node.fullexp)) {
        node.isTrue = ".has()?;\n";
      } else {
        node.isTrue = " != null";
        if (t.contains("List") || t.contains("Set") || t.contains("Map")) {
          node.testIsEmpty = true;
        }
      }
      node.left.setType("boolean");
    } else {
      node.isTrue = "";
    }
  }
}
