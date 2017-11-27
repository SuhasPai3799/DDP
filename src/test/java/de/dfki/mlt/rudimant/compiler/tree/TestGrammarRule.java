package de.dfki.mlt.rudimant.compiler.tree;

import static de.dfki.mlt.rudimant.compiler.Visualize.*;
import static de.dfki.mlt.rudimant.compiler.tree.TstUtils.*;
import static org.junit.Assert.*;

import org.junit.*;

public class TestGrammarRule {

  @BeforeClass
  public static void setUpClass() {
    setUpEmpty();
  }

  private int extractSize(String s) {
    String pref = "new boolean[";
    int pos = s.indexOf(pref);
    assertTrue(pos >= 0);
    int endpos = s.indexOf(']', pos);
    int n = Integer.parseInt(s.substring(pos + pref.length(), endpos));
    return n;
  }

  @Test
  public void testNoBaseTerms() {
    String in = "r: if ((1 == 1 && (2 != 3 || 2 != 4))) { int i = 7; }";
    assertEquals(4, extractSize(generate(in)));
  }

  @Test
  public void testNoBaseTerms2() {
    String in = "Quiz quiz; "
        + "r: if ((quiz.status == \"running\""
        + " && ! myLastDA() >= #Request(Turning, theme=Tablet))) { int i = 7; }";
    assertEquals(3, extractSize(generate(in)));
  }

  @Test
  public void testNoBaseTerms3() {
    String in = "r: if ((1 == 1 && (2 != 3 || ! 2 != 4))) { int i = 7; }";
    assertEquals(4, extractSize(generate(in)));
  }

  @Test
  public void testNoBaseTerms4() {
    String in = "Quiz q; boolean childAtHome(); "
        + "r: if (childAtHome() && quiz.status == \"wait_for_tablet\") { int i = 7; }";
    assertEquals(3, extractSize(generate(in)));
  }

  @Test
  public void testNoBaseTerms5() {
    String in = "Quiz q; boolean childAtHome(); "
        + "r: if (childAtHome() && childAtHome()) { int i = 7; }";
    assertEquals(3, extractSize(generate(in)));
  }

  @Test
  public void testNoBaseTerms6() {
    String in = "Quiz q; boolean childAtHome(); "
        + "r: if (quiz.status == \"wait_for_tablet\" && childAtHome()) { int i = 7; }";
    assertEquals(3, extractSize(generate(in)));
  }

  @Test
  public void testGenerateAssigns() {
    String in = "Quiz quiz; r: if ((quiz.status == \"running\" && ! myLastDA() >= #Request(Turning, theme=Tablet))) { int i = 7; }";
    String s = generate(in);
    assertTrue(s.contains("__x0[0] ="));
    assertTrue(s.contains("__x0[1] ="));
    assertTrue(s.contains("__x0[2] ="));
  }

  @Test
  public void testGenerateAssigns2() {
    String in = "r: if ((1 == 1 && (2 != 3 || ! 2 != 4))) { int i = 7; }";
    String s = generate(in);
    assertTrue(s.contains("__x0[1] = 1 == 1"));
    assertTrue(s.contains("__x0[2] = 2 != 3"));
    assertTrue(s.contains("__x0[3] = 2 != 4"));
  }

  @Test
  public void testGenerateAssigns3() {
    String in = "boolean childAtHome(); r: if (childAtHome() && childAtHome()) { int i = 7; }";
    String s = generate(in);
    assertTrue(s.contains("__x0[1] = childAtHome()"));
    assertTrue(s.contains("__x0[2] = childAtHome()"));
  }

  @Test
  public void testGenerateAssigns4() {
    String in = "Object childAtHome(); r: if (childAtHome() && childAtHome()) { int i = 7; }";
    String s = generate(in);
    assertTrue(s.contains("__x0[1] = childAtHome() != null"));
    assertTrue(s.contains("__x0[2] = childAtHome() != null"));
  }

  @Test
  public void testGenerateAssigns5() {
    String in = "Child c; r: if (c <= Actor && c.forename) { int i = 7; }";
    String s = generate(in);
    assertTrue(s.contains("__x0[1] = c.getClazz().isSubclassOf(getRdfClass(\"Actor\"))"));
    assertTrue(s.contains("__x0[2] = (c != null && exists(((String)c.getSingleValue(\"<dom:forename>\"))))"));
  }

  @Test
  public void testGenerateAssigns6() {
    String in = "int j; boolean b; "
        + "r: if (j && lastDA() <= #Confirm(Frame) && b && x != y) { int i = 7; }";
    String s = generate(in);
    assertTrue(s.contains("__x0[1] = j != 0"));
    assertTrue(s.contains("__x0[2] = lastDA().subsumes(new DialogueAct(\"Confirm\", \"Frame\"))"));
    assertTrue(s.contains("__x0[3] = b"));
    assertTrue(s.contains("__x0[4] = ! (x == y)"));
  }

  @Test
  public void testGenerateAssigns7() {
    String in = "Agent user; r: if (! user) { int i = 7; }";
    String s = generate(in);
    assertEquals(2, extractSize(s));
    assertTrue(s.contains("__x0[1] = user != null"));
  }
}
