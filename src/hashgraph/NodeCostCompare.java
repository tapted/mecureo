package hashgraph;

/**
 * Comparator for node costs
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class NodeCostCompare implements java.util.Comparator {
    public int compare(Object o1, Object o2) {
        Double d1 = new Double(((Node)o1).cost);
        Double d2 = new Double(((Node)o2).cost);
        return d1.compareTo(d2);
    }
    public boolean equals(Object o1, Object o2) {
        return ((Node)o1).cost == ((Node)o2).cost;
    }
}