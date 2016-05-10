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
	 * Constructor sets up a Stream Node
	 * 
	 */
	public Stream(int x, int y) {
		this(x, y, (int) IDGenerator.createID());
	}

	private Stream(int x, int y, int createdID) {
		super(x, y, createdID);
		this.operatorList = new ArrayList<Operator>();
		this.name = "StreamID" + createdID;

	}

	public ArrayList<Operator> getOperatorList() {
		return operatorList;
	}

	public void addOperator(Operator operator) {
		this.operatorList.add(operator);
	}

	public void removeOperator(NiagarinoOperators operator) {
		this.operatorList.remove(operator);
	}

}
