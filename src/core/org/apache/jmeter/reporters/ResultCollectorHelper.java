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

package org.apache.jmeter.reporters;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.Visualizer;

/**
 * Helper class to allow TestResultWrapperConverter to send samples
 * directly to the visualiser if required.
 */
public class ResultCollectorHelper {

    private final Visualizer visualizer;
    private final boolean errorsOnly;
    private final boolean successOnly;

    public ResultCollectorHelper(ResultCollector resultCollector, Visualizer visualizer) {
        this.visualizer = visualizer;
        this.errorsOnly = resultCollector.isErrorLogging();
        this.successOnly = resultCollector.isSuccessOnlyLogging();
    }

    public void add(SampleResult sample){
        if (ResultCollector.isSampleWanted(sample.isSuccessful(), errorsOnly, successOnly)){
            visualizer.add(sample);
        }
    }
}
