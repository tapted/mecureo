package foldocml;

import hashgraph.Node;

/**
 * Distance function returning difference in <b><i>node</b></i> &nbsp;weights
 * @author Trent Apted
 * @version 0.4
 * @see Connection#ratioSimilar1(HashGraph g1, HashGraph g2, PrintStream log)
 */
public class DiffDistance implements DistanceFunc {
    public double dist(Node left, Node right) {
        double val = left.getDCost() - right.getDCost();
        return val < 0 ? -val : val;
    }
}

