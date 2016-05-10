/**
 * 
 */
package niagaCanvas;

import java.util.ArrayList;

/**
 * @author sebastian
 *
 */
public class Stream extends NiagarinoOperators {
	/**
	 * 
	 */
	private ArrayList<Stream> parentIDs;
	/**
	 * 
	 */
	private ArrayList<Stream> childIDs;
	/**
	 * An ArrayList of all operators encapsulated in this stream
	 */
	private ArrayList<Operator> operatorList;
	/**
	 * The first Operator in the Stream. This Operator has a fixed position at
	 * the top line of the Stream.
	 */
	private Operator start;

	/**
	 * The last Operator in the Stream. This Operator has a fixed position at
	 * the bottom line of the Stream.
	 */
	private Operator end;

	/**
	 * Constructor sets up a Stream Node.
	 * 
	 */
	public Stream(int x, int y) {
		this(x, y, (int) IDGenerator.createID());
	}

	/**
	 * Hidden constructor.
	 */
	private Stream(int x, int y, int createdID) {
		super(x, y, createdID);
		this.operatorList = new ArrayList<Operator>();
		this.name = "StreamID" + createdID;
		this.childIDs = new ArrayList<Stream>();
		this.parentIDs = new ArrayList<Stream>();
		this.start = null;
		this.end = null;
	}

	/**
	 * getter
	 * 
	 * @return The Operators that are enveloped by this Stream.
	 */
	public ArrayList<Operator> getOperatorList() {
		return operatorList;
	}

	/**
	 * Adds an Operator to this Stream.
	 * 
	 * @param operator
	 *            The Operator that should be added to the Stream.
	 */
	public void addOperator(Operator operator) {

		operator.setStreamID(this);
		if (!this.operatorList.contains(operator)) {
			System.out.println(operator.getID() + " Operator added to Stream: " + this.name);

			this.operatorList.add(operator);
		}
	}

	/**
	 * Removes an Operator to this Stream.
	 * 
	 * @param operator
	 *            The Operator that should be removed from this Stream.
	 */
	public void removeOperator(NiagarinoOperators operator) {
		this.operatorList.remove(operator);
	}

	/**
	 * Getter
	 * 
	 * @return the start
	 */
	public Operator getStart() {
		return start;
	}

	/**
	 * Setter
	 * 
	 * @param start
	 *            the start to set
	 */
	public void setStart(Operator start) {
		if (this.end == start)
			this.end = null;
		this.start = start;
	}

	/**
	 * Getter
	 * 
	 * @return the end
	 */
	public Operator getEnd() {
		return end;
	}

	/**
	 * Setter
	 * 
	 * @param end
	 *            the end to set
	 */
	public void setEnd(Operator end) {
		if (this.start == end)
			this.start = null;

		this.end = end;
	}

	/**
	 * Getter.
	 * 
	 * @return the parents of this Stream in an ArrayList
	 */
	public ArrayList<Stream> getParentIDs() {
		return this.parentIDs;
	}

	/**
	 * Getter.
	 * 
	 * @return the children of this Stream in an ArrayList
	 */
	public ArrayList<Stream> getChildIDs() {
		return childIDs;
	}

	/**
	 * delete one child.
	 * 
	 * @param childID
	 *            the childID to set
	 */
	public void removeChild(Stream node) {
		this.childIDs.remove(node);
	}

	/**
	 * delete one parent.
	 * 
	 * @param node
	 *            the parent to delete.
	 */
	public void removeParent(Stream node) {
		this.childIDs.remove(node);
	}

}
