package foldocpos;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
//import org.apache.xerces.parsers.SAXParser;

/**
 * An org.xml.sax.helpers.DefaultHandler to re-interpret
 * Dictionary-style NLP tokens for a DictAdapter.<p>
 *
 * It implements (through inheritance) all the org.xml.sax
 * handler interfaces.
 *
 */
public class DictHandler extends DefaultHandler {

    private String tag = null;
    private String type = null;
    private String lastword = null;
    
    private DictAdapter da;
    /**
     * Create a DictHandler, using da for callbacks
     *
     * @param da the DictAdapter called when tokens are found
     */
    public DictHandler(DictAdapter da) {
	this.da = da;
    }    


    /**
     * The start of an XML element. The NLP elements
     * p, s, t and w are handled
     */
     public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes)
                  throws SAXException {
	if (localName.equals("p")) {
	    //System.out.println("\n<p>");
	} else if (localName.equals("s")) {
	    //System.out.print(" _ ");
	} else if (localName.equals("t")) {
            if (attributes.getLength() > 0)
                type = attributes.getValue(0);
            //System.out.print(" ");
	} else if (localName.equals("w")) {
            //System.out.print(attributes.getValue(0));
            if (attributes.getLength() > 0)
                tag = attributes.getValue(0);
	} else {
	}
    }

    /**
     *
     *
     */
    public void endElement(String uri,
                             String localName,
                             String qName)
                  throws SAXException {
	//System.err.print("\n<<END: " + localName + ">>");
	if (localName.equals("p")) {
	    //System.out.println("</p>");
	    da.endOfParagraph();
	} else if (localName.equals("s")) {
	    //System.out.println(".");
	    da.endOfSentence();
	} else if (localName.equals("w")) {
            tag = null;
	} else if (localName.equals("t")) {
	    type = lastword = null;
	} else {
	}
    }

    public void characters(char[] ch,
                           int start,
                           int length)
                throws SAXException {
        if (tag != null) {
            String pair = lastword;
            lastword = (new String(ch, start, length)).trim();
            da.word(lastword, tag); //single only
            /*
            if (pair != null && type != null && !lastword.equals("*")) {
                lastword = pair + " " + lastword; //also triples this way
                da.word(lastword, tag);
                }
                */
	    //System.out.print("[" + (new String(ch, start, length)).trim() + "]");
    	}
    }

    public void endDocument() throws SAXException {
        try {
            da.endOfDefinition();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
