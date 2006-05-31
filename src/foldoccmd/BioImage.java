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
public class BioImage extends ShowBio {
    HashGraph merged;

    void dumpCompare() {
        java.io.PrintWriter pw = new java.io.PrintWriter(System.out);
        println("Merging...");
        merged = bio_a.merge(bio_b, false);
        try {
            Connection.outputClusteredDOT(merged, pw,
                                          Integer.parseInt(cgi.getValue("minKids", "1")),
                                          Integer.parseInt(cgi.getValue("minLinks", "1")),
                                          bio_a_name, bio_b_name,
                                          "");
        } catch (java.io.IOException ioe) {
        }
    }

    public static void main(String[] args) {
        ShowBio app = new BioImage();
        app.of = null;
        app.cgiMain();
    }
}
