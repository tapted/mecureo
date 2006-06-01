/**
 * 
 */
package foldocparser.tools;

import hashgraph.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author William
 *
 */
public class OutputUtils {

	public static final String RDF_TAIL = "</rdf:RDF>";
	public static final String ABOUT_BEGIN =
		"http://foldoc.doc.ic.ac.uk/foldoc/foldoc.cgi?query=";
	public static final String HCI_ABOUT_BEGIN =
		"http://www.usabilityfirst.com/glossary/term_";
	public static final String HCI_ABOUT_TAIL = ".txl";
	public static final DecimalFormat rdfNumForm =
		new DecimalFormat("0.000000");

	public static boolean SMART_LINKS = true;

	public static final int DEFAULT_PAD_LEN = 5;

	public static String pad(int i, int len) {
		StringBuffer sb = new StringBuffer();
		sb.append(i);
		while (sb.length() < len)
			sb.insert(0, '0');
		return sb.toString();
	}

	public static void timestamp(String msg) {
	     Calendar now = Calendar.getInstance();
	     SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
	     System.err.println("Current time = " + formatter.format(now.getTime()) + " (" + msg + ")");
	}
	
	public static void memorystamp(String msg) {
		System.err.println("Java memory in use = " + 
				   (Runtime.getRuntime().totalMemory() - 
				    Runtime.getRuntime().freeMemory()) + " (" + msg + ")");
	}

	public static String capitalise(String in){
		StringBuffer out = new StringBuffer(in);
		out.setCharAt(0,Character.toUpperCase(out.charAt(0)));
		return out.toString();
	} 

	public static Set<Link> getLinksFromNode (Node n) {
		Set<Link> kids = new HashSet<Link>();
		//add backward links first so that forward links override.
		for (Iterator back = n.backIter(); back.hasNext(); ) {
			//backlinks go the other way
			if (SMART_LINKS) {
				Link l = (Link)back.next();
				if (l.getTok().isStrictParent()) {
					kids.add(l.reverseDirection());
				} else if (l.getTok().isChild()) {
					kids.add(l.reverseBoth());
				} else {
					kids.add(l);
				}
			} else {
				kids.add((Link)back.next());
			}
		}
		for (Link l : (Collection<Link>) n.values()) {
			if (SMART_LINKS && l.getTok().isStrictParent()) 
				kids.add(l.reverseType());
			else 
				kids.add(l);
		}
		return kids;
	}
	
	public static String toURICompatible(String key) {
		//StringBuffer cur = new StringBuffer(ABOUT_BEGIN);
		StringBuffer cur = new StringBuffer();
		//replace ampersands and spaces with %escape and remove quotation marks
		for (int i = 0; i < key.length(); ++i) {
			char c = key.charAt(i);
			switch (c) {
			case '&':
				cur.append("%26");
				break;
			case ' ':
				cur.append("%20");
				break;
			case '"':
				break;
			default:
				if (!Character.isLetterOrDigit(c))
					cur.append("%" + Integer.toHexString((int)c));
				else
					cur.append(c);
			}
		}
		return cur.toString();
	}

	public static String toHTMLEscape(String key) {
		final String accept = "\n ()+=-";
		StringBuffer cur = new StringBuffer();
		for (int i=0; i < key.length(); i++) {
			int c = key.charAt(i);
			if (!Character.isLetterOrDigit((char)c) && accept.indexOf((char)c) < 0) {
				cur.append("&#" + c + ";");
			} else {
				cur.append((char)c);
			}
		}
		return cur.toString();
	}

	public static String toRDFTitle(String key) {
		StringBuffer cur = new StringBuffer();
		//replace ampersands with &amp; and remove quotation marks
		for (int i = 0; i < key.length(); ++i) {
			char c = key.charAt(i);
			switch (c) {
			case '&':
				cur.append("&amp;");
				break;
			case '"':
				break;
			default:
				if (c > 127)
					cur.append("%" + Integer.toHexString((int)c));
				else
					cur.append(c);
			}
		}
		return cur.toString();
	}
	
	public static void main (String args[]) {
		String str = "abc@#$%^abc";
		System.out.println(toHTMLEscape(str));
	}

}
