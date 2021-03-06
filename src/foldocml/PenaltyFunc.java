package foldocml;

import hashgraph.*;


/**
 * A Superclass for Penalty Functions - to return a penalty multiplier to the
 * distance result for common nodes, based on the nodes that are disjoint
 * <p>
 * penalty (GLOBAL) and penaltyFinal (LOCAL) should return a number in [0, 1]
 * to multiply a base similarity by to reduce the similarity
 * </p>
 * <p>
 * The base class uses an heuristic based only on the cardinality of the
 * disjoint/common sets
 * </p>
 * @author Trent Apted
 * @version 0.4
 * @see Connection#ratioSimilar1(HashGraph g1, HashGraph g2, PrintStream log)
 */
public class PenaltyFunc {
    /** Use only the numbers of nodes common/uncommon */
    public static final short MODE_GLOBAL = 1;
    /** Use individual nodes' information */
    public static final short MODE_LOCAL = 2;
    /** MODE_GLOBAL or MODE_LOCAL */
    public short mode;

    public PenaltyFunc() {
        mode = MODE_GLOBAL;
    }

    public PenaltyFunc(short mode) {
        this.mode = mode;
    }

    /*
     * These must be overridden for the appropriate mode
     * Default provided only for this one.
     */
    /**
     * For MODE_GLOBAL - don't use any information from individual
     * nodes not in the graphs
     */
    double penalty(int common, int leftOnly, int rightOnly) {
        final double BIAS_CONTRIB = 0.5;
        double uncommon = leftOnly + rightOnly;
        /* give a bias is leftOnly or rightOnly is very small
           compared to the other */
        double baseBias = 1 + ((leftOnly > rightOnly) ? leftOnly : rightOnly);
        double minBias = 1 + ((leftOnly < rightOnly) ? leftOnly : rightOnly);
        double bias = (1 - (minBias / baseBias)) * BIAS_CONTRIB;
        double offset = 0.5;
        return (1 - BIAS_CONTRIB)*(1 - (uncommon / (uncommon + common * 2)) + offset) + bias;
    }

    /**
     * for MODE_LOCAL - collect information from each node and how it
     * might fit in the other graph
     */
    public void penaltyStep(Node n, HashGraph other) {
        //eg: find shortest distance from n to any node in other
        //using original graph and accumulate
        //-> needs to keep a reference to the original graph
    }

    /**
     * for MODE_LOCAL - process information gathered from penaltyStep
     * to give a final penalty
     */
    public double penaltyFinal(int common, int leftOnly, int rightOnly) {
        //something based on the information generated by penaltyStep
        return penalty(common, leftOnly, rightOnly);
    }
}

