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

import java.awt.BorderLayout;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

/**
 * This listener can record results to a file but not to the UI. It is meant to
 * provide an efficient means of recording data by eliminating GUI overhead.
 */
public class SimpleDataWriter extends AbstractVisualizer {
    private static final long serialVersionUID = 240L;

    /**
     * Create the SimpleDataWriter.
     */
    public SimpleDataWriter() {
        init();
        setName(getStaticLabel());
    }

    @Override
    public String getLabelResource() {
        return "simple_data_writer_title"; // $NON-NLS-1$
    }

    /**
     * Initialize the component in the UI
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
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
     * Does nothing, but required by interface.
     *
     * @param sample ignored
     */
    @Override
    public void add(SampleResult sample) {
        // NOOP
    }
}
