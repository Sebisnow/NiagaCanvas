package niagaCanvas;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.NodeRealizer;
import jkanvas.nodelink.layout.CircleLayouter;
import jkanvas.nodelink.layout.RandomLayouter;
import jkanvas.optional.MDSLayouter;
import jkanvas.painter.RenderpassPainter;

/**
 * A small node-link example application.
 * 
 * @author Sebastian
 */
public final class NodeLink extends DefaultNodeLinkRenderpass<AnimatedPosition> {

	/** The color for no selection. */
	private static final Color NO_SEL = new Color(247, 247, 247);

	/** The color for primary selection. */
	private static final Color PRIM_SEL = new Color(202, 0, 32);

	/** The color for secondary selection. */
	private static final Color SEC_SEL = new Color(5, 113, 176);

	/** The simple view in order to add nodes dynamically. */
	private final SimpleLayoutedView<AnimatedPosition> simpleView;

	/**
	 * Creates a node-link diagram.
	 * 
	 * @param view
	 *            The view on the graph.
	 */
	public NodeLink(final SimpleLayoutedView<AnimatedPosition> view, Canvas c) {
		super(view, c.getBounds());
		this.simpleView = view;

		setIds("nl");

	}

	@Override
	protected void processMessage(final String msg) {
		switch (msg) {
		case "random":
			simpleView.setLayouter(new RandomLayouter<>());
			break;
		case "circle":
			simpleView.setLayouter(new CircleLayouter<>());
			break;
		case "mds": {
			// estimate node size
			final Shape s = getNodeRealizer().createNodeShape(view.getNode(0), 0, 0);
			final double radius = s.getBounds2D().getWidth() * 0.5;
			simpleView.setLayouter(new MDSLayouter<>(radius));
			break;
		}
		}
	}

	/**
	 * Getter.
	 * 
	 * @param node
	 *            The node.
	 * @return The color of the node.
	 */
	public Color getNodeColor(final AnimatedPosition node) {
		return primSel == node ? PRIM_SEL : (secSel == node ? SEC_SEL : NO_SEL);
	}

	/** The primary selection. */
	private AnimatedPosition primSel;

	/** The Operatorlist of a Stream as ArrayList, for dragging. */
	private Map<AnimatedPosition, Integer[]> opList = new HashMap<AnimatedPosition, Integer[]>();

	/** The start x position of the current drag. */
	private double startX;

	/** The start y position of the current drag. */
	private double startY;

	/** The secondary selection. */
	private AnimatedPosition secSel;

	@Override
	public boolean click(final Camera cam, final Point2D pos, final MouseEvent e) {

		final AnimatedPosition n = pick(pos);
		if (SwingUtilities.isRightMouseButton(e)) {
			if (n == null) {
				// TODO could call a popup
				// if (e.isPopupTrigger()) {
				// addContextMenu(e, pos);
				// return false;
				// }
				return false;
			} else {

				if (secSel != null && secSel.getClass().equals(n.getClass())) {
					// when selecting a second node create edge if they are of
					// the same NiagarinoOperator Type
					simpleView.addEdge(secSel, n);
					secSel = null;
					primSel = null;
				} else if (secSel != null) {
					// clicked on a node and a node was selected already but
					// they are of different type. Reset selection and do
					// nothing.
					secSel = null;
					primSel = null;
					return false;
				} else {
					// on right click select node if nothing is selected already
					secSel = n;
					return true;
				}
			}
		}

		// left mouse Button pressed
		if (n == null) {
			if (secSel instanceof Stream) {
				// when a Stream is selected and nothing gets selected next
				// create Stream
				simpleView.addNode(new Stream((int) pos.getX(), (int) pos.getY()));
				primSel = null;
				secSel = null;
				return true;
			} else if (secSel instanceof Operator) {
				// when an Operator is selected and nothing gets selected next
				// create Operator
				// simpleView.addNode(new Operator((int) pos.getX(), (int)
				// pos.getY()));
				primSel = null;
				secSel = null;
				return true;
			}
			// no node selected, remove all possible selections anyway as
			// precaution
			primSel = null;
			secSel = null;
			return false;
		} else if (n instanceof Stream && secSel instanceof Operator) {
			// when an Operator is selected and a Stream gets selected next
			// create a new Operator
			simpleView.addNode(new Operator((int) pos.getX(), (int) pos.getY()));
			primSel = null;
			secSel = null;
			return true;
		} else {
			// e.g. stream selected and stream selected next on right click. Do
			// nothing but reset everything.
			secSel = null;
			primSel = null;
			return false;
		}
	}

	@Override
	public boolean doubleClick(final Camera cam, final Point2D pos, final MouseEvent e) {
		NiagarinoOperators node = (NiagarinoOperators) pick(pos);
		// On double right click remove the node
		if (SwingUtilities.isRightMouseButton(e) && node != null) {
			System.out.println(node.getID());
			this.simpleView.removeNode(node);
			primSel = null;
			secSel = null;
			return true;
		}

		// If double left click see if Stream or Operator and open specific
		// Option window
		if (node instanceof Stream) {

			OptionWindow window = new OptionWindow(new JFrame(), (Stream) node);
			window.setVisible(true);

			return true;
		} else if (node instanceof Operator) {

			OptionWindow window = new OptionWindow(new JFrame(), (Operator) node);
			window.setVisible(true);

			return true;
		}
		return false;

	}

	@Override
	public String getTooltip(final Point2D p) {
		final NiagarinoOperators n = (NiagarinoOperators) pick(p);
		if (n == null)
			return null;
		// TODO also return the name of the Node if there is a node at p
		// return NodeLink.getClassName(n)
		return n.name;

	}

	public static String getClassName(Object o) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf(".");
		return classString.substring(dotIndex + 1);
	}

	@Override
	public boolean acceptDrag(final Point2D p, final MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			return false;
		}
		final AnimatedPosition n = pick(p);
		if (n == null)
			return false;
		// initialize node dragging
		if (n instanceof Stream) {
			this.opList.clear();
			for (AnimatedPosition op : ((Stream) n).getOperatorList()) {
				Integer[] x = new Integer[2];
				x[0] = (int) op.getX();
				x[1] = (int) op.getY();
				this.opList.put(op, x);
				op.clearAnimation();
			}
		}
		primSel = n;
		primSel.clearAnimation();
		startX = primSel.getX();
		startY = primSel.getY();
		return true;
	}

	@Override
	public void drag(final Point2D start, final Point2D cur, final double dx, final double dy) {
		// Animate the content of a stream
		if (primSel instanceof Stream) {
			for (AnimatedPosition op : this.opList.keySet()) {
				op.setPosition(this.opList.get(op)[0] + dx, this.opList.get(op)[1] + dy);
			}
		}

		primSel.setPosition(startX + dx, startY + dy);
	}

	@Override
	public void endDrag(final Point2D start, final Point2D cur, final double dx, final double dy) {
		// draw shape around the position and check whether that shape
		// intersects the shape of another node

		final AnimatedPosition n = pickShape(cur);
		if (pick(start) instanceof Operator && !(n instanceof Stream)) {
			return;
		}

		if (n == null) {
			drag(start, cur, dx, dy);
			primSel = null;
		} else if (n instanceof Stream) {
			Stream stream = (Stream) n;
			if (pick(cur) instanceof Operator) {
				Operator startOperator = (Operator) pick(cur);

				// Add Operator to the Stream
				startOperator.setStreamID(stream);
				if (!stream.getOperatorList().contains(startOperator)) {
					System.out.println("Placed on Stream:");
					stream.addOperator(startOperator);
				}
				System.out.println(stream.getOperatorList());

				drag(start, cur, dx, dy);
				primSel = null;
			}
		}
	}

	@Override
	public void getBoundingBox(final RectangularShape bbox) {
		final NodeRealizer<AnimatedPosition> n = getNodeRealizer();
		bbox.setFrame(0, 0, 0, 0);
		for (final AnimatedPosition p : view.nodes()) {
			final double x = p.getX();
			final double y = p.getY();
			final Shape shape = n.createNodeShape(p, x, y);
			final Rectangle2D b = shape.getBounds2D();
			RenderpassPainter.addToRect(bbox, b);
			// include the end of the animation in the bounding box
			if (p.inAnimation()) {
				final double px = p.getPredictX();
				final double py = p.getPredictY();
				final Shape endShape = n.createNodeShape(p, px, py);
				RenderpassPainter.addToRect(bbox, endShape.getBounds2D());
			}
		}
	}
}
