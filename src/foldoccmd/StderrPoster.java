package foldoccmd;

/**
 * Provides a progress indicator to standard error output. Implements interface
 * in <code>foldocui.Poster</code>
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class StderrPoster implements foldocui.Poster {
    private final java.text.DecimalFormat format = new java.text.DecimalFormat("0%");

    private double last = 0;
    private double interval = 0.1;

    /**
     * Print a message every <code>interval*100%</code> of progress
     * @param interval the interval to use
     */
    public StderrPoster(double interval) {
        this.interval = interval;
    }

    /**
     * Use the default interval of 0.1 (every 10%)
     * @see #StderrPoster(double interval)
     */
    public StderrPoster() {}

    /**
     * Called regularly by the HashGraph loader to post progress
     * @param d the current progress: a real in [0, 1]
     * @see hashgraph.HashGraph#readData(java.io.ObjectInputStream in, foldocui.Poster post)
     */
    public void post(double d) {
        if (d > last) {
            while (last < d)
                last += interval;
            System.err.print(format.format(last) + (last < 0.95 ? "...\n" : "\n"));
        }
    }
}