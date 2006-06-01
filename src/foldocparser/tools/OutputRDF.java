/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Trent Apted, University of Sydney
 * Author email       tapted@it.usyd.edu.au
 * Package            Mecureo
 * Web                
 * Created            
 * Filename           $RCSfile: OutputRDF.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003/09/09 00:38:03 $
 *               by   $Author: alum $
 *
 *****************************************************************************/
package foldocparser.tools;

import foldocparser.Normalizer;
import hashgraph.HashGraph;
import hashgraph.Link;
import hashgraph.Node;

import java.io.*;
import java.util.*;

/**
 * @author Trent Apted
 *
 */
public class OutputRDF {

	private static final char nl = '\n';
	private static final String RDF_HEADER =
		"<?xml version=\"1.0\"?>"
			+ nl
			+ ""
			+ nl
			+ "<rdf:RDF"
			+ nl
			+ "  xml=\"http://foldoc.doc.ic.ac.uk/foldoc/foldoc.cgi?query=\""
			+ nl
			+ "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
			+ nl
			+ "  xmlns:dc=\"http://purl.org/metadata/dublin_core/\""
			+ nl
			+ "  xmlns:gmp=\"http://www.gmp.usyd.edu.au/schema/resources/\">"
			+ nl
			+ nl;

	public static void outputRDF(HashGraph g, PrintWriter rdf) throws IOException {
		outputRDF(g, rdf, Math.max(3, (int)Math.ceil(Math.log(g.size()) / Math.log(10))));
	}

	@SuppressWarnings("unchecked")
	public static void outputRDF(HashGraph g, PrintWriter rdf, int PAD_LEN) throws IOException {
		//generate RDF
//	PrintWriter rdf = new PrintWriter(new BufferedWriter(
		//					      new FileWriter(RDF_FILE_OUTPUT)));
		Normalizer norm = null;
		try {
			norm = new Normalizer(g);
		} catch (IllegalArgumentException iae) {
			norm = Normalizer.NULL_NORMALIZER;
		}
		/* Changed 2002-07-08 THA
		HashMap serials = new HashMap(g.size());
		*/
		//need to generate serials in alphabetical order
		ArrayList strings = new ArrayList(g.size());
		for (Iterator it = g.nodeIterator(); it.hasNext();)
			strings.add(((Node)it.next()).getKey());
//		  Collections.sort(strings);


		//sort humanish-alphabetically (A-Z only)
		java.text.Collator sorter = java.text.Collator.getInstance();
		sorter.setStrength(java.text.Collator.PRIMARY);
		Collections.sort(strings, sorter);

		/* 2002-07-08 THA: no longer using serials
		for (int i = 0; i < strings.size(); ++i)
			serials.put(strings.get(i), pad(i, PAD_LEN));
		*/
		rdf.print(RDF_HEADER);

		//generate is alphabetical order for clarity. gets are O(1) anyway..
//	for(Iterator nodes = g.nodeIterator(); nodes.hasNext(); ) {
		for(Iterator keys = strings.iterator(); keys.hasNext(); ) {
			String key = (String)keys.next();
			/* 2002-07-08 THA fixed
			//need to reject ATM if the key contains a \"
			if (key.indexOf("\"") >= 0) {
				System.err.println("had to reject " + key);
				continue;
			}
			*/
			Node n = g.get(key);
			//output head
			rdf.println("  <rdf:Description about=\"" + OutputUtils.toURICompatible(key) + "\">");
			rdf.println("    <dc:Title>" + OutputUtils.toRDFTitle(key) + "</dc:Title>");
			rdf.println("    <gmp:results rdf:parseType=\"Resource\" gmp:dataset=\"gmp:average\" gmp:mark=\"" +
					OutputUtils.rdfNumForm.format(norm.norm(n.getDCost())) + "\" gmp:reliability=\"" +
					OutputUtils.rdfNumForm.format(norm.norm(n.getDCost())) + "\"/>");
			//output children
			//generate kids from _bidirectional_ links
			Set<Link> kids = OutputUtils.getLinksFromNode(n);

			/*
			 * Changes 2002-07-11 to include type information for VLUM output
			 * required to maintain kids as a set of links (rather than nodes)
			 * THA
			 */

			/* FROM Parser.java
            
			if (kw.isParent()) {
				lnk = new Link(n, dest, wt, kw);
				n.put(dest.getKey(), lnk);
				dest.linkBack(lnk);
			} else {
				lnk = new Link(dest, n, wt, kw);
				dest.put(n.getKey(), lnk);
				n.linkBack(lnk);
			}
			*/
            
			//make sure there's no link to itself (eg a loop; it happens eg 'recursion')
//		kids.remove(n);
			Set<Node> linked = new HashSet<Node>(); //don't link a node twice
			linked.add(n);		    //never link to itself

			for (Link l : kids) {
				Node peer = l.not(n);
				if (linked.contains(peer)) continue; //already linked
				linked.add(peer);
				String kk = peer.getKey();
				//fixed //if (kk.indexOf("\"") < 0)
				//		rdf.println("	 <gmp:peer rdf:resource=\"" + toRDFAbout(kk) +"\"/>");
				//		 <gmp:peer gmp:peerType="actor			 " rdf:resource="http://www.imdb.com/Title?0017324"/>
				rdf.println("	 <gmp:peer gmp:peerType=\"" + l.typeString() 
						+ "\" rdf:resource=\"" + OutputUtils.toURICompatible(kk) + "\"/>");
			}
			//output tail
			rdf.println("  </rdf:Description>");
		}
		rdf.println(OutputUtils.RDF_TAIL);
//	rdf.close();
	}

	/**
	 * Make a set of HTML files for loading an RDF file into VLUM
	 * Requires that the following templates exist (from current working dir)
	 *	    template/blank.html
	 *	    template/index.html 	(replaces @@vlumframe@@)
	 *	    template/loading.html
	 *	    template/reminder.html
	 *	    template/vlumframe.html	(replaces @@rdffile@@, @@initialnode@@)
	 */
	@SuppressWarnings("unchecked")
	public static void makeHTMLSet(File directory,
	                                 String rdfFile,
	                                 String filePostfix,
	                                 String startNode)
	              throws IOException {
	    final String TEMPL_PATH = "template" + File.separatorChar;
	    String names[] = {"blank.html",
	                      "loading.html",
	                      "reminder.html",
	                      "vlumframe.html",
	                      "index.html"};
	
	    String toNames[] = {names[0],
	                        names[1],
	                        names[2],
	                        "vlum" + filePostfix + ".html",
	                        "index" + filePostfix + ".html"};
	
	    //use a hashmap to replace tokens in the files
	    HashMap replaces = new HashMap();
	    replaces.put("rdffile", rdfFile);
	    replaces.put("initialnode", OutputUtils.toRDFTitle(startNode));
	    replaces.put("vlumframe", toNames[3]);
	
	    //first copy the files that don't change (don't replace if already exist)
	    //forget it; can pack it all together, replace tokens as we go
	    for (int i = 0; i < names.length; ++i) {
	        File toFile = new File(directory, toNames[i]);
	        if (!toFile.exists()) {
	            //there's a renameTo().. but no copyTo()
	            BufferedReader from = new BufferedReader(new FileReader(TEMPL_PATH + names[i]));
	            BufferedWriter to = new BufferedWriter(new FileWriter(toFile));
	            int c = 0;
	            while (c >= 0) {
	                boolean gotAt = false;
	                //detect "@@" but also let "@" through
	                while ((c = from.read()) >= 0 && !(gotAt && c == '@')) {
	                    if (gotAt) to.write('@');
	                    gotAt = c == '@';
	                    if (!gotAt) to.write(c);
	                }
	                if (c < 0) break;
	                //read token string
	                StringBuffer token = new StringBuffer();
	                while ((c = from.read()) >= 0 && c != '@') {
	                    token.append((char)c);
	                }
	                from.read(); //sink second @ at end
	                //look up token and write to 'to' if it exists
	                //System.err.println("Found token: " + token.toString());
	                String val;
	                if ((val = (String)replaces.get(token.toString())) != null)
	                        to.write(val.toString());
	                //System.err.println("Val was: " + val);
	            }
	            from.close();
	            to.close();
	        } else { //file exists
	            System.err.println(toFile.toString() + " already exists; not replaced");
	        }
	    }
	}

	public static String pad(int i, int len) {
		StringBuffer sb = new StringBuffer();
		sb.append(i);
		while (sb.length() < len)
			sb.insert(0, '0');
		return sb.toString();
	}

}
