package foldocpos;

import java.io.*;
import java.util.*;

import foldocparser.Token;

/**
 * Represents the keywords-relationship mapping.<p>
 * When an instance of this class is created, the keyword-
 * retlationship mapping is loaded from a file (keywords.txt by
 * default), and parsed with individual tokens created for
 * each keyword.
 *
 */
public class Keywords extends HashMap {

    /**
     * versioning for serailsation
     */
    private static final long serialVersionUID = 1L;

    public Keywords() {
        this("keywords.txt");
    }

    /**
     * Check for mutli-word keywords -- if the first word is also
     * a valid keyword, change the flag to include K_LONGER.
     */
    private void longerCheck() {
        //System.err.println("Checking for K_LONGER candidates..");
        for (Iterator it = keySet().iterator(); it.hasNext(); ) {
            String s = (String)it.next();
            int pos = s.indexOf(' ');
            if (pos > 0) {
                String first = s.substring(0, pos);
                Token tfirst = (Token)get(first);
                if  (tfirst != null &&
                     ((tfirst.getFlags() & Token.M_KEYTYPE & Token.K_INVALID) == 0)) {
                    //System.err.println(s + " begins with " + first + ", which is not invalid -- marking K_LONGER");
                    tfirst.setFlags(tfirst.getFlags() | Token.K_LONGER);
//                    if (((((Token)get(first)).getFlags() & Token.M_KEYTYPE) & (Token.K_INVALID | Token.K_LONGER)) != 0) {
//                        System.err.println("(((((Token)get("+first+")).getFlags() & Token.M_KEYTYPE) & (Token.K_INVALID | Token.K_LONGER))) != 0 is now true");
//                    }
                }
            }
        }
    }
    
    public Keywords(String fileName) {
        //read keywords from a file
        //parse the keywords file
        //parent means current defn is a parent of next keyword
        //child means current defn is a child of next keyword
        java.util.Map<String, Integer> FLAGS = new HashMap<String, Integer>();
        FLAGS.put("weak", new Integer(Token.WEAK));
        FLAGS.put("normal", new Integer(Token.NORMAL));
        FLAGS.put("strong", new Integer(Token.STRONG));
        FLAGS.put("very", new Integer(Token.VERY_STRONG));
        FLAGS.put("child", new Integer(Token.CHILD));
        FLAGS.put("parent", new Integer(Token.PARENT));
        FLAGS.put("sibling", new Integer(Token.SIBLING));
        FLAGS.put("dissim", new Integer(Token.DISSIM));
        FLAGS.put("category", new Integer(Token.CATEGORY));
        FLAGS.put("invalid", new Integer(Token.K_INVALID));
        FLAGS.put("longer", new Integer(Token.K_LONGER));

        try {
            BufferedReader in = new BufferedReader(
                    new FileReader(fileName));
            String l = in.readLine();
            //sink comments and blank lines
            while (l != null && (l.length() == 0 || l.charAt(0) == '#'))
                l = in.readLine();
            while (l != null) {     //for each line/keyword
            	if (l.matches("\\s*#.*")) {
            		l = in.readLine();
            		continue;
            	}
                l = l.toLowerCase();
                int pos = 0;
                String key;
                int flags = Token.K_WORD;
                //read key (up to ':')
                for (; pos < l.length() && l.charAt(pos) != ':'; ++pos);
                if (pos >= l.length()) {
                    System.err.println("error in keywords.txt: line \""+l+"\" without key");
                } else {
                    key = l.substring(0, pos);
                    while (pos < l.length()) {    //for each flag
                        //sink white space/:/junk
                        for(; pos < l.length() && !Character.isLetter(l.charAt(pos)); ++pos);
                        int start = pos;
                        //read to next white space
                        for(; pos < l.length() && Character.isLetter(l.charAt(pos)); ++pos);
                        if (start == pos) {
                            //maybe an error, maybe just trailing white space
                        } else {
                            String val = l.substring(start, pos);
                            Integer f = (Integer)FLAGS.get(val);
                            if (f == null) {
                                System.err.println("No mapping for '" + val + "' flag");
                            } else {
                                flags |= f.intValue();
                            }
                        }
                    } //next flag
                    put(key, new Token(flags, key));
                } //end of if
                l = in.readLine();
            } //next line
	    in.close();
            System.err.println("Read in " + size() + " keywords from " + fileName + ".");
            longerCheck();
        } catch (IOException ioe) {
            System.err.println("Unexpected IOException reading " + fileName + ":");
            System.err.println(ioe);
        }

    }

}