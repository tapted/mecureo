package hashgraph;

/**
 * Comparator for link costs
 * @author Trent Apted
 * @version 0.4
 */
public class LinkCostCompare implements java.util.Comparator {
    public int compare(Object o1, Object o2) {
        Double d1 = new Double(((Link)o1).weight);
        Double d2 = new Double(((Link)o2).weight);
        return d1.compareTo(d2);
    }
    public boolean equals(Object o1, Object o2) {
        return ((Link)o1).weight == ((Link)o2).weight;
    }
}