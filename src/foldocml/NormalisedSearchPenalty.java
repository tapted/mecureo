package foldocml;

import java.util.*;
import hashgraph.*;

/**
 * This <code>SearchPenalty</code> normalises each shortest distance over the maximum and all disjoint nodes
 */
public class NormalisedSearchPenalty extends SearchPenalty {

    ArrayList costs = new ArrayList(); //of `Double's

    public NormalisedSearchPenalty(HashGraph ontology,
                                   double infinity,     //the penalty when there is no path
                                   double maxDistance) {
        super(ontology, infinity, maxDistance);
    }

    public NormalisedSearchPenalty(HashGraph ontology, double infinity) {
        super(ontology, infinity);
    }


    public void penaltyStep(Node n, HashGraph other) {
        super.penaltyStep(n, other);
        costs.add(new Double(lastDist));
    }

    /**
     * Normalise the penalty over the total number of nodes not common
     * Normalise each penalty, and return the average.
     * Will return the normalised sum
     */
    public double penaltyFinal(int common, int leftOnly, int rightOnly) {
        //normalise

        double max, min, mult, tmp1, /*tmp2,*/ sum;
        int sz = costs.size();
        if (sz == 0) {
            return 0.0;
        }
        if (sz == 1) {
            return 1.0 - 1.0 / ((Double)costs.get(0)).doubleValue();
        }

        /*
        //find min and max (do it efficiently)
        Iterator cit = costs.iterator();
        min = ((Double)cit.next()).doubleValue();
        max = ((Double)cit.next()).doubleValue();
        if (max < min) {
            tmp1 = min;
            min = max;
            max = tmp1;
        }

        if (sz % 2 == 1) { //odd
            tmp1 = ((Double)cit.next()).doubleValue();
            if (tmp1 < min) {
                min = tmp1;
            } else if (tmp1 > max) {
                max = tmp1;
            }
        }

        for ( ; cit.hasNext(); ) {
            //we can do this, because there is always an even number left
            tmp1 = ((Double)cit.next()).doubleValue();
            tmp2 = ((Double)cit.next()).doubleValue();
            if (tmp1 < tmp2) {
                if (tmp1 < min)
                    min = tmp1;
                if (tmp2 > max)
                    max = tmp2;
            } else {
                if (tmp2 < min)
                    min = tmp2;
                if (tmp1 > max)
                    max = tmp1;
            }
            }
            */
        /*
         * THIS IS WRONG!! min should always be zero - I'll get rid of the overhead later
         *
         mult = 1.0 / (max - min);
         */
        max = infinity;
        mult = 1.0 / max;
        min = 0;
        /*end changes */

        sum = 0.0;

        //adjust values
        for (int i = 0; i < sz; ++i) {
            tmp1 = (((Double)costs.get(i)).doubleValue() - min) * mult;
            costs.set(i, new Double(tmp1));
            sum += tmp1;
        }

        penaltySum = sum;

        return penaltySum;
    }
}