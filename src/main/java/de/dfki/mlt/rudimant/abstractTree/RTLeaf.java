/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.abstractTree;

/**
 * the lowest, simplest nodes in the AbstractTree
 *
 * @author Anna Welker
 */
public abstract class RTLeaf implements RTExpression {

  /**
   * the leaf's type
   */
  String type = "Object";
  String content;

  @Override
  public abstract String getType();
}
