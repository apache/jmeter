// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
     * @param  listener  the feature to be added to the ModelListener
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
    
    public StatCalculator getStatCalc(){
    	return this.statCalc;
    }
}
