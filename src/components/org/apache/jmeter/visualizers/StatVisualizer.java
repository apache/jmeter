/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.visualizers;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Scrollable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.layout.VerticalLayout;
/****************************************
 * Title: StatVisualizer.java Description: Aggregrate Table-Based Reporting
 * Visualizer for JMeter Props to the people who've done the other visualizers
 * ahead of me (Stefano Mazzocchi), who I borrowed code from to start me off
 * (and much code may still exist).. Thank you! Copyright: Copyright (c) 2001
 * Company: Apache Foundation
 *
 *@author    James Boutcher
 *@created   $Date$
 *@version   1.0
 ***************************************/
public class StatVisualizer
	extends AbstractVisualizer
	implements Scrollable, AccumListener, Clearable
{
	//    protected NamePanel namePanel;
	//    protected GraphAccum graph;
	//    protected JPanel legendPanel;
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected JTable myJTable;
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected JScrollPane myScrollPane;
	private final static String VISUALIZER_NAME =
		JMeterUtils.getResString("Aggregate Report");
	private long sleepTill = 0;
	private static int width = 2000;
	//    private boolean data = true;
	//    private boolean average = true;
	//    private boolean deviation = true;
	transient private StatVisualizerModel model;
	transient private StatTableModel myStatTableModel;
	/****************************************
	 * Constructor for the Graph object
	 ***************************************/
	public StatVisualizer()
	{
		super();
		model = new StatVisualizerModel();
		model.addAccumListener(this);
		this.setPreferredSize(new Dimension(width, 800));
		init();
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return VISUALIZER_NAME;
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param res  !ToDo (Parameter description)
	 ***************************************/
	public void add(SampleResult res)
	{
		model.addNewSample(res);
	}
	/****************************************
	 * Gets the PreferredScrollableViewportSize attribute of this Visualizer
	 *
	 *@return   The PreferredScrollableViewportSize value
	 ***************************************/
	public Dimension getPreferredScrollableViewportSize()
	{
		return this.getPreferredSize();
	}
	/****************************************
	 * Gets the ScrollableUnitIncrement attribute of the Visualizer
	 *
	 *@param visibleRect  Description of Parameter
	 *@param orientation  Description of Parameter
	 *@param direction    Description of Parameter
	 *@return             The ScrollableUnitIncrement value
	 ***************************************/
	public int getScrollableUnitIncrement(
		Rectangle visibleRect,
		int orientation,
		int direction)
	{
		// yanked this from some other visualizer - along with most of the core GUI stuff. not my bag.
		return 5;
	}
	/****************************************
	 * Gets the ScrollableBlockIncrement attribute of the Visualizer
	 *
	 *@param visibleRect  Description of Parameter
	 *@param orientation  Description of Parameter
	 *@param direction    Description of Parameter
	 *@return             The ScrollableBlockIncrement value
	 ***************************************/
	public int getScrollableBlockIncrement(
		Rectangle visibleRect,
		int orientation,
		int direction)
	{
		return (int) (visibleRect.width * .9);
	}
	/****************************************
	 * Gets the ScrollableTracksViewportWidth attribute of the Visualizer
	 *
	 *@return   The ScrollableTracksViewportWidth value
	 ***************************************/
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}
	/****************************************
	 * Gets the ScrollableTracksViewportHeight attribute of the Visualizer
	 *
	 *@return   The ScrollableTracksViewportHeight value
	 ***************************************/
	public boolean getScrollableTracksViewportHeight()
	{
		return true;
	}
	/****************************************
	 * Clears this visualizer, it model, and forces a repaint of the table
	 ***************************************/
	public void clear()
	{
		myStatTableModel.clear();
		myJTable.tableChanged(new TableModelEvent(myStatTableModel));
		model.clear();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param s  !ToDo (Parameter description)
	 ***************************************/
	public void updateGui(RunningSample s)
	{
		updateChart(myStatTableModel,s);
		myStatTableModel.fireTableDataChanged();
	}
	// overrides AbstractVisualizer
	// forces GUI update after sample file has been read
	public TestElement createTestElement()
	{
		TestElement t = super.createTestElement();
		sleepTill = 0;
		return t;
	}
	/****************************************
	 * Main method to update the chart with data contained in the passed-in-map. No
	 * matter how quickly you repeatedly call this method, the table will only be
	 * updated at most once per second.
	 *
	 *@param aTable   !ToDo (Parameter description)
	 *@param dataset  !ToDo (Parameter description)
	 *@return         A flag whether or not the graph was updated at all
	 ***************************************/
	public synchronized boolean updateChart(StatTableModel aTable, RunningSample rs)
	{
		int ridx = aTable.getRowWithKey(rs.getLabel());
			aTable.setValueAt(rs.getLabel(), ridx, 0);
			aTable.setValueAt(new Long(rs.getNumSamples()), ridx, 1);
			aTable.setValueAt(new Long(rs.getAverage()), ridx, 2);
			aTable.setValueAt(new Long(rs.getMin()), ridx, 3);
			aTable.setValueAt(new Long(rs.getMax()), ridx, 4);
			aTable.setValueAt(rs.getErrorPercentageString(), ridx, 5);
			aTable.setValueAt(rs.getRateString(), ridx, 6);
			if (rs.getErrorPercentage() > .5)
			{
				// have some fun with cell renderers later, change this text to red or something.
				// here is where the logic would be.
			}
		// while
		// we ended up updating the data in the table, so we'll return true so our caller can force a repaint
		// of components
		return (true);
	}
	/****************************************
	 * Main visualizer setup..
	 ***************************************/
	private void init()
	{
		this.setLayout(new BorderLayout());
		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
		// TITLE
		JLabel panelTitleLabel = new JLabel("Aggregate Report");
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(
			new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);
		mainPanel.add(getFilePanel());
		myStatTableModel = new StatTableModel();
		//        SortFilterModel mySortedModel = new SortFilterModel(myStatTableModel);
		myJTable = new JTable(myStatTableModel);
		myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		myScrollPane = new JScrollPane(myJTable);
		this.add(mainPanel, BorderLayout.NORTH);
		this.add(myScrollPane, BorderLayout.CENTER);
	}
}
// class StatVisualizer
/****************************************
 * Pulled this mainly out of a Core Java book to implement a sorted table -
 * haven't implemented this yet, it needs some non-trivial work done to it to
 * support our dynamically-sizing TableModel for this visualizer.
 *
 *@author    $Author$
 *@created   $Date$
 *@version   $Revision$
 ***************************************/
class SortFilterModel extends AbstractTableModel
{
	private TableModel model;
	private int sortColumn;
	private Row[] rows;
	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param m  !ToDo (Parameter description)
	 ***************************************/
	public SortFilterModel(TableModel m)
	{
		model = m;
		rows = new Row[model.getRowCount()];
		for (int i = 0; i < rows.length; i++)
		{
			rows[i] = new Row();
			rows[i].index = i;
		}
	}
	
	public SortFilterModel()
	{
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param aValue  !ToDo (Parameter description)
	 *@param r       !ToDo (Parameter description)
	 *@param c       !ToDo (Parameter description)
	 ***************************************/
	public void setValueAt(Object aValue, int r, int c)
	{
		model.setValueAt(aValue, rows[r].index, c);
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param r  !ToDo (Parameter description)
	 *@param c  !ToDo (Parameter description)
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Object getValueAt(int r, int c)
	{
		return model.getValueAt(rows[r].index, c);
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param r  !ToDo (Parameter description)
	 *@param c  !ToDo (Parameter description)
	 *@return   !ToDo (Return description)
	 ***************************************/
	public boolean isCellEditable(int r, int c)
	{
		return model.isCellEditable(rows[r].index, c);
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int getRowCount()
	{
		return model.getRowCount();
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int getColumnCount()
	{
		return model.getColumnCount();
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param c  !ToDo (Parameter description)
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getColumnName(int c)
	{
		return model.getColumnName(c);
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param c  !ToDo (Parameter description)
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Class getColumnClass(int c)
	{
		return model.getColumnClass(c);
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param c  !ToDo (Parameter description)
	 ***************************************/
	public void sort(int c)
	{
		sortColumn = c;
		Arrays.sort(rows);
		fireTableDataChanged();
	}
	/****************************************
	 * !ToDo
	 *
	 *@param table  !ToDo
	 ***************************************/
	public void addMouseListener(final JTable table)
	{
		table.getTableHeader().addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent event)
			{
				if (event.getClickCount() < 2)
				{
					return;
				}
				int tableColumn = table.columnAtPoint(event.getPoint());
				int modelColumn = table.convertColumnIndexToModel(tableColumn);
				sort(modelColumn);
			}
		});
	}
	/****************************************
	 * !ToDo (Class description)
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	private class Row implements Comparable
	{
		/****************************************
		 * !ToDo (Field description)
		 ***************************************/
		public int index;
		/****************************************
		 * !ToDo (Method description)
		 *
		 *@param other  !ToDo (Parameter description)
		 *@return       !ToDo (Return description)
		 ***************************************/
		public int compareTo(Object other)
		{
			Row otherRow = (Row) other;
			Object a = model.getValueAt(index, sortColumn);
			Object b = model.getValueAt(otherRow.index, sortColumn);
			if (a instanceof Comparable)
			{
				return ((Comparable) a).compareTo(b);
			}
			else
			{
				return index - otherRow.index;
			}
		}
	}
}
// class SortFilterModel
/****************************************
 * Class which implements the model for our main table in this visualizer.
 *
 *@author    $Author$
 *@created   $Date$
 *@version   $Revision$
 ***************************************/
class StatTableModel extends AbstractTableModel
{
	final String[] columnNames =
		{ "URL", "Count", "Average", "Min", "Max", "Error%", "Rate" };
	Vector data;
	Map rowValues;
	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public StatTableModel()
	{
		super();
		data = new Vector();
		rowValues = new HashMap();
	}
	
	public int getRowWithKey(String key)
	{
		Integer row = (Integer)rowValues.get(key);
		if(row == null)
		{
			return data.size();
		}
		else
		{
			return row.intValue();
		}
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param value  !ToDo (Parameter description)
	 *@param row    !ToDo (Parameter description)
	 *@param col    !ToDo (Parameter description)
	 ***************************************/
	public void setValueAt(Object value, int row, int col)
	{
		Object[] temp;
		if(col == 0)
		{
			if(!rowValues.containsKey(value))
			{
				rowValues.put(value,new Integer(row));
			}
		}
		//Extends the size of the vector as needed (can be used for append)
		if (row >= data.size())
		{
			data.setSize(row + 1);
		}
		//Check if the line is not empty
		if ((Object[]) (data.get(row)) == null)
		{
			temp = new Object[this.getColumnCount()];
		}
		else
		{
			temp = (Object[]) (data.get(row));
		}
		temp[col] = value;
		//Columns are stored as an array
		data.set(row, temp);
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int getColumnCount()
	{
		return columnNames.length;
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int getRowCount()
	{
		return data.size();
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param col  !ToDo (Parameter description)
	 *@return     !ToDo (Return description)
	 ***************************************/
	public String getColumnName(int col)
	{
		return columnNames[col];
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param row  !ToDo (Parameter description)
	 *@param col  !ToDo (Parameter description)
	 *@return     !ToDo (Return description)
	 ***************************************/
	public Object getValueAt(int row, int col)
	{
		//When created, rows are null, need to check that
		if ((((Object[]) data.get(row))[col]) != null)
		{
			return (((Object[]) (data.get(row)))[col]);
		}
		else
		{
			return (" ");
		}
	}
	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param c  !ToDo (Parameter description)
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Class getColumnClass(int c)
	{
		return getValueAt(0, c).getClass();
	}
	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void clear()
	{
		data.clear();
	}
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param row  !ToDo (Parameter description)
	 ***************************************/
	public void insertRowAt(int row)
	{
		data.insertElementAt(new Object[this.getColumnCount()], row);
	}
}
// class StatTableModel
