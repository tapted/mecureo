package foldocml;

import hashgraph.*;

/**
 * A Similarity class based on the Minimum Spanning Tree similiarity
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 * @see Connection#ratioSimilar6(HashGraph g1, HashGraph g2, PrintStream log)
 */
public class SpanSimilar extends Similarity {

    HashGraph ont;

    public SpanSimilar(HashGraph ontology) {
        ont = ontology;
    }

    public double compare(HashGraph left, HashGraph right) {
        super.compare(left, right);
        HashGraph merged = left.merge(right, false);
        merged.copyLinks(ont);
        double sum = left.minSpanWeight() + right.minSpanWeight();
        double mw = merged.minSpanWeight();

        //avg(lw, rw) < mw < lw + rw

        similar = ((sum / 2) / mw  -  0.5) * 2.0;
        return similar;
    }
}