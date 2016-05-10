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

	private ArrayList<Operator> operatorList;

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
		if (!this.operatorList.contains(operator))
			this.operatorList.add(operator);
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

}
