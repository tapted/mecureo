package foldocml;

import hashgraph.Node;

/**
 * An interface for distance functions for nodes that are <i>common</i> between
 * two models being compared
 * @author Trent Apted
 * @version 0.4
 */
public interface DistanceFunc {
    /**
     * Must return a value in the range [0, 1.0]
     */
    double dist(Node left, Node right);
}

