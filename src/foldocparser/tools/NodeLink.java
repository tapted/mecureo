/*
 * Created on 25/08/2003
 *
 * Refactored to seperate package by Andrew Lum
 * 
 */
package foldocparser.tools;

import hashgraph.*;


/**
 * @author Trent Apted
 *
 */
class NodeLink implements Comparable {
	public Node n;
	public String s;
	public NodeLink(Node n, String s) {
		this.n = n;
		this.s = s;
	}
	public boolean equals(Object o) {
		return (o instanceof NodeLink && ((NodeLink)o).n.equals(n));
	}
	//compare by NODE COSTS
	public int compareTo(Object o) {
		if (equals(o))
			return 0;
		else if (o instanceof NodeLink && n.getDCost() > ((NodeLink)o).n.getDCost())
			return 1;
		else
			return -1;
	}
	//to test ordering
	public String toString() {
		return "" + n.getDCost();
	}
	public int hashCode() {
		return n.hashCode();
	}
}

