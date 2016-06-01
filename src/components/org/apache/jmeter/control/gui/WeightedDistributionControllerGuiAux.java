package org.apache.jmeter.control.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.apache.jmeter.control.WeightedDistributionController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.util.JMeterUtils;

class WeightedDistributionControllerGuiAux {

}

class WeightedDistributionTableModelListener implements TableModelListener {

	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getSource() instanceof EventFiringPowerTableModel && e.getType() == TableModelEvent.UPDATE) {
			switch (e.getColumn()) {
			case WeightedDistributionControllerGui.ENABLED_COLUMN:
				handleEnabledChange(e);
				break;
			case WeightedDistributionControllerGui.ELEMENT_NAME_COLUMN:
				handleElementNameChange(e);
				break;
			case WeightedDistributionControllerGui.WEIGHT_COLUMN:
				handleWeightChange(e);
				break;
			default:
				break;
			}
		}
	}

	private void handleEnabledChange(TableModelEvent e) {
		if (WeightedDistributionControllerGui.isCurrentElementWeightedDistributionController()) {
			EventFiringPowerTableModel firingModel = (EventFiringPowerTableModel) e.getSource();
			Object[] rowData = firingModel.getRowData(e.getFirstRow());
			boolean isEnabled = (boolean) rowData[WeightedDistributionControllerGui.ENABLED_COLUMN];
			WeightedDistributionController wdc = (WeightedDistributionController) GuiPackage.getInstance()
					.getCurrentElement();
			((JMeterTreeNode) wdc.getNode()
					.getChildAt((int) rowData[WeightedDistributionControllerGui.HIDDEN_CHILD_NODE_IDX_COLUMN]))
							.setEnabled(isEnabled);
			updateProbabilityColumn(firingModel);
			GuiPackage.getInstance().getMainFrame().repaint();
		}
	}

	private void handleElementNameChange(TableModelEvent e) {
		EventFiringPowerTableModel firingModel = (EventFiringPowerTableModel) e.getSource();
		Object[] rowData = firingModel.getRowData(e.getFirstRow());
		((JMeterTreeNode) GuiPackage.getInstance().getCurrentNode()
				.getChildAt((int) rowData[WeightedDistributionControllerGui.HIDDEN_CHILD_NODE_IDX_COLUMN]))
						.setName((String) rowData[WeightedDistributionControllerGui.ELEMENT_NAME_COLUMN]);
		GuiPackage.getInstance().getMainFrame().repaint();
	}

	private void handleWeightChange(TableModelEvent e) {
		if  (WeightedDistributionControllerGui.isCurrentElementWeightedDistributionController()) {
			EventFiringPowerTableModel firingModel = (EventFiringPowerTableModel) e.getSource();
			Object[] rowData = firingModel.getRowData(e.getFirstRow());
			int weight = (int) rowData[WeightedDistributionControllerGui.WEIGHT_COLUMN];
			WeightedDistributionController wdc = (WeightedDistributionController) GuiPackage.getInstance()
					.getCurrentElement();
			((JMeterTreeNode) wdc.getNode()
					.getChildAt((int) rowData[WeightedDistributionControllerGui.HIDDEN_CHILD_NODE_IDX_COLUMN]))
						.getTestElement().setProperty(WeightedDistributionController.WEIGHT, weight);
			updateProbabilityColumn(firingModel);
			GuiPackage.getInstance().getMainFrame().repaint();
		}
	}

	@SuppressWarnings("unchecked")
	private void updateProbabilityColumn(EventFiringPowerTableModel firingModel) {
		WeightedDistributionController wdc = (WeightedDistributionController) GuiPackage.getInstance()
				.getCurrentElement();
		wdc.resetCumulativeProbability();
		List<Integer> weightData = (List<Integer>) firingModel.getColumnData(
				WeightedDistributionControllerGui.COLUMN_NAMES[WeightedDistributionControllerGui.WEIGHT_COLUMN]);
		List<Boolean> enabledData = (List<Boolean>) firingModel.getColumnData(
				WeightedDistributionControllerGui.COLUMN_NAMES[WeightedDistributionControllerGui.ENABLED_COLUMN]);
		List<Float> probabilityData = new ArrayList<Float>(weightData.size());
		for (int i = 0; i < weightData.size(); i++) {
			if (enabledData.get(i)) {
				probabilityData.add(wdc.calculateProbability(weightData.get(i)));
			} else {
				probabilityData.add(wdc.calculateProbability(0));
			}
		}
		firingModel.setColumnData(WeightedDistributionControllerGui.PERCENT_COLUMN, probabilityData);
	}
}

class EventFiringPowerTableModel extends PowerTableModel {
	private static final long serialVersionUID = -600418978315572279L;

	public EventFiringPowerTableModel(String[] headers, Class<?>[] classes) {
		super(headers, classes);
	}

	public EventFiringPowerTableModel() {
		super();
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		super.setValueAt(aValue, row, column);
		fireTableCellUpdated(row, column);
	}

}

@SuppressWarnings("serial")
class IneditablePercentageRenderer extends DefaultTableCellRenderer {
	private static final String FORMAT = "###.####%";
	private final DecimalFormat formatter = new DecimalFormat(FORMAT);

	public IneditablePercentageRenderer() {
		super();
		setBackground(Color.LIGHT_GRAY);
		setHorizontalAlignment(RIGHT);
	}

	public void setValue(Object value) {
		setText((value == null) ? "" : formatter.format(value));
	}
}

@SuppressWarnings("serial")
class IntegerEditor extends DefaultCellEditor {

	JFormattedTextField ftf;
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	private boolean DEBUG = false;
	private static final String ERRMSG = String.format("Valid values are integers between %d - %d",
			WeightedDistributionController.MIN_WEIGHT, WeightedDistributionController.MAX_WEIGHT);

	public IntegerEditor() {
		super(new JFormattedTextField());
		ftf = (JFormattedTextField) getComponent();
		NumberFormatter intFormatter = new NumberFormatter(intFormat);
		intFormatter.setMinimum(WeightedDistributionController.MIN_WEIGHT);
		intFormatter.setMaximum(WeightedDistributionController.MAX_WEIGHT);
		ftf.setFormatterFactory(new DefaultFormatterFactory(intFormatter));
		ftf.setValue(0);
		ftf.setHorizontalAlignment(JTextField.TRAILING);
		ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

		// React when the user presses Enter while the editor is
		// active. (Tab is handled as specified by
		// JFormattedTextField's focusLostBehavior property.)
		ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
		ftf.getActionMap().put("check", new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!ftf.isEditValid()) { // The text is invalid.
					JMeterUtils.reportErrorToUser(ERRMSG);
					ftf.postActionEvent();
					/*
					 * if (userSaysRevert()) { //reverted ftf.postActionEvent();
					 * //inform the editor }
					 */
				} else {
					try { // The text is valid,
						ftf.commitEdit(); // so use it.
						ftf.postActionEvent(); // stop editing
					} catch (java.text.ParseException exc) {
						//TODO: something
					}
				}
			}
		});
	}

	// Override to invoke setValue on the formatted text field.
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		JFormattedTextField ftf = (JFormattedTextField) super.getTableCellEditorComponent(table, value, isSelected, row,
				column);
		ftf.setValue(value);
		return ftf;
	}

	// Override to ensure that the value remains an Integer.
	public Object getCellEditorValue() {
		JFormattedTextField ftf = (JFormattedTextField) getComponent();
		Object o = ftf.getValue();
		if (o instanceof Integer) {
			return o;
		} else if (o instanceof Number) {
			return new Integer(((Number) o).intValue());
		} else {
			if (DEBUG) {
				System.out.println("getCellEditorValue: o isn't a Number");
			}
			try {
				return intFormat.parseObject(o.toString());
			} catch (ParseException exc) {
				System.err.println("getCellEditorValue: can't parse o: " + o);
				return null;
			}
		}
	}

	// Override to check whether the edit is valid,
	// setting the value if it is and complaining if
	// it isn't. If it's OK for the editor to go
	// away, we need to invoke the superclass's version
	// of this method so that everything gets cleaned up.
	public boolean stopCellEditing() {
		JFormattedTextField ftf = (JFormattedTextField) getComponent();
		if (ftf.isEditValid()) {
			try {
				ftf.commitEdit();
			} catch (java.text.ParseException exc) {
			}

		} else { // text is invalid
			JMeterUtils.reportErrorToUser(ERRMSG);
			return false;
			/*
			 * if (!userSaysRevert()) { //user wants to edit return false;
			 * //don't let the editor go away }
			 */
		}
		return super.stopCellEditing();
	}
}
