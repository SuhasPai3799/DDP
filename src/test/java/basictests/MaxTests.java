/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package basictests;

import visitortests.*;
import java.io.IOException;

import org.apache.thrift.transport.TTransportException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dfki.lt.hfc.WrongFormatException;
import de.dfki.mlt.rudimant.GrammarMain;

/**
 *
 * @author mawo
 */
public class MaxTests {

  private static final String RESOURCE_DIR = "src/test/resources/";


  private String[] enterName(String filename){
    String[] name = {"-c", "src/test/resources/rudi.config.yml", "-d",
    "src/test/resources/basic/" + filename};

    return name;
  }

  @BeforeClass
  public static void setUpClass()
    throws TTransportException, IOException, WrongFormatException {
    SeriousTest.setUpClass();
  }

  @AfterClass
  public static void tearDownClass() {
    SeriousTest.tearDownClass();
  }

  @Test
  public void maxTest1() throws Exception {
//    String[] strings = new String[]{
//      "-c", "src/test/resources/rudi.config.yml", "-d",
//      "src/test/resources/MiniTest.rudi",};

    // TODO: enter user.GivenName into ontology to make this work
    GrammarMain.main(enterName("MaxTest1.rudi"));
  }

  @Test
  public void maxTest2() throws Exception {
    // TODO: rudimant needs to know what gameLogic.isTurnBased() is supposed to be
    GrammarMain.main(enterName("MaxTest2.rudi"));
  }
}