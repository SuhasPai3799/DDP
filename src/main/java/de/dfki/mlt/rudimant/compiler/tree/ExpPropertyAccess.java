/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.compiler.tree;

import java.util.Arrays;

import de.dfki.mlt.rudimant.compiler.Type;

/**
 * this represents an access to the ontology (will result in an rdf object in
 * output)
 *
 * @author Anna Welker
 */
public class ExpPropertyAccess extends RTExpLeaf {

  ExpVariable label;
  boolean propertyVariable = false;
  Type rangeType;
  boolean functional;


  public ExpPropertyAccess(String fullexp, ExpVariable l, boolean var, Type rt,
      boolean func) {
    // an access will always return sth of type Object, so to not get null
    // I'll set the type of this to Object by default
    type = rt;
    label = l;
    propertyVariable = var;
    rangeType = rt;
    functional = func;
    this.fullexp = fullexp;
  }

  @Override
  public void visit(RudiVisitor v) {
    v.visitNode(this);
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
