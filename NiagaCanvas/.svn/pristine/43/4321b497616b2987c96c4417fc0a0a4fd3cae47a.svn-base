package deprecated;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPosition;
import jkanvas.io.json.JSONManager;
import jkanvas.nodelink.DefaultEdgeRealizer;
import jkanvas.painter.RenderpassPainter;
import jkanvas.util.Resource;
import niagaCanvas.CanvasSetup;
import niagaCanvas.EditorUI;
import niagaCanvas.JSONSetup;
import niagaCanvas.NodeLink;
import niagaCanvas.Operator;
import niagaCanvas.RectangularNodeRealizer;
import niagaCanvas.SimpleLayoutedView;
import niagaCanvas.Stream;

public class ProtoNia {

	public static EditorUI frame;
	public static String name;

	/**
	 * 
	 * this method starts the ui and sets up the canvas
	 */
	public static void createUI() {

		// configure Canvas
		final RenderpassPainter p = new RenderpassPainter();
		Canvas c = new Canvas(p, 800, 600);
		c.setName("Origin");
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setName("Scroll");
		scrollPane.setMinimumSize(new Dimension(100, 600));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, c);
		splitPane.setName("SplitPane");

		frame = new EditorUI(c);
		frame.add(splitPane);
		name = frame.name;
		CanvasSetup.setupCanvas(frame, c, p, true, true, false, false);

		final JSONManager mng = new JSONManager();
		try {
			JSONSetup.setupCanvas(frame, mng, Resource.getFor("nodelink.json"), true, false);
		} catch (IOException e1) {
			// Auto-generated catch block
			e1.printStackTrace();
		}
		final SimpleLayoutedView<AnimatedPosition> view = new SimpleLayoutedView<AnimatedPosition>(c, true);

		// creates a subclass of NodeLinkRenderpass
		// TODO
		NodeLink r = new NodeLink(view, c);

		r.setEdgeRealizer(new DefaultEdgeRealizer<AnimatedPosition>());
		r.setNodeRealizer(new RectangularNodeRealizer<AnimatedPosition>());

		// adds 2 nodes and connects them with an edge
		view.addNode(new Stream(50, 70));
		view.addNode(new Stream(200, 70));
		view.addNode(new Operator(600, 200));

		view.addNode(new Operator(400, 400));
		view.addEdge(0, 1);

		// The Renderpass renders/paints the picture
		RenderpassPainter pass;
		try {
			pass = mng.getForId("painter", RenderpassPainter.class);
			pass.addPass(r);
		} catch (IOException e1) {
			// TOO Auto-generated catch block
			e1.printStackTrace();
		}

		// frame.add(c);

	}

	public static void main(String[] args) {
		createUI();
	}

}
