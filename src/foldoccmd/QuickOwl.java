package foldoccmd;

import java.util.*;
import foldocml.*;
import java.io.*;
import hashgraph.HashGraph;

/**
 * Command line cgi program for generating RDF for VLUM [in XML] from a MECUREO query
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class QuickOwl {

    /** Implict defualt constructor */
    public QuickOwl() {    }

    public static String getenv(String key, String def) {
	String val = System.getProperty(key);
	if (val == null)
	    return def;
	return val;
    }

    /**
     * Usage: java foldoccmd.QuickRdf ontologyGraph queryList [depth = 1.0] > RDFFile
     * @param args [usage]
     * @throws IOException on an IO error
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java foldoccmd.QuickRdf ontologyGraph queryList [depth = 1.0] > RDFFile");
            return;
        }
        ArrayList nodes = new ArrayList();
        double depth = 1.0;

        int nonString;

        for (nonString = 1; nonString < args.length; ++nonString) {
            try { /*java sucks*/
                Double.parseDouble(args[nonString]);
                break;
            } catch (NumberFormatException nfe) {
                nodes.add(args[nonString]);
            }
        }

        if (nonString < args.length) try {
            depth = Double.parseDouble(args[nonString]);
        } catch (NumberFormatException nfe) {
            System.err.println("Couldn't parse depth, using 1.0");
        }
        boolean USE_HCI = false;
		boolean FULL_GRAPH = false;
        for (int idx = nonString + 1; idx < args.length; ++idx) {
            System.err.println("args[" + idx + "] = " + args[idx]);
            if (args[idx].equals("HCI")) {
                USE_HCI = true;
            }
			if (args[idx].equals("FULL_GRAPH")) {
				FULL_GRAPH = true;
			}
        }
		System.err.println("Creating OWL Ontology");
        System.err.println("Loading " + args[0]);
        Connection conn = Connection.connect(new java.io.File(getenv("ROOT", "") + args[0]));

		String mode = "dist";
        if (nonString + 2 < args.length)
        	mode = args[nonString + 2];
        
        HashGraph query;
        if (mode.equals("num")) {
        	query = conn.sizegrow(nodes.iterator(), depth);
        } else if (mode.equals("depth")) {
        	query = conn.depthgrow(nodes.iterator(), depth);        
        } else {
        	query = conn.grow(nodes.iterator(), depth);
        }

        System.err.println("Query size is " + query.size() + " nodes");
        PrintWriter pw = new PrintWriter(System.out);
        if (USE_HCI) {
            System.err.println("Using HCI Output");
            Connection.outputHCI(query, pw);
        } else if (FULL_GRAPH) {
			HashGraph g = Connection.openGraph(new java.io.File(getenv("ROOT", "") + args[0])); 
			Connection.outputOWL(g, pw);
		} else {
			Connection.outputOWL(query, pw);
        }
    }
}
