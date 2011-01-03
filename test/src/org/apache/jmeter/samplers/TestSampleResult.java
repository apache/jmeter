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

import org.apache.jmeter.util.Calculator;
import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.format.RawFormatter;
import org.apache.log.output.io.WriterTarget;

// TODO need more tests - particularly for the new functions

public class TestSampleResult extends TestCase {
        public TestSampleResult(String name) {
            super(name);
        }

        public void testElapsedTrue() throws Exception {
            SampleResult res = new SampleResult(true);

            // Check sample increments OK
            res.sampleStart();
            Thread.sleep(110); // Needs to be greater than the minimum to allow for boundary errors
            res.sampleEnd();
            long time = res.getTime();
            if(time < 100){
                fail("Sample time should be >=100, actual "+time);
            }
        }

        public void testElapsedFalse() throws Exception {
            SampleResult res = new SampleResult(false);

            // Check sample increments OK
            res.sampleStart();
            Thread.sleep(110); // Needs to be greater than the minimum to allow for boundary errors
            res.sampleEnd();
            long time = res.getTime();
            if(time < 100){
                fail("Sample time should be >=100, actual "+time);
            }
        }

        public void testPauseFalse() throws Exception {
            SampleResult res = new SampleResult(false);
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
            if ((sampleTime < 180) || (sampleTime > 290)) {
                fail("Accumulated time (" + sampleTime + ") was not between 180 and 290 ms");
            }
        }

        public void testPauseTrue() throws Exception {
            SampleResult res = new SampleResult(true);
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
            if ((sampleTime < 180) || (sampleTime > 290)) {
                fail("Accumulated time (" + sampleTime + ") was not between 180 and 290 ms");
            }
        }

        private static final Formatter fmt = new RawFormatter();

        private StringWriter wr = null;

        private void divertLog() {// N.B. This needs to divert the log for SampleResult
            wr = new StringWriter(1000);
            LogTarget[] lt = { new WriterTarget(wr, fmt) };
            SampleResult.log.setLogTargets(lt);
        }

        public void testPause2True() throws Exception {
            divertLog();
            SampleResult res = new SampleResult(true);
            res.sampleStart();
            res.samplePause();
            assertEquals(0, wr.toString().length());
            res.samplePause();
            assertFalse(wr.toString().length() == 0);
        }

        public void testPause2False() throws Exception {
            divertLog();
            SampleResult res = new SampleResult(false);
            res.sampleStart();
            res.samplePause();
            assertEquals(0, wr.toString().length());
            res.samplePause();
            assertFalse(wr.toString().length() == 0);
        }
        
        public void testByteCount() throws Exception {
            SampleResult res = new SampleResult();
            
            res.sampleStart();
            res.setBytes(100);
            res.setSampleLabel("sample of size 100 bytes");
            res.sampleEnd();
            assertEquals(100, res.getBytes());
            assertEquals("sample of size 100 bytes", res.getSampleLabel());
        }

        public void testSubResultsTrue() throws Exception {
            testSubResults(true);
        }

        public void testSubResultsFalse() throws Exception {
            testSubResults(false);
        }

        private void testSubResults(boolean nanoTime) throws Exception {
            // This test tries to emulate a http sample, with two
            // subsamples, representing images that are downloaded for the
            // page representing the first sample.

            // Sample that will get two sub results, simulates a web page load 
            SampleResult resWithSubResults = new SampleResult(nanoTime);            

            long beginTest = resWithSubResults.currentTimeInMillis();

            resWithSubResults.sampleStart();
            Thread.sleep(100);
            resWithSubResults.setBytes(300);
            resWithSubResults.setSampleLabel("sample with two subresults");
            resWithSubResults.setSuccessful(true);
            resWithSubResults.sampleEnd();
            long sampleWithSubResultsTime = resWithSubResults.getTime();

            // Sample with no sub results, simulates an image download
            SampleResult resNoSubResults1 = new SampleResult(nanoTime);            
            resNoSubResults1.sampleStart();
            Thread.sleep(100);
            resNoSubResults1.setBytes(100);
            resNoSubResults1.setSampleLabel("sample with no subresults");
            resNoSubResults1.setSuccessful(true);
            resNoSubResults1.sampleEnd();
            long sample1Time = resNoSubResults1.getTime();

            assertTrue(resNoSubResults1.isSuccessful());
            assertEquals(100, resNoSubResults1.getBytes());
            assertEquals("sample with no subresults", resNoSubResults1.getSampleLabel());
            assertEquals(1, resNoSubResults1.getSampleCount());
            assertEquals(0, resNoSubResults1.getSubResults().length);
            
            // Sample with no sub results, simulates an image download 
            SampleResult resNoSubResults2 = new SampleResult(nanoTime);            
            resNoSubResults2.sampleStart();
            Thread.sleep(100);
            resNoSubResults2.setBytes(200);
            resNoSubResults2.setSampleLabel("sample with no subresults");
            resNoSubResults2.setSuccessful(true);
            resNoSubResults2.sampleEnd();
            long sample2Time = resNoSubResults2.getTime();

            assertTrue(resNoSubResults2.isSuccessful());
            assertEquals(200, resNoSubResults2.getBytes());
            assertEquals("sample with no subresults", resNoSubResults2.getSampleLabel());
            assertEquals(1, resNoSubResults2.getSampleCount());
            assertEquals(0, resNoSubResults2.getSubResults().length);
            
            // Now add the subsamples to the sample
            resWithSubResults.addSubResult(resNoSubResults1);
            resWithSubResults.addSubResult(resNoSubResults2);
            assertTrue(resWithSubResults.isSuccessful());
            assertEquals(600, resWithSubResults.getBytes());
            assertEquals("sample with two subresults", resWithSubResults.getSampleLabel());
            assertEquals(1, resWithSubResults.getSampleCount());
            assertEquals(2, resWithSubResults.getSubResults().length);
            long totalTime = resWithSubResults.getTime();
            
            long overallTime = resWithSubResults.currentTimeInMillis() - beginTest;

            // Check the sample times
            long allsamplesTime = sampleWithSubResultsTime + sample1Time + sample2Time;
            if (totalTime < allsamplesTime) {
                fail("Total: "+totalTime+" < sum(samples): "+ allsamplesTime);
            }
            /*
             * The granularity of System.currentTimeMillis() - plus the fact that the nanoTime()
             * offset is now calculated for each sampleResult - means that there can be some
             * minor variation in the value returned by SampleResult#currentTimeInMillis().
             * 
             * Allow for this by adding a fudge factor - 3ms seems to be sufficient.
            */
            if (totalTime > overallTime+3) {
                fail("Total: "+totalTime+" > 3 + overall time: "+ overallTime);
            }
            
            // Check that calculator gets the correct statistics from the sample
            Calculator calculator = new Calculator();
            calculator.addSample(resWithSubResults);
            assertEquals(600, calculator.getTotalBytes());
            assertEquals(1, calculator.getCount());
            assertEquals(1d / (totalTime / 1000d), calculator.getRate(),0.0001d); // Allow for some margin of error
            // Check that the throughput uses the time elapsed for the sub results
            assertFalse(1d / (sampleWithSubResultsTime / 1000d) <= calculator.getRate());
        }

        // TODO some more invalid sequence tests needed
        
        public void testEncodingAndType() throws Exception {
            // check default
            SampleResult res = new SampleResult();
            assertEquals(SampleResult.DEFAULT_ENCODING,res.getDataEncodingWithDefault());
            assertEquals("DataType should be blank","",res.getDataType());
            assertNull(res.getDataEncodingNoDefault());
            
            // check null changes nothing
            res.setEncodingAndType(null);
            assertEquals(SampleResult.DEFAULT_ENCODING,res.getDataEncodingWithDefault());
            assertEquals("DataType should be blank","",res.getDataType());
            assertNull(res.getDataEncodingNoDefault());

            // check no charset
            res.setEncodingAndType("text/html");
            assertEquals(SampleResult.DEFAULT_ENCODING,res.getDataEncodingWithDefault());
            assertEquals("text",res.getDataType());
            assertNull(res.getDataEncodingNoDefault());

            // Check unquoted charset
            res.setEncodingAndType("text/html; charset=aBcd");
            assertEquals("aBcd",res.getDataEncodingWithDefault());
            assertEquals("aBcd",res.getDataEncodingNoDefault());
            assertEquals("text",res.getDataType());

            // Check quoted charset
            res.setEncodingAndType("text/html; charset=\"aBCd\"");
            assertEquals("aBCd",res.getDataEncodingWithDefault());
            assertEquals("aBCd",res.getDataEncodingNoDefault());
            assertEquals("text",res.getDataType());         
        }
}

