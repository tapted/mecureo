package foldocml;

import java.io.*;
import hashgraph.*;
import java.util.*;

/**
 * This class provides the API to the MECUREO toolset
 * <p>
 * It provides access to an [already parsed] ontology in order to
 * perform queries on it. For example, to generate or compare user models in the
 * context of the ontology, a connection is created to represent the ontology from
 * the ontology file using <code>connect</code>. <code>generate</code> is called
 * with a iterator of keywords to generate any number of models. These models may
 * then be compared using one of the <code>ratioSimilar</code> functions.
 * </p>
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class Connection {

    /**
     * The ontology this <code>Connection</code> is connected to
     */
    HashGraph base;

    /**
     * Create a connection to the ontology stored in <code>ontologyGraph</code>
     * @param ontologyGraph the ontology <code>File</code>
     * @return a <code>Connection</code> for <code>ontologyGraph</code>
     * @throws IOException on an IO error
     */
    public static Connection connect(File ontologyGraph) throws IOException {
        return new Connection(openGraph(ontologyGraph));
    }

    /**
     * Open a model (or ontology) stored in the proprietary format in <code>graph</code>
     * @param graph the model <code>File</code>
     * @return a <code>HashGraph</code> that can be used as a parameter for a <code>Connection</code>
     * @throws IOException on an IO error
     */
    public static HashGraph openGraph(File graph) throws IOException  {
        HashGraph g = new HashGraph();
        ObjectInputStream ois;
        
        if (!graph.exists()) {
            String path = graph.getCanonicalPath();
            File f;
            if (graph.getName().endsWith(".gz")) {
                f = new File(path.substring(0, path.length() -3));
            } else {
                f = new File(path + ".gz");
            }
            if (f.exists()) {
                graph = f;
                System.err.println(graph.getCanonicalPath() + " found");
            }
        }
        
        if (graph.getName().endsWith(".gz")) {
            ois = new ObjectInputStream
                (new BufferedInputStream
                 (new java.util.zip.GZIPInputStream
                  (new FileInputStream(graph))));
        } else {
            ois = new ObjectInputStream
                (new BufferedInputStream
                 (new FileInputStream(graph)));
        }
        try {
            g.readData(ois, new foldoccmd.StderrPoster());
        } catch (ClassNotFoundException c) {
        }
        return g;
    }


    /**
     * Output a model as [textual] dot language to the specified output stream with optional clustering into three clusters.
     * <p>The clusters are:
     * <ul><li>Nodes common</li><li>Nodes only in left</li><li>Nodes only in right</li></ul>
     * </p><p>Clustering is only useful after a merge operation on two models<br>
     * If minKids &gt; 0 then only nodes with at least <code>minKids</code> <i>forward</i> &nbsp;links will be output<br>
     * If minLinks &gt; 0 then only nodes with at least <code>minLinks</code> links in either direction will be output<br>
     * In general minKids = minLinks works fine
     * </p>
     * @param g the [merged] model to output
     * @param out the <code>PrintWriter</code> stream to output to
     * @param cluster if true then output is clustered
     * @param minKids minimum number of forward links for a node to be output
     * @param minLinks minimum number of peers for a node to be output
     * @throws IOException on an IO error
     * @see foldocparser.Outputter#autoDOTCluster(HashGraph, PrintWriter, int, int)
     * @see foldocparser.Outputter#outputDOT(HashGraph, PrintWriter, int, int)
     */
    public static void outputDOT(HashGraph g, PrintWriter out, boolean cluster, int minKids, int minLinks) throws IOException {
        if (cluster)
            foldocparser.tools.OutputDOT.autoDOTCluster(g, out, minKids, minLinks);
        else
            foldocparser.tools.OutputDOT.outputDOT(g, out, minKids, minLinks);
        out.flush();
    }

    /**
     * Output a model as [textual] 'dot' language to standard output using default settings
     * <p>The default settings are for no clustering and 0 minimum peerage</p>
     * @param g the model to output
     * @throws IOException on an IO error
     * @see #outputDOT(HashGraph, PrintWriter, boolean, int, int)
     */
    public static void outputDOT(HashGraph g)  throws IOException {
        outputDOT(g, new PrintWriter(System.out));
    }

    /**
     * Output as dot without clustering
     * @param g the model to output
     * @param out the <code>PrintWriter</code> stream to output to
     * @param minKids minimum number of forward links for a node to be output
     * @param minLinks minimum number of peers for a node to be output
     * @throws IOException on an IO error
     * @see #outputDOT(HashGraph, PrintWriter, boolean, int, int)
     */
    public static void outputDOT(HashGraph g, PrintWriter out, int minKids, int minLinks) throws IOException  {
        outputDOT(g, out, false, minKids, minLinks);
    }

    /**
     * Output a model as dot to the specified <code>PrintWriter</code> without
     * clustering and with 0 minumum kids/peerage
     * @param g the model to output
     * @param out the <code>PrintWriter</code> stream to output to
     * @throws IOException on an IO error
     * @see #outputDOT(HashGraph, PrintWriter, boolean, int, int)
     */
    public static void outputDOT(HashGraph g, PrintWriter out)  throws IOException {
        outputDOT(g, out, 0);
    }

    /**
     * Output a model as dot to the specified <code>PrintWriter</code> without
     * clustering and with minKids = minLinks = minPeers
     * @param g the model to output
     * @param out the <code>PrintWriter</code> stream to output to
     * @param minPeers the number minimum number of links=kids as for <code>outputDOT/5</code>
     * @throws IOException on an IO error
     * @see #outputDOT(HashGraph, PrintWriter, boolean, int, int)
     */
    public static void outputDOT(HashGraph g, PrintWriter out, int minPeers)  throws IOException {
        outputDOT(g, out, minPeers, minPeers);
    }

    /**
     * Output a model as [textual] RDF format for VLUM [ie XML] to the specified
     * <code>PrintWriter</code>
     * <p>
     * Note that this output may become verbose for large models.
     * </p>
     * @param g the model to output
     * @param out the <code>PrintWriter</code> stream to output to
     * @throws IOException on an IO error
     * @see foldocparser.Outputter#outputRDF(HashGraph g, PrintWriter out)
     */
    public static void outputRDF(HashGraph g, PrintWriter out) throws IOException {
        foldocparser.tools.OutputRDF.outputRDF(g, out);
        out.flush();
    }

	public static void outputOWL(HashGraph g, PrintWriter out) throws IOException {
		foldocparser.tools.OutputOWL.outputOWL(g, out);
		out.flush();
	}

	public static void outputXML(HashGraph g, PrintWriter out) throws IOException {
		foldocparser.tools.OutputXML.outputXML(g, out);
		out.flush();
	}

    /**
     * Output a model as VLUM RDF for HCI
     * @param g
     * @param out
     * @throws IOException
     * @see #outputRDF(HashGraph g, PrintWriter out)
     */
    public static void outputHCI(HashGraph g, PrintWriter out) throws IOException {
        foldocparser.tools.OutputHCI.outputHCIRDF(g, out);
        out.flush();
    }

    /**
     * Create a connection interface for the given model.
     * <p>
     * Note that models and ontologies are interchangeable - indeed a subset of
     * the ontology may be used by generating a model and creating a connection
     * to it.
     * </p>
     * @param ontology the model/ontology to connect to [give an interface for]
     */
    public Connection(HashGraph ontology) {
        base = ontology;
    }

    /**
     * Generate a [user] model using this <code>Connection</code>
     * @param words an iterator of <code>Object</code>s - for which the <code>.toString()</code>
     * method will be used as a basis for <b><i>matching</i></b> &nbsp;a concept in the ontology.<br>
     * Current matching involves exact string matches.
     * @param distance the depth of the subgraphs formed for each word
     * @param infinity the <i>node weight</i> &nbsp;penalty for nodes not in the subgraphs formed for other words
     * @param log a <code>PrintWriter</code> text stream where verbose logging information is sent
     * @return the generated model
     * @see #grow(Iterator words, double depth)
     * @see foldoccmd.NodeSearch
     * @deprecated the preferred way to generate a model is now <code>grow</code> -
     * it uses a more intelligent procedure that is able to make use of 'focal points'
     * inherent in the word list [ie where a number of the words on the word list
     * are from a small region in the ontology].
     */
    public HashGraph generate(Iterator words,
                       double distance,
                       double infinity,
                       PrintWriter log) {

        HashGraph g = new HashGraph();
        Node n;
        double curInf = 0; //progressive infinity for new subgraphs
        boolean first = true;
        for ( ; words.hasNext(); ) {
            String s = words.next().toString();
            //subgraph
            n = base.get(s);

            if (n != null) {
                if (first) {
                    if (log != null) log.println("Starting generate.");
                    g = base.subgraph(s, distance);
                    first = false;
                } else {
                    g = g.costMerge(base.subgraph(s, distance), infinity, curInf);
                }
                curInf += infinity; //the 'infinity' for nodes already in g
                if (log != null) log.println("Added " + s + ".");
            } else {
                if (log != null) log.println("Couldn't find a node for " + s +"; skipping.");
            }
        }
        g.copyLinks(base); //get all the links from the original graph
        return g;
    }

    /**
     * Generate a [user] model using this <code>Connection</code>
     * @param words an iterator of <code>Object</code>s - for which the <code>.toString()</code>
     * method will be used as a basis for <b><i>matching</i></b> a concept in the ontology.
     * <blockquote>
     * Current matching involves exact string matches. However, <code>foldoccmd.NodeSearch</code>
     * is able to perform more complex matching based on word stems and substring
     * matches when the word stem match fails. It can convert one iterator of words
     * into another, all of whose elements will be exact matches in a given ontology.
     * </blockquote>
     * @param depth the depth [distance] of the model - any path from a node corresponding
     * to a word in <code>words</code> to any other node will be no greater than <code>depth</code>
     * minus the <i>node weight</i> &nbsp;of the start node, except where focal
     * points [ie where a number of the words on the word list are from a small
     * region in the ontology] are identified. Here a 'bonus' is given that will
     * allow paths originating from such focal points to be slightly longer
     * @return the generated model
     * @see hashgraph.HashGraph#grow(Iterator words, double depth)
     */
    public HashGraph grow(Iterator words, double depth) {
        return base.grow(words, depth);
    }
    
    public HashGraph grow(Iterator words, double depth, int maxNodes) {
        return base.grow(words, depth, maxNodes);
    }
    
    public HashGraph sizegrow(Iterator words, double maxNodes) {
        return base.sizegrow(words, maxNodes);
    }
    
    public HashGraph depthgrow(Iterator words, double ddepth) {
        return base.depthgrow(words, ddepth);
    }

    /**
     * Compare model <code>g1</code> with model <code>g2</code> using the
     * <code>Similarity</code> class </code>s</code>. Dump debugging information
     * [ie the results of the comparison] to <code>log</code> under the <code>name</code>
     * of the similarity class.
     * @param g1 the 'left' model*
     * @param g2 the 'right' model* *(most Similiarity functions are symmetric)
     * @param s the Similarity class (function)
     * @param log a <code>PrintStream</code> on which to show debugging information
     * @param name what to call the Similarity class in the debugging information
     * @return the 'ratio similar' return result of the Similarity comparison
     */
    double processSim(HashGraph g1,
                      HashGraph g2,
                      Similarity s,
                      PrintStream log,
                      String name) {
        s.compare(g1, g2);
        if (log != null) {
            log.println("\nDEBUG: " + name);
            log.println("DEBUG: nodesCommon = " + s.nodesCommon);
            log.println("DEBUG: nodesLeft = " + s.nodesLeft);
            log.println("DEBUG: nodesRight = " + s.nodesRight);
            log.println("DEBUG: similar = " + s.similar);
            log.println("DEBUG: commonSim = " + s.commonSim);
            log.println("DEBUG: penalty = " + s.penalty);
        }
        return s.similar;

    }

    /**
     * Perform a <b>Base Nested Similarity</b> analysis<br>
     * <p>
     * This returns a ratio to represent how similiar <code>g2</code> is with
     * the <i>current <code>Connection</code></i> using the same function as
     * 'Global Similarity'
     * </p>
     * @param g2 the model to test
     * @param log if not <code>null</code> where verbose output is sent
     * @return the ratio of how similiar <code>g2</code> is to the connection
     * @see #ratioSimilar1(HashGraph g1, HashGraph g2, PrintStream log)
     */
    public double ratioSimilar0(HashGraph g2,
                         PrintStream log) {
        Similarity s = new Similarity();
        return processSim(base, g2, s, log, "Base Nested Similarity");
    }

    /**
     * Perform a <b>Global Similarity</b><br>
     * <p>
     * This applies an heuristic using the number of nodes common and not common
     * beween g1 and g2, as well as the difference in <i>node weights</i> &nbsp;
     * for nodes that are common and a biased penalty function for nodes that are
     * not common.
     * </p><p>
     * It runs in O(|g1| + |g2|)
     * </p><p>
     * Formally, the value returned is achieved by <br><br>
     * <code>commonSimilarity = sum{1 - abs(|l[i]| - |r[j]|)} / |rc|</code>
     * <br><br>summed for l[i] == r[j]
     * <br>where |rc| == |lc| == the number of nodes common,
     * <br>l[i] is the i-th concept in <code>g1</code>
     * <br>r[j] is the j-th concept in <code>g2</code>
     * <br>|p[k]| is the node weight in p for the k-th concept
     * <br><br>
     * <code>bias = 1 - min{|lu|, |ru|} / max{|lu|, |ru|}</code>
     * <br>
     * <code>penalty = 1.5 - (|lu| + |ru|) / (|lu| + |ru| + |rc| + |lc|)</code>
     * <br>
     * <code>finalSimilarity =  ((bias + penalty)/2)*commonSimilarity</code>
     * <br><br>where |lu| is the number of nodes in <code>g1</code> not in <code>g2</code>
     * <br>and |ru| is the number of nodes in <code>g2</code> not in <code>g1</code>
     * </p><ul>
     * <li>This is with the current contribution constants.
     * </li><li>The contributions of bias
     * and penalty (both separately and together) can be adjusted, but if all
     * comparisons are done with the same constants the relative similarities
     * returned should be consistent.
     * </li><li>'bias' gives a beneficial bias when it is found that one graph is clearly
     * a subset of another - this is helpful for <i>classification</i> &nbsp;
     * comparisons.
     * </li><li>'penalty' penalises the similarity of nodes common [commonSimilarity] by
     * determining the ratio of numbers of nodes not common to those common<br>
     * In practice small offsets are used to simulate Laplace estimators in case
     * any part yeilds zero.
     * </li><li>The final similarity returned combines these values [again with adjustable
     * contribution constants], heuristically.
     * </ul>
     * @param g1 the 'left' model
     * @param g2 the 'right' model
     * @param log if not <code>null</code>, where verbose output is sent
     * @return the ratio of similarity for <code>g1</code> and <code>g2</code>
     * @see Similarity
     * @see DiffDistance
     * @see PenaltyFunc
     */
    public double ratioSimilar1(HashGraph g1,
                         HashGraph g2,
                         PrintStream log) {
        Similarity s = new Similarity();
        return processSim(g1, g2, s, log, "Global Similarity");
    }

    /**
     * Perform a <b>Global Similarity</b> without verbose output
     * @param g1 the 'left' model
     * @param g2 the 'right' model
     * @return the ratio of similarity for <code>g1</code> and <code>g2</code>
     */
    public double ratioSimilar1(HashGraph g1,
                         HashGraph g2) {
        return ratioSimilar1(g1, g2, null);
    }

    /**
     * Perform a <b>Basic Search Similarity</b><br>
     * <p>
     * This uses the same <code>commonSimilarity</code> value as for Global
     * Similarity, but calculates the penalty differently, and is not designed
     * to return a value in [0, 1] - the separate values for commonSimilarity
     * and penalty are to be used to evaluate the similarity as there is not
     * yet a clear way to combine them.
     * </p><p>
     * For each node in <code>g1</code> not in <code>g2</code> the shortest
     * path <i>in the ontology</i> (for this connection) between that node
     * and <i>any</i> node in <code>g2</code> is found. A maximum distance
     * is specified and if the path exceeds that distance [or there is no path]
     * the maximum distance is substituted (the default used here is 5)
     * </p><p>
     * This, and the other '... Search Similarity' algorithms run in worst case
     * that is essentially
     * O((|lu| + |ru|)*NlgN), where N is the size of the ontology. However, the
     * reality is not really this bad because the maximum distance is able to
     * provide early exits for many searches.
     * </p><p>
     * Formally,<br><br>
     * <pre>penalty = sum{min{shortestDist(l[i], r), 5}} +
     * &nbsp;        sum{min{shortestDist(r[j], l), 5}}</pre>
     * <br><ul><li>for l[i] not in r and r[j] not in l</li></ul>
     * </p>
     * @param g1 the 'left' model
     * @param g2 the 'right' model
     * @param log if not <code>null</code> where verbose output is sent
     * @return the ratio of similarity for <code>g1</code> and <code>g2</code>
     * @see Similarity
     * @see DiffDistance
     * @see SearchPenalty
     */
    public double ratioSimilar2(HashGraph g1,
                         HashGraph g2,
                         PrintStream log) {
        Similarity s = new Similarity(new DiffDistance(), new SearchPenalty(base, 5));
        return processSim(g1, g2, s, log, "Basic Search Similarity");
    }

    /**
     * Perform an <b>Averaged Search Similarity</b><br>
     * <p>Similar to Basic Search Similarity, except<br><br>
     * <pre>penalty = (sum{min{shortestDist(l[i], r), 5}} +
       &nbsp;          sum{min{shortestDist(r[j], l), 5}}
       &nbsp;         ) / (|lu| + |ru| + 1)</pre>
     * </p>
     * @param g1 the 'left' model
     * @param g2 the 'right' model
     * @param log if not <code>null</code> where verbose output is sent
     * @return the ratio of similarity for <code>g1</code> and <code>g2</code>
     * @see Similarity
     * @see DiffDistance
     * @see AveragedSearchPenalty
     */
    public double ratioSimilar3(HashGraph g1,
                         HashGraph g2,
                         PrintStream log) {
        Similarity s = new Similarity(new DiffDistance(), new AveragedSearchPenalty(base, 5));
        return processSim(g1, g2, s, log, "Averaged Search Similarity");
    }

    /**
     * Perform a <b>Normalised Search Similarity</b><br>
     * <p>Similar to Basic Search Similarity, except<br><br>
     * <pre>penalty = (sum{min{shortestDist(l[i], r), 5} / 5} +
       &nbsp;          sum{min{shortestDist(r[j], l), 5} / 5})
       </pre>
     * </p>
     * @param g1 the 'left' model
     * @param g2 the 'right' model
     * @param log if not <code>null</code> where verbose output is sent
     * @return the ratio of similarity for <code>g1</code> and <code>g2</code>
     * @see Similarity
     * @see DiffDistance
     * @see NormalisedSearchPenalty
     */
    public double ratioSimilar4(HashGraph g1,
                         HashGraph g2,
                         PrintStream log) {
        Similarity s = new Similarity(new DiffDistance(), new NormalisedSearchPenalty(base, 5));
        return processSim(g1, g2, s, log, "Normalised Search Similarity");
    }

    /**
     * Perform an <b>Averaged Normalised Search Similarity</b><br>
     * <p>Similar to Normalised Search Similarity, except<br><br>
     * <pre>penalty = 1 - (sum{min{shortestDist(l[i], r), 5} / 5} +
       &nbsp;              sum{min{shortestDist(r[j], l), 5} / 5}
       &nbsp;             ) / (|lu| + |ru| + |lc| + |rc|)</pre>
     * </p>
     * <ul><li>Unlike the other search methods, this is able to give a final
     * 'ratio similar' that is in [0, 1], and realistically representative of
     * the actual % similar of the graphs</li></ul>
     * @param g1 the 'left' model
     * @param g2 the 'right' model
     * @param log if not <code>null</code> where verbose output is sent
     * @return the ratio of similarity for <code>g1</code> and <code>g2</code>
     * @see Similarity
     * @see DiffDistance
     * @see AvgNormSearchPenalty
     */
    public double ratioSimilar5(HashGraph g1,
                         HashGraph g2,
                         PrintStream log) {
        Similarity s = new Similarity(new DiffDistance(), new AvgNormSearchPenalty(base, 5));
        return processSim(g1, g2, s, log, "Averaged Normalised Search Similarity");
    }

    /**
     * Perform a <b>Minimum Spanning Tree Similarity</b><br>
     * <p>
     * Perhaps surprising in its simplicity, but also surprising in its effectiveness.
     * This method simple merges <code>g1</code> and <code>g2</code>, into
     * <code>g3</code>, say. Then computes minimum spanning trees for all three
     * models and sums the edge weights of each tree: <code>w1, w2, w3</code>, say.
     * Then,
     * <pre>similarity = ((w1 + w2) / (2 * w3)  -  0.5) * 2</pre>
     * </p><p>
     * The logic is this: <pre>
avg{w1, w2} = (w1 + w2)/2 &lt;= w3 &lt;= (w1 + w2)
so
0.5 &lt;= (w1 + w2) / (2 * w3) &lt;= 1.0</pre>
     * and where the value lies in this range has some reflection on the similarity
     * of the two models
     * </p><p>
     * The potential runtime using Prim's algorithm and Fibonnacci heaps is
     * O(|g3|*lg(|g3|)), but until I implement these in Java the current
     * runtime is more like O(|g3|*|g3|)
     * </p>
     * @param g1 the 'left' model
     * @param g2 the 'right' model
     * @param log if not <code>null</code> where verbose output is sent
     * @return the ratio of similarity for <code>g1</code> and <code>g2</code>
     * @see SpanSimilar
     */
    public double ratioSimilar6(HashGraph g1,
                         HashGraph g2,
                         PrintStream log) {
        Similarity s = new SpanSimilar(base);
        return processSim(g1, g2, s, log, "Minimum Spanning Tree Similarity");
    }
}
