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


public static final List<String> CS_syns = Arrays.asList("Computer Science", "CS","CSE","CSE.", "Comp. Sci", "Comp. Science");
public static final List<String> Elec_syns = Arrays.asList("Electrical Engineering", "EEE", "Elec. Engg", "Elec", "Electrical", "Electronics", "ECE", "EE");

public static final HashMap<String, List<String>> dept_syns = new HashMap<String, List<String>> ()
{
    {
        put("Computer_Science", CS_syns);
        put("Electrical_Engineering", Elec_syns);
    };
};

}
