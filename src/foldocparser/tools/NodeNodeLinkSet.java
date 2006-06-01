/*
 * Created on 25/08/2003
 *
 * Refactored to seperate package by Andrew Lum
 *
 *
 */
 
package foldocparser.tools;

import hashgraph.*;
import java.util.*;

/**
 * @author Andrew
 *
 */
class NodeNodeLinkSet implements Comparable {
	public Node n;
	public Set kids; //Set of NodeLink s
	public NodeNodeLinkSet(Node n, Set kids) {
		this.n = n;
		this.kids = kids;
	}
	public boolean equals(Object o) {
		return (o instanceof NodeNodeLinkSet && ((NodeNodeLinkSet)o).n.equals(n));
	}
	//compare by NODE COSTS
	public int compareTo(Object o) {
		if (equals(o))
			return 0;
		else if (o instanceof NodeNodeLinkSet &&
				 n.getDCost() > ((NodeNodeLinkSet)o).n.getDCost())
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
