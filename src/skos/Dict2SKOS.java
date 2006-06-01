/**
 * Provides tools to use the Simple Knowledge Organisation System (SKOS) REF format
 * to represent a dictionary.
 */
package skos;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import foldocpos.FOLDOCReader;

/**
 * Converts original "FOLDOC-like dictionary" files (.dict) to SKOS RDF (.rdf) format.<br/>
 * Version 1.0:
 * <ul>
 * 	<li>Terms are put inside the skos:prefLabel tags.</li>
 *  <li>Web reference ("[...]") is put as a property of the Concept class 
 *      (i.e. rdf:about).</li>
 *  <li>Parsed definition is put in the skos:definition tag.</li>
 *  <li>Hyperlinks ("{...}") are made separate concept classes without definitions.</li>
 *  <li>Categories ("<...>") are put as skos:isSubjectOf tags.</li>
 * </ul>
 * Version 1.1:
 * <ul>
 * 	<li>Terms are put inside the skos:prefLabel tags.</li>
 *  <li>Web reference ("[...]") is put as a property of the Concept class 
 *      (i.e. rdf:about).</li>
 *  <li>FOLDOCReader coded definition is put in the skos:definition tag.</li>
 * </ul>
 * 
 * TODO: add skos:subject in the category concepts <br/>
 * TODO: put top categories in ConceptScheme as hasTopConcept tags <br/>
 * TODO: turn FOLDOCReader coding into brackets, as it's more human editable
 * 
 * @author William T. Niu
 * @since 29 December 2005
 * @version 2.0
 * @bug: v1.0 category/hyperlink scopeNote
 */
public class Dict2SKOS
{
	private static final short INDENTSIZE = 2;
	private static final short CLASS = 1;
	private static final short PROPERTY = 2;
	private PrintStream out;
	private FOLDOCReader in;
	public static final String version = "v1.0"; //"v1.0"

	private static final String SKOSHeader = 
	//		"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n\n"+
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"+
	"<rdf:RDF\n"+
	"    xml:base=\"http://www.it.usyd.edu.au/~niu/ontology\"\n"+
	"    xmlns:sago=\"http://www.it.usyd.edu.au/~niu/ontology/sago#\"\n"+
	"    xmlns:dct=\"http://purl.org/dc/terms/\"\n"+
	"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
	"    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"+
	"    xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n\n";
	
	/** To get some data regarding the output file */
	private File outFile;

	/** The list of terms added as Concept classes */
	private HashSet<String> concepts = new HashSet<String>();
	
	/** 
	 * Keeps a list of categories and hyperlinks and add
	 * them as Concept class at the end.  
	 */
	private HashMap<String,String> addToEnd = new HashMap<String, String>();
	
	/**
	 * Constuctor
	 * @param in to read in the dict file
	 * @param out the File object to output the SKOS RDF file
	 * @throws FileNotFoundException
	 */
	public Dict2SKOS(Reader in, File out) throws FileNotFoundException {
		if (out==null)
			this.out = System.out;
		else
			this.out = new PrintStream(out);
		this.in = new FOLDOCReader(in);
		outFile = null;// out;
	}

	/**
	 * Put in basic information into the main ConceptScheme. 
	 * So far only issued date and modified date, which are the same, are added
	 */
	private String printConceptScheme() {
		List<String> props = new ArrayList<String>();
		Map<String, String> attr = new HashMap<String, String>();
		String date = (new Date (outFile.lastModified())).toString();
		
		props.add(tagGen("dct:issued", null, date, PROPERTY));
		props.add(tagGen("dct:modified", null, date, PROPERTY));
		attr.put("rdf:about", toURI(outFile.getName()));
		return tagGen("skos:ConceptScheme", attr, props, CLASS);
	}
	
	/**
	 * where the conversion takes place.
	 * @throws Exception is thrown when errors occur during reading in the dict file
	 */
	public void convert() throws Exception {
		out.print(SKOSHeader);
		if (outFile!=null)
			out.println(printConceptScheme());

		// parse and put definitions
		String s, def="", term;
		while ( (s = in.nextDef()) != null) {
			term = s;
			while ((s = in.readLine()) != null)
				if (s.length()>0) def+=s;
			//System.err.println(def);
			printConcept(term, def, "");
			def="";
		}
		
		// put categories and hyperlinks
		for (String str : addToEnd.keySet())
			printConcept(str, "", addToEnd.get(str));
		
		out.print("</rdf:RDF>");
		out.close();
	}

	/**
	 * Generate a class or property tag conforming to XML standard. 
	 * @param tagName name of the tag to be generated
	 * @param attr pairs of attributes for the tag in Map&lt;String, String&gt;
	 * @param content content to put between the opening and closing tag.
	 * @param type specified the tag is a Class or Property tag
	 * @return generated XML tag in String
	 */
	@SuppressWarnings("unchecked")
	private String tagGen (String tagName, Map<String, String> attr, Object content, short type) {
		String ret = indent(type)+"<"+tagName;
		if (attr == null && content == null) return ret+" />"; // append newline for class?
		if (attr == null)
			ret += ">";
		else 
			for (String key : attr.keySet())
				ret += (" "+key+"=\""+attr.get(key)+"\"");
		
		if (content == null) 
			ret += (" />");
		else {
			if (attr != null) ret += ">";
			if (content instanceof String) // property tag
				ret += content+"</"+tagName+">";
			else {  // class tag
				for (String key : (List<String>) content)
					ret += "\n"+key;
				ret += "\n"+indent(type)+"</"+tagName+">";
			}
		}
		
		return ret; // append newline for class?
	}

	private static String toURI (String key) {
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
	
	/**
	 * Parse and print a FOLDOCReader-coded definition.
	 * @param term The definition term; can be multiple words.
	 * @param def FOLDOCReader-coded definition; to be parsed 
	 * @param scopeNote String to put in the skos:scopeNote tag.
	 * 				    So far this is to indicate whether the Concept
	 *                  class is a category or a hyperlink.
	 * TODO: test with v1.0
	 */
	@SuppressWarnings("unchecked")
	private void printConcept (String term, String def, String scopeNote) {
		if (concepts.contains(term.toLowerCase())) {
			System.err.println("Duplicated definition discarded: "+term);
			return;
		} else
			concepts.add(term.toLowerCase());

		Map<String, Object> map = parseDef(def, (version.equals("v1.0")));
		List<String> props = new ArrayList<String>();
		Map<String, String> attr = new HashMap<String, String>();
		
		//replace ampersands and spaces with %escape and remove quotation marks
		String str = term.replaceAll("\\&\\s*amp\\s*;", "&amp;");
		props.add(tagGen("skos:prefLabel", null, str, PROPERTY));

		if ((def = (String) map.get("def")) != "") {
			// RDF parser doesn't like '&'
			if (def.indexOf('&')>=0) {
				def = def.replaceAll("\\&\\s*amp\\s*;", "&amp;");
				def = def.replaceAll("\\& ", "&amp; ");
			}
			def = def.trim();
			props.add(tagGen("skos:definition", null, def, PROPERTY));
		}	
		
		// for a category or hyperlink
		if (version == "v1.0" && scopeNote != "")
			props.add(tagGen("skos:scopeNote", null, scopeNote, PROPERTY));

		if (version == "v1.0" && ((HashSet) map.get("category")).size() > 0)
			for (String hs : (HashSet<String>) map.get("category")) {
				attr.clear();
				attr.put("rdf:resource",toURI(hs));
				props.add(tagGen("skos:isSubjectOf", attr, null, PROPERTY));
			}
		
		if (outFile != null) {
			attr.clear();
			attr.put("rdf:resource",toURI(outFile.getName()));
			props.add(tagGen("skos:inScheme", attr, null, PROPERTY));
		}
		
		String s = (String)map.get("webref");
		s = s.replace('#',' ').trim();
		if (s=="") s = term;
		attr.clear();
		attr.put("rdf:about", toURI(s));
		out.println(tagGen("skos:Concept", attr, props, CLASS));

		if (version != "v1.0") return;
		// make hlinks and cetegories concepts with no definition, if not already exists!
		String tmp[] = {"category"}; //{"hyperlink", "category"};
		for (String temp : tmp)
			for (String h : (HashSet<String>) map.get(temp)) {
				if (temp=="category" && h.startsWith("-")) 
					h = h.substring(1).trim();
				if (!concepts.contains(h.toLowerCase()))
					addToEnd.put(h, temp);
			}
	}
	
	/**
	 * Parses def to find categories ("<>"), hyperlinks ("{}"), and 
	 * web references ("[]").
	 * @param def The definition to be parsed
	 * @param toParse Whether to extract the categories and hyperlinks
	 * @return a map with a parsed definition, a web reference, categories,
	 * 		   and/or hyperlinks.
	 */
	private Map<String, Object> parseDef (String def, boolean toParse) {
		Map<String, Object> map = new HashMap<String, Object>();
		Set<String> cate = new HashSet<String> ();
		Set<String> hlinks = new HashSet<String> ();
		String webref="";
		String match = toParse ? "246" : "4";
		//if (toParse) System.err.println("v1.0");
		
		// Web reference (*4...*3), categories (*2...*1), hyperlinks (*6...*5)
		// loop using matches(regexp)
		int inx = def.indexOf('*');
		try {
			while (inx >= 0 && def.matches(".*\\*["+match+"].+\\*[135].*")) {
				switch(def.charAt(inx+1)) {
				case '2': //category
					String c = def.substring(inx+2, def.indexOf("*1",inx+1)).trim();
					cate.add(c);
					break;
				case '4': //web reference
					webref = def.substring(inx+2, def.indexOf("*3",inx+1)).trim();
					//def = def.replaceFirst("\\s*\\*4\\s"+webref+"\\s*\\*3\\s"," ");
					break;
				case '6': //hyperlink
					String h = def.substring(inx+2, def.indexOf("*5",inx+1)).trim();
					hlinks.add(h);
					break;
				}
				inx = def.indexOf('*',inx+1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("def: "+def);
		}
		
		def = def.replaceAll("\\s*\\*4.+\\*3\\s*", " ");
		if (toParse) {
//			def = def.replaceAll("\\s*\\*2.+?\\*1\\s*", " ");
//			def = def.replaceAll("\\s*\\*6.+?\\*5\\s*", " ");
//			def = def.replaceAll("related categories","");  // for usability.dict only...
			def = def.replaceAll("\\*(2) ", "&lt;");
			def = def.replaceAll(" \\*(1)", "&gt;");
			def = def.replaceAll("\\*(6) ", "{");
			def = def.replaceAll(" \\*(5)", "}");
			def = def.replaceAll("\\( ", "(");
			def = def.replaceAll(" \\)", ")");
			def = def.replaceAll("\\.related", ". Related");
			def = def.replaceAll("\\.categories", ". Categories");
		}
		
		map.put("webref", webref);
		map.put("def", def.trim());
		map.put("category", cate);
		map.put("hyperlink", hlinks);
		return map;
	}

	/**
	 * prints indentation(s)
	 */
	private String indent(int i) {
		StringBuffer re = new StringBuffer();
		i *= INDENTSIZE;
		while(i-->0) re.append(' ');
		return re.toString();
	}
	
	/**
	 * If outfile is not supplied, content will be ouput to screen without the
	 * ConceptScheme class.
	 * @param args input and output files
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: java ... Dict2SKOS inputFile [outFile]\n"+
					"\tinputFile is a FOLDOC-like dictionary (.dict)\n"+
					"\toutFile is a SKOS RDF file ends in .rdf. "+
					"If not supplied, print to standard output.");
			return;
		}
		System.err.println("reading in "+args[0]);
		Reader in;
		if (args[0].endsWith(".gz")) {
			in = new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0])));
		} else {
			in = new FileReader(args[0]);
		}
		
		// default print to standard output
		File out = null;
		if (args.length>1) 
			out = new File(args[1]);

		Dict2SKOS d2k = new Dict2SKOS(in, out);
		System.err.println("Dict2SKOS version: "+Dict2SKOS.version);
		d2k.convert();
		System.err.println("File converted and output to "+out.getAbsolutePath());
	}

}
