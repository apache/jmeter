// $Header$
/*
 * Copyright 2000-2004 The Apache Software Foundation.
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


/**
 * @author Michael Stover
 * @version 1.0
 */

public class Sample implements Serializable
{
    public long data;
    public long average;
    public long median;
    public long deviation;
    public float throughput;
    public boolean error = false;

    /**
     *  Constructor for the Sample object
     *
     *@param  data       Description of Parameter
     *@param  average    Description of Parameter
     *@param  deviation  Description of Parameter
     */
    public Sample(
        long data,
        long average,
        long deviation,
        float throughput,
        long median,
        boolean error)
    {
        this.data = data;
        this.average = average;
        this.deviation = deviation;
        this.throughput = throughput;
        this.error = error;
        this.median = median;
    }

    public Sample()
    {}
}
