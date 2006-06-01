/*
 * Created on 25/08/2003
 */
 
package foldocparser.tools;

import hashgraph.HashGraph;
import hashgraph.Link;
import hashgraph.Node;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Andrew, hacked by William Niu on 4 January, 2006
 *
 */
public class OutputOWL {
	private static final char nl = '\n';
	private static final String OWL_HEADER =
  "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + nl + nl +
  "<rdf:RDF" + nl + 
  "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + nl + 
  "  xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" + nl +
  "  xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" + nl +
  "  xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" + nl +
  "  xmlns=\"http://www.it.usyd.edu.au/~alum/ontologies/mecureo#\"" + nl + 
  "  xml:base=\"http://www.it.usyd.edu.au/~alum/ontologies/mecureo\">" + nl + nl +

  "  <owl:Ontology>" + nl +
  "    <rdfs:comment>" + nl +
  "      Mecureo Generated Ontology" + nl +
  "    </rdfs:comment>" + nl +  
  "  </owl:Ontology>" + nl + nl +
  
  "<owl:Class rdf:ID=\"Concept\">" + nl + 
  "</owl:Class>" + nl + nl +
  
  "<owl:ObjectProperty rdf:ID=\"hasChild\">" + nl + 
  "  <rdfs:domain rdf:resource=\"#Concept\"/>" + nl +
  "  <rdfs:range rdf:resource=\"#Concept\"/>" + nl +
  "  <owl:inverseOf rdf:resource=\"#hasParent\"/>" + nl +
  "</owl:ObjectProperty>" + nl + nl + 
  
  "<owl:ObjectProperty rdf:ID=\"hasParent\">" + nl + 
  "  <rdfs:domain rdf:resource=\"#Concept`\"/>" + nl +
  "  <rdfs:range rdf:resource=\"#Concept\"/>" + nl +
  "  <owl:inverseOf rdf:resource=\"#hasChild\"/>" + nl +
  "</owl:ObjectProperty>" + nl + nl + 

  "<owl:ObjectProperty rdf:ID=\"hasAntonym\">" + nl + 
  "  <rdfs:domain rdf:resource=\"#Concept\"/>" + nl +
  "  <rdfs:range rdf:resource=\"#Concept\"/>" + nl +
  "</owl:ObjectProperty>" + nl + nl + 

  "<owl:ObjectProperty rdf:ID=\"hasSynonym\">" + nl + 
  "  <rdfs:domain rdf:resource=\"#Concept\"/>" + nl +
  "  <rdfs:range rdf:resource=\"#Concept\"/>" + nl +
  "</owl:ObjectProperty>" + nl + nl + 

  "<owl:ObjectProperty rdf:ID=\"hasCategory\">" + nl + 
  "  <rdfs:domain rdf:resource=\"#Concept\"/>" + nl +
  "  <rdfs:range rdf:resource=\"#Concept\"/>" + nl +
  "</owl:ObjectProperty>" + nl + nl + 

  "<owl:ObjectProperty rdf:ID=\"hasSibling\">" + nl + 
  "  <rdfs:domain rdf:resource=\"#Concept\"/>" + nl +
  "  <rdfs:range rdf:resource=\"#Concept\"/>" + nl +
  "</owl:ObjectProperty>" + nl + nl + 

  "<owl:ObjectProperty rdf:ID=\"hasUnknown\">" + nl + 
  "  <rdfs:domain rdf:resource=\"#Concept\"/>" + nl +
  "  <rdfs:range rdf:resource=\"#Concept\"/>" + nl +
  "</owl:ObjectProperty>" + nl;

	private static final String OWL_TAIL = nl + "</rdf:RDF>";
	
	@SuppressWarnings("unchecked")
	public static void outputOWL(HashGraph g, PrintWriter rdf) throws IOException {
		//need to generate serials in alphabetical order
		List<String> strings = new ArrayList<String>(g.size());
		for (Iterator<Node> it = g.nodeIterator(); it.hasNext();)
			strings.add((String)it.next().getKey());

		//sort humanish-alphabetically (A-Z only)
		java.text.Collator sorter = java.text.Collator.getInstance();
		sorter.setStrength(java.text.Collator.PRIMARY);
		Collections.sort(strings, sorter);

		rdf.print(OWL_HEADER);

		//generate is alphabetical order for clarity. gets are O(1) anyway..
		for (String key : strings) {
			Node n = g.get(key);
			//output head
			rdf.println("  <Concept rdf:ID=\"" + OutputUtils.toURICompatible(key) + "\">");
			rdf.println("    <dc:Title>" + 
					key.replaceAll("\\&","&amp;") + "</dc:Title>");
			
			//output children
			//generate kids from _bidirectional_ links
			Set<Link> kids = OutputUtils.getLinksFromNode(n);
			
			//make sure there's no link to itself (eg a loop; it happens eg 'recursion')
			HashSet<Node> linked = new HashSet<Node>(); //don't link a node twice
			linked.add(n);		    //never link to itself

			for (Link l : kids) {
				Node peer = l.not(n);
				if (linked.contains(peer)) continue; //already linked
				linked.add(peer);
				String kk = peer.getKey();
				rdf.println("    <has" + OutputUtils.capitalise(l.typeString()) + 
						" rdf:resource=\"#" + OutputUtils.toURICompatible(kk) + "\" />");
			}
			//output tail
			rdf.println("  </Concept>" + nl);
		}
		rdf.println(OWL_TAIL);
	}
}
