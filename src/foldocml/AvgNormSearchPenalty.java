package foldocml;

import hashgraph.*;

/**
 * This <code>SearchPenalty</code> averages and normalises the penalties
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 * @see Connection#ratioSimilar4(HashGraph g1, HashGraph g2, PrintStream log)
 */
public class AvgNormSearchPenalty extends NormalisedSearchPenalty {
    public AvgNormSearchPenalty(HashGraph ontology,
                                 double infinity,     //the penalty when there is no path
                                 double maxDistance) {
        super(ontology, infinity, maxDistance);
    }

    public AvgNormSearchPenalty(HashGraph ontology, double infinity) {
        super(ontology, infinity);
    }

    /**
     * Average the penalty over the total number of nodes not common
     * will return the averaged normalised sum (a value between 0 and 1)
     */
    public double penaltyFinal(int common, int leftOnly, int rightOnly) {
        double den = leftOnly + rightOnly;
        if (den == 0)
            return 1;
        double num = super.penaltyFinal(common, leftOnly, rightOnly);
        return 1 - (num / (den + common));
    }
}