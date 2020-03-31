/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.correlation;

import java.awt.BorderLayout;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.CorrelationRecorder;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

/**
 * This class is used to set buffer data while correlation.
 *
 */
public class CorrelationResponseRecorder extends AbstractVisualizer {

    private static final long serialVersionUID = 240L;

    private Buffer buffer;

    private CorrelationRecorder correlationRecorder;

    /**
     * Create the CorrelationResponseRecorder.
     */
    public CorrelationResponseRecorder() {
        final int maxResults = JMeterUtils.getPropDefault("view.results.tree.max_results", 500);
        if (maxResults > 0) {
            buffer = new CircularFifoBuffer(maxResults);
        } else {
            buffer = new UnboundedFifoBuffer();
        }
        init();
        setName(getStaticLabel());
    }

    @Override
    public String getLabelResource() {
        return "correlation_response_recorder_title";
    }

    /**
     * Initialize the component in the UI
     */
    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
    }

    /**
     * Does nothing, but required by interface.
     */
    @Override
    public void clearData() {
        // NOOP
    }

    /**
     * To add the buffered data to correlation recorder.
     *
     * @param sample ignored
     */
    @SuppressWarnings("unchecked")
    @Override
    public void add(SampleResult sample) {
        synchronized (buffer) {
            buffer.add(sample);
            correlationRecorder = new CorrelationRecorder();
            correlationRecorder.setBuffer(buffer);
        }
    }

}
