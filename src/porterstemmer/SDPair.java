package porterstemmer;

/**
 * A &lt;byte, String&gt; pair =&gt; &lt;Distance, Match&gt;
 * @author Trent Apted
 * @version 0.2
 */
public class SDPair implements Comparable {
    public byte dist;
    public String str;
    public SDPair (byte dist, String s) {
        this.dist = dist;
        str = s;
    }
    public int compareTo(Object o) {
        SDPair right = (SDPair)o;
        if (dist < right.dist)
            return -1;
        else if (dist == right.dist)
            return 0;
        else
            return 1;
    }
}
