package posparser;

import hashgraph.*;
import java.util.zip.*;
import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.apache.xerces.parsers.SAXParser;

import opennlp.common.xml.NLPDocument;

/**
 * A wrapper to start NLP parsing; converting the
 * XML output to be interpreted by org.xml.(...)Handler
 * callbacks.
 */
public class POSWrapper {

    public static GetPosDoc gpd = new GetPosDoc();
    static SAXParser parser = new SAXParser();
    public static boolean PARSING_STATUS = false;

    public static String readLines(BufferedReader in) throws IOException {
	StringBuffer sb = new StringBuffer();
	String line = in.readLine();
	if (line != null) {
	    sb.append(line);
	    while ((line = in.readLine()) != null) {
	        sb.append('\n');
	 	sb.append(line);
	    }
	}
	return sb.toString();
    }

    /**
     * So far, it is known that input can be a java.io.File,
     * a java.lang.String or a java.io.Reader
     * callbacks should inherit from DefaultHandler and override
     * the appropriate methods.
     */
    public static void parse(Object input, DefaultHandler callbacks) 
			throws IOException, SAXException {
	if (PARSING_STATUS) System.err.println("Retrieving Document (NLP)...");
	NLPDocument doc = gpd.getDoc(input);
			
	if (PARSING_STATUS) System.err.println("Processing (Ontology)...");
	process(doc.toXml(), callbacks);
	
    }

    public static HashGraph parse(String fileName) throws IOException, SAXException {

        
    	BufferedReader in = null;
        if (fileName.endsWith(".gz")) {
            in = new BufferedReader(new InputStreamReader(
                            new GZIPInputStream(
                            new FileInputStream(fileName))));
        } else {
            in = new BufferedReader(new InputStreamReader(
                            new FileInputStream(fileName)));
        }

	parse(in, new DebugHandler());
	
	return null;
    }

    public static void process(String xmlDoc)
			throws IOException, SAXException {
	process(xmlDoc, new DebugHandler());
    }

    public static void process(String xmlDoc, DefaultHandler callbacks)
			throws IOException, SAXException {
	
	//System.err.println(xmlDoc);
	StringReader sr = new StringReader(xmlDoc);
	org.xml.sax.InputSource is = new org.xml.sax.InputSource(sr);

	/*
	String[] features = parser.getPropertiesRecognized();
	System.err.println("Parser Recognises:");
	for (int i = 0; i < features.length; ++i) {
	    System.err.println("  " + features[i]);
	}
	*/

	//parser.setProperty(" ", value);
	parser.setContentHandler(callbacks);
	if (PARSING_STATUS) System.err.println("Parsing...");
	parser.parse(is);
	if (PARSING_STATUS) System.err.println("\nParsing Complete.");
    }

    public static void main(String[] args) throws Exception {
	System.err.println("Parsing " + args[0] + "...");
	parse(args[0]);
    }
}
