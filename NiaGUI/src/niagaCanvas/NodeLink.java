package niagaCanvas;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	static final Color NO_SEL = new Color(247, 247, 247);

	/** The color for primary selection. */
	static final Color PRIM_SEL = new Color(202, 0, 32);

	/** The color for secondary selection. */
	static final Color SEC_SEL = new Color(5, 113, 176);

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
					this.view.setSecSel(null);
					primSel = null;
					this.view.setPrimSel(null);
					return true;
				} else if (secSel != null) {
					// clicked on a node and a node was selected already but
					// they are of different type. Reset selection and do
					// nothing.
					this.view.setSecSel(null);
					secSel = null;
					primSel = null;
					this.view.setPrimSel(null);
					return false;
				} else {
					// on right click select node if nothing is selected already

					this.view.setSecSel(n);
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
				this.view.setSecSel(null);
				primSel = null;
				secSel = null;
				this.view.setPrimSel(null);
				return true;
			} else if (secSel instanceof Operator) {
				// when an Operator is selected and nothing gets selected next
				// do nothing
				// simpleView.addNode(new Operator((int) pos.getX(), (int)
				// pos.getY()));
				this.view.setSecSel(null);
				primSel = null;
				secSel = null;
				this.view.setPrimSel(null);
				return true;
			}
			// no node selected, remove all possible selections anyway as
			// precaution
			this.view.setSecSel(null);
			primSel = null;
			secSel = null;
			this.view.setPrimSel(null);
			return false;
		} else if (n instanceof Stream && secSel instanceof Operator) {
			// when an Operator is selected and a Stream gets selected next
			// create a new Operator
			simpleView.addNode(new Operator((int) pos.getX(), (int) pos.getY()));

			this.view.setSecSel(null);
			primSel = null;
			secSel = null;
			this.view.setPrimSel(null);
			return true;
		} else {
			// e.g. stream selected and stream selected next on right click. Do
			// nothing but reset everything.

			this.view.setSecSel(null);
			secSel = null;
			primSel = null;
			this.view.setPrimSel(null);
			return false;
		}
	}

	@Override
	public boolean doubleClick(final Camera cam, final Point2D pos, final MouseEvent e) {
		NiagarinoOperators node = (NiagarinoOperators) pick(pos);
		// On double right click remove the node
		if (SwingUtilities.isRightMouseButton(e) && node != null) {
			this.simpleView.removeNode(node);
			primSel = null;
			this.view.setSecSel(null);
			secSel = null;
			this.view.setPrimSel(null);
			return true;
		}

		// If double left click see if Stream or Operator and open specific
		// Option window
		if (node instanceof Stream) {

			OptionWindow window = new OptionWindow((Stream) node, this.view, this);
			window.setVisible(true);

			return true;
		} else if (node instanceof Operator) {

			OptionWindow window = new OptionWindow((Operator) node, this.view);
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
		return n.name.contains("ID") ? Integer.toString(n.getID()) : n.name + ": " + "ID: " + n.getID();

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
			Stream stream = (Stream) n;
			this.opList.clear();
			for (Operator op : stream.getOperatorList()) {
				if (op.getStreamID() == stream.getID()) {
					Integer[] pos = new Integer[2];
					pos[0] = (int) op.getX();
					pos[1] = (int) op.getY();
					this.opList.put(op, pos);
					op.clearAnimation();
				}
			}
		} else if (n instanceof Operator) {
			Operator op = (Operator) n;
			Stream st = (Stream) this.view.getNode(op.getStreamID());
			if (st.getEnd() == op || st.getStart() == op) {
				return false;
			}
		}
		this.primSel = n;
		this.view.setPrimSel(n);
		this.primSel.clearAnimation();
		startX = this.primSel.getX();
		startY = this.primSel.getY();
		return true;
	}

	@Override
	public void drag(final Point2D start, final Point2D cur, final double dx, final double dy) {
		// Animate the content of a stream
		if (this.primSel instanceof Stream) {
			for (AnimatedPosition op : this.opList.keySet()) {
				op.setPosition(this.opList.get(op)[0] + dx, this.opList.get(op)[1] + dy);
			}
		}
		this.primSel.setPosition(startX + dx, startY + dy);
		AnimatedPosition node = this.pickShape(this.primSel.getPos());
		// System.out.println("Pos of PrimSel: " + this.primSel.getPos());
		if (node instanceof Stream) {
			this.secSel = node;
			this.view.setSecSel(node);
		} else {
			this.secSel = null;
			this.view.setSecSel(null);
		}
	}

	@Override
	public void endDrag(final Point2D start, final Point2D cur, final double dx, final double dy) {
		// draw shape around the position and check whether that shape
		// intersects the shape of another node
		start.setLocation(this.startX + dx, this.startY + dy);
		final AnimatedPosition st = this.pick(start);
		final AnimatedPosition end = this.pickShape(start);

		if (end instanceof Stream && st instanceof Operator) {
			Stream stream = (Stream) end;
			Operator operator = (Operator) st;
			// System.out.println(stream.getID() + " Stream " +
			// stream.getPos());
			// System.out.println(operator.getStreamID() + " operators StreamID
			// " + operator.getPos());
			//
			// System.out.println(this.primSel + " The primary selection");
			// System.out.println(this.secSel + " The Sec selection");

			// Add Operator to the Stream
			if (operator.getStreamID() != stream.getID()) {
				// System.out.println("Adding");
				((Stream) this.view.getNode(operator.getStreamID())).removeOperator(operator);

				stream.addOperator(operator);
				this.checkOperatorsStreamID();
			}
			// System.out.println(stream.getOperatorList());

			this.drag(start, cur, dx, dy);
		}
		this.primSel = null;

		this.view.setPrimSel(null);
		this.secSel = null;

		this.view.setSecSel(null);

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

	/**
	 * TODO unfinished Checks whether the node in question is part of a circle.
	 * USE RECURSION RATHER THAN LOOPS
	 * 
	 * @param node
	 *            the node to start from.
	 * @return True if the node is part of a circle, false otherwise.
	 */
	public boolean isCircle(Stream node) {
		int inval = NiagarinoOperators.INVALID;
		ArrayList<Stream> seenNodes = new ArrayList<Stream>();
		boolean b = true;
		while (b) {
			if (!seenNodes.contains(node)) {
				seenNodes.add(node);
				for (Stream child : node.getChildIDs()) {
					if (!seenNodes.contains(child))
						seenNodes.add(child);
				}
				// node = (NiagarinoOperators)
				// this.view.getNode(node.getChildID());

			} else {
				return true;
			}
		}
		return false;

	}

}
