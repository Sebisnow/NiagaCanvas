package niagaCanvas;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jkanvas.animation.AnimatedPosition;

public class RemoveEdgeView {

	/**
	 * @param attField
	 *            The Textfield where the schema can be edited in.
	 */
	private SimpleNodeLinkView<AnimatedPosition> view;

	/**
	 * Create the instance that creates the window.
	 * 
	 * @param editor
	 *            The EditorUI that saves the Schema
	 */
	public RemoveEdgeView(SimpleNodeLinkView<AnimatedPosition> view) {
		this.view = view;

		this.setUp();

	}

	/**
	 * This creates the frame and sets up the first part of the GUI where a
	 * nades can be specified for edgeRemove.
	 */
	private void setUp() {
		JFrame frame = new JFrame("Remove an Edge between Nodes");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 200));
		frame.setLocation(200, 200);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100, 400, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 80, 80, 80, 80 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };

		frame.getContentPane().setLayout(gridBagLayout);

		JTextField textField = new JTextField();
		textField.setEditable(true);
		textField.setPreferredSize(new Dimension(200, 40));
		textField.setName("Nodes");

		GridBagConstraints text = new GridBagConstraints();
		text.anchor = GridBagConstraints.SOUTHWEST;
		text.fill = GridBagConstraints.HORIZONTAL;
		text.insets = new Insets(5, 5, 5, 5);
		text.gridx = 1;
		text.gridy = 1;
		frame.getContentPane().add(textField, text);

		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(150, 40));
		label.setText("Enter the ID of the Nodes the Edge Connects:");

		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.SOUTH;
		gbc_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_label.insets = new Insets(5, 5, 5, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 0;
		frame.getContentPane().add(label, gbc_label);
		getButton(frame, textField);

		frame.pack();
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
	}

	private void getButton(JFrame frame, JTextField textField) {
		JButton button = new JButton("Remove");
		button.setFont(new Font("Dialog", Font.PLAIN, 15));
		button.setPreferredSize(new Dimension(80, 80));

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// try to parse the input in the text field, if anything goes
				// wrong catch it depending on the text input.
				try {
					parseString(textField);
					frame.setVisible(false);

				} catch (NumberFormatException bounds) {
					JOptionPane.showMessageDialog(frame, "Please Enter the NodeIDs as numbers divided by spaces!");
				} catch (IllegalAccessException illegal) {
					JOptionPane.showMessageDialog(frame, "Make sure you specified more than one NodeID!");
				} finally {
					textField.setText("NodeID NodeID");
				}
			}
		});

		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.NORTHWEST;
		gbc_button.insets = new Insets(0, 5, 5, 5);
		gbc_button.gridx = 1;
		gbc_button.gridy = 2;
		frame.getContentPane().add(button, gbc_button);
	}

	/**
	 * This method tries to parse the text input according to instructions and
	 * save it as the Schema for the EditorUi instance. If nothing was changed
	 * nothing is saved. If the format is not according to instructions
	 * (Name(String):Type(String) progressing(Integer)) Errors are thrown to be
	 * caught by calling method for handling.
	 * 
	 * @throws IllegalAccessException
	 *             Is thrown not enough ids are specified. Minimum is two.
	 * 
	 * @throws NumberFormatException
	 *             If NodeIDs are not specified as numbers.
	 */
	protected void parseString(JTextField textField) throws IllegalAccessException, NumberFormatException {
		String text = textField.getText();

		String[] substrings = text.split(" ");
		int[] ids = new int[substrings.length];
		int n = 0;
		for (String str : substrings) {

			// Throws NumberFormatException if not a Number
			ids[n] = Integer.parseInt(str);
			n++;
		}
		if (n != 2) {
			// not enough arguments/ids specified or too many
			throw new IllegalAccessException();
		} else {
			view.removeEdge(view.getNode(ids[0]), view.getNode(ids[1]));
		}
	}
}
