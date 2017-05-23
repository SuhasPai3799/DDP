package de.dfki.mlt.rudimant.abstractTree;

import java.util.ArrayList;
import java.util.List;

public class StatExpression extends RTStatement {

  RTExpression expression;

  public StatExpression(RTExpression exp) {
    expression = exp;
  }

  @Override
  public void visit(RTStatementVisitor v) {
    v.visitNode(this);
  }

  @Override
  public String visitStringV(RTStringVisitor v) {
    return v.visitNode(expression);
  }

  @Override
  public void visitVoidV(VGenerationVisitor v) {
    v.visitNode(this);
  }

  @Override
  public Iterable<? extends RudiTree> getDtrs() {
    List<RudiTree> dtrs = new ArrayList<>(1);
    dtrs.add(expression);
    return dtrs;
  }

}
