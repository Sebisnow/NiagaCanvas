package ui.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ToNiaXML extends DefaultHandler {
	/**
	 * @param string
	 *            this is the Object to store the result in as a String
	 */
	private StringBuilder string;

	/**
	 * @param children
	 *            contains a list of children for each parent node
	 */
	private HashMap<Integer, ArrayList<Integer>> children;

	/**
	 * @param root
	 *            stores the current root when constructing the tree for xml
	 */
	private int root;

	/**
	 * @param ids
	 *            List of Lists which saves triples of the properties of xml:
	 *            id, parent, name for future reference and parent finding
	 * 
	 */
	private ArrayList<ArrayList<String>> ids;

	/**
	 * @param idMap
	 *            stores the data of each node or id
	 */
	private HashMap<Integer, ArrayList<String>> idMap;

	/**
	 * @param NAMES
	 *            stores the Names of the Operators in use
	 */

	private final List<String> NAMES = Arrays.asList("stream", "operator", "scan", "print", "window", "aggregate");

	/**
	 * @param edges
	 *            stores the edges with its source and target as tuple
	 */
	private HashMap<Integer, Integer> edges;

	public ToNiaXML(String xml) {
		super();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		try {
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(this);
			try (FileWriter fw = new FileWriter("tempfile.xml")) {
				fw.write(xml);
				fw.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}

			xmlReader.parse(convertToFileURL("tempfile.xml"));
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Before we start paring the document we initialize all variables
	 * 
	 */
	public void startDocument() throws SAXException {
		this.idMap = new HashMap<Integer, ArrayList<String>>();
		this.string = new StringBuilder();
		this.ids = new ArrayList<ArrayList<String>>();
		this.children = new HashMap<Integer, ArrayList<Integer>>();
		this.root = 0;
		this.edges = new HashMap<Integer, Integer>();
	}

	/**
	 * This is the main part of the sax parser where the vertex data is saved in
	 * the idMap and the edges are stored in edges
	 * 
	 */
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

		switch (localName) {
		case "mxCell":

			// find all edges and save them to the edges Map
			if (atts.getQName(0).equals("edge")) {
				this.edges.put(Integer.parseInt(atts.getValue("source")), Integer.parseInt(atts.getValue("target")));

			} else if (atts.getValue("value") == null) {
				// if there is no part in xml file with the name value do not
				// consider the part further
				break;

			} else {
				// Check if the current value is one of the operators saved in
				// NAMES if that is the case we save it's id, parent and the
				// Name in the idMap with the corresponding id
				for (String i : this.NAMES) {
					if (atts.getValue("value").equals(i)) {

						ArrayList<String> temp = new ArrayList<String>();
						temp.add(atts.getValue(0));
						temp.add(atts.getValue(1));
						temp.add(i);
						this.ids.add(temp);
						this.idMap.put(Integer.parseInt(atts.getValue(0)), temp);

					}

				}
			}
			break;

		case "mxGraphModel":

			ArrayList<String> temp = new ArrayList<String>();
			temp.add("1");
			temp.add("1");
			temp.add("plan");
			this.ids.add(temp);
			this.idMap.put(1, temp);

			break;

		case "root":
			this.string.append("<schema>");
			break;

		default:
			break;

		}

	}

	/**
	 * After the whole document has been parsed the new document can be created
	 * with the saved data and calls the mapChildren method to sort the nodes
	 * 
	 * @throws SAXException
	 *             throws a SAXException if a problem occurs to be caught by the
	 *             parser object
	 * 
	 */
	public void endDocument() throws SAXException {
		SortedSet<Integer> parents = new TreeSet<Integer>();
		for (int i : this.idMap.keySet()) {
			parents.add(Integer.parseInt(this.idMap.get(i).get(1)));
		}

		mapChildren(parents);

		if (!parents.isEmpty()) {
			recursion(parents.first());
		}
		this.string.append("</" + this.idMap.get(this.root).get(2) + ">");

		this.string.append("</schema>");

	}

	/**
	 * Maps all children to its Parent in a HashMap to conveniently access the
	 * children through the parent
	 * 
	 * @param parents
	 *            All unique parents occurring in the xml
	 */
	private void mapChildren(SortedSet<Integer> parents) {

		Iterator<Integer> iterator = parents.iterator();
		while (iterator.hasNext()) {
			int next = iterator.next();
			this.children.put(next, new ArrayList<Integer>());

		}
		// save the parent of every id in par and check if they are contained in
		// parent list

		for (int i : this.idMap.keySet()) {

			int par = Integer.parseInt(this.idMap.get(i).get(1));
			this.children.get(par).add(i);

		}

	}

	/**
	 * This method recursively traverses the tree nodes in a DFS way and adds
	 * the XML lines to the string that will be the output
	 * 
	 * @param parent
	 *            this is the current parent node
	 */
	public void recursion(int parent) {

		for (int i : this.children.get(parent)) {
			this.string.append(
					"<" + this.idMap.get(i).get(2) + " id=\"" + i + "\" parent=\"" + this.idMap.get(i).get(1) + "\">");

			if (this.children.containsKey(i) && i != parent) {
				recursion(i);

			} else if (i == parent) {
				this.root = i;
				continue;
			}

			this.string.append("</" + this.idMap.get(i).get(2) + ">");
		}

	}

	public String getString() {
		return this.string.toString();
	}

	private String convertToFileURL(String filename) {
		String path = new File(filename).getAbsolutePath();
		if (File.separatorChar != '/') {
			path = path.replace(File.separatorChar, '/');
		}

		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return "file:" + path;
	}

}
