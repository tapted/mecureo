package foldocpos;

import posparser.*;

import java.io.*;
import java.util.zip.*;

/**
 * Dumps the result of each definition to stdout, after being
 * pre-processed by the FOLDOCReader
 */
public class ReaderTester {
    
    public static void main(String[] args) throws Exception {
        
        String fileName = args[0];
        FOLDOCReader in;
        
        if (fileName.endsWith(".gz")) {
            in = new FOLDOCReader(new InputStreamReader(
                                    new GZIPInputStream(
                                    new FileInputStream(fileName))));
        } else {
            in = new FOLDOCReader(new FileReader(fileName));
        }
        
        String s;
        //POSParser parser = new POSParser();
        DictHandler dh = new DictHandler(new DebugDictAdapter());
        //DictHandler dh = new DictHandler(new POSTokenizer(parser, in));
        
        while ((s = in.nextDef()) != null) {
            System.out.println("\n\n" + s);
            String def = POSWrapper.readLines(in);
	    System.out.println(def);
	    POSWrapper.parse(def, dh);
	    /*
	    String line;
	    while ((line = in.readLine()) != null) {
	    	System.out.println("[" + in.getLineNumber() + "]" + line);
	    }
	    */
/*
	    int c = 1;
	    while (c > 0) {
	 	System.out.print("[" + in.getLineNumber() + "]");
		while (c > 0 && (c = in.read()) != '\n') {
		    System.out.print((char)c);
		}
		System.out.println();
	    }
*/

	    System.in.read();
        }
    }
}
