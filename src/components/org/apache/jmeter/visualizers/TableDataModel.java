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


import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;


/**
 * This class implements the TableModel for the information kept by the
 * GraphModel.
 *
 * @author     <a href="mailto:alf@i100.no">Alf Hogemark</a>Hogemark
 * Created      March 10, 2002
 * @version    $Revision$ Last updated: $Date$
 */
public class TableDataModel extends GraphModel implements TableModel
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    List urlList = new ArrayList();

    /**
     * Constructor for the TableDataModel object.
     */
    public TableDataModel()
    {
        super();
    }

    /**
     * Gets the GuiClass attribute of the TableModel object.
     *
     * @return    the GuiClass value
     */
    public Class getGuiClass()
    {
        return TableVisualizer.class;
    }

    public void clear()
    {
        super.clear();
        urlList.clear();
    }

    /**
     * Gets the ClassLabel attribute of the GraphModel object.
     *
     * @return    the ClassLabel value
     */
    public String getClassLabel()
    {
        return JMeterUtils.getResString("view_results_in_table");
    }

    public Sample addNewSample(
        long time,
        long timeStamp,
        boolean success,
        String url)
    {
        Sample s = super.addNewSample(time, timeStamp, success);

        urlList.add(url);
        return s;
    }

    public Sample addSample(SampleResult e)
    {
        Sample s = addNewSample(e.getTime(), e.getTimeStamp(), e.isSuccessful(),
                (String) e.getSampleLabel());

        fireDataChanged();

        return s;
    }

    // Implmentation of the TableModel interface
    public int getRowCount()
    {
        return getSampleCount();
    }

    public int getColumnCount()
    {
        // We have two columns : sampleNo and sampleValue
        return 4;
    }

    public String getColumnName(int columnIndex)
    {
        switch (columnIndex)
        {
        case 0:
            return "SampleNo";

        case 1:
            return JMeterUtils.getResString("url");

        case 2:
            return "Sample - ms";

        case 3:
            return JMeterUtils.getResString("Success?");

        default:
            return null;
        }
    }

    public Class getColumnClass(int columnIndex)
    {
        if (columnIndex == 0)
        {
            return Integer.class;
        }
        else if (columnIndex == 1)
        {
            return String.class;
        }
        else if (columnIndex == 2)
        {
            return Long.class;
        }
        else if (columnIndex == 3)
        {
            return Boolean.class;
        }
        else
        {
            return null;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (columnIndex == 0)
        {
            if ((rowIndex >= 0) && (rowIndex < getSampleCount()))
            {
                return new Integer(rowIndex + 1);
            }
        }
        else if (columnIndex == 1)
        {
            log.debug("rowIndex = " + rowIndex);
            if ((rowIndex >= 0) && (rowIndex < urlList.size()))
            {
                log.debug(" url = " + urlList.get(rowIndex));
                return urlList.get(rowIndex);
            }
        }
        else if (columnIndex == 2)
        {
            if ((rowIndex >= 0) && (rowIndex < getSampleCount()))
            {
                return new Long(((Sample) getSamples().get(rowIndex)).data);
            }
        }
        else if (columnIndex == 3)
        {
            if ((rowIndex >= 0) && (rowIndex < urlList.size()))
            {
                return JOrphanUtils.valueOf(
                    !((Sample) getSamples().get(rowIndex)).error);
            }
        }
        return null;
    }

    /**
     * Dummy implementation.
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {}

    /**
     * Dummy implementation.
     */
    public void addTableModelListener(TableModelListener l)
    {}

    /**
     * Dummy implementation.
     */
    public void removeTableModelListener(TableModelListener l)
    {}
}

