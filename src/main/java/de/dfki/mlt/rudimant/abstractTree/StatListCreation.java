/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant.abstractTree;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Anna Welker, anna.welker@dfki.de
 */
public class StatListCreation extends RTStatement {

  //RTExpression left;
  ArrayList<RTExpression> objects;
  String origin;
  String variableName;
  String listType;

  public StatListCreation(String variableName, ArrayList<RTExpression> objects,
          String origin) {
    this.objects = objects;
    this.origin = origin;
    this.variableName = variableName;
  }

  public StatListCreation(String variableName, ArrayList<RTExpression> objects,
          String origin, String listType) {
    this.objects = objects;
    this.origin = origin;
    this.variableName = variableName;
    this.listType = listType;
  }

  @Override
  public void visit(RudiVisitor v) {
    v.visitNode(this);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 17 * hash + Objects.hashCode(this.objects);
    hash = 17 * hash + Objects.hashCode(this.variableName);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final StatListCreation other = (StatListCreation) obj;
    if (!Objects.equals(this.variableName, other.variableName)) {
      return false;
    }
    if (!Objects.equals(this.objects, other.objects)) {
      return false;
    }
    return true;
  }

}
