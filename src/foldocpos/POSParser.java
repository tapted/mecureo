package foldocpos;

import foldocparser.*;
import hashgraph.*;
import posparser.*;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.xml.sax.SAXException;

/**
 * An NLP Parser for a FOLDOC-like dictionary - creating an
 * ontology utilising part-of-speech information
 */
public class POSParser extends Parser {

    private static final boolean DEBUGGING = true;

    /**
     * The maximum number of tagged words in the first
     * sentence of a definition for an unclassified relationship
     * to be classified as the "weak synonym" type
     */
    protected static final int WEAK_SYN_MAX_TAGGED = 4;

    protected static final int UNCLAS_SKIP_UPTO = 300;

    public static boolean NLP_OUTPUT = false;
    public static boolean SHOW_UNCLAS = false;
    
    public POSParser() {
        super();
    }

    protected POSToker pt;
    protected DictHandler dh;

    protected int gooddefs;
    
    public HashGraph parse(String fileName) throws IOException {

        System.err.println("\nParsing in POSParser...");
        
	POSToker.S_OUTTOK = NLP_OUTPUT;

        preParse(fileName);
        logfile = new PrintWriter(
                                  new BufferedWriter(
                                  new FileWriter("detected2.log")));
        makeStems();

        Reader in;
        
        if (fileName.endsWith(".gz")) {
            in = new InputStreamReader(
                                       new GZIPInputStream(
                                       new FileInputStream(fileName)));
        } else {
            in = new FileReader(fileName);
        }

        String s;
        
        pt = new POSToker(this, in);
        dh = new DictHandler(pt);
        t = new Tokenizer(0); //dummy toker for line numbers

        int bad_defs = 0;
        gooddefs = 0;

        while ((s = pt.nextDefinition()) != null) {
            try {
                if (NLP_OUTPUT || SHOW_UNCLAS) System.err.println("=======" + s + "=======");
                definition(s);
                System.err.print("\033[1G" + (++gooddefs));
            } catch (Exception e) {
                bad_defs++;
                System.err.println("Uncaught Exception #" + bad_defs + ": the NLP parser may have got lost in " +
                                  s + "\non or around [" + pt.input.getLineNumber() + "]. Details:");
                e.printStackTrace();
                System.err.println("Press Enter to continue");
                System.in.read();
            }
        }

        System.err.println("Had " + bad_defs + " exceptions");
        logfile.close();
        return graph;
    }

    /* DEBUGGING for classifying tags */
    HashMap tagSet;
    /* DEBUGGING for classifying keywords */
    HashMap sentence = new HashMap();

    void mapTag(HashMap hm, String tag, String word) {
        if (hm.containsKey(tag)) {
            ((ArrayList)hm.get(tag)).add(word);
        } else {
            ArrayList dummy = new ArrayList();
            dummy.add(word);
            hm.put(tag, dummy);
        }
    }

    /**
     * true if last added tag was the first significant word in a sentence
     */
    boolean firstSignificant = false;
    boolean firstSentence = false;
    
    void addTag(String tag, String word) {
        mapTag(tagSet, tag, word);
        mapTag(sentence, tag, word);
    }

    /** process a definition */
    //double prog; //progress modifier: increase * PROGRESS_MOD each iter
    boolean syn; //indicates synonym link
    Node cur;
    //Token tok;
    Token keyword;
    protected void definition(String d) throws IOException {
	if (!defn_restrict.contains(d))
            return; //ignore: it's been restricted
        
        prog = 0;
        syn = true;
        cur = graph.getAddNode(d);
        keyword = Token.T_NORM;
        curDef = d;
        tagSet = new HashMap();
        firstSentence = true;

        String def = POSWrapper.readLines(pt.input);
        if (NLP_OUTPUT || SHOW_UNCLAS) System.err.println(def);

        try {
            POSWrapper.parse(def, dh);
        } catch (SAXException saxe) {
            saxe.printStackTrace();
        }

        if (NLP_OUTPUT) {
            System.err.println("TAGS for \"" + d + "\":\n" + tagSet);
            dumpNode(d);
 	    if (!d.startsWith("-")) {
		System.err.print("Press Enter to continue...");
                System.in.read();
	    }
        }
    }

    private void dumpNode(String key) {
        Node n = (Node)graph.get(key);
        if (n == null) {
            System.err.println("<<no node created: g[" + key + "] == null>>");
            return;
        }
        System.err.println(n);
        for (Iterator it = n.values().iterator(); it.hasNext(); ) {
            System.err.println("    " + it.next());
        }
        for (Iterator it = n.backIter(); it.hasNext(); ) {
            System.err.println("    " + it.next());
        }
    }

    public void token(Token tok) {
        token(tok, null);
    }

    public static int deepSize(Iterator it) {
        int sz = 0;
        for (;it.hasNext(); sz += ((Collection)it.next()).size());
        return sz;
    }

    public int sigSize() {
        int sz = 0;
        for (Iterator it = tagSet.keySet().iterator(); it.hasNext(); ) {
            String tag = (String)it.next();
            if ( tag.endsWith("NN") || tag.endsWith("NNS") ||
                 tag.endsWith("NNP") || tag.endsWith("NNPS")) {
                //return 1;
                sz += ((Collection)tagSet.get(tag)).size();
            }
        }
        return sz;
    }
    
    public void token(Token tok, String tag) {
        if ( tok.getFlags() == Token.EOD ||
             tok.toString().trim().length() == 0)
            return; //ignore and leave syn

        firstSignificant =
            firstSentence &&
            keyword == Token.T_NORM && //no keyword yet
            deepSize(tagSet.values().iterator()) <= WEAK_SYN_MAX_TAGGED;
        
        if (tag != null) addTag("\n" + tag, tok.toString());

        t.lineNo = pt.input.getLineNumber();
        
        if (tok.getFlags() == Token.EOP){
            //ignore end of paragraph
        } else if ((tok.getFlags() & Token.M_ASSOC) == Token.CATEGORY) {
            category(tok, cur);
            syn = true;
        } else if (tok.getFlags() == Token.K_NOTHING) {
            syn = false;
        } else if (tok.getFlags() == Token.WEB_REF) {
            try {
                Integer.parseInt(tok.toString());
                cur.webID = tok.toString();
            } catch (NumberFormatException nfe) {}
        } else if (tok.getFlags() == Token.EOS) {
            keyword = Token.T_NORM;
            firstSentence = false;
            sentence = new HashMap();
        } else if ((tok.getFlags() & Token.M_TYPE) == Token.H_LINK) {
            if (true /*|| ALLOW_LEAVES || defn_restrict.contains(tok.toString())*/) {
                if (syn) {
                    hlink(tok, cur, prog, Token.T_SYN);
                } else if (firstSignificant) {
                    hlink(tok, cur, prog, Token.T_WEAKSYN);
                } else {
                    boolean added = false;
                    if ((tok.getFlags() & Token.NULL_WORD) ==0) { //then in braces
                        added = hlink(tok, cur, prog, keyword);
                    } else if (tag != null) {
                        //NEW NLP STUFF!!!
                        //Adjust 'keyword' base weight to reflect tag and link status

                        if (tag.equals("NNP") || tag.equals("NNPS")) { //proper nouns
                            added = hlink(tok, cur, prog, keyword);
                        } else {
                            Token tdash = keyword.duplicate();
                            tdash.weaken(); //weaken once
                            if ( tag.equals("NN") || tag.equals("NNS")) {
                                added = hlink(tok, cur, prog, tdash);
                            } else if (tag.equals("VBZ") || tag.equals("JJ")) {
                                tdash.weaken(); //weaken twice
                                added = hlink(tok, cur, prog, tdash);
                            } //else ignore
                        }
                    }
                    if ( DEBUGGING && SHOW_UNCLAS && added &&
                         keyword == Token.T_NORM && //compare reference
                         deepSize(sentence.values().iterator()) > 1) {
                        System.err.println("\"" + tok + "\"[" + tag + "] not classified in " + sentence);
                        System.err.println("Press enter to coninue...");
                        if (UNCLAS_SKIP_UPTO <= gooddefs) {
                            try {System.in.read();} catch (IOException ioe) {}
                        }
                    }
                }
            } else if (category_restrict == null) {
                //print a parse error if not in 'restrict mode'
                if (WARNINGS)
                    System.err.println("[" + pt.input.getLineNumber() +
                                       "] Warning: symbol rejected {" + tok + "}");

            }
            syn = false;
        } else if ((tok.getFlags() & Token.M_TYPE) == Token.K_WORD) {
            if  (keyword == Token.T_NORM || //compare references
                 (tok.getFlags() & Token.M_STRENGTH) >= //GE because closer => more appropriate
                 (keyword.getFlags() & Token.M_STRENGTH))
                keyword = tok;
            syn = false;
        } else {
            syn = false;
        } //syn stays true if got a category
    }

    /** Do a pre-parse so that nodes can be validated */
    protected void preParse(String fileName) throws IOException {
        try {
            BufferedReader catFile = new BufferedReader(new FileReader(CATEGORY_FILE));
            String s;
            while ((s = catFile.readLine()) != null) {
                if (category_restrict == null || category_restrict.contains(s)) {
                    //prepend a hyphen for categories in graph!
                    Node n = graph.getAddNode(CATEGORY_PREPEND + s);
                    categories.put(s, n);
                }
            }

            Reader in;

            if (fileName.endsWith(".gz")) {
                in = new InputStreamReader(
                                           new GZIPInputStream(
                                                               new FileInputStream(fileName)));
            } else {
                in = new FileReader(fileName);
            }

            FOLDOCReader tk = new FOLDOCReader(in);
            tk.PRINT_SKIPPED = false;
//            dh = new DictHandler(pt);
//            t = new Tokenizer(0); //dummy toker for line numbers

            if (category_restrict == null) {
                String q;
                while ((q = tk.nextDef()) != null)
                    defn_restrict.add(q);
                
            } else { //add only definitions in category_restrict
                /*
            Outer:
                while (true) {
                    s = tk.nextDef();
                    Token tok;
                    while ((tok = tk.nextToken()).getFlags() != Token.EOD) {
                        if ((tok.getFlags() & Token.M_ASSOC) == Token.CATEGORY &&
                                     category_restrict.contains(tok.toString())) {
                            defn_restrict.add(s);
                            continue Outer;
                        }
                    }
                    }
                    */
            }
        } catch (EOFException eof) {
            System.err.println("Preparse done: found " + defn_restrict.size() +
                               " defintions for " + graph.size() + " categories.");
        } catch (IOException ioe) {
            System.err.println("IO Error in preparse");
            throw ioe;
	}
    }

}
