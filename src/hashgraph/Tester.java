package hashgraph;

import java.io.*;

/**
 * Tester for progressive int encoding
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class Tester {

    public Tester() {
    }
    public static void main(String[] args) throws Exception {
//        Tester tester1 = new Tester();
        ObjectOutputStream oos = new ObjectOutputStream(
          new BufferedOutputStream(
            new FileOutputStream("PROG_TEST.DAT")));

        HashGraph.progWriteInt(oos, 0);
        HashGraph.progWriteInt(oos, 1);
        HashGraph.progWriteInt(oos, 63);
        HashGraph.progWriteInt(oos, 127);
        HashGraph.progWriteInt(oos, 255);
        HashGraph.progWriteInt(oos, 256);
        HashGraph.progWriteInt(oos, 5000);
        HashGraph.progWriteInt(oos, 32000);
        HashGraph.progWriteInt(oos, 32766);
        HashGraph.progWriteInt(oos, 64000);
        HashGraph.progWriteInt(oos, 100000);
        HashGraph.progWriteInt(oos, 1000000);
        HashGraph.progWriteInt(oos, 10000000);
        HashGraph.progWriteInt(oos, 100000000);
        HashGraph.progWriteInt(oos, 1000000000);

        oos.close();

        ObjectInputStream ois = new ObjectInputStream(
          new BufferedInputStream(
            new FileInputStream("PROG_TEST.DAT")));

        while (true) {
            System.out.println(HashGraph.progReadInt(ois));
        }

    }
}