package hashgraph;

import java.util.*;
import foldocparser.Token;

/**
 * <p>A general weighted digraph structure with hash table lookup</p>
 * <p>Represents a set of Nodes and the links between them</p>
 *
 * @author Trent Apted
 * @version 1.0
 */
public class HashGraph implements Cloneable, java.io.Serializable {
    /** Flags for temp variable of Nodes to indicate merge source */
    public static final short MERGE_COMMON = 0x4,
                              MERGE_THIS = 0x1,
                              MERGE_OTHER = 0x2;

    /** Flag: wether to process backward links */
    public static boolean FLAG_BACK_LINK = true;
    /** A mapping from <code>Node.getKey()</code>s to the <code>Node</code>s themselves */
    private HashMap nodes;
    /** Tracks the state for failfast iterators; incremented for each removal or insertion */
    protected int modCount = 0;
    /** The modCount at the point when the Nodes in the Network were last given a complete path cost labelling */
    private int lastPaths = -1;
    /** The modCount at the point when the Nodes were last given a connectivity labelling */
    private int lastLabel = -1;

    /**
     * Create a new, empty HashGraph
     */
    public HashGraph() { /*fold00*/
        nodes = new HashMap();
    }

    /**
     * Create a new, empty HashGraph, with initial _node_ capacity
     * @param initialCapacity the intial capacity
     */
    public HashGraph(int initialCapacity) {
        nodes = new HashMap(initialCapacity);
    }

    /**
     * Always succeeds. Returns a node for key in this graph.
     */
    public Node getAddNode(String key) { /*fold00*/
        Node ret = (Node)nodes.get(key);
        if (ret == null) {
            ret = new Node(key);
            nodes.put(key, ret);
        }
        return ret;
    }

    /**
     * Add a node. Returns a previous mapping if any.
     */
    public Node add(String key) { /*fold00*/
        return (Node)nodes.put(key, new Node(key));
    }

    /**
     * Clear the network of all Nodes.<p>
     * O(1)
     */
    public void clear() { /*fold00*/
        modCount++;
        nodes.clear();
    }

    /**
     * Tests if a specific Node object exists in the network.<p>
     * O(1)
     *
     * @param o the Node or key to perform the test on.
     * @return true if the Node exists
     */
    public boolean contains(Object o) { /*fold00*/
        if (o instanceof Node) {
            return nodes.containsKey(((Node)o).getKey());
        } else { //try as if it were mapKey -- Integer or String
            return nodes.containsKey(o);
        }
    }

    /**
     * Get a node from the network.<p>
     * O(1)
     *
     * @param the key
     * @return the node object corresponding to the key (word)
     */
    public Node get(Object key) { /*fold00*/
        if (key instanceof Node)
            return (Node)nodes.get(((Node)key).getKey());
        return (Node)nodes.get(key);
    }

    /**
     * Return the nodes collection
     * @return a <code>Collection</code> of <code>Node</code>
     */
    public Collection getNodes() { /*fold00*/
        return nodes.values();
    }

    /**
     * Wether the HashGraph is empty
     * @return true if there are no links or nodes
     */
    public boolean isEmpty() { /*fold00*/
        return nodes.isEmpty();
    }

    /**
     * [private routine]
     * Clears all the temporary variables of the nodes<p>
     * O(n) ; number of nodes = size()
     */
    private void clearNodeTemps() { /*FOLD00*/
         Iterator it = nodes.values().iterator();
         while (it.hasNext())
            ((Node)it.next()).clearTemp();
    }

    /**
     * Tests if one node is reachable from another.<p>
     * Will check if a path of connections exists between the two nodes.<p>
     * O(n); density() (number of links) - doesn't bother with the priority queue<p>
     * However, if a previous dijkstra's or reachable labelling has been done
     * without a node being added or deleted since, there will be an early exit if
     * both nodes are already labelled (not with infinity) as they must
     * necessarily be connected for either algorithm to label them.
     * Also if it was a dijkstra's (and only dijkstra's) it will exit early if one
     * is labelled and the other is not (returning false), as they cannot be connected.
     *
     * @param key1
     * @param key2
     * @return true if a path of connections exist between the nodes.
     */
    public boolean isReachable(String key1, String key2) { /*fold00*/
        return isReachable((Node)nodes.get(key1), (Node)nodes.get(key2));
    }

    /**
     * [private routine]
     * Ensure all links from n are labelled, stopping when the target is reached<p>
     * Gives an arbitrary labelling, was originally a recursive routine, but
     * a non-recursive routine is more efficient, using a simple stack/queue
     * Does a breadth-first search (because pairing is quite low).
     */
    private void labelAllStop(Node n, Node target) { /*fold00*/
        //Set all the temporary variables to 0 (not labelled) - can't use clearAllTemps()
        //so that Diskstra's is not reset
        Iterator nit = nodes.values().iterator();
        while (nit.hasNext()) {
            ((Node)nit.next()).temp = 0;
        }
        lastLabel = modCount;
        final int LABELLED = 1;
        LinkedList lq = new LinkedList();
        n.temp = LABELLED;
        lq.addLast(n); //add source
        Node cur, n2;
        Iterator lit;
        while (target.temp != LABELLED && !lq.isEmpty() && (cur = (Node)lq.removeLast()) != null) { //end when known reachable or empty stack
            lit = cur.values().iterator(); //Links
            while (target.temp != LABELLED && lit.hasNext()) { //iterates only over number of links in n
                n2 = FLAG_BACK_LINK ? ((Link)lit.next()).not(cur) : ((Link)lit.next()).getChild(); //get the other node
                if (n2.temp != LABELLED) {
                    n2.temp = LABELLED;    //it is accessible from n
                    lq.addLast(n2);	   //add it to the queue
                }
            }
            if (FLAG_BACK_LINK) { //duplicate some code for speed.. (else two extra ifs each iter)
                lit = cur.backIter();
                while (target.temp != LABELLED && lit.hasNext()) { //iterates only over number of links in n
                    n2 = ((Link)lit.next()).not(cur); //get the other node
                    if (n2.temp != LABELLED) {
                        n2.temp = LABELLED; //it is accessible from n
                        lq.addLast(n2);     //add it to the queue
                    }
                }
            }
        }
    }

    /**
     * Tests if one node is reachable from another.  Will check if a path of connections exists between the two nodes.
     *
     * Algorithm:<p>
     * Start at ip1, label it and all nodes connected to it arbitrarily
     * if ip2 is labelled at any time then it is reachable from ip1<p>
     * O(n) on the number of links
     *
     * @param n1 the first node.
     * @param n2 the second node.
     * @return true if a path of connections exist between the nodes.
     */
    public boolean isReachable(Node n1, Node n2) { /*fold00*/
        if (n1 == null || n2 == null)
            return false;
        //only label if a change has occurred since a run of paths (which
        //gives labels to all connected nodes) or neither node has a label
        //(in which case they may still be connected, but disconnected from all
        //currently labelled Nodes)
        if (lastPaths == modCount) { //there is a current cost labelling
            if (n1.cost != Node.INFINITY && n2.cost != Node.INFINITY) {
                return true; //connected by dijkstra's
            } else if ((n1.cost == Node.INFINITY) != (n2.cost == Node.INFINITY)) {
                return false; //disconnected by dijkstra's : one is infinity and one isn't
            } //else both infinity -- may or may not be connected - run alg.
        }
        if (lastLabel == modCount) { //there is a current connectivity labelling
            if (n1.temp == 1 && n2.temp == 1) { //both labelled
                return true; //due to early exits in isReachable, this is the only guaranteed state
            }
        }
        labelAllStop(n1, n2);
        cleanUp();
        //return true if n2 is NOT UNlabelled
        return n2.temp != 0;
    }

    /**
     * Returns a node iterator for the graph<p>
     * O(1) - backed by the HashMap of nodes.
     *
     * @return an iterator for the nodes in the network.
     */
    public Iterator nodeIterator() { /*fold00*/
        return nodes.values().iterator();
    }

    /**
     * Returns a link iterator for forward links in the graph<p>
     * O(1);
     *
     * @return an iterator for the links in the network.
     */
    public Iterator linkIterator() { /*fold00*/
        return new LinkIterator();
    }

    /**
     * A nested class implementing an Iterator over the <code>Link</code>s in the graph
     * @author Trent Apted
     * @version 0.4
     */
    public class LinkIterator implements Iterator { /*fold00*/
        Iterator nit;
        Iterator lit;
        LinkIterator() {
            nit = nodeIterator();
            if (nit.hasNext())
                lit = ((Node)nit.next()).values().iterator();
        }
        public boolean hasNext() {
            while (nit.hasNext() && !lit.hasNext())
                lit = ((Node)nit.next()).values().iterator();
            return nit.hasNext() || lit.hasNext();
        }
        public Object next() {
            while (!lit.hasNext())
                lit = ((Node)nit.next()).values().iterator();
            return lit.next();
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns the shortest path between two nodes on the graph
     *
     * @param key1
     * @param key2
     * @return a List with each Node object in the path stored in order in the list.
     * @see #storePaths(Node, double, HashGraph)
     */
    public List path(String key1, String key2) { /*fold00*/
        return path((Node)nodes.get(key1), (Node)nodes.get(key2));
    }

    /**
     * Returns the shortest path between two nodes on the network.<p>
     * Checks to see if the current labelling can be used before running dijkstra's
     *
     * @param n1 the first node.
     * @param n2 the second node.
     * @return a List with each Link object in the path stored in order in the list.<br>
     * If n1 = n2 returns an empty List<br>
     * If n1 is not reachable from n2 returns null<br>
     * Otherwise returns a list of Link objects from n1 to n2
     * @see #storePaths(Node, double, HashGraph)
     */
    public List path(Node n1, Node n2) { /*FOLD00*/
        if (n1.equals(n2)) {
            return new LinkedList();
        } else {
            if (n1.cost != 0 || lastPaths != modCount) { //check to see if the
                storePaths(n1); 		       //current labelling can be used
                cleanUp();
            }
            if (n2.prev == null) {//not reachable
                return null;
            } else {
                return (new Path(n1, n2, this)).getPath();
            }
        }
    }

    /**
     * Finds the shortest paths from n to all other nodes.<p>
     * Cheks to see if current labelling can be used before running dijkstra's<p>
     * Only maps those nodes reachable from n, so: <p>
     * <pre>
     * allCosts(srcNode).containsKey(destNode) %lt;=&gt; isReachable(srcNode, destNode)
     * </pre>
     *
     * @param n the source node
     * @return a HashMap mapping nodeKeys (Integer or String) to <code>Path</code>s,
     *		the cost of going from n to the key along the shortest path
     * @see #storePaths(Node, double, HashGraph)
     */
    public HashMap allPaths(Node n) { /*fold00*/
        if (n.cost != 0 || lastPaths != modCount) //check to see if the current
            storePaths(n);			  //labelling can be used
        Iterator it = nodes.values().iterator();
        Node n2;
        HashMap ret = new HashMap();
        ret.put(n.getKey(), new Path(n, n, this));
        while(it.hasNext()) {
            n2 = (Node)it.next();
            if (n2.prev != null) {
                ret.put(n2.getKey(), new Path(n, n2, this));
            }
        }
        cleanUp();
        return ret;
    }

    /**
     * Returns a <code>HashMap</code> from <code>Node</code>s to <code>Integer</code>s<p>
     * The mapping is it's key's shortest path cost from <code>n</code>.<p>
     * Checks to see if the current labelling can be used before running dijkstra's
     *
     * @param n the source node from which to calculate all costs
     * @return a <code>HashMap</code> from <code>Node</code>s to <code>Integer</code>s
     * @see #storePaths(Node, double, HashGraph)
     */
    public HashMap allCosts(Node n) { /*fold00*/
        if (n.cost != 0 || lastPaths != modCount) //check to see if the current
            storePaths(n);			  //labelling can be used
        Iterator it = nodes.values().iterator();
        Node n2;
        HashMap ret = new HashMap();
        //put the source in the list
        ret.put(n.getKey(), new Double(n.cost)); //n.cost is always 0.
        while(it.hasNext()) {
            n2 = (Node)it.next();
            if (n2.prev != null) {
                ret.put(n2.getKey(), new Double(n2.cost));
            }
        }
        cleanUp();
        return ret;
    }

    /**
     * Return a <b><i>deep </b></i> copy of this graph
     * @return a <code>HashGraph</code> duplicate of this
     * @throws CloneNotSupportedException not thrown
     */
    public Object clone() throws CloneNotSupportedException {
        HashGraph ret = new HashGraph(nodes.size());
        for (Iterator it = nodeIterator(); it.hasNext(); ) {
            Node n = (Node)it.next();
            ret.nodes.put(n.getKey(), n.costCopy());
        }
        ret.copyLinks(this);
        return ret;
    }

    /**
     * Stores the path and costs of the shortestPaths from n1 to all other nodes<p/>
     * The path can then be found by iterating through the Nodes' temporary variables<p/>
     * @param n1 the source
     * @see #storePaths(Node, double, HashGraph)
     */
    void storePaths(Node n1) { /*fold00*/
        storePaths(n1, -1);
    }

    /**
     * Store paths with a maximum cost [early exit]
     * @param n1
     * @param maxCost
     * @see #storePaths(Node, double, HashGraph)
     */
    void storePaths(Node n1, double maxCost) { /*fold00*/
        storePaths(n1, maxCost, null);
    }

	public HashGraph depthgrow(Iterator target, double ddepth) {
		int depth = (int)ddepth;
		if (depth != ddepth) {
			System.err.println("WARNING: depth was not an integer; using " + depth);
		}
		
		clearNodeTemps();
		HashSet result = new HashSet();
		//BFS => Queue
		LinkedList queue = new LinkedList();		
		final int INQUEUED = 1;
		
		for (Iterator it = target; it.hasNext(); ) {
			Node n = get(it.next());
			if (n != null) {
				n.cost = 0;
				n.temp = INQUEUED;
				queue.addLast(n);
			}
		}
		
		Node eye, dest;
		Iterator lit;
		Link lnk;
		
		while (!queue.isEmpty()) {
			eye = ((Node)queue.removeFirst());
			result.add(eye);
			if (((int)eye.cost) < depth) {
			    lit = eye.values().iterator();
            	for (int i = 0; i < 2; ++i) { //go through twice
                	while (lit.hasNext()) {
                    	lnk = (Link)lit.next();
                    	dest = lnk.not(eye);
                    	if (eye.cost + 1 < dest.cost) {
                        	dest.cost = eye.cost + 1;
                        	if (dest.temp != INQUEUED) {
                        		queue.addLast(dest);
                        		dest.temp = INQUEUED;
                        	} else {
                        		System.err.println("ERR: Depth not monotonic");
                        	}
                    	}
                    	
                    }
                }
                lit = eye.backIter();
            }
        }
        
        //add results
        HashGraph ret = new HashGraph(result.size());
        for (Iterator it = result.iterator(); it.hasNext(); ) {
            Node n = (Node)it.next();
            ret.nodes.put(n.getKey(), n.costCopy());
            n.noderef = null; //free up for garbage collector
        }
        ret.copyLinks(this);
        cleanUp();
        return ret;				
	}
	
	public HashGraph sizegrow(Iterator target, double maxNodes) {
		int num = (int)maxNodes;
		if (num != maxNodes) {
			System.err.println("WARNING: maxNodes was not an integer; using " + num);
		}
		
		return grow(target, 1000000000.0, num);		
	}

    public HashGraph grow(Iterator target, double amount) {
    	return grow(target, amount, 0xffffff);
    }

    /**
     * Grow <code>target</code> using <code>this</code> the specified <code>amount</code>
     * as if it were a model to include concepts related to <code>target</code>
     * @param target the model to grow
     * @param amount the amount to grow it
     * @return the grown model
     * @see foldocml.Connection#grow(Iterator words, double depth)
     */
    public HashGraph grow(Iterator target, double amount, int maxNodes) {
        clearNodeTemps();
        DataStructures.PairHeap pq = new DataStructures.PairHeap();
        //add all nodes in target to queue, with cost = 0
        for (Iterator it = target/*.nodeIterator()*/; it.hasNext(); ) {
            Node n = get(it.next());
            if (n == null)
                continue;
            n.cost = 0;
            n.noderef = pq.insert(n);
        }
        Node eye = null;
        HashSet result = new HashSet();
        Iterator lit;
        Link lnk;
        Node dest;
        double newCost;
        boolean changed = true;
        final int SCANNED = 1;
        while (!pq.isEmpty() && result.size() < maxNodes) {
            eye = (Node)pq.deleteMin();
            if (eye.cost > amount)
                break;
            result.add(eye);
            eye.temp = SCANNED;
            //System.err.println(eye);
            lit = eye.values().iterator();
            for (int i = 0; i < 2; ++i) { //go through twice
                while (lit.hasNext()) {
                    lnk = (Link)lit.next();
                    dest = lnk.not(eye);
                    if (dest.cost > 0) { //else 0 => already in queue or result
                        if (eye.cost + lnk.getWeight() == 0)
                            dest.cost = 0;
                        else if (eye.cost == 0) //adjust cost a la 'parallel resistors'
                            dest.cost = 1/(1/dest.cost + 1/lnk.getWeight());
                        else { //pick minimum
                            newCost = eye.cost + lnk.getWeight();
                            if (newCost < dest.cost)
                                dest.cost = newCost;
                            else
                                changed = false;
                        }
                        //System.err.println("\t" + dest);
                        if (changed && dest.temp != SCANNED) { //else already in results
                            if (dest.noderef != null) //already in queue
                                pq.decreaseKey(dest.noderef, dest);
                            else
                                dest.noderef = pq.insert(dest);
                        } else {
                            changed = true;
                        }
                    }
                }
                lit = eye.backIter();
            }
        }
        //add results
        HashGraph ret = new HashGraph(result.size());
        for (Iterator it = result.iterator(); it.hasNext(); ) {
            Node n = (Node)it.next();
            ret.nodes.put(n.getKey(), n.costCopy());
            n.noderef = null; //free up for garbage collector
        }
        ret.copyLinks(this);
        cleanUp();
        return ret;
    }

    /**
     * The old version of storePaths - for generating subgraphs
     * <p>Performs Dijkstra's from <code>n1</code> with <code>maxCost</code> and
     * <code>goal</code> used for early exit criterion</p>
     * @param n1 the source
     * @param maxCost maximum path length
     * @param goal the target [for early exit]
     * @return goal if found, else null
     */
    Node storePaths(Node n1, double maxCost, HashGraph goal) { /*FOLD00*/
        clearNodeTemps();
        if (maxCost < 0) {
            lastPaths = modCount;
            lastLabel = modCount - 1; //overwrites a connectivity labelling
                                      //but provides more early exit possibilities for
                                      //isReachable()
        }
        DataStructures.PairHeap pq = new DataStructures.PairHeap();
        n1.cost = 0;
        n1.noderef = pq.insert(n1);
        Node eye = null;
        //declare outside to avoid being garbage collected
        Iterator lit; //Iterates Links
        Link lnk;     //Current Link
        Node dest;    //Target of link
        double newCost;  //new cost to test from lnk to dest
        boolean wasLabelled; //whether dest was not initally unlabelled
        final int SCANNED = 1;
        int backLink = FLAG_BACK_LINK ? 1 : 0;

        while(!pq.isEmpty()) {	   //dijkstra's algorithm
            eye = (Node)pq.deleteMin();

            if (goal != null && goal.contains(eye))
                return eye;

            eye.temp = SCANNED;
            lit = eye.values().iterator();
            do {
                while (lit.hasNext()) {
                    lnk = (Link)lit.next();
                    dest = FLAG_BACK_LINK ? lnk.not(eye) : lnk.getChild();
                    if (dest.temp != SCANNED) {
                        newCost = eye.cost + lnk.weight;
                        if (dest.cost > newCost) {
                            dest.cost = newCost;  //added 2002-08-26
                            //wasLabelled = dest.cost != Node.INFINITY /*&& dest.noderef != null*/;
                            dest.prev = eye;
                            if (dest.noderef != null) {
                                pq.decreaseKey(dest.noderef, dest);
                            } else if (maxCost < 0 || newCost <= maxCost) {
                                dest.noderef = pq.insert(dest);
                                //dest.cost = newCost; //removed 2002-08-26
                            }
                        }
                    }
                }
                lit = eye.backIter();
            } while (backLink-- > 0);
        }
        return null;
    }

    private void cleanUp() {
        for (Iterator it = nodeIterator(); it.hasNext(); ) {
            Node n = (Node)it.next();
            n.noderef = null;
            //n.prev = null;
        }
    }

    /**
     * Gives the total (minimum) cost between two given nodes.
     *
     * @param key1
     * @param key2
     * @return the ping between the two nodes.
     * @see #storePaths(Node, double, HashGraph)
     */
    public double cost(String key1, String key2) { /*fold00*/
        return cost((Node)nodes.get(key1), (Node)nodes.get(key2));
    }

    /**
     * Gives the total (minimum) cost time between two given nodes.
     * @param n1 the first node.
     * @param n2 the second node.
     * @return the cost between the two nodes.
     * returns -1 if no path exists between n1 & n2
     * return 0 if n1 = n2
     */
    public double cost(Node n1, Node n2) { /*FOLD00*/
        if (n1.equals(n2)) {
            return 0;
        } else if (lastPaths == modCount && (n2.cost == Node.INFINITY) != (n1.cost == Node.INFINITY)) {
            return -1; //one is labelled, the other is not, so not reachable
            /*
             * This provides an early exit if dijkstra's has been performed from ANY source
             * and it was found from this that n1 and n2 are disconnected.
             * I could just run isReachable, but the scalability of dijkstra's
             * with a Multi-Level Bucket is not much worse than isReachable, so it
             * would almost double the runtime
             */
        } else	  { //only reexamine if NEITHER n1 or n2 is already a source or network has changed
            if ((n1.cost != 0 && n2.cost != 0) || lastPaths != modCount)
                storePaths(n1);
                cleanUp();
            if ((n2.cost == Node.INFINITY) != (n1.cost == Node.INFINITY)) {
                return -1; //one is labelled, the other is not, so not reachable
            } else { //determine which is the source, and return the other's cost from it
                return n2.cost == 0 ? n1.cost : n2.cost;
            }
        }
    }

    /**
     * Remove a given Node and its links from the graph<p>
     * O(n) to remove a node (n = #links to/from o; &lt; size() AND &lt;&lt; density())<p>
     *
     * @param n the Node to remove (or its key)
     * @return false if no such Node exists in the graph.
     * @throws ClassCastException the object is neither a key nor a Node.
     */
    public boolean remove(Object o) { /*fold00*/
        if (!contains(o)) return false;
        if (o instanceof Node) {
            removeLinks((Node)o);
            nodes.remove(o);
        } else {
            removeLinks((Node)nodes.get(o));
            nodes.remove(o);
        }
        return true;
    }

    /**
     * [private routine]
     * Remove, from Nodes connected to n, back references to Links involving n<p>
     * O(n); degree of n; &lt; size &lt;&lt; density()
     */
    private void removeLinks(Node n) { /*fold00*/
        //if there are no backward links this is too inefficient to do!!
        if (!FLAG_BACK_LINK || n == null) return; //n is not contained in Network
        Iterator it = n.backIter(); //iterates backward Nodes
        Link lnk;
        Object k = n.getKey();
        while (it.hasNext()) {
            lnk = (Link)it.next();
            lnk.getParent().remove(k); //remove the mapping to n in the node in the link
        }                                    //that is not n
    }

    /**
     * Return the number of nodes in the network.<p>
     * O(1)
     *
     * @return the number of nodes in the network.
     */
    public int size() { /*fold00*/
        return nodes.size();
    }

    /**
     * Return the number of links in the network.
     * Does not include backward links.
     * O(n); number of nodes
     *
     * @return the number of links in the network.
     */
    public int density() { /*fold00*/
        int sum = 0;
        Iterator it = nodes.values().iterator();
        while (it.hasNext()) {
            Node n = (Node)it.next();
            sum += n.size();
            sum += n.backLinks.size();
        }
        return sum / 2; //each link is counted twice
    }

    /**
     * Returns a string representation of this network:<p>
     * Lists all the nodes first, each on a new line, followed by all the links
     * (each on a new line)<p>
     *
     * O(n) - max {size(). density()}
     */
    public String toString() { /*fold00*/
        String s = "";
        Iterator it = nodeIterator();
        while (it.hasNext()) {
            s += "\n" + it.next().toString();
        }
        return s;
    }

    /**
     * Merge <code>this</code> with another HashGraph, ignoring costs.
     * Performs a shallow copy.
     * @param other the otehr graph to merge
     * @param link wether to [shallow] copy links
     * @return
     */
    public HashGraph merge(HashGraph other, boolean link) { /*fold00*/
        HashGraph ret = new HashGraph();
        ret.nodes = (HashMap)nodes.clone(); //shallow copy
        for (Iterator it = other.nodeIterator(); it.hasNext();) {
            Node n = (Node)it.next();
            if (ret.contains(n.getKey())) {
                if (link) {
                    //append links
                    Node n2 = ret.get(n.getKey());
                    n2.putAll(n);
                    n2.backLinks.putAll(n.backLinks);
                }
            } else {
                ret.nodes.put(n.getKey(), n);
            }
        }
        return ret;
    }

    /**
     * Merge and link this with other
     * @param other the other graph to merge
     * @return the merged graph
     */
    public HashGraph merge(HashGraph other) { /*fold00*/
        return merge(other, true);
    }

    /**
     * Get as many links as possible from <code>from</code> that include nodes in <code>this</code>
     * @param from
     */
    public void copyLinks(HashGraph from) { /*fold00*/
        for (Iterator it = nodeIterator(); it.hasNext();) {
            Node parent = (Node)it.next();
            //get each node from 'from', if it exists
            //can't use putAll... cos it would reference the wrong graph
            Node old;
            if ((old = (Node)from.nodes.get(parent.getKey())) != null) {
                for (Iterator it2 = old.values().iterator(); it2.hasNext();) {
                    Link l = (Link)it2.next();
                    Node kid = (Node)nodes.get(l.not(parent).getKey());
                    if (kid != null)
                        parent.put(kid.getKey(), new Link(parent, kid, l.weight, l.tok));
                }
                //backlinks are a bit more complicated
                for (Iterator lit = old.backLinks.values().iterator(); lit.hasNext();) {
                    Link l = (Link)lit.next();
                    Node oldPar = l.not(parent);	       //get parent in from
                    Node newPar = (Node)nodes.get(oldPar.getKey());  //look for parent in this
                    if (newPar != null)
                        parent.backLinks.put(newPar.getKey(), new Link(newPar, parent, l.weight, l.tok));
                }
            }
        }
        ++modCount;
    }

    /**
     * Merge with another HashGraph, adding costs when shared or infinity if not.
     * Performs a DEEP copy: so that links can be maintained
     * @param other the other graph to merge
     * @param thisInf the penalty for a disjoint node originating from <code>this</code>
     * @param otherInf the penalty for a disjoint node originating from <code>other</code>
     * @return the merged model
     */
    public HashGraph costMerge(HashGraph other, double thisInf, double otherInf) { /*fold00*/
        HashGraph ret = new HashGraph();
        //first get an unique set of nodes from this.
        //maintain cost values, but ignore links for now.
        for (Iterator it = nodeIterator(); it.hasNext();) {
            Node n = ((Node)it.next()).costCopy();
            n.temp = MERGE_THIS; //not common
            ret.nodes.put(n.getKey(), n);
        }
        //add other graph, adding costs if common and flagging
        for (Iterator it = other.nodeIterator(); it.hasNext();) {
            Node n = ((Node)it.next()).costCopy();
            n.temp = MERGE_OTHER; //not common
            //see if it already exists in ret
            Node n2 = ret.get(n.getKey());
            if (n2 == null) {
                ret.nodes.put(n.getKey(), n);
            } else {
                n2.cost += n.cost;
                n2.temp = MERGE_COMMON;  //flag it as 'common'
            }
        }
        //now go through to increment Nodes not common by infinity
        for (Iterator it = ret.nodeIterator(); it.hasNext();) {
            Node n = (Node)it.next();
            if (n.temp == MERGE_THIS)
                n.cost += thisInf;
            else if (n.temp == MERGE_OTHER)
                n.cost += otherInf;
        }

        //copy links from this
        ret.copyLinks(this);
        //copy links from other
        ret.copyLinks(other);

        return ret;
    }

    /**
     * costMerge/3 with thisInf = otherInf = infinity
     * @param other
     * @param infinity = otherInf = thisInf
     * @return
     */
    public HashGraph costMerge(HashGraph other, double infinity) { /*fold00*/
        return costMerge(other, infinity, infinity);
    }

    /**
     * Intersect with another HashGraph, adding costs.
     * Performs a DEEP copy: so that links can be maintained
     * @param other the graph to find common nodes with
     * @return the intersection of the graphs
     */
    public HashGraph costIntersect(HashGraph other) { /*fold00*/
        HashGraph ret = new HashGraph();
        //go through this looking for things also in other
        for (Iterator it = nodeIterator(); it.hasNext();) {
            Node n = ((Node)it.next()).costCopy();
            Node n2 = (Node)other.nodes.get(n.getKey());
            if (n2 != null) {
                n.cost += n2.cost; //add costs
                ret.nodes.put(n.getKey(), n);
            }
        }

        ret.copyLinks(this);
        ret.copyLinks(other);

        return ret;
    }


    /**
     * Retrieve a subgraph with all nodes within cost <code>dist</code>ance
     * <p>
     * Performs a DEEP copy: links need to be modified and costs need to be
     * maintained as different accross common subgraphs
     * </p>
     *
     * @param key the start node
     * @param dist the distance to search
     * @return the subgraph generated
     */
    public HashGraph subgraph (String key, double dist) { /*FOLD00*/
        HashGraph ret = new HashGraph();
        //make sure a dijkstra's labelling exists from key
        Node n;
        if ((n = (Node)nodes.get(key)) == null)
            return null; //key no mapping for key
        if (n.cost != 0 || lastPaths != modCount) //check to see if the current
            storePaths(n, dist);			//labelling can be used
        //do a BFS looking for nodes within cost distance
        LinkedList lq = new LinkedList();
        lq.addFirst(n);
        while (!lq.isEmpty()) {
            Node eye;
            //pop the first element (cost must be less than dist)
            eye = (Node)lq.removeFirst();
            ret.nodes.put(eye.getKey(), eye.costCopy()); //DEEP copy it
            //put links on the queue in both directions IF not already scanned
            for (Iterator it = eye.values().iterator() ; it.hasNext();) {
                Node n2 = ((Link)it.next()).not(eye);
                if (n2.cost <= dist && !ret.nodes.containsKey(n2.getKey()))
                    lq.addLast(n2);
            }
            for (Iterator it = eye.backLinks.values().iterator(); it.hasNext();) {
                Node n2 = ((Link)it.next()).not(eye);
                if (n2.cost <= dist && !ret.nodes.containsKey(n2.getKey()))
                    lq.addLast(n2);
            }
        }
        //now copy the links into subgraph
        ret.copyLinks(this);
        cleanUp();
        return ret;
    }

    //you get a native stack overflow unless you do it yourself
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException { /*fold00*/
        writeData(out);
    }


    /**
     * Serialise this graph to out
     * @param out the output of the serialisation
     * @throws java.io.IOException on an IO error
     */
    public void writeData(java.io.ObjectOutputStream out) throws java.io.IOException { /*fold00*/
        //first generate a String table
        int serial = 0;
        HashMap strings = new HashMap();
        for (Iterator it = nodeIterator(); it.hasNext();)
            strings.put(((Node)it.next()).getKey(), new Integer(serial++));
        //output the string table
        //out.writeInt(strings.size());
        progWriteInt(out, strings.size());
        for (Iterator it = strings.entrySet().iterator(); it.hasNext();) {
            Map.Entry me = (Map.Entry)it.next();
            //out.writeInt(((Integer)me.getValue()).intValue());
            progWriteInt(out, ((Integer)me.getValue()).intValue());
            out.writeUTF((String)me.getKey());
        }
        //now output the graph
        //out.writeInt(nodes.size());
        progWriteInt(out, nodes.size());
        int count = 0;
        int linkCount = 0;
        for (Iterator it = nodeIterator(); it.hasNext();) {
            Node n = (Node)it.next();
            //out.writeInt(((Integer)strings.get(n.getKey())).intValue());
            progWriteInt(out, ((Integer)strings.get(n.getKey())).intValue());
            //out.writeDouble(n.cost);
            out.writeFloat((float)n.cost);

            try {
                progWriteInt(out, Integer.parseInt(n.webID));
            } catch (NumberFormatException nfe) {
                progWriteInt(out, 0);
            }

            //out.writeInt(n.values().size());
            progWriteInt(out, n.values().size());
            Iterator lit = n.values().iterator();
            for (int back = 0; back < 2; ++back) {
                for (; lit.hasNext();) {
                    Link l = (Link)lit.next();
                    //out.writeInt(((Integer)strings.get(l.getChild().getKey())).intValue());
                    progWriteInt(out, ((Integer)strings.get(l.getChild().getKey())).intValue());
                    //out.writeInt(((Integer)strings.get(l.getParent().getKey())).intValue());
                    progWriteInt(out, ((Integer)strings.get(l.getParent().getKey())).intValue());
                    //out.writeDouble(l.weight);
                    out.writeFloat((float)l.weight);
                    //out.writeInt(l.tok.getFlags());
                    progWriteInt(out, l.tok.getFlags());
                    linkCount++;
                }
                //out.writeInt(n.backLinks.size());
                progWriteInt(out, n.backLinks.size());
                lit = n.backIter();
            }
            count++;
        }
        out.close(); //I hate java. Do you have any idea how long it took me to
                     //figure out that the absence of this line was causing
                     //all my problems. grrrrrrrrrrr.
    }

    //inverse of writeObject
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException { /*fold00*/
        readData(in);
    }

    /**
     * Deserialise into <code>this</code> with progress posts to <code>post</code>
     * @param in the serialsed input
     * @param post the <code>Poster</code> to which progress updates are sent
     * @throws java.io.IOException on an IO error
     * @throws ClassNotFoundException not thrown
     */
    public void readData(java.io.ObjectInputStream in, foldocui.Poster post) throws java.io.IOException, ClassNotFoundException { /*fold00*/
        final double TABLE_PROP = 0.2; //proportion of time spent reading table
        final int postInterval = 100;
        int lastPost = 0;
        int count = 0;
        int linkCount = 0;
        try {

            //first retrieve the String table
            HashMap strings = new HashMap();
            int numStrings = progReadInt(in); //should actually be equal to graph size...
            nodes = new HashMap(numStrings);

            for (int i = 0; i < numStrings; ++i) {
                strings.put(new Integer(progReadInt(in)), in.readUTF());
                if (i > lastPost && post != null) {
                    post.post(TABLE_PROP * (double)i / numStrings);
                    lastPost += postInterval;
                }
            }
            lastPost = 0;
            //now retrieve the graph


            int graphSize = progReadInt(in);
            for (int i = 0; i < graphSize; ++i) {
                Node n = getAddNode((String)strings.get(new Integer(progReadInt(in))));
                //n.cost = in.readDouble();
                n.cost = in.readFloat();
                n.webID = Integer.toString(progReadInt(in));
                //System.err.println("Read n.webID = " + n.webID);
                int linksSize = progReadInt(in);
                for (int back = 0; back < 2; ++back) {
                    for (int j = 0; j < linksSize; ++j) {
                        Node kid = getAddNode((String)strings.get(new Integer(progReadInt(in))));
                        Node par = getAddNode((String)strings.get(new Integer(progReadInt(in))));
                        //double wt = in.readDouble();
                        double wt = in.readFloat();
                        Token tk = new Token(progReadInt(in), "");
                        Link l = new Link(par, kid, wt, tk);
                        if (back == 0)
                            n.put(l.not(n).getKey(), l);
                        else
                            n.backLinks.put(l.not(n), l);
                        linkCount++;
                    }
                    linksSize = progReadInt(in);
                }
                if (i > lastPost && post != null) {
                    post.post(TABLE_PROP + (1-TABLE_PROP) * (double)i / graphSize);
                    lastPost += postInterval;
                }
                count++;
            }



        } catch (java.io.EOFException eof) {
            System.err.println("Unexpected EOF");
        }
        ++modCount;
        System.err.println("Read in " + count + " nodes.");
        System.err.println("Read in " + linkCount + " links.");
    }

    /**
     * Deserialise into <code>this</code> without progress posts
     * @param in the serialsed input
     * @throws java.io.IOException on an IO error
     * @throws ClassNotFoundException not thrown
     */
    public void readData(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException { /*fold00*/
        readData(in, null);
    }

    /**
     * for loading to make sure paths are redone
     */
    public void reset() { /*FOLD00*/
        if (modCount > 0)
            modCount--;
    }

    /**
     * Looks for the minimum distance from n (a node in this HashGraph)
     * to any node in 'to' (a subgraph of this HashGraph)
     * @param n the start node
     * @param to the target graph
     * @param maxDistance the maximum path length =&gt; an early exit
     * @return the distance or -1 if no path
     */
    public double minDistance (Node n, HashGraph to, double maxDistance) { /*FOLD00*/
        if (!contains(n))
            return -1;
        Node nin = (Node)nodes.get(n.getKey());
        Node closest = storePaths(nin, maxDistance, to);
        cleanUp();
        if (closest == null) { //no path
            return -1;
        } else {
            return closest.cost;
        }
    }

    /**
     * minDistance with infinity maxDistance
     * @see #minDistance(Node, HashGraph, double)
     */
    public double minDistance (Node n, HashGraph to) {
        return minDistance(n, to, -1);
    }

    /**
     * Generate a minimum spanning tree and return the sum of its link weights
     * @return the weigth sum
     */
    public double minSpanWeight() {
        HashGraph ret = new HashGraph();
        ret.FLAG_BACK_LINK = false;
        Set unlinked = new HashSet(nodes.values());
        HashSet edges = new HashSet();
        double weightSum = 0;
        //get unique, undirected edges
        for (Iterator it = linkIterator(); it.hasNext(); ) {
            edges.add(new UndirLink((Link)it.next()));
        }
        //sort [no need for a priority queue]
        Object[] edgeQueue = edges.toArray();
        Arrays.sort(edgeQueue);

        for (int i = 0; !unlinked.isEmpty() && i < edgeQueue.length; ++i) {
            UndirLink ul = (UndirLink)edgeQueue[i];
            String k1 = ul.n1.getKey(), k2 = ul.n2.getKey();
            if (ret.isReachable(k1, k2))
                continue; //would create a cycle
            Node n1 = ret.getAddNode(k1);
            Node n2 = ret.getAddNode(k2);
            Link l = new Link(n1, n2, ul.weight, null);
            n1.put(k2, l);
            n2.put(k1, l);
            unlinked.remove(get(k1));
            unlinked.remove(get(k2));
            weightSum += ul.weight;
        }
        cleanUp();
        return weightSum;
    }

    /**
     * TODO: Need to put this in a better place
     * Read/Write an int progressively: use first 2 bits of first byte in the
     * encoding to store the total number of bytes used:
     * 00 = 4 bytes [for backward compatability; only graphs &lt; 1billion nodes :]
     * 01 = 3 bytes
     * 10 = 2 bytes
     * 11 = 1 byte
     */
    private static final char PROG_SIZE_MASK = 0xc0;
    private static final int PROG_MASK_SHIFT = 6;

    /**
     * Read a progressively encoded 32-bit int from in
     * @param in the input
     * @return the next progInt on in
     * @throws java.io.IOException on an IO error
     */
    public static int progReadInt(java.io.ObjectInputStream in) /*fold00*/
                                                    throws java.io.IOException {
        int cur = 0xff & in.readByte();
        int res = ~PROG_SIZE_MASK & cur;
        for ( int i = (PROG_SIZE_MASK & cur) >> PROG_MASK_SHIFT;
              i < 0x03;
              ++i) {
            res <<= 8;
            res |= 0xff & in.readByte();
        }
        return res;
    }

    /**
     * Progressively encode a 32-bit int and write it to out - must be less than 2^30
     * @param out where to write the progInt
     * @param val the int to encode/write
     * @throws java.io.IOException on a IO error
     * @throws IllegalArgumentException if val is too big to encode
     */
    public static void progWriteInt(java.io.ObjectOutputStream out, int val) /*fold00*/
                                                    throws java.io.IOException {
        //hold 00/01/10/11 bytes
        int bytes[] = {0, 0, 0, 0};
        int vt = val;
        for ( int i = 0x03; i >= 0; --i, vt >>= 8)
            bytes[i] = (vt & 0xff);

        if ((bytes[0] & PROG_SIZE_MASK) != 0)
            throw new java.lang.IllegalArgumentException(
              "'val' is to big to write progressively. Must be < 2^30.");

        //predetermine size
        char psize = 0x03;
        for ( int prog = val >> PROG_MASK_SHIFT;
              prog != 0;
              prog >>= 8, --psize);

        //write first byte
        out.writeByte((psize << PROG_MASK_SHIFT) | bytes[psize]);
        //other bytes
        for (int i = psize + 1; i < bytes.length; ++i)
            out.writeByte(bytes[i]);
    }
    
    public double averageWeights() {
    	//Store x-bar in cost
    	//Store n in temp
    	//clearNodeTemps(); with 0 average
    	
    	double relSum = 0.0;
    	int relN = 0;
    	
    	for (Iterator nit = nodeIterator(); nit.hasNext(); )
            ((Node)nit.next()).clearTemp(0.0);
            
    	for (Iterator nit = nodeIterator(); nit.hasNext(); ) {
    		Node n = (Node)nit.next();
    		for (Iterator lit = n.values().iterator(); lit.hasNext(); ) {
    			Link l = (Link)lit.next();
    			Node n2 = l.not(n);
    			/*
    			if (n.temp > 0) { //already an average
    				n.cost = (n.cost*n.temp + l.weight)/(n.temp + 1);    				
    				n.temp++;
    			}
    			if (n2.temp > 0) {
    				//n2.cost = (n2.cost*n2.temp + l.weight)/(n2.temp + 1);
    				n2.temp++;
    			}*/
    			n.temp++;
    			n.cost += l.weight;
    			n2.temp++;
    			n2.cost += l.weight;
    			relN++;
    			relSum += l.weight;
    		}
    	}
    	
    	return relSum / relN;    	
    }
    
    public double avgavgPeers() {
    	double peerSum = 0.0;
    	int peerN = 0;
    	for (Iterator nit = nodeIterator(); nit.hasNext(); ) {
    		Node n = (Node)nit.next();
		if (n.temp > 0) {
    		    peerSum += n.cost / n.temp;
    		    peerN++;
		}
    	}
    	return peerSum / peerN;
    }
}
