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
package org.apache.jmeter.visualizers; // java
import java.util.*; // apache
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
/****************************************
 * Title: StatVisualizerModel.java Description: Aggregrate Table-Based Reporting
 * Model for JMeter Props to the people who've done the other visualizers ahead
 * of me (Stefano Mazzocchi), who I borrowed code from to start me off (and much
 * code may still exist).. Thank you! Copyright: Copyright (c) 2001 Company:
 * Apache Foundation
 *
 *@author    James Boutcher
 *@created   March 21, 2002
 *@version   1.0
 ***************************************/
public class StatVisualizerModel implements Clearable
{
	private String name;
	private List listeners;
	private Map labelMap;
	/****************************************
	 * Default Constuctor
	 ***************************************/
	public StatVisualizerModel()
	{
		listeners = new LinkedList();
		labelMap = Collections.synchronizedMap(new HashMap(10));
	}
	/****************************************
	 * Sets the Name attribute of the StatVisualizerModel object
	 *
	 *@param name  The new Name value
	 ***************************************/
	public void setName(String name)
	{
		this.name = name;
	}
	/****************************************
	 * Returns the Map containing the Samples we've collected and their
	 * corresponding RunningSample instance.
	 *
	 *@return   The URLStats value
	 ***************************************/
	public Map getURLStats()
	{
		return (labelMap);
	}
	/****************************************
	 * Gets the GuiClass attribute of the StatVisualizerModel object
	 *
	 *@return   The GuiClass value
	 ***************************************/
	public Class getGuiClass()
	{
		return StatVisualizer.class;
	}
	/****************************************
	 * Gets the Name attribute of the StatVisualizerModel object
	 *
	 *@return   The Name value
	 ***************************************/
	public String getName()
	{
		return name;
	}
	/****************************************
	 * Registers a listener (a visualizer, graph, etc) to this model. This will
	 * allow the model to fire GUI updates to anyone when data changes, etc.
	 *
	 *@param listener       !ToDo
	 ***************************************/
	public void addGraphListener(GraphListener listener)
	{
		listeners.add(listener);
	}
	
	public void addAccumListener(AccumListener listener)
	{
		listeners.add(listener);
	}
	/****************************************
	 * !ToDo
	 *
	 *@param res  !ToDo
	 ***************************************/
	public void addNewSample(SampleResult res)
	{
		String aLabel = res.getSampleLabel();
		String responseCode = res.getResponseCode();
		RunningSample myRS;
		if (labelMap.containsKey(aLabel))
		{
			myRS = (RunningSample) labelMap.get(aLabel);
		}
		else
		{
			// put a new one there..
			myRS = new RunningSample();
			labelMap.put(aLabel, myRS);
		}
		myRS.addSample(res);
		this.fireDataChanged(myRS);
	}
	/****************************************
	 * Reset everything we can in the model.
	 ***************************************/
	public void clear()
	{
		// clear the data structures
		labelMap.clear();
		this.fireDataChanged();
	}
	/****************************************
	 * Called when the model changes - then we call out to all registered listeners
	 * and tell them to update themselves.
	 ***************************************/
	protected void fireDataChanged()
	{
		Iterator iter = listeners.iterator();
		while (iter.hasNext())
		{
			Object myObj = iter.next();
			if (!(myObj instanceof GraphListener))
			{
				continue;
			}
			((GraphListener) myObj).updateGui();
		}
	}
	
	protected void fireDataChanged(RunningSample s)
	{
		Iterator iter = listeners.iterator();
		while (iter.hasNext())
		{
			Object myObj = iter.next();
			if (!(myObj instanceof AccumListener))
			{
				continue;
			}
			((AccumListener) myObj).updateGui(s);
		}
	}
}
