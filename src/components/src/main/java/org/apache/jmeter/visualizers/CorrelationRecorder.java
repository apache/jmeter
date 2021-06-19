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

package org.apache.jmeter.visualizers;

import java.util.Queue;

import org.apache.jmeter.samplers.SampleResult;

public class CorrelationRecorder {

    // (+) HTTP(S) Test Script Recorder
    // |-----Correlation Recorder
    // followed by a Correlation template(?)
    // Also, based on the recorded data in correlation template,
    // enable/disable or show/hide(?) the correlation option
    private static Queue<SampleResult> buffer;

    public static Queue<SampleResult> getBuffer() {
        return buffer;
    }

    public void setBuffer(Queue<SampleResult> buffer) {
        CorrelationRecorder.buffer = buffer;
    }

}
