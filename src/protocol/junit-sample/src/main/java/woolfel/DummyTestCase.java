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

package woolfel;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class DummyTestCase extends TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummyTestCase.class);
    public DummyTestCase() {
        super();
        System.out.println("public DummyTestCase()");
    }

    protected DummyTestCase(String arg0) {
        super(arg0);
        System.out.println("protected DummyTestCase("+arg0+")");
    }

    @Override
    public void setUp(){
        System.out.println("DummyTestCase#setup(): "+getName());
    }

    @Override
    public void tearDown(){
        System.out.println("DummyTestCase#tearDown(): "+getName());
    }

    public void testMethodPass() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
            assertEquals(10,10);
        } catch (InterruptedException e) {
            LOGGER.warn("Exception on sleep", e);
        }
    }

    public void testMethodPass2() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
            assertEquals("one","one");
        } catch (InterruptedException e) {
            LOGGER.warn("Exception on sleep", e);
        }
    }

    public void testMethodFail() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
            assertEquals(20,10);
        } catch (InterruptedException e) {
            LOGGER.warn("Exception on sleep", e);
        }
    }

    public void testMethodFail2() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
            assertEquals("one","two");
        } catch (InterruptedException e) {
            LOGGER.warn("Exception on sleep", e);
        }
    }

    // Normal test failure
    public void testFail() {
        fail("Test failure");
    }

    // Generate test error
    @SuppressWarnings("ConstantOverflow")
    public void testException() {
        @SuppressWarnings("unused")
        int i = 27 / 0; // will generate Divide by zero error
    }

    public void testStringCompareFail(){
        assertEquals("this","that");
    }

    public void testObjectCompareFail(){
        assertEquals(new Object(),new Object());
    }
}
