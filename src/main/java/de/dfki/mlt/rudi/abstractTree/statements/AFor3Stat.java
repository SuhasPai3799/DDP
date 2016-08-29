/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudi.abstractTree.statements;

import java.util.List;

import de.dfki.mlt.rudi.abstractTree.AbstractStatement;
import de.dfki.mlt.rudi.abstractTree.AbstractTree;

/**
 * FOR LPAR LPAR VARIABLE ( COMMA VARIABLE )+ RPAR COLON exp RPAR loop_statement_block
 * @author anna
 */
public class AFor3Stat implements AbstractStatement, AbstractTree{
  
  private List<String> variables;
  private AbstractTree exp;
  private AbstractBlock block;

  public AFor3Stat(List<String> variables, AbstractTree exp, AbstractBlock block) {
    this.variables = variables;
    this.exp = exp;
    this.block = block;
  }
  
  @Override
  public void testType() {
    // no types for statements
  }
  
  @Override
  public String generate(Writer out){
    String ret = "for (Object[] o : " + this.exp + ") {";
    int count = 0;
    for (String s : this.variables){
      ret += "\nObject " + s + " = o[" + count + "]";
    }
    return ret + block + "}";
  }
}
