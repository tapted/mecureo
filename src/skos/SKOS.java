/**
 * 
 */
package skos;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Vocabulary definitions for SKOS Core. Adopting the style in package
 * com.hp.hpl.jena.vocabulary.
 * @author William T. Niu
 * @since 1 January 2006
 */
public class SKOS
{

	  protected static final String uri ="http://www.w3.org/2004/02/skos/core#";

	  /** returns the URI for this schema
	   * @return the URI for this schema
	   */
	  public static String getURI() {
	        return uri;
	  }

	  private static Model m = ModelFactory.createDefaultModel();

	  public static final Resource Concept = m.createResource(uri + "Concept" );
	  public static final Resource ConceptScheme = m.createResource(uri + "ConceptScheme" );
	  public static final Resource CollectableProperty = m.createResource(uri + "CollectableProperty" );
	  public static final Resource Collection = m.createResource(uri + "Collection" );
	  public static final Resource OrderedCollection = m.createResource(uri + "OrderedCollection" );
	  
	  public static final Property altLabel = m.createProperty(uri, "altLabel" );
	  public static final Property altSymbol = m.createProperty(uri, "altSymbol" );
	  public static final Property broader = m.createProperty(uri, "broader" );
	  public static final Property changeNote = m.createProperty(uri, "changeNote" );
	  public static final Property definition = m.createProperty(uri, "definition" );
	  public static final Property editorialNote = m.createProperty(uri, "editorialNote" );
	  public static final Property example = m.createProperty(uri, "example" );
	  public static final Property hasTopConcept = m.createProperty(uri, "hasTopConcept" );
	  public static final Property hiddenLabel = m.createProperty(uri, "hiddenLabel" );
	  public static final Property historyNote = m.createProperty(uri, "historyNote" );
	  public static final Property isPrimarySubjectOf = m.createProperty(uri, "isPrimarySubjectOf" );
	  public static final Property isSubjectOf = m.createProperty(uri, "isSubjectOf" );
	  public static final Property member = m.createProperty(uri, "member" );
	  public static final Property memberList = m.createProperty(uri, "memberList" );
	  public static final Property narrower = m.createProperty(uri, "narrower" );
	  public static final Property note = m.createProperty(uri, "note" );
	  public static final Property prefLabel = m.createProperty(uri, "prefLabel" );
	  public static final Property prefSymbol = m.createProperty(uri, "prefSymbol" );
	  public static final Property primarySubject = m.createProperty(uri, "primarySubject" );
	  public static final Property related = m.createProperty(uri, "related" );
	  public static final Property scopeNote = m.createProperty(uri, "scopeNote" );
	  public static final Property semanticRelation = m.createProperty(uri, "semanticRelation" );
	  public static final Property subject = m.createProperty(uri, "subject" );
	  public static final Property subjectIndicator = m.createProperty(uri, "subjectIndicator" );
	  public static final Property symbol = m.createProperty(uri, "symbol" );
}