package niagaCanvas;

import jkanvas.Canvas;
import jkanvas.painter.RenderpassPainter;

public class NiaGUI {

	private final EditorUI frame;
	private final Canvas c;

	public NiaGUI() {
		final RenderpassPainter p = new RenderpassPainter();
		this.c = new Canvas(p, 800, 600);
		this.c.setName("Origin");
		this.frame = new EditorUI(c);

		frame.setupGUI(p);

	}

}
