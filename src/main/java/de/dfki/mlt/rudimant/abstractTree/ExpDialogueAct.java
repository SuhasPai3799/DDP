/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.abstractTree;

import static de.dfki.mlt.rudimant.Constants.*;

import java.util.List;

/**
 * this class represents dialogue acts
 *
 * @author Anna Welker
 */
public class ExpDialogueAct extends RTExpression {

  // comment LITERAL_OR_GRAPH LPAR ( exp (COMMA exp)*)? RPAR comment
  RTExpression daType;
  RTExpression proposition;
  List<RTExpression> exps;

  public ExpDialogueAct(RTExpression daType, RTExpression proposition,
      List<RTExpression> exps) {
    this.daType = daType;
    this.proposition = proposition;
    this.exps = exps;
    this.type = DIALOGUE_ACT_TYPE;
    this.isRdfType = true;
  }

  public void visit(RudiVisitor v) {
    v.visitNode(this);
  }
}
