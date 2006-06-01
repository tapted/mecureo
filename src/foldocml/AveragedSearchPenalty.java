package foldocml;

import hashgraph.*;

/**
 * This <code>SearchPenalty</code> averages the penalty over the
 * number of nodes disjoint
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 * @see Connection#ratioSimilar3(HashGraph g1, HashGraph g2, PrintStream log)
 */
public class AveragedSearchPenalty extends SearchPenalty {
    public AveragedSearchPenalty(HashGraph ontology,
                                 double infinity,     //the penalty when there is no path
                                 double maxDistance) {
        super(ontology, infinity, maxDistance);
    }

    public AveragedSearchPenalty(HashGraph ontology, double infinity) {
        super(ontology, infinity);
    }

    /**
     * Average the penalty over the total number of nodes not common
     */
    public double penaltyFinal(int common, int leftOnly, int rightOnly) {
        return penaltySum / (leftOnly + rightOnly + 1);
    }
}