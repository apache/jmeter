package org.apache.jmeter.control.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.WeightedDistributionController;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.gui.GuiUtils;

public class WeightedDistributionControllerGui extends AbstractControllerGui {
	private static final long serialVersionUID = 2245012323333943250L;

	protected static final int ENABLED_COLUMN = 0;
	protected static final int ELEMENT_NAME_COLUMN = 1;
	protected static final int WEIGHT_COLUMN = 2;
	protected static final int PERCENT_COLUMN = 3;
	protected static final int HIDDEN_CHILD_NODE_IDX_COLUMN = 4;

	protected static final int HIDDEN_COLUMN_WIDTH = 0;
	protected static final int NUMERIC_COLUMN_WIDTH = 125;

	protected static final String[] COLUMN_NAMES = { "Enabled", "Element Name", String.format("Weight (%d-%d)",
			WeightedDistributionController.MIN_WEIGHT, WeightedDistributionController.MAX_WEIGHT), "Percentage", null };

	private JTable table;

	public WeightedDistributionControllerGui() {
		super();
		init();
	}

	protected JTable getTable() {
		return this.table;
	}

	@Override
	public TestElement createTestElement() {
		WeightedDistributionController wdc = new WeightedDistributionController();
		modifyTestElement(wdc);
		return wdc;
	}

	@Override
	public String getLabelResource() {
		return getClass().getName();
	}

	@Override
	public String getStaticLabel() {
		return "Weighted Distribution Controller";
	}

	@Override
	public void modifyTestElement(TestElement el) {
		GuiUtils.stopTableEditing(getTable());
		Data model = ((PowerTableModel) getTable().getModel()).getData();
		model.reset();
		if (el instanceof WeightedDistributionController && model.size() > 0) {
			WeightedDistributionController wdc = (WeightedDistributionController) el;
			if (wdc.getNode() != null) {
				while (model.next()) {
					int childNodeIdx = (int) model.getColumnValue(HIDDEN_CHILD_NODE_IDX_COLUMN);
					TestElement currTestElement = ((JMeterTreeNode) wdc.getNode().getChildAt(childNodeIdx))
							.getTestElement();
					currTestElement.setProperty(WeightedDistributionController.WEIGHT,
							(int) model.getColumnValue(WEIGHT_COLUMN));
					currTestElement.setName((String) model.getColumnValue(ELEMENT_NAME_COLUMN));
					currTestElement.setEnabled((boolean) model.getColumnValue(ENABLED_COLUMN));
				}
			}
		}
		this.configureTestElement(el);
	}

	@Override
	public void configure(TestElement el) {
		super.configure(el);
		((PowerTableModel) getTable().getModel()).clearData();
		if (el instanceof WeightedDistributionController) {
			WeightedDistributionController wdc = (WeightedDistributionController) el;
			if (wdc.getNode() != null) {
				wdc.resetCumulativeProbability();
				for (int childNodeIdx = 0; childNodeIdx < wdc.getNode().getChildCount(); childNodeIdx++) {
					JMeterTreeNode currNode = (JMeterTreeNode) wdc.getNode().getChildAt(childNodeIdx);
					TestElement currTestElement = currNode.getTestElement();
					if (currTestElement instanceof Controller || currTestElement instanceof Sampler) {
						int weight = currTestElement.getPropertyAsInt(WeightedDistributionController.WEIGHT,
								WeightedDistributionController.DFLT_WEIGHT);
						((PowerTableModel) getTable().getModel()).addRow(
								new Object[] {
										currTestElement.isEnabled(),
										currTestElement.getName(),
										weight,
										currTestElement.isEnabled() ? wdc.calculateProbability(weight) : 0.0f, 
										childNodeIdx
										});
					}
				}
			}
		}
	}

	public void updatePercentageColumn() {

	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(createTablePanel(), BorderLayout.CENTER);
		// Force the table to be at least 70 pixels high
		add(Box.createVerticalStrut(70), BorderLayout.WEST);
		add(createRandomSeedPanel(), BorderLayout.SOUTH);
	}

	private Component createRandomSeedPanel() {
		Box seedPanel = Box.createHorizontalBox();
		JLabel seedLabel = new JLabel("Seed for Random function");//$NON-NLS-1$
		seedPanel.add(seedLabel);

		JTextField seedField = new JTextField(0);
		seedField.setName("seed field");
		seedPanel.add(seedField);

		return seedPanel;
	}

	@SuppressWarnings({ "serial" })
	private Component createTablePanel() {
		TableModel tableModel = new EventFiringPowerTableModel(COLUMN_NAMES,
				new Class[] { Boolean.class, String.class, Integer.class, Float.class, Integer.class }) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column != PERCENT_COLUMN && column != HIDDEN_CHILD_NODE_IDX_COLUMN;
			}
		};

		table = new JTable(tableModel);
		Font defaultFont = table.getTableHeader().getFont();
		table.getTableHeader().setFont(new Font("Bold", Font.BOLD, defaultFont.getSize()));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		;
		table.getColumnModel().getColumn(ENABLED_COLUMN).setPreferredWidth(NUMERIC_COLUMN_WIDTH);
		table.getColumnModel().getColumn(ENABLED_COLUMN).setMaxWidth(NUMERIC_COLUMN_WIDTH);
		table.getColumnModel().getColumn(ENABLED_COLUMN).setResizable(false);
		table.getColumnModel().getColumn(WEIGHT_COLUMN).setPreferredWidth(NUMERIC_COLUMN_WIDTH);
		table.getColumnModel().getColumn(WEIGHT_COLUMN).setMaxWidth(NUMERIC_COLUMN_WIDTH);
		table.getColumnModel().getColumn(WEIGHT_COLUMN).setResizable(false);
		table.getColumnModel().getColumn(WEIGHT_COLUMN).setCellEditor(new IntegerEditor());
		table.getColumnModel().getColumn(PERCENT_COLUMN).setPreferredWidth(NUMERIC_COLUMN_WIDTH);
		table.getColumnModel().getColumn(PERCENT_COLUMN).setMaxWidth(NUMERIC_COLUMN_WIDTH);
		table.getColumnModel().getColumn(PERCENT_COLUMN).setResizable(false);
		table.getColumnModel().getColumn(PERCENT_COLUMN).setCellRenderer(new IneditablePercentageRenderer());
		table.getColumnModel().getColumn(HIDDEN_CHILD_NODE_IDX_COLUMN).setMinWidth(HIDDEN_COLUMN_WIDTH);
		table.getColumnModel().getColumn(HIDDEN_CHILD_NODE_IDX_COLUMN).setMaxWidth(HIDDEN_COLUMN_WIDTH);
		table.getColumnModel().getColumn(HIDDEN_CHILD_NODE_IDX_COLUMN).setResizable(false);
		table.getModel().addTableModelListener(new WeightedDistributionTableModelListener());
		return makeScrollPane(table);
	}

	public static boolean isCurrentElementWeightedDistributionController() {
		return GuiPackage.getInstance().getCurrentElement() instanceof WeightedDistributionController;
	}
}