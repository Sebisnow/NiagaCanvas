/**
 * Copyright (c) 2006-2012, JGraph Ltd */
package ui;

import java.awt.Color;
import java.awt.Point;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

import ui.editor.BasicGraphEditor;
import ui.editor.EditorMenuBar;
import ui.editor.EditorPalette;

public class GraphEditor extends BasicGraphEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4601740824088314699L;

	/**
	 * Holds the shared number formatter.
	 * 
	 * @see NumberFormat#getInstance()
	 */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	/**
	 * Holds the URL for the icon to be used as a handle for creating new
	 * connections. This is currently unused.
	 */
	public static URL url = null;

	// GraphEditor.class.getResource("/com/mxgraph/examples/swing/images/connector.gif");

	public GraphEditor() {

		this("NiagarUI", new CustomGraphComponent(new CustomGraph()));

	}

	/**
	 * 
	 */
	public GraphEditor(String appTitle, mxGraphComponent component) {
		super(appTitle, component);

		final mxGraph graph = graphComponent.getGraph();

		// Creates the shapes palette
		EditorPalette shapesPalette = insertPalette(mxResources.get("shapes"));

		// Sets the edge template to be used for creating new edges if an edge
		// is clicked in the shape palette
		shapesPalette.addListener(mxEvent.SELECT, new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt) {
				Object tmp = evt.getProperty("transferable");

				if (tmp instanceof mxGraphTransferable) {
					mxGraphTransferable t = (mxGraphTransferable) tmp;
					Object cell = t.getCells()[0];

					if (graph.getModel().isEdge(cell)) {
						((CustomGraph) graph).setEdgeTemplate(cell);
					}
				}
			}

		});
		shapesPalette.addListener(mxEvent.CELLS_ADDED, new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt) {
				Object tmp = evt.getProperty("transferable");

				if (tmp instanceof mxGraphTransferable) {
					mxGraphTransferable t = (mxGraphTransferable) tmp;
					Object cell = t.getCells()[0];

					if (graph.getModel().isEdge(cell)) {
						((CustomGraph) graph).setEdgeTemplate(cell);
					}
				}
			}

		});
		// TODO
		// Add operator actions, variables, etc.
		// Adds some template cells for dropping into the graph
		shapesPalette.addTemplate("Stream",
				new ImageIcon(GraphEditor.class.getResource("/com/mxgraph/examples/swing/images/swimlane.png")),
				"swimlane", 280, 280, "stream");
		shapesPalette.addTemplate("Operator",
				new ImageIcon(GraphEditor.class.getResource("/com/mxgraph/examples/swing/images/rounded.png")),
				"icon;image=/com/mxgraph/examples/swing/images/wrench.png", 40, 40, "operator");
		shapesPalette.addTemplate("Scan",
				new ImageIcon(GraphEditor.class.getResource("/com/mxgraph/examples/swing/images/rounded.png")),
				"icon;image=/com/mxgraph/examples/swing/images/wrench.png", 40, 40, "scan");
		shapesPalette.addTemplate("Print",
				new ImageIcon(GraphEditor.class.getResource("/com/mxgraph/examples/swing/images/rounded.png")),
				"icon;image=/com/mxgraph/examples/swing/images/wrench.png", 40, 40, "print");
		shapesPalette.addTemplate("Window",
				new ImageIcon(GraphEditor.class.getResource("/com/mxgraph/examples/swing/images/swimlane.png")),
				"swimlane", 200, 200, "window");
		shapesPalette.addTemplate("Aggregate",
				new ImageIcon(GraphEditor.class.getResource("/com/mxgraph/examples/swing/images/swimlane.png")),
				"swimlane", 200, 200, "aggregate");

	}

	/**
	 * 
	 */
	public static class CustomGraphComponent extends mxGraphComponent {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6833603133512882012L;

		/**
		 * 
		 * @param graph
		 */
		public CustomGraphComponent(mxGraph graph) {
			super(graph);

			// Sets switches typically used in an editor
			setPageVisible(true);
			setGridVisible(true);
			setToolTips(true);
			getConnectionHandler().setCreateTarget(true);

			// Loads the defalt stylesheet from an external file
			mxCodec codec = new mxCodec();
			Document doc = mxUtils.loadDocument(GraphEditor.class
					.getResource("/com/mxgraph/examples/swing/resources/default-style.xml").toString());
			codec.decode(doc.getDocumentElement(), graph.getStylesheet());

			// Sets the background to white
			getViewport().setOpaque(true);
			getViewport().setBackground(Color.WHITE);
		}

		/**
		 * Overrides drop behaviour to set the cell style if the target is not a
		 * valid drop target and the cells are of the same type (eg. both
		 * vertices or both edges).
		 */
		public Object[] importCells(Object[] cells, double dx, double dy, Object target, Point location) {
			if (target == null && cells.length == 1 && location != null) {
				target = getCellAt(location.x, location.y);

				if (target instanceof mxICell && cells[0] instanceof mxICell) {
					mxICell targetCell = (mxICell) target;
					mxICell dropCell = (mxICell) cells[0];

					if (targetCell.isVertex() == dropCell.isVertex() || targetCell.isEdge() == dropCell.isEdge()) {
						mxIGraphModel model = graph.getModel();
						model.setStyle(target, model.getStyle(cells[0]));
						graph.setSelectionCell(target);

						return null;
					}
				}
			}

			return super.importCells(cells, dx, dy, target, location);
		}

	}

	/**
	 * A graph that creates new edges from a given template edge.
	 */
	public static class CustomGraph extends mxGraph {
		/**
		 * Holds the edge to be used as a template for inserting new edges.
		 */
		protected Object edgeTemplate;

		/**
		 * Custom graph that defines the alternate edge style to be used when
		 * the middle control point of edges is double clicked (flipped).
		 */
		public CustomGraph() {
			setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		}

		/**
		 * Sets the edge template to be used to inserting edges.
		 */
		public void setEdgeTemplate(Object template) {
			edgeTemplate = template;
		}

		/**
		 * Prints out some useful information about the cell in the tooltip.
		 */
		public String getToolTipForCell(Object cell) {
			String tip = "<html>";
			mxGeometry geo = getModel().getGeometry(cell);
			mxCellState state = getView().getState(cell);

			if (getModel().isEdge(cell)) {
				tip += "points={";

				if (geo != null) {
					List<mxPoint> points = geo.getPoints();

					if (points != null) {
						Iterator<mxPoint> it = points.iterator();

						while (it.hasNext()) {
							mxPoint point = it.next();
							tip += "[x=" + numberFormat.format(point.getX()) + ",y=" + numberFormat.format(point.getY())
									+ "],";
						}

						tip = tip.substring(0, tip.length() - 1);
					}
				}

				tip += "}<br>";
				tip += "absPoints={";

				if (state != null) {

					for (int i = 0; i < state.getAbsolutePointCount(); i++) {
						mxPoint point = state.getAbsolutePoint(i);
						tip += "[x=" + numberFormat.format(point.getX()) + ",y=" + numberFormat.format(point.getY())
								+ "],";
					}

					tip = tip.substring(0, tip.length() - 1);
				}

				tip += "}";
			} else {
				tip += "geo=[";

				if (geo != null) {
					tip += "x=" + numberFormat.format(geo.getX()) + ",y=" + numberFormat.format(geo.getY()) + ",width="
							+ numberFormat.format(geo.getWidth()) + ",height=" + numberFormat.format(geo.getHeight());
				}

				tip += "]<br>";
				tip += "state=[";

				if (state != null) {
					tip += "x=" + numberFormat.format(state.getX()) + ",y=" + numberFormat.format(state.getY())
							+ ",width=" + numberFormat.format(state.getWidth()) + ",height="
							+ numberFormat.format(state.getHeight());
				}

				tip += "]";
			}

			mxPoint trans = getView().getTranslate();

			tip += "<br>scale=" + numberFormat.format(getView().getScale()) + ", translate=[x="
					+ numberFormat.format(trans.getX()) + ",y=" + numberFormat.format(trans.getY()) + "]";
			tip += "</html>";

			return tip;
		}

		/**
		 * Overrides the method to use the currently selected edge template for
		 * new edges.
		 * 
		 * @param graph
		 * @param parent
		 * @param id
		 * @param value
		 * @param source
		 * @param target
		 * @param style
		 * @return
		 */
		public Object createEdge(Object parent, String id, Object value, Object source, Object target, String style) {
			if (edgeTemplate != null) {
				mxCell edge = (mxCell) cloneCells(new Object[] { edgeTemplate })[0];
				edge.setId(id);

				return edge;
			}

			return super.createEdge(parent, id, value, source, target, style);
		}

	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		mxSwingConstants.SHADOW_COLOR = Color.WHITE;
		mxConstants.W3C_SHADOWCOLOR = "#535353";

		GraphEditor editor = new GraphEditor();

		editor.createFrame(new EditorMenuBar(editor)).setVisible(true);
	}
}
