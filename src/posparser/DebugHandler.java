package posparser;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * An XML callback handler that does debugging output dumps
 * to stdout of each of the tokens
 */
public class DebugHandler extends DefaultHandler {

    boolean inWord = false;

    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes)
                  throws SAXException {
	if (localName.equals("p")) {
	    System.out.println("\n<p>");
	} else if (localName.equals("s")) {
	    System.out.print(" _ ");
	} else if (localName.equals("t")) {
	    System.out.print(" ");
	} else if (localName.equals("w")) {
	    inWord = true;
	    System.out.print(attributes.getValue(0));
	} else {
	}
    }

    public void endElement(String uri,
                             String localName,
                             String qName)
                  throws SAXException {
	//System.err.print("\n<<END: " + localName + ">>");
	if (localName.equals("p")) {
	    System.out.println("</p>");
	} else if (localName.equals("s")) {
	    System.out.println(".");
	} else if (localName.equals("w")) {
	    inWord = false;
	} else {
	}
    }

    public void characters(char[] ch,
                           int start,
                           int length)
                throws SAXException {
	if (inWord) {
	    System.out.print("[" + (new String(ch, start, length)).trim() + "]");
    	}
    }
}
