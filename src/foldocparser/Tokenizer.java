package foldocparser;
import java.io.*;
import java.util.*;

/**
 * Provides high-level tokenization of a textual source to
 * provide Parser with construction tokens.<p>
 *
 * Deals with the text input at a relatively low level to
 * handle all anomalies, giving warnings, and provide
 * recovery mechanisms.
 */
@SuppressWarnings("unchecked")
public class Tokenizer {

	private String lastBadRead;

	private String fillTo(Set chars) throws IOException {
		StringBuffer sb = new StringBuffer();
		boolean endFound = false;
		//sink leading crap till a letter
		do {
			for(; pos < line.length() &&
				IGNORE_CHARS.contains(new Character(line.charAt(pos))); ++pos);
			if (pos >= line.length())
				if (!wrap()) {
					lastBadRead = "<<leading>>";
					return null;
				}
		} while (IGNORE_CHARS.contains(new Character(line.charAt(pos))));

		while (!endFound) {
			for (; pos < line.length() &&
				   !chars.contains(new Character(line.charAt(pos))); ++pos) {
				sb.append(line.charAt(pos));
			}
			if (pos >= line.length()) {
				//wrap to read next line after padding
				sb.append(' ');
				if (!wrap()) {
					//caller needs to bail
					lastBadRead = sb.toString();
					return null;
				}
			} else {
				endFound = true;
			}
		}
		return sb.toString();
	}

	//SOME STATIC SETS FOR THE FILL_TO CHARS
	private static Set DEFAULT_CHARS = new HashSet();
	private static Set IGNORE_CHARS = new HashSet();
	private static Set CATEGORY_CHARS = new HashSet();
	private static Set HLINK_CHARS = new HashSet();
	private static Set WEBREF_CHARS = new HashSet();
	private static Map KEYWORDS = new foldocpos.Keywords();
	private static Set STOPWORDS = new foldocpos.Stopwords();

	/**
	 * Static initialiser
	 */
	{
		DEFAULT_CHARS.add(new Character('{'));
		DEFAULT_CHARS.add(new Character('.'));
		DEFAULT_CHARS.add(new Character(' '));
		IGNORE_CHARS.add(new Character('('));
		IGNORE_CHARS.add(new Character('['));
		CATEGORY_CHARS.add(new Character('>'));
		CATEGORY_CHARS.add(new Character(','));
		CATEGORY_CHARS.add(new Character('{'));
		CATEGORY_CHARS.add(new Character('.'));
		HLINK_CHARS.add(new Character('}'));
		WEBREF_CHARS.add(new Character(']'));
		WEBREF_CHARS.add(new Character('{'));
		WEBREF_CHARS.add(new Character('<'));
		WEBREF_CHARS.add(new Character('.'));
	}

	BufferedInputStream in;
	PrintStream log;
	String line;
	public long lineNo = 0;
	int defLine;
	int pos;
	boolean defnWaiting;
	SymbolProcessor symWaiting = null;
	//the default SP tries to extract keywords from the surrounding text
	SymbolProcessor DEFAULT_SP = new SymbolProcessor() {
		public Token run() throws IOException {
			//look for a keyword
			//stop at end of sentence or '{'
			String res = fillTo(DEFAULT_CHARS);
			String ores = res;
			if (res == null) {
				return new Token(Token.EOD, "Normal: end of defn: go to nextDef");
			}
			//"e.g" screws everything up - special cases here
			if (res.equalsIgnoreCase("e") && pos + 1 < line.length()
								&& line.charAt(pos) == '.'
								&& line.charAt(pos + 1) == 'g' ||
							res.equalsIgnoreCase("eg") && line.charAt(pos) == '.') {
				pos += 3 - res.length();
				if (pos < line.length() && line.charAt(pos) == '.')
					pos++;
				return (Token)KEYWORDS.get("e.g.");
			}
			if (line.charAt(pos) == '.') {
				++pos;
				return new Token(Token.EOS, "End of sentence: reset keywords");
			}
			if (line.charAt(pos) == ' ') {
				++pos; //put it on something interesting
				res = res.toLowerCase();
				//lookup res in the keywords
				Token kw = (Token)KEYWORDS.get(res);
				if (kw != null) {   //else ignore
					//check invalid (CLEAN UP!)
                    System.err.println("here");
					if (((kw.getFlags() & Token.M_KEYTYPE) & (Token.K_INVALID | Token.K_LONGER)) != 0) {
						//try to extend.. only once
						String res2 = fillTo(DEFAULT_CHARS);
						if (res2 == null) {
							return new Token(Token.EOD, "EOD after invalid kw");
						} else {
							//check for res + res2
							res = res + res2;
							System.err.println("We are invalid or longer: Checking for '" + res +"'..");
							Token kw2 = (Token)KEYWORDS.get(res);
							if (kw2 == null && ((kw.getFlags() & Token.K_INVALID) != 0)) { //also check for res2
								kw2 = (Token)KEYWORDS.get(res2);
							}
							if (kw2 != null)
								kw = kw2;
						}
					}
				}
				//check again in case res, res2 and res + res2 are all invalid
				if (kw != null) {
					return kw;
				} //else fall through
			}
			if (alphalength(ores) < 4 ||
					STOPWORDS.contains(ores.trim().toLowerCase()))
					return new Token(Token.K_NOTHING, res);
			else
					return new Token(Token.NULL_WORD, ores);
		}
	};

	private static int alphalength(String s) {
		int count = 0;
		for (int p = 0; p < s.length(); ++p)
			if (Character.isLetter(s.charAt(p)))
				++count;
		return count;
	}

	HashMap calls;

	public Tokenizer(int dummy) {
		lineNo = dummy;
	}

	public Tokenizer(BufferedInputStream file) {
		this(file, System.err);
	}

	@SuppressWarnings("unchecked")
	public Tokenizer(BufferedInputStream file, PrintStream logFile) {
		in = file;
		log = logFile;
		initFile();

		//INITIALIZE PROCESSING CALLS
		//this should prolly be static.. but I won't be creating more than one
		//Tokenizer, and this lets us access object data members
		calls = new HashMap();
		calls.put(new Character('<'),
			new SymbolProcessor() {
				public Token run() throws IOException {
					//find ending '>' or a comma
					++pos; //go past '<' or ' '
					String res = fillTo(CATEGORY_CHARS);
					System.err.println("<...>: "+res);
					if (res == null) {
						//bail out
						recover();
						Token rt = new Token(Token.EOD,
							"[" + lineNo + "] Warning: '<...>' bailed out after "
							+ lastBadRead);
						log.println(rt.toString());
						return rt;
					}
					//bad find
					if (line.charAt(pos) != ',' && line.charAt(pos) != '>') {
						return new Token(Token.K_NOTHING, res);
					}
					if (line.charAt(pos) == ',') {
						//there's another category waiting
						symWaiting = this;
					}
					++pos; //go onto space after ',' or whatever is after '>'
					return new Token(Token.H_LINK |
									 Token.VERY_STRONG |
									 Token.CATEGORY,
									 res);
				}
			});
		calls.put(new Character('{'),
			new SymbolProcessor() {
				public Token run() throws IOException {
					//find ending '{' - everything in between is a link
					++pos; //ignore opening '{'
					String res = fillTo(HLINK_CHARS);
					System.err.println("{...}: "+res);
					if (res == null) {
						//bail
						recover();
						Token rt = new Token(Token.EOD,
							"[" + lineNo + "] Warning: '{...}' bailed out after "
							+ lastBadRead);
						log.println(rt.toString());
						return rt;
					}
					++pos; //go past '}'
					//tokenizer doesn't know about keywords to qualify it better
					//leave it to parser to add things to the flags
					return new Token(Token.H_LINK, res);
				}
			});
		calls.put(new Character('['),
			new SymbolProcessor() {
				public Token run() throws IOException {
					++pos; //ignore opening '['
					String res = fillTo(WEBREF_CHARS);
					System.err.println("[...]: "+res);
					if (res == null) {
						//bail
						recover();
						Token rt = new Token(Token.EOD,
							"[" + lineNo + "] Warning: '[...]' bailed out after "
							+ lastBadRead);
						log.println(rt.toString());
						return rt;
					}
					if (line.charAt(pos) != ']') {
						return new Token(Token.K_NOTHING, res);
					}
					++pos; //go past ']'
					int num = 0;
					try {
						num = Integer.parseInt(res);
					} catch (NumberFormatException nfe) {
						if (Parser.WARNINGS) {
							log.println("[" + lineNo + "] Warning: [" + res + "] could" +
										"not form a number reference");
						}
						return new Token(Token.K_NOTHING, res);
					}
					return new Token(Token.WEB_REF, Integer.toString(num));
				}
			});
	}

	private void initFile() {
		try {
			nextLine();
			while (line.charAt(0) < 32) {
				log.println("[" + lineNo + "] Warning: expected a definition start of file.");
				nextLine();
			}
		} catch (IOException ioe) {
			log.println("[" + lineNo + "] Error: unable to initialise tokenizer: ");
			log.println(ioe.toString());
		}
	}

	private void nextLine() throws IOException {
		StringBuffer sb = new StringBuffer();
		int c = in.read();
		while (c >= 0 && c != '\n') {
			sb.append((char)(c));
			c = in.read();
		}
		if (c < 0) {
			throw new EOFException();
		}
		line = sb.toString();
		//log.println("[" + lineNo + "]" + line);
		lineNo++;
		pos = 0;
	}

	/**
	 * For wrapping text
	 * go to next line and validate position
	 * true if valid
	 */
	private boolean wrap() throws IOException {
			do {
				nextLine();
			} while (line.length() == 0);
			//validate position: should be a tab. else new defn.
			if (line.charAt(pos) == '\t') {
				++pos; //go past tab
				return true;
			} else {
				return false;
			}
	}

	public void recover() {
		defnWaiting = (line.length() > 0 && line.charAt(0) >= ' ');
	}

	/**
	 * To skip ahead from current definition
	 */
	public String nextDefinition() throws IOException {
		if (defnWaiting) {
			defnWaiting = false;
		} else //while (wrap()); //read lines until wrap fails
		///*
		{
			//a definition starts with a word in the 0 column (not \n or \t).
			while (line.length() == 0 || line.charAt(0) < ' ') {
				nextLine();
			}
		}
		//*/
		String ret = line;
		nextLine();
		if (line.length() != 0) {
			//this is a silly error to have... FOLDOC meets it.. but needn't
			//log.println("[" + lineNo + "] Warning: Expected blank line after definition header");
		} else {
			nextLine();
		}
		while (line.length() == 0 || line.charAt(0) != '\t') {
			log.println("[" + lineNo + "] Warning: Expected start of definition body");
			nextLine();
		}
		//now line contains first line of definition.
		defLine = 1;
		pos = 1;	//a tab at start of line
		symWaiting = null; //clean up last defn
		return ret;
	}

	public Token nextToken() throws IOException {
		Token ret = null;
		if (defnWaiting) {
			throw new RuntimeException("Called nextToken() without nextDefinition()");
		}
		if (symWaiting != null) {
			SymbolProcessor tmp = symWaiting;
			symWaiting = null;
			ret = tmp.run();
		}
		while (ret == null) {
			while (pos >= line.length()) {
				if (!wrap())
					return new Token(Token.EOD, "Normal: end of defn: go to nextDef");
				pos = 1;
			}
			if (line.charAt(0) != '\t') {
				//new definition
				defnWaiting = true;
				ret = new Token(Token.EOD, "End of definition");
			} else {
				SymbolProcessor val = (SymbolProcessor)calls.get(
										new Character(line.charAt(pos)));
				if (val == null) {
					ret = DEFAULT_SP.run();
				} else {
					ret = val.run();
				}
			}
		}
		return ret;
	}
}

abstract class SymbolProcessor {
	public abstract Token run() throws IOException;
}
