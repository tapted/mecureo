package skos;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

import foldocpos.POSTokenizer;
import foldocpos.POSParser;

/**
 * There is no special brackets to parse (i.e. "\\*[1-6]"), so states are
 * irrelevant.
 * @author William T. Niu
 * @since 1 January 2006
 */
public class SKOSTokenizer extends POSTokenizer
{
	Model input;

	/**
	 * @param parser 
	 * @param in the generated RDF model
	 */
	public SKOSTokenizer (POSParser parser, Model in) {
		super(parser, null);
		this.input = in;
	}

	/**
	 * Return a Set of definition terms, excluding categories and hyperlinks.
	 * Not very good in terms of performance...
	 * @return an Set&lt;String&gt; of definitions
	 */
	public Set<String> defSet() {
		Set<String> toRemove = new HashSet<String>();
		Set<String> toReturn = new HashSet<String>();
		
		// a list of statements of categories or hyperlinks
		// want to get the class identifiers (rdf:about)
		StmtIterator noteIter = input.listStatements(
			new SimpleSelector(null,SKOS.scopeNote,(RDFNode)null) {
				public boolean selects (Statement s) {
					return s.getString().matches(".*[(category)(hyperlink)].*");
				}
			});
		
		while (noteIter.hasNext()) {
			Resource res = noteIter.nextStatement().getSubject();
			NodeIterator defIter =  input.listObjectsOfProperty(res,SKOS.prefLabel);
			try {
				toRemove.add(defIter.nextNode().toString());
			} catch(Exception e) {
				// when defIter is null
				System.err.println("Term with Identifier \""+res+"\" is missing definition!");
			}
		}
		
		StmtIterator defIter =  input.listStatements(null,SKOS.prefLabel,(RDFNode)null);
		while (defIter.hasNext()) {
			String s = defIter.nextStatement().getString();
			if (!toRemove.contains(s))
				toReturn.add(s);//.replaceAll(SKOSPOSParser.AMP,"&"));
		}
		return toReturn;
	}
	
	protected void resetState() {
		state = 0;
		inval = null;
		lastTag = null;
		gotStar = false;
		phrase.setLength(0);
	}

}
