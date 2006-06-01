package foldocml;
import foldocparser.Normalizer;
import hashgraph.*;
import java.util.*;

/**
 * FOLDOC Machine Learning Comparison class - to generate and contain the
 * results of a compare algorithm
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 * @see Connection#ratioSimilar1(HashGraph g1, HashGraph g2, PrintStream log)
 */
public class Similarity {
    /** The number of nodes in left but not in right */
    public int nodesLeft;
    /** The number of nodes in right but not in left */
    public int nodesRight;
    /** The number of nodes in both left and right */
    public int nodesCommon;
    /** The result of the common similarity using <code>dist</code> */
    public double commonSim;
    /** The result of the disjoint penalty using <code>pen</code> */
    public double penalty;
    /** The final similarity result; combining <code>dist</code> and <code>pen</code> */
    public double similar;

    /** Distance function being used; for common nodes */
    DistanceFunc dist;
    /** Penalty function being used; for disjoint nodes */
    PenaltyFunc pen;

    /**
     * Create a Similarity with base logic using <code>dist</code> and <code>pen</code>
     * @param dist the distance function to use [default: new DiffDistance()]
     * @param pen the penalty function to use [default: new PenaltyFunc()]
     */
    public Similarity(DistanceFunc dist, PenaltyFunc pen) {
        this.dist = dist;
        this.pen = pen;
    }

    /**
     * Create a Similarity using the defaults
     * @see #Similarity(DistanceFunc dist, PenaltyFunc pen)
     */
    public Similarity() {
        this(new DiffDistance(), new PenaltyFunc());
    }

    /**
     * Compare <code>left</code> to <code>right</code> using this Simliarity
     * function.
     * <p>
     * The detailed results of the comparison are loaded into the public data
     * members/fields of this class
     * </p>
     * @param left the 'left' model
     * @param right the 'right' model
     * @return the final comparison result
     */
    public double compare(HashGraph left, HashGraph right) {
        /** First the graphs need to be normalised */
        Normalizer norm;
        try {
            norm = new Normalizer(left);
            norm.normalize(left);
        } catch (IllegalArgumentException iae) {
            System.err.println("left couldn't be normalised; igoring");
        }
        try {
            norm = new Normalizer(right);
            norm.normalize(right);
        } catch (IllegalArgumentException iae) {
            System.err.println("right couldn't be normalised; igoring");
        }


        ArrayList commonLeft = new ArrayList(),
                  commonRight = new ArrayList(),
                  leftOnly = new ArrayList(),
                  rightOnly = new ArrayList();

        //indexes in commonLeft and commonRight need to match
        /* First merge the graphs */
        HashGraph merged = left.costMerge(right, 0);

        /* Now classify the _original_ nodes, based on information in the merged graph */
        for (Iterator mit = merged.nodeIterator(); mit.hasNext(); ) {
            Node m = (Node)mit.next();
            if (m.temp == HashGraph.MERGE_COMMON) {
                commonLeft.add(left.get(m.getKey()));
                commonRight.add(right.get(m.getKey()));
            } else if (m.temp == HashGraph.MERGE_THIS) {
                leftOnly.add(left.get(m.getKey()));
            } else if (m.temp == HashGraph.MERGE_OTHER) {
                rightOnly.add(right.get(m.getKey()));
            }
        }

        nodesCommon = commonLeft.size();
        nodesLeft = leftOnly.size();
        nodesRight = rightOnly.size();

        /* Now call the distance function for common nodes */
        /* costs are normalised, so maximum distance is 1.0 */
        commonSim = 0;
        for (int i = 0; i < nodesCommon; ++i) {
            commonSim += (1.0 - dist.dist((Node)commonLeft.get(i), (Node)commonRight.get(i))) / nodesCommon;
        }
        //for example, if the common parts of the graphs are exactly the same,
        //commonSim will now be == 1.0

        /* Now penalise the similarity based on the uncommon parts */
        if (pen.mode == PenaltyFunc.MODE_LOCAL) {
            for (Iterator lit = leftOnly.iterator(); lit.hasNext(); )
                pen.penaltyStep((Node)lit.next(), right);
            for (Iterator rit = rightOnly.iterator(); rit.hasNext(); )
                pen.penaltyStep((Node)rit.next(), left);
            penalty = pen.penaltyFinal(nodesCommon, nodesLeft, nodesRight);
        } else {
            penalty = pen.penalty(nodesCommon, nodesLeft, nodesRight);
        }

        similar = commonSim * penalty;
        return similar;

    }
}
