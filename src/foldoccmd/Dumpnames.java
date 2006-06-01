package foldoccmd;
import foldocml.*;
import hashgraph.*;
import java.util.Iterator;

/**
 * <p>Merely lists the node names contained in the fdg graph specified
 *    by the first argument to stdout</p>
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class Dumpnames {
    public static void main(String[] args) throws Exception {
        HashGraph g = Connection.openGraph(new java.io.File(args[0]));
        for (Iterator it = g.nodeIterator(); it.hasNext(); )
            System.out.println(((Node)it.next()).getKey());
    }
}