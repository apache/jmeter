/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2001 The Apache Software Foundation.  All rights
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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


/**
 * The model that collects the average of the set of pages to be sampled.
 *
 * @author     Khor Soon Hin
 * Created      2001/08/11
 * @version    $Revision$ Last updated: $Date$
 */

public class GraphAccumModel implements Clearable, Serializable
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    protected String name;
    protected List samples;
    protected List listeners;

    protected long averageSum = 0;
    protected long variationSum = 0;
    protected long counter = 0;
    protected long previous = 0;
    protected long max = 1;
    protected boolean bigChange = false;
    protected SampleResult current;

    /**
     * Constructor.
     */
    public GraphAccumModel()
    {
        log.debug("Start : GraphAccumModel1");
        listeners = new LinkedList();
        samples = Collections.synchronizedList(new LinkedList());
        log.debug("End : GraphAccumModel1");
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
     * Gets the SampleCount attribute of the GraphAccumModel object.
     *
     * @return    the SampleCount value
     */
    public int getSampleCount()
    {
        return samples.size();
    }

    /**
     * Gets the List attribute of the GraphAccumModel object.
     *
     * @return    the List value
     */
    public List getList()
    {
        return samples;
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
     * Gets the Max attribute of the GraphAccumModel object.
     *
     * @return    the Max value
     */
    public long getMax()
    {
        log.debug("getMax1 : Returning - " + max);
        return max;
    }

    /**
     * Adds a feature to the ModelListener attribute of the GraphAccumModel
     * object.
     *
     * @param  listener   the feature to be added to the GraphAccumListener
     *                    attribute.
     */
    public void addGraphAccumListener(GraphAccumListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Clear the results.
     */
    public void clear()
    {
        log.debug("Start : clear1");
        samples.clear();
        max = 1;
        bigChange = true;
        this.fireDataChanged();
        log.debug("End : clear1");
    }

    /**
     * Add the new sample to the results.
     *
     * @param  res  sample containing the results
     */
    public void addNewSample(SampleResult res)
    {
        log.debug("Start : addNewSample1");
        // Set time to time taken to load this url without components (e.g.
        // images etc)
        long totalTime = res.getTime();

        if (log.isDebugEnabled())
        {
            log.debug("addNewSample1 : time - " + totalTime);
            log.debug("addNewSample1 : max - " + max);
        }
        if (totalTime > max)
        {
            bigChange = true;
            max = totalTime;
        }
        current = res;
        samples.add(res);
        log.debug("End : addNewSample1");
        fireDataChanged();
    }

    /**
     *  Depending on whether the graph needs to be rescale call the appropriate
     *  methods.
     */
    protected void fireDataChanged()
    {
        log.debug("Start : fireDataChanged1");
        Iterator iter = listeners.iterator();

        if (bigChange)
        {
            while (iter.hasNext())
            {
                ((GraphAccumListener) iter.next()).updateGui();
            }
            bigChange = false;
        }
        else
        {
            quickUpdate(current);
        }
        log.debug("End : fireDataChanged1");
    }

    /**
     * The sample to be added did not exceed the current set of samples so do
     * not need to rescale graph.
     */
    protected void quickUpdate(SampleResult s)
    {
        Iterator iter = listeners.iterator();
        {
            while (iter.hasNext())
            {
                ((GraphAccumListener) iter.next()).updateGui(s);
            }
        }
    }
}

