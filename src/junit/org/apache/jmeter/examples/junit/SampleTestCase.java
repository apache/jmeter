/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.examples.junit;

import java.util.Random;

import junit.framework.TestCase;

/**
 * @author pete
 *
 * This is a sample test case for demonstration purposes. It just has
 * a few basic test methods.
 */
public class SampleTestCase extends TestCase {

    protected Random RANDOM = new Random();
    
	/**
	 * 
	 */
	public SampleTestCase() {
		super();
	}

	/**
	 * @param arg0
	 */
	public SampleTestCase(String name) {
		super(name);
	}

    public void testInt(){
        assertEquals(nextInt(),nextInt());
    }
    
    public void testShort(){
        long time = 100;
        try {
            Thread.sleep(time);
        } catch (InterruptedException e){
            fail(e.getMessage());
        }
    }
    
    public void testLong(){
        long time = nextLong();
        try {
            Thread.sleep(time);
        } catch (InterruptedException e){
            fail(e.getMessage());
        }
    }
    
    public void testFloat(){
        long time = nextInt();
        try {
            Thread.sleep(time);
        } catch (InterruptedException e){
            fail(e.getMessage());
        }
    }
    
    public void testDouble(){
        long time = nextInt();
        try {
            Thread.sleep(time);
        } catch (InterruptedException e){
            fail(e.getMessage());
        }
    }
    
    public void setUp(){
        System.out.println("setUp");
    }
    
    public void tearDown(){
        System.out.println("tearDown");
    }
    
    public int nextInt(){
        return RANDOM.nextInt(1000);
    }
    
    public long nextLong(){
        return RANDOM.nextLong();
    }
}
