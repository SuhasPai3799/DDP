/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudi.abstractTree;

/**
 * a special kind of the AbstractTree is an expression; expressions can have types
 * @author Anna Welker
 */
public interface AbstractExpression extends AbstractTree {

  /**
   *
   * @return the expression's type
   */
  public String getType() throws Exception;
}
