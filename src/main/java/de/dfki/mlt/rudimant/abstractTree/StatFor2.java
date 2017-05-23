/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.abstractTree;

import java.util.Arrays;

/**
 * FOR LPAR VARIABLE COLON exp RPAR loop_statement_block a 'modern' for
 * statement
 *
 * @author Anna Welker
 */
public class StatFor2 extends RTStatement {

  String varType;
  ExpUVariable var;
  RTExpression initialization;
  RTStatement statblock;
  String position;

  public StatFor2(ExpUVariable v, RTExpression e, RTStatement stat,
      String pos) {
    var = v;
    initialization = e;
    statblock = stat;
    varType = null;
    position = pos;
  }

  public StatFor2(String vType, ExpUVariable v, RTExpression exp,
      RTStatement stat, String pos) {
    var = v;
    initialization = exp;
    statblock = stat;
    if (vType.equals("var")) {
      varType = null;
    } else {
      varType = vType;
    }
    position = pos;
  }

  @Override
  public void visit(RTStatementVisitor v) {
    v.visitNode(this);
  }

  @Override
  public String visitStringV(RTStringVisitor v){
    throw new UnsupportedOperationException("Nodes bigger than expressions must not return Strings but write to out!");
  }

  @Override
  public void visitVoidV(VGenerationVisitor v) {
    v.visitNode(this);
  }

  public Iterable<? extends RudiTree> getDtrs() {
    RudiTree[] dtrs = { var, initialization, statblock };
    return Arrays.asList(dtrs);
  }
}
