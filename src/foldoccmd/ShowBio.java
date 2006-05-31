/*
 * Created on 10-Oct-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package foldoccmd;
import foldocml.Connection;
import hashgraph.*;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author tapted
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ShowBio extends CgiApp {
	Connection conn;
	HashGraph bio_a;
	HashGraph bio_b;
	String bio_a_name;
	String bio_b_name;
	
	static DecimalFormat df = new DecimalFormat("0.00%");
	
	void generateDist() {
        bio_a = conn.grow(cgi.getIterator("bio_a"), cgi.getValue("distance", 0.8));
        bio_b = conn.grow(cgi.getIterator("bio_b"), cgi.getValue("distance", 0.8));
	}
	
	void generateDepth() {
        bio_a = conn.depthgrow(cgi.getIterator("bio_a"), cgi.getValue("distance", 2));
        bio_b = conn.depthgrow(cgi.getIterator("bio_b"), cgi.getValue("distance", 2));
	}

	void generateNum() {
        bio_a = conn.sizegrow(cgi.getIterator("bio_a"), cgi.getValue("distance", 40));
        bio_b = conn.sizegrow(cgi.getIterator("bio_b"), cgi.getValue("distance", 40));
	}
	
	void dumpStats() {
		bio_a_name = cgi.getValue("bio_a_name", "Bio A");
		bio_b_name = cgi.getValue("bio_b_name", "Bio B");
		println  ("==== " + bio_a_name + " statistics ====" + bio_a.statistcs());
		println("\n==== " + bio_b_name + " statistics ====" + bio_b.statistcs());		
	}
	
	void dumpCompare() {
		println("Min Spanning Tree Similarity = " + df.format(conn.ratioSimilar6(bio_a, bio_b, System.err)));
		println("           Global Similarity = " + df.format(conn.ratioSimilar1(bio_a, bio_b, System.err)));
		println("Avg. Norm. Search Similarity = " + df.format(conn.ratioSimilar5(bio_a, bio_b, System.err)));
		println("  Averaged Search Similarity = " + df.format(conn.ratioSimilar3(bio_a, bio_b, System.err)));
		println("Normalised Search Similarity = " + df.format(conn.ratioSimilar4(bio_a, bio_b, System.err)));
		println("     Basic Search Similarity = " + df.format(conn.ratioSimilar2(bio_a, bio_b, System.err)));
	}
	
	public String html_title() {
		return "Bio Comparison";
	}
	
	public void doMain() throws Exception {
		//println(cgi.toString());
		println("Loading ontology...");
        conn = Connection.connect(new 
        		java.io.File(cgi.getValue("ontology", "fdg/fcasetag.fdg")));
        String mode = cgi.getValue("mode", "num");
        println("  OK. Generating models...");
        if (mode.equals("dist")) {
        	generateDist();
        } else if (mode.equals("depth")) {
        	generateDepth();        
        } else {
        	generateNum();
        }
        println("  OK.");
        dumpStats();
        println("\nComparing models...");        
        dumpCompare();
        }

        List collections_dot_list(Iterator it) {
            List l = new ArrayList();
            while (it.hasNext())
                l.add(it.next());
            return l;
        }

        public void tail() {
            println("<form action=\"compimage.cgi\" method=\"POST\">");
            BioParser.hiddenFormBio(collections_dot_list(cgi.getIterator("bio_a")), "bio_a", cgi.getValue("bio_a_name", "bio_a"));
            BioParser.hiddenFormBio(collections_dot_list(cgi.getIterator("bio_b")), "bio_b", cgi.getValue("bio_b_name", "bio_a"));
            BioParser.formTail(0, cgi);
        }

        public static void main(String[] args) {
            ShowBio app = new ShowBio();
            app.cgiMain();
            app.tail();
        }
}
