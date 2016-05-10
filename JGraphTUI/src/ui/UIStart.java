package ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

public class UIStart {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIStart window = new UIStart();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public UIStart() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(5);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		frame.setJMenuBar(getMenu());
	}

	/**
	 * Method to create the Menu with shortcuts and all
	 * 
	 * @return menu returns the desired and created menu bar object
	 */

	private JMenuBar getMenu() {
		JMenuBar menubar = new JMenuBar();
		ImageIcon icon = new ImageIcon("exit.png");

		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

		JMenuItem eMenuItem = new JMenuItem("Import XML", icon);
		eMenuItem.setActionCommand("importXml");
		eMenuItem.setMnemonic(KeyEvent.VK_O);
		eMenuItem.setToolTipText("Exit application");
		eMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});

		file.add(eMenuItem);
		menubar.add(file);

		JMenuItem menuItem_2 = new JMenuItem("Save as XML", (Icon) null);
		menuItem_2.setActionCommand("SaveXml");
		menuItem_2.setToolTipText("Exit application");
		menuItem_2.setMnemonic(KeyEvent.VK_A);
		file.add(menuItem_2);

		file.addSeparator();

		JMenuItem menuItem_3 = new JMenuItem("Exit", (Icon) null);
		menuItem_3.setToolTipText("Exit application");
		menuItem_3.setMnemonic(KeyEvent.VK_E);
		file.add(menuItem_3);

		JMenu menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_F);
		menubar.add(menu);

		JMenuItem menuItem = new JMenuItem("Copy", (Icon) null);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		menuItem.setActionCommand("copy");
		menuItem.setToolTipText("Exit application");
		menuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(menuItem);

		JMenu menu_1 = new JMenu("Help");
		menu_1.setMnemonic(KeyEvent.VK_F);
		menubar.add(menu_1);

		JMenuItem menuItem_1 = new JMenuItem("About", (Icon) null);
		menuItem_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menuItem_1.setToolTipText("Exit application");
		menuItem_1.setMnemonic(KeyEvent.VK_E);
		menu_1.add(menuItem_1);
		return menubar;
	}

}
