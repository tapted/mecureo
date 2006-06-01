package foldocparser;

import hashgraph.*;
import java.io.*;
import java.util.zip.*;
import java.util.*;
import porterstemmer.*;

/**
 * <p>An Exception to throw when a leaf would be required, but leaves are disabled</p>
 * <p></p>
 * @author Trent Apted
 * @version 0.4
 */
class LeavesNotAllowed extends Exception {

	/**
	 * versioning for serailsation
	 */
	private static final long serialVersionUID = 1L;}

/**
 * <p>A Parser for a FOLDOC-ish dictionary. Creates an ontology, represented as a HashGraph</p>
 * <p></p>
 * @author Trent Apted
 * @version 0.4
 */
@SuppressWarnings("unchecked")
public class Parser {

	public static final boolean DEBUGGING = false; //true; //
	public static boolean NLP_OUTPUT = false; //true; //
	public static boolean SHOW_UNCLAS = false; //true; //
	
	/** Where the categories are stored as a text list. File is allowed to not exist. */
	public static String CATEGORY_FILE = "categories.txt";
	/** FLAG: wether to allow nodes to be created for words not 'defined' */
	public static boolean ALLOW_LEAVES = false;
	/** FLAG: wether to show warnings about anomalies in the dictionary to stderr */
	public static boolean WARNINGS = false;
	/** FLAG: wether to perform substring matches [expensive operation] when matching would-be leaves to definitions */
	public static boolean SUBSTRING_MATCHES = false;
	/** The string to prepend on to the names for category nodes, to avoid conflicts with concepts */
	public static final String CATEGORY_PREPEND = "-";
	/** CONSTANT: The 'C' used for weight penalty */
	public static int WEIGHT_C = 10;

	public static boolean NO_MATCH_CACHING = false; //effects debugging output only
	public static boolean NO_SELF_LINKS = true;

	public static final int ML_NONE = -1;
	public static final int ML_EXACT = 0;
	public static final int ML_TRIMMED = 1;
	public static final int ML_NONALPHA = 2;
	public static final int ML_NOCASE = 3;
	public static final int ML_CSTEMS = 4;
	public static final int ML_SUBSTRING = 5;		
	public static final int ML_ACRONYM = 6;

	/** Value controlling the stage at which 'detection' level
	 * relationship matching is last allowed at
	 */
	public static int MATCH_LEVEL = ML_CSTEMS;

	/** The ontology, as it is created */
	public HashGraph graph;
	/** A map&lt;String, Node&gt; of the nodes created for categories in CATEGORY_FILE */
	public HashMap categories;
	/** The Tokeniser being used */
	protected Tokenizer t;
	/** The name of the file currently being parsed */
	protected String curFile;
	/** The definition term currently being parsed */
	//protected String curDef="";

	static String lastNull = "", secondNull = "", nextNull = "";
	
	public static PrintWriter logfile;
	boolean lastlogged = false;
	protected String curDef = "";

	//if Java was smart it would have a way of returning a reference to
	//a key in a map or set so that there wouldn't be two copies of each
	//definition string floating around.. but it's not.
	/** A set&lt;String&gt; of keys that are definitions in the dictionary */
	protected HashSet defn_restrict = new HashSet();
	/** A set&lt;String&gt; of keys that are categories from CATEGORY_FILE
		or null if categories are created automatically */
	protected HashSet category_restrict = null;

	/** A map&lt;String, String&gt; of aliases to override the matchings [not imlemented yet] */
	protected HashMap aliases = new HashMap();

	/** An instance of the portersstemmer matcher for matching componenet stems.
	 * Loaded with the definitions' component stems */
	protected Indexer stemFinder;
	/** A map&lt;String, String&gt; mapping the third stage filter of definitions' keys to their key */
	protected HashMap lcaseFinder;
	/** A map&lt;String, String&gt; mapping the second stage filter of definitions' keys to their keys */
	protected HashMap nonAlnumFinder;
	/** A map&lt;String, String&gt; of matchings that have already been made */
	protected HashMap matched = new HashMap();


	/**
	 * Initialise the matching maps from defn_restrict
	 */
	protected void makeStems() {
		stemFinder = new Indexer(defn_restrict.iterator());
		lcaseFinder = new HashMap();
		nonAlnumFinder = new HashMap();
		for (Iterator it = defn_restrict.iterator(); it.hasNext();) {
			String s = it.next().toString();
			String cur = filter2(filter1(s));
			nonAlnumFinder.put(cur, s);
			lcaseFinder.put(filter3(cur), s);
		}
	}

	/** First stage filter: trim s */
	private String filter1(String s) {
		return s.trim();
	}

	/** First stage finder */
	private String find1(String s) {
		if (defn_restrict.contains(s))
			return s;
		return null;
	}

	/** 
	 * The second stage filter: non-alpha numeric replacement.
	 * @note refactored by William Niu, discarded removeNonAlnum() 
	 * */
	private String filter2(String s) {
		return s.replaceAll("[^\\p{Alnum}]"," ")
				.trim()
				.replaceAll("\\s+"," ");
	}

	/** Second stage finder */
	private String find2(String s) {
		return (String)nonAlnumFinder.get(s);
	}

	/** The third stage filter: case insensitivity */
	private String filter3(String s) {
		return s.toLowerCase();
	}

	/** Third stage finder  */
	private String find3(String s) {
		return (String)lcaseFinder.get(s);
	}

	/** Fourth stage finder [uses third stage filter]:
	 * component stem matching */
	private String find4(String s) {
		SDPair match[] = stemFinder.match(s);
		if (match.length > 0)
			return match[0].str;
		else
			return null;
	}

	/** Fifth stage finder [uses third stage filter]:
	 * substring matching - O(n) */
	private String find5(String s) {
		ArrayList match = stemFinder.lookHarder(s);
		if (match.size() > 0)
			return ((SDPair)match.get(0)).str;
		else
			return null;
	}

	boolean detect_log(boolean nullword, boolean logged, String s, String match, int pass, int level, boolean reject) {
		if (logged) return true;
		if (!nullword) return true;
		if (level != MATCH_LEVEL) {
			lastlogged = false;
			return false;
		}
		if (reject && lastlogged) {
			logfile.print("REJECTED: Acronym");
		} else if (match != null) {
			lastlogged = true;
			logfile.print("\n" + curDef + " -> " + match + " (" + s + ")[" + level + "][" + pass + "] ");
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Try to match <code>s</code> to a definition, in stages
	 * <p>If it could not be matched, and leaves are enabled then it will return
	 * <code>s</code>. If it could not be matched and leaves are disabled it
	 * will throw the exception.</p>
	 * @param s the <code>String</code> to be matched
	 * @param log if true then each involved match/nomatch is printed to stderr [default to true]
	 * @return the key for the matched definition
	 * @throws LeavesNotAllowed if no match and leaves disabled
	 */
	String matchAll(String s, boolean log, boolean nullword, int passes) throws LeavesNotAllowed {
		
		boolean secondPass = passes == 1;	
		boolean logged = false;		

		if (defn_restrict.contains(s)) {
			logged = detect_log(nullword, logged, s, s, passes, ML_EXACT, false);
			return s;
		}
		if (matched.containsKey(s)) {
			return (String)matched.get(s);
		}
		if (nullword && MATCH_LEVEL < ML_TRIMMED)
	   			throw new LeavesNotAllowed();
		String cur = filter1(s);
		String match = find1(cur);
		//String type = "trim";
		logged = detect_log(nullword, logged, s, match, passes, ML_TRIMMED, false);
		if (match == null && (!nullword || MATCH_LEVEL >= ML_NONALPHA)) {
			cur = filter2(cur);
			match = find2(cur);
			//type = "respace";
		}
		logged = detect_log(nullword, logged, s, match, passes, ML_NONALPHA, false);
		if (match == null && (!nullword || MATCH_LEVEL >= ML_NOCASE)) {
			cur = filter3(cur);
			match = find3(cur);
			//type = "insensitive";
		}
		logged = detect_log(nullword, logged, s, match, passes, ML_NOCASE, false);
		if (match == null && (!nullword || MATCH_LEVEL >= ML_CSTEMS)) {
			match = find4(cur);
			//type = "component stem matching";
		}
		logged = detect_log(nullword, logged, s, match, passes, ML_CSTEMS, false);
		if (match == null &&
				(MATCH_LEVEL >= ML_SUBSTRING ||
				(!nullword && SUBSTRING_MATCHES))) {
			match = find5(cur);
			//type = "substring match";
		}
		logged = detect_log(nullword, logged, s, match, passes, ML_SUBSTRING, false);
		//avoid adding acronyms for link detection
		if (match != null && nullword && isAcronym(match) != isAcronym(s)) {
			//System.err.println(match + " <> " + s);
			match = null;
			logged = detect_log(nullword, false, s, match, passes, MATCH_LEVEL, true);
		}
		if (!nullword && match == null && ALLOW_LEAVES) {
			//System.err.println("No match for '" + s + "': making leaf.");
			return s;
		} else if (match == null){
			String prefix = lastNull;
			if (nullword && passes == 0) {
				nextNull = lastNull;
				lastNull = s;
				if (prefix.length() > 0) {
					//System.err.println("Trying " + prefix + " " + s + " at second level");
					match = matchAll(prefix + " " + s, log, true, 1);				
					//System.err.println("Second-pass   " + prefix + " " + s + " -> " + match);
					lastNull = secondNull = nextNull = "";
					return match;
				} 
			}

			//third pass
			if (nullword && secondPass) {
				prefix = secondNull;
				secondNull = nextNull;
				if (secondNull.length() > 0) {
					match = matchAll(prefix + " " + s, log, true, 2);
					//System.err.println("Third-pass	" + prefix + " " + s + " -> " + match);
					return match;
				}
			}
				
			//System.err.print("!" + s + "! ");
			if (match == null)
				throw new LeavesNotAllowed();
		}

		if (!NO_MATCH_CACHING)
			matched.put(s, match);
		//System.err.println("Matched `" + s + "' to `" + match + "' using  " + type);
		if (nullword) {
				//System.err.println("Non-bracketed " + s + " -> " + match);
		} else {
			lastNull = secondNull = nextNull = "";
		}
		return match;
	}

	static boolean isAcronym(String s) {
		boolean allCaps = true;
		for (int i = 0; allCaps && i < s.length(); ++i)
			allCaps = Character.isUpperCase(s.charAt(i));
		return allCaps;
	}

	/**
	 * calls matchAll(s, true)
	 * @see #matchAll(String s, boolean log)
	 */
	String matchAll(String s) throws LeavesNotAllowed {
		return matchAll(s, true, false);
	}

	String matchAll(String s, boolean log, boolean nullword) throws LeavesNotAllowed {
 		return matchAll(s, log, nullword, 0);
	}

	/**
	 * Create and initialise new parser instance
	 */
	public Parser() {
		graph = new HashGraph();
		categories = new HashMap();
	}

	/**
	 * parse <code>fileName</code>, after creating a <code>Tokeniser</code> for it
	 * <p>If <code>fileName</code> ends in ".gz" a gzip input stream is created,
	 * otherwise a normal input stream is created for the <code>Tokeniser</code></p>
	 * @param fileName the dictionary to parse
	 * @return the corresponding ontology, as a <code>HashGraph</code>
	 * @throws IOException on an IO error
	 */
	public HashGraph parse(String fileName) throws IOException {
		graph.sourceFilenames.add(fileName);
		curFile = fileName;
		preParse(fileName);
		if (fileName.endsWith(".gz")) {
			t = new Tokenizer(new BufferedInputStream(
							new GZIPInputStream(
							new FileInputStream(fileName))));
		} else {
			t = new Tokenizer(new BufferedInputStream(
							new FileInputStream(fileName)));
		}
		logfile = new PrintWriter(
						new BufferedWriter(
								new FileWriter("detected2.log")));
		makeStems();
		try {
			while(true) definition(t.nextDefinition());
		} catch (EOFException ee) {
		}
		logfile.close();
		return graph;
	}

	/**
	 * Parse using ./Dictionary.gz
	 * @return the ontology
	 * @throws IOException on an IO error
	 * @see #parse(String fileName)
	 */
	public HashGraph parse() throws IOException {
		return parse("Dictionary.gz");
	}

	/** process a category token */
	protected boolean category(Token tok, Node child) {
		if (category_restrict != null && !category_restrict.contains(tok.toString()))
			return false; //resticted
		Node cat = graph.get(CATEGORY_PREPEND + tok.toString());
		if (cat == null) {
			if (WARNINGS)
				System.err.println("[" + t.lineNo +
						"] Warning: category rejected <" + tok + ">");
			return false;
		}
		if (!categories.containsKey(tok.toString()))
			categories.put(tok.toString(), cat);
		Link lnk = new Link(cat, child, tok.baseWeight(), tok, curDef, curFile); //should be 1 for category
		//DON'T DO BACKWARD LINKS FOR CATEGORIES!!!
		//child.linkBack(lnk);
		cat.put(child.getKey(), lnk);
		return true;
	}

	/** process a relationship [hyperlink] */
	protected boolean hlink(Token tok, Node n, double pWeight, Token kw) {

		boolean nullword = (tok.getFlags() & Token.NULL_WORD) != 0;
		if (nullword && MATCH_LEVEL == ML_NONE)
			return false;
		//do a check for a category that could stuff things up
		/*no longer happens, due to CATEGORY_PREPEND
		if (categories.containsKey(tok.toString())) {
			if (WARNINGS)
				System.err.println("[" + t.lineNo +
					"] Warning: Had to reject link " + n + " -> " + tok +
							   " due to category violation");

			return;
		}
		*/
		if (tok.toString().trim().length() == 0) {
			if (WARNINGS)
				System.err.println("[" + t.lineNo +
					"] Warning: Got a zero-length link in " + n);
			return false;
		}
		Node dest;
		try {
			dest = graph.getAddNode(matchAll(tok.toString(), true, nullword));
		} catch (LeavesNotAllowed lna) {
			return false;
		}

		if (NO_SELF_LINKS && dest == n) { //compare refs
			return false; //link to self
		}
		
		Link lnk;
		double wt = Math.min(1.0, pWeight + kw.baseWeight());
		if (kw.isParent()) {
			lnk = new Link(n, dest, wt, kw, curDef, curFile);
			System.err.println("add: "+lnk);
			//if (n.put(dest.getKey(), lnk) != null)
			n.put(dest.getKey(), lnk);
			dest.linkBack(lnk);
		} else {
			lnk = new Link(dest, n, wt, kw, curDef, curFile);
			System.err.println("add: "+lnk);
			//if (dest.put(n.getKey(), lnk) != null)
			dest.put(n.getKey(), lnk);
			n.linkBack(lnk);
		}
//		System.err.println("line#: "+t.lineNo+" for keyword: "+
//				kw.toString()+" curFile: "+lnk.getFilename());
		if (DEBUGGING) System.err.println(lnk.toStringReasoning());
		/* if got this far, a node was added */
		prog = updateProg(prog);

		return true;
	}

	/** Update weight modifier progress:
	 * prog between 0 and 0.5, unstable near 0. */
	protected double prog = 0; //progress modifier: increase * PROGRESS_MOD each iter
	protected double updateProg(double prog) {
		return prog + (0.5 - prog) / WEIGHT_C;
	}

	/** process a definition */
	protected void definition(String d) throws IOException {
		boolean syn = true; //indicates synonym link
		prog = 0;
		curDef = d;
		if (!defn_restrict.contains(d))
			return; //ignore: it's been restricted
		Node cur = graph.getAddNode(d);
		Token tok = t.nextToken();
		Token keyword = Token.T_NORM;
		while (tok.getFlags() != Token.EOD) {
			if (tok.toString().trim().length() == 0) { //ignore
			} else if ((tok.getFlags() & Token.M_ASSOC) == Token.CATEGORY) {
				category(tok, cur);
			} else if (tok.getFlags() == Token.K_NOTHING) {
			} else if (tok.getFlags() == Token.WEB_REF) {
				try {
					Integer.parseInt(tok.toString());
					cur.webID = tok.toString();
				} catch (NumberFormatException nfe) {}
			} else if (tok.getFlags() == Token.EOS) {
				keyword = Token.T_NORM;
			} else if ((tok.getFlags() & Token.M_TYPE) == Token.H_LINK) {
				if (true || ALLOW_LEAVES || defn_restrict.contains(tok.toString())) {
					if (syn) {
						hlink(tok, cur, prog, Token.T_SYN);
					} else {
						hlink(tok, cur, prog, keyword);
						//prog = updateProg(prog); //done in hlink
					}
				} else if (category_restrict == null) {
					//print a parse error if not in 'restrict mode'
					if (WARNINGS)
						System.err.println("[" + t.lineNo +
							"] Warning: symbol rejected {" + tok + "}");

				}
			} else if ((tok.getFlags() & Token.M_TYPE) == Token.K_WORD) {
				if  (keyword == Token.T_NORM || //compare refs
					 (tok.getFlags() & Token.M_STRENGTH) >= //GE as closer => more appropriate
					 (keyword.getFlags() & Token.M_STRENGTH))
					keyword = tok;
			}
			syn = false; //any token removes synonym flag
			tok = t.nextToken();
		}
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
			Tokenizer tk;
			if (fileName.endsWith(".gz")) {
				tk = new Tokenizer(new BufferedInputStream(
								new GZIPInputStream(
								new FileInputStream(fileName))));
			} else {
				tk = new Tokenizer(new BufferedInputStream(
								new FileInputStream(fileName)));
			}
			if (category_restrict == null)
				while (true)
					defn_restrict.add(tk.nextDefinition());
			else { //add only definitions in category_restrict
				Outer:
				while (true) {
					s = tk.nextDefinition();
					Token tok;
					while ((tok = tk.nextToken()).getFlags() != Token.EOD) {
						if ((tok.getFlags() & Token.M_ASSOC) == Token.CATEGORY &&
									 category_restrict.contains(tok.toString())) {
							defn_restrict.add(s);
							continue Outer;
						}
					}
				}
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
