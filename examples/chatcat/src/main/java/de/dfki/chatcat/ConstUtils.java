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
import org.json.JSONObject;
import org.json.JSONArray;


public interface ConstUtils{


public static final List<String> CS_syns = Arrays.asList("Computer Science", "CS","CSE","CSE.", "Comp. Sci", "Comp. Science", "Computer Science Engineering", "Comp Sci", "Comp Science");
public static final List<String> Elec_syns = Arrays.asList("Electrical Engineering", "EEE", "Elec. Engg", "Elec", "Electrical", "Electronics", "ECE", "EE", "Elec Engg");
public static final List<String> Mech_syns = Arrays.asList("Mechanical Engineering", "Mechanical", "Mech", "Mech.", "Mechatronics", "ME", "Mech. Engg", "Mechanical Engg");
public static final List<String> Chem_syns = Arrays.asList("Chemical Engineering","Chemical", "Chem Engineering", "Chem Engg", "Chem", "ChemE");
public static final HashMap<String, List<String>> dept_syns = new HashMap<String, List<String>> ()
{
    {
        put("Computer_Science", CS_syns);
        put("Electrical_Engineering", Elec_syns);
        put("Mechanical_Engineering",Mech_syns);
        put("Chemical_Engineering", Chem_syns);
    };
};

public static final List<String> objectPronouns = Arrays.asList("that", "this","it", "it's","the","its");
public static final List<String> personalPronouns = Arrays.asList("he","his","he's","hers","him","her","she","they","her's");
public static final List<String> BTech_syns = Arrays.asList("btech.", "btech","bachelors","bachelors of technology","beng.","beng","b.tech.", "bachelor of technology");
public static final List<String> MTech_syns = Arrays.asList("mtech.", "mtech","masters", "masters of technology","master of technology", "meng");
public static final List<String> PHD_syns = Arrays.asList("phd","p.h.d","phd.","doctorate","doctor of philosphy","doctors of philosophy");
public static final List<String> Dual_syns = Arrays.asList("dual", "dd", "dual degree","dual deg", "double deg", "integrated deg", "integrated", "integrated degree");

public static final HashMap<String, List<String>> program_syns = new HashMap<String, List<String>> ()
{
    {
        put("BTech.", BTech_syns);
        put("MTech.", MTech_syns);
        put("PHD.", PHD_syns);
        put("DD", Dual_syns);
    };
};

public static final HashMap<String,String> course_info = new HashMap<String,String>()
{
    {
        put("da", "Request");
        put("prop","Courses");
        put("what","*");
        put("theme","CourseInfo");
    }
};

public static String getDialogueAct(String rasaOutput)
{
    JSONObject json =  new JSONObject(rasaOutput);
    System.out.println(json.keySet());

    JSONObject intent = json.getJSONObject("intent");

    JSONArray entities = json.getJSONArray("entities");

    System.out.println(intent);
    System.out.println(entities);

    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");


    if(intent_name.equals("mood_great"))
    {
        return "Inform(Mood, polarity=\"pos\")";
    }
    if(intent_name.equals("affirm"))
    {
        return "Confirm(top)";
    }
    if(intent_name.equals("deny"))
    {
        return "Disconfirm(top)";
    }
    if(intent_name.equals("greet"))
    {
        return "InitialGreeting(Greet)";
    }

    
    if(intent_conf < 0.9)
    {
        return "NULL";
    }
    
    if(intent_name.equals("prof_course_info"))
    {
        return getDAProfCourseInfo(intent, entities);
    }
    if(intent_name.equals("prof_research_info"))
    {
        return getDAProfResearchInfo(intent, entities);
    }
    if(intent_name.equals("course_info"))
    {
        return getDACourseInfo(intent, entities);
    }
    if(intent_name.equals("course_prereq_info"))
    {
        return getDACoursePrereqInfo(intent, entities);
    }
    if(intent_name.equals("course_prof_info"))
    {
        return getDACourseProfInfo(intent, entities);
    }
    if(intent_name.equals("dept_courses_info"))
    {
        return getDADeptCourseInfo(intent, entities);
    }
    if(intent_name.equals("dept_profs_info"))
    {
        return getDADeptProfsInfo(intent, entities);
    }
    if(intent_name.equals("dept_programs_info"))
    {
        return getDADeptProgInfo(intent, entities);
    }
    if(intent_name.equals("dept_ug_programs_info"))
    {
        return getDADeptUGProgInfo(intent, entities);
    }
    if(intent_name.equals("dept_pg_programs_info"))
    {
        return getDADeptPGProgInfo(intent, entities);
    }
    if(intent_name.equals("dept_info"))
    {
        return getDADeptInfo(intent, entities);
    }
    if(intent_name.equals("dept_facilities_info"))
    {
        return getDADeptFacilitiesInfo(intent, entities);
    }
    if(intent_name.equals("program_info"))
    {
        return getDAProgramInfo(intent, entities);
    }
    if(intent_name.equals("prof_publication_info"))
    {
        return getDAProfPublicationInfo(intent, entities);
    }
    if(intent_name.equals("prof_advisee_info"))
    {
        return getDAProfAdviseeInfo(intent, entities);
    }
    if(intent_name.equals("student_research_info"))
    {
        return getDAStudentResearchInfo(intent, entities);
    }

    
   return "NULL"; 
    
   
}




// Prof related queries


public static String getDAProfCourseInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Professors"))
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"CourseInfo\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("Pronouns"))
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"CourseInfo\")",  "pronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"CourseInfo\")",  entity_val);
            return res;
        }
        

    }
    
    return "NULL";
    

}
public static String getDAProfAdviseeInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Professors"))
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"AdviseeInfo\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("Pronouns"))
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"AdviseeInfo\")",  "pronoun");
            return res;
        }
       
        

    }
    
    return "NULL";
}

public static String getDAProfPublicationInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Professors"))
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"PublicationInfo\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("Pronouns"))
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"PublicationInfo\")",  "pronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"PublicationInfo\")",  entity_val);
            return res;
        }
        

    }
    
    return "NULL";
    

}

public static String getDAProfResearchInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Professors"))
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"ResearchInfo\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("Pronouns"))
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"ResearchInfo\")",  "pronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Professor, what=\"%1$s\", theme=\"ResearchInfo\")",  entity_val);
            return res;
        }

    }
    
    return "NULL";
    

}

public static String getDAStudentResearchInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");

        if(entity_name.equals("Department"))
        {
            String res = String.format("Request(Student, dept=\"%1$s\", theme=\"ResearchStudents\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Student, dept=\"%1$s\", theme=\"ResearchStudents\")",  "objectPronoun");
            return res;
        }
        

    }
    
    return "NULL";
}


// Course related queries


public static String getDACourseInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");

        if(entity_name.equals("Courses"))
        {
            String res = String.format("Request(Courses, what=\"%1$s\", theme=\"CourseInfo\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Courses, what=\"%1$s\", theme=\"CourseInfo\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Courses, what=\"%1$s\", theme=\"CourseInfo\")",  entity_val);
            return res;
        }

    }
    
    return "NULL";
    

}

public static String getDACoursePrereqInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");

        if(entity_name.equals("Courses"))
        {
            String res = String.format("Request(Courses, what=\"%1$s\", theme=\"CoursePrereqInfo\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Courses, what=\"%1$s\", theme=\"CoursePrereqInfo\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Courses, what=\"%1$s\", theme=\"CoursePrereqInfo\")",  entity_val);
            return res;
        }

    }
    
    return "NULL";
    

}

public static String getDACourseProfInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Courses"))
        {
            String res = String.format("Request(Courses, what=\"%1$s\", theme=\"CourseTeacherInfo\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Courses, what=\"%1$s\", theme=\"CourseTeacherInfo\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Courses, what=\"%1$s\", theme=\"CourseTeacherInfo\")",  entity_val);
            return res;
        }

    }
    
    return "NULL";
    

}


// Department related queries

public static String getDADeptCourseInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Department"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"CourseList\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"CourseList\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"CourseList\")",  entity_val);
            return res;
        }

    }
    return "NULL";

}

public static String getDADeptProfsInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Department"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"ProfList\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"ProfList\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"ProfList\")",  entity_val);
            return res;
        }

    }
    return "NULL";

}

public static String getDADeptProgInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Department"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"ProgList\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"ProgList\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"ProgList\")",  entity_val);
            return res;
        }

    }
    return "NULL";
}

public static String getDADeptUGProgInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Department"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"UGProgList\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"UGProgList\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"UGProgList\")",  entity_val);
            return res;
        }

    }
    return "NULL";
}



public static String getDADeptPGProgInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Department"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"PGProgList\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"PGProgList\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"PGProgList\")",  entity_val);
            return res;
        }

    }
    return "NULL";
}


public static String getDADeptInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Department"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"DeptInfo\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"DeptInfo\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"DeptInfo\")",  entity_val);
            return res;
        }

    }
    return "NULL";
}

public static String getDADeptFacilitiesInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    String prof_name = "";
    for(int i=0; i<entities.length(); i++)
    {
        JSONObject entity = entities.getJSONObject(i);
        String entity_name = entity.getString("entity");
        String entity_val = entity.getString("value");
        Double entity_conf = entity.getDouble("confidence_entity");
        if(entity_name.equals("Department"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"Facilities\")",  entity_val);
            return res;
        }
        else if(entity_name.equals("ObjectPronoun"))
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"Facilities\")",  "objectPronoun");
            return res;
        }
        else if(entity_conf > 0.5)
        {
            String res = String.format("Request(Department, what=\"%1$s\", theme=\"Facilities\")",  entity_val);
            return res;
        }


    }
    return "NULL";
}

public static String getDAProgramInfo(JSONObject intent, JSONArray entities)
{
    String intent_name = intent.getString("name");
    Double intent_conf = intent.getDouble("confidence");
    System.out.println(String.valueOf(entities.length()));
    if(entities.length() == 1)
    {
        
        String res = "Request(Programs, theme=\"ProgramInfo\", pronoun=\"True\")";
        return res;
    }
    else if(entities.length() == 2)
    {
        Boolean flag = false;
        for(int i=0; i<entities.length(); i++)
        {   
            JSONObject entity = entities.getJSONObject(i);
            String entity_name = entity.getString("entity");
            String entity_val = entity.getString("value");
            if(entity_name.equals("ObjectPronoun"))
            {
                flag=true;
                break;
            }
        }
        if(flag)
        {
            String program_name = "NULL";
            for(int i=0; i<entities.length(); i++)
            {
                JSONObject entity = entities.getJSONObject(i);
                String entity_name = entity.getString("entity");
                String entity_val = entity.getString("value");
                if(entity_name.equals("Program"))
                {
                    program_name = entity_val;
                }
            }
            String res = String.format("Request(Programs, theme=\"ProgramInfo\", dept=\"objectPronoun\", program_type=\"%1$s\" )", program_name);
            return res;
        }
        else
        {
            String program_name = "NULL";
            String dept_name = "NULL";

            
            for(int i=0; i<entities.length(); i++)
            {
                JSONObject entity = entities.getJSONObject(i);
                String entity_name = entity.getString("entity");
                String entity_val = entity.getString("value");
                if(entity_name.equals("Program"))
                {
                    program_name = entity_val;
                }
                else if(entity_name.equals("Department"))
                {
                    dept_name = entity_val;
                }
            }
            String res = String.format("Request(Programs, theme=\"ProgramInfo\", dept=\"%1$s\", program_type=\"%2$s\" )", dept_name, program_name);
            return res;

        }
    }
    return "NULL";
}

}



