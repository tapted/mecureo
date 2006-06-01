package DataStructures;

// BinaryHeap class
// Based on the DataStructures.BinaryHeap by Mark Allen Weiss
// with added dynamic resizing functionality by Trent apted
/**
 * Implements a binary heap.
 * Note that all "matching" is based on the compareTo of java.lang.Comparable
 * @author Mark Allen Weiss
 * @author Trent Apted
 */
public class BinaryHeap {
	public BinaryHeap( ) {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Construct the binary heap.
	 * @param capacity the INITIAL capacity of the binary heap.
	 */
	public BinaryHeap(int capacity) {
		currentSize = 0;
		array = new Comparable[capacity + 1];
	}

	/**
	 * Insert into the priority queue, maintaining heap order.
	 * Duplicates are allowed.
	 * @param x the item to insert.
	 * @exception Overflow if container is full.
	 */
	public void insert(Comparable x) {
		if(isFull())
			resize(); //changed 2002-04-22 THA
		// Percolate up
		int hole = ++currentSize;
		for(; hole > 1 && x.compareTo(array[hole / 2]) < 0; hole /= 2 )
			array[hole] = array[hole / 2];
		array[hole] = x;
	}

	/**
	 * Find the smallest item in the priority queue.
	 * @return the smallest item, or null, if empty.
	 */
	public Comparable findMin( ) {
		if(isEmpty())
			return null;
		return array[1];
	}

	/**
	 * Remove the smallest item from the priority queue.
	 * @return the smallest item, or null, if empty.
	 */
	public Comparable deleteMin( ) {
		if(isEmpty())
			return null;

		Comparable minItem = findMin( );
		array[1] = array[currentSize--];
		percolateDown(1);

		return minItem;
	}

	/**
	 * Establish heap order property from an arbitrary
	 * arrangement of items. Runs in linear time.
	 */
//	private void buildHeap( ) {
//		for(int i = currentSize / 2; i > 0; i--)
//			percolateDown( i );
//	}

	/**
	 * Test if the priority queue is logically empty.
	 * @return true if empty, false otherwise.
	 */
	public boolean isEmpty( ) {
		return currentSize == 0;
	}

	/**
	 * Test if the priority queue is logically full.
	 * @return true if full, false otherwise.
	 */
	public boolean isFull( ) {
		return currentSize == array.length - 1;
	}

	/**
	 * Make the priority queue logically empty.
	 */
	public void makeEmpty( ) {
		currentSize = 0;
	}

	private static final int DEFAULT_CAPACITY = 100;

	private int currentSize;	  // Number of elements in heap
	private Comparable [] array; // The heap array

	/**
	 * Internal method to percolate down in the heap.
	 * @param hole the index at which the percolate begins.
	 */
	private void percolateDown( int hole ) {
		int child;
		Comparable tmp = array[hole];

		for( ; hole * 2 <= currentSize; hole = child) {
			child = hole * 2;
			if(child != currentSize &&
					array[child + 1].compareTo(array[child] ) < 0 )
				child++;
			if(array[child].compareTo(tmp) < 0 )
				array[hole] = array[child];
			else
				break;
		}
		array[hole] = tmp;
	}

	/**
	 * Added 2002-04-22 THA.
	 * Double the size..
	 */
	private void resize() {
		Comparable[] tmp = new Comparable[array.length * 2];
		System.arraycopy(array, 0, tmp, 0, array.length);
		array = tmp;
	}
}