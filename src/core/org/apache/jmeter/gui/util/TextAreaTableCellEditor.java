package org.apache.jmeter.gui.util;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class TextAreaTableCellEditor implements TableCellEditor,FocusListener {
	JScrollPane pane;
	JTextArea editor;
	String value = "";
	LinkedList listeners = new LinkedList();
	int row,col;
	
	public Component getTableCellEditorComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             int row,
                                             int column)
    {
    	editor = new JTextArea(value.toString());
    	editor.addFocusListener(this);
    	editor.setEnabled(true);
    	editor.setRows(editor.getRows());
    	editor.revalidate();
    	pane = new JScrollPane(editor,JScrollPane.VERTICAL_SCROLLBAR_NEVER,
    			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    	pane.validate();
    	this.row = row;
    	this.col = col;
    	return pane;
    }
    
    public int getColumn()
    {
    	return col;
    }
    
    public int getRow()
    {
    	return row;
    }
    
    public void focusLost(FocusEvent fe)
    {
    	stopCellEditing();
    }
    
    public void focusGained(FocusEvent fe)
    {
    }
    
    public TextAreaTableCellEditor()
    {
    	editor = new JTextArea();
    	editor.setRows(3);
    }
    
    public Component getComponent()
    {
    	return editor;
    }
    
    public Object getCellEditorValue()
    {
    	return editor.getText();
    }
    
    public void cancelCellEditing()
    {
    	Iterator iter = ((List)listeners.clone()).iterator();
    	while(iter.hasNext())
    	{
    		((CellEditorListener)iter.next()).editingCanceled(new ChangeEvent(this));
    	}
    }
    
    public boolean stopCellEditing()
    {
    	Iterator iter = ((List)listeners.clone()).iterator();
    	while(iter.hasNext())
    	{
    		((CellEditorListener)iter.next()).editingStopped(new ChangeEvent(this));
    	}
    	return true;
    }
    
    public void addCellEditorListener(CellEditorListener lis)
    {
    	listeners.add(lis);
    }
    
    public boolean isCellEditable(EventObject anEvent)
    {
    	if (anEvent instanceof MouseEvent)
		{
			if (((MouseEvent)anEvent).getClickCount() > 0)
			{
				return true;
			}
		}
		else if(anEvent instanceof FocusEvent)
		{
			if(((FocusEvent)anEvent).getID() == FocusEvent.FOCUS_GAINED)
			{
				return true;
			}
		}
		return true;
    }
    
    public void removeCellEditorListener(CellEditorListener lis)
    {
    	listeners.remove(lis);
    }
    
    public boolean shouldSelectCell(EventObject eo)
    {
    	return true;
    }

}
