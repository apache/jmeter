package org.apache.jmeter.modifiers.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class UserParametersGui extends AbstractConfigGui {

    private static Logger log = LoggingManager.getLoggerFor(JMeterUtils.GUI);
	private String THREAD_COLUMNS =
		JMeterUtils.getResString("user");

	JTable paramTable;
	PowerTableModel tableModel;
	private int numUserColumns = 1;
	private JButton addParameterButton,
		addUserButton,
		deleteRowButton,
		deleteColumnButton;
    private JCheckBox perIterationCheck;

	public UserParametersGui() {
		super();
		init();
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
	 */
	public String getStaticLabel() {
		return JMeterUtils.getResString("user_parameters_title");
	}
	
	public void configure(TestElement el)
	{
        initTableModel();
        paramTable.setModel(tableModel);
		UserParameters params = (UserParameters)el;
		CollectionProperty names = params.getNames();
        CollectionProperty threadValues = params.getThreadLists();
		tableModel.setColumnData(0,(List)names.getObjectValue());
		PropertyIterator iter = threadValues.iterator();
		if(iter.hasNext())
		{
			tableModel.setColumnData(1,(List)iter.next().getObjectValue());
		}
		int count = 2;
		while(iter.hasNext())
		{
			String colName = THREAD_COLUMNS+"_"+count;
			tableModel.addNewColumn(colName,String.class);
			tableModel.setColumnData(count,(List)iter.next().getObjectValue());
			count++;
		}
        perIterationCheck.setSelected(params.isPerIteration());
		super.configure(el);
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		UserParameters params = new UserParameters();
		modifyTestElement(params);
		return params;
	}

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement params)
    {
        ((UserParameters)params).setNames(new CollectionProperty(UserParameters.NAMES,
                tableModel.getColumnData(JMeterUtils.getResString("name"))));
        CollectionProperty threadLists = new CollectionProperty(UserParameters.THREAD_VALUES,new ArrayList());
        log.debug("making threadlists from gui = " + threadLists);
        for (int x = 1; x < tableModel.getColumnCount(); x++) {
        	threadLists.addItem(tableModel.getColumnData(THREAD_COLUMNS + "_" + x));
            log.debug("Adding column to threadlist: " + tableModel.getColumnData(THREAD_COLUMNS + "_" + x));
            log.debug("Threadlists now = " + threadLists);
        }
        log.debug("In the end, threadlists = " + threadLists);
        ((UserParameters)params).setThreadLists(threadLists);
        ((UserParameters)params).setPerIteration(perIterationCheck.isSelected());
        super.configureTestElement(params);
    }

	private void init() {
		this.setLayout(new BorderLayout());
		add(makeTitlePanel(), BorderLayout.NORTH);
		add(makeParameterPanel(), BorderLayout.CENTER);
	}
    
    protected JPanel makeTitlePanel()
    {
        perIterationCheck = new JCheckBox(JMeterUtils.getResString("update_per_iter"));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(super.makeTitlePanel(),BorderLayout.NORTH);
        panel.add(perIterationCheck,BorderLayout.SOUTH);
        perIterationCheck.setSelected(true);
        return panel;
    }

	private JPanel makeParameterPanel() {
		JPanel paramPanel = new JPanel(new BorderLayout());
		JLabel tableLabel =
			new JLabel(JMeterUtils.getResString("user_parameters_table"));
		initTableModel();
		paramTable = new JTable(tableModel);
		paramTable.setRowSelectionAllowed(true);
		paramTable.setColumnSelectionAllowed(true);
		paramTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paramTable.setCellSelectionEnabled(true);
		paramPanel.add(tableLabel, BorderLayout.NORTH);
		JScrollPane scroller = new JScrollPane(paramTable);
		Dimension tableDim = scroller.getPreferredSize();
		tableDim.height = 70;
		scroller.setPreferredSize(tableDim);
		scroller.setColumnHeaderView(paramTable.getTableHeader());
		paramPanel.add(scroller, BorderLayout.CENTER);
		paramPanel.add(makeButtonPanel(), BorderLayout.SOUTH);
		return paramPanel;
	}

    protected void initTableModel()
    {
        tableModel =
        	new PowerTableModel(
        		new String[] {
        			JMeterUtils.getResString("name"),
        			THREAD_COLUMNS + "_" + numUserColumns },
        		new Class[] { String.class, String.class });
    }

	private JPanel makeButtonPanel() {
		JPanel buttonPanel = new JPanel();
		addParameterButton =
			new JButton(JMeterUtils.getResString("add_parameter"));
		addUserButton = new JButton(JMeterUtils.getResString("add_user"));
		deleteRowButton =
			new JButton(JMeterUtils.getResString("delete_parameter"));
		deleteColumnButton =
			new JButton(JMeterUtils.getResString("delete_user"));
		buttonPanel.add(addParameterButton);
		buttonPanel.add(addUserButton);
		buttonPanel.add(deleteRowButton);
		buttonPanel.add(deleteColumnButton);
		addParameterButton.addActionListener(new AddParamAction());
		addUserButton.addActionListener(new AddUserAction());
		deleteRowButton.addActionListener(new DeleteRowAction());
		deleteColumnButton.addActionListener(new DeleteColumnAction());
		return buttonPanel;
	}

	private class AddParamAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (paramTable.isEditing()) {
				TableCellEditor cellEditor =
					paramTable.getCellEditor(
						paramTable.getEditingRow(),
						paramTable.getEditingColumn());
				cellEditor.stopCellEditing();
			}

			tableModel.addNewRow();
			tableModel.fireTableDataChanged();

			// Enable DELETE (which may already be enabled, but it won't hurt)
			deleteRowButton.setEnabled(true);

			// Highlight (select) the appropriate row.
			int rowToSelect = tableModel.getRowCount() - 1;
			paramTable.setRowSelectionInterval(rowToSelect, rowToSelect);
		}
	}

	private class AddUserAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			if (paramTable.isEditing()) {
				TableCellEditor cellEditor =
					paramTable.getCellEditor(
						paramTable.getEditingRow(),
						paramTable.getEditingColumn());
				cellEditor.stopCellEditing();
			}

			tableModel.addNewColumn(
				THREAD_COLUMNS + "_" + tableModel.getColumnCount(),
				String.class);
			tableModel.fireTableDataChanged();

			// Enable DELETE (which may already be enabled, but it won't hurt)
			deleteColumnButton.setEnabled(true);

			// Highlight (select) the appropriate row.
			int colToSelect = tableModel.getColumnCount() - 1;
			paramTable.setColumnSelectionInterval(colToSelect, colToSelect);
		}
	}

	private class DeleteRowAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (paramTable.isEditing()) {
				TableCellEditor cellEditor =
					paramTable.getCellEditor(
						paramTable.getEditingRow(),
						paramTable.getEditingColumn());
				cellEditor.cancelCellEditing();
			}

			int rowSelected = paramTable.getSelectedRow();
			if (rowSelected >= 0) {
				tableModel.removeRow(rowSelected);
				tableModel.fireTableDataChanged();

				// Disable DELETE if there are no rows in the table to delete.
				if (tableModel.getRowCount() == 0) {
					deleteRowButton.setEnabled(false);
				}

				// Table still contains one or more rows, so highlight (select)
				// the appropriate one.
				else {
					int rowToSelect = rowSelected;

					if (rowSelected >= tableModel.getRowCount()) {
						rowToSelect = rowSelected - 1;
					}

					paramTable.setRowSelectionInterval(
						rowToSelect,
						rowToSelect);
				}
			}
		}
	}

	private class DeleteColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (paramTable.isEditing()) {
				TableCellEditor cellEditor =
					paramTable.getCellEditor(
						paramTable.getEditingRow(),
						paramTable.getEditingColumn());
				cellEditor.cancelCellEditing();
			}

			int colSelected = paramTable.getSelectedColumn();
			if(colSelected == 0 || colSelected == 1)
			{
				JOptionPane.showMessageDialog(null, 
						JMeterUtils.getResString("column_delete_disallowed"), "Error", 
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (colSelected >= 0) {
				tableModel.removeColumn(colSelected);
				tableModel.fireTableDataChanged();

				// Disable DELETE if there are no rows in the table to delete.
				if (tableModel.getColumnCount() == 0) {
					deleteColumnButton.setEnabled(false);
				}

				// Table still contains one or more rows, so highlight (select)
				// the appropriate one.
				else {

					if (colSelected >= tableModel.getColumnCount()) {
						colSelected = colSelected - 1;
					}

					paramTable.setColumnSelectionInterval(
						colSelected,
						colSelected);
				}
			}
		}
	}
}
