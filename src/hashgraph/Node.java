package hashgraph;

import java.util.*;

/**
 * Represents a node in a HashGraph.
 *<p>
 * It also represents the mapping from nodeKeys to Links for this Node
 *</p>
 * @author Trent Apted
 */
@SuppressWarnings("unchecked")
public class Node extends HashMap implements Comparable, java.io.Serializable {

	/**
	 * versioning for serailsation
	 */
	private static final long serialVersionUID = 1L;

	private static int nextSerial = 1;

	//package variables for dijkstra's algorithm
	/** Dijkstra's : the cost from the source to this node */
	public double cost;
	/** Dijkstra's : the previous node in the shortest path */
	transient Node prev;
	/** Dijkstra's : a temporary variable for flag status */
	public transient int temp;
	/** Pairing Heap + Dijkstra's : for O(1) decreaseKey() */
	transient DataStructures.PairNode noderef;
	/** Dijkstra's : The cost label initially given to a node to denote "unlabelled" */
	static final double INFINITY = Double.MAX_VALUE / 3;

	/** An ID String for use in the outputter for RDF links */
	public String webID = "";

		void clearTemp() {
				clearTemp(INFINITY);
		}

	/** Dijkstra's : Reset temporary variables */
	void clearTemp(double infinity) {
		cost = infinity;
		prev = null;
		noderef = null;
		temp = 0;
	}

	/** This node's dictionary entry */
	private String key;
	/** backward links */
	Map backLinks;
	/** a serial number for RDF */
	private int serial;

	public Iterator backIter() {
		return backLinks == null ? null : backLinks.values().iterator();
	}

	/** Overrides put in HashMap */
	public Object put(Object key, Object value) {
		Link newl = (Link)value;
		Link oldl = (Link)get(key);
		if (oldl == null || newl.weight < oldl.weight)
			return super.put(key, newl);
		else
			return null;
	}

	public Object linkBack(Link l) {
		return backLinks.put(l.parent.key, l);
	}

	/**
	 *
	 * @param
	 * @return
	 */
	public String getKey() {
		return key;
	}

	Node (String word) {
		this(word, true);
	}

	/**
	 * only let HashGraph create them
	 */
	Node (String word, boolean backLink) {
		key = word;
		if (backLink)
			backLinks = new HashMap();
		serial = nextSerial++;
	}

	/**
	 * Returns the hashcode value for this object.
	 *
	 * @return the hashcode for this object.
	 */
	public int hashCode() {
		return key.hashCode();
	}

	/**
	 * Test if two Node objects are equal.
	 *
	 * @param o the object to compare to this Node object
	 * @return true if both are nodes have the same key
	 */
	public boolean equals(Object o) {
		return o instanceof Node && key.equals(((Node)o).key);
	}

	/**
	 * Performs cost comparison
	 *
	 * @returns 0 if equal, -1 if <code>this is less than o </code> or
	 *						1 if <code>this is greater than o</code>
	 */
	public int compareTo(Object o) {
		//return (o instanceof Node) ? key.compareTo(((Node)o).key) : -1;
		if (o instanceof Node) {
			Node n = (Node)o;
			if (cost > n.cost)
				return 1;
			else if (cost == n.cost)
				return 0;
		}
		return -1;
	}

	/**
	 * Returns a string representation of this node
	 */
	public String toString() {
		return "<N>: " + key + "(" + cost + ")";
	}

	public int getSerial() {
		return serial;
	}

	public Map getBackLinks() {
		return backLinks;
	}

	public Node costCopy() {
		Node ret = new Node(key);
		ret.cost = cost;
		ret.temp = 0;
		ret.webID = webID;
		return ret;
	}

	public double getDCost() {
		return cost;
	}

	public void setDCost(double c) {
		cost = c;
	}
}
