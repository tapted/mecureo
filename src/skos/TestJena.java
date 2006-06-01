package skos;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;

import java.io.*;

import java.util.Iterator;


/** Tutorial 8 - demonstrate Selector methods
 *
 * @author  bwm - updated by kers/Daniel
 * @version Release='$Name:  $' Revision='$Revision: 1.3 $' Date='$Date: 2005/10/06 17:49:05 $'
 */
public class TestJena extends Object {
    
    static final String inputFileName = "dict\\atl.rdf";
    
    public static void main (String args[]) {
    	System.out.println("power: "+(2^7));
        // create an empty model
//        Model model = ModelFactory.createDefaultModel();
//       
//        // use the FileManager to find the input file
//        InputStream in = FileManager.get().open(inputFileName);
//        if (in == null) {
//            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
//        }
//        
        // read the RDF/XML file
//        model.read( in, "" );
        
        // select all the resources with a VCARD.FN property
        // whose value ends with "Smith"
//        StmtIterator iter = model.listStatements(
//            new 
//                SimpleSelector(null, VCARD.FN, (RDFNode) null) {
//                    public boolean selects(Statement s) {
//                            return s.getString().endsWith("Smith");
//                    }
//                });
//        StmtIterator iter2 = model.listStatements(
//                new 
//                    SimpleSelector(null, skos.Concept, (RDFNode) null) {
//                        public boolean selects(Statement s) {
//                                return s.getString().endsWith("Smith");
//                        }
//                    });
//        String skos = "http://www.w3.org/2004/02/skos/core#";
//        Property pro = model.createProperty(skos+"prefLabel");
//        Property def = model.createProperty(skos+"definition");
//        Resource res = model.createResource(skos+"Concept");
//        //Selector sel = new SimpleSelector(null, pro, null);
//        //StmtIterator iter = model.listStatements((Resource)null, pro, (RDFNode)null);
//        //Iterator iter = model.listObjectsOfProperty(res, null);
//        StmtIterator iter = model.listStatements();

		//ResIterator iter = model.listSubjectsWithProperty(pro, model.createLiteral("Data mining"));
		//Resource res2=null;
		//if (iter.hasNext()) 
		//	res2 = iter.nextResource();
		
		//System.err.println(res2);
		
//        RDFNode n = model.createLiteral("Data mining");
//		ResIterator iter = model.listSubjectsWithProperty(pro,n);
//
//		Resource res2=null;
//		if (iter.hasNext()) {
//			res2 = iter.nextResource();
//		}

		// this should return a single Statement
//		NodeIterator iter2 = model.listObjectsOfProperty(res,SKOS.definition);
//		String def="";
//		if (iter2.hasNext())
//			def = iter2.nextNode().toString();
//		
//		Iterator iter2 = model.listObjectsOfProperty(res2,def);
//		String def2="";
//		if (iter2.hasNext())
//			def2 = iter2.next().toString();
//		System.out.println("def: "+def);
//        for (Iterator i = (Iterator)model.listObjectsOfProperty(pro);i.hasNext();)
//        	System.out.println("-->"+i.next().toString()+"<--");
//        
//        if (iter.hasNext()) {
//            System.out.println("The database contains vcards for:");
//            while (iter.hasNext()) {
//                //System.out.println("  " + iter.next());
//                System.out.println("  " + iter.nextStatement());
//                                              //.getString());
//            }
//        } else {
//            System.out.println("No Smith's were found in the database");
//        }            
    }
}
