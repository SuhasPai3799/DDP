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
	public static String answerDeptCourseCount(String dept_name)
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
	/**
	 * Given a department name, return the name of a close department name as stored in the database.
	 * @param dept_name Name of the department for which we are trying to find the closest possible name in the database.
	 * @return "NULL" if no department is found close enough.
	 */
	public static String getClosestDept(String dept_name)
	{
		
		for (Map.Entry mapElement : dept_syns.entrySet()) {
			String dept_official_name = (String)mapElement.getKey();
			List<String> dept_name_syns = (List)mapElement.getValue();
			for(String syn: dept_name_syns)
			{
				if((syn.toLowerCase()).equals(dept_name.toLowerCase()))
				{
					return dept_official_name;
				}
			}
			
			logger.log(Level.INFO, dept_official_name);
		}
		return "NULL";
	}
	/**
	 * Given the name of a course, return the details of that particular course
	 * @param c_name
	 * @return
	 */
	public static String answerCourseInfo(String c_name)
	{
		String c_name_uri = "<univ:" + c_name + ">";
		if(c_name.matches("[a-zA-Z][a-zA-Z][0-9]+"))
		{
			String check_course = c_name.toLowerCase();
			String query = "select ?a where ?a <rdf:type> <univ:Courses> ?d";
			List<Object> res = _agent._proxy.query(query);
			

			for(Object course: res)
			{
				String course_id = String.valueOf(course);
				
				if(c_name_uri.toLowerCase().equals(course_id.toLowerCase()))
				{	
					logger.log(Level.INFO, "Working");
					String course_comment_query = String.format("select ?a where %s <rdfs:comment> ?a ?x", course_id);
					List<Object> comments = _agent._proxy.query(course_comment_query);
					return (String)(comments.get(0));
				}
			}
		}
		return "Course Info";
	}

	// public static List<String> answerDeptCoursesOffered(String dept_name)
	// {
	// 	String closest_dept_name = getClosestDept(dept_name)
	// 	if(closest_dept_name == "NULL")
	// 	{
	// 		return "-1";
	// 	}
	// 	String dept_uri 
	// }
	public HFCUtils(ChatAgent agent)
	{
		this._agent = agent;
	}
}