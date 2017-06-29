/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.tree;

import java.util.Arrays;

import de.dfki.mlt.rudimant.Type;

/**
 * this represents an access to the ontology (will result in an rdf object in
 * output)
 *
 * @author Anna Welker
 */
public class ExpUPropertyAccess extends RTExpLeaf {

  ExpUVariable label;
  boolean propertyVariable = false;
  Type rangeType;
  boolean functional;


  public ExpUPropertyAccess(String fullexp, ExpUVariable l, boolean var, String rt,
      boolean func) {
    // an access will always return sth of type Object, so to not get null
    // I'll set the type of this to Object by default
    if(rt == null){
      type = new Type("Object");
    } else {
      type = new Type(rt);
    }
    label = l;
    propertyVariable = var;
    rangeType = new Type(rt);
    functional = func;
    this.fullexp = fullexp;
  }

  @Override
  public void visit(RTExpressionVisitor v) {
    v.visitNode(this);
  }

  /**
   * if we are an expression but this method is called, we should write to out;
   * it means that the instance calling us must be a statement
   * @param v
   */
  @Override
  public void visitVoidV(VGenerationVisitor v) {
    v.out.append(v.visitNode(this));
  }

  @Override
  public String visitStringV(RTStringVisitor v){
    return v.visitNode(this);
  }

  public Iterable<? extends RudiTree> getDtrs() {
    RudiTree[] dtrs = { label };
    return Arrays.asList(dtrs);
  }

  String getPropertyName() {
    String ret = "";
    if(!propertyVariable) ret += ('"');
    ret += (label.content);
    if(!propertyVariable) ret += ('"');
    return ret;
  }

  public String toString() {
    return (propertyVariable ? ".getV(" + label.content + ")"
        : "." + label.content) +
        (type != null ? "[" + type + "]" : "");
  }
}
