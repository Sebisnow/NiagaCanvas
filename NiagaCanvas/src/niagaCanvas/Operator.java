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
}
