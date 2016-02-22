/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Versuch2.abstractTree.leafs;

import Versuch2.abstractTree.AbstractLeaf;
import Versuch2.abstractTree.AbstractTree;
import Versuch2.abstractTree.AbstractType;

/**
 * class that handles comments, please don't use this as one side in an expression!
 * @author anna
 */
public class AComment implements AbstractTree, AbstractLeaf{

  private String comment;

  public AComment(String comment) {
    this.comment = comment;
  }
  
  @Override
  public String toString(){
    return this.comment;
  }

  @Override
  public AbstractType getType() {
    return AbstractType.NONE;
  }

  @Override
  public void testType() {
    // no type to be tested
  }
}
