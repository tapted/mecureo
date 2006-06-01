/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Trent Apted, University of Sydney
 * Author email       tapted@it.usyd.edu.au
 * Package            Mecureo
 * Web                
 * Created            
 * Filename           $RCSfile: OutputHCI.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003/09/09 00:23:38 $
 *               by   $Author: alum $
 *
 * ****************************************************************************/
package foldocparser.tools;

import hashgraph.*;
import java.io.*;
import java.util.*;
import foldocparser.*;

/**
 * @author Trent Apted
 *
 */
public class OutputHCI {

	private static final char nl = '\n';
	//private static final int DEFAULT_PAD_LEN = 5;
	private static final String RDF_HEADER =
		"<?xml version=\"1.0\"?>"
			+ nl
			+ ""
			+ nl
			+ "<rdf:RDF"
			+ nl
			+ "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
			+ nl
			+ "  xmlns:dc=\"http://purl.org/metadata/dublin_core/\""
			+ nl
			+ "  xmlns:gmp=\"http://www.gmp.usyd.edu.au/schema/resources/\">"
			+ nl
			+ nl;

	private static final String RDF_TAIL = "</rdf:RDF>";

	private static int counter = 0;

	/**
	 * Outputting the DOT graph (to dot langauge)
	 * Provide some defaults for the parameters (also so that existing code
	 * wasn't broken when more options were added
	 */

	public static void outputHCIRDF(HashGraph g, PrintWriter rdf) throws IOException {
	    outputHCIRDF(g, rdf, Math.max(3, (int)Math.ceil(Math.log(g.size()) / Math.log(10))));
	}

	public static void outputHCIRDF(HashGraph g, PrintWriter rdf, int PAD_LEN) throws IOException {
	        //generate RDF
	//	PrintWriter rdf = new PrintWriter(new BufferedWriter(
	//					      new FileWriter(RDF_FILE_OUTPUT)));
	        Normalizer norm = new Normalizer(g);
	        //need to generate serials in alphabetical order
	        ArrayList strings = new ArrayList(g.size());
	        for (Iterator it = g.nodeIterator(); it.hasNext();)
	            strings.add(((Node)it.next()).getKey());
	        Collections.sort(strings);
	        rdf.print(RDF_HEADER);
	
	        //generate is alphabetical order for clarity. gets are O(1) anyway..
	        for(Iterator keys = strings.iterator(); keys.hasNext(); ) {
	            String key = (String)keys.next();
	            Node n = g.get(key);
	            //output head
	            rdf.println("  <rdf:Description about=\"" + toHCIAbout(key, g) + "\">");
	            rdf.println("    <dc:Title>" + OutputUtils.toRDFTitle(key) + "</dc:Title>");
	            rdf.println("    <gmp:results rdf:parseType=\"Resource\" gmp:dataset=\"gmp:average\" gmp:mark=" +
	            		OutputUtils.rdfNumForm.format(norm.norm(n.getDCost())) + " gmp:reliability=" +
	                    OutputUtils.rdfNumForm.format(norm.norm(n.getDCost())) + "/>");
	            //output children
	            //generate kids from _bidirectional_ links
	            HashSet kids = new HashSet();
	/*
	            for (Iterator fwd = n.values().iterator(); fwd.hasNext(); ) {
	                kids.add((Link)fwd.next());
	            }
	            for (Iterator back = n.backIter(); back.hasNext(); ) {
	                kids.add((Link)back.next());
	                }
	                */
	            for (Iterator back = n.backIter(); back.hasNext(); ) {
	                //backlinks go the other way
	                if (OutputUtils.SMART_LINKS) {
	                    Link l = (Link)back.next();
	                    if (l.getTok().isStrictParent()) {
	                        kids.add(l.reverseDirection());
	                    } else if (l.getTok().isChild()) {
	                        kids.add(l.reverseBoth());
	                    } else {
	                        kids.add(l);
	                    }
	                } else {
	                    kids.add((Link)back.next());
	                }
	            }
	            for (Iterator fwd = n.values().iterator(); fwd.hasNext(); ) {
	                if (OutputUtils.SMART_LINKS) {
	                    Link l = (Link)fwd.next();
	                    if (l.getTok().isStrictParent()) {
	                        kids.add(l.reverseType());
	                    } else {
	                        kids.add(l);
	                    }
	                } else {
	                    kids.add((Link)fwd.next());
	                }
	            }
	            //make sure there's no link to itself (eg a loop; it happens eg 'recursion')
	            HashSet linked = new HashSet(); //don't link a node twice
	            linked.add(n);		    //never link to itself
	
	            for (Iterator kit = kids.iterator(); kit.hasNext(); ) {
	                Link l = (Link)kit.next();
	                Node peer = l.not(n);
	                if (linked.contains(peer)) continue; //already linked
	                linked.add(peer);
	                String kk = peer.getKey();
	                rdf.println("	 <gmp:peer gmp:peerType=\"" + l.typeString() + "\" rdf:resource=\"" + toHCIAbout(kk, g) + "\"/>");
	            }
	            //output tail
	            rdf.println("  </rdf:Description>");
	        }
	        rdf.println(RDF_TAIL);
	    }

	private static String toHCIAbout(String key, HashGraph g) {
	    Node n = (Node)g.get(key);
	    //System.err.println("g.get(" + key + ").webID = " + n.webID);
	    if (n.webID.equals("0")) {
	        counter++;
	        n.webID = "-" + counter;
	    }
	    return OutputUtils.HCI_ABOUT_BEGIN + n.webID + OutputUtils.HCI_ABOUT_TAIL;
	}
}
