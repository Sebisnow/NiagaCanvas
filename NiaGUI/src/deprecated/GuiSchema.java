package deprecated;

import java.util.HashMap;

public class GuiSchema {

	/**
	 * @param attributes
	 *            The Attributes that occur in the Schema as Map Keys are their
	 *            names.
	 */
	private HashMap<String, String[]> attributes;

	/**
	 * Constructor initializes attributes
	 */
	public GuiSchema() {
		this.attributes = new HashMap<String, String[]>();
	}

	/**
	 * Adds an Attribute at the end of the Schema.
	 * 
	 * @param ob
	 *            The Object to add as the next (last) attribute of the Schema.
	 */
	public void addAttribute(String attName, String[] values) {
		this.attributes.put(attName, values);
	}

	/**
	 * Return the HashMap that defines the Schema.
	 * 
	 * @return returns the ArrayList of Objects that defines the Schema.
	 */
	public HashMap<String, String[]> getSchema() {
		return this.attributes;
	}

}
