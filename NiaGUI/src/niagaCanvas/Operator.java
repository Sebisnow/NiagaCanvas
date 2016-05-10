/**
 * 
 */
package niagaCanvas;

import java.util.ArrayList;
import java.util.HashMap;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.layout.SimpleLayoutedView;
import niagarino.operator.AbstractOperator;

/**
 * @author sebastian
 * @param <T>
 *
 */
/**
 * @author sebastian
 *
 */
public class Operator extends NiagarinoOperators {

	/** The possible classes of the Operator of Niagarino */
	// public static String[] OPERATORCLASS = { "BernoulliSampling",
	// "BernoulliSamplingEfficient", "CrossProduct",
	// "Derive", "FileIterator", "Frame", "KSorter", "Limit", "Multiplex",
	// "NoOp", "OrderedAggregate", "Print",
	// "Priority", "ProgressingMerge", "Punctuator", "ReservoirSampling",
	// "SamplingOperator", "Scan",
	// "SegmentedStorageRead", "SegemntedStorageWrite", "Selection",
	// "SequentialFileIterator", "SourceOperator",
	// "StratifiedSamplingOperator", "TopK", "TupleListIterator", "TupleWindow",
	// "ValueWindow", "XMLParser" };
	private Class<? extends AbstractOperator> opClass;

	// TODO can probably be removed:
	public boolean modified;
	/**
	 * Is used to store the different properties of an Operator
	 * 
	 * @param properties
	 *            properties of the Operator in <parameter description,
	 *            parameter> notation.
	 */
	public HashMap<String, String> properties;

	/**
	 * Similar to properties but in a different Format to make converting to XML
	 * easier.
	 * 
	 * @param attributes
	 *            properties of the Operator in <parameter description,
	 *            parameter> notation.
	 */
	public HashMap<String, ArrayList<String>> attributes;
	/** The ID of the Parent Node */
	private int parentID;
	/** The ID of the Child Node */
	private int childID;
	/** The ID of the Stream Node containing this Operator */
	private int streamID;

	/**
	 * Constructor sets up a Operator Node
	 * 
	 */
	public Operator(int x, int y) {
		this(x, y, (int) IDGenerator.createID());
	}

	/**
	 * Constructor sets up a Operator Node
	 * 
	 */
	public Operator(int x, int y, int createID) {
		super(x, y, createID);
		this.properties = new HashMap<String, String>();
		// this.properties.put("none",["ID", String.valueOf(this.getID())]);
		this.attributes = new HashMap<String, ArrayList<String>>();
		this.name = "OperatorID" + createID;
		this.parentID = NiagarinoOperators.INVALID;
		this.childID = NiagarinoOperators.INVALID;
		this.streamID = NiagarinoOperators.INVALID;
		this.modified = false;
	}

	@SuppressWarnings("unchecked")
	public static void addOperator(Canvas c, int x, int y) {
		SimpleLayoutedView<AnimatedPosition> lay = (SimpleLayoutedView<AnimatedPosition>) c.getLayout();

		lay.addNode(new Operator(x, y));
		c.refresh();

	}

	public Class<? extends AbstractOperator> getOpClass() {
		return opClass;
	}

	public void setOpClass(Class<? extends AbstractOperator> cl) {
		this.opClass = cl;
	}

	/**
	 * getter
	 * 
	 * @return the NiagarinoOperators ID
	 */
	public int getParentID() {
		return parentID;
	}

	/**
	 * setter Sets the NiagarinoOperators ID of the parent.
	 */
	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	/**
	 * getter
	 * 
	 * @return the NiagarinoOperators ID
	 */
	public int getChildID() {
		return childID;
	}

	/**
	 * setter Sets the NiagarinoOperators ID of the parent.
	 */
	public void setChildID(int childID) {
		this.childID = childID;
	}

	public int getStreamID() {
		return this.streamID;
	}

	public void setStreamID(Stream parent) {
		this.streamID = parent.getID();
	}

	/**
	 * setter Sets the StreamID of the Operator to Invalid.
	 */
	public void deleteStreamID() {
		this.streamID = NiagarinoOperators.INVALID;
	}

}
