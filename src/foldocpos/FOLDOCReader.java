package foldocpos;

import java.io.*;

/**
 * A java Reader that re-interprets FOLDOC-style dictionaries
 * so that the NLP parser behaves correctly.<p>
 * Confusing punctuation is converted to noun-interpreted tokens
 * so that they can be detected by a POSToker (ie braces, brackets
 * etc).<br>
 * <i> e.g. </i> and <i> i.e. </i> are converted to <i> eg </i> and
 * <i> ie </i> respectively, so that the sentence detector is never
 * confused.<br>
 * An instance of this class can be passed as the <code>Object input</code>
 * when executing an NLP Parse, thus it sits between the NLP parser and
 * the file<br>
 * EOFs are simulated at the end of each defintion so that progress
 * is steady and the NLP parser doesn't get overloaded.
 *
 */
public class FOLDOCReader extends LineNumberReader {
	
	int last = -1;
	int waiting = -1;
        int waiting2 = -1;
        int xwaiting = -1;
        int defWaitChar = -1;
	
        boolean defWaiting = true, lastWhite = true;

        public boolean PRINT_SKIPPED = true;

        public boolean inBrace, inAngle, inSquare, inQuote, inSingle, inParen;
        boolean clBrace, clAngle, clSquare, clQuote, clSingle, clParen;

	public FOLDOCReader(Reader in) {
	    super(in);
	}

	public FOLDOCReader(Reader in, int sz) {
	    super(in, sz);
        }

        public void reset() {
            inBrace = inAngle = inSquare = inQuote = inSingle = inParen = false;
            lastWhite = true;
        }

        public int read(char[] cbuf,
                        int off,
                        int len)
                  throws IOException {

            int ctr = 0;
            int c = read();
            if (c < 0) return c;

            for (; c > 0 && ctr < len; ++ctr) {
                cbuf[off + ctr] = (char)c;
                c = read();
            }
            return ctr;
        }

        public String readLine()
                    throws IOException {
	    StringBuffer sb;
	    int c = read();
	    if (c < 0) return null;
	    sb = new StringBuffer();
	    while (c > 0 && c != '\n') {
		sb.append((char)c);
		c = read();
	    }
	    return sb.toString();
        }

        private boolean noSplit(int i) {
            char c = (char)i;
            return Character.isLetterOrDigit(c) || c == '\'';
        }

	private int respaced(int next) {
		if (last < 0 || Character.isWhitespace((char)next) ||
				Character.isWhitespace((char)last) || 
		            (noSplit(last) && noSplit(next)) ||
                   next == '.' || next == ',' || next == ';') {
			last = next;
			return last;
                } else {
                    if (next == '|' || next == '/' || next == '-') {
                        last = ' ';
                        return '-';
                    }
                    last = ' ';
                    //System.err.println("respaced: " + waiting + " < " + next);
                    waiting = next;
                    return last;
		}
	}

	private int egCheck(int c) throws IOException {
	    if (!lastWhite || (c != 'e' && c != 'i')) {
		lastWhite = false;
		return c;
	    }
	    lastWhite = false;

	    //System.err.println("\n<<c = " + (char)c + ">>");
	    int n, d;
	    while ((n = readOne()) > 0 && Character.isWhitespace((char)n));
	    
	    //System.err.println("\n<<n = " + (char)n + ">>");
	    if (n != '.') {
		waiting2 = n;
		return c;
	    }
	    
	    while ((d = readOne()) > 0 && Character.isWhitespace((char)d));

	    //System.err.println("\n<<d = " + (char)d + ">>");
	    if (d != 'g' && d != 'e') {
		waiting2 = d;
		return c;
	    }
	    while ((n = readOne()) > 0 && Character.isWhitespace((char)n));
	    //System.err.println("\n<<n = " + (char)n + ">>");
	    if (n != '.') System.err.println("Bad egg on [" + getLineNumber() + "]!");
	    waiting2 = d;
	    return c;
	}

        public int read() throws IOException {
            if (defWaiting)
                return -1;

	    for (int c = readOne();; c = readOne()) { //continue until a return
	        if (c < 0 || Character.isLetterOrDigit((char)c))
		    return egCheck(c);
	        if (Character.isWhitespace((char)c)) {
		    lastWhite = true;
		    if (c == '\n') //grok only recognises double-newlines as paras
			return c;
		    int n = readOne();
		    while (n > 0 && Character.isWhitespace((char)n))
                        n = readOne();

                    /*
                     if (n == '.' || n == ',' || n == '!' || n == '?' || n == ';') {
                        waiting2 = n;
                        return ' '; //push whitespace before these
                    }*/

                    //System.err.println("read/ws: " + waiting + " < " + n);

		    waiting = n;
		    return c; //single whitespace   
                }
                
                int num = 0;
		switch (c) {
                case('('): inParen = true;   return c;
		case(')'): inParen = false;  return c;
                case('{'): ++num;
                case('}'): ++num;
                case('['): ++num;
		case(']'): ++num;
		case('<'): ++num;
                case('>'):
                    ++num;
                    xwaiting = num;
                    return '*';
                    
		case('"'): inQuote = !inQuote; break;
		case('\''): inSingle = !inSingle; last = ' '; return c;
                case('-'):

                     case('.'):
                     case(','):
                     case('!'):
                     case('?'):
                     case(';'):

                case(':'): return c;
		default:   /*return c*/;
                }
                /*
		switch (c) {
		case('{'): inBrace = true;   break;
		case('}'): inBrace = false;  break;
		case('('): inParen = true;   return c;
		case(')'): inParen = false;  return c;
		case('['): inSquare = true;  break;
		case(']'): inSquare = false; break;
		case('<'): inAngle = true;   break;
		case('>'): inAngle = false;  break;
		case('"'): inQuote = !inQuote; break;
		case('\''): inSingle = !inSingle; break;
		case('-'):
		case(':'): return c;
		default:   ;
                }
                */
	    }	
        }
/*
        public void tick() {
            if (clBrace) inBrace = false;
            if (clParen) inParen = false;
            if (clSquare) inSquare = false;
            if (clAngle)

        }
*/
	private int readOne() throws IOException {
		
		if (defWaiting)
                    return -1;

                if (xwaiting >= 0) {
                    if (xwaiting == 0) {
                        xwaiting = -2;
                        return ' ';
                    } else {
                        int c = '0' + xwaiting;
                        xwaiting = 0;
                        return c;
                    }
                }
/*
                if (xwaiting == -2) {
                    xwaiting = -1;
                    return ' ';
                }
*/
		if (waiting2 >= 0) {
			last = waiting2;
			waiting2 = -1;
			return last;
		}

                if (waiting >= 0) {

                    last = waiting;
                    //System.err.println("readOne: " + waiting + " < -1 ");
                    waiting = -1;
                    return last;
                }

		int next = unSkipped();
		if (next < 0) return next;
		
		if (next == '\n') {
			
			int after = unSkipped();
			if (after < 0) return after;
			
			if (after == '\t') { //inside paragraph, inside def
				last = ' ';
				return last;
			} else {
			    
                            while (after == '\n') {
                                after = unSkipped();
                                if (after < 0) return after;

                                if (after == '\t') { //new paragraph, inside def
                                    //System.err.println("readOne/n: " + waiting + " < /n");

                                    waiting = '\n';
                                    last = '\n';
                                    return last;
                                }
                            }

                            //if got to here, a non-tab followed a new line
                            //=> new definition
                            //System.err.println("readOne~n: " + waiting + " < " + after);

                            defWaitChar = after;
                            defWaiting = true;

                            //System.err.println("waiting = " + (char)waiting);
                            //System.err.println("[[defWaiting = " + defWaiting + "]]");
                            
                            last = -1;
                            return last;
                        }
			
		} else {
		    return respaced(next);			
		}
        }

        private int unSkipped() throws java.io.IOException {
            int c = super.read();
            if (c == '|') {
                return 'X';
            }
            if (c == '?') {
                return '.';
            }
            while (c == '|' || c == '/' || c == ':' || c == '"') {
                c = super.read();
            }
            return c;
        }

        
        public String nextDef() throws IOException {
            //System.err.println("[[defWaiting=" + defWaiting + "]]");
            //System.err.println("[[waiting=" + waiting + "]]");
            int lastline = getLineNumber() - 1;
            boolean skipped = false;
            while (!defWaiting && read() > 0) {
                //if (read() < 0) break;
                if (PRINT_SKIPPED && lastline != getLineNumber()) {
                    lastline = getLineNumber();
                    skipped = true;
                    System.err.print("[" + lastline + "]");
                }
            }
            if (skipped)
                System.err.println(" skipped.");
	    if (defWaiting) {
		inBrace = inAngle = inSquare = 
			  inParen = inSingle = 
                          inQuote = defWaiting = false;
                clBrace = clAngle = clSquare =
                          clParen = clSingle =
                          clQuote = false;
                lastWhite = true;

                //System.err.println("xwaiting = " + (char)xwaiting);
                //System.err.println("waiting2 = " + (char)waiting2);
                xwaiting = waiting2 = -1;

                //System.err.println("waiting = " + (char)waiting);
                waiting = defWaitChar;
                String l = readLine();
                //System.err.println("l = " + l);

                if (l.startsWith("- "))
                    return "-" + l.substring(2);
                else
                    return l;
	    }
	    return null;
        }
}
