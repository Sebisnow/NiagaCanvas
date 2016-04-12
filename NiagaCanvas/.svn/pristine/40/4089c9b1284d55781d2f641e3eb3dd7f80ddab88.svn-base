package niagaCanvas;

import java.awt.Button;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.reflections.Reflections;

import niagacanvas.operator.GetParamDesc;
import niagacanvas.operator.OperatorAnnotation;
import niagacanvas.operator.ParamDescription;
import niagarino.operator.AbstractOperator;
import niagarino.operator.predicate.Predicate;

@SuppressWarnings("serial")
public class OptionWindow extends JFrame {
	public boolean isSet;
	public String name;
	private JTextField textField;
	private final NiagarinoOperators op;
	private OptionWindow self;

	/**
	 * Create the panel for Stream.
	 * 
	 */
	public OptionWindow(JFrame frame, Stream operator) {
		self = this;
		this.op = operator;
		this.getContentPane().setPreferredSize(new Dimension(500, 200));
		this.setTitle("Manage Options of the Stream: " + this.op.name);
		isSet = false;
		setPreferredSize(new Dimension(400, 200));
		setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 90, 90, 90, 90 };
		gridBagLayout.rowHeights = new int[] { 50, 50, 50, 50, 50 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };

		getContentPane().setLayout(gridBagLayout);

		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(5, 5, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.SOUTHEAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		getContentPane().add(lblName, gbc_lblName);

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.anchor = GridBagConstraints.SOUTH;
		gbc_textField.insets = new Insets(5, 5, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 1;
		getContentPane().add(textField, gbc_textField);
		textField.setColumns(10);

		JLabel lblOperatorType = new JLabel("Operator Type:");
		GridBagConstraints gbc_lblOperatorType = new GridBagConstraints();
		gbc_lblOperatorType.insets = new Insets(0, 0, 5, 5);
		gbc_lblOperatorType.gridx = 0;
		gbc_lblOperatorType.gridy = 2;
		getContentPane().add(lblOperatorType, gbc_lblOperatorType);

		JLabel lblType = new JLabel("Stream");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.gridx = 1;
		gbc_lblType.gridy = 2;
		getContentPane().add(lblType, gbc_lblType);

		Button button = new Button("Create");
		button.setSize(new Dimension(80, 80));
		button.setFont(new Font("Dialog", Font.PLAIN, 15));
		button.setPreferredSize(new Dimension(80, 80));

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				op.name = textField.getText();

				isSet = true;
				setVisible(false);
			}
		});
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.insets = new Insets(0, 0, 0, 5);
		gbc_button.anchor = GridBagConstraints.NORTH;
		gbc_button.gridx = 1;
		gbc_button.gridy = 3;
		this.getContentPane().add(button, gbc_button);

		this.pack();
		this.setVisible(true);

	}

	/**
	 * Create the window to edit Operators.
	 * 
	 * 
	 */
	public OptionWindow(JFrame frame, Operator operator) {
		this.op = operator;

		this.setTitle("Manage Options of the Operator: " + this.op.name);
		isSet = false;
		this.setPreferredSize(new Dimension(600, 400));
		this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100, 110, 90, 10 };
		gridBagLayout.rowHeights = new int[] { 35, 46, 55, 0, 10 };

		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		JLabel lblName = new JLabel("The Name of your Operator:");
		lblName.setName("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		getContentPane().add(lblName, gbc_lblName);

		JButton button = new JButton("Create");
		button.setName("create");
		button.setFont(new Font("Dialog", Font.PLAIN, 15));
		button.setPreferredSize(new Dimension(100, 40));

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				op.name = textField.getText();

				isSet = true;
				setVisible(false);
			}
		});

		textField = new JTextField();
		if (this.op.name != null) {
			textField.setText(this.op.name);
		}
		textField.setName("Name");
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		getContentPane().add(textField, gbc_textField);

		if (operator.getParentID() != NiagarinoOperators.INVALID) {
			setParent();
		}
		if (operator.getChildID() != NiagarinoOperators.INVALID) {
			setChild();
		}
		// Handle Operator Type fun.
		JLabel lblOperatorType = new JLabel("Operator Type");
		lblOperatorType.setName("Name");
		GridBagConstraints gbc_lblOperatorType = new GridBagConstraints();
		gbc_lblOperatorType.anchor = GridBagConstraints.EAST;
		gbc_lblOperatorType.insets = new Insets(0, 0, 5, 5);
		gbc_lblOperatorType.gridx = 0;
		gbc_lblOperatorType.gridy = 1;
		getContentPane().add(lblOperatorType, gbc_lblOperatorType);

		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setName("Name");
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!e.getItem().equals("")) {
					((Operator) op).properties.put("OperatorType", (String) e.getItem());
					redrawOperator((String) e.getItem());

				}
			}
		});
		comboBox.addItem("");
		String opType = operator.properties.get("OperatorType");
		Reflections reflections = new Reflections("niagacanvas.operator");

		// Sets the standard selection to the Operator Type of the operator if
		// already given.
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(OperatorAnnotation.class);
		for (Class<?> cl : classes) {
			comboBox.addItem(cl.getSimpleName());
		}
		comboBox.setSelectedItem(opType);

		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
		getContentPane().add(comboBox, gbc_comboBox);

		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.insets = new Insets(0, 0, 5, 5);
		gbc_button.anchor = GridBagConstraints.NORTH;
		gbc_button.gridx = 1;
		gbc_button.gridy = getContentPane().getComponentCount() + 1;
		getContentPane().add(button, gbc_button);

		this.pack();
		this.setVisible(true);

	}

	/**
	 * Helper method to show ChildId
	 */
	private void setChild() {
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		getContentPane().add(lblName, gbc_lblName);

	}

	/**
	 * Helper method to show ChildId
	 */
	private void setParent() {
		// TODO Auto-generated method stub

	}

	/**
	 * Gets called when the combo box status changed. Then the frame is redrawn
	 * to accommodate the new options
	 * 
	 * @param opName
	 *            The name of the Niagarino Operator to change the options to.
	 */
	private void redrawOperator(String opName) {
		Operator operator = (Operator) this.op;
		JButton button = null;

		// System.out.println(this.getContentPane().getComponents().length);
		for (Component c : this.getContentPane().getComponents()) {

			if (c instanceof JButton && c.getName().equals("create")) {
				button = (JButton) c;
				this.getContentPane().remove(c);

			} else if (c.getName() != null && c.getName().equals("Name")) {

				continue;

			} else {
				this.getContentPane().remove(c);
			}
		}
		if (button != null) {
			button.setName("create");
		}

		// TODO right size for this array? should be
		ParamDescription[] param = new ParamDescription[10];
		// find the correct Operator class and save it to the Operator.
		for (Class<? extends AbstractOperator> cl : getAbsOperator()) {
			if (cl.getSimpleName().equals(opName)) {
				try {
					param = (ParamDescription[]) this.getParamMethod(cl).invoke(null);
				} catch (Exception e) {
					// TODO please handle if necessary, should not throw error.
					e.printStackTrace();
				}
				operator.setOpClass(cl);
			}
		}

		// Show the saved parameters in the GUI
		int cnt = 3;
		for (ParamDescription paramDesc : param) {
			if (paramDesc == null) {
				continue;
			}
			JLabel label = new JLabel(paramDesc.getName() + ": ");
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.insets = new Insets(0, 0, 5, 5);
			gbc_label.gridx = 0;
			gbc_label.gridy = cnt;
			gbc_label.anchor = GridBagConstraints.EAST;
			getContentPane().add(label, gbc_label);

			operator.properties.put("ParamDescription", paramDesc.getName());
			switch (paramDesc.getValueRange()) {
			case ParamDescription.STRING:
				JTextField text = new JTextField(paramDesc.getName());

				if (operator.properties.get(paramDesc.getName()) != null) {
					text.setName(operator.properties.get(paramDesc.getName()));
				}

				text.setEditable(true);
				text.setToolTipText(paramDesc.getDescription());

				GridBagConstraints gbc_text = new GridBagConstraints();
				gbc_text.insets = new Insets(0, 0, 5, 5);
				gbc_text.fill = GridBagConstraints.HORIZONTAL;
				gbc_text.gridx = 1;
				gbc_text.gridy = cnt;
				getContentPane().add(text, gbc_text);
				JButton save = new JButton("Save");
				save.setName("save");
				save.setFont(new Font("Dialog", Font.PLAIN, 15));
				save.setPreferredSize(new Dimension(80, 20));

				save.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						operator.properties.put(paramDesc.getName(), text.getText());
						ArrayList<String> predValues = new ArrayList<String>();
						predValues.add(text.getText());
						operator.attributes.put("String", predValues);
					}
				});
				GridBagConstraints gbc_save = new GridBagConstraints();
				gbc_save.insets = new Insets(0, 0, 5, 5);
				gbc_save.fill = GridBagConstraints.HORIZONTAL;
				gbc_save.gridx = 3;
				gbc_save.gridy = cnt;
				getContentPane().add(save, gbc_save);
				cnt++;
				break;
			case ParamDescription.INT:
				JTextField text1 = new JTextField(paramDesc.getName());

				if (operator.properties.get(paramDesc.getName()) != null) {
					text1.setText(operator.properties.get(paramDesc.getName()));
				}

				text1.setEditable(true);
				text1.setToolTipText(paramDesc.getDescription());

				GridBagConstraints gbc_text1 = new GridBagConstraints();
				gbc_text1.insets = new Insets(0, 0, 5, 5);
				gbc_text1.fill = GridBagConstraints.HORIZONTAL;
				gbc_text1.gridx = 1;
				gbc_text1.gridy = cnt;
				getContentPane().add(text1, gbc_text1);
				JButton save1 = new JButton("Save");
				save1.setName("save");
				save1.setFont(new Font("Dialog", Font.PLAIN, 15));
				save1.setPreferredSize(new Dimension(80, 20));

				save1.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							Integer.parseInt(text1.getText());
							operator.properties.put(paramDesc.getName(), text1.getText());
							ArrayList<String> predValues = new ArrayList<String>();
							predValues.add(text1.getText());
							operator.attributes.put("Boolean", predValues);
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(self, "Only Integers please!");
						}
					}
				});
				GridBagConstraints gbc_save1 = new GridBagConstraints();
				gbc_save1.insets = new Insets(0, 0, 5, 5);
				gbc_save1.fill = GridBagConstraints.HORIZONTAL;
				gbc_save1.gridx = 3;
				gbc_save1.gridy = cnt;
				getContentPane().add(save1, gbc_save1);
				cnt++;

				break;
			case ParamDescription.BOOLEAN:
				JComboBox<String> box = new JComboBox<String>();
				box.addItem("true");
				box.addItem("false");
				box.addItemListener(new ItemListener() {
					@Override

					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							operator.properties.put(paramDesc.getName(), (String) e.getItem());

							ArrayList<String> predValues = new ArrayList<String>();
							predValues.add((String) e.getItem());
							operator.attributes.put("Boolean", predValues);
						}
					}
				});
				if (operator.properties.get(paramDesc.getName()) != null) {
					box.setSelectedIndex(Integer.parseInt(operator.properties.get(paramDesc.getName())));
				}

				cnt++;

				break;
			case ParamDescription.DOUBLE:
				JTextField text2 = new JTextField(paramDesc.getName());

				if (operator.properties.get(paramDesc.getName()) != null) {
					text2.setText(operator.properties.get(paramDesc.getName()));
				}

				text2.setEditable(true);
				text2.setToolTipText(paramDesc.getDescription());

				GridBagConstraints gbc_text2 = new GridBagConstraints();
				gbc_text2.insets = new Insets(0, 0, 5, 5);
				gbc_text2.fill = GridBagConstraints.HORIZONTAL;
				gbc_text2.gridx = 1;
				gbc_text2.gridy = cnt;
				getContentPane().add(text2, gbc_text2);
				JButton save2 = new JButton("Save");
				save2.setName("save");
				save2.setFont(new Font("Dialog", Font.PLAIN, 15));
				save2.setPreferredSize(new Dimension(80, 20));

				save2.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						operator.properties.put(paramDesc.getName(), text2.getText());
						// Functional Rudimentary implementation. Receives one
						// Double as
						// input.
						ArrayList<String> predValues = new ArrayList<String>();
						predValues.add(text2.getText());
						operator.attributes.put("Double", predValues);
					}
				});
				GridBagConstraints gbc_save2 = new GridBagConstraints();
				gbc_save2.insets = new Insets(0, 0, 5, 5);
				gbc_save2.fill = GridBagConstraints.HORIZONTAL;
				gbc_save2.gridx = 3;
				gbc_save2.gridy = cnt;
				getContentPane().add(save2, gbc_save2);

				cnt++;

				break;
			case ParamDescription.LONG:
				JTextField text3 = new JTextField(paramDesc.getName());

				if (operator.properties.get(paramDesc.getName()) != null) {
					text3.setText(operator.properties.get(paramDesc.getName()));
				}

				text3.setEditable(true);
				text3.setToolTipText(paramDesc.getDescription());

				GridBagConstraints gbc_text3 = new GridBagConstraints();
				gbc_text3.insets = new Insets(0, 0, 5, 5);
				gbc_text3.fill = GridBagConstraints.HORIZONTAL;
				gbc_text3.gridx = 1;
				gbc_text3.gridy = cnt;
				getContentPane().add(text3, gbc_text3);
				JButton save3 = new JButton("Save");
				save3.setName("save");
				save3.setFont(new Font("Dialog", Font.PLAIN, 15));
				save3.setPreferredSize(new Dimension(80, 20));

				save3.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						operator.properties.put(paramDesc.getName(), text3.getText());
						// Functional Rudimentary implementation. Receives one
						// Long as
						// input.
						ArrayList<String> predValues = new ArrayList<String>();
						predValues.add(text3.getText());
						operator.attributes.put("Long", predValues);
					}
				});
				GridBagConstraints gbc_save3 = new GridBagConstraints();
				gbc_save3.insets = new Insets(0, 0, 5, 5);
				gbc_save3.fill = GridBagConstraints.HORIZONTAL;
				gbc_save3.gridx = 3;
				gbc_save3.gridy = cnt;
				getContentPane().add(save3, gbc_save3);
				cnt++;

				break;
			case ParamDescription.PREDICATE:
				Reflections ref = new Reflections("niagarino.operator.predicate");

				JComboBox<String> box1 = new JComboBox<String>();
				for (Class<? extends Predicate> cl : ref.getSubTypesOf(Predicate.class)) {
					box1.addItem(cl.getSimpleName());
				}
				box1.addItemListener(new BoxItemListener(operator, paramDesc));

				if (operator.properties.get("Predicate") != null) {
					box1.setSelectedItem(operator.properties.get("Predicate"));
				}

				GridBagConstraints gbc_box1 = new GridBagConstraints();
				gbc_box1.insets = new Insets(0, 0, 5, 5);
				gbc_box1.fill = GridBagConstraints.HORIZONTAL;
				gbc_box1.gridx = 1;
				gbc_box1.gridy = cnt;
				getContentPane().add(box1, gbc_box1);

				JTextField text5 = new JTextField();
				if (operator.properties.get(box1.getSelectedItem()) != null) {
					text5.setText(operator.properties.get(box1.getSelectedItem()));
				}
				text5.setEditable(true);
				text5.setToolTipText(paramDesc.getDescription());

				GridBagConstraints gbc_text5 = new GridBagConstraints();
				gbc_text5.insets = new Insets(0, 0, 5, 5);
				gbc_text5.fill = GridBagConstraints.HORIZONTAL;
				gbc_text5.gridx = 2;
				gbc_text5.gridy = cnt;
				getContentPane().add(text5, gbc_text5);

				JButton saveBut = new JButton("Save");
				saveBut.setName("save");
				saveBut.setFont(new Font("Dialog", Font.PLAIN, 15));
				saveBut.setPreferredSize(new Dimension(80, 20));

				saveBut.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						operator.properties.put((String) box1.getSelectedItem(), text5.getText());
						// add Predicate to Map with values on what kind of
						// Predicate as String and the Predicate itself as
						// String.
						ArrayList<String> predValues = new ArrayList<String>();
						predValues.add((String) box1.getSelectedItem());
						predValues.add(text5.getText());
						operator.attributes.put("Predicate", predValues);
					}
				});
				GridBagConstraints gbc_saveBut = new GridBagConstraints();
				gbc_saveBut.insets = new Insets(0, 0, 5, 5);
				gbc_saveBut.fill = GridBagConstraints.HORIZONTAL;
				gbc_saveBut.gridx = 3;
				gbc_saveBut.gridy = cnt;
				getContentPane().add(saveBut, gbc_saveBut);
				cnt++;

				break;
			case ParamDescription.GROUP:
				// TODO only implemented as String
				JTextField text6 = new JTextField(paramDesc.getName());

				if (operator.properties.get(paramDesc.getName()) != null) {
					text6.setText(operator.properties.get(paramDesc.getName()));
				}

				text6.setEditable(true);
				text6.setToolTipText(paramDesc.getDescription());

				GridBagConstraints gbc_text6 = new GridBagConstraints();
				gbc_text6.insets = new Insets(0, 0, 5, 5);
				gbc_text6.fill = GridBagConstraints.HORIZONTAL;
				gbc_text6.gridx = 1;
				gbc_text6.gridy = cnt;
				getContentPane().add(text6, gbc_text6);

				JButton saveBut1 = new JButton("Save");
				saveBut1.setName("save");
				saveBut1.setFont(new Font("Dialog", Font.PLAIN, 15));
				saveBut1.setPreferredSize(new Dimension(80, 20));

				saveBut1.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						operator.properties.put(paramDesc.getName(), text6.getText());

						// TODO Rudimentary implementation. Different Groups are
						// entered expected to be separated with ":".
						ArrayList<String> predValues = new ArrayList<String>();
						for (String sub : text6.getText().split(":")) {
							predValues.add(sub);
						}
						operator.attributes.put("Group", predValues);
					}
				});
				GridBagConstraints gbc_saveBut1 = new GridBagConstraints();
				gbc_saveBut1.insets = new Insets(0, 0, 5, 5);
				gbc_saveBut1.fill = GridBagConstraints.HORIZONTAL;
				gbc_saveBut1.gridx = 3;
				gbc_saveBut1.gridy = cnt;
				getContentPane().add(saveBut1, gbc_saveBut1);
				cnt++;

				break;
			case ParamDescription.AGGREGATIONFUNCTION:
				// TODO only implemented as String
				JTextField text7 = new JTextField(paramDesc.getName());

				if (operator.properties.get(paramDesc.getName()) != null) {
					text7.setText(operator.properties.get(paramDesc.getName()));
				}

				text7.setEditable(true);
				text7.setToolTipText(paramDesc.getDescription());

				GridBagConstraints gbc_text7 = new GridBagConstraints();
				gbc_text7.insets = new Insets(0, 0, 5, 5);
				gbc_text7.fill = GridBagConstraints.HORIZONTAL;
				gbc_text7.gridx = 1;
				gbc_text7.gridy = cnt;
				getContentPane().add(text7, gbc_text7);

				JButton saveBut2 = new JButton("Save");
				saveBut2.setName("save");
				saveBut2.setFont(new Font("Dialog", Font.PLAIN, 15));
				saveBut2.setPreferredSize(new Dimension(80, 20));

				saveBut2.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						operator.properties.put(paramDesc.getName(), text7.getText());

						// TODO Rudimentary implementation.
						// Can later easily be implemented using separate
						// fields, so far only as Sting separated by colon ad
						// slash if more than one aggregation.
						// An Aggregation
						// Function has one inputAttribute from the schema and
						// new attribute produced as output with a name.
						ArrayList<String> predValues = new ArrayList<String>();
						for (String attri : text7.getText().split(":")) {
							predValues.add(attri);
						}
						operator.attributes.put("AggregationFunction", predValues);
					}
				});
				GridBagConstraints gbc_saveBut2 = new GridBagConstraints();
				gbc_saveBut2.insets = new Insets(0, 0, 5, 5);
				gbc_saveBut2.fill = GridBagConstraints.HORIZONTAL;
				gbc_saveBut2.gridx = 3;
				gbc_saveBut2.gridy = cnt;
				getContentPane().add(saveBut2, gbc_saveBut2);
				cnt++;

				break;
			case ParamDescription.STRINGFUNCTION:
				// TODO only implemented as String
				JTextField text8 = new JTextField(paramDesc.getName());

				if (operator.properties.get(paramDesc.getName()) != null) {
					text8.setText(operator.properties.get(paramDesc.getName()));
				}

				text8.setEditable(true);
				text8.setToolTipText(paramDesc.getDescription());

				GridBagConstraints gbc_text8 = new GridBagConstraints();
				gbc_text8.insets = new Insets(0, 0, 5, 5);
				gbc_text8.fill = GridBagConstraints.HORIZONTAL;
				gbc_text8.gridx = 1;
				gbc_text8.gridy = cnt;
				getContentPane().add(text8, gbc_text8);

				JButton saveBut3 = new JButton("Save");
				saveBut3.setName("save");
				saveBut3.setFont(new Font("Dialog", Font.PLAIN, 15));
				saveBut3.setPreferredSize(new Dimension(80, 20));

				saveBut3.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						operator.properties.put(paramDesc.getName(), text8.getText());

						// TODO Rudimentary implementation.
						// Function has one String as parameter to execute the
						// function.
						ArrayList<String> predValues = new ArrayList<String>();
						predValues.add(text8.getText());
						operator.attributes.put("StringFunction", predValues);
					}
				});
				GridBagConstraints gbc_saveBut3 = new GridBagConstraints();
				gbc_saveBut3.insets = new Insets(0, 0, 5, 5);
				gbc_saveBut3.fill = GridBagConstraints.HORIZONTAL;
				gbc_saveBut3.gridx = 3;
				gbc_saveBut3.gridy = cnt;
				getContentPane().add(saveBut3, gbc_saveBut3);
				cnt++;
				break;

			default:
				break;
			}

		}
		if (button != null) {
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 0, 5, 5);
			gbc_button.anchor = GridBagConstraints.NORTH;
			gbc_button.gridx = 1;
			gbc_button.gridy = cnt;
			getContentPane().add(button, gbc_button);
		}
		this.pack();
		this.validate();
		this.repaint();
	}

	/**
	 * Helper method to get annotated method of a certain Operator.
	 * 
	 * @param cl
	 *            The class we want the ParamDesc from to know how to
	 *            instantiate the class.
	 * @return Returns the Method that can retrieve the ParamDesc.
	 */
	private Method getParamMethod(Class<? extends AbstractOperator> cl) {
		Method meth = null;
		for (final Method method : cl.getDeclaredMethods()) {
			if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
				// skip non-static methods
				continue;
			}

			if (method.isAnnotationPresent(GetParamDesc.class)) {
				meth = method;
			}
		}

		return meth;
	}

	/**
	 * Retrieves all Niagarino Operators, all Operators from the Niagarino
	 * package that are Annotated as Operators and implement the
	 * AbstractOperator Interface.
	 * 
	 * @return Returns a Set of Niagarino Operators as options.
	 */
	@SuppressWarnings("unchecked")
	private Set<Class<? extends AbstractOperator>> getAbsOperator() {
		Reflections reflections = new Reflections("niagacanvas.operator");

		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(OperatorAnnotation.class);
		Set<Class<? extends AbstractOperator>> operatorClasses = new HashSet<Class<? extends AbstractOperator>>();

		for (Class<?> cl : classes) {
			if (cl.getGenericSuperclass().equals(AbstractOperator.class)) {
				operatorClasses.add((Class<? extends AbstractOperator>) cl);
			}
		}
		return operatorClasses;
	}

	/**
	 * @author sebastian
	 *
	 *         This Item Listener Class can be used more often. It saves the
	 *         selected Niagarino Operator in the operator variable.
	 */
	public class BoxItemListener implements ItemListener {

		private ParamDescription field;
		private Operator operator;

		public BoxItemListener(Operator operator, ParamDescription field2) {
			this.field = field2;
			this.operator = operator;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				this.operator.properties.put(field.getName(), (String) e.getItem());
		}
	}

}
