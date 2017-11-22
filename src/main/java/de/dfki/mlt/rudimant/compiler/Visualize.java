package de.dfki.mlt.rudimant.compiler;

import static de.dfki.mlt.rudimant.compiler.Constants.RULE_FILE_EXT;

import java.io.*;

import de.dfki.lt.hfc.WrongFormatException;
import de.dfki.lt.loot.gui.DrawingPanel;
import de.dfki.lt.loot.gui.MainFrame;
import de.dfki.lt.loot.gui.Style;
import de.dfki.lt.loot.gui.layouts.CompactLayout;
import de.dfki.lt.loot.gui.util.ObjectHandler;
import de.dfki.mlt.rudimant.common.BasicInfo;
import de.dfki.mlt.rudimant.common.RuleInfo;
import de.dfki.mlt.rudimant.compiler.tree.GrammarFile;
import de.dfki.mlt.rudimant.compiler.tree.RudiTree;
import de.dfki.mlt.rudimant.compiler.tree.TreeModelAdapter;


public class Visualize extends CompilerMain {

  static String header = "";
  static String footer = "";

  public static void setUp(String configFile, String h, String f)
      throws FileNotFoundException {
    readConfig(configFile);
    header = h;
    footer = f;
  }

  public static InputStream getInput(String input) {
    String toParse = header + input + footer;
    return new ByteArrayInputStream(toParse.getBytes());
  }

  private static RudimantCompiler initRc()
      throws IOException, WrongFormatException {
    RuleInfo.resetIdGenerator();
    RudimantCompiler rc = init(confDir, configs);
    String[] pkg = {};
    rc.getMem().enterClass("Test", pkg, null);
    rc.readAgentSpecs("Test");
    return rc;
  }

  public static String generate(String in, boolean show) {
    RudimantCompiler rc;
    try {
      rc = initRc();
      if (show) rc.showTree();
      StringWriter sw = new StringWriter();
      parseAndGenerate(rc, getInput(in), sw, "test");
      sw.flush();
      return sw.toString();
    } catch (IOException | WrongFormatException e) {
      throw new RuntimeException(e);
    }
  }

  public static String generate(String in) { return generate(in, false); }

  public static BasicInfo generateAndGetRulesInfo(File input) {
    RudimantCompiler rc;
    try {
      RuleInfo.resetIdGenerator();
      rc = init(confDir, configs);
      rc.processToplevel(input);
    } catch (IOException | WrongFormatException e) {
      throw new RuntimeException(e);
    }
    return rc.getMem().getInfo();
  }

  public static void show(RudiTree root, String realName, MainFrame mf) {
    CompactLayout cl = new CompactLayout();
    cl.setTreeHorizontal(true);

    DrawingPanel dp = new DrawingPanel(root, cl, new TreeModelAdapter());
    mf.setContentArea(dp);
    mf.setTitle(realName);
  }

  public static void show(RudiTree root, String realName) {
    MainFrame mf = new MainFrame("foo");
    mf.addFileAssociation(new RudiFileHandler(), "rudi");
    show(root, realName, mf);
  }

  public static class RudiFileHandler implements ObjectHandler {
    public boolean process(File f, InputStream in, MainFrame mf)
        throws IOException {
      String inputRealName = f.getName().replace(RULE_FILE_EXT, "");

      // create the abstract syntax tree
      GrammarFile gf = null;

      // do the type checking
      // create the abstract syntax tree
      gf = parseAndTypecheck(in);

      // show tree
      show(gf, inputRealName, mf);
      return true;
    }
  }

  public static void init() {
    Style.increaseDefaultFontSize(1.5);
  }

  public static GrammarFile parseAndTypecheck(InputStream in) {
    try {
      // create the abstract syntax tree
      RudimantCompiler rc = initRc();
      GrammarFile result = GrammarFile.parseAndTypecheck(rc, in, "test");
      return result;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static GrammarFile parseAndTypecheck(String in) {
    return parseAndTypecheck(getInput(in));
  }

  private static GrammarFile parseAndGenerate(RudimantCompiler rc,
      InputStream in, Writer out, String name) throws IOException {
    GrammarFile gf = GrammarFile.parseAndTypecheck(rc, in, name);
    if (gf == null)
      throw new UnsupportedOperationException("Parsing failed.");
    if (rc.visualise())
      Visualize.show(gf, name);
    gf.generate(rc, out);
    return gf;
  }

  public static GrammarFile parseAndTypecheckWithError(InputStream in, Writer out)
      throws Throwable {
    try {
      // create the abstract syntax tree
      RudimantCompiler rc = initRc();
      rc.throwTypeErrors();
      GrammarFile result = parseAndGenerate(rc, in, out, "test");
      return result;
    } catch (RuntimeException ex) {
      if (ex.getCause() instanceof TypeException)
        throw ex.getCause();
      else
        throw ex;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static GrammarFile parseAndTypecheckWithError(String in, Writer out)
      throws Throwable {
    return parseAndTypecheckWithError(getInput(in), out);
  }

  /**
   * @param args: the file that should be parsed without ending (in args[0])
   * @throws FileNotFoundException
   * @throws WrongFormatException
   * @throws Exception
   */
  public static void main(String[] args) {

    File inputFile = new File(args[0]);

    try {
      if (args.length > 1) { readConfig(args[1]); }

      Style.increaseDefaultFontSize(1.5);
      MainFrame root = new MainFrame("foo");
      root.addFileAssociation(new RudiFileHandler(), "rudi");
      new RudiFileHandler().process(inputFile, new FileInputStream(inputFile),
          root);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
