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


import java.util.ArrayList;
import java.util.Collection;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;


/**
 * @author     Michael Stover
 * @version    $Revision$
 */
public class SplineModel implements Clearable
{
    public final int DEFAULT_NUMBER_OF_NODES = 10;
    public final int DEFAULT_REFRESH_PERIOD = 1;

    protected final boolean SHOW_INCOMING_SAMPLES = true;
    protected int numberOfNodes = DEFAULT_NUMBER_OF_NODES;
    protected int refreshPeriod = DEFAULT_REFRESH_PERIOD;

    /** Current Spline curve. */
    protected Spline3 dataCurve = null;

    /** Sum of the samples. */
    protected long sum = 0;

    /** Average of the samples. */
    protected long average = 0;

    /** Number of collected samples. */
    protected long n = 0;

    ArrayList samples;

    private GraphListener listener;

    private long minimum = Integer.MAX_VALUE;
    private long maximum = Integer.MIN_VALUE;
    private long incoming;

    private String name;

    public SplineModel()
    {
        samples = new ArrayList();
    }

    public void setListener(GraphListener vis)
    {
        listener = vis;
    }

    public void setName(String newName)
    {
        name = newName;
    }

    public boolean isEditable()
    {
        return true;
    }

    public Spline3 getDataCurve()
    {
        return dataCurve;
    }

    public Class getGuiClass()
    {
        return org.apache.jmeter.visualizers.SplineVisualizer.class;
    }

    public Collection getAddList()
    {
        return null;
    }

    public String getClassLabel()
    {
        return JMeterUtils.getResString("spline_visualizer_title");
    }

    public long getMinimum()
    {
        return minimum;
    }

    public long getMaximum()
    {
        return maximum;
    }

    public long getAverage()
    {
        return average;
    }

    public long getCurrent()
    {
        return incoming;
    }

    public long[] getSamples()
    {
        int n = samples.size();
        long[] longSample = new long[n];

        for (int i = 0; i < n; i++)
        {
            longSample[i] = ((Long) samples.get(i)).longValue();
        }
        return longSample;
    }

    public long getSample(int i)
    {
        Long sample = (Long) this.samples.get(i);

        return sample.longValue();
    }

    public int getNumberOfCollectedSamples()
    {
        return this.samples.size();
    }

    public String getName()
    {
        return name;
    }

    public void uncompile()
    {
        clear();
    }

    public synchronized void clear()
    {
        // this.graph.clear();
        samples.clear();

        this.n = 0;
        this.sum = 0;
        this.average = 0;

        minimum = Integer.MAX_VALUE;
        maximum = Integer.MIN_VALUE;

        this.dataCurve = null;

        if (listener != null)
        {
            listener.updateGui();
        }
    }

    public synchronized void add(SampleResult sampleResult)
    {
        long sample = sampleResult.getTime();

        this.n++;
        this.sum += sample;
        this.average = this.sum / this.n;
        if (SHOW_INCOMING_SAMPLES)
        {
            incoming = sample;
        }
        if (sample > maximum)
        {
            maximum = sample;
        }
        if (sample < minimum)
        {
            minimum = sample;
        }
        samples.add(new Long(sample));
        int n = getNumberOfCollectedSamples();

        if ((n % (numberOfNodes * refreshPeriod)) == 0)
        {
            float[] floatNode = new float[numberOfNodes];
            //NOTUSED: long[] longSample = getSamples();
            // load each node
            int loadFactor = n / numberOfNodes;

            for (int i = 0; i < numberOfNodes; i++)
            {
                for (int j = 0; j < loadFactor; j++)
                {
                    floatNode[i] += getSample((i * loadFactor) + j);
                }
                floatNode[i] = floatNode[i] / loadFactor;
            }
            // compute the new Spline curve
            dataCurve = new Spline3(floatNode);
            if (listener != null)
            {
                listener.updateGui();
            }
        }
        else
        {// do nothing, wait for the next pile to complete
        }
    }
}
