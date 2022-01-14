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

	public static String getNameFromURI(String uri)
	{
		return uri.split(":")[1].split(">")[0];
	}
	public static String cleanXSD(String input)
	{
		return input.split("\"")[1];
	}
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
	 * @param c_name Name of the course for which details are being returned
	 * @return String containing the label for the course as described in the ontology
	 */
	public static String answerCourseInfo(String c_name)
	{
		String c_name_uri = "<univ:" + c_name + ">";
		if(c_name.matches("[a-zA-Z][a-zA-Z][0-9]+"))
		{
			
			String query = "select ?a where ?a <rdf:type> <univ:Courses> ?d";
			List<Object> res = _agent._proxy.query(query);
			

			for(Object course: res)
			{
				String course_id = String.valueOf(course);
				
				if(c_name_uri.toLowerCase().equals(course_id.toLowerCase()))
				{	
					
					String course_comment_query = String.format("select ?a where %s <rdfs:comment> ?a ?x", course_id);
					List<Object> comments = _agent._proxy.query(course_comment_query);
					return (String)(comments.get(0));
				}
			}
		}
		else
		{
			String query = "select ?a ?b "
						+  " where " +  " ?a <rdf:type> <univ:Courses> ?_ " 
						+  " & ?a <rdfs:label> ?b ?_ ";
			QueryResult res = _agent._proxy.selectQuery(query);
			for(List<String> row: res.getTable().getRows())
			{
				String course_label = row.get(1).toLowerCase();
				if(course_label.contains(c_name.toLowerCase()))
				{
					String course_id = row.get(0);
					String course_comment_query = String.format("select ?a where %s <rdfs:comment> ?a ?x", course_id);
					List<Object> comments = _agent._proxy.query(course_comment_query);
					return (String)(comments.get(0));
				}

			}
		}
		return "No such course exists.";
	}

	public static String answerCourseTeacherInfo(String c_name)
	{
		String c_name_uri = "<univ:" + c_name + ">";
		if(c_name.matches("[a-zA-Z][a-zA-Z][0-9]+"))
		{
			
			String query = "select ?a where ?a <rdf:type> <univ:Courses> ?d";
			List<Object> res = _agent._proxy.query(query);
			

			for(Object course: res)
			{
				String course_id = String.valueOf(course);
				
				if(c_name_uri.toLowerCase().equals(course_id.toLowerCase()))
				{	
					logger.log(Level.INFO, course_id);
					String course_teacher_query = String.format("select ?b where ?a <univ:teaches> %s ?_ & ?a <rdfs:label> ?b ?_", course_id);
					List<Object> course_teacher = _agent._proxy.query(course_teacher_query);
					logger.log(Level.INFO, String.valueOf((course_teacher.get(0))));
					return String.valueOf((course_teacher.get(0)));
				}
			}
		}
		else
		{
			String query = "select ?a ?b "
						+  " where " +  " ?a <rdf:type> <univ:Courses> ?_ " 
						+  " & ?a <rdfs:label> ?b ?_ ";
			QueryResult res = _agent._proxy.selectQuery(query);
			for(List<String> row: res.getTable().getRows())
			{
				String course_label = row.get(1).toLowerCase();
				if(course_label.contains(c_name.toLowerCase()))
				{
					String course_id = row.get(0);
					String course_teacher_query = String.format("select ?b where ?a <univ:teaches> %s ?_ &  ?a <rdfs:label> ?b ?_", course_id);
					List<Object> course_teacher = _agent._proxy.query(course_teacher_query);
					logger.log(Level.INFO, String.valueOf((course_teacher.get(0))));
					return String.valueOf((course_teacher.get(0)));
				}

			}	
		}
		return "No such course exists.";
	}

	public static String answerProfCourses(String query_prof_name)
	{
		String query_prof_uri = "<univ:" + query_prof_name + ">";
		String query = "select ?a where ?a <rdf:type> <univ:Professors> ?_";
		List<Object> all_profs = _agent._proxy.query(query);
		Boolean flag = false;
		for(Object prof: all_profs)
		{
			String prof_uri = String.valueOf(prof);
			String prof_name = getNameFromURI(prof_uri);
			if(prof_name.toLowerCase().contains(query_prof_name.toLowerCase()))
			{
				String prof_course_query = String.format("select ?a ?b where %s <univ:teaches> ?a ?_  & ?a <rdfs:label> ?b ?_ ", prof_uri);
				QueryResult res = _agent._proxy.selectQuery(prof_course_query);
				flag = true;
				Integer count = 1;
				String ret = query_prof_name + " teaches the following courses: \n" ;
				for(List<String> row: res.getTable().getRows())
				{
					
					String course_label = cleanXSD(String.valueOf(row.get(1)));
					String course_id = getNameFromURI(String.valueOf(row.get(0)));
					ret += String.valueOf(count) + ". " + course_id + " - " + course_label;
					count++;
				}
				return ret;
			}
		}
		if(!flag)
		{
			return "Prof." + query_prof_name + " does not teach any course. "; 
		}
		return "There is no Prof with the name - " + query_prof_name;
		
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