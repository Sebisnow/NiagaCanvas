package niagaCanvas;

import java.util.ArrayList;

import jkanvas.nodelink.IndexedPosition;

public abstract class NiagarinoOperators extends IndexedPosition {
	private int id;
	ArrayList<IndexedPosition> elements;
	public static final int INVALID = -1;

	protected String name;
	public boolean resizable;

	public NiagarinoOperators(double x, double y, int index) {
		super(x, y, index);
		this.id = index;

		this.elements = new ArrayList<IndexedPosition>();

		this.resizable = true;
	}

	public int getID() {
		return this.id;
	}

	/**
	 * @return the elements
	 */
	public ArrayList<IndexedPosition> getElements() {
		return this.elements;
	}

	/**
	 * @param elements
	 *            the elements to set
	 */
	public void setElements(ArrayList<IndexedPosition> elements) {
		this.elements = elements;
	}

	/**
	 * Getter
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
