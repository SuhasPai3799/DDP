/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.tree;

import java.util.Arrays;

import de.dfki.mlt.rudimant.Type;

/**
 * this is either a variable declaration, or an assignment of a variable to a
 * new value. Most of the type checking rudimant currently does happens here.
 *
 * @author Anna Welker
 */
public class ExpAssignment extends RTExpression {

  RTExpression left; // can be either a UVariable or a Field access
  RTExpression right;
  boolean declaration;

  public ExpAssignment(RTExpression l, RTExpression r) {
    left = l;
    right = r;
    declaration = false;
  }

  /** This is called in case this is a combined variable declaration with
   * initial assignment
   * @param actualType the declared type
   * @param l
   * @param r
   * @param position
   */
  public ExpAssignment(String actualType, RTExpression l, RTExpression r) {
    this(l, r);
    declaration = true;
    type = new Type(actualType);
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
    RudiTree[] dtrs = { left, right };
    return Arrays.asList(dtrs);
  }

  public void propagateType(Type upperType) {
    if (type != null) {
      logger.error("Why didn't this type percolate up? " + fullexp + " " + type);
      return;
    }
    type = upperType;
    right.propagateType(upperType);
  }
}
