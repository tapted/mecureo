package hashgraph;

import java.util.*;

/**
 * A Path in a HashGraph<p>
 * Description: Represents a path through a HashGraph. Implementation is fail-fast.<p>
 *              This tends to take up a lot of memory.
 * @author Trent Apted
 * @version 0.9
 */
public class Path {
    /** the path */
    private LinkedList path;
    /** the path cost */
    private double cost;
    /** the network the path is contained within */
    private HashGraph backing;
    /** the modCount of the network when the path was created */
    private int initState;

    /**
     * package private: traverse shortest path found from source to dest.
     */
    Path(Node source, Node dest, HashGraph backing) {
        this.backing = backing;
        initState = backing.modCount;
        cost = dest.cost;
        Node n = dest;
        path = new LinkedList();
        while(n != null) {
            path.addFirst(n);
            n = n.prev;
        }
    }

    /**
     * check if the underlying network has been changed
     */
    private void check() {
        if (initState != backing.modCount)
            throw new ConcurrentModificationException("The backing network has been changed");
    }

    /**
     * get a List of nodes along this path
     */
    public List getPath() {
        check();
        return path;
    }

    /**
     * get the summed weight of the links in this path
     */
    public double getCost() {
        check();
        return cost;
    }

    /**
     * Return a String list of the nodes in this path, in order
     */
    public String toString() {
        return "\n" + path.toString();
    }
}