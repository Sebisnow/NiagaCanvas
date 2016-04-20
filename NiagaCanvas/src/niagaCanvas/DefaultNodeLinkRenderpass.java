package niagaCanvas;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationList;
import jkanvas.nodelink.EdgeRealizer;
import jkanvas.nodelink.NodeLinkView;
import jkanvas.nodelink.NodeRealizer;
import jkanvas.nodelink.layout.LayoutedView;
import jkanvas.painter.Renderpass;

/**
 * Paints a layouted node-link diagram.
 * 
 * @author Sebastian
 * @param <T>
 *            The type of nodes.
 */
public class DefaultNodeLinkRenderpass<T extends AnimatedPosition> extends Renderpass {

	/** The bounding box. */
	private final Rectangle2D bbox;

	/** The layout view if any. */
	private final LayoutedView<T> layout;

	/** The view on the graph. */
	protected final SimpleNodeLinkView<T> view;

	/** The node realizer. */
	private NodeRealizer<T> nodeRealizer;
	/** The node realizer. */

	/** The edge realizer. */
	private EdgeRealizer<T> edgeRealizer;

	/** The animation list. */
	private AnimationList list;

	/**
	 * Creates a node-link painter.
	 * 
	 * @param view
	 *            The view on the graph.
	 */
	@SuppressWarnings("unchecked")
	public DefaultNodeLinkRenderpass(final LayoutedView<T> view) {
		this.view = (SimpleNodeLinkView<T>) Objects.requireNonNull(view);
		layout = view;
		bbox = null;
	}

	/**
	 * Creates a node-link painter.
	 * 
	 * @param view
	 *            The view on the graph.
	 * @param bbox
	 *            The bounding box of the render pass.
	 */
	@SuppressWarnings("unchecked")
	public DefaultNodeLinkRenderpass(final NodeLinkView<T> view, final Rectangle2D bbox) {
		this.view = (SimpleNodeLinkView<T>) Objects.requireNonNull(view);
		this.bbox = Objects.requireNonNull(bbox);
		layout = null;
	}

	/**
	 * Setter.
	 * 
	 * @param edgeRealizer
	 *            The edge realizer.
	 */
	public void setEdgeRealizer(final EdgeRealizer<T> edgeRealizer) {
		this.edgeRealizer = Objects.requireNonNull(edgeRealizer);
	}

	/**
	 * Getter.
	 * 
	 * @return The current edge realizer.
	 */
	public EdgeRealizer<T> getEdgeRealizer() {
		return edgeRealizer;
	}

	/**
	 * Setter.
	 * 
	 * @param nodeRealizer
	 *            The node realizer.
	 */
	public void setNodeRealizer(final NodeRealizer<T> nodeRealizer) {
		this.nodeRealizer = Objects.requireNonNull(nodeRealizer);
	}

	/**
	 * Getter.
	 * 
	 * @return The current node realizer.
	 */
	public NodeRealizer<T> getNodeRealizer() {
		return nodeRealizer;
	}

	@Override
	public void draw(final Graphics2D gfx, final KanvasContext ctx) {
		renderEdges(gfx, ctx);
		renderNodes(gfx, ctx);
	}

	/** The node set. This set is used to detect whether a node is new. */
	private final Set<T> lastNodes = Collections.newSetFromMap(new IdentityHashMap<T, Boolean>());

	/**
	 * Renders all nodes and differentiates between Streams and Operators.
	 * 
	 * @param gfx
	 *            The graphics context.
	 * @param ctx
	 *            The canvas context.
	 */
	@SuppressWarnings("unchecked")
	private void renderNodes(final Graphics2D gfx, final KanvasContext ctx) {
		final Rectangle2D visible = ctx.getVisibleCanvas();
		final RectangularNodeRealizer<T> streamRealizer = new RectangularNodeRealizer<T>();
		final DefaultNodeRealizer<T> operatorRealizer = new DefaultNodeRealizer<T>();
		int count = 0, secCount = 0;

		this.checkOperatorsStreamID();

		// no Stream on Canvas, create one.
		if (view.getStreams().size() == 0) {
			view.addNode((T) new Stream(200, 200));
		}
		this.checkStreams();
		for (final Stream node : view.getStreams()) {
			++count;
			// automatically adds new nodes to the animation list
			// this needs only to be done in the draw method
			if (!lastNodes.contains(node)) {
				list.addAnimated(node);
				lastNodes.add((T) node);
			}
			final double x = node.getX();
			final double y = node.getY();
			final Shape nodeShape = streamRealizer.createNodeShape((T) node, x, y);
			if (!nodeShape.intersects(visible)) {
				--count;
				continue;
			}
			final Graphics2D g = (Graphics2D) gfx.create();

			g.setComposite(AlphaComposite.getInstance(10, 0.5f));
			streamRealizer.drawNode(g, (T) node);
			g.dispose();
		}

		for (final Operator node : view.getOperators()) {
			// If the node is not on a Stream put it on a Stream.
			ArrayList<Stream> intersectList = new ArrayList<Stream>();
			for (final Stream stream : view.getStreams()) {
				final Shape nodeShape = streamRealizer.createNodeShape((T) stream, stream.getX(), stream.getY());
				if (nodeShape.contains(node.getPos())) {
					intersectList.add(stream);
				}
			}
			if (intersectList.size() > 0) {
				// There is no valid StreamID set for this node although it
				// intersects a Stream, then take any of the intersecting
				// Streams and add it.
				if (!intersectList.contains(view.getNode(node.getStreamID()))) {
					node.setStreamID(intersectList.get(0));
					intersectList.get(0).addOperator(node);

				}
				++secCount;

				// automatically adds new nodes to the animation list
				// this needs only to be done in the draw method
				if (!lastNodes.contains(node)) {
					list.addAnimated(node);
					lastNodes.add((T) node);
				}
				final double x = node.getX();
				final double y = node.getY();
				final Shape nodeShape = operatorRealizer.createNodeShape((T) node, x, y);
				if (!nodeShape.intersects(visible)) {
					continue;
				}
				final Graphics2D g = (Graphics2D) gfx.create();

				g.setComposite(AlphaComposite.getInstance(2, 1.0f));
				operatorRealizer.drawNode(g, (T) node);

				g.dispose();
			} else {
				// There is no Stream intersecting this Operator, move it.
				Stream stream = view.getStreams().get(0);
				node.setStreamID(stream);
				stream.addOperator(node);
				node.setPosition(stream.getPos());
			}

		}
		if (count + secCount < lastNodes.size()) {
			// clear set when nodes got removed
			lastNodes.clear();
		}
	}

	/**
	 * Helper Method, that checks the OperatorLists of all Streams in this view
	 * to make sure there are no Operators on more than one Stream.
	 * 
	 */
	private void checkStreams() {
		for (Stream st : this.view.getStreams()) {
			for (Stream secSt : this.view.getStreams()) {
				if (st != secSt)
					this.checkOperators(secSt, st);
			}
		}
	}

	/**
	 * Helper Method, that compares the Operators in two Streams and deletes the
	 * Operator from one Stream if it is contained in both. If the operator that
	 * is in both Streams has a StreamID set it is deleted from the other stream
	 * and kept in the Stream the Operator belongs to.
	 * 
	 */
	private void checkOperators(Stream fstStream, Stream secStream) {
		for (Operator op : fstStream.getOperatorList()) {
			if (secStream.getOperatorList().contains(op)) {
				if (this.view.getNode(op.getStreamID()) == fstStream) {
					secStream.getOperatorList().remove(op);
				} else {
					op.setStreamID(fstStream);
					fstStream.getOperatorList().remove(op);
				}
			}
		}
	}

	/**
	 * Small helper method to convert doubles to ints.
	 * 
	 * @param x
	 *            the double value to convert to an integer.
	 * @return the integer value of x.
	 */
	@SuppressWarnings("unused")
	private int getInt(double x) {
		return (int) x;
	}

	/**
	 * Renders all edges.
	 * 
	 * @param gfx
	 *            The graphics context.
	 * @param ctx
	 *            The canvas context.
	 */
	private void renderEdges(final Graphics2D gfx, final KanvasContext ctx) {
		final Rectangle2D visible = ctx.getVisibleCanvas();
		final EdgeRealizer<T> edgeRealizer = getEdgeRealizer();
		for (final T from : this.view.nodes()) {
			for (final int toId : this.view.edgesFrom(this.view.getId(from))) {

				/*
				 * Check whether Node exists if not remove from edges set of
				 * from node. TO DO There is an issue here with directed edges
				 * if you remove the node that is the target an error is thrown.
				 * Maybe there is a check too much. Correct in
				 * SimpleNodeLinkView.
				 */
				if (this.view.getNode(toId) != null) {
					final T to = this.view.getNode(toId);

					final Shape edgeShape = edgeRealizer.createLineShape(from, to);

					if (!edgeShape.intersects(visible)) {
						continue;
					}
					final Graphics2D g = (Graphics2D) gfx.create();
					g.setComposite(AlphaComposite.getInstance(2, 0.8f));
					edgeRealizer.drawLines(g, edgeShape, from, to);
					g.dispose();
				} else {
					this.view.removeEdge(this.view.getId(from), toId);
				}

			}
		}
	}

	/**
	 * Finds a node at the given position.
	 * 
	 * @param pos
	 *            The position.
	 * @return The node or <code>null</code> if there is no node at the given
	 *         position. Returns Operator if there is an Operator and a Stream.
	 */
	public T pick(final Point2D pos) {
		final RectangularNodeRealizer<T> streamRealizer = new RectangularNodeRealizer<T>();
		final DefaultNodeRealizer<T> operatorRealizer = new DefaultNodeRealizer<T>();
		T cur = null, op = null;
		for (final T node : view.nodes()) {
			final double x = node.getX(), y = node.getY();
			Shape shape = streamRealizer.createNodeShape(node, x, y);
			if (node instanceof Operator) {
				shape = operatorRealizer.createNodeShape(node, x, y);
				if (shape.contains(pos)) {
					// return the last match -- ie topmost node
					op = node;
				}
			}

			if (shape.contains(pos)) {
				// return the last match -- ie topmost node
				cur = node;
			}
		}
		return op != null ? op : cur;
	}

	/**
	 * Finds a submerged node at the given position, if no node is underneath
	 * another the only node is returned or null if there is non at all.
	 * 
	 * @param pos
	 *            The position.
	 * @return The node or <code>null</code> if there is no node at the given
	 *         position.
	 */
	@SuppressWarnings("unused")
	public T pickSub(final Point2D pos) {
		final NodeRealizer<T> nodeRealizer = getNodeRealizer();
		T cur = null;
		T last = null;
		for (final T node : view.nodes()) {
			final double x = node.getX();
			final double y = node.getY();
			final Shape shape = nodeRealizer.createNodeShape(node, x, y);
			if (shape.contains(pos)) {
				// return the last match -- ie topmost node
				if (last != null) {
					last = node;
				}
				cur = node;
			}
		}
		return last;
	}

	/**
	 * draw shape around the position and check whether that shape intersects
	 * the shape of another node
	 * 
	 * @param pos
	 *            The position.
	 * @return The node or <code>null</code> if there is no node at the given
	 *         position.
	 */
	@SuppressWarnings("unused")
	public T pickShape(final Point2D pos) {
		final NodeRealizer<T> nodeRealizer = getNodeRealizer();
		final Shape shapeNode = nodeRealizer.createNodeShape(pick(pos), pos.getX(), pos.getY());

		T cur = null;
		T last = null;
		for (final T node : view.nodes()) {
			final Shape shape = nodeRealizer.createNodeShape(node, node.getX(), node.getY());
			if (shape.intersects(shapeNode.getBounds2D())) {
				// return the first match -- ie bottommost node
				if (last == null) {
					last = node;
				}
				cur = node;
			}
		}

		return last;
	}

	/**
	 * Checks whether all Operators have a Stream ID
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void checkOperatorsStreamID() {
		final RectangularNodeRealizer<T> streamRealizer = new RectangularNodeRealizer<T>();
		for (Operator op : this.view.getOperators()) {
			if (op.getStreamID() != niagaCanvas.NiagarinoOperators.INVALID) {
				for (final Stream stream : view.getStreams()) {
					final Shape nodeShape = streamRealizer.createNodeShape((T) stream, stream.getX(), stream.getY());
					if (nodeShape.contains(op.getPos())) {
						op.setStreamID(stream);
						stream.addOperator(op);
					}
				}
			} else {

			}
		}
	}

	@Override
	public void setAnimationList(final AnimationList list) {
		this.list = Objects.requireNonNull(list);
	}

	/**
	 * Getter.
	 * 
	 * @return The animation list.
	 */
	protected AnimationList getAnimationList() {
		return list;
	}

	@Override
	public boolean isChanging() {
		for (final T node : view.nodes()) {
			if (node.inAnimation())
				return true;
		}
		return false;
	}

	@Override
	public void getBoundingBox(final RectangularShape rect) {
		if (layout == null) {
			rect.setFrame(bbox);
		} else {
			layout.getBoundingBox(rect);
		}
	}

}
