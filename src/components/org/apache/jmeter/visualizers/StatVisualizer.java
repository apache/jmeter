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
public class StatVisualizer extends AbstractVisualizer
        implements Scrollable, AccumListener, Clearable
{
    // protected NamePanel namePanel;
    // protected GraphAccum graph;
    // protected JPanel legendPanel;
    /****************************************
     * !ToDo (Field description)
     ***************************************/
    protected JTable myJTable;

    /****************************************
     * !ToDo (Field description)
     ***************************************/
    protected JScrollPane myScrollPane;
    private final static String VISUALIZER_NAME =
            JMeterUtils.getResString("aggregate_report");
    private long sleepTill = 0;
    private static int width = 2000;
    // private boolean data = true;
    // private boolean average = true;
    // private boolean deviation = true;
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
        model.clear();
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param s  !ToDo (Parameter description)
     ***************************************/
    public void updateGui(RunningSample s)
    {
        myStatTableModel.rowChanged(s.getIndex());
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
        JLabel panelTitleLabel = new JLabel(VISUALIZER_NAME);
        Font curFont = panelTitleLabel.getFont();
        int curFontSize = curFont.getSize();

        curFontSize += 4;
        panelTitleLabel.setFont(
                new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
        mainPanel.add(panelTitleLabel);
        mainPanel.add(getFilePanel());
        myStatTableModel = new StatTableModel(model);
        // SortFilterModel mySortedModel = new SortFilterModel(myStatTableModel);
        myJTable = new JTable(myStatTableModel);
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        myScrollPane = new JScrollPane(myJTable);
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(myScrollPane, BorderLayout.CENTER);
    }

    /****************************************
     * Class which implements the model for our main table in this
     * visualizer.
     *
     * @author    $Author$
     * @created   $Date$
     * @version   $Revision$
     ***************************************/
    class StatTableModel extends AbstractTableModel
    {
        private final String[] columnNames =
                { "URL", "Count", "Average", "Min", "Max", "Error%", "Rate" };
        private final Class[] columnClasses =
                { String.class, Long.class, Long.class, Long.class, Long.class, String.class, String.class };
        private final String TOTAL_LABEL = JMeterUtils.getResString("aggregate_report_total_label");

        private transient StatVisualizerModel model;
        private int currentRowCount = 0;

        /****************************************
         * !ToDo (Constructor description)
         ***************************************/
        public StatTableModel(StatVisualizerModel model)
        {
            super();
            this.model = model;
        }

        public void rowChanged(int index)
        {
            TableModelEvent event;

            // Create the table changed event, carefully handling the case where the
            // table grows beyond its current known size:
            synchronized (this)
            {
                if (index >= currentRowCount - 1)
                {
                    event = new TableModelEvent(this, currentRowCount - 1, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
                    currentRowCount = index + 2;
                }
                else event = new TableModelEvent(this, index);
            }
            // Fire the event:
            fireTableChanged(event);
            // No matter which row changes, the totals row will have changed too:
            fireTableChanged(new TableModelEvent(this, currentRowCount));
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
            currentRowCount = model.getRunningSampleCount() + 1;
            return currentRowCount;
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
            RunningSample s;

            if (row == model.getRunningSampleCount())
            {
                if (col == 0) return TOTAL_LABEL;
                s = model.getRunningSampleTotal();
            }
            else
            {
                s = model.getRunningSample(row);
            }

            switch (col)
            {
            case 0:
                return s.getLabel();

            case 1:
                return new Long(s.getNumSamples());

            case 2:
                return new Long(s.getAverage());

            case 3:
                return new Long(s.getMin());

            case 4:
                return new Long(s.getMax());

            case 5:
                return s.getErrorPercentageString();

            case 6:
                return s.getRateString();

            default:
                return "__ERROR__";
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
            return columnClasses[c];
        }

        /****************************************
         * !ToDo (Method description)
         ***************************************/
        public void clear()
        {
            fireTableDataChanged();
        }
    }
    // class StatTableModel
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
    {}

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
                }
                );
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
