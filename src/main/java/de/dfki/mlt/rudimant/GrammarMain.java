/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.mlt.rudimant;

import static de.dfki.mlt.rudimant.Constants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.yaml.snakeyaml.Yaml;

import de.dfki.lt.hfc.WrongFormatException;
import de.dfki.lt.hfc.db.client.HfcDbClient;
import de.dfki.lt.hfc.db.rdfProxy.RdfProxy;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * the main class of rudimant
 *
 * @author Anna Welker
 */
public class GrammarMain {

  private static String help = "Hello, this is rudimant.\n"
          + "Currently, the following flags are available:\n"
          + "-l\tTranscribe file in logmode. Text will be added so that "
          + "the outcome of\n"
          + "\tall boolean expressions is being logged as "
          + "soon as they are evaluated.\n"
          + "-e\tDo not crash if there are .rudi files that cannot be translated\n"
          + "-d\tDo not try to do type checking while translating\n"
          + "\n\nPlease use this tool as follows:\n"
          + "java rudimant <directory_to_be_searched/> <paht_of_config_file/> OPTIONS\n"
          + "-o=DIRECTORY\tSpecify DIRECTORY as the output directory";

  private Map<String, Object> configs;

  private RudimantCompiler rc;
  // rdf functionality

  public static RdfProxy startClient(Map<String, Object> configs)
      throws IOException, WrongFormatException, TException {
    HfcDbClient client = new HfcDbClient();
    client.init((String) configs.get(CFG_SERVER_HOST),
        (int) configs.get(CFG_SERVER_PORT));
    /*
    String ontoFileName = (String) configs.get(CFG_ONTOLOGY_FILE);
    if (ontoFileName == null) {
      throw new IOException("Ontology file is missing.");
    }
    client.readConfig(new File(ontoFileName));
    */
    return new RdfProxy(client._client);
  }

  public static RudimantCompiler initCompiler(Map<String, Object> configs)
      throws IOException, WrongFormatException, TException {
    RdfProxy proxy = startClient(configs);
    if (configs.containsKey(CFG_NAME_TO_URI)) {
      proxy.setBaseToUri((Map<String, String>)configs.get(CFG_NAME_TO_URI));
    }
    RudimantCompiler rc = new RudimantCompiler((String)configs.get(CFG_WRAPPER_CLASS),
        (String)configs.get(CFG_TARGET_CONSTRUCTOR),
        proxy);
    rc.setLog((boolean)
        (configs.get(CFG_LOG) == null ? false : configs.get(CFG_LOG)));
    rc.setThrowExceptions((boolean)configs.get(CFG_TYPE_ERROR_FATAL));
    rc.setTypeCheck((boolean)configs.get(CFG_TYPE_CHECK));
    if (configs.containsKey(CFG_PACKAGE)) {
      rc.setPackageName((String) configs.get(CFG_PACKAGE));
    }
    return rc;
  }

  public static void process(RudimantCompiler rc,
      List<String> files, File outputDirectory)
      throws IOException, WrongFormatException, TException {
    if (! outputDirectory.exists()) {
      if (! outputDirectory.mkdirs()) {
        throw new IOException("Output directory could not be created: "
            + outputDirectory.getAbsolutePath());
      }
    } else {
      if (! outputDirectory.isDirectory()) {
        throw new IOException("Output directory is not a directory: "
            + outputDirectory.getAbsolutePath());
      }
    }
    for (String file : files) {
      rc.process(new File(file), outputDirectory);
    }
  }

  /** For unit tests */
  public void setConfig(Map<String, Object> conf) {
    configs = conf;
  }

  final static Object [][] defaults = {
    { CFG_SERVER_HOST, "localhost" , "s"},
    { CFG_SERVER_PORT, 9000 , null },
    { CFG_LOG, false , "l"},
    { CFG_TYPE_ERROR_FATAL, true, "e" },
    { CFG_TYPE_CHECK, true , "d" },
    { CFG_TARGET_CONSTRUCTOR, "", "CA" }
  };

  public Map<String, Object> defaultConfig() {
    configs = new LinkedHashMap<String, Object>();
    for (Object[] pair : defaults) {
      configs.put((String)pair[0], pair[1]);
    }
    return configs;
  }

  static Object getDefault(String key) {
    Object result = null;
    for (Object[] pair : defaults) {
      if (key.equals(pair[0])) {
        result = pair[1];
        break;
      }
    }
    return result;
  }

  static void setDefault(String key, Map<String, Object> configs) {
    if (! configs.containsKey(key)) {
      configs.put(key, getDefault(key));
    }
  }

  static void setValue(String key, String option, Object def,
      Map<String, Object> configs, OptionSet options) {
    if (options.has(option)) {
      if (options.valueOf(option) == null) {
        configs.put(key, ! (boolean) def);
      } else {
        configs.put(key, (String) options.valueOf(option));
      }
    } else if (! configs.containsKey(key)) {
      configs.put(key, def);
    }
  }

  /**
   *
   * @param args: the file that should be parsed without ending (in args[0])
   *             + the complete path of the configfile
   * @throws Exception
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void main(String[] args)
      throws IOException, TTransportException, FileNotFoundException,
      WrongFormatException, TException {
    // BasicConfigurator.resetConfiguration();
    // BasicConfigurator.configure();

    OptionParser parser = new OptionParser("hleds:r:w:c:p:o:");
    parser.accepts("help");
    OptionSet options = null;

    GrammarMain main = new GrammarMain();
    Map<String, Object> configs;
    int port = (Integer)getDefault(CFG_SERVER_PORT);
    File outputDirectory = null;
    List files = null;

    try {
      options = parser.parse(args);
      files = options.nonOptionArguments();

      if (files.isEmpty()) {
        usage("Input file is missing");
      }

      outputDirectory = new File((String)files.get(0)).getParentFile();

      if (options.has("help") || options.has("h")) {
        System.out.println(help);
        System.exit(0);
      }
      if (options.has("c")) {
        Yaml yaml = new Yaml();
        configs = (Map<String, Object>)
            yaml.load(new FileReader((String)options.valueOf("c")));
      } else {
        configs = main.defaultConfig();
      }

      for (Object[] val : defaults) {
        if (val[2] != null) {
          setValue((String)val[0], (String)val[2], val[1], configs, options);
        }
      }

      if (options.has("w")) {
        configs.put(CFG_WRAPPER_CLASS, options.valueOf("w"));
      }
      if (options.has("r")) {
        configs.put(CFG_ONTOLOGY_FILE, options.valueOf("r"));
      }
      if (options.has("o")) {
        configs.put(CFG_OUTPUT_DIRECTORY, options.valueOf("o"));
      }
      if (options.has("p")) {
        port = Integer.parseInt((String)options.valueOf("p"));
      } else {
        if (configs.containsKey(CFG_SERVER_PORT)) {
          Object p = configs.get(CFG_SERVER_PORT);
          if (p instanceof Integer) {
            port = (Integer)p;
          } else if (p instanceof String) {
            port = Integer.parseInt((String) p);
          }
        }
      }
      configs.put(CFG_SERVER_PORT, port);
      if (configs.containsKey(CFG_OUTPUT_DIRECTORY)) {
        outputDirectory = new File((String)configs.get(CFG_OUTPUT_DIRECTORY));
      }
      main.setConfig(configs);
      initCompiler(configs);
      process(initCompiler(configs), files, outputDirectory);
    } catch (OptionException ex) {
      usage("Error parsing options: " + ex.getLocalizedMessage());
      System.exit(1);
    } catch (NumberFormatException nex) {
      usage("Argument of -p (port) must be a number");
      System.exit(1);
    }

  }

  private static void usage(String message) {
    System.out.println(message);
    System.out.println("Please use this tool as follows: "
        + "java rudimant <directory_to_be_searched/> <output_directory/>? OPTIONS\n"
        + "For help see rumdimant -help\n");
    System.exit(-1);
  }

}
