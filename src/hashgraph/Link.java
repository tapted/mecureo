package hashgraph;

import foldocparser.Token;

/**
 * Represents a link between servers/computers in a network or, more abstractly,
 * represents a weighted edge in a graph. The Link is undirected.
 *
 * @author Trent Apted
 */
public class Link implements Comparable, java.io.Serializable {

    /** The token used to create this link */
    Token tok;
    /** parent here is the originator (the definition whose context this link
     *  was extracted) child is the link.
     */
    Node parent, child;
    /** The weight - influenced both by the Token and context in the parser */
    double weight;

    /**
     * Creates a new Link with...
     *
     * @param par the originating Node
     * @param kid the target node
     * @param wt the weight of the link
     */
    public Link (Node par, Node kid, double wt, Token tk) {
        parent = par;
        child = kid;
        weight = wt;
        tok = tk;
	//throw new Error("Sample stack trace: first link creation");
    }

    /**
     * @return this links weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @return the parent node
     */
    public Node getParent() {
        return parent;
    }

    /**
     * @return the child node
     */
    public Node getChild() {
        return child;
    }


    /**
     * Test if two Link objects are equal.<p>
     * Two links are defined to be equal if they connect the same Node objects.<p>
     * O(1)
     *
     * @param o the object to compare to this Node object
     * @return true if both are links and have the nodes
     */
    public boolean equals(Object o) {
        if (o instanceof Link) {
            Link l = (Link)o;
            return parent.equals(l.parent) && child.equals(l.child);
        } else {
            return false;
        }
    }

    public Link reverseType() {
        return new Link(parent, child, weight, tok.reverse());
    }

    public Link reverseDir() {
        return new Link(child, parent, weight, tok);
    }

    public Link reverseBoth() {
        return new Link(child, parent, weight, tok.reverse());
    }

    /**
     * Returns the hashcode value for this object.<p>
     * Adds the two node's hashcodes.<p>
     * O(1) - node hashCodes are O(1)
     *
     * @return the hashcode value for this object.
     */
    public int hashCode() {
        return parent.hashCode() + child.hashCode();
    }

    /**
     * Performs weight comparison
     *
     * @returns 0 if .equals(), -1 if <code>this is less than o </code> or
     *                           1 if <code>this is greater than o</code>
     */
    public int compareTo(Object o) {
        if (o instanceof Link) {
            double wt = ((Link)o).weight;
            if (weight == wt)
                return equals(o) ? 0 : -1; //so that equal weights can be put in TreeSet
            else if (weight < wt)
                return -1;
            else
                return 1;
        } else {
            return -1;
        }
   }

   //declare these here so Java can use pointers to them
   private static final String dashed = "\"dashed\"", dotted = "\"dotted\"",
                               solid = "\"solid\"", bold = "\"bold\"",
                               preStyle = "style=";

   public String dotType() {
        StringBuffer sb = new StringBuffer("[");

        //line style: "dashed", "dotted", "solid", "invis" and "bold"
        sb.append(preStyle);
        if (weight < 0.3)
            sb.append(bold);
        else if (weight > 0.8)
            sb.append(dotted);
        else if (weight > 0.7)
            sb.append(dashed);
        else
            sb.append(solid);

        if (tok.isAntonym())
            sb.append(",dir=\"both\",arrowhead=\"inv\",arrowtail=\"inv\"");
        else if (tok.isBiDir())
            sb.append(",dir=\"both\"");
        else if (tok.isNoDir())
            sb.append(",dir=\"none\"");

        sb.append(']');
        return sb.toString();
   }

    /**
     * Returns a string representation of this link<p>
     *O(1)
     */
   public String toString() {
       String arrow = " --> ";
       if (tok.isSynonym())
           arrow = " <=> ";
       else if (tok.isAntonym())
           arrow = " >-< ";
       else if (tok.isChild())
           arrow = " <-- ";
       else if (tok.isBiDir())
           arrow = " <-> ";
       else if (tok.isNoDir())
           arrow = " --- ";
       return "\t<L>: " + parent  + arrow + child + " @ " + weight + " (" + tok + ")";
   }

    public Node not(Node n) {
        return parent.equals(n) ? child : parent;
    }

   public String typeString() {
       return tok.typeString();
   }

   public Token getTok() {
       return tok;
   }
}
