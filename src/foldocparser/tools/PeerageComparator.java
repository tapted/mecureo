/*
 * Created on 25/08/2003
 *
 * Refactored to seperate package by Andrew Lum
 *
 *
 */
package foldocparser.tools;

import hashgraph.*;

/**
 * @author Trent Apted
 *
 */
class PeerageComparator implements java.util.Comparator {
	public int compare(Object o1, Object o2) {
		Integer i1 = new Integer(((Node)o1).size());
		Integer i2 = new Integer(((Node)o2).size());
		return i2.compareTo(i1);
	}
	public boolean equals(Object o1, Object o2) {
		return ((Node)o1).size() == ((Node)o2).size();
	}
}
