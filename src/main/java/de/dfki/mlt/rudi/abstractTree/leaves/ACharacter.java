/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudi.abstractTree.leaves;

import de.dfki.mlt.rudi.abstractTree.*;

/**
 *
 * @author anna
 */
public class ACharacter  implements AbstractTree, AbstractExpression, AbstractLeaf{
  
  private String content;

  public ACharacter(String content) {
    this.content = content;
  }

  @Override
  public void testType() {
    // everything okay
  }
  
  @Override
  public String toString(){
    return "\'" + this.content + "\'";
  }

  @Override
  public String getType() {
    return "char";
  }
  
}