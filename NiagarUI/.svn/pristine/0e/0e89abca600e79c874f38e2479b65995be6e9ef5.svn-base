package ui;

import javax.swing.JFrame;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

public class HelloWorld extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	public HelloWorld() {
		super("Hello, World!");

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();

		Object[] o = new Object[5];
		Object o1 = graph.insertVertex(parent, null, "Hullo", 200, 20, 10, 30);
		Object o2 = graph.insertVertex(parent, null, "this", 100, 30, 10, 50);
		Object o0 = graph.insertVertex(parent, null, "is", 50, 10, 80, 30);
		Object o3 = graph.insertVertex(parent, null, "all", 300, 50, 30, 30);
		Object o4 = graph.insertVertex(parent, null, "it", 400, 60, 90, 30);
		o[0] = o0;
		o[1] = o1;
		o[2] = o2;
		o[3] = o3;
		o[4] = o4;

		Stream stream = new Stream("The Stream", new mxPoint(200, 100));
		Object v5 = graph.insertVertex(parent, null, "Hullo", 100, 20, 100, 30);
		Operator v4 = new Operator(graph.insertVertex(parent, null, "Again", 100, 20, 200, 300), stream);
		mxCell streamcell = new mxCell(v5, new mxGeometry(120, 40, 90, 40), "BASIC");
		Object group = graph.groupCells(v5, 10, o);

		graph.getModel().beginUpdate();
		try {
			Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80, 30);
			Object v2 = graph.insertVertex(parent, null, "World!", 240, 150, 80, 30);
			graph.insertEdge(parent, null, "Edge", v1, v2);
			System.out.println(graph.getOrigin().getX() + " " + graph.getOrigin().getY());
			v4.setPosition(200, 400);
		} finally {
			graph.getModel().endUpdate();
		}
		mxXmlUtils.getXml(v4);
		// mxCellRenderer.createVmlDocument(graph, cells, scale, background,
		// clip)
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		getContentPane().add(graphComponent);

		System.out.println("XML " + mxXmlUtils.getXml(v4));
	}

	public static void main(String[] args) {
		HelloWorld frame = new HelloWorld();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 320);
		frame.setVisible(true);
	}

}
