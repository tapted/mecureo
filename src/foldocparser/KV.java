package foldocparser;

/**
 * A key, value pair
 */
public class KV {
    public int key, val;
    double lval;
    public KV(int k, int v) {
            key = k;
            val = v;
            lval = Math.log(v);
    }
}

