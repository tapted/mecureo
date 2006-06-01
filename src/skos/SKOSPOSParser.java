package skos;

import posparser.POSWrapper;
import foldocparser.*;
import foldocpos.*;
import hashgraph.HashGraph;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

/**
 * 
 * @author William T. Niu
 * @since 31 December 2005
 */
@SuppressWarnings("unchecked")
public class SKOSPOSParser extends POSParser {

	/** The RDF Model of the SKOS file */
	private Model model;
	
	public SKOSPOSParser() {
		super();
		//model = ModelFactory.createDefaultModel();
	}

	/**
	 * Initialises an RDF model with the current SKOS dictionary file.
	 */
	private void createModel() {
        // create an empty RDF model
        model = ModelFactory.createDefaultModel();
        
        // use the FileManager to find the input file
        InputStream in = FileManager.get().open(curFile);
        if (in == null)
            throw new IllegalArgumentException( "File: " + curFile + " not found");
       
        // read the SKOS RDF/XML file
        model.read( in, "" );
	}
	
	public HashGraph parse(String fileName) throws IOException {
		graph.sourceFilenames.add(fileName);
		curFile = fileName;
		POSTokenizer.S_OUTTOK = NLP_OUTPUT;
		if (DEBUGGING) System.err.println("\nParsing in SKOSPOSParser...");

		// create an RDF model with fileName
		if (DEBUGGING) System.err.println("Creating RDF model");
		createModel();

		pt = new SKOSTokenizer(this, model);
		
		// process categories and definitions
		if (DEBUGGING) System.err.println("PreParsing...");
		preParse(fileName);
		
		logfile = new PrintWriter(
					new BufferedWriter(
						new FileWriter("detected2.log")));
		// "clean up" and stem the definition words in defn_restrict
		if (DEBUGGING) System.err.println("Stemming...");
		makeStems();

		dh = new DictHandler(pt);
		t = new Tokenizer(0); //dummy toker for line numbers

		int bad_defs = 0;
		gooddefs = 0;

		for (String s : pt.defSet()) {
			try {
				if (NLP_OUTPUT || SHOW_UNCLAS || s.matches(".*&.*")) 
					System.err.println("=======" + s + "=======");
				definition(s);
				++gooddefs;
				if (DEBUGGING) System.err.print(/*"\033[1G"*/"\n" + gooddefs);
			} catch (Exception e) {
				bad_defs++;
				System.err.println("Uncaught Exception #" + bad_defs + 
						": the NLP parser may have got lost in " +
						s + "\non or around [" + /*pt.input.getLineNumber() +*/ "]. Details:");
				e.printStackTrace();
				System.err.println("Press Enter to continue");
				System.in.read();
			}
		}

		System.err.println("Had " + bad_defs + " exceptions");
		logfile.close();
		return graph;
	}
	
	/** 
	 * Do a pre-parse so that nodes can be validated. Category file (categories.txt)
	 * is processed and stored in categories. Definition terms are stored in 
	 * defn_restrict.
	 * */
	protected void preParse(String fileName) throws IOException {
		try {
			BufferedReader catFile = new BufferedReader(
										new FileReader(CATEGORY_FILE));
			String s;
			while ((s = catFile.readLine()) != null) {
				//prepend a hyphen for categories in graph!
				hashgraph.Node n = graph.getAddNode(CATEGORY_PREPEND + s);
				categories.put(s, n);
			}

			for (String s2 : pt.defSet())
				defn_restrict.add(s2);
				
			System.err.println("Preparse done: found " + defn_restrict.size() +
					   " defintions for " + graph.size() + " categories.");
		} catch (IOException ioe) {
			System.err.println("IO Error in preparse");
			throw ioe;
		}
	}

//	public void token(Token tok, String tag) {
//		super.token(tok, tag);
//	}
//	
	protected void definition(String d) throws IOException {
		if (!defn_restrict.contains(d))
			return; //ignore: it's been restricted
		
		prog = 0;
		syn = true;
		cur = graph.getAddNode(d);
		keyword = Token.T_NORM;
		curDef = d;
		tagSet = new HashMap();
		firstSentence = true;

		// read the definition
		// this should return a single Resource (or Subject)
		ResIterator iter = model.listSubjectsWithProperty(SKOS.prefLabel, 
				model.createLiteral(d));//.replaceAll("\\&",AMP)));
		Resource res=null;
		if (iter.hasNext()) 
			res = iter.nextResource();

		// extract Web reference
		try {
//			if (res==null) {
//				System.err.println("d: "+d);
//				System.in.read();
//			}
			String str = res.toString();//.replaceFirst(Dict2SKOS.DEFAULT_WEBREF_PREFIX,"");
			int num = Integer.parseInt(str);
			if (DEBUGGING) System.err.println("[" + str + "]");
			token(new Token(Token.WEB_REF, Integer.toString(num)));
		} catch (NumberFormatException nfe) {/*only want integer*/}
		
		if (DEBUGGING) System.err.println("Got resource for \""+d+"\": "+res);

		StmtIterator iter2 = model.listStatements(res,SKOS.definition,(RDFNode)null);
		String def="";
		
		// Get the definition...
		// RDF parser doesn't like '&', so convert the coded '&' back
		if (iter2.hasNext())
			def = iter2.nextStatement().getObject().toString();//.replaceAll(AMP, "&");
		
		if (NLP_OUTPUT || SHOW_UNCLAS) System.err.println(def);

		try {
			POSWrapper.parse(def, dh);
		} catch (SAXException saxe) {
			saxe.printStackTrace();
		}
		
		if (NLP_OUTPUT) {
			System.err.println("TAGS for \"" + d + "\":\n" + tagSet);
			dumpNode(d);
 			if (!d.startsWith("-")) {
				System.err.print("Press Enter to continue...");
				System.in.read();
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		OutputStream out = null;
		if (args.length < 1) {
			System.err.println("Usage: java ... SKOSPOSParse inputFile [options] outputFile\n" +
					"Options:\n" +
					"\t-l	  : Allow Leaves\n" +
					"\t-w	  : Show warnings\n" +
					//"\t-s	  : Show statistics\n" +
					"\t-c File : use File (next param) as category file\n" +
					"\t-C Int  : use Int (next param) as the 'C' value [default 10]\n" +
					"\t-f [dot,hci,owl,rdf,skos,xml]: select additional output files\n"+
					"\t-m [-1,0,1,2,3,4,5]: set the Match Level -1:None 5:Substring matching\n" +		
			"if inputFile ends in \".gz\" GZIPInputStreams will be used");
			return;
		}
		//boolean STATS = false;
		int outputFormats = 0;
		List<String> formatList = new ArrayList<String>();
		formatList.add("dot"); formatList.add("hci"); formatList.add("owl");
		formatList.add("rdf"); formatList.add("skos"); formatList.add("xml");
		
		for (int i = 1; i < args.length; ++i) {
			if (args[i].startsWith("-")) {
				if (args[i].indexOf('l') >= 0)
					Parser.ALLOW_LEAVES = true;
				if (args[i].indexOf('w') >= 0)
					Parser.WARNINGS = true;
//				if (args[i].indexOf('s') >= 0)
//					STATS = true;
				if (args[i].indexOf('f') >= 0) {
					while (++i<args.length) {
						int j = formatList.indexOf(args[i]);
						if (j >= 0)	outputFormats |= (int)Math.pow(2,j); 
						else { i--; break; }
					}
					continue;
				}
				if (args[i].indexOf('c') >= 0 && i + 1 < args.length)
					Parser.CATEGORY_FILE = args[++i];
				if (args[i].indexOf('C') >= 0 && i + 1 < args.length)
					Parser.WEIGHT_C = Integer.parseInt(args[++i]);
				if (args[i].indexOf('m') >= 0 && i + 1 < args.length)
					Parser.MATCH_LEVEL = Integer.parseInt(args[++i]);
			} else if (i == args.length - 1) {
				out = new FileOutputStream(args[i]);
			} else {
				System.err.println("Unknown option: " + args[i]);
				return;
			}
		}
		if (out == null) {
			System.err.println("You must specify an output filename");
			return;
		}
		System.out.println("Using detect match level: ");
		switch (Parser.MATCH_LEVEL) {
		case (-1): System.out.println("-1: No Matching"); break;
		case (0): System.out.println(" 0: Exact Matching Only"); break;
		case (1): System.out.println(" 1: Trimed Matching"); break;
		case (2): System.out.println(" 2: Alphanumeric Matching"); break;
		case (3): System.out.println(" 3: Case Insensitive Matching"); break;
		case (4): System.out.println(" 4: Componenent Stem Matching"); break;
		case (5): System.out.println(" 5: Substring Matching"); break;
		default:  System.out.println(" <<Unknown match level>>");
		}
//		if (STATS) {
//			POSParse.doStats(args[0]);
//		}
//		
		SKOSPOSParser p = new SKOSPOSParser();
		HashGraph g = p.parse(args[0]);
//		System.err.print("Categories");
//		java.util.Iterator it = p.categories.values().iterator();
//		for (;it.hasNext();)
//		System.out.println(it.next());
		
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(out));
		
		g.writeData(oos);
		System.err.println("Output graph to " + args[args.length - 1]);
		
		// output to other formats
		for (int i=0; i<6; i++) {
			if ((outputFormats & (int)Math.pow(2,i)) != 0) {
				String ext= formatList.get(i);
				PrintWriter writer =  new PrintWriter(new BufferedWriter(
						new FileWriter(args[0]+"."+ext)));
				Class toRun = Class.forName(
						"foldocparser.tools.Output"+ext.toUpperCase());
				Method outMethod = toRun.getMethod(
						"output"+ext.toUpperCase(),
						Class.forName("hashgraph.HashGraph"), 
						Class.forName("java.io.PrintWriter"));
				outMethod.invoke(null, g, writer);
				System.err.println("Output graph to " + args[0]+'.'+ext);
				writer.close();
			}
		}
		
		foldocparser.Parse.showCStats(g);
	}

}
