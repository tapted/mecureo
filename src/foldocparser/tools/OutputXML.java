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

import hashgraph.*;
import java.io.*;
import java.util.*;
import foldocparser.*;

/**
 * @author Trent Apted
 *
 */
public class OutputXML {

	private static final String RDF_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>" +		"\n<resultset>" +		"\n";

	private static final String RDF_TAIL = "</resultset>";
	
	public static void outputXML(HashGraph g, PrintWriter rdf) throws IOException {
		outputXML(g, rdf, Math.max(3, (int)Math.ceil(Math.log(g.size()) / Math.log(10))));
	}

	public static void outputXML(HashGraph g, PrintWriter rdf, int PAD_LEN) throws IOException {
		Normalizer norm = null;
		try {
			norm = new Normalizer(g);
		} catch (IllegalArgumentException iae) {
			norm = Normalizer.NULL_NORMALIZER;
		}
		//need to generate serials in alphabetical order
		ArrayList strings = new ArrayList(g.size());
		for (Iterator it = g.nodeIterator(); it.hasNext();)
			strings.add(((Node)it.next()).getKey());
		//sort humanish-alphabetically (A-Z only)
		java.text.Collator sorter = java.text.Collator.getInstance();
		sorter.setStrength(java.text.Collator.PRIMARY);
		Collections.sort(strings, sorter);
		rdf.print(RDF_HEADER);

		//generate is alphabetical order for clarity. gets are O(1) anyway..
		for(Iterator keys = strings.iterator(); keys.hasNext(); ) {
			String key = (String)keys.next();
			
			Node n = g.get(key);
			//output head
			rdf.println("    <result concept=\"" + OutputUtils.toRDFTitle(key)
					+ "\" value="+ OutputUtils.rdfNumForm.format(norm.norm(n.getDCost())) 
					+ "/>");
		}
		rdf.println(RDF_TAIL);
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

}
