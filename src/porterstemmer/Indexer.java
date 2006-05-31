package porterstemmer;

import java.io.*;
import java.util.*;

/**
 * Class for performing component stem matchings using the porters stemmer
 * <p> </p>
 * @author Trent Apted
 * @version 0.2
 */
public class Indexer {

    /** An instance of the Porters' stemmer */
    Stemmer st;
    /** The stems of all the words in the word list */
    HashMap stems = new HashMap(); /* String -> ArrayList[SDPair] */
    /** The exact strings, for initial exact matches */
    HashSet exact = new HashSet();

    /**
     * return the component stem of string str using Porters Stemmer
     * @param str The string to stem
     * @return the stem of str
     */
    public String stem(String str) {
        str = str.toLowerCase();
        String stem = "";
        char c = 0;
        for (int i = 0; i < str.length(); ++i) {
            while (i < str.length() && Character.isLetter(c = str.charAt(i))) {
                st.add(c);
                ++i;
            }
            st.stem();
            stem += st.toString();
            if (i < str.length()) stem += c;
        }
        return stem;
    }

    /**
     * Create an Indexer from the file wordList
     * @param wordList a valid File with each entry to stem on a new line
     * @throws IOException on an IO error
     */
    public Indexer(File wordList) throws IOException {
        st = new Stemmer();
        BufferedReader in = new BufferedReader(new FileReader(wordList));
        String str;
        while ((str = in.readLine()) != null) {
            //System.err.print(str + " -> ");
            String stem = stem(str);
            //System.err.println(stem);
            if (stem.length() > 0) {
                ArrayList sdpa = (ArrayList)stems.get(stem);
                if (sdpa == null)
                    stems.put(stem, (sdpa = new ArrayList()));
//                sdpa.add(new SDPair((byte)(str.length() - stem.length()), str));
				sdpa.add(new SDPair(EditDist.dist(str, stem), str));
                exact.add(str);
            }
        }
        for (Iterator it = stems.values().iterator(); it.hasNext(); )
            Collections.sort((ArrayList)it.next());
    }

    /**
     * Create an <code>Indexer</code> from the words in <code>it</code>
     * <p>Runs O(n) of the word list but only needs to run once</p>
     * <p>This fills a hashmap with the component stems of all the words so that
     * match() can run in O(1)</p>
     * @param it an iterator&lt;Object&gt;: the .toString() method is called for words
     */
    public Indexer(Iterator it) {
        st = new Stemmer();
        for (; it.hasNext(); ) {
            String str = it.next().toString();
            String stem = stem(str);
            if (stem.length() > 0) {
                ArrayList sdpa = (ArrayList)stems.get(stem);
                if (sdpa == null)
                    stems.put(stem, (sdpa = new ArrayList()));
//                sdpa.add(new SDPair((byte)(str.length() - stem.length()), str));
				sdpa.add(new SDPair(EditDist.dist(str, stem), str));
                exact.add(str);
            }
        }
        for (Iterator it2 = stems.values().iterator(); it2.hasNext(); )
            Collections.sort((ArrayList)it2.next());
    }

    void resortMatchesByEditDistFrom(String str, ArrayList sdpa) {
        for (Iterator it = sdpa.iterator(); it.hasNext(); ) {
        	SDPair n = (SDPair)it.next();
        	n.dist = EditDist.dist(str, n.str);
        }
        Collections.sort(sdpa);
        foldoccmd.RunLog.log1.append(str + " --> " + sdpa + "\n");
    }
    
    void filterListBy(String str, ArrayList sdpa) {
    	int sz = sdpa.size();
    	/* remove acronym-like words from sdpa if they are not an exact match */
    	for (ListIterator it = sdpa.listIterator(); it.hasNext(); ) {
    		String s = ((SDPair)it.next()).str;
    		if  (!(s.indexOf(' ') >= 0) &&
    			 !s.substring(1).equals(s.substring(1).toLowerCase()) && 
    			 !s.equalsIgnoreCase(str)) {
    			sz--;
    			foldoccmd.RunLog.log2.append("Filt. " + s + "\t\t because it is acronym-like and unlike       " + str + "\t (" + sz + " remain)\n");
    			it.remove();
    		}
    	}    		
    }
    
    /**
     * Return the component stemming matches to s
     * <p>Runs in O(1) of the wordList</p>
     * @param s the string to stem and match
     * @return an array of &lt;distance, string&gt; pairs in the wordList that matched
     */
    public SDPair[] match(String s) {
    	return match(s, 0);
    }
    public SDPair[] match(String s, int min_stemlen) {
        SDPair[] ret;
        int i = 0;
        String stemmed = stem(s);
        ArrayList sdpa = null;
        if (s.length() >= min_stemlen) {
        	sdpa = (ArrayList)stems.get(stemmed);
        	if (sdpa != null) {
        		sdpa = (ArrayList)sdpa.clone();
        		filterListBy(s, sdpa);
        		resortMatchesByEditDistFrom(s, sdpa);
        	}
        }
        if (sdpa == null)
            sdpa = new ArrayList(); //make a sentinel
        boolean inList = false;
        for (Iterator it = sdpa.iterator(); !inList && it.hasNext(); )
            inList = ((SDPair)it.next()).str.equals(s);
        if (exact.contains(s)) {
            if (!inList)
                ret = new SDPair[sdpa.size() + 1];
            else
                ret = new SDPair[sdpa.size()];
            ret[i++] = new SDPair((byte)0, s);
        } else {
            ret = new SDPair[sdpa.size()];
        }
        for (Iterator it = sdpa.iterator(); it.hasNext(); ) {
            SDPair sdp = (SDPair)it.next();
            if (!sdp.str.equals(s))
                ret[i++] = sdp;
        }
        return ret;
    }

    /**
     * Split a string into words
     * @param s the string to split
     * @return an ArrayList of Strings, each a single word
     */
    ArrayList split(String s) {
        ArrayList ret = new ArrayList();
        StringBuffer sb;
        for (int i = 0; i < s.length(); ++i) {
            sb = new StringBuffer();
            for (; i < s.length() && !Character.isLetter(s.charAt(i)); ++i);
            for (; i < s.length() && Character.isLetter(s.charAt(i)); ++i)
                sb.append(s.charAt(i));
            if (sb.length() > 1) /*otherwise too many matches*/
            ret.add(stem(sb.toString()));
        }
        return ret;
    }

    /**
     * Splits s into component words and performs a substring search on
     * all words in the wordList
     * <p>Runs in O(n) of the wordList</p>
     * @param s the string to scan for
     * @return an ArrayList of SDPairs containing nodes found, sorted by increasing distance
     */
    public ArrayList lookHarder(String s) {
        ArrayList ret = new ArrayList();
        ArrayList words = split(s);
        for (int i = 0; i < words.size(); ++i) {
//            for (Iterator it = lower.entrySet().iterator(); it.hasNext(); ) {
            for (Iterator it = exact.iterator(); it.hasNext(); ) {
//                Map.Entry me = (Map.Entry)it.next();
//                String se = (String)me.getKey();
                String se = (String)it.next();
                String sl = se.toLowerCase();
                int dist = se.length();
                for (int j = i; j < words.size(); ++j) {
                    if (sl.indexOf((String)words.get(j)) >= 0)
                        dist -= ((String)words.get(j)).length();
                }
                if (dist < 0)
                    dist = 0;
                if (dist != se.length()) {
                    if (dist > 127)
                        dist = 127;
//                    ret.add(new SDPair((byte)dist, (String)me.getValue()));
//                    ret.add(new SDPair((byte)dist, se));
					ret.add(new SDPair(EditDist.dist(s, se), se));
                }
            }
        }
        Collections.sort(ret);
        return ret;
    }

    /**
     * Dump the contents of the stems mapping to stdout
     * @deprecated Map.Entry is no longer public in the JDK, so this doesn't compile
     */
    public void dump() {
        /*
        This no longer works in the current version of jdk...
        for (Iterator it = stems.entrySet().iterator(); it.hasNext(); ) {
            HashMap.Entry e = (HashMap.Entry)it.next();
            ArrayList sdpa = (ArrayList)e.getValue();
            if (sdpa.size() < 2) continue;
            System.out.println(e.getKey().toString() + " * " + sdpa.size());
            for (Iterator it2 = sdpa.iterator(); it2.hasNext(); ) {
                SDPair sdp = (SDPair)it2.next();
                System.out.println("\t" + sdp.dist + ": " + sdp.str);
            }
        }
        */
    }


    /**
     * A tester: returns the matching for each line given on standard input
     * using the possibilities in "./txt/nodenames.txt"
     * @param args not used
     * @throws IOException on an IO error
     */
    public static void main(String[] args) throws IOException {
        Indexer idx = new Indexer(new File("txt" + File.separator + "nodenames.txt"));
        //idx.dump();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;
        while ((s = in.readLine()) != null) {
            SDPair[] arr = idx.match(s);
            for (int i = 0; i < arr.length; ++i)
                System.out.println(arr[i].str + "(" + arr[i].dist + ")");
            if (arr.length == 0) {
                ArrayList more = idx.lookHarder(s);
                for (Iterator it = more.iterator(); it.hasNext(); ) {
                    SDPair sdp = (SDPair)it.next();
                    System.out.println(sdp.str + "(" + sdp.dist + ")");
                }
            }
        }
    }
}
