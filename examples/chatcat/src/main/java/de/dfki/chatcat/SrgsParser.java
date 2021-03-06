package de.dfki.chatcat;

import static de.dfki.chatcat.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.jvoicexml.processor.srgs.ChartGrammarChecker;
import org.jvoicexml.processor.srgs.ChartGrammarChecker.ChartNode;
import org.jvoicexml.processor.srgs.JVoiceXmlGrammarManager;
import org.jvoicexml.processor.srgs.grammar.GrammarException;
import org.jvoicexml.processor.srgs.grammar.Grammar;

import de.dfki.mlt.rudimant.agent.DialogueAct;
import de.dfki.mlt.rudimant.agent.nlg.Interpreter;
import de.dfki.mlt.srgsparser.JSInterpreter;

public class SrgsParser extends Interpreter {
  JVoiceXmlGrammarManager manager;
  Grammar ruleGrammar;
  ChartGrammarChecker checker;

  @SuppressWarnings("rawtypes")
  @Override
  public boolean init(File configDir, String language, Map config) {
    String grammarName = (String)config.get(CFG_SRGS_GRAMMAR);
    if (grammarName == null) return false;
    try {
      manager = new JVoiceXmlGrammarManager();
      ruleGrammar = manager.loadGrammar(new File(configDir, grammarName).toURI());
      checker = new ChartGrammarChecker(manager);
    } catch (IOException | GrammarException ex){
      logger.error("Could not read grammar file {} because of {}",
          new File(configDir, grammarName), ex.toString());
      return false;
    };
    return true;
  }
  public String noDA()
  {
    return "OutOfInputSpace(top)";
  }
  public String clean(String text)
  {
    text = text.replace("?", "");
    text = text.replace("!", "");
    text = text.replace("."," ");
    text = text.replace(","," ");
    return text;
  }
  @Override
  public DialogueAct analyse(String text) {
    
    text = clean(text);
    System.out.println(text);
    String[] tokens = text.split(" +");
    try {
      //TODO: Find out why no validRule is returned
      ChartNode validRule = checker.parse(ruleGrammar, tokens);
      if (validRule != null) {
        JSInterpreter walker = new JSInterpreter(checker);
        JSONObject object = walker.evaluate(validRule);
        String da = object.getString(DA_SLOT);
        if (da == null) return null;
        String prop = object.getString(PROP_SLOT);
        if (prop == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(da).append('(').append(prop);
        for (String key : object.keySet()) {
          if (! key.equals(DA_SLOT) && !key.equals(PROP_SLOT)) {
            sb.append(", ").append(key)
              .append("=\"").append(object.get(key)).append('"');
          }
        }
        sb.append(')');
        
        return new DialogueAct(sb.toString());
      }
    }
    catch (GrammarException ex) {
      logger.error(ex.toString());
    }
    return new DialogueAct(noDA());
  }

}
