package foldocparser.tools;

import hashgraph.HashGraph;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Read in an FDG file and output in one or more available formats. 
 * So far the supported formats are: DOT, HCI, OWL, RDF, SKOS, XML.</p>
 * Usage: java ... FDG2Other inputFile [formats]<br />
 * For example, "java FDG2Other in.fdg owl xml rdf" will output files in OWL,
 * XML, and RDF formats.
 * @author William
 *
 */
public class FDG2Other {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: java ... java ... FDG2Other inputFile [formats]+\n" +
					"\t-f formats are one or more of [dot,hci,owl,rdf,skos,xml]\n\n"+
					"For example, \"java FDG2Other in.fdg owl xml rdf\" \n"+
					"will output files in OWL, XML, and RDF formats.");
			return;
		}
		int outputFormats = 0;
		List<String> formatList = new ArrayList<String>();
		formatList.add("dot"); formatList.add("hci"); formatList.add("owl");
		formatList.add("rdf"); formatList.add("skos"); formatList.add("xml");
		
		for (int i=1; i < args.length; i++) {
			int j = formatList.indexOf(args[i].toLowerCase());
			if (j >= 0)	outputFormats |= (int)Math.pow(2,j); 
		}
		
		HashGraph g = new HashGraph();
		ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(
						new FileInputStream(args[0])));
		g.readData(ois);
		foldocparser.Parse.showCStats(g);
		
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
				//	foldocparser.tools.OutputSKOS.outputSKOS(g, writer, 
				//     new java.io.File("/home/niu/USydney/workspace/Sago/dict/foldoc.skos1"));
				System.err.println("Output graph to " + args[0]+'.'+ext);
				writer.close();
			}
		}
		ois.close();
	}

}
