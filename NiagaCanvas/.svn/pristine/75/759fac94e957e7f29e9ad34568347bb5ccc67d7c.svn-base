/**
 * 
 */
package niagaCanvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPosition;
import jkanvas.io.json.JSONManager;
import jkanvas.painter.RenderpassPainter;
import jkanvas.util.Resource;

/**
 * @author sebastian
 *
 */
public class EditorUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Canvas c;
	public String name;
	public SimpleLayoutedView<AnimatedPosition> view;
	/**
	 * @param schema
	 * 
	 */
	public ArrayList<String[]> schema;

	private JSplitPane splitPane;

	private RenderpassPainter pass;
	private EditorUI self;

	/**
	 * 
	 */
	public EditorUI(Canvas c) {
		this.schema = new ArrayList<String[]>();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.self = this;
		this.setTitle("starting...");

		this.c = c;
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setName("Scroll");
		scrollPane.setMinimumSize(new Dimension(100, 600));
		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, c);
		this.splitPane.setName("SplitPane");

		this.add(this.splitPane);

		this.setJMenuBar(this.createMenuBar(new JMenuBar()));
		this.pack();
		this.setVisible(true);
	}

	public JMenuBar createMenuBar(JMenuBar menuBar) {
		// create a Menu entry
		JMenu menu = new JMenu("Menu");

		// with subentries save . . . exit
		JMenuItem saveitem = new JMenuItem("Save");
		saveitem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new NiaXMLConverter(name, self);

				} catch (DataFormatException data) {
					System.out.println(data.getMessage());
					JOptionPane.showMessageDialog(self,
							"Please take care that a Schema and a Stream are specified and try again!");

				}
			}
		});
		saveitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));

		JMenuItem exititem = new JMenuItem("Exit");
		exititem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(1);
			}
		});
		exititem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, InputEvent.CTRL_MASK));

		menu.add(saveitem);
		menu.add(exititem);

		JMenuItem schemaitem = new JMenuItem("Edit Schema");
		EditorUI editor = this;
		schemaitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SchemaWindow(editor);
			}
		});
		schemaitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));

		menu.add(schemaitem);

		// add the created menu with its items to the menubar
		menuBar.add(menu);

		JMenu edit = new JMenu("Edit");
		JMenuItem stream = this.createStreamItem();
		stream.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		edit.add(stream);

		JMenuItem operatoritem = this.createOperatorItem();
		operatoritem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
		edit.add(operatoritem);

		JMenuItem removeEdge = new JMenuItem("Remove Edge");
		removeEdge.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new RemoveEdgeView(view);
			}

		});
		removeEdge.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		edit.add(removeEdge);

		menuBar.add(edit);

		menuBar.setBackground(new Color(200, 200, 200));
		menuBar.setVisible(true);
		return menuBar;

	}

	/**
	 * Generates A Menu Entry to create a new Stream
	 * 
	 * @return the menu Item
	 */
	public JMenuItem createStreamItem() {
		JMenuItem streamitem = new JMenuItem("New Stream");
		streamitem.setText("New Stream");
		streamitem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addNiagarinoOperator(new Stream(100, 200));
			}

		});
		return streamitem;
	}

	/**
	 * Generates A Menu Entry to create a new Operator
	 * 
	 * @return the menu Item
	 */
	public JMenuItem createOperatorItem() {
		JMenuItem operatoritem = new JMenuItem("New Operator");
		operatoritem.setText("Operator");

		operatoritem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addNiagarinoOperator(new Operator(300, 200));
			}

		});
		return operatoritem;
	}
	// TODO delete
	// public void clickNoNode(MouseEvent e, Point2D pos) {
	// JPopupMenu pop = new JPopupMenu();
	// JMenuItem stream = createStreamItem();
	// pop.add(stream);
	// pop.pack();
	// pop.setVisible(true);
	// pop.show(e.getComponent(), (int) pos.getX(), (int) pos.getY());
	//
	// }

	protected void setupGUI(RenderpassPainter p) {

		CanvasSetup.setupCanvas(this, this.c, p, true, true, false, false);

		final JSONManager mng = new JSONManager();
		try {
			JSONSetup.setupCanvas(this, mng, Resource.getFor("nodelink.json"), true, false);
		} catch (IOException e1) {
			// Auto-generated catch block
			e1.printStackTrace();
		}
		this.view = new SimpleLayoutedView<AnimatedPosition>(c, true);
		// creates a subclass of NodeLinkRenderpass
		// with it's help the Nodes get drawn after handing the NodeLink over to
		// a Renderpass

		NodeLink r = new NodeLink(this.view, c);

		r.setEdgeRealizer(new EdgeArrowRealizer<AnimatedPosition>());
		r.setNodeRealizer(new RectangularNodeRealizer<AnimatedPosition>() {

			@Override
			public Color getColor(final AnimatedPosition node) {
				return r.getNodeColor(node);
			}
		});
		// adds 3 nodes and connects some with an edge
		this.view.addNode(new Stream(200, 70));
		this.view.addNode(new Operator(600, 200));

		this.view.addNode(new Operator(400, 400));
		this.view.addEdge(1, 2);
		// The Renderpass renders/paints the picture

		try {
			this.pass = mng.getForId("painter", RenderpassPainter.class);
			this.pass.addPass(r);
		} catch (IOException e1) {
			// TOO Auto-generated catch block
			e1.printStackTrace();
		}

		// uncomment for start Window
		SchemaWindow s = new SchemaWindow(this);
		s.frame.setAutoRequestFocus(true);
		// frame.add(c);

	}

	public void addNiagarinoOperator(NiagarinoOperators op) {
		this.view = new SimpleLayoutedView<AnimatedPosition>(c, true);

		// creates a subclass of NodeLinkRenderpass
		NodeLink r = new NodeLink(this.view, this.c);
		r.setNodeRealizer(new RectangularNodeRealizer<AnimatedPosition>());
		this.view.addNode(op);
		this.pass.addPass(r);
		// TODO assign focus so that op is drawn right away not just
		// after clicking on canvas

	}

	public Canvas getC() {
		return c;
	}

	public void setC(Canvas c) {
		this.c = c;
	}

	public JSplitPane getSplitPane() {
		return splitPane;
	}

	public void setSplitPane(JSplitPane splitPane) {
		this.splitPane = splitPane;
	}
}
