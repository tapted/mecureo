package foldocml;

import hashgraph.*;

/**
 * A Penalty function that uses the original ontology to
 * determine 'closeness' of any node not in one of the
 * graphs being compared to the other graph
 * <p>The base class simply uses the shortest path in the ontology</p>
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 * @see Connection#ratioSimilar2(HashGraph g1, HashGraph g2, PrintStream log)
 */
public class SearchPenalty extends PenaltyFunc {
    /** The ontology within which to search */
    HashGraph ont;
    /** The maximum search distance */
    double maxDist;
    /** The current sum of the penalties */
    double penaltySum = 0.0;
    /** The penalty when no path exists */
    double infinity;
    /** The last distance added to penaltySum [for subclass reuse] */
    protected double lastDist;

    /**
     * maxDistance can be specified for efficiency to limit the
     * depth of the search for each penalty
     */
    public SearchPenalty(HashGraph ontology,
                         double infinity,     //the penalty when there is no path
                         double maxDistance) {
        super(MODE_LOCAL);
        ont = ontology;
        this.infinity = infinity;
        maxDist = maxDistance;
    }

    public SearchPenalty(HashGraph ontology, double infinity) {
        //it makes sense to use the same value
        //if maxDistance > infinity it's possible for node _with_ a path
        //to be penalised harder than those without. Bad.
        this(ontology, infinity, infinity);
    }

    public void reset() {
        penaltySum = 0.0;
    }

    /**
     * Accumulated penaltyies calcualted by finding the minimum
     * distance from n to any node in other using the oringial
     * ontology (or 'infinity' if there is no path)
     */
    public void penaltyStep(Node n, HashGraph other) {

//        System.err.println("penaltyStep(" + n + ", " + System.identityHashCode(other) + ") [lastDist = " + lastDist + ", penaltySum = " + penaltySum + "]");
        /*
         * find the closest node in other to n using ont
         */
        lastDist = ont.minDistance(n, other, maxDist);
        if (lastDist < 0) { //no path
            lastDist = infinity;
            penaltySum += infinity;
        } else {
            penaltySum += lastDist;
        }
    }

    /**
     * for MODE_LOCAL - process information gathered from penaltyStep
     * to give a final penalty
     */
    public double penaltyFinal(int common, int leftOnly, int rightOnly) {
        return penaltySum;
    }
}