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

package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Sample test cases for demonstrating JUnit4 sampler.
 *
 */
public class DummyAnnotatedTest
{
    public int two = 1; //very wrong.

    public DummyAnnotatedTest() {
    }

    // Generates expected Exception
    @Test(expected=RuntimeException.class)
    public void expectedExceptionPass() {
        throw new RuntimeException();
    }

    // Fails to generate expected Exception
    @Test(expected=RuntimeException.class)
    public void expectedExceptionFail() {
    }

    @Before
    public void verifyTwo() {
        System.out.println("DummyAnnotatedTest#verifyTwo()");
        two = 2;
    }

    @After
    public void printDone() {
        System.out.println("DummyAnnotatedTest#printDone()");
    }

    @Test
    // Succeeds only if Before method - verifyTwo() - is run.
    public void add() {
        int four = two + 2;
        if (4 != four) {
            throw new RuntimeException("4 did not equal four.");
        }
        //or if you have assertions enabled
        assert 4 == four;
    }

    //should always fail
    @Test(timeout = 1000)
    public void timeOutFail() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ignored) {
        }
    }

    //should not fail
    @Test(timeout = 1000)
    public void timeOutPass() {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void alwaysFail() {
        fail("This always fails");
    }

    @Test
    // Generate a test error
    public void divideByZero() {
        @SuppressWarnings("unused")
        int i = 27 / 0; // will generate Divide by zero error
    }

    @Test
    public void stringCompareFail(){
        assertEquals("this","that");
    }

    @Test
    public void objectCompareFail(){
        assertEquals(new Object(),new Object());
    }
}
