package deprecated;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import niagaCanvas.EditorUI;

public class StartWindow {
	/**
	 * 
	 */
	private static final long serialVersionUID = -747581984456689456L;
	public boolean isSet;
	public String name;
	private JFrame frame;
	private int cnt;
	private int indexCnt;
	private EditorUI editor;
	private JButton button;
	private GridBagConstraints gbc_button;

	/**
	 * Create the panel.
	 */
	public StartWindow(EditorUI editor) {
		this.editor = editor;
		this.cnt = 0;
		this.indexCnt = 0;
		this.button = new JButton("Start");
		this.gbc_button = new GridBagConstraints();
		this.createStartWindow();

	}

	private void createStartWindow() {
		this.frame = new JFrame("Startup Options");
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.frame.setPreferredSize(new Dimension(800, 600));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 200, 200, 100, 50 };
		gridBagLayout.rowHeights = new int[] { 80, 80, 80, 80 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		this.frame.getContentPane().setLayout(gridBagLayout);

		JTextField textField = new JTextField();
		textField.setEditable(true);
		textField.setPreferredSize(new Dimension(200, 40));
		textField.setName("Name");
		this.cnt++;

		GridBagConstraints text = new GridBagConstraints();
		text.anchor = GridBagConstraints.NORTH;
		text.fill = GridBagConstraints.HORIZONTAL;
		text.insets = new Insets(0, 0, 5, 5);
		text.gridx = 1;
		text.gridy = this.cnt;
		this.frame.getContentPane().add(textField, text);

		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(150, 40));
		label.setText("Name of the Plan: ");

		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.NORTH;
		gbc_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = this.cnt;
		this.frame.getContentPane().add(label, gbc_label);
		this.cnt += 2;

		// TODO refine to Objects instead of Strings - e.g. through use of
		// reflections

		// TODO check for existing Schema
		if (this.editor.schema.size() > 0) {
			for (String[] attr : this.editor.schema) {
				JTextField field = this.newLabelText("Attribute:");
				field.setText(attr.toString());
			}
		}

		JButton but = new JButton("Add another Attribute to Schema");
		but.setFont(new Font("Dialog", Font.PLAIN, 15));
		but.setPreferredSize(new Dimension(100, 80));

		but.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newLabel("Attribute: ");
				frame.pack();
				System.out.println("initiated");
			}
		});
		GridBagConstraints gbc_but = new GridBagConstraints();
		gbc_but.anchor = GridBagConstraints.NORTH;
		gbc_but.fill = GridBagConstraints.HORIZONTAL;
		gbc_but.insets = new Insets(0, 0, 5, 5);
		gbc_but.gridx = 3;
		gbc_but.gridy = 1;
		this.frame.getContentPane().add(but, gbc_but);

		this.button.setFont(new Font("Dialog", Font.PLAIN, 15));
		this.button.setPreferredSize(new Dimension(80, 80));

		this.button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				for (Component comp : frame.getContentPane().getComponents()) {
					// TODO refine to Objects instead of Strings
					if (comp instanceof JTextField) {
						if (comp.getName().equals("Name")) {
							editor.setTitle(((JTextField) comp).getText() == null ? "NiagarZUI"
									: ((JTextField) comp).getText());
						} else {
							map.put(comp.getName(), ((JTextField) comp).getText());
						}
					}
				}
				// save the inserted values in the right order in the Schema
				for (int i = 0; i < map.size(); i++) {
					if (map.get(String.valueOf(i)) != null) {
						// TODO changed
						// editor.schema.put("that", null);
					}
				}
				System.out.println(map);
				// isSet = true;
				frame.setVisible(false);
			}
		});

		this.gbc_button.anchor = GridBagConstraints.CENTER;
		this.gbc_button.insets = new Insets(0, 0, 5, 5);
		this.gbc_button.gridx = 1;
		this.gbc_button.gridy = this.cnt + 1;
		this.frame.getContentPane().add(this.button, this.gbc_button);

		this.frame.pack();
		this.frame.setVisible(true);
		this.frame.setAlwaysOnTop(true);
	}

	private void newLabel(String string) {
		if (this.gbc_button.gridy <= this.cnt + 1) {

			JTextField textField = new JTextField();
			textField.setEditable(true);
			textField.setPreferredSize(new Dimension(200, 40));
			textField.setName(String.valueOf(this.indexCnt));
			this.indexCnt++;

			GridBagConstraints text = new GridBagConstraints();
			text.anchor = GridBagConstraints.NORTH;
			text.fill = GridBagConstraints.HORIZONTAL;
			text.insets = new Insets(0, 0, 5, 5);
			text.gridx = 1;
			text.gridy = this.cnt;
			this.frame.getContentPane().add(textField, text);

			JLabel label = new JLabel();
			label.setPreferredSize(new Dimension(300, 40));
			label.setText(string);

			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.anchor = GridBagConstraints.NORTH;
			gbc_label.fill = GridBagConstraints.HORIZONTAL;
			gbc_label.insets = new Insets(0, 0, 5, 5);
			gbc_label.gridx = 0;
			gbc_label.gridy = this.cnt;
			this.frame.getContentPane().add(label, gbc_label);
			this.cnt++;
			this.gbc_button.gridy = cnt++;
			this.frame.getContentPane().remove(button);
			this.frame.getContentPane().add(this.button, this.gbc_button);
		} else {
			newLabelText(string);
		}
	}

	private JTextField newLabelText(String string) {
		JTextField textField = new JTextField();
		textField.setEditable(true);
		textField.setPreferredSize(new Dimension(200, 40));
		textField.setName(String.valueOf(this.indexCnt));
		this.indexCnt++;

		GridBagConstraints text = new GridBagConstraints();
		text.anchor = GridBagConstraints.NORTH;
		text.fill = GridBagConstraints.HORIZONTAL;
		text.insets = new Insets(0, 0, 5, 5);
		text.gridx = 1;
		text.gridy = this.cnt;
		this.frame.getContentPane().add(textField, text);

		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(300, 40));
		label.setText(string);

		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.NORTH;
		gbc_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = this.cnt;
		this.frame.getContentPane().add(label, gbc_label);
		this.cnt++;
		return textField;
	}

}
