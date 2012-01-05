/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;

public class SplineModel implements Clearable {
    public static final int DEFAULT_NUMBER_OF_NODES = 10;

    public static final int DEFAULT_REFRESH_PERIOD = 1;

    protected final boolean SHOW_INCOMING_SAMPLES = true;

    // These are not currently updated
    protected int numberOfNodes = DEFAULT_NUMBER_OF_NODES;

    protected int refreshPeriod = DEFAULT_REFRESH_PERIOD;

    /** Current Spline curve. */
    //@GuardedBy("this")
    private Spline3 dataCurve = null;

    final CachingStatCalculator samples;

    //@GuardedBy("this")
    private GraphListener listener;

    //@GuardedBy("this")
    private String name;

    public SplineModel() {
        samples = new CachingStatCalculator("Spline");
    }

    public synchronized void setListener(GraphListener vis) {
        listener = vis;
    }

    public synchronized void setName(String newName) {
        name = newName;
    }

    public boolean isEditable() {
        return true;
    }

    public synchronized Spline3 getDataCurve() {
        return dataCurve;
    }

    public long getMinimum() {
        return samples.getMin().longValue();
    }

    public long getMaximum() {
        return samples.getMax().longValue();
    }

    public long getAverage() {
        return (long) samples.getMean();
    }

    public long getCurrent() {
        return samples.getCurrentSample().getData();
    }

    public long getSample(int i) {
        return samples.getSample(i).getData();
    }

    public long getNumberOfCollectedSamples() {
        return samples.getCount();
    }

    public synchronized String getName() {
        return name;
    }

    public void uncompile() {
        clearData();
    }

    public synchronized void clearData() {
        // this.graph.clear();
        samples.clear();

        this.dataCurve = null;

        if (listener != null) {
            listener.updateGui();
        }
    }

    public synchronized void add(SampleResult sampleResult) {
        samples.addSample(sampleResult);
        long n = samples.getCount();

        if ((n % (numberOfNodes * refreshPeriod)) == 0) {
            float[] floatNode = new float[numberOfNodes];
            // NOTUSED: long[] longSample = getSamples();
            // load each node
            long loadFactor = n / numberOfNodes;

            for (int i = 0; i < numberOfNodes; i++) {
                for (int j = 0; j < loadFactor; j++) {
                    floatNode[i] += samples.getSample((int) ((i * loadFactor) + j)).getData();
                }
                floatNode[i] = floatNode[i] / loadFactor;
            }
            // compute the new Spline curve
            dataCurve = new Spline3(floatNode);
            if (listener != null) {
                listener.updateGui();
            }
        } else {// do nothing, wait for the next pile to complete
        }
    }
}
