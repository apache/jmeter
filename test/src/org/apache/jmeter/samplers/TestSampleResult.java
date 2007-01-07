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

package org.apache.jmeter.samplers;

import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.format.RawFormatter;
import org.apache.log.output.io.WriterTarget;

// TODO need more tests - particularly for the new functions

public class TestSampleResult extends TestCase {
        public TestSampleResult(String name) {
            super(name);
        }

        public void testElapsed() throws Exception {
            SampleResult res = new SampleResult();

            // Check sample increments OK
            res.sampleStart();
            Thread.sleep(100);
            res.sampleEnd();
            assertTrue(res.getTime() >= 100);
        }

        public void testPause() throws Exception {
            SampleResult res = new SampleResult();
            // Check sample increments OK
            res.sampleStart();
            Thread.sleep(100);
            res.samplePause();

            Thread.sleep(200);

            // Re-increment
            res.sampleResume();
            Thread.sleep(100);
            res.sampleEnd();
            long sampleTime = res.getTime();
            if ((sampleTime < 200) || (sampleTime > 290)) {
                fail("Accumulated time (" + sampleTime + ") was not between 200 and 290 ms");
            }
        }

        private static Formatter fmt = new RawFormatter();

        private StringWriter wr = null;

        private void divertLog() {// N.B. This needs to divert the log for SampleResult
            wr = new StringWriter(1000);
            LogTarget[] lt = { new WriterTarget(wr, fmt) };
            SampleResult.log.setLogTargets(lt);
        }

        public void testPause2() throws Exception {
            divertLog();
            SampleResult res = new SampleResult();
            res.sampleStart();
            res.samplePause();
            assertTrue(wr.toString().length() == 0);
            res.samplePause();
            assertFalse(wr.toString().length() == 0);
        }
        // TODO some more invalid sequence tests needed
}

