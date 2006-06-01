package foldocml;
import hashgraph.*;

/**
 * In Development: a Similarity function to attempt a weighting on minimum spanning tree similarity
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class WeightedSpanSimilar extends Similarity {

    HashGraph ont;

    public WeightedSpanSimilar(HashGraph ontology) {
        ont = ontology;
    }

    /**<pre>
     *  The next three lines after the decs attempt to normalise the ratio of the
     *  weight sum of the minumum weight spanning trees [that aren't quite]
     *  after they have been weighted according to the number of nodes in
     *  each graph. If minSpanWeight() wasn't optimised and returned the true
     *  minimum weight spanning tree it would be normalised between 0 and 1.
     *  As it is it's not quite, but it's rarely out of the range by more than
     *  0.05 or so.
     *  Let x = lsw, y = rsw, z = msw, a = |left|, b = |right|, c = |merged|
     *  then it's derived from the two facts:
     *
     *  (x + y) / 2  <=  z  <=  x + y
     *  and
     *  (a + c) / 2  <=  c  <=  a + c
     *
     *  after forming the weghted ratio [sum] as
     *
     *  cz / (bx + ay)
     *
     *  it can be shown that
     *
     *            cz           ax + by + 1         3(ax + by + 1)
     *  0  <=  ---------  -  ---------------  <=  ----------------
     *          bx + ay       4(bx + ay + 1)       4(bx + ay + 1)
     *
     *  The result is subtracted from 1 because part of the algebra reverses
     *  the inequalities. And although the normalisation doesn't only involve
     *  constants, this seems to work reasonably well in practice.
     *  </pre>
     */
    public double compare(HashGraph left, HashGraph right) {
        super.compare(left, right);
        HashGraph merged = left.merge(right, false);
        merged.copyLinks(ont);

        double lsw = left.minSpanWeight();
        double rsw = right.minSpanWeight();
        double lw = lsw * right.size();     //bx
        double rw = rsw * left.size();      //ay
        double mw = merged.minSpanWeight() * merged.size(); //cz

        double axby = lsw * left.size() + rsw * right.size();
        double bxay = lw + rw;


        double sum = mw / bxay;
        sum -=  axby / (4*bxay) + 0.25;
        sum /= 0.75 * (axby / bxay) + 0.75;

        //System.err.println(merged);
        System.err.println("left.size() = " + left.size());
        System.err.println("left.density() = " + left.density());
        System.err.println("right.size() = " + right.size());
        System.err.println("right.density() = " + right.density());
        System.err.println("merged.size() = " + merged.size());
        System.err.println("merged.density() = " + merged.density());

        System.err.println("\nlsw = " + lsw + "\nrsw = " + rsw + "\nlw = " + lw + "\nrw = " + rw + "\nsum = " + sum + "\nmw = " + mw);

        //avg(lw, rw) < mw < lw + rw
        similar = 1 - sum;
        if (similar < 0)
            similar = 0;
        if (similar > 1)
            similar = 1;
        return similar;
    }

}
