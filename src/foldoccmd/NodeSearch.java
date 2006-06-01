package foldoccmd;
import java.io.*;
import java.util.*;
import porterstemmer.*;

/**
 * <p>Command line cgi program for displaying <b><i>Query match possibilities</b></i></p>
 * @author Trent Apted
 * @version 0.4
 */
public class NodeSearch {
    /**
     * Usage: java foldoccmd.NodeSearch wordList queryList [depth = 1.0] [minKids = 0] > htmlFile
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java foldoccmd.NodeSearch wordList queryList [depth = 1.0] [minKids = 0] > htmlFile");
            return;
        }
        ArrayList nodes = new ArrayList();
        int minKids = 0;
        double depth = 1.0;

        int nonString;
        //parse arguments

        for (nonString = 1; nonString < args.length; ++nonString) {
            try { /*java sucks*/
                Double.parseDouble(args[nonString]);
                break;
            } catch (NumberFormatException nfe) {
                nodes.add(args[nonString]);
            }
        }

        if (nonString + 1 < args.length) try {
            minKids = Integer.parseInt(args[nonString + 1]);
        } catch (NumberFormatException nfe) {
            System.err.println("Couldn't parse minKids, using 0");
        }
        if (nonString < args.length) try {
            depth = Double.parseDouble(args[nonString]);
        } catch (NumberFormatException nfe) {
            System.err.println("Couldn't parse depth, using 1.0");
        }

        //output HTML
        System.out.println("<html><head><title>Choose Nodes</title></head><body bgcolor=\"#FFFFFF\"><pre>");
        Indexer idx;
        try {
            System.out.print("Loading " + args[0] + "...\n");
            System.out.flush();
            idx = new Indexer(new File(args[0]));
            System.out.println(" loaded.");
        } catch (IOException ioe) {
            System.out.println(ioe);
            ioe.printStackTrace(System.out);
            System.out.println("</pre></body></html>");
            System.out.flush();
            throw ioe;
        }
        System.out.println("</pre>");
        System.out.println("<p>Select the nodes you wish to appear in the output graph and click 'Generate Graph' to generate the gif file<br>");
        System.out.println("The numbers in brackets are an indication of the accuracy of the match (lower is better)</p>");

 
        boolean rdf = false, txt = false, vlum = false, owl = false;
        for (++nonString; nonString < args.length; ++nonString) {
        	System.out.println("<!-- BLAH BLAH " + args[nonString] + "-->");
            if (args[nonString].equals("rdf"))
                rdf = true;
			if (args[nonString].equals("owl"))
				owl = true;
            if (args[nonString].equals("txt"))
                txt = true;
            if (args[nonString].equals("vlum"))
            	vlum = true;
        }

		System.out.print("<form name=\"ChooseWords\" method=\"get\" action=\""); /*quickdot.cgi\">");*/

        /** TODO: make this more flexible */
        String prepend = "";
        if (!args[0].endsWith(".txt"))
            prepend = args[0].substring(args[0].length() - 8, args[0].length() - 1);

        if (rdf && txt)
            System.out.println(prepend + "rdftext.cgi\">");
        else if (rdf && vlum)
            System.out.println(prepend + "rdfvlum.cgi\">");
        else if (rdf)
            System.out.println(prepend + "quickrdf.cgi\">");
        else if (txt)
            System.out.println(prepend + "dottext.cgi\">");
		else if (owl)
			System.out.println(prepend + "quickowl.cgi\">");
        else
            System.out.println(prepend + "quickdot.cgi\">");
        
        if (prepend.length() != 0) {
        	System.out.println("Ontology:<blockquote>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/usability.fdg\">Matched (stems)<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/udetectont.fdg\">Matched with Link Detection<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/umatchdetect.fdg\">Matched with <i>matched</i> Link Detection<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/unlpdetect.fdg\">Matched with <b>NLP</b> Link Detection<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/unlpcasedetect.fdg\" checked>Matched with <i>case-matched <b>NLP</b></i> Link Detection<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/unlpstemdetect.fdg\">Matched with <i>stem-matched <b>NLP</b></i> Link Detection<br>");
                System.out.println("</blockquote>");
        } else {
        	System.out.println("Ontology:<blockquote>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/matchedont.fdg\">Matched (stems, no detection)<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/submatchedont.fdg\">Substring matched<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/detectedont.fdg\">Matched with Link Detection 1 (exact)<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/insense.fdg\">Matched with Link Detection 3 (case insensitive)<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/stemmed.fdg\">Matched with Link Detection 4 (component stems)<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/dftag.fdg\">Matched with <b>NLP</b> Link Detection<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/fcasetag.fdg\" checked>Matched with <i>case-matched <b>NLP</b></i> Link Detection<br>");
        	System.out.println("  <input type=\"radio\" name=\"ontology\" value=\"/usr/hons2001/alum/lib/html/demos/mecureo/fdg/fmatchtag.fdg\">Matched with <i>stem-matched <b>NLP</b></i> Link Detection<br>");
                System.out.println("</blockquote>");
		}        	
        	

//        System.out.println("<input type=\"hidden\" name=\"list\" value=\"" + args[0] + "\">");
        System.out.println("  <table width=\"100%\" border=\"0\">");
        int row = 1;
        for (Iterator it = nodes.iterator(); it.hasNext(); ) {
            int col = 1;
            String so = (String)it.next();
            System.out.print("    <tr><th NOWRAP>" + so);
            SDPair[] arr = idx.match(so);
            if (arr.length == 0) {
                System.out.println(" (deep)</th>");
                ArrayList more = idx.lookHarder(so);
                for (Iterator mit = more.iterator(); mit.hasNext(); ) {
                    SDPair sdp = (SDPair)mit.next();
                    System.out.print("      <td NOWRAP><input type=\"checkbox\" name=\"w" +
                                     row + "." + col + "\" value=\"&quot;" + sdp.str + "&quot;\" ");
                    if (col == 1)
                        System.out.println("checked>");
                    else
                        System.out.println(">");
                    System.out.println(sdp.str + " (" + sdp.dist + ")</td>");
                    ++col;
                }
            } else {
                 System.out.println("</th>");
                 for (int i = 0; i < arr.length; ++i) {
                     System.out.print("      <td NOWRAP><input type=\"checkbox\" name=\"w" +
                                      row + "." + col + "\" value=\"&quot;" + arr[i].str + "&quot;\" ");
                     if (col == 1)
                         System.out.println("checked>");
                     else
                         System.out.println(">");
                     System.out.println(arr[i].str + " (" + arr[i].dist + ")</td>");
                     ++col;
                 }
            }
            System.out.println("    </tr>");
            ++row;
        }
        System.out.println("</table>");
        System.out.println("<p>\n  <input type=\"text\" name=\"distance\" value=\"" + depth + "\">");
        System.out.println("Distance / Nodes / Depth <br>");
        System.out.println("<input type=\"text\" name=\"peerage\" value=\"" + minKids + "\">");
        System.out.println("Minimum Peerage</p>\n<p>Grow Mode (using Distance above):\n<blockquote>");
        System.out.println("<input type=\"radio\" name=\"mode\" value=\"dist\">Weighted Distance<br>");
        System.out.println("<input type=\"radio\" name=\"mode\" value=\"num\" checked>Node Number<br>");
        System.out.println("<input type=\"radio\" name=\"mode\" value=\"depth\">Depth</blockquote></p>");
        System.out.println("<input type=\"submit\" name=\"Submit\" value=\"Generate Model\">\n</p>");
        
        
        System.out.println("</form></body></html>");
    }
}
