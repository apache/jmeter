package org.apache.jmeter.gui.util;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class TextAreaCellRenderer implements TableCellRenderer {

	private JTextArea rend = new JTextArea("");
	/**
	 * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable arg0,
		Object arg1,
		boolean arg2,
		boolean arg3,
		int arg4,
		int arg5) 
	{
		rend = new JTextArea(arg1.toString());
		rend.revalidate();
		if(!arg3 && !arg2)
		{
			rend.setBackground(JMeterColor.LAVENDER);
		}
		if(arg0.getRowHeight(arg4) < getPreferredHeight())
		{
			arg0.setRowHeight(arg4,getPreferredHeight());
		}
		return rend;
	}
	
	public int getPreferredHeight()
	{
		return rend.getPreferredSize().height + 5;
	}

}
