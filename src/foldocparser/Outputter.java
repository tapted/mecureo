package foldocparser;

import hashgraph.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

class PeerageComparator implements java.util.Comparator {
    public int compare(Object o1, Object o2) {
        Integer i1 = new Integer(((Node)o1).size());
        Integer i2 = new Integer(((Node)o2).size());
        return i2.compareTo(i1);
    }
    public boolean equals(Object o1, Object o2) {
        return ((Node)o1).size() == ((Node)o2).size();
    }
}

class NodeLink implements Comparable {
    public Node n;
    public String s;
    public NodeLink(Node n, String s) {
        this.n = n;
        this.s = s;
    }
    public boolean equals(Object o) {
        return (o instanceof NodeLink && ((NodeLink)o).n.equals(n));
    }
    //compare by NODE COSTS
    public int compareTo(Object o) {
        if (equals(o))
            return 0;
        else if (o instanceof NodeLink && n.getDCost() > ((NodeLink)o).n.getDCost())
            return 1;
        else
            return -1;
    }
    //to test ordering
    public String toString() {
        return "" + n.getDCost();
    }
    public int hashCode() {
        return n.hashCode();
    }
}

class NodeNodeLinkSet implements Comparable {
    public Node n;
    public Set kids; //Set of NodeLink s
    public NodeNodeLinkSet(Node n, Set kids) {
        this.n = n;
        this.kids = kids;
    }
    public boolean equals(Object o) {
        return (o instanceof NodeNodeLinkSet && ((NodeNodeLinkSet)o).n.equals(n));
    }
    //compare by NODE COSTS
    public int compareTo(Object o) {
        if (equals(o))
            return 0;
        else if (o instanceof NodeNodeLinkSet &&
                 n.getDCost() > ((NodeNodeLinkSet)o).n.getDCost())
            return 1;
        else
            return -1;
    }
    //to test ordering
    public String toString() {
        return "" + n.getDCost();
    }
    public int hashCode() {
        return n.hashCode();
    }
}

/**
 * A class providing static functions for the textual output
 * of a fdg graph to RDF or DOT
 */
public class Outputter {

    public static boolean SMART_LINKS = true;

    private static final char nl = '\n';
    private static final int DEFAULT_PAD_LEN = 5;
    private static final String RDF_HEADER =

"<?xml version=\"1.0\"?>" + nl +
"" + nl +
"<rdf:rdf" + nl +
"  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + nl +
"  xmlns:dc=\"http://purl.org/metadata/dublin_core/\"" + nl +
"  xmlns:gmp=\"http://www.gmp.usyd.edu.au/schema/resources/\">" + nl +
nl
;

    private static final String RDF_TAIL = "</rdf:rdf>";
    private static final String ABOUT_BEGIN = "http://foldoc.doc.ic.ac.uk/foldoc/foldoc.cgi?query=";
    private static final String HCI_ABOUT_BEGIN = "http://www.usabilityfirst.com/glossary/term_";
    private static final String HCI_ABOUT_TAIL = ".txl";
    private static final DecimalFormat rdfNumForm = new DecimalFormat("\"0.000000\"");

    private static final double VERT_EPSILON = 0; //increasing this will make a vertical dot graph wider

    private static String toRDFAbout(String key) {
        StringBuffer cur = new StringBuffer(ABOUT_BEGIN);
        //replace ampersands and spaces with %escape and remove quotation marks
        for (int i = 0; i < key.length(); ++i) {
            char c = key.charAt(i);
            switch (c) {
            case '&':
                cur.append("%26");
                break;
            case ' ':
                cur.append("%20");
                break;
            case '"':
                break;
            default:
                if (!Character.isLetterOrDigit(c))
                    cur.append("%" + Integer.toHexString((int)c));
                else
                    cur.append(c);
            }
        }
        return cur.toString();
    }

    private static int counter = 0;
    private static String toHCIAbout(String key, HashGraph g) {
        Node n = (Node)g.get(key);
        //System.err.println("g.get(" + key + ").webID = " + n.webID);
        if (n.webID.equals("0")) {
            counter++;
            n.webID = "-" + counter;
        }
        return HCI_ABOUT_BEGIN + n.webID + HCI_ABOUT_TAIL;
    }

    private static String toRDFTitle(String key) {
        StringBuffer cur = new StringBuffer();
        //replace ampersands with &amp; and remove quotation marks
        for (int i = 0; i < key.length(); ++i) {
            char c = key.charAt(i);
            switch (c) {
            case '&':
                cur.append("&amp;");
                break;
            case '"':
                break;
            default:
                if (c > 127)
                    cur.append("%" + Integer.toHexString((int)c));
                else
                    cur.append(c);
            }
        }
        return cur.toString();
    }

    public static void main (String[] args) {
        System.out.println(rdfNumForm.format(0.0012));
    }

    /**
     * Outputting the DOT graph (to dot langauge)
     * Provide some defaults for the parameters (also so that existing code
     * wasn't broken when more options were added
     */
    public static void outputDOT(HashGraph g, PrintWriter dot) throws IOException {
        outputDOT(g, dot, 1, 1, false, true);
    }

    public static void outputVerticalDOT(HashGraph g, PrintWriter dot) throws IOException {
        outputDOT(g, dot, 1, 1);
    }

    public static void outputDOT(HashGraph g,
                                 PrintWriter dot,
                                 int MIN_KIDS,
                                 int MIN_LINKS) throws IOException {
        outputDOT(g, dot, MIN_KIDS, MIN_LINKS, true, true);
    }

    public static void outputDOT(HashGraph g,
                                 PrintWriter dot,
                                 int MIN_KIDS,
                                 int MIN_LINKS,
                                 boolean vert,
                                 boolean lineStyles) throws IOException {
        outputDOT(g, dot, MIN_KIDS, MIN_LINKS, vert, lineStyles, null);
    }

    public static void autoDOTCluster(HashGraph g,
                                 PrintWriter dot,
                                 int MIN_KIDS,
                                 int MIN_LINKS) throws IOException {
        ArrayList subgraphs = new ArrayList();
        subgraphs.add("Target");
        subgraphs.add("Source");
        subgraphs.add("Common");
        outputDOT(g, dot, MIN_KIDS, MIN_LINKS, true, true, subgraphs.iterator());
    }

    //order for subgraphs (Strings) are
    //'THIS' [target], 'OTHER', and 'COMMON'
    public static void outputDOT(HashGraph g,
                                 PrintWriter dot,
                                 int MIN_KIDS,
                                 int MIN_LINKS,
                                 boolean vert,
                                 boolean lineStyles,
                                 Iterator subgraphs) throws IOException {

        System.err.println("Outputting DOT...");

        //"dashed", "dotted", "solid", "invis" and "bold"
//	PrintWriter dot = new PrintWriter(new BufferedWriter(
//					   new FileWriter(DOT_FILE_OUTPUT)));
        HashSet in = new HashSet();
        ArrayList sg = new ArrayList(g.getNodes());
        Collections.sort(sg, new PeerageComparator());

        ArrayList outputSet = new ArrayList(); //of NodeNodeLinkSet s

        dot.print("digraph G {\n  ");
        if (subgraphs != null) {
            //print the node listings for each subgraph then proceed as usual
            ArrayList[] subnodes = {new ArrayList(), new ArrayList(), new ArrayList()};

            //collect names
            for(Iterator nodes = sg.iterator(); nodes.hasNext(); ) {
                Node n = (Node)nodes.next();
                String ns = serialToStr(n.getSerial());
                switch (n.temp) {
                case HashGraph.MERGE_THIS:
                    subnodes[0].add(ns);
                    break;
                case HashGraph.MERGE_OTHER:
                    subnodes[1].add(ns);
                    break;
                case HashGraph.MERGE_COMMON:
                    subnodes[2].add(ns);
                    break;
                default:
                    throw new RuntimeException("Cannot cluster an unmerged graph");
                }
            }

            //output

            dot.print("compound = true;\nremincross = true;\n");

            for (int i = 0; i < subnodes.length; ++i) {
                String name = subgraphs.next().toString();
                dot.print("subgraph cluster" + name + " {\n" +
                          "\tlabel = \"" + name + "\";\n\t");
                for (Iterator names = subnodes[i].iterator(); names.hasNext(); ) {
                    dot.print(names.next() + ";");
                }
                dot.print("\n}\n");
            }
        }

        //CONVERT GRAPH to a set of NodeNodeLinkSet objects
        for(Iterator nodes = sg.iterator(); nodes.hasNext(); ) {
            Node n = (Node)nodes.next();
            if ((n.size() + n.getBackLinks().size()) /*/ 2*/ < MIN_KIDS)
                continue;
            in.add(n);
            String ns = serialToStr(n.getSerial());
            //output children
            //generate kids from bidirectional links: CHILDREN ONLY

            HashSet kids = new HashSet();
            for (Iterator fwd = n.values().iterator(); fwd.hasNext(); ) {
                Link ln = (Link)fwd.next();
                Node c = ln.getChild();
                if (!c.equals(n) && (c.size() + c.getBackLinks().size()) /*/ 2*/ >= MIN_LINKS) {
                    in.add(c);
                    kids.add(new NodeLink(c, ln.dotType()));
                }
            }
            for (Iterator back = n.backIter(); back.hasNext(); ) {
                Link ln = (Link)back.next();
                Node c = ln.getChild();
                if (!c.equals(n) && (c.size() + c.getBackLinks().size()) /*/ 2*/ >= MIN_LINKS) {
                    in.add(c);
                    kids.add(new NodeLink(c, ln.dotType()));
                }
            }

            /* changed 2002-07-12 for vertical DOT,  THA

            for (Iterator kit = kids.iterator(); kit.hasNext(); ) {
                NodeLink nlk = (NodeLink)kit.next();
                //in.add(k);
                String ks = serialToStr(nlk.n.getSerial());
                dot.print(ns + " -> " + ks);
                if (lineStyles)
                    dot.print("[style=\"" + nlk.s + "\"]");
                dot.print(" ; ");
            }

            */
            if (!kids.isEmpty())
                outputSet.add(new NodeNodeLinkSet(n, kids));

        }

        Collections.sort(outputSet); //order by increasing node costs

        double lastCost = 0;
        if (outputSet.size() > 0)
            lastCost = ((NodeNodeLinkSet)outputSet.get(0)).n.getDCost();

        //OUTPUT EDGES
        for (Iterator nit = outputSet.iterator(); nit.hasNext(); ) {
            NodeNodeLinkSet nnls = (NodeNodeLinkSet)nit.next();
            String ns = serialToStr(nnls.n.getSerial());
            if (vert && nnls.n.getDCost() > lastCost + VERT_EPSILON) {
                dot.print('\n');
                lastCost = nnls.n.getDCost();
            }
            for (Iterator kit = nnls.kids.iterator(); kit.hasNext(); ) {
                NodeLink nlk = (NodeLink)kit.next();
                String ks = serialToStr(nlk.n.getSerial());
                dot.print(ns + " -> " + ks);
                if (lineStyles)
//		    dot.print("[style=\"" + nlk.s + "\"]");
                    dot.print(nlk.s);
                dot.print("; ");
            }
        }

//	System.err.println("Outputting " + in.size() + " nodes");

        //OUTPUT NODE LABELS
        for(Iterator nodes = in.iterator(); nodes.hasNext(); ) {
            Node n = (Node)nodes.next();
            String ns = serialToStr(n.getSerial());
            dot.print("\n  " + ns + " [label=\"" + n.getKey() + "\"];");
        }


        //output tail
        dot.println("\n}");
//	dot.close();

    }

    public static void outputRDF(HashGraph g, PrintWriter rdf) throws IOException {
        outputRDF(g, rdf, Math.max(3, (int)Math.ceil(Math.log(g.size()) / Math.log(10))));
    }

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
//        Collections.sort(strings);


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
            rdf.println("  <rdf:Description about=\"" + toRDFAbout(key) + "\">");
            rdf.println("    <dc:Title>" + toRDFTitle(key) + "</dc:Title>");
            rdf.println("    <gmp:results rdf:parseType=\"Resource\" gmp:dataset=\"gmp:average\" gmp:mark=" +
                        rdfNumForm.format(norm.norm(n.getDCost())) + " gmp:reliability=" +
                        rdfNumForm.format(norm.norm(n.getDCost())) + "/>");
            //output children
            //generate kids from _bidirectional_ links
            HashSet kids = new HashSet();

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
            
            //add backward links first so that forward links override.
            for (Iterator back = n.backIter(); back.hasNext(); ) {
//		kids.add(((Link)back.next()).not(n));
                //backlinks go the other way
                if (SMART_LINKS) {
                    Link l = (Link)back.next();
                    if (l.getTok().isStrictParent()) {
                        kids.add(l.reverseDir());
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
                //		kids.add(((Link)fwd.next()).not(n));
                if (SMART_LINKS) {
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
//	    kids.remove(n);
            HashSet linked = new HashSet(); //don't link a node twice
            linked.add(n);		    //never link to itself

            for (Iterator kit = kids.iterator(); kit.hasNext(); ) {
//		String kk = (String)((Node)kit.next()).getKey();
                Link l = (Link)kit.next();
                Node peer = l.not(n);
                if (linked.contains(peer)) continue; //already linked
                linked.add(peer);
                String kk = peer.getKey();
                //fixed //if (kk.indexOf("\"") < 0)
                //		rdf.println("	 <gmp:peer rdf:resource=\"" + toRDFAbout(kk) +"\"/>");
                //		 <gmp:peer gmp:peerType="actor			 " rdf:resource="http://www.imdb.com/Title?0017324"/>
                rdf.println("	 <gmp:peer gmp:peerType=\"" + l.typeString() + "\" rdf:resource=\"" + toRDFAbout(kk) + "\"/>");
            }
            //output tail
            rdf.println("  </rdf:Description>");
        }
        rdf.println(RDF_TAIL);
//	rdf.close();
    }

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
            rdf.println("    <dc:Title>" + toRDFTitle(key) + "</dc:Title>");
            rdf.println("    <gmp:results rdf:parseType=\"Resource\" gmp:dataset=\"gmp:average\" gmp:mark=" +
                        rdfNumForm.format(norm.norm(n.getDCost())) + " gmp:reliability=" +
                        rdfNumForm.format(norm.norm(n.getDCost())) + "/>");
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
                if (SMART_LINKS) {
                    Link l = (Link)back.next();
                    if (l.getTok().isStrictParent()) {
                        kids.add(l.reverseDir());
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
                if (SMART_LINKS) {
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
        replaces.put("initialnode", toRDFTitle(startNode));
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

    public static String serialToStr(int serial) {
        String s = "";
        while (serial > 0) {
            s = s + (char)('a' + serial % 26);
            serial /= 26;
        }
        return s;
    }
}

