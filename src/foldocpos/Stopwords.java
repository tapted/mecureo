package foldocpos;

import java.io.*;
import java.util.HashSet;

/**
 * Represents a set of stopwords.<p>
 *
 * When an instance of this class is created, the stopwords
 * are loaded from a file (stopwords.txt by default) to be
 * checked for contains(word).
 */
public class Stopwords extends HashSet {

    public Stopwords() {
        this("stopwords.txt");
    }
    
    public Stopwords(String fileName) {
	//read stopwords
	try {
            BufferedReader in = new BufferedReader(
                    new FileReader(fileName));
            String l;
	    while ((l = in.readLine()) != null) {
                add(l);
	    }
	    in.close();
	    System.err.println("Read in " + size() + " stopwords from " + fileName + ".");
	} catch (IOException ioe) {
            System.err.println("Unexpected IOException reading " + fileName + ":");
	    System.err.println(ioe);
	}
    }
}