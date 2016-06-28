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

package org.apache.jmeter.control;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Test;

public class TestWeightedDistributionController extends JMeterTestCase {
    
    @Test
    public void testDistribution() {
        int no_of_iters = 1030;
        
        String[] names = { "Zero - 90%", "One - 9%", "Two - 1%" };
        int[] wgts = { 90, 9, 1 };
        int[] exps = SequentialNumberGenerator.findExpectedResults(wgts, no_of_iters);
                       
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setIntegerGenerator(new SequentialNumberGenerator());

        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[1])));
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[2])));
        wdc.addTestElement(sub_2);
        
        assertEquals(Arrays.stream(wgts).sum(), wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }

    @Test
    public void testDistributionTwoWdcs() {
        int no_of_iters = 1000;
        
        String[] names = { "A-Zero - 90%", "A-One - 9%", "A-Two - 1%", "B-Three 33%", "B-Four 66%" };
        int[] wgts = { 90, 9, 1, 33, 66 };
        
        int[] exps_A = SequentialNumberGenerator.findExpectedResults(Arrays.copyOfRange(wgts, 0, 3), no_of_iters);
        int[] exps_B = SequentialNumberGenerator.findExpectedResults(Arrays.copyOfRange(wgts, 3, 5), no_of_iters);
        
        int[] exps = ArrayUtils.addAll(exps_A, exps_B);
        
        testLog.debug("Testing WeightedDistributionController with two weighted dist controllers");
        
        GenericController control = new GenericController();
        
        WeightedDistributionController wdc_A = new WeightedDistributionController();
        wdc_A.setIntegerGenerator(new SequentialNumberGenerator());
        
        TestSampler sub_A_0 = new TestSampler(names[0]);
        sub_A_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc_A.addTestElement(sub_A_0);
        
        TestSampler sub_A_1 = new TestSampler(names[1]);
        sub_A_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[1])));
        wdc_A.addTestElement(sub_A_1);
        
        TestSampler sub_A_2 = new TestSampler(names[2]);
        sub_A_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[2])));
        wdc_A.addTestElement(sub_A_2);
        
        WeightedDistributionController wdc_B = new WeightedDistributionController();
        wdc_B.setIntegerGenerator(new SequentialNumberGenerator());
        
        TestSampler sub_B_3 = new TestSampler(names[3]);
        sub_B_3.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[3])));
        wdc_B.addTestElement(sub_B_3);
        
        TestSampler sub_B_4 = new TestSampler(names[4]);
        sub_B_4.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[4])));
        wdc_B.addTestElement(sub_B_4);
        
        control.addTestElement(wdc_A);
        control.addTestElement(wdc_B);
        
        control.setRunningVersion(true);
        wdc_A.setRunningVersion(true);
        wdc_B.setRunningVersion(true);
        sub_A_0.setRunningVersion(true);
        sub_A_1.setRunningVersion(true);
        sub_A_2.setRunningVersion(true);
        sub_B_3.setRunningVersion(true);
        sub_B_4.setRunningVersion(true);
        control.initialize();
        
        int[] results = executeTest(control, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithDisabled() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero - 1000 - disabled", "One - 1% - enabled", "Two - 9% - enabled", "Three - 1% - disabled", "Four - 90% - enabled", "Five - 10% - disabled" };
        int[] wgts = { 1000, 1, 9, 1, 90, 10 };
        
        boolean[] enbs = { false, true, true, false, true, false };
        
        int exp_wgts[] = new int[wgts.length];
        for (int i = 0; i < exp_wgts.length; i++) {
            exp_wgts[i] = enbs[i] ? wgts[i] : 0;
        }
        
        int[] exps = SequentialNumberGenerator.findExpectedResults(exp_wgts, no_of_iters);
        
        testLog.debug("Testing WeightedDistributionController with some disabled samplers");
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setIntegerGenerator(new SequentialNumberGenerator());
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT, Integer.toString(wgts[0])));
        sub_0.setEnabled(enbs[0]);
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[1])));
        sub_1.setEnabled(enbs[1]);
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[2])));
        sub_2.setEnabled(enbs[2]);
        wdc.addTestElement(sub_2);
        
        TestSampler sub_3 = new TestSampler(names[3]);
        sub_3.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[3])));
        sub_3.setEnabled(enbs[3]);
        wdc.addTestElement(sub_3);
        
        TestSampler sub_4 = new TestSampler(names[4]);
        sub_4.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[4])));
        sub_4.setEnabled(enbs[4]);
        wdc.addTestElement(sub_4);
        
        TestSampler sub_5 = new TestSampler(names[5]);
        sub_5.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[5])));
        sub_5.setEnabled(enbs[5]);
        wdc.addTestElement(sub_5);
        
        assertEquals(Arrays.stream(exp_wgts).sum(), wdc.getCumulativeProbability());
                
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        sub_4.setRunningVersion(true);
        sub_5.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithZeroWeights() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero - 0%", "One - 1%", "Two - 9%", "Three - 0%", "Four - 90%", "Five - 0%" };
        int[] wgts = { 0, 1, 9, 0, 90, 0 };
        int[] exps = SequentialNumberGenerator.findExpectedResults(wgts, no_of_iters);
        
        testLog.debug("Testing WeightedDistributionController with some samplers having zero weight");
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setIntegerGenerator(new SequentialNumberGenerator());
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[1])));
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[2])));
        wdc.addTestElement(sub_2);
        
        TestSampler sub_3 = new TestSampler(names[3]);
        sub_3.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[3])));
        wdc.addTestElement(sub_3);
        
        TestSampler sub_4 = new TestSampler(names[4]);
        sub_4.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[4])));
        wdc.addTestElement(sub_4);
        
        TestSampler sub_5 = new TestSampler(names[5]);
        sub_5.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[5])));
        wdc.addTestElement(sub_5);
        
        assertEquals(Arrays.stream(wgts).sum(), wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        sub_4.setRunningVersion(true);
        sub_5.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithNegativeWeights() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero - negative 1%", "One - 1%", "Two - 9%", "Three - negative 1%", "Four - 90%", "Five - negative 1%" };
        int[] wgts = { -1, 1, 9, -1, 90, -1 };
        int[] exps = SequentialNumberGenerator.findExpectedResults(wgts, no_of_iters);
        
        testLog.debug("Testing WeightedDistributionController with some controllers having negative weight");
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setIntegerGenerator(new SequentialNumberGenerator());
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[1])));
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[2])));
        wdc.addTestElement(sub_2);
        
        TestSampler sub_3 = new TestSampler(names[3]);
        sub_3.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[3])));
        wdc.addTestElement(sub_3);
        
        TestSampler sub_4 = new TestSampler(names[4]);
        sub_4.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[4])));
        wdc.addTestElement(sub_4);
        
        TestSampler sub_5 = new TestSampler(names[5]);
        sub_5.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[5])));
        wdc.addTestElement(sub_5);
        
        //assertEquals(Arrays.stream(exps).sum(), wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        sub_4.setRunningVersion(true);
        sub_5.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithAllDisabled() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero - 90%", "One - 9%", "Two - 1%" };
        int[] wgts = { 90, 9, 1 };
        int[] exps = { 0, 0, 0 };
        
        testLog.debug("Testing WeightedDistributionController with all controllers disabled");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        sub_0.setEnabled(false);
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[1])));
        sub_1.setEnabled(false);
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[2])));
        sub_2.setEnabled(false);
        wdc.addTestElement(sub_2);
        
        assertEquals(0, wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    
    @Test
    public void testDistributionWithAllZeroWeights() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero", "One", "Two" };
        int[] wgts = { 0, 0, 0 };
        int[] exps = { 0, 0, 0 };
        
        testLog.debug("Testing WeightedDistributionController with all controllers haing zero weight");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[1])));
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[2])));
        wdc.addTestElement(sub_2);
        
        assertEquals(0, wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    
    @Test
    public void testDistributionWithOneSampler() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero" };
        int[] wgts = { 1 };
        int[] exps = { no_of_iters };
        
        testLog.debug("Testing WeightedDistributionController with only one sampler");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc.addTestElement(sub_0);
        
        assertEquals(1, wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithOneDisabledSampler() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero" };
        int[] wgts = { 1 };
        int[] exps = { 0 };
        
        testLog.debug("Testing WeightedDistributionController with one disabled sampler");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        sub_0.setEnabled(false);
        wdc.addTestElement(sub_0);
        
        assertEquals(0, wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithOneZeroWeightSampler() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero" };
        int[] wgts = { 0 };
        int[] exps = { 0 };
        
        testLog.debug("Testing WeightedDistributionController with one zero weight sampler");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc.addTestElement(sub_0);
        
        assertEquals(0, wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithOneNegativeWeightSampler() {
        int no_of_iters = 1000;
        
        String[] names = { "Negative One" };
        int[] wgts = { -1 };
        int[] exps = { 0 };
        
        testLog.debug("Testing WeightedDistributionController with one negative weight sampler");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc.addTestElement(sub_0);
        
        assertEquals(0, wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    
    @Test
    public void testDistributionWithNoSamplers() {
        int no_of_iters = 1000;
        
        testLog.debug("Testing WeightedDistributionController with no sampler");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        assertEquals(0, wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        wdc.initialize();
        
        int actual_iters = 0;
        for (int i = 0; i < no_of_iters; i++) {
            @SuppressWarnings("unused")
            TestElement sampler;
            while((sampler = wdc.next()) != null) {
                actual_iters++;
            }
        }
        
        assertEquals(0, actual_iters);
    }
    
    @Test
    public void testEvaluatingWeightExpressions() {
        int no_of_iters = 1000;
        
        JMeterContext jmctx = JMeterContextService.getContext();
        jmctx.setVariables(new JMeterVariables());
        JMeterVariables jmvars = jmctx.getVariables();
        jmvars.put("FIVE", "5");
        jmvars.put("TEN", "10");
        jmvars.put("NAN", "Not a Number");
        
        String[] names = { "Zero -val replace 10", "One - val replace 5", "Two - val replace not a number"};
        String[] wgtsStr = { "${TEN}", "${FIVE}", "${NAN}" };
        int[] wgtsEval = { 10, 5, 0 };
        int[] exps = SequentialNumberGenerator.findExpectedResults(wgtsEval, no_of_iters);
                       
        testLog.debug("Testing WeightedDistributionController with expressions for weights");
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setIntegerGenerator(new SequentialNumberGenerator());
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  wgtsStr[0]));
        TestElement eval_sub_0 = wdc.evaluateTestElement(sub_0);
        wdc.addTestElement(wdc.evaluateTestElement(eval_sub_0));
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  wgtsStr[1]));
        TestElement eval_sub_1 = wdc.evaluateTestElement(sub_1);
        wdc.addTestElement(eval_sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  wgtsStr[2]));
        TestElement eval_sub_2 = wdc.evaluateTestElement(sub_2);
        wdc.addTestElement(eval_sub_2);
        
        assertEquals(Arrays.stream(wgtsEval).sum(), wdc.getCumulativeProbability());
        
        wdc.setRunningVersion(true);
        eval_sub_0.setRunningVersion(true);
        eval_sub_1.setRunningVersion(true);
        eval_sub_2.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
        
    }
    
    @Test
    public void testSetSeed() {
        testLog.debug("Testing WeightedDistributionController random seed");
        
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setGeneratorSeed(1);
        assertEquals(85, wdc.getIntegerGenerator().nextInt(100));
        assertEquals(88, wdc.getIntegerGenerator().nextInt(100));
        wdc.setGeneratorSeed(2);
        assertEquals(8, wdc.getIntegerGenerator().nextInt(100));
        assertEquals(72, wdc.getIntegerGenerator().nextInt(100));
        wdc.setGeneratorSeed(1);
        assertEquals(85, wdc.getIntegerGenerator().nextInt(100));
        assertEquals(88, wdc.getIntegerGenerator().nextInt(100));
        wdc.setGeneratorSeed(1);
        assertEquals(47, wdc.getIntegerGenerator().nextInt(100));
        assertEquals(13, wdc.getIntegerGenerator().nextInt(100));
    }
    
    @Test
    public void testCalculateProbability() {
        String[] names = { "Zero - 90%", "One - 9%", "Two - 1%" };
        int[] wgts = { 90, 9, 1 };
        
        testLog.debug("Testing WeightedDistributionController calculateProbability()");
                       
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setIntegerGenerator(new SequentialNumberGenerator());

        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[1])));
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[2])));
        wdc.addTestElement(sub_2);
        
        assertEquals(0.0, wdc.calculateProbability(0), 0.001);
        assertEquals(0.0, wdc.calculateProbability(-1), 0.001);
        wdc.resetCumulativeProbability();
        assertEquals(0.01, wdc.calculateProbability(1), 0.001);
        assertEquals(0.1, wdc.calculateProbability(10), 0.001);
        assertEquals(1.0, wdc.calculateProbability(100), 0.001);
        assertEquals(2.0, wdc.calculateProbability(200), 0.001);
        
        wdc = new WeightedDistributionController();
        sub_0 = new TestSampler("Zero");
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(0)));
        wdc.addTestElement(sub_0);
        
        assertEquals(0.0, wdc.calculateProbability(0), 0.001);
        assertEquals(0.0, wdc.calculateProbability(100), 0.001);
    }
    
    @Test
    public void testGetChildTestElementReturnsNullInUnitTests() {
        String[] names = { "Zero - 90%", "One - 9%", "Two - 1%" };
        int[] wgts = { 90, 9, 1 };
                       
        testLog.debug("Testing WeightedDistributionController that in unit tests child element returns null (since gui not running)");
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setIntegerGenerator(new SequentialNumberGenerator());

        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[0])));
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[1])));
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new StringProperty(WeightedDistributionController.WEIGHT,  Integer.toString(wgts[2])));
        wdc.addTestElement(sub_2);
        
        assertNull(wdc.getChildNode(0));
        assertNull(wdc.getChildTestElement(0));
        assertNull(wdc.getChildNode(1));
        assertNull(wdc.getChildTestElement(1));
    }
        
    static int findWeightSum(int[] wgts) {
        int wgtsum = 0;
        for (int wgt : wgts) {
            if (wgt > 0) {
                wgtsum += wgt;
            }
        }
        return wgtsum;
    }
    
    static int[] executeTest(Controller control, String[] names, int iters) {
        Map<String, Integer> resultsMap = new HashMap<>(names.length);
        
        for (String name : names) {
            resultsMap.put(name, 0);
        }
        
        for (int i = 0; i < iters; i++) {
            TestElement sampler;
            while((sampler = control.next()) != null) {
                resultsMap.put(sampler.getName(), resultsMap.get(sampler.getName()) + 1);
            }
        }
        
        int[] results = new int[names.length];
        
        for(int i = 0; i < names.length; i++) {
            results[i] = resultsMap.get(names[i]);
        }
        
        return results;
    }
}

class SequentialNumberGenerator implements IntegerGenerator {
    
    int currVal;
    int maxVal;
    
    public SequentialNumberGenerator() {
        this.currVal = 0;
    }
    
    public int nextInt(int n) {
        int result = currVal % n;
        ++currVal;
        return result;
    }

    
    static int[] findExpectedResults(int[] wgts, int iters) {
        int[] exps = new int[wgts.length];
        int wgtsum = TestWeightedDistributionController.findWeightSum(wgts);
        
        int completedIters = wgtsum > 0 ? iters/wgtsum : 0;
        int remainingIters = wgtsum > 0 ? iters % wgtsum : 0;
        
        for (int i = 0; i < exps.length; i++) {
            int currWgt = wgts[i] > 0 ? wgts[i] : 0;
            exps[i] = (completedIters * currWgt + (remainingIters > 0
                    ? remainingIters > currWgt
                            ? currWgt
                            : remainingIters
                    : 0));
            remainingIters -= currWgt;
        }
        
        return exps;
    }
}