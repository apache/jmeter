/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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


/**
 * Aggregrate Table-Based Reporting Visualizer for JMeter.  Props to the people
 * who've done the other visualizers ahead of me (Stefano Mazzocchi), who I
 * borrowed code from to start me off (and much code may still exist). Thank
 * you!
 *
 * @author    James Boutcher
 * @version   $Revision$
 */
public class StatVisualizer extends AbstractVisualizer
        implements AccumListener, Clearable
{
    protected JTable myJTable;

    protected JScrollPane myScrollPane;
    private final static String VISUALIZER_NAME =
            JMeterUtils.getResString("aggregate_report");
    private long sleepTill = 0;
    transient private StatVisualizerModel model;
    transient private StatTableModel myStatTableModel;

    public StatVisualizer()
    {
        super();
        model = new StatVisualizerModel();
        model.addAccumListener(this);
        init();
    }

    public String getStaticLabel()
    {
        return VISUALIZER_NAME;
    }

    public void add(SampleResult res)
    {
        model.addNewSample(res);
    }

    /**
     * Clears this visualizer and its model, and forces a repaint of the table.
     */
    public void clear()
    {
        myStatTableModel.clear();
        model.clear();
    }

    public synchronized void updateGui(RunningSample s)
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

    /**
     * Main visualizer setup.
     */
    private void init()
    {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(makeTitlePanel());
        myStatTableModel = new StatTableModel(model);
        // SortFilterModel mySortedModel =
        //       new SortFilterModel(myStatTableModel);
        myJTable = new JTable(myStatTableModel);
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        myScrollPane = new JScrollPane(myJTable);
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(myScrollPane, BorderLayout.CENTER);
    }

    /**
     * Class which implements the model for our main table in this
     * visualizer.
     *
     * @version   $Revision$
     */
    class StatTableModel extends AbstractTableModel
    {
        private final String[] columnNames =
            { "URL", "Count", "Average", "Min", "Max", "Error%", "Rate" };
        private final Class[] columnClasses =
            {
                String.class,
                Long.class,
                Long.class,
                Long.class,
                Long.class,
                String.class,
                String.class };
        private final String TOTAL_LABEL =
            JMeterUtils.getResString("aggregate_report_total_label");

        private transient StatVisualizerModel model;
        private int currentRowCount = 0;

        public StatTableModel(StatVisualizerModel model)
        {
            super();
            this.model = model;
        }

        public void rowChanged(int index)
        {
            TableModelEvent event;

            // Create the table changed event, carefully handling the case
            // where the table grows beyond its current known size.
            synchronized (this)
            {
                if (index >= currentRowCount - 1)
                {
                    event =
                        new TableModelEvent(
                            this,
                            currentRowCount - 1,
                            index,
                            TableModelEvent.ALL_COLUMNS,
                            TableModelEvent.INSERT);
                    currentRowCount = index + 2;
                }
                else
                {
                    event = new TableModelEvent(this, index);
                } 
            }
            // Fire the event:
            fireTableChanged(event);
            // No matter which row changes, the totals row will have changed
            // too:
            fireTableChanged(new TableModelEvent(this, currentRowCount));
        }

        public int getColumnCount()
        {
            return columnNames.length;
        }

        public int getRowCount()
        {
            currentRowCount = model.getRunningSampleCount() + 1;
            return currentRowCount;
        }

        public String getColumnName(int col)
        {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col)
        {
            RunningSample s;

            if (row == model.getRunningSampleCount())
            {
                if (col == 0)
                {
                    return TOTAL_LABEL;
                } 
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

        public Class getColumnClass(int c)
        {
            return columnClasses[c];
        }

        public void clear()
        {
            fireTableDataChanged();
        }
    }
}


/**
 * Pulled this mainly out of a Core Java book to implement a sorted table -
 * haven't implemented this yet, it needs some non-trivial work done to it to
 * support our dynamically-sizing TableModel for this visualizer.
 *
 * @version   $Revision$
 */
class SortFilterModel extends AbstractTableModel
{
    private TableModel model;
    private int sortColumn;
    private Row[] rows;

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

    public void setValueAt(Object aValue, int r, int c)
    {
        model.setValueAt(aValue, rows[r].index, c);
    }

    public Object getValueAt(int r, int c)
    {
        return model.getValueAt(rows[r].index, c);
    }

    public boolean isCellEditable(int r, int c)
    {
        return model.isCellEditable(rows[r].index, c);
    }

    public int getRowCount()
    {
        return model.getRowCount();
    }

    public int getColumnCount()
    {
        return model.getColumnCount();
    }

    public String getColumnName(int c)
    {
        return model.getColumnName(c);
    }

    public Class getColumnClass(int c)
    {
        return model.getColumnClass(c);
    }

    public void sort(int c)
    {
        sortColumn = c;
        Arrays.sort(rows);
        fireTableDataChanged();
    }

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

    private class Row implements Comparable
    {
        public int index;

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
}  // class SortFilterModel
