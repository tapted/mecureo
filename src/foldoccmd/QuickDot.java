package foldoccmd;

import java.util.*;
import foldocml.*;
import java.io.*;
import hashgraph.HashGraph;

/**
 * <p>Command line cgi program for generating textual DOT from a MECUREO query</p>
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class QuickDot {

    /** Implicit default constuctor */
    public QuickDot() { }

    /**
     * Usage: java foldoccmd.QuickDot ontologyGraph queryList [depth = 1.0] [minKids = 0] > DOTFile
     * @param args [usage]
     * @throws IOException on an IO error
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java foldoccmd.QuickDot ontologyGraph queryList [depth = 1.0] [minKids = 0] > DOTFile");
            return;
        }
        ArrayList nodes = new ArrayList();
        int minKids = 0;
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

        if (nonString + 1 < args.length) try {
            minKids = Integer.parseInt(args[nonString + 1]);
        } catch (NumberFormatException nfe) {
            System.err.println("Couldn't parse minKids, using 0");
        }
        if (nonString < args.length) try {
            depth = Double.parseDouble(args[nonString]);
        } catch (NumberFormatException nfe) {
            System.err.println("Couldn't parse depth, using 1.0");
        }
        System.err.println("Loading " + args[0]);

        Connection conn = Connection.connect(new
java.io.File(QuickRdf.getenv("ROOT",
"") + args[0]));

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
        Connection.outputDOT(query, pw, minKids, minKids);
//	pw.close();
    }
}
