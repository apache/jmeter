/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache JMeter" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache JMeter", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.jmeter.visualizers;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.math.StatCalculator;

/**
 * @author     Michael Stover
 * Created      February 8, 2001
 * @version    $Revision$ Last updated: $Date$
 */
public class GraphModel implements Clearable, Serializable
{

    private String name;
    private List samples;
    private List listeners;
    //private long previous = 0;
    private boolean bigChange = false;
    private Sample current = new Sample(0, 0, 0, 0, 0,false);
    private long startTime = Long.MAX_VALUE;
    private long endTime = Long.MIN_VALUE;
    private int throughputMax = 20;
    private long graphMax = 20;
    private StatCalculator statCalc = new StatCalculator();

    /**
     * Constructor for the GraphModel object.
     */
    public GraphModel()
    {
        listeners = new LinkedList();
        samples = Collections.synchronizedList(new LinkedList());
    }

    /**
     * Sets the Name attribute of the GraphModel object.
     *
     * @param  name  the new Name value
     */

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the CurrentData attribute of the GraphModel object.
     *
     * @return    the CurrentData value
     */
    public long getCurrentData()
    {
        return current.data;
    }

    /**
     * Gets the CurrentAverage attribute of the GraphModel object.
     *
     * @return    the CurrentAverage value
     */
    public long getCurrentAverage()
    {
        return current.average;
    }
    
    public long getCurrentMedian()
    {
        return current.median;
    }

    /**
     * Gets the CurrentDeviation attribute of the GraphModel object.
     *
     * @return    the CurrentDeviation value
     */
    public long getCurrentDeviation()
    {
        return current.deviation;
    }

    public float getCurrentThroughput()
    {
        return current.throughput;
    }

    /**
     * Gets the SampleCount attribute of the GraphModel object.
     *
     * @return    the SampleCount value
     */
    public int getSampleCount()
    {
        return samples.size();
    }

    /**
     * Gets the Samples attribute of the GraphModel object.
     *
     * @return    the Samples value
     */
    public List getSamples()
    {
        return samples;
    }

    /**
     * Gets the GuiClass attribute of the GraphModel object.
     *
     * @return    the GuiClass value
     */
    public Class getGuiClass()
    {
        return GraphVisualizer.class;
    }

    /**
     * Gets the Name attribute of the GraphModel object.
     *
     * @return    the Name value
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the Max attribute of the GraphModel object.
     *
     * @return    the Max value
     */
    public long getMaxSample()
    {
        return statCalc.getMax().longValue();
    }

    public long getGraphMax()
    {
        return graphMax;
    }

    public int getThroughputMax()
    {
        return throughputMax;
    }

    /**
     * Adds a feature to the ModelListener attribute of the GraphModel object.
     *
     * @param  modelListener  the feature to be added to the ModelListener
     *           attribute
     */
    public void addGraphListener(GraphListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Adds a feature to the Sample attribute of the GraphModel object.
     *
     * @param  e  the feature to be added to the Sample attribute
     */
    public Sample addSample(SampleResult e)
    {
        Sample s =
            addNewSample(e.getTime(), e.getTimeStamp(), e.isSuccessful());

        fireDataChanged();
        return s;
    }

    public void clear()
    {
        samples.clear();
        //previous = 0;
        graphMax = 1;
        bigChange = true;
        current = new Sample(0, 0, 0, 0, 0,false);
        statCalc.clear();
        startTime = Long.MAX_VALUE;
        endTime = Long.MIN_VALUE;
        this.fireDataChanged();
    }

    protected void fireDataChanged()
    {
        Iterator iter = listeners.iterator();

        if (bigChange)
        {
            while (iter.hasNext())
            {
                ((GraphListener) iter.next()).updateGui();
            }
            bigChange = false;
        }
        else
        {
            quickUpdate(current);
        }
    }

    protected void quickUpdate(Sample s)
    {
        Iterator iter = listeners.iterator();
        {
            while (iter.hasNext())
            {
                ((GraphListener) iter.next()).updateGui(s);
            }
        }
    }

    /**
     * Adds a feature to the NewSample attribute of the GraphModel object.
     *
     * @param  sample  the feature to be added to the NewSample attribute
     */
    protected Sample addNewSample(long sample, long timeStamp, boolean success)
    {
        //NOTUSED int counter = 0;
        float average;
        long deviation, median;
        synchronized (statCalc)
        {
            statCalc.addValue(sample);
            //NOTUSED counter = statCalc.getCount();
            average = (float) statCalc.getMean();
            deviation = (long) statCalc.getStandardDeviation();
            median = statCalc.getMedian().longValue();
            long start = timeStamp - sample;
            if (startTime > start)
            {
                startTime = start;
            }
            if(endTime < timeStamp)
            {
                endTime = timeStamp;
            }
        }

        float throughput = 0;

        if (endTime - startTime > 0)
        {
            throughput =
                (float) (((float) (samples.size() + 1))
                    / ((float) (timeStamp - startTime))
                    * 60000);
        }
        if (throughput > throughputMax)
        {
            bigChange = true;
            throughputMax = (int) (throughput * 1.5F);
        }
        if (average > graphMax)
        {
            bigChange = true;
            graphMax = (long) average * 3;
        }
        if (deviation > graphMax)
        {
            bigChange = true;
            graphMax = deviation * 3;
        }
        Sample s =
            new Sample(
                sample,
                (long) average,
                deviation,
                throughput,
                median,
                !success);

        //previous = sample;
        current = s;
        samples.add(s);
        return s;
    }
}
