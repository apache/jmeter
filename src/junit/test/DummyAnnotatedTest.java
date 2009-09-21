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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DummyAnnotatedTest
{
    public String name;
    public int two = 1; //very wrong.
    
    public DummyAnnotatedTest() {
        name="NOT SET";
    }
    public DummyAnnotatedTest(String name) {
        this.name = name;
    }

    @Test(expected=RuntimeException.class)
    public void fail() {
        throw new RuntimeException();
    }
    
    @Before
    public void verifyTwo() {
        two = 2;
    }
    
    @After
    public void printDone() {
        System.out.println("done with an annotated test.");
    }
    
    @Test
    public void add() {
        int four = two+2;
        if(4!=four) {
            throw new RuntimeException("4 did not equal four.");
        }
        //or if you have assertions enabled
        assert 4 == four;
    }
    
    //should always fail
    @Test(timeout=1000)
    public void timeOut() {
        try{
            Thread.sleep(2000);
        }catch (InterruptedException e) { }
    }
}
