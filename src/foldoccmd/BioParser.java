package foldoccmd;
import java.io.*;
import java.util.*;

import porterstemmer.*;

/**
 * <p>Command line cgi program for displaying <b><i>Query match possibilities</b></i></p>
 * @author Trent Apted
 * @version 0.4
 */
public class BioParser {
	public static final int MAX_WINDOW = 5;
	public static final int MIN_BIO_WORDLEN = 1;
	public static final int MIN_STEMLEN = 3;
	public static final String DUMP_SEP = " &middot; ";
	public static final String HTML_TAIL = "</pre></body></html>";
	public static final String HTML_HEADER = "<html><head><title>\n" +
	                                         "  BioParser Word List\n" +
											 "</title></head><body>\n" +
											 "  <h1>BioParser Word List</h1>\n" +
											 "  <pre>\n";
	
	public static final String USAGE = 
						   "Usage: java foldoccmd.BioParser " +
          		           "-DREQUEST_METHOD=\"$REQUEST_METHOD\" " + 
 						   "-DQUERY_STRING=\"$QUERY_STRING\" " +
 						   "> htmlFile";
	
	public static boolean REMOVE_MATCHES = true; 
	public static int MIN_STEM_LENGTH = 4;
		
	public static LinkedList makewords (String bio) {
		LinkedList ret = new LinkedList();
		boolean instr = false;
		boolean gotwhite = false;
		StringBuffer word = new StringBuffer();
		for (int i = 0; i < bio.length(); ++i) {			
			char c = 0;
			for  (; i < bio.length(); ++i) {
				c = bio.charAt(i);
				gotwhite = gotwhite || Character.isWhitespace(c);
				if ((!instr && !Character.isLetter(c)) || c == '"')
					break;
				word.append(c);
			}
			instr = !instr && c == '"';
			if (gotwhite && word.length() >= MIN_BIO_WORDLEN) {
				ret.add(word.toString());
				word.setLength(0);
			} else if (gotwhite) {
				word.setLength(0);
			} else {
				/* we might be an acronym.. */				
			}
		}
		if (word.length() > MIN_BIO_WORDLEN)
			ret.add(word.toString());
		return ret;
	}

	hallcgi.ShowParse cgi;
    Indexer idx;
	
	public LinkedList biowords (String bio) {
		LinkedList words = makewords(bio);
//		System.err.println("Bio is :" + words);
		LinkedList candidates = new LinkedList();
		StringBuffer windword = new StringBuffer();
		for (int window = MAX_WINDOW; window > 0; --window) {
			ListIterator lstart = words.listIterator();
			for (int i = 0; i < words.size() - window + 1; ++i) {
				windword.setLength(0);
				windword.append((String)lstart.next());
				for (int j = 1; j < window; ++j) {
					windword.append(' ');
					windword.append((String)lstart.next());
				}
				SDPair matches[] = idx.match(windword.toString(), MIN_STEM_LENGTH);
				/* bloody Java can't copy iterators, so rewind it */
				for (int j = 1; j < window; ++j) {
					if (matches.length > 0 && REMOVE_MATCHES)
						lstart.set("xx-removed-xx");
					lstart.previous();
				}
				if (matches.length > 0) {
					if (matches[0].str.length() > MIN_STEM_LENGTH || matches[0].str.equalsIgnoreCase(windword.toString())) {
						candidates.add(matches[0].str);					
						if (REMOVE_MATCHES) {
							lstart.previous();
							lstart.set("xx-removed-xx");
						}
					}
				}
			}
		}
		//System.err.println("Bio is :" + words);
		//System.err.println("Candidates are: " + candidates);
		return candidates;
	}
	
    void loadIndex(String filename) throws IOException {
        System.out.print("Loading " + filename + "...\n"); System.out.flush();
		idx = new Indexer(new File(filename));
    	System.out.print("  loaded OK.\n"); System.out.flush();
    }
    
	public BioParser() throws IOException {
		cgi = new hallcgi.ShowParse();
		loadIndex(cgi.getValue("wordfile", "txt/trimnodenames.txt"));
	}
	
	public void ontologyForm() {
		if (cgi.isChecked("usability")) {
			System.out.println(USABILITY_FORM);
		} else {
			System.out.println(FOLDOC_FORM);
		}
		System.out.flush();
	}

        public static void hiddenFormBio(Collection cand, String bioname, String name) {
            Iterator lit = cand.iterator();
            System.out.println("</pre>");
            System.out.println(HID_NAME + bioname + "_name" + HID_VALUE + name + FTAIL);
            for (; lit.hasNext(); ) {
                String so = (String)lit.next();
                System.out.print(HID_NAME + bioname + HID_VALUE + /*"&quot;" +*/ so + /*"&quot;" +*/ FTAIL);
            }
            System.out.println("<pre>");
        }
	
	public static void dumpBio(Collection cand, String name)  {
		Iterator lit = cand.iterator();
		System.out.println("</pre><h3>Terms extracted from " + name + "</h3><p>");
		if (lit.hasNext()) {
			String s = (String)lit.next();
			System.out.print(s);
		}
		while (lit.hasNext())
			System.out.print(DUMP_SEP + (String)lit.next());
		System.out.println("\n</p><pre>");
    }

        public static void formTail(int default_size, hallcgi.ShowParse cgi) {
            formTail(default_size, "Generate Model", cgi);
        }

	public static void formTail(int default_size, String button_label, hallcgi.ShowParse cgi) {
        System.out.println("</pre><p>\n  <input type=\"text\" name=\"distance\" value=\"" + cgi.getValue("depth", cgi.getValue("distance", Integer.toString(default_size))) + "\">");
        System.out.println("Distance / Nodes / Depth <br>");
        System.out.println("<input type=\"text\" name=\"peerage\" value=\"" + cgi.getValue("minKids", cgi.getValue("peerage", "0")) + "\">");
        System.out.println("Minimum Peerage</p>\n<p>Grow Mode (using Distance above):\n</p>\n<blockquote><p>");
        System.out.println("<input type=\"radio\" name=\"mode\" value=\"dist\">Weighted Distance<br>");
        System.out.println("<input type=\"radio\" name=\"mode\" value=\"num\" checked>Node Number<br>");
        System.out.println("<input type=\"radio\" name=\"mode\" value=\"depth\">Depth\n</p></blockquote><p>");
        System.out.println("<input type=\"submit\" name=\"Submit\" value=\"" + button_label + "\">\n</p>");
	}
	
	public void doMain() {
		foldoccmd.RunLog.log1.append("\n\nExtracting words from first bio...\n\n");
		LinkedList bio1 = biowords(cgi.getValue("bio", ""));
		foldoccmd.RunLog.log1.append("\n\nExtracting words from second bio...\n\n");
		LinkedList bio2 = biowords(cgi.getValue("bio_two", ""));
		ontologyForm();
		hiddenFormBio(bio1, "bio_a", cgi.getValue("bio_name", "First Bio"));
		hiddenFormBio(bio2, "bio_b", cgi.getValue("bio_two_name", "Second Bio"));
		formTail(bio1.size() + bio2.size(), cgi);
		
		dumpBio(bio1, cgi.getValue("bio_name", "First Bio"));
		dumpBio(bio2, cgi.getValue("bio_two_name", "Second Bio"));
		System.out.println(RunLog.log1);
		System.out.println("\n--- Log2 ---\n");
		System.out.println(RunLog.log2);
	}
		
    /**
     * Usage: java foldoccmd.NodeSearch wordList queryList [depth = 1.0] [minKids = 0] > htmlFile
     */
    public static void main(String args[]) {
        System.err.println(USAGE);
        try {
        	System.out.println(HTML_HEADER);
        	BioParser app = new BioParser();
        	app.doMain();
        } catch (Exception ioe) {
            System.out.println(ioe);
            ioe.printStackTrace(System.out);
        } finally {
            System.out.println(HTML_TAIL);
            System.out.flush();        	
        }

        /*
        System.out.println("<p>\n  <input type=\"text\" name=\"distance\" value=\"" + depth + "\">");
        System.out.println("Distance / Nodes / Depth <br>");
        System.out.println("<input type=\"text\" name=\"peerage\" value=\"" + minKids + "\">");
        System.out.println("Minimum Peerage</p>\n<p>Grow Mode (using Distance above):\n<blockquote>");
        System.out.println("<input type=\"radio\" name=\"mode\" value=\"dist\">Weighted Distance<br>");
        System.out.println("<input type=\"radio\" name=\"mode\" value=\"num\" checked>Node Number<br>");
        System.out.println("<input type=\"radio\" name=\"mode\" value=\"depth\">Depth</blockquote></p>");
        System.out.println("<input type=\"submit\" name=\"Submit\" value=\"Generate Model\">\n</p>");
        
        
        System.out.println("</form></body></html>");
        */
    }
    public static final String HID_NAME = "\n  <input type=\"hidden\" name=\"";
	public static final String HID_VALUE = "\" value=\"";
	public static final String FTAIL = "\" />";
	public static final String RAD_ONTOLOGY = "\n  <input type=\"radio\" name=\"ontology\" value=\"";
	public static final String CHECKTAIL = "\" checked />";
	
	
	public static final String USABILITY_FORM = 
		"</pre><form action=\"usesmartcomp.cgi\" method=\"POST\" >\n" +
		" Please choose an Ontology: <blockquote>" +
		RAD_ONTOLOGY + "fdg/usability.fdg"      + FTAIL + "Matched (stems)<br>" +
		RAD_ONTOLOGY + "fdg/udetectont.fdg"     + FTAIL + "Matched with Link Detection<br>" +
		RAD_ONTOLOGY + "fdg/umatchdetect.fdg"   + FTAIL + "Matched with <i>matched</i> Link Detection<br>" +
    	RAD_ONTOLOGY + "fdg/unlpdetect.fdg"     + FTAIL + "Matched with <b>NLP</b> Link Detection<br>" +
		RAD_ONTOLOGY + "fdg/unlpcasedetect.fdg" + CHECKTAIL + "Matched with <i>case-matched <b>NLP</b></i> Link Detection<br>" +
    	RAD_ONTOLOGY + "fdg/unlpstemdetect.fdg" + FTAIL + "Matched with <i>stem-matched <b>NLP</b></i> Link Detection<br>" +
        "\n </blockquote><pre>";
	
	public static final String FOLDOC_FORM =
		"</pre><form action=\"smartcomp.cgi\" method=\"POST\" >\n" +
		" Please choose an Ontology: <blockquote>" +
    	RAD_ONTOLOGY + "fdg/matchedont.fdg" 	+ FTAIL + "Matched (stems, no detection)<br>" +
    	RAD_ONTOLOGY + "fdg/submatchedont.fdg" 	+ FTAIL + "Substring matched<br>" +
    	RAD_ONTOLOGY + "fdg/detectedont.fdg"	+ FTAIL + "Matched with Link Detection 1 (exact)<br>" +
    	RAD_ONTOLOGY + "fdg/insense.fdg" 		+ FTAIL + "Matched with Link Detection 3 (case insensitive)<br>" +
    	RAD_ONTOLOGY + "fdg/stemmed.fdg" 		+ FTAIL + "Matched with Link Detection 4 (component stems)<br>" +
    	RAD_ONTOLOGY + "fdg/dftag.fdg" 			+ FTAIL + "Matched with <b>NLP</b> Link Detection<br>" +
    	RAD_ONTOLOGY + "fdg/fcasetag.fdg" 	+ CHECKTAIL + "Matched with <i>case-matched <b>NLP</b></i> Link Detection<br>" +
    	RAD_ONTOLOGY + "fdg/fmatchtag.fdg" 		+ FTAIL + "Matched with <i>stem-matched <b>NLP</b></i> Link Detection<br>" +
        "\n </blockquote><pre>";

}
