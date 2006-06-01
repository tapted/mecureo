/*
 * 
 */
package foldocparser.tools;

import foldocparser.Normalizer;
import foldocparser.Token;
import tokenizers.SentenceTokenizer;
import hashgraph.*;

import java.io.*;
import java.util.*;

import skos.SKOS;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author William T. Niu
 * @since 4 January 2006
 * @version 1.1
 */
public class OutputSKOS {
	private static final String version = "v1.0";
	private static final boolean DEBUG = false; //true; //
	private static final boolean SKOS_ONLY = true; //false; //
	private static File DEFAULT_FILE;
	private static Model model = null;
	
	private static final String RDF_HEADER =
		"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n\n"+
		"<rdf:RDF\n"+
		"    xml:base=\"http://www.it.usyd.edu.au/~niu/ontology\"\n"+
	    "    xmlns:sago=\"http://www.it.usyd.edu.au/~niu/ontology/sago#\"\n"+
	    //"    xmlns:dct=\"http://purl.org/dc/terms/\"\n"+
	    "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
	    //"    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"+
		//"    xmlns:gmp=\"http://www.gmp.usyd.edu.au/schema/resources/\"\n"+
		(SKOS_ONLY ? "" : "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n") +
	    "    xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n\n";
	
	private static final String OWL_PRE =    
	    "  <owl:ObjectProperty rdf:ID=\"hasChild\">\n"+
	    "    <owl:inverseOf rdf:resource=\"#hasParent\"/>\n"+
	    "    <rdf:type rdf:resource=\"&owl;TransitiveProperty\"/>\n"+
	    "    <rdfs:domain rdf:resource=\"#Concept\"/>\n"+
	    "    <rdfs:range rdf:resource=\"#Concept\"/>\n"+
	    "  </owl:ObjectProperty>\n\n"+
	    
	    "  <owl:ObjectProperty rdf:ID=\"hasParent\">\n"+
	    "    <owl:inverseOf rdf:resource=\"#hasChild\"/>\n"+
	    "    <rdf:type rdf:resource=\"&owl;TransitiveProperty\"/>\n"+
	    "    <rdfs:domain rdf:resource=\"#Concept`\"/>\n"+
	    "    <rdfs:range rdf:resource=\"#Concept\"/>\n"+
	    "  </owl:ObjectProperty>\n\n"+
	    
	    "  <owl:ObjectProperty rdf:ID=\"hasAntonym\">\n"+
	    "    <rdf:type rdf:resource=\"&owl;SymmetricProperty\"/>\n"+
	    "    <rdfs:domain rdf:resource=\"#Concept\"/>\n"+
	    "    <rdfs:range rdf:resource=\"#Concept\"/>\n"+
	    "  </owl:ObjectProperty>\n\n"+
	    
	    "  <owl:ObjectProperty rdf:ID=\"hasSynonym\">\n"+
	    "    <rdf:type rdf:resource=\"&owl;SymmetricProperty\"/>\n"+
	    "    <rdf:type rdf:resource=\"&owl;TransitiveProperty\"/>\n"+  //does it?
	    "    <rdfs:domain rdf:resource=\"#Concept\"/>\n"+
	    "    <rdfs:range rdf:resource=\"#Concept\"/>\n"+
	    "  </owl:ObjectProperty>\n\n"+
	    
	    "  <owl:ObjectProperty rdf:ID=\"hasCategory\">\n"+
	    "    <rdfs:domain rdf:resource=\"#Concept\"/>\n"+
	    "    <rdfs:range rdf:resource=\"#Concept\"/>\n"+
	    "  </owl:ObjectProperty>\n\n"+
	    
	    "  <owl:ObjectProperty rdf:ID=\"hasSibling\">\n"+
	    "    <rdf:type rdf:resource=\"&owl;SymmetricProperty\"/>\n"+
	    "    <rdf:type rdf:resource=\"&owl;TransitiveProperty\"/>\n"+
	    "    <rdfs:domain rdf:resource=\"#Concept\"/>\n"+
	    "    <rdfs:range rdf:resource=\"#Concept\"/>\n"+
	    "  </owl:ObjectProperty>\n\n"+
	    
	    "  <owl:ObjectProperty rdf:ID=\"hasUnknown\">\n"+
	    "    <rdf:type rdf:resource=\"&owl;SymmetricProperty\"/>\n"+
	    "    <rdf:type rdf:resource=\"&owl;TransitiveProperty\"/>\n"+
	    "    <rdfs:domain rdf:resource=\"#Concept\"/>\n"+
	    "    <rdfs:range rdf:resource=\"#Concept\"/>\n"+
	    "  </owl:ObjectProperty>\n\n";

	// TODO: clean up the FOLDOCReader coded definition
	private static String getDef (Set<Link> kids, Node term) {
		// if from multi-sources, will need to store that info in Node
		// for now just grab a filename in any Link, if there is any. 
		// if this Node doesn't have any Link, try DEFAULT_FILE.
		// if DEFAULT_FILE is null, return null (not "")
		
		// get filename
		File file = DEFAULT_FILE;
		if (!kids.isEmpty()) {
		  //String name = kids.iterator().next().getFilename();
		  file = new File(kids.iterator().next().getFilename());
		}

		// return null, if no filename found or is category
		if (file==null || term.getKey().startsWith("-")) return null;
		
		// create an empty RDF model, if not created already
		if (model == null) {
			System.err.println("creating a model...");
			model = ModelFactory.createDefaultModel();
			
			// use the FileManager to find the input file
			System.err.println("opening file for definition: " + file);
			InputStream in = null;
			try {
			  in = FileManager.get().open(file.getAbsolutePath());
			} catch (IllegalArgumentException iae) {
			  System.err.println("File: " + file.getName() + " not found!\n" +
					     "Trying default file...");
			  in = FileManager.get().open(DEFAULT_FILE.getAbsolutePath());
			  if (in == null)
			    throw new IllegalArgumentException( "File: " + DEFAULT_FILE.getName() + " not found");
			}
			
			// read the SKOS RDF/XML file
			System.err.println("the model is reading the file...");
			model.read( in, "" );
		}

		// read the definition
		// get the reference of the definitinon
		if (DEBUG) System.err.println("reading def..."+term.getKey());
		ResIterator iter = model.listSubjectsWithProperty(SKOS.prefLabel, 
				model.createLiteral(term.getKey()));
		
		Resource res=null;
		if (iter.hasNext()) 
			res = iter.nextResource();
		if (res == null) {
			System.err.println("Cannot find reference for term: "+term.getKey());
			return null;
		}

		if (DEBUG) System.err.println("Got resource for \""+term.getKey()+"\": "+res);

		// Get the skos:definition tag(s)
		StmtIterator iter2 = model.listStatements(res,SKOS.definition,(RDFNode)null);
		String def=null;
		if (iter2.hasNext())
			def = iter2.nextStatement().getObject().toString();
		
		if (DEBUG) System.err.println(def);
		
		//tag keywords of each Link inside the definition
		for (Link l : kids) {
			String key = l.getTok().toString(); //get keyword
			if (key == null || 
				key.equals(l.getChild().getKey()) || 
				key.equals(l.getParent().getKey()) ||
				key.equals("")) {
				//happens in category terms, which shouldn't happen
				// or when key == "", it's UNKNOWN relationship
				break;
			}
			// check if the keyword is in this definition
			if (version.equals("v1.1") && term.getKey().equals(l.getInDef()))
				def = tagDef(def, l, term);
		}
		
		return def;
	}
	
	//TODO: use Porter Stemming
	//TODO: miss identified sentence
	//TODO: span search over the whole def, instead of each sentence?
	//TODO: find words in hyperlinks
	private static String tagDef (String def, Link l, Node term) {
		def = def.replaceAll("[Ee]\\.g\\.","eg").replaceAll("[Ii]\\.e\\.","ie");
		String defLower = def.toLowerCase();
		Node notTerm = l.not(term);
		String keyword = l.getTok().toString();
		String keyLower = keyword.toLowerCase();
		int ix = defLower.indexOf(keyLower);
		
		// if no keyword found in the definition, do nothing; shouldn't happen
		if (ix < 0 ) {
			System.err.println("keyword "+keyword+" not found in def:\n"+def+'\n');
			try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		// if only one keyword found, tag it (easy!)
		} else if (ix == defLower.lastIndexOf(keyLower)) {
			def = def.replaceFirst(keyword, 
					"<sago:linkRef rdf:resource=\""+
					OutputUtils.toURICompatible(notTerm.getKey())+"\">"+
					keyword+"</sago:linkRef>");
		// if more than one keyword found, 
		// need to find the one followed by notTerm.getKey()
		} else {
			SentenceTokenizer st = new SentenceTokenizer();
			List<String> sentList = st.tokenize(defLower);
			String theSent = null;
			for (String str : sentList) {
				int ix1 = str.indexOf(keyLower);
				int ix2 = str.indexOf(notTerm.getKey().toLowerCase());
				if (ix1 > -1 && ix2 > -1 && ix1 < ix2) { 
					theSent = str; 
					break; 
				}
			}
			if (theSent != null) {
				ix = defLower.indexOf(theSent);
				ix = defLower.indexOf(keyLower, ix);
				def = def.substring(0,ix) + 
					"<sago:linkRef rdf:resource=\""+
					OutputUtils.toURICompatible(notTerm.getKey())+"\">"+
					keyword+"</sago:linkRef>" +
					def.substring(ix+keyword.length());
			} else {
				System.err.println(keyword+" AND "+notTerm.getKey()+" not found in: "+defLower);
//				System.err.println(sentList);
//				try {
//					System.in.read();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			}
		}
		if (DEBUG) System.err.println(def);
		return def;
	}

	public static void outputSKOS(HashGraph g, PrintWriter rdf) throws IOException {
		outputSKOS(g, rdf, null);
	}
	public static void outputSKOS(HashGraph g, PrintWriter rdf, File file) throws IOException {
		DEFAULT_FILE = file;
		//generate RDF
		Normalizer norm = null;
		try {
			norm = new Normalizer(g);
		} catch (IllegalArgumentException iae) {
			norm = Normalizer.NULL_NORMALIZER;
		}

		//need to generate serials in alphabetical order
		List<String> strings = new ArrayList<String>(g.size());
		for (Iterator<Node> it = g.nodeIterator(); it.hasNext();)
			strings.add(it.next().getKey());

		//sort humanish-alphabetically (A-Z only)
		java.text.Collator sorter = java.text.Collator.getInstance();
		sorter.setStrength(java.text.Collator.PRIMARY);
		Collections.sort(strings, sorter);

		rdf.print(RDF_HEADER);
		if (!SKOS_ONLY) rdf.print(OWL_PRE); // if using OWL

		for (String key : strings) {
			Node n = g.get(key);
			//output head
			rdf.println("  <skos:Concept rdf:about=\"" + OutputUtils.toURICompatible(key) + "\">");
			rdf.println("    <sago:cweight>"+OutputUtils.rdfNumForm.format(norm.norm(n.getDCost()))+
					"</sago:cweight>");
			rdf.println("    <skos:prefLabel>" + key.replaceAll("\\&","&amp;") + "</skos:prefLabel>");

			//output children
			//generate kids from _bidirectional_ links
			Set<Link> kids = OutputUtils.getLinksFromNode(n);

			String def = getDef(kids, n);
			if (def == null) // category
				rdf.println("    <skos:scopeNote>category</skos:scopeNote>");
			else
				rdf.println("    <skos:definition>"+ def +"</skos:definition>");

			//make sure there's no link to itself (eg a loop; it happens eg 'recursion')
			Set<Node> linked = new HashSet<Node>(); //don't link a node twice
			linked.add(n);		    //never link to itself

			for (Link l : kids) {
				Node peer = l.not(n);
				if (linked.contains(peer)) continue; //already linked
				linked.add(peer);
				String kk = peer.getKey();
				rdf.println(getTag(l, kk));
			}
			
			//output tail
			rdf.println("  </skos:Concept>\n");
		}
		model.close();
		rdf.println(OutputUtils.RDF_TAIL);
//	rdf.close();
	}
	private static String getTag (Link l, String term) {
		Token tok = l.getTok();
		String ret="";
		if (SKOS_ONLY) {
			switch (tok.getFlags() & Token.M_ASSOC) {
			case Token.CHILD: ret = "narrower"; break;
			case Token.CATEGORY: /*ret = "subject"; break;*/
			case Token.PARENT: ret = "broader"; break;
			case Token.DISSIM:
			case Token.SIBLING:
			case Token.UNKNOWN:
			case Token.SYNONYM: ret = "related"; break;
			default: ret = "unknown token flag!"; break;
			}
			if (DEBUG) System.err.println(ret);
			ret = "    <skos:"+ret +" rdf:resource=\"#" +
					OutputUtils.toURICompatible(term) + "\">\n" +
				"      <sago:keyword sago:source=\""+
					OutputUtils.toURICompatible(l.getInDef())+"\">"+
					tok.toString()+"</sago:keyword>\n" +
				"      <sago:strength>"+tok.strengthString() + "</sago:strength>\n" + 
				"      <sago:type>"+tok.typeString() + "</sago:type>\n" +
				"      <sago:weight>"+OutputUtils.rdfNumForm.format(l.getWeight()) + "</sago:weight>\n"+
				"    </skos:" + ret + ">";
		} else
			ret = "    <has" + OutputUtils.capitalise(tok.typeString()) +
				" rdf:resource=\"#" + OutputUtils.toURICompatible(term) + "\" />";

		return ret;
	}
}
