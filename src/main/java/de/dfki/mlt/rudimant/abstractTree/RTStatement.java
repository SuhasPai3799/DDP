/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.abstractTree;

/**
 * this is a marker for statement nodes
 *
 * @author Anna Welker
 */
public abstract class RTStatement extends RudiTree {

  /**
   * visitor method
   */
  public abstract void visit(RTStatementVisitor v);

}
