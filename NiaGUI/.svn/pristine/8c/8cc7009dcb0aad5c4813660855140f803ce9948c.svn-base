package niagaCanvas;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * @author sebastian
 *
 */
public class SchemaWindow {

	/**
	 * @param name
	 *            The name that the Schema should get.
	 */
	public String name;

	/**
	 * @param frame
	 *            The frame that the schema can be created in.
	 */
	public JFrame frame;

	/**
	 * @param editor
	 *            The Editor instance in which the Schema will be saved and
	 *            accessible.
	 */
	private EditorUI editor;

	/**
	 * @param attField
	 *            The Textfield where the schema can be edited in.
	 */
	private JTextField attField;

	/**
	 * Create the instance that creates the window.
	 * 
	 * @param editor
	 *            The EditorUI that saves the Schema
	 */
	public SchemaWindow(EditorUI editor) {
		this.editor = editor;

		this.createSchemaWindow();

	}

	/**
	 * @method This method creates the window and delegates its jobs.
	 * 
	 */
	private void createSchemaWindow() {
		// set up the frame and the name field
		JTextField nameField = this.setUp();

		// set up the attribute Text field
		this.attributeField();

		JButton button = new JButton("Start");
		button.setFont(new Font("Dialog", Font.PLAIN, 15));
		button.setPreferredSize(new Dimension(80, 80));

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.setTitle(nameField.getText());
				editor.name = nameField.getText();

				// try to parse the input in the text field, if anything goes
				// wrong catch it depending on the text input.
				try {
					if (!editor.schema.isEmpty()) {
						editor.schema.removeAll(editor.schema);
					}
					parseString();
					frame.setVisible(false);

				} catch (IllegalArgumentException except) {
					JOptionPane.showMessageDialog(frame, "Please take " + "care that progressing is either 0 or 1!");
				} catch (ArrayIndexOutOfBoundsException bounds) {
					JOptionPane.showMessageDialog(frame,
							"Please take care that you enter attributes " + "in the correct format!");
				} catch (IllegalAccessException illegal) {
					JOptionPane.showMessageDialog(frame,
							"Please take care that the Type of the attribute is either int or double!");
				} finally {
					// uncomment if default description should be added after a
					// faulty description
					// attField.setText("Name:Type progressing");
				}
			}
		});

		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.NORTHWEST;
		gbc_button.insets = new Insets(0, 5, 5, 5);
		gbc_button.gridx = 1;
		gbc_button.gridy = 2;
		this.frame.getContentPane().add(button, gbc_button);

		this.frame.pack();
		this.frame.setVisible(true);
		this.frame.setAlwaysOnTop(true);

	}

	/**
	 * This method tries to parse the text input according to instructions and
	 * save it as the Schema for the EditorUi instance. If nothing was changed
	 * nothing is saved. If the format is not according to instructions
	 * (Name(String):Type(String) progressing(Integer)) Errors are thrown to be
	 * caught by calling method for handling.
	 * 
	 * @throws IllegalAccessException
	 *             Is thrown if the attribute type is neither double nor int.
	 * 
	 * @throws IllegalArgumentException
	 *             If progressing is not either set to 0 or 1 this exception is
	 *             thrown.
	 * @throws ArrayIndexOutOfBoundsException
	 *             If more than three arguments per attribute are specified.
	 */
	protected void parseString() throws IllegalAccessException {

		String text = attField.getText();
		ArrayList<String[]> schema = new ArrayList<String[]>();

		if (!text.contains("Name:Type:progressing/")) {
			String[] substrings = text.split("/");
			for (String str : substrings) {
				if (text.contains(":")) {
					String[] attributes = new String[3];
					int cnt = 0;
					for (String sub : str.split(":")) {
						attributes[cnt] = sub;
						cnt++;
						// if too many values are specified this throws an
						// ArrayOutOfBoundsException
					}

					// attributes[1] = sub[1].split(" ")[0];
					if (!attributes[1].equals("int") && !attributes[1].equals("double")) {
						throw new IllegalAccessException();
					}

					int progressing = Integer.parseInt(attributes[2]);
					if (progressing != 0 && progressing != 1)
						throw new IllegalArgumentException();
					// attributes[2] = sub[1].split(" ")[1];

					schema.add(attributes);
				}
			}
			this.editor.schema = schema;
		}
	}

	/**
	 * This creates the Attribute part of the frame and is only outsourced for
	 * better readability.
	 */
	private void attributeField() {

		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(150, 40));
		label.setText("Schema of the plan: ");

		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.NORTH;
		gbc_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_label.insets = new Insets(5, 5, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 1;
		this.frame.getContentPane().add(label, gbc_label);

		this.attField = new JTextField();

		String textEntry = "";
		if (this.editor.schema.size() > 0) {
			for (String[] attrList : this.editor.schema) {
				textEntry = textEntry + attrList[0] + ":";
				textEntry += attrList[1] + ":";
				textEntry += attrList[2] + "/";
			}
		} else {
			textEntry = "Name:Type:progressing/";
		}

		attField.setText(textEntry);
		attField.setEditable(true);
		attField.setPreferredSize(new Dimension(200, 40));
		attField.setToolTipText("Please enter like: 'Name:Type progressing'"
				+ " where Name is a String, Type is the String 'int' or 'double' and progressing is 0 for false or 1 for true."
				+ "End attribute with semicolon to start next.");

		GridBagConstraints text = new GridBagConstraints();
		text.anchor = GridBagConstraints.NORTHWEST;
		text.fill = GridBagConstraints.HORIZONTAL;
		text.insets = new Insets(5, 5, 5, 5);
		text.gridx = 1;
		text.gridy = 1;
		this.frame.getContentPane().add(attField, text);

	}

	/**
	 * This creates the frame and sets up the first part of the GUI where a
	 * schema name can be entered.
	 */
	private JTextField setUp() {
		this.frame = new JFrame("Startup Options");
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.frame.setPreferredSize(new Dimension(600, 200));
		this.frame.setLocation(200, 200);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100, 400, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 80, 80, 80, 80 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };

		this.frame.getContentPane().setLayout(gridBagLayout);

		JTextField textField = new JTextField();
		textField.setEditable(true);
		textField.setPreferredSize(new Dimension(200, 40));
		textField.setName("Name");

		GridBagConstraints text = new GridBagConstraints();
		text.anchor = GridBagConstraints.SOUTHWEST;
		text.fill = GridBagConstraints.HORIZONTAL;
		text.insets = new Insets(5, 5, 5, 5);
		text.gridx = 1;
		text.gridy = 0;
		this.frame.getContentPane().add(textField, text);

		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(150, 40));
		label.setText("Name of the Plan: ");

		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.SOUTH;
		gbc_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_label.insets = new Insets(5, 5, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		this.frame.getContentPane().add(label, gbc_label);
		return textField;
	}
}
