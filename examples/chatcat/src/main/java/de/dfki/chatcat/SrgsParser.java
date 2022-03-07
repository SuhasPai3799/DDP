package de.dfki.chatcat;

import static de.dfki.chatcat.Constants.*;
import static de.dfki.chatcat.ConstUtils.*;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import java.util.Scanner;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.NameValuePair;
import java.util.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;


import org.json.JSONObject;

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

  public String rasaNLU(String text)
  {
      CloseableHttpClient httpclient = HttpClients.createDefault();

      //Creating a HttpGet object
      JSONObject json = new JSONObject();
      StringEntity params = null;
      System.out.println(text);
      json.put("text", text);
      try{
      params = new StringEntity(json.toString());
      }
      catch(UnsupportedEncodingException e)
      {
        e.printStackTrace();
      }
      
      HttpPost httppost = new HttpPost("http://localhost:5005/model/parse");
      
      httppost.setHeader("Content-type", "application/json");
      httppost.setEntity(params);
      
    
      //Printing the method used
     
      HttpResponse httpresponse = null;
      Scanner sc = null;
      //Executing the Get request
      try{
      httpresponse = httpclient.execute(httppost);
      }
      catch(IOException e)
      {
        e.printStackTrace();
      }

      try
      {
        sc = new Scanner(httpresponse.getEntity().getContent());
      }
      catch(IOException e)
      {
        e.printStackTrace();
      }

      //Printing the status line
      System.out.println(httpresponse.getStatusLine());
      String jsonResult = "";
      try{
      jsonResult = EntityUtils.toString(httpresponse.getEntity());
      }
      catch(IOException e)
      {
        e.printStackTrace();
      }
      String res = getDialogueAct(jsonResult);
      logger.error(res);
      while(sc.hasNext()) {
         System.out.println(sc.nextLine());
      }
      return res;
  }



  @Override
  public DialogueAct analyse(String text) {
    
    text = clean(text);
    System.out.println(text);
    String rasaDA = rasaNLU(text);
    if(rasaDA.equals("NULL"))
    {
      ;
    }
    else
    { 
      logger.error("Rasa DA being returned");
      return new DialogueAct(rasaDA);
    }
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
        logger.error(sb.toString());
        return new DialogueAct(sb.toString());
      }
    }
    catch (GrammarException ex) {
      logger.error(ex.toString());
    }
    return new DialogueAct(noDA());
  }

}
