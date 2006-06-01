package foldocml;
import java.io.*;
import hashgraph.*;

/**
 * Tester for all similarity functions
 * @author Trent Apted
 * @version 0.4
 */
public class Tester {
    public static void main(String[] args) throws Exception {
        Connection conn = Connection.connect(new File(args[0]));
        HashGraph left = Connection.openGraph(new File(args[1]));
        HashGraph right = Connection.openGraph(new File(args[2]));
        System.out.println("Similar1 = " + conn.ratioSimilar1(left, right, System.err));
//        System.out.println("Similar2 = " + conn.ratioSimilar2(left, right, System.err));
//        System.out.println("Similar3 = " + conn.ratioSimilar3(left, right, System.err));
//        System.out.println("Similar4 = " + conn.ratioSimilar4(left, right, System.err));
        System.out.println("Similar5 = " + conn.ratioSimilar5(left, right, System.err));
        System.out.println("Similar6 = " + conn.ratioSimilar6(left, right, System.err));
    }
}