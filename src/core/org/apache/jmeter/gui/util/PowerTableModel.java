package org.apache.jmeter.gui.util;

import java.lang.reflect.Constructor;

import javax.swing.table.DefaultTableModel;
import org.apache.jmeter.util.Data;
/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class PowerTableModel extends DefaultTableModel {
	Data model = new Data();
	Class[] columnClasses;

	public PowerTableModel(String[] headers, Class[] cc) {
		model.setHeaders(headers);
		columnClasses = cc;
	}

	public void setRowValues(int row, Object[] values) {
		model.setCurrentPos(row);
		for (int i = 0; i < values.length; i++) {
			model.addColumnValue(model.getHeaders()[i], values[i]);
		}
	}

	public Data getData() {
		return model;
	}

	/****************************************
		 * Description of the Method
		 *
		 *@param row  Description of Parameter
		 ***************************************/
	public void removeRow(int row) {
		if (model.size() > row) {
			model.removeRow(row);
		}
	}
	
	public void clearData()
	{
		String[] headers = model.getHeaders();
		model = new Data();
		model.setHeaders(headers);
		this.fireTableDataChanged();
	}

	public void addRow(Object data[]) {
		model.setCurrentPos(model.size());
		for (int i = 0; i < data.length; i++) {
			model.addColumnValue(model.getHeaders()[i], data[i]);
		}
	}

	/****************************************
	 ***************************************/
	public void addNewRow() {		
		addRow(createDefaultRow());
	}
	
	private Object[] createDefaultRow()
	{
		Object[] rowData = new Object[getColumnCount()];
		for(int i = 0;i < rowData.length;i++)
		{
			rowData[i] = createDefaultValue(i);
		}
		return rowData;
	}
	
	public Object[] getRowData(int row)
	{
		Object[] rowData = new Object[getColumnCount()];
		for(int i = 0;i < rowData.length;i++)
		{
			rowData[i] = model.getColumnValue(i,row);
		}
		return rowData;
	}
	
	private Object createDefaultValue(int i)
	{
		Class colClass = getColumnClass(i);
		try {
			return colClass.newInstance();
		} catch(Exception e) {
			try {
				Constructor constr = colClass.getConstructor(new Class[]{String.class});
				return constr.newInstance(new Object[]{""});
			} catch(Exception err) {
			} 
			try {
				Constructor constr = colClass.getConstructor(new Class[]{Integer.TYPE});
				return constr.newInstance(new Object[]{new Integer(0)});
			} catch(Exception err) {
			} 
			try {
				Constructor constr = colClass.getConstructor(new Class[]{Long.TYPE});
				return constr.newInstance(new Object[]{new Long(0L)});
			} catch(Exception err) {
			}
			try {
				Constructor constr = colClass.getConstructor(new Class[]{Boolean.TYPE});
				return constr.newInstance(new Object[]{new Boolean(false)});
			} catch(Exception err) {
			}
			try {
				Constructor constr = colClass.getConstructor(new Class[]{Float.TYPE});
				return constr.newInstance(new Object[]{new Float(0F)});
			} catch(Exception err) {
			}
			try {
				Constructor constr = colClass.getConstructor(new Class[]{Double.TYPE});
				return constr.newInstance(new Object[]{new Double(0D)});
			} catch(Exception err) {
			}
			try {
				Constructor constr = colClass.getConstructor(new Class[]{Character.TYPE});
				return constr.newInstance(new Object[]{new Character(' ')});
			} catch(Exception err) {
			}
			try {
				Constructor constr = colClass.getConstructor(new Class[]{Byte.TYPE});
				return constr.newInstance(new Object[]{new Byte(Byte.MIN_VALUE)});
			} catch(Exception err) {
			}
			try {
				Constructor constr = colClass.getConstructor(new Class[]{Short.TYPE});
				return constr.newInstance(new Object[]{new Short(Short.MIN_VALUE)});
			} catch(Exception err) {
			}
		}
		return "";		
	}

	/****************************************
	 * required by table model interface
	 *
	 *@return   The RowCount value
	 ***************************************/
	public int getRowCount() {
		if (model == null) {
			return 0;
		}
		return model.size();
	}

	/****************************************
	 * required by table model interface
	 *
	 *@return   The ColumnCount value
	 ***************************************/
	public int getColumnCount() {
		return model.getHeaders().length;
	}

	/****************************************
	 * required by table model interface
	 *
	 *@param column  Description of Parameter
	 *@return        The ColumnName value
	 ***************************************/
	public String getColumnName(int column) {
		return model.getHeaders()[column];
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param row     !ToDo (Parameter description)
	 *@param column  !ToDo (Parameter description)
	 *@return        !ToDo (Return description)
	 ***************************************/
	public boolean isCellEditable(int row, int column) {
		// all table cells are editable
		return true;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param column  !ToDo (Parameter description)
	 *@return        !ToDo (Return description)
	 ***************************************/
	public Class getColumnClass(int column) {
		return columnClasses[column];
	}

	/****************************************
	 * required by table model interface
	 *
	 *@param row     Description of Parameter
	 *@param column  Description of Parameter
	 *@return        The ValueAt value
	 ***************************************/
	public Object getValueAt(int row, int column) {
		return model.getColumnValue(column, row);
	}

	/****************************************
	 * Sets the ValueAt attribute of the Arguments object
	 *
	 *@param value   The new ValueAt value
	 *@param row     The new ValueAt value
	 *@param column  !ToDo (Parameter description)
	 ***************************************/
	public void setValueAt(Object value, int row, int column) {
		if(row < model.size())
		{
			model.setCurrentPos(row);
			model.addColumnValue(model.getHeaders()[column], value);
		}
	}

}