package foldocpos;
import java.io.*;
import java.util.*;

import foldocparser.*;

/**
 * A DictAdapter that re-interpretes dictionary NLP XML tokens into
 * parsing tokens for POSParser.<br>
 * The token(...) method of a POSParser is the only callback.
 */
public class POSTokenizer extends DictAdapter {

	protected static Map KEYWORDS = new Keywords();
	protected static Set STOPWORDS = new Stopwords();

	public static boolean WARN_NEST = false, //true,
		S_INTOK = false, //true, //
		S_OUTTOK = false; //true; //

	protected POSParser parser;
	FOLDOCReader input;

	protected int lastState = 0, state = 0;
	int S_ANG = 0x01, S_BRA = 0x02, S_SQU = 0x04, S_QUO = 0x08,
		S_SIN = 0x10, S_PAR = 0x20, S_WAI = 0x40;

	protected StringBuffer phrase;
	protected String inval = null;
	protected Token longer = null;
	protected String lastTag = null;
	protected boolean gotStar = false;
	boolean printNext = false;
	
	public POSTokenizer(POSParser parser, Reader in) {
		this.parser = parser;
		if (in != null) this.input = new FOLDOCReader(in);
		this.phrase = new StringBuffer();
		resetState();
		//setState();
	}

	/**
	 * Return a list (Set) of definitions. This is used in SKOSTokenizer subclass.
	 * @return an Set&lt;String&gt; of definitions
	 */
	public Set<String> defSet() {
		return null;
	}
	public String nextDefinition() throws java.io.IOException {
		return input.nextDef();
	}

	public void endOfSentence() {
		clearPhrase();
		if (S_OUTTOK) System.err.println(".");
		parser.token(new Token(Token.EOS, "End of sentence: reset keywords"));
		resetState();
	}
	public void endOfParagraph() {
		parser.token(new Token(Token.EOP, "End of paragraph: reset something"));
		resetState();
	}
	public void endOfDefinition() {
		parser.token(new Token(Token.EOD, "Normal: end of defn: go to nextDef"));
		resetState();
	}

	public void word(String word, String tag) {

		if (S_INTOK) System.err.println("got \"" + word + "\" in " + state);

		try {
			try {
				if (gotStar) {
					gotStar = false;
					setState(Integer.parseInt(word));
					return;
				}
			} catch (NumberFormatException nfe) {
				//don't return
			}
			if (word.equals("*")) {
				gotStar = true;
				return;
			}

			//set a priority:
			if ((state & S_BRA) != 0) {
				w_Brace(word, tag);
			} else if ((state & S_ANG) != 0) {
				w_Angle(word);
			} else if ((state & S_SQU) != 0) {
				w_Square(word);
			}else {
				w_Word(word, tag);
			}
		} catch (Exception e) {
			System.err.println("Exception in POSTokenizer.word: " + e);
			e.printStackTrace();
		}
		/* Set the state for what's waiting in phrase */
	}

	boolean flush = false;
	
	private void setState(int newState) {
		//lastState = state;
		switch (newState) {
		case 1: state &= ~S_ANG; break;
		case 2: state |= S_ANG; break;
		case 3: state &= ~S_SQU; break;
		case 4: state |= S_SQU; break;
		case 5: state &= ~S_BRA; break;
		case 6: state |= S_BRA; break;
		}
		
		clearPhrase(); //always clear after a setState
		
		if (S_INTOK && S_OUTTOK)
			System.err.println(newState + ", " + lastState + " -> " + state);
	}
	
	protected void resetState() {
		input.reset();
		state = 0;
		inval = null;
		lastTag = null;
		gotStar = false;
		phrase.setLength(0);
	}

	private int lastWarnLine = -1;
	private void warn(String desc) {
		if (input.getLineNumber() != lastWarnLine) {
			System.err.println("[" + input.getLineNumber() +
							   "] Warning: " + desc);
		}
		lastWarnLine = input.getLineNumber();
	}

	public static int alphalength(String s) {
		int count = 0;
		for (int p = 0; p < s.length(); ++p)
			if (Character.isLetter(s.charAt(p)))
				++count;
		return count;
	}

	protected void append(Object s) {
		if (phrase.length() > 0 || phrase.toString().equals("-")) {
			if (lastState != state) {
				clearPhrase();
				phrase.append(s);
			} else {
				phrase.append(" " + s);
			}
		} else {
			phrase.append(s);
		}
		lastState = state;
	}

	protected boolean keyword(String key) {
		boolean ret = true;
		/* Check keywords first */
		Token kw = (Token)KEYWORDS.get(key.toLowerCase());
		if (kw == null && longer != null) {
			kw = longer;
			longer = null;
		}

		if (kw != null) {
			if (kw.getFlags() == Token.K_INVALID) {
				inval = key;
				return false;
			} else { //got an interesting keyword
				if (S_OUTTOK) System.err.println("!" + key + "!");
				if ((kw.getFlags() & (Token.K_LONGER & Token.M_KEYTYPE)) != 0) {
					if (S_OUTTOK) System.err.println("Token "+kw.toString()+" is K_LONGER");
					inval = key;
					ret = false;
				}
				parser.token(kw, lastTag);
			}
		} else {
			if ( alphalength(key) < 4 ||
				 STOPWORDS.contains(key.trim().toLowerCase())) {
				if (S_OUTTOK) System.err.println("~" + key + "~");
				parser.token(new Token(Token.K_NOTHING, key));
			} else {
				if (S_OUTTOK) System.err.println("`" + key + "`");
				parser.token(new Token(Token.NULL_WORD, key), lastTag);
			}
		}
		return ret;
	}

	protected void clearPhrase() {
		clearPhrase(true);
	}

	protected void clearPhrase(boolean force) {
		boolean clear = true;
		if (phrase.length() != 0) {
			if ((lastState & S_BRA) != 0) {
				if (S_OUTTOK) System.err.println("{" + phrase + "}");
				parser.token(new Token(Token.H_LINK, phrase.toString()), lastTag);
			} else if ((lastState & S_ANG) != 0) {
				if (S_OUTTOK) System.err.println("<" + phrase + ">");
				parser.token(new Token(Token.H_LINK |
									   Token.VERY_STRONG |
									   Token.CATEGORY,
									   phrase.toString()));
			} else if ((lastState & S_SQU) != 0) {
				try {
					int num = Integer.parseInt(phrase.toString());
					if (S_OUTTOK) System.err.println("[" + phrase + "]");
					parser.token(new Token(Token.WEB_REF,
										   Integer.toString(num)));
				} catch (NumberFormatException nfe) {}
			} else {
				String res = phrase.toString().trim().toLowerCase();
				if (inval != null) 
					keyword(inval + ' ' + res); //double-word token
				clear = keyword(res);
			}
		}
		if (clear || force) 
			inval = null;
		phrase.setLength(0);
	}
	
	void w_Angle(String word) {
		if (WARN_NEST && state != S_ANG) {
			warn("<Nesting> state=" + Integer.toBinaryString(state));
		}
		if (flush /*state != lastState*/) {
			clearPhrase();
			append(word);
		} else {
			if (word.equals(",")) {
				clearPhrase();
			} else {
				append(word);
			}
			//warn("<" + word + "> state=" + Integer.toBinaryString(state));
		}
	}

	void w_Brace(String word, String tag) {
		if (WARN_NEST && state != S_BRA) {
			if (state != (S_BRA | S_SQU)) { //[{Jargon File}]
				warn("{Nesting} state=" + Integer.toBinaryString(state));
			}
		}
		if (flush /*state != lastState*/) {
			clearPhrase();
		}
		append(word);
		lastTag = tag;
	}
	
	void w_Square(String word) {
		if (WARN_NEST && state != S_SQU) {
			warn("[Nesting] state=" + Integer.toBinaryString(state));
		}
		clearPhrase();
		append(word);
	}

	void w_Word(String word, String tag) {
		clearPhrase(false);
		append(word);
		lastTag = tag;
	}
}