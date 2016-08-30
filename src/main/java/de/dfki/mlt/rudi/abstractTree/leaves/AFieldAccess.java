/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudi.abstractTree.leaves;

import de.dfki.mlt.rudi.abstractTree.*;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 *
 * @author anna
 */
public class AFieldAccess  extends AbstractLeaf{

  private String type;
  private ArrayList<String> representation;

  public AFieldAccess(String type, ArrayList<String> representation) {
    this.type = type;
    this.representation = representation;
  }

  @Override
  public void testType() {
    // nothing to do
  }

  @Override
  public void generate(Writer out) throws IOException{
    out.append("Rdf here\n");
    //out.append(this.representation);
  }

  @Override
  public String getType() {
    return this.type;
  }
}
