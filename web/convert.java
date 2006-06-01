
/* Run with java -classpath mecureo.jar App ontology.fdg */
import foldocml.Connection;
import java.io.*;
import hashgraph.HashGraph;
class App {
    public static void main(String[] args) throws IOException {
        HashGraph ont = Connection.openGraph(new java.io.File(args[0]));
        Connection.outputRDF(ont, new PrintWriter(System.out));
    }
}
