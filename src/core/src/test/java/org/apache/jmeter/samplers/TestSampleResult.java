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

package org.apache.jmeter.samplers;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.Calculator;
import org.apache.jmeter.util.LogRecordingDelegatingLogger;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestSampleResult implements JMeterSerialTest {

    @Test
    void testElapsedTrue() throws Exception {
        SampleResult res = new SampleResult(true);

        // Check sample increments OK
        res.sampleStart();
        Thread.sleep(110); // Needs to be greater than the minimum to allow for boundary errors
        res.sampleEnd();
        long time = res.getTime();
        if (time < 100) {
            Assertions.fail("Sample time should be >=100, actual " + time);
        }
    }

    @Test
    void testElapsedFalse() throws Exception {
        SampleResult res = new SampleResult(false);

        // Check sample increments OK
        res.sampleStart();
        Thread.sleep(110); // Needs to be greater than the minimum to allow for boundary errors
        res.sampleEnd();
        long time = res.getTime();
        if (time < 100) {
            Assertions.fail("Sample time should be >=100, actual " + time);
        }
    }

    @Test
    void testPauseFalse() throws Exception {
        SampleResult res = new SampleResult(false);
        // Check sample increments OK
        res.sampleStart();
        long totalSampleTime = sleep(100); // accumulate the time spent 'sampling'
        res.samplePause();

        Thread.sleep(200); // This should be ignored

        // Re-increment
        res.sampleResume();
        totalSampleTime += sleep(100);
        res.sampleEnd();
        long sampleTime = res.getTime();
        assertAlmostEquals(totalSampleTime, sampleTime, 50, "Accumulated sample time");
    }

    private static void assertAlmostEquals(long expected, long actual, long delta, String message) {
        long actualDelta = Math.abs(expected - actual);
        if (actualDelta > delta) {
            Assertions.fail(() -> message + ", expected " + expected
                    + " within delta of " + delta + ", but got " + actual
                    + " which results in actual delta of " + actualDelta);
        }
    }
    @Test
    void testPauseTrue() throws Exception {
        SampleResult res = new SampleResult(true);
        // Check sample increments OK
        res.sampleStart();
        long totalSampleTime = sleep(100); // accumulate the time spent 'sampling'
        res.samplePause();

        Thread.sleep(200); // this should be ignored

        // Re-increment
        res.sampleResume();
        totalSampleTime += sleep(100);
        res.sampleEnd();
        long sampleTime = res.getTime();
        assertAlmostEquals(totalSampleTime, sampleTime, 50, "Accumulated sample time");
    }

    private LogRecordingDelegatingLogger recordLogger;

    private void divertLog() {// N.B. This needs to divert the log for SampleResult
        if (SampleResult.log instanceof LogRecordingDelegatingLogger) {
            recordLogger = (LogRecordingDelegatingLogger) SampleResult.log;
        } else {
            recordLogger = new LogRecordingDelegatingLogger(SampleResult.log);
            SampleResult.log = recordLogger;
        }
        recordLogger.clearLogRecords();
    }

    @Test
    void testPause2True() throws Exception {
        divertLog();
        SampleResult res = new SampleResult(true);
        res.sampleStart();
        res.samplePause();
        Assertions.assertEquals(0, recordLogger.getLogRecordCount());
        res.samplePause();
        Assertions.assertNotEquals(0, recordLogger.getLogRecordCount());
    }

    @Test
    void testPause2False() throws Exception {
        divertLog();
        SampleResult res = new SampleResult(false);
        res.sampleStart();
        res.samplePause();
        Assertions.assertEquals(0, recordLogger.getLogRecordCount());
        res.samplePause();
        Assertions.assertNotEquals(0, recordLogger.getLogRecordCount());
    }

    @Test
    void testByteCount() throws Exception {
        SampleResult res = new SampleResult();

        res.sampleStart();
        res.setBytes(100L);
        res.setSampleLabel("sample of size 100 bytes");
        res.sampleEnd();
        Assertions.assertEquals(100, res.getBytesAsLong());
        Assertions.assertEquals("sample of size 100 bytes", res.getSampleLabel());
    }

    @Test
    void testSubResultsTrue() throws Exception {
        testSubResults(true, 0);
    }

    @Test
    void testSubResultsTrueThread() throws Exception {
        testSubResults(true, 500L, 0);
    }

    @Test
    void testSubResultsFalse() throws Exception {
        testSubResults(false, 0);
    }

    @Test
    void testSubResultsFalseThread() throws Exception {
        testSubResults(false, 500L, 0);
    }

    @Test
    void testSubResultsTruePause() throws Exception {
        testSubResults(true, 100);
    }

    @Test
    void testSubResultsTruePauseThread() throws Exception {
        testSubResults(true, 500L, 100);
    }

    @Test
    void testSubResultsFalsePause() throws Exception {
        testSubResults(false, 100);
    }

    @Test
    void testSubResultsFalsePauseThread() throws Exception {
        testSubResults(false, 500L, 100);
    }

    // temp test case for exploring settings
    public void xtestUntilFail() throws Exception {
        while (true) {
            testSubResultsTruePause();
            testSubResultsFalsePause();
        }
    }

    private void testSubResults(boolean nanoTime, long pause) throws Exception {
        testSubResults(nanoTime, 0L, pause); // Don't use nanoThread
    }

    private void testSubResults(boolean nanoTime, long nanoThreadSleep, long pause) throws Exception {
        // This test tries to emulate a http sample, with two
        // subsamples, representing images that are downloaded for the
        // page representing the first sample.

        // Sample that will get two sub results, simulates a web page load
        SampleResult parent = new SampleResult(nanoTime, nanoThreadSleep);

        JMeterTestCase.assertPrimitiveEquals(nanoTime, parent.useNanoTime);
        Assertions.assertEquals(nanoThreadSleep, parent.nanoThreadSleep);

        long beginTest = parent.currentTimeInMillis();

        parent.sampleStart();
        Thread.sleep(100);
        parent.setBytes(300L);
        parent.setSampleLabel("Parent Sample");
        parent.setSuccessful(true);
        parent.sampleEnd();
        long parentElapsed = parent.getTime();

        // Sample with no sub results, simulates an image download
        SampleResult child1 = new SampleResult(nanoTime);
        child1.sampleStart();
        Thread.sleep(100);
        child1.setBytes(100L);
        child1.setSampleLabel("Child1 Sample");
        child1.setSuccessful(true);
        child1.sampleEnd();
        long child1Elapsed = child1.getTime();

        Assertions.assertTrue(child1.isSuccessful());
        Assertions.assertEquals(100, child1.getBytesAsLong());
        Assertions.assertEquals("Child1 Sample", child1.getSampleLabel());
        Assertions.assertEquals(1, child1.getSampleCount());
        Assertions.assertEquals(0, child1.getSubResults().length);

        long actualPause = 0;
        if (pause > 0) {
            long t1 = parent.currentTimeInMillis();
            Thread.sleep(pause);
            actualPause = parent.currentTimeInMillis() - t1;
        }

        // Sample with no sub results, simulates an image download
        SampleResult child2 = new SampleResult(nanoTime);
        child2.sampleStart();
        Thread.sleep(100);
        child2.setBytes(200L);
        child2.setSampleLabel("Child2 Sample");
        child2.setSuccessful(true);
        child2.sampleEnd();
        long child2Elapsed = child2.getTime();

        Assertions.assertTrue(child2.isSuccessful());
        Assertions.assertEquals(200, child2.getBytesAsLong());
        Assertions.assertEquals("Child2 Sample", child2.getSampleLabel());
        Assertions.assertEquals(1, child2.getSampleCount());
        Assertions.assertEquals(0, child2.getSubResults().length);

        // Now add the subsamples to the sample
        parent.addSubResult(child1);
        parent.addSubResult(child2);
        Assertions.assertTrue(parent.isSuccessful());
        Assertions.assertEquals(600, parent.getBytesAsLong());
        Assertions.assertEquals("Parent Sample", parent.getSampleLabel());
        Assertions.assertEquals(1, parent.getSampleCount());
        Assertions.assertEquals(2, parent.getSubResults().length);
        long parentElapsedTotal = parent.getTime();

        long overallTime = parent.currentTimeInMillis() - beginTest;

        long sumSamplesTimes = parentElapsed + child1Elapsed + actualPause + child2Elapsed;

        /*
         * Parent elapsed total should be no smaller than the sum of the individual samples.
         * It may be greater by the timer granularity.
         */

        long diff = parentElapsedTotal - sumSamplesTimes;
        long maxDiff = nanoTime ? 10 : 16; // TimeMillis has granularity of 10-20
        if (diff < 0 || diff > maxDiff) {
            Assertions.fail("ParentElapsed: " + parentElapsedTotal + " - " + " sum(samples): " + sumSamplesTimes
                    + " => " + diff + " not in [0," + maxDiff + "]; nanotime=" + nanoTime);
        }

        // The overall time to run the test must be no less than,
        // and may be greater (but not much greater) than the parent elapsed time

        diff = overallTime - parentElapsedTotal;
        if (diff < 0 || diff > maxDiff) {
            Assertions.fail("TestElapsed: " + overallTime + " - " + " ParentElapsed: " + parentElapsedTotal
                    + " => " + diff + " not in [0," + maxDiff + "]; nanotime=" + nanoTime);
        }

        // Check that calculator gets the correct statistics from the sample
        Calculator calculator = new Calculator();
        calculator.addSample(parent);
        Assertions.assertEquals(600, calculator.getTotalBytes());
        Assertions.assertEquals(1, calculator.getCount());
        Assertions.assertEquals(1d / (parentElapsedTotal / 1000d), calculator.getRate(), 0.0001d); // Allow for some margin of error
        // Check that the throughput uses the time elapsed for the sub results
        Assertions.assertFalse(1d / (parentElapsed / 1000d) <= calculator.getRate());
    }

    // TODO some more invalid sequence tests needed

    @Test
    void testEncodingAndType() throws Exception {
        // check default
        SampleResult res = new SampleResult();
        Assertions.assertEquals(SampleResult.DEFAULT_ENCODING, res.getDataEncodingWithDefault());
        Assertions.assertEquals("", res.getDataType(), "DataType should be blank");
        Assertions.assertNull(res.getDataEncodingNoDefault());

        // check null changes nothing
        res.setEncodingAndType(null);
        Assertions.assertEquals(SampleResult.DEFAULT_ENCODING, res.getDataEncodingWithDefault());
        Assertions.assertEquals("", res.getDataType(), "DataType should be blank");
        Assertions.assertNull(res.getDataEncodingNoDefault());

        // check no charset
        res.setEncodingAndType("text/html");
        Assertions.assertEquals(SampleResult.DEFAULT_ENCODING, res.getDataEncodingWithDefault());
        Assertions.assertEquals("text", res.getDataType());
        Assertions.assertNull(res.getDataEncodingNoDefault());

        // Check unquoted charset
        res.setEncodingAndType("text/html; charset=aBcd");
        Assertions.assertEquals("aBcd", res.getDataEncodingWithDefault());
        Assertions.assertEquals("aBcd", res.getDataEncodingNoDefault());
        Assertions.assertEquals("text", res.getDataType());

        // Check quoted charset
        res.setEncodingAndType("text/html; charset=\"aBCd\"");
        Assertions.assertEquals("aBCd", res.getDataEncodingWithDefault());
        Assertions.assertEquals("aBCd", res.getDataEncodingNoDefault());
        Assertions.assertEquals("text", res.getDataType());
    }

    // sleep and return how long we actually slept
    // may be rather longer if the system is busy
    private long sleep(long ms) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread.sleep(ms);
        return System.currentTimeMillis() - start;
    }

    @Test
    void testCompareSampleLabels() {
        final boolean prevValue = TestPlan.getFunctionalMode();
        TestPlan plan = new TestPlan();
        plan.setFunctionalMode(true);
        try {
            SampleResult result = new SampleResult();
            result.setStartTime(System.currentTimeMillis());
            result.setSampleLabel("parent label");

            SampleResult subResult = new SampleResult();
            subResult.setStartTime(System.currentTimeMillis());
            subResult.setSampleLabel("subResult label");

            result.addSubResult(subResult);
            Assertions.assertEquals("subResult label", subResult.getSampleLabel());

            plan.setFunctionalMode(false);
            subResult.setSampleLabel("parent label");
            result.addRawSubResult(subResult);
            Assertions.assertEquals("parent label-0", subResult.getSampleLabel());
        } finally {
            plan.setFunctionalMode(prevValue);
        }
    }

    @Test
    void testBug63433() {
        SampleResult firstResult = new SampleResult();
        Assertions.assertFalse(firstResult.markFile("result.csv"), "Expected false on first call of markFile");
        Assertions.assertTrue(firstResult.markFile("result.csv"), "Expected true on second call of markFile");

        SampleResult secondResult = new SampleResult();
        Assertions.assertFalse(secondResult.markFile(null), "Expected false on first call of markFile with null");
        Assertions.assertTrue(secondResult.markFile(null), "Expected true on second call of markFile with null");
    }
}
