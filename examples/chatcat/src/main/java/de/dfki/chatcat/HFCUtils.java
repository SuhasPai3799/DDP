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
import opennlp.ccgbank.parse.start;
import de.dfki.lt.hfc.db.QueryException;
import de.dfki.lt.hfc.db.QueryResult;
import de.dfki.lt.hfc.types.XsdAnySimpleType;
import de.dfki.lt.hfc.types.XsdDateTime;
import de.dfki.lt.loot.jada.Pair;

import static de.dfki.chatcat.ConstUtils.*;

import static de.dfki.chatcat.ListComparator.*;




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
	public static String[] prof_syns = {"professors", "professor","instructors", "teachers", "instructor","teacher", "profs","dr\\.","prof\\.","prof","dr" };
	public static String[] dept_common_syns = {"departments", "department", "dept.", "dept","branches", "branch"};
	
	public static String getProfName(String query_prof_name)
	{
		query_prof_name = cleanProfNames(query_prof_name);
		String query_prof_uri = "<univ:" + query_prof_name + ">";
		String query = "select ?a ?b where ?a <rdf:type> <univ:Professors> ?_ & ?a <rdfs:label> ?b ?_";
		QueryResult res = _agent._proxy.selectQuery(query);
		Boolean flag = false;
		for(List<String> row: res.getTable().getRows())
		{
			String prof_uri = String.valueOf(row.get(0));
			String prof_name = String.valueOf(row.get(1));
			if(prof_name.toLowerCase().contains(query_prof_name.toLowerCase()))
			{
				return prof_name;
			}
		}
		return "NULL";
	}
	public static String getProfNameFromOutput(String output)
	{
		if(output.equals("No such course exists."))
		return output;
		String res = output.split("teaches")[0].split("Prof.")[1];
		logger.log(Level.INFO, res);
		return res.trim();
	}
	public static String cleanCourseNames(String query_course_name)
	{
		query_course_name = query_course_name.toLowerCase();
		String[] splitted = query_course_name.split("\\s+");
		for(String word: splitted)
		{
			logger.log(Level.INFO, word);
		}
		if(splitted.length == 1)
		return splitted[0];
		if(splitted[0].equals("the") && splitted[1].equals("course"))
		return String.join(" ", Arrays.copyOfRange(splitted, 2, splitted.length)).trim();
		if(splitted[0].equals("the"))
		return String.join(" ", Arrays.copyOfRange(splitted,1,splitted.length));
		return query_course_name;
	}

	public static String cleanProfNames(String query_prof_name)
	{
		query_prof_name = query_prof_name.toLowerCase();
		for(String prof_syn: prof_syns)
		{
			if(query_prof_name.matches(prof_syn + ".*"))
			{
				query_prof_name = query_prof_name.split(prof_syn)[1];
			}
		}
		if(query_prof_name.contains("'"))
		{
			query_prof_name = query_prof_name.split("'")[0];
		}
		logger.log(Level.INFO, query_prof_name);
		
		return query_prof_name.trim();
	}
	
	public static String formatNewLine(String word)
	{
		int index = word.indexOf(" ");
		int count = 1;
		StringBuffer string = new StringBuffer(word);
        
		while(index >= 0) {
		if(count%10==0)
		{
			string.setCharAt(index,'\n');
		}
		count++;
		index = word.indexOf(" ", index+1);
		}
		return string.toString(); 
	}

	/**
	 * Given a department name, return the name of a close department name as stored in the database.
	 * @param dept_name Name of the department for which we are trying to find the closest possible name in the database.
	 * @return "NULL" if no department is found close enough.
	 */
	public static String getClosestDept(String dept_name)
	{
		String[] splitted = dept_name.split("\\s+");
		if(splitted.length>1)
		{
			if(splitted[0].equals("the") && Arrays.asList(dept_common_syns).contains(splitted[1]))
			{
				dept_name = String.join(" ", Arrays.copyOfRange(splitted, 2, splitted.length)).trim();
			}
			else if(splitted[0].equals("the"))
			{
				dept_name = String.join(" ", Arrays.copyOfRange(splitted, 1, splitted.length)).trim();
			}
		}
		for(String syns:dept_common_syns)
		{
			if(dept_name.contains(syns))
			{
				dept_name = dept_name.replaceAll(syns, "");
			}
		}
		dept_name = dept_name.trim();
		logger.log(Level.INFO, dept_name);
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

	public static String getClosestProg(String program_name)
	{
		
		for(Map.Entry mapElement: program_syns.entrySet())
		{
			String program_official_name = (String)mapElement.getKey();
			List<String> program_name_syns = (List)mapElement.getValue();
			for(String syn:program_name_syns)
			{
				if((syn.toLowerCase()).equals(program_name.toLowerCase()))
				{
					return program_official_name;
				}
			}
		}
		return "NULL";
	}

	/* 
	Functions related to answering prof-related queries
	*/


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
					return formatNewLine("Info about the course " + c_name + " " +  (String)(comments.get(0)));
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
					return formatNewLine("Info about the course " + c_name + " " +  (String)(comments.get(0)));
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
					return "Prof. " + String.valueOf((course_teacher.get(0))) + " teaches the course " + c_name;
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
					return "Prof. " + String.valueOf((course_teacher.get(0))) + " teaches the course " + c_name;
				}

			}	
		}
		return "No such course exists.";
	}

	
	public static String answerCoursePrereqInfo(String c_name)
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
					String prereq_query = String.format("select ?a ?b where %s <univ:hasPrerequisite> ?a ?_ & ?a <rdfs:label> ?b ?_",course_id);
					QueryResult prereq_res = _agent._proxy.selectQuery(prereq_query);
					Integer count = 1;
					String ret = "The course : " + c_name + " has the following prerequisite courses: \n";
					
					for(List<String> course_row: prereq_res.getTable().getRows())
					{
						String prereq_course_label = cleanXSD(String.valueOf(course_row.get(1)));
						String prereq_course_id = getNameFromURI(String.valueOf(course_row.get(0)));
						ret += String.valueOf(count) + ". " + prereq_course_id + " - " + prereq_course_label + "\n";
						count++;
					}
					if(count == 1)
					{
						ret = "The course : " + c_name + " has no prerequisistes.";
					}
					return ret;
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
					String prereq_query = String.format("select ?a ?b where %s <univ:hasPrerequisite> ?a ?_ & ?a <rdfs:label> ?b ?_",course_id);
					QueryResult prereq_res = _agent._proxy.selectQuery(prereq_query);
					Integer count = 1;
					String ret = "The course : " + c_name + " has the following prerequisite courses: \n";
					for(List<String> course_row: prereq_res.getTable().getRows())
					{
						String prereq_course_label = cleanXSD(String.valueOf(course_row.get(1)));
						String prereq_course_id = getNameFromURI(String.valueOf(course_row.get(0)));
						ret += String.valueOf(count) + ". " + prereq_course_id + " - " + prereq_course_label + "\n";
						count++;
					}
					return ret;
				}

			}
		}
		return "No course with name " + c_name + " exists.";
	}


	/* 
	Functions related to answering prof-related queries
	*/

	public static String answerProfCourses(String query_prof_name)
	{

		query_prof_name = cleanProfNames(query_prof_name);
		String query_prof_uri = "<univ:" + query_prof_name + ">";
		String query = "select ?a ?b where ?a <rdf:type> <univ:Professors> ?_ & ?a <rdfs:label> ?b ?_";
		QueryResult res = _agent._proxy.selectQuery(query);
		Boolean flag = false;
		for(List<String> row: res.getTable().getRows())
		{
			String prof_uri = String.valueOf(row.get(0));
			String prof_name = String.valueOf(row.get(1));
			if(prof_name.toLowerCase().contains(query_prof_name.toLowerCase()))
			{
				String prof_course_query = String.format("select ?a ?b where %s <univ:teaches> ?a ?_  & ?a <rdfs:label> ?b ?_ ", prof_uri);
				QueryResult course_res = _agent._proxy.selectQuery(prof_course_query);
				flag = true;
				Integer count = 1;
				String ret = query_prof_name + " teaches the following courses: \n" ;
				for(List<String> course_row: course_res.getTable().getRows())
				{
					
					String course_label = cleanXSD(String.valueOf(course_row.get(1)));
					String course_id = getNameFromURI(String.valueOf(course_row.get(0)));
					ret += String.valueOf(count) + ". " + course_id + " - " + course_label + "\n";
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
	public static String answerProfPublications(String query_prof_name)
	{

		query_prof_name = cleanProfNames(query_prof_name);
		String query_prof_uri = "<univ:" + query_prof_name + ">";
		String query = "select ?a ?b where ?a <rdf:type> <univ:Professors> ?_ & ?a <rdfs:label> ?b ?_";
		QueryResult res = _agent._proxy.selectQuery(query);
		Boolean flag = false;
		for(List<String> row: res.getTable().getRows())
		{
			String prof_uri = String.valueOf(row.get(0));
			String prof_name = String.valueOf(row.get(1));
			if(prof_name.toLowerCase().contains(query_prof_name.toLowerCase()))
			{
				String prof_publication_query = String.format("select ?a ?b where %s <univ:hasPublished> ?a ?_  & ?a <rdfs:label> ?b ?_ ", prof_uri);
				QueryResult publication_res = _agent._proxy.selectQuery(prof_publication_query);
				flag = true;
				Integer count = 1;
				String ret = query_prof_name + " has the following publications : \n" ;
				for(List<String> publication_row: publication_res.getTable().getRows())
				{
					
					String publication_name = cleanXSD(String.valueOf(publication_row.get(1)));
					ret += String.valueOf(count) + ". " + publication_name + "\n";
					count++;
				}
				return ret;
			}
		}
		if(!flag)
		{
			return "Prof." + query_prof_name + " does not have any publications. "; 
		}
		return "There is no Prof with the name - " + query_prof_name;
		
	}

	public static String answerProfAdvisees(String query_prof_name)
	{
		query_prof_name = cleanProfNames(query_prof_name);
		String query_prof_uri = "<univ:" + query_prof_name + ">";
		String query = "select ?a ?b where ?a <rdf:type> <univ:Professors> ?_ & ?a <rdfs:label> ?b ?_";
		QueryResult res = _agent._proxy.selectQuery(query);
		Boolean flag = false;
		for(List<String> row: res.getTable().getRows())
		{
			String prof_uri = String.valueOf(row.get(0));
			String prof_name = String.valueOf(row.get(1));
			if(prof_name.toLowerCase().contains(query_prof_name.toLowerCase()))
			{
				String prof_advisee_query = String.format("select ?a ?b where %s <univ:advises> ?a ?_  & ?a <rdfs:label> ?b ?_ ", prof_uri);
				QueryResult publication_res = _agent._proxy.selectQuery(prof_advisee_query);
				
				Integer count = 1;
				String ret = query_prof_name + " guides the following people : \n" ;	
				for(List<String> student_row: publication_res.getTable().getRows())
				{
					flag = true;
					String student_name = cleanXSD(String.valueOf(student_row.get(1)));
					
					ret += String.valueOf(count) + ". " + student_name + "\n";
					count++;
				}
				if(!flag)
				{
					return "Prof." + query_prof_name + " does not have any advisees. "; 
				}
				return ret;
			}
		}
		
		if(!flag)
		{
			return "Prof." + query_prof_name + " does not have any advisees. "; 
		}
		return "There is no Prof with the name - " + query_prof_name;
	}
	public static String answerProfResearchArea(String query_prof_name)
	{
		query_prof_name = cleanProfNames(query_prof_name);
		String query_prof_uri = "<univ:" + query_prof_name + ">";
		String query = "select ?a ?b where ?a <rdf:type> <univ:Professors> ?_ & ?a <rdfs:label> ?b ?_";
		QueryResult res = _agent._proxy.selectQuery(query);
		Boolean flag = false;
		for(List<String> row: res.getTable().getRows())
		{
			String prof_uri = String.valueOf(row.get(0));
			String prof_name = String.valueOf(row.get(1));
			if(prof_name.toLowerCase().contains(query_prof_name.toLowerCase()))
			{
				String prof_research_query = String.format("select ?b where %s <univ:isSpecializedIn> ?a ?_  & ?a <rdfs:label> ?b ?_ ", prof_uri);
				List<Object> all_areas = _agent._proxy.query(prof_research_query);
				flag = true;
				Integer count = 1;
				String ret = query_prof_name + " has the following research interests: \n" ;
				for(Object area: all_areas)
				{
					
					String res_area = String.valueOf(area);
					ret += String.valueOf(count) + ". " + res_area + "\n";
					count++;
				}
				return ret;
			}
		}
		if(!flag)
		{
			return "Prof." + query_prof_name + " does not have any enlisted research areas. "; 
		}
		return "There is no Prof with the name - " + query_prof_name;
	}
	

	public static String answerFieldProfs(String query_field_name)
	{
		String query = "select ?a ?b where ?a <rdf:type> <univ:ResearchFields> ?_ & ?a <rdfs:label> ?b ?_";
		String query_field_uri = "<univ:" + (query_field_name.trim()) + ">";
		QueryResult res = _agent._proxy.selectQuery(query);
		for(List<String> row: res.getTable().getRows())
		{
			String field_name = String.valueOf(row.get(1));
			String field_uri = String.valueOf(row.get(0));
			if(field_name.toLowerCase().contains(query_field_name.toLowerCase().trim()))
			{
				String prof_query = String.format("select ?b where ?a <univ:isSpecializedIn> %s ?_ & ?a <rdfs:label> ?b ?_", field_uri);
				List<Object> prof_obj = _agent._proxy.query(prof_query);
				Integer count = 1;
				String ret = "The following professors are working in the field " + query_field_name + ": \n";
				for(Object prof_name_obj: prof_obj )
				{	
					
					ret += String.valueOf(count) + ". " + String.valueOf(prof_name_obj) + "\n";
					count++;
				}
				return ret;
			}
		}
		return "There are no professors enlisted doing research in the field " + query_field_name;

	}

	/* Functions related to students */

	public static String answerResearchStudentsInfo(String dept_name)
	{
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name == "NULL")
		{
			return "-1";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
		String query = String.format("select ?c ?d ?e ?f where %s <univ:offers> ?a ?_ & ?a <rdf:type> <univ:Programs> ?_ & ?c <univ:enrolledIn> ?a ?_ & ?c <univ:isAdvisedBy> ?e ?_ & ?c <rdfs:label> ?d ?_ & ?e <rdfs:label> ?f ?_", dept_uri);
		QueryResult res = _agent._proxy.selectQuery(query);
		Integer count = 1;
		String ret = dept_name + " department has the following students performing research : \n";
		for(List<String> student_row: res.getTable().getRows())
		{
			String student_name = cleanXSD(student_row.get(1));
			String prof_name = cleanXSD(student_row.get(3));
			ret += String.valueOf(count) + ". " + student_name + " performs research under Prof." + prof_name + "\n";
			count++;
		}
		return ret;
	}

	/* 
	Functions related to answering dept-related queries
	*/

	public static String answerDeptCourseCount(String dept_name)
	{
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name == "NULL")
		{
			return "-1";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
		String query = String.format("select ?a where ?a <univ:offeredBy> %s ?_ & ?a <rdf:type> <univ:Courses> ?_", dept_uri );
		List<Object> res = _agent._proxy.query(query);
		return String.valueOf(res.size());
		
	}

	public static String answerDeptFacilities(String dept_name)
	{
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name == "NULL")
		{
			return "No department with name " + dept_name + " exists";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
		String query = String.format("select ?b where %s <univ:hasFacility> ?a ?_ & ?a <rdfs:label> ?b ?_", dept_uri);
		List<Object> res = _agent._proxy.query(query);
		String ret = "The department of " + dept_name + " has the following lab facilities : \n";
		Integer count = 1;
		for(Object fac: res)
		{
			String lab_fac = String.valueOf(fac);
			ret += String.valueOf(count) + ". " + lab_fac + "\n";
			count++;
		} 
		return ret;
	}

	public static String answerDeptCourseList(String  dept_name)
	{
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name=="NULL")
		{
			return "No department with name " + dept_name + " exists";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
		String query = String.format("select ?a ?b where %s <univ:offers> ?a ?_ & ?a <rdf:type> <univ:Courses> ?_ & ?a <rdfs:label> ?b ?_", dept_uri);
		QueryResult res = _agent._proxy.selectQuery(query);
		Integer count = 1;
		String ret = dept_name + " department offers the following courses : \n";
		for(List<String> course_row: res.getTable().getRows())
		{
			String course_label = cleanXSD(String.valueOf(course_row.get(1)));
			String course_id = getNameFromURI(String.valueOf(course_row.get(0)));
			ret += String.valueOf(count) + ". " + course_id + " - " + course_label + "\n";
			count++;
		}
		return ret;
	}

	public static String answerDeptProfList(String dept_name)
	{
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name=="NULL")
		{
			return "No department with name " + dept_name + " exists";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
		String query = String.format("select distinct ?b where ?a <univ:teaches> ?x ?_ & ?x <univ:offeredBy> %s ?_ & ?a <rdfs:label> ?b ?_", dept_uri);
		List<Object> res = _agent._proxy.query(query);
		Integer count = 1;
		String ret = dept_name + " department has the following professors : \n";
		for(Object prof_obj:res)
		{
			String prof_name = String.valueOf(prof_obj);
			ret += String.valueOf(count) + ". " + prof_name + "\n";
			count++;
		}
		return ret;
	}


	public static String answerDeptProgramList(String dept_name)
	{
		
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name=="NULL")
		{
			return "No department with name " + dept_name + " exists";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
		String query = String.format("select ?b where %s <univ:offers> ?a ?_ & ?a <rdfs:label> ?b ?_ & ?a <rdf:type> <univ:Programs> ?_", dept_uri);
		List<Object> res = _agent._proxy.query(query);
		Integer count = 1;
		
		String ret = dept_name + " department offers the following programs : \n";
		for(Object prog_obj:res)
		{
			String prog_name = String.valueOf(prog_obj);
			ret += String.valueOf(count) + ". " + prog_name + "\n";
			count++;
		}
		return ret;
	}

	public static String answerDeptUGProgramList(String dept_name)
	{
		
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name=="NULL")
		{
			return "No department with name " + dept_name + " exists";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
		String query = String.format("select ?b where %s <univ:offers> ?a ?_ & ?a <rdfs:label> ?b ?_ & ?a <rdf:type> <univ:UndergradPrograms> ?_", dept_uri);
		List<Object> res = _agent._proxy.query(query);
		Integer count = 1;
	
		String ret = dept_name + " department offers the following undergraduate programs : \n";
		for(Object prog_obj:res)
		{
			String prog_name = String.valueOf(prog_obj);
			ret += String.valueOf(count) + ". " + prog_name + "\n";
			count++;
		}
		return ret;
	}

	public static String answerDeptPGProgramList(String dept_name)
	{
		
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name=="NULL")
		{
			return "No department with name " + dept_name + " exists";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
		String query = String.format("select ?b where %s <univ:offers> ?a ?_ & ?a <rdfs:label> ?b ?_ & ?a <rdf:type> <univ:PostgradPrograms> ?_", dept_uri);
		List<Object> res = _agent._proxy.query(query);
		Integer count = 1;
	
		String ret = dept_name + " department offers the following postgraduate programs : \n";
		for(Object prog_obj:res)
		{
			String prog_name = String.valueOf(prog_obj);
			ret += String.valueOf(count) + ". " + prog_name + "\n";
			count++;
		}
		return ret;
	}

	public static String answerDeptInfo(String dept_name)
	{
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name=="NULL")
		{
			return "No department with name " + dept_name + " exists";
		}
		String dept_uri = "<univ:" + closest_dept_name + ">";
	
		String query = String.format("select ?b where %s <rdfs:comment> ?b ?_", dept_uri);
		List<Object> res = _agent._proxy.query(query);
		logger.log(Level.INFO, String.valueOf(res.get(0)));
		return formatNewLine(String.valueOf(res.get(0)));
	}


	/* Program related queries */

	public static String answerProgramInfo(String dept_name, String program_name)
	{
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name=="NULL")
		{
			return "No department with name " + dept_name + " exists";
		}
		String closest_program_name = getClosestProg(program_name);
		if(closest_program_name=="NULL")
		{
			return "No program with name " + program_name + " exists";
		}
		String program_uri = "<univ:"+closest_program_name + "_" + closest_dept_name + ">";
		logger.log(Level.INFO, program_uri);
		String query = String.format("select ?b where %s <rdfs:comment> ?b ?_",program_uri);
		List<Object> res = _agent._proxy.query(query);
		logger.log(Level.INFO, String.valueOf(res.get(0)));
		return formatNewLine(String.valueOf(res.get(0)));
	}

	public static String answerProgramAdmissionInfo(String dept_name, String program_name)
	{
		String closest_dept_name = getClosestDept(dept_name);
		if(closest_dept_name=="NULL")
		{
			return "No department with name " + dept_name + " exists";
		}
		String closest_program_name = getClosestProg(program_name);
		if(closest_program_name=="NULL")
		{
			return "No program with name " + program_name + " exists";
		}
		logger.log(Level.INFO, "Check check");
		String program_uri = "<univ:"+closest_program_name + "_" + closest_dept_name + ">";
		String query = String.format("select ?c ?b where ?a <rdfs:label> ?b ?_ & %s <univ:hasAdmissionRequisites> ?a ?_ & ?a <rdfs:comment> ?c ?_", program_uri);
		QueryResult res = _agent._proxy.selectQuery(query);
		Integer count = 1;
		String ret = "The program " + closest_program_name + " in " + dept_name + " has the following admission requisites: \n";
		for(List<String> admit_row: res.getTable().getRows())
		{
			String admit_label = cleanXSD(String.valueOf(admit_row.get(1)));
			String admit_comment = cleanXSD(String.valueOf(admit_row.get(0)));
			
			ret += String.valueOf(count) + ". " + admit_label + ":" + admit_comment + "\n";
			count++;
		}
		return ret;
	}


	  

	/* 
	Anaphora Resolution
	*/

	public static String resolveProfName()
	{
		String query = "select ?a ?c where ?b <rdf:type> <univ:Professors> ?c & ?b <univ:name> ?a ?_ ";
		QueryResult res = _agent._proxy.selectQuery(query);
		List<List<String>> all_profs = res.getTable().getRows();
		Collections.sort(all_profs, new ListComparator<>());
		List<String> context_prof = all_profs.get(all_profs.size() - 1);
		logger.log(Level.INFO, context_prof.get(0));
		return cleanXSD(context_prof.get(0));
	}

	public static String resolveObjectPronoun(String class_uri)
	{
		String query = String.format("select ?a ?c where ?b <rdf:type> %s ?c & ?b <univ:name> ?a ?_",class_uri);
		QueryResult res = _agent._proxy.selectQuery(query);
		List<List<String>> all_obj= res.getTable().getRows();
		Collections.sort(all_obj, new ListComparator<>());
		List<String> context_obj = all_obj.get(all_obj.size() - 1);
		logger.log(Level.INFO, context_obj.get(0));
		return cleanXSD(context_obj.get(0));
	}

	public static Boolean checkObjectPronoun(String word)
	{
		for(String pronoun: objectPronouns)
		{
			if(word.toLowerCase().equals(pronoun.toLowerCase()))
			{
				return true;
			}
		}
		logger.log(Level.INFO, word + " Doesn't match any");
		return false;
	}

	public static Boolean checkPersonalPronoun(String word)
	{
		for(String pronoun: personalPronouns)
		{
			if(word.toLowerCase().equals(pronoun.toLowerCase()))
			{
				return true;
			}
		}
		logger.log(Level.INFO, word + " Doesn't match any");
		return false;
	}
	

	public HFCUtils(ChatAgent agent)
	{
		this._agent = agent;
	}
}