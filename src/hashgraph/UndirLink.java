package hashgraph;

/**
 * Treat a link as undirected. Not used.
 * @author Trent Apted
 * @version 0.4
 */
public class UndirLink implements Comparable {

    Node n1, n2;
    double weight;

    public UndirLink(Link l) {
        /*arbitrary*/
        n1 = l.parent;
        n2 = l.child;
        weight = l.weight;
    }

    public boolean equals(Object o) {
        if (!(o instanceof UndirLink)) return false;
        UndirLink uo = (UndirLink)o;
        return uo.n1.equals(n1) && uo.n2.equals(n2) ||
               uo.n1.equals(n2) && uo.n2.equals(n1);
    }

    public int hashCode() {
        return n1.hashCode() + n2.hashCode();
    }

    public int compareTo(Object o) {
        if (!(o instanceof UndirLink)) return -1;
        UndirLink uo = (UndirLink)o;
        if (weight == uo.weight)
            return 0;
        if (weight > uo.weight)
            return 1;
        return -1;
    }

}