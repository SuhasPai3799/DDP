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

public interface ConstUtils{


public static final List<String> CS_syns = Arrays.asList("Computer Science", "CS","CSE","CSE.", "Comp. Sci", "Comp. Science", "Computer Science Engineering");
public static final List<String> Elec_syns = Arrays.asList("Electrical Engineering", "EEE", "Elec. Engg", "Elec", "Electrical", "Electronics", "ECE", "EE");
public static final List<String> Mech_syns = Arrays.asList("Mechanical Engineering", "Mechanical", "Mech", "Mech.", "Mechatronics", "ME", "Mech. Engg", "Mechanical Engg");

public static final HashMap<String, List<String>> dept_syns = new HashMap<String, List<String>> ()
{
    {
        put("Computer_Science", CS_syns);
        put("Electrical_Engineering", Elec_syns);
        put("Mechanical_Engineering",Mech_syns);
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

}
