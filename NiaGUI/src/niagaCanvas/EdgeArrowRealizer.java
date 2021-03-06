package niagaCanvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;

import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.EdgeRealizer;
import jkanvas.util.ArrowFactory;
import jkanvas.util.VecUtil;

public class EdgeArrowRealizer<T extends AnimatedPosition> implements EdgeRealizer<T> {

	@Override
	public Shape createLineShape(final T from, final T to) {

		/**
		 * Path2D path = (Path2D) PaintUtil.createLine(from.getX(), from.getY(),
		 * to.getX(), to.getY(), 1.0); TipType arrows = ArrowFactory.ARROW_FULL;
		 * ArrowFactory arrow = new ArrowFactory(arrows, arrows, 10.0);
		 * arrows.drawTip(path, to.getPos(), from.getPos());
		 * 
		 * // return arrow.createArrow(from.getPos(), to.getPos()); return
		 * PaintUtil.createLine(from.getX(), from.getY(), to.getX(), to.getY(),
		 * 1.0);
		 **/
		if (to == null || from == null)
			throw new ArrayIndexOutOfBoundsException();
		final ArrowFactory arrows = new ArrowFactory(ArrowFactory.ARROW_FULL, ArrowFactory.NONE, 10);
		Point2D f = from.getPos();
		Point2D t = to.getPos();
		Point2D diff = VecUtil.subVec(t, f);
		final Point2D d;
		if (to instanceof Stream) {
			Shape streamShape = new RectangularNodeRealizer<T>().createNodeShape((T) from, from.getX(), from.getY());

			double y = streamShape.getBounds2D().getMaxY();
			f.setLocation(from.getX(), y);
			streamShape = new RectangularNodeRealizer<T>().createNodeShape((T) to, to.getX(), to.getY());
			y = streamShape.getBounds2D().getMinY() - 2;
			t.setLocation(to.getX(), y);
			diff = VecUtil.subVec(t, f);
			d = VecUtil.setLength(diff, VecUtil.getLength(diff));
		} else {
			d = VecUtil.setLength(diff, VecUtil.getLength(diff) - DefaultNodeRealizer.RADIUS);
		}
		return arrows.createArrow(f, VecUtil.addVec(f, d));
	}

	/**
	 * Getter.
	 * 
	 * @param from
	 *            The starting node.
	 * @param to
	 *            The ending node.
	 * @return The color of the edge. The default implementation returns
	 *         {@link Color#BLACK}.
	 */
	public Color getColor(final T from, final T to) {
		return Color.BLACK;
	}

	@Override
	public void drawLines(final Graphics2D g, final Shape edgeShape, final T from, final T to) {

		g.setColor(getColor(from, to));
		g.fill(edgeShape);
		g.draw(edgeShape);

	}
}
