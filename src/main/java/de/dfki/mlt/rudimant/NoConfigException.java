/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant;

/**
 *
 * @author pal
 */
public class NoConfigException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NoConfigException(String message) {
    super(message);
  }
}
