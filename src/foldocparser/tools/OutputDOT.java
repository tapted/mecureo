/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Trent Apted, University of Sydney
 * Author email       tapted@it.usyd.edu.au
 * Package            Mecureo
 * Web                
 * Created            
 * Filename           $RCSfile: OutputDOT.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003/09/09 00:36:29 $
 *               by   $Author: alum $
 *
 * ****************************************************************************/

package foldocparser.tools;

import hashgraph.*;
import java.io.*;
import java.util.*;


public class OutputDOT {

	private static final double VERT_EPSILON = 0; //increasing this will make a vertical dot graph wider

	//private static int counter = 0;

	public static void outputDOT(HashGraph g, PrintWriter dot) throws IOException {
		outputDOT(g, dot, 1, 1, false, true);
	}

	public static void outputVerticalDOT(HashGraph g, PrintWriter dot) throws IOException {
		outputDOT(g, dot, 1, 1);
	}

	public static void outputDOT(HashGraph g,
								 PrintWriter dot,
								 int MIN_KIDS,
								 int MIN_LINKS) throws IOException {
		outputDOT(g, dot, MIN_KIDS, MIN_LINKS, true, true);
	}

	public static void outputDOT(HashGraph g,
								 PrintWriter dot,
								 int MIN_KIDS,
								 int MIN_LINKS,
								 boolean vert,
								 boolean lineStyles) throws IOException {
		outputDOT(g, dot, MIN_KIDS, MIN_LINKS, vert, lineStyles, null);
	}

	public static void autoDOTCluster(HashGraph g,
								 PrintWriter dot,
								 int MIN_KIDS,
								 int MIN_LINKS) throws IOException {
		ArrayList subgraphs = new ArrayList();
		subgraphs.add("Target");
		subgraphs.add("Source");
		subgraphs.add("Common");
		outputDOT(g, dot, MIN_KIDS, MIN_LINKS, true, true, subgraphs.iterator());
	}

	//order for subgraphs (Strings) are
	//'THIS' [target], 'OTHER', and 'COMMON'
	public static void outputDOT(HashGraph g,
								 PrintWriter dot,
								 int MIN_KIDS,
								 int MIN_LINKS,
								 boolean vert,
								 boolean lineStyles,
								 Iterator subgraphs) throws IOException {

		System.err.println("Outputting DOT...");

		//"dashed", "dotted", "solid", "invis" and "bold"
//	PrintWriter dot = new PrintWriter(new BufferedWriter(
//					   new FileWriter(DOT_FILE_OUTPUT)));
		HashSet in = new HashSet();
		ArrayList sg = new ArrayList(g.getNodes());
		Collections.sort(sg, new PeerageComparator());

		ArrayList outputSet = new ArrayList(); //of NodeNodeLinkSet s

		dot.print("digraph G {\n  ");
		if (subgraphs != null) {
			//print the node listings for each subgraph then proceed as usual
			ArrayList[] subnodes = {new ArrayList(), new ArrayList(), new ArrayList()};

			//collect names
			for(Iterator nodes = sg.iterator(); nodes.hasNext(); ) {
				Node n = (Node)nodes.next();
				String ns = serialToStr(n.getSerial());
				switch (n.temp) {
				case HashGraph.MERGE_THIS:
					subnodes[0].add(ns);
					break;
				case HashGraph.MERGE_OTHER:
					subnodes[1].add(ns);
					break;
				case HashGraph.MERGE_COMMON:
					subnodes[2].add(ns);
					break;
				default:
					throw new RuntimeException("Cannot cluster an unmerged graph");
				}
			}

			//output

			dot.print("compound = true;\nremincross = true;\n");

			for (int i = 0; i < subnodes.length; ++i) {
				String name = subgraphs.next().toString();
				dot.print("subgraph cluster" + name + " {\n" +
						  "\tlabel = \"" + name + "\";\n\t");
				for (Iterator names = subnodes[i].iterator(); names.hasNext(); ) {
					dot.print(names.next() + ";");
				}
				dot.print("\n}\n");
			}
		}

		//CONVERT GRAPH to a set of NodeNodeLinkSet objects
		for(Iterator nodes = sg.iterator(); nodes.hasNext(); ) {
			Node n = (Node)nodes.next();
			if ((n.size() + n.getBackLinks().size()) /*/ 2*/ < MIN_KIDS)
				continue;
			in.add(n);
			//String ns = serialToStr(n.getSerial());
			//output children
			//generate kids from bidirectional links: CHILDREN ONLY

			HashSet kids = new HashSet();
			for (Iterator fwd = n.values().iterator(); fwd.hasNext(); ) {
				Link ln = (Link)fwd.next();
				Node c = ln.getChild();
				if (!c.equals(n) && (c.size() + c.getBackLinks().size()) /*/ 2*/ >= MIN_LINKS) {
					in.add(c);
					kids.add(new NodeLink(c, ln.dotType()));
				}
			}
			for (Iterator back = n.backIter(); back.hasNext(); ) {
				Link ln = (Link)back.next();
				Node c = ln.getChild();
				if (!c.equals(n) && (c.size() + c.getBackLinks().size()) /*/ 2*/ >= MIN_LINKS) {
					in.add(c);
					kids.add(new NodeLink(c, ln.dotType()));
				}
			}

			/* changed 2002-07-12 for vertical DOT,  THA

			for (Iterator kit = kids.iterator(); kit.hasNext(); ) {
				NodeLink nlk = (NodeLink)kit.next();
				//in.add(k);
				String ks = serialToStr(nlk.n.getSerial());
				dot.print(ns + " -> " + ks);
				if (lineStyles)
					dot.print("[style=\"" + nlk.s + "\"]");
				dot.print(" ; ");
			}

			*/
			if (!kids.isEmpty())
				outputSet.add(new NodeNodeLinkSet(n, kids));

		}

		Collections.sort(outputSet); //order by increasing node costs

		double lastCost = 0;
		if (outputSet.size() > 0)
			lastCost = ((NodeNodeLinkSet)outputSet.get(0)).n.getDCost();

		//OUTPUT EDGES
		for (Iterator nit = outputSet.iterator(); nit.hasNext(); ) {
			NodeNodeLinkSet nnls = (NodeNodeLinkSet)nit.next();
			String ns = serialToStr(nnls.n.getSerial());
			if (vert && nnls.n.getDCost() > lastCost + VERT_EPSILON) {
				dot.print('\n');
				lastCost = nnls.n.getDCost();
			}
			for (Iterator kit = nnls.kids.iterator(); kit.hasNext(); ) {
				NodeLink nlk = (NodeLink)kit.next();
				String ks = serialToStr(nlk.n.getSerial());
				dot.print(ns + " -> " + ks);
				if (lineStyles)
//			dot.print("[style=\"" + nlk.s + "\"]");
					dot.print(nlk.s);
				dot.print("; ");
			}
		}

//	System.err.println("Outputting " + in.size() + " nodes");

		//OUTPUT NODE LABELS
		for(Iterator nodes = in.iterator(); nodes.hasNext(); ) {
			Node n = (Node)nodes.next();
			String ns = serialToStr(n.getSerial());
			dot.print("\n  " + ns + " [label=\"" + n.getKey() + "\"];");
		}


		//output tail
		dot.println("\n}");
//	dot.close();
	}
	
	public static String serialToStr(int serial) {
		String s = "";
		while (serial > 0) {
			s = s + (char)('a' + serial % 26);
			serial /= 26;
		}
		return s;
	}
}
