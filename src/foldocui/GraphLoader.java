package foldocui;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import hashgraph.*;

/**
 * Load a graph using a progress bar in a GUI progress window
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class GraphLoader extends JDialog implements Poster {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jpnlLoading = new JPanel();
    private JLabel jlblLoading = new JLabel();
    private JProgressBar jpbProg = new JProgressBar();
    private JLabel jlblPercent = new JLabel();

    public GraphLoader(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            pack();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public GraphLoader() {
        this(null, "Loading graph...", true);
    }
    void jbInit() throws Exception {
        jpnlLoading.setLayout(borderLayout1);
        this.setResizable(false);
        this.setModal(true);
        jlblLoading.setToolTipText("");
        jlblLoading.setHorizontalAlignment(SwingConstants.CENTER);
        jlblLoading.setHorizontalTextPosition(SwingConstants.LEADING);
        jlblLoading.setText("Loading Graph...");
        jpbProg.setValue(0);
        jlblPercent.setHorizontalAlignment(SwingConstants.CENTER);
        jlblPercent.setHorizontalTextPosition(SwingConstants.LEADING);
        jlblPercent.setText("0%");
        getContentPane().add(jpnlLoading);
        jpnlLoading.add(jpbProg, BorderLayout.CENTER);
        jpnlLoading.add(jlblPercent, BorderLayout.SOUTH);
        jpnlLoading.add(jlblLoading, BorderLayout.NORTH);
    }
    public void openGraph(File file, Interface destination) /*throws IOException, ClassNotFoundException*/ {
        dest = destination;
        graphFile = file;
        curThread = new GraphOpener();
        //sleeper = new Sleeper(curThread, 100);
        curThread.start();
        this.show();
    }
    public void post(double d) {
        final java.text.DecimalFormat format = new java.text.DecimalFormat("0%");
        jlblPercent.setText(format.format(d));
        int last = jpbProg.getValue();
        jpbProg.setValue((int)(d * jpbProg.getMaximum()));
        if (jpbProg.getValue() > last) {
            try {
                Thread.sleep(10);
                Thread.yield();
            } catch (InterruptedException ie) {}
        }
    }

    Interface dest;
    HashGraph graph;
    File graphFile;
    GraphOpener curThread;
    Sleeper sleeper;
    private BorderLayout borderLayout1 = new BorderLayout();

    class Sleeper extends Thread {
        Thread child;
        long time;
        public Sleeper (Thread kid, long millis) {
            child = kid;
            time = millis;
            child.start();
            start();
        }
        public void run() {
            try {
                while (child.isAlive()) {
                    Thread.sleep(1);
                    sleep(time);
                }
            } catch (InterruptedException ie) {}
        }
    }
    class GraphOpener extends Thread {
        public void run() {
            try {
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                                                      new FileInputStream(graphFile)));
                graph = new HashGraph();
                graph.readData(ois, GraphLoader.this);
                dest.addGraph(graphFile.getAbsolutePath(), graph);
                GraphLoader.this.hide();
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
    }
}
