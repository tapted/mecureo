package foldocparser;

import hashgraph.*;

/**
 * A class to provide on-the-fly normalization of graph node weights
 */
public class Normalizer {
    //anything above this will become zero
    static final double EFFECTIVE_INFINITY = 10000;
    double max = 0;
    double min = EFFECTIVE_INFINITY;
    double mult = 0;
    double diffMult = 0;
    double normMax;

    public static final Normalizer NULL_NORMALIZER = new Normalizer();

    private Normalizer() {
    }

    
    public Normalizer (HashGraph g) {
        this(g, 1);
    }
    public Normalizer (HashGraph g, double normMaximum) {
        normMax = normMaximum;
        for (java.util.Iterator it = g.nodeIterator(); it.hasNext();) {
            double d = ((Node)it.next()).getDCost();
            if (d < EFFECTIVE_INFINITY && d > max)
                max = d;
            if (d < min)
                min = d;
        }
        if (max <= min)
            throw new IllegalArgumentException("Could not normalize g");
        mult = normMax/(max - min);
        diffMult = normMax / max;
        max -= min;
    }
    public double norm(double d) {
        double r = (max - (d - min)) * mult;
        return r < 0 ? 0 : r;
    }
    public double diffNorm(double d) {
        double r = diffMult * d;
        return r < 0 ? 0 : r;
    }
    public void normalize (HashGraph g) {
        for (java.util.Iterator it = g.nodeIterator(); it.hasNext();) {
            Node n = (Node)it.next();
            n.setDCost(normMax - norm(n.getDCost()));
        }
    }


}