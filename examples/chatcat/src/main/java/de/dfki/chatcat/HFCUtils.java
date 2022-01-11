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

import static de.dfki.chatcat.ConstUtils.*;





public class HFCUtils{
	private int testVal;
	static ChatAgent _agent;
	public static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static String testFunc(String dept_name)
	{
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name == "NULL")
		{
			return "-1";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
		String query = String.format("select ?a where ?a <univ:offeredBy> %s ?d", dept_uri );
		List<Object> res = _agent._proxy.query(query);
		return String.valueOf(res.size());
		
	}
	public static String getClosestDept(String dept_name)
	{
		logger.log(Level.INFO, "Hello");
		for (Map.Entry mapElement : dept_syns.entrySet()) {
			String dept_official_name = (String)mapElement.getKey();
			List<String> dept_name_syns = (List)mapElement.getValue();
			for(String syn: dept_name_syns)
			{
				if(syn.equals(dept_name))
				{
					return dept_official_name;
				}
			}
			
			logger.log(Level.INFO, dept_official_name);
		}
		return "NULL";
	}
	public HFCUtils(ChatAgent agent)
	{
		this._agent = agent;
	}
}