// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
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
 * @version   $Revision$ on $Date$
 */
public class StatVisualizer extends AbstractVisualizer
        implements AccumListener, Clearable
{
    protected JTable myJTable;

    protected JScrollPane myScrollPane;
    transient private StatVisualizerModel model;
    transient private StatTableModel myStatTableModel;

    public StatVisualizer()
    {
        super();
        model = new StatVisualizerModel();
        model.addAccumListener(this);
        init();
    }

    public String getLabelResource()
    {
        return "aggregate_report";
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

        //sleepTill = 0;
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
