package foldocpos;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import hashgraph.*;

import foldocparser.*;

/**
 * <p>The NLP version of the command line Parse wrapper (for POSParser)</p>
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class POSParse {

    //private static final String RDF_FILE_OUTPUT = "foldoc.rdf";
    //private static final String DOT_FILE_OUTPUT = "foldocall.dot";

    private static HashGraph g;

    /**
     * Usage: java ... Parse inputFile [options] outputFile
     <p>Options:
<pre>
    -l      : Allow Leaves
    -w      : Show warnings
    -s      : Show statistics
    -c File : use File (next param) as category file
</pre>
     if inputFile ends in \".gz\" GZIPInputStreams will be used
     </p>
     * @param args [usage]
     * @throws Exception if an IO error occurs
     */
    public static void main(String[] args) throws Exception {
        OutputStream out = null;
        if (args.length < 1) {
            System.err.println("Usage: java ... POSParse inputFile [options] outputFile\n" +
                               "Options:\n" +
                               "\t-l      : Allow Leaves\n" +
                               "\t-w      : Show warnings\n" +
                               "\t-s      : Show statistics\n" +
                               "\t-c File : use File (next param) as category file\n" +
                               "\t-C Int  : use Int (next param) as the 'C' value [default 10]\n" +
			       "\t-m [-1,0,1,2,3,4,5]: set the Match Level -1:None 5:Substring matching\n" +	
                               "if inputFile ends in \".gz\" GZIPInputStreams will be used");
            return;
        }
        boolean STATS = false;
        for (int i = 1; i < args.length; ++i) {
            if (args[i].startsWith("-")) {
                if (args[i].indexOf('l') >= 0)
                    Parser.ALLOW_LEAVES = true;
                if (args[i].indexOf('w') >= 0)
                    Parser.WARNINGS = true;
                if (args[i].indexOf('s') >= 0)
                    STATS = true;
                if (args[i].indexOf('c') >= 0 && i + 1 < args.length)
                    Parser.CATEGORY_FILE = args[++i];
                if (args[i].indexOf('C') >= 0 && i + 1 < args.length)
                    Parser.WEIGHT_C = Integer.parseInt(args[++i]);
		if (args[i].indexOf('m') >= 0 && i + 1 < args.length)
		    Parser.MATCH_LEVEL = Integer.parseInt(args[++i]);

            } else if (i == args.length - 1) {
                out = new FileOutputStream(args[i]);
            } else {
                System.err.println("Unknown option: " + args[i]);
                return;
            }
        }
        if (out == null) {
            System.err.println("You must specify an output filename");
            return;
        }
	System.err.print("Using detect match level: ");
	switch (Parser.MATCH_LEVEL) {
	    case (-1): System.out.println("-1: No Matching"); break;
            case (0): System.out.println(" 0: Exact Matching Only"); break;
	    case (1): System.out.println(" 1: Trimed Matching"); break;
	    case (2): System.out.println(" 2: Alphanumeric Matching"); break;
	    case (3): System.out.println(" 3: Case Insensitive Matching"); break;
	    case (4): System.out.println(" 4: Componenent Stem Matching"); break;
	    case (5): System.out.println(" 5: Substring Matching"); break;
	    default:  System.out.println(" <<Unknown match level>>");
	}
	if (STATS) {
            doStats(args[0]);
        }

        POSParser p = new POSParser();
        g = p.parse(args[0]);
        System.out.println("Categories");
        java.util.Iterator it = p.categories.values().iterator();
        for (;it.hasNext();)
            System.out.println(it.next());

        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(out));

        g.writeData(oos);

        System.out.println("Output graph to " + args[args.length - 1]);

        foldocparser.Parse.showCStats(g);

/*
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                                                      new FileInputStream("graph.ser")));
        g = new HashGraph();
        g.readData(ois);
        System.out.println("read object");
//	System.out.println(g);

        g.allCosts(g.get("type"));
        System.out.println(g.get("type"));
        System.out.println(g.get("type").values());
        System.out.println(g.get("type").getBackLinks().values());
        System.out.println(g.get("interpreter"));
        System.out.println(g.get("pointer"));
        //System.out.println(g);
*/
        //g.remove("programming");
        //System.out.println(g.subgraph("type", 5));

        //outputDOT();
    }

    //System.out.println(p.graph);
    /**
     * Prints the parse statistics for <code>fileName</code> (when -s is used)
     * @param fileName the dictionary
     * @throws Exception if an IO error occurs
     */
    public static void doStats(String fileName) throws Exception {
        int i = 0;
        int defs = 0;
        int cats = 0;
        int nothing = 0;
        int reset = 0;
        int links = 0;
        int kw = 0;
        long start = System.currentTimeMillis();
        try {
            Tokenizer t;
            if (fileName.endsWith(".gz")) {
                t = new Tokenizer(new BufferedInputStream(
                                        new GZIPInputStream(
                                        new FileInputStream(fileName))));
            } else {
                t = new Tokenizer(new BufferedInputStream(
                                        new FileInputStream(fileName)));
            }
            System.out.println(t.nextDefinition());
            for (i = 0; i < 1000000; ++i) {
                Token tk = t.nextToken();
                if (tk.getFlags() == Token.EOD) {
                    /*System.out.println(*/t.nextDefinition()/*)*/;
                    defs++;
                } else if ((tk.getFlags() & Token.M_ASSOC) == Token.CATEGORY) {
                    //System.out.println("\t" + tk.toString());
                    cats++;
                } else if (tk.getFlags() == Token.K_NOTHING) {
                    nothing++;
                    //System.out.print(" ? " + tk.toString());
                } else if (tk.getFlags() == Token.EOS) {
                    //System.out.println("\t\t\t/----/");
                    reset++;
                } else if ((tk.getFlags() & Token.M_TYPE) == Token.H_LINK) {
                    //System.out.println("\t\t: " + tk.toString());
                    links++;
                } else if ((tk.getFlags() & Token.M_TYPE) == Token.K_WORD) {
                    //System.out.println("\t\t\t" + tk.toString());
                    kw++;
                }
            }
        } catch (EOFException e) {
            System.out.println("\nTotal Tokens processed = " + i);
            System.out.println("Definitions = " + defs);
            System.out.println("Category links = " + cats);
            System.out.println("Plus other links = " + links);
            System.out.println("Keywords processed = " + kw);
            System.out.println("Times keyword tracking reset = " + reset);
            System.out.println("Tokens ignored = " + nothing);
            System.out.println("Completed in " + (System.currentTimeMillis() - start) + " ms");
            //throw e;
        }
    }
}
