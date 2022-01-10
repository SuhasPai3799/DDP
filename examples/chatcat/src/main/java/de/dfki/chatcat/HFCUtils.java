package de.dfki.chatcat;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


import de.dfki.lt.hfc.WrongFormatException;
import de.dfki.lt.hfc.db.rdfProxy.Rdf;
import de.dfki.lt.hfc.db.rdfProxy.RdfProxy;
import de.dfki.lt.hfc.db.server.HandlerFactory;
import de.dfki.lt.hfc.db.server.HfcDbHandler;
import de.dfki.lt.hfc.db.server.HfcDbServer;
import de.dfki.mlt.rudimant.agent.Agent;
import de.dfki.mlt.rudimant.agent.Behaviour;
import de.dfki.mlt.rudimant.agent.DialogueAct;


import de.dfki.lt.hfc.db.QueryException;
import de.dfki.lt.hfc.db.QueryResult;
import de.dfki.lt.hfc.types.XsdAnySimpleType;
import de.dfki.lt.hfc.types.XsdDateTime;
import de.dfki.lt.loot.jada.Pair;




public class HFCUtils{
	private int testVal;
	static ChatAgent _agent;
	public static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static String testFunc()
	{

		String query = "select ?a where ?a <univ:offeredBy> ?c ?d";
		
		if(_agent._proxy == null)
		{

			System.out.println("Agent proxy null");
		}
		else
		{
			System.out.println("Works agent proxy");
			List<Object> res = _agent._proxy.query(query);
			for(Object o : res)
			{	
				if(o!= null)
				{
					System.out.println(String.valueOf(o));
				}
			}
			logger.log(Level.INFO,"Not Null");
			
		}
		
		
    	
		return "5";
	}
	public HFCUtils(ChatAgent agent)
	{
		this._agent = agent;
	}
}