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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 *  This class implements the TableModel for the information kept
 *  by the GraphModel.
 *
 *@author     <a href="mailto:alf@i100.no">Alf Hogemark</a>Hogemark
 *@created    March 10, 2002
 *@version    1.0
 */
public class TableDataModel extends GraphModel implements TableModel
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.gui");
	List urlList = new ArrayList();

	/**
	 *  Constructor for the TableDataModel object
	 */
	public TableDataModel()
	{
		super();
	}

	/**
	 * Gets the GuiClass attribute of the TableModel object
	 *
	 * @return    The GuiClass value
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
	 * Gets the ClassLabel attribute of the GraphModel object
	 *
	 * @return    The ClassLabel value
	 */
	public String getClassLabel()
	{
		return "View Results in Table";
	}

	public Sample addNewSample(long time,long timeStamp,boolean success,String url)
	{
		Sample s = super.addNewSample(time,timeStamp,success);
		urlList.add(url);
		return s;
	}

	public Sample addSample(SampleResult e)
	{
		Sample s = addNewSample(e.getTime(),e.getTimeStamp(),e.isSuccessful(),
				(String)e.getSampleLabel());
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
		switch(columnIndex)
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
		if(columnIndex == 0)
		{
			return Integer.class;
		}
		else if(columnIndex == 1)
		{
			return String.class;
		}
		else if(columnIndex == 2)
		{
			return Long.class;
		}
		else if(columnIndex == 3)
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
		if(columnIndex == 0)
		{
			if((rowIndex >= 0) && (rowIndex < getSampleCount()))
			{
				return new Integer(rowIndex+1);
			}
		}
		else if(columnIndex == 1)
		{
			log.info("rowIndex = "+rowIndex);
			if((rowIndex >= 0) && (rowIndex < urlList.size()))
			{
				log.info(" url = "+urlList.get(rowIndex));
				return urlList.get(rowIndex);
			}
		}
		else if(columnIndex == 2)
		{
			if((rowIndex >= 0) && (rowIndex < getSampleCount()))
			{
				return new Long(((Sample)getSamples().get(rowIndex)).data);
			}
		}
		else if(columnIndex == 3)
		{
			if((rowIndex >= 0) && (rowIndex < urlList.size()))
			{
				return new Boolean(!((Sample)getSamples().get(rowIndex)).error);
			}
		}
		return null;
	}

	/**
	 * Dummy implementation
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
	}

	/**
	 * Dummy implementation
	 */
	public void addTableModelListener(TableModelListener l)
	{
	}

	/**
	 * Dummy implementation
	 */
	public void removeTableModelListener(TableModelListener l)
	{
	}
}


