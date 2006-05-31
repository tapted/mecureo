package foldocparser;

//import java.io.*;
//import java.util.zip.*;

/**
 * An obsolete generic tester
 */
public class Tester {

    public static void main(String[] args) throws Exception {
        Parser p = new Parser();
        p.parse();
        System.out.println("Categories");
        java.util.Iterator it = p.categories.values().iterator();
        for (;it.hasNext();)
            System.out.println(it.next());

        System.out.println("Graph Size: " + p.graph.size());
        System.out.println("Graph Density: " + p.graph.density());
        System.out.println(p.graph);
        /*
        int i = 0;
        int defs = 0;
        int cats = 0;
        int nothing = 0;
        int reset = 0;
        int links = 0;
        int kw = 0;
        long start = System.currentTimeMillis();
        try {


            Tokenizer t = new Tokenizer(new BufferedInputStream(
                                        new GZIPInputStream(
                                        new FileInputStream("Dictionary.gz"))));
            System.out.println(t.nextDefinition());
            for (i = 0; i < 1000000; ++i) {
                Token tk = t.nextToken();
                if (tk.getFlags() == Token.EOD) {
                    System.out.println(t.nextDefinition());
                    defs++;
                } else if ((tk.getFlags() & Token.M_ASSOC) == Token.CATEGORY) {
                    System.out.println("\t" + tk.toString());
                    cats++;
                } else if (tk.getFlags() == Token.K_NOTHING) {
                    nothing++;
                    //System.out.print(" ? " + tk.toString());
                } else if (tk.getFlags() == Token.EOS) {
                    System.out.println("\t\t\t/----/");
                    reset++;
                } else if ((tk.getFlags() & Token.M_TYPE) == Token.H_LINK) {
                    System.out.println("\t\t: " + tk.toString());
                    links++;
                } else if ((tk.getFlags() & Token.M_TYPE) == Token.K_WORD) {
                    System.out.println("\t\t\t" + tk.toString());
                    kw++;
                }
            }
        } catch (Exception e) {
            System.out.println("\nTotal Tokens processed = " + i);
            System.out.println("Definitions = " + defs);
            System.out.println("Category links = " + cats);
            System.out.println("Plus other links = " + links);
            System.out.println("Keywords processed = " + kw);
            System.out.println("Times keyword tracking reset = " + reset);
            System.out.println("Tokens ignored = " + nothing);
            System.out.println("Completed in " + (System.currentTimeMillis() - start) + " ms");
            throw e;
        }
        */
    }

}