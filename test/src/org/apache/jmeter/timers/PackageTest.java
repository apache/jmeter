/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.jmeter.timers;

import junit.framework.TestCase;

public class PackageTest extends TestCase {

    public PackageTest(String arg0) {
		super(arg0);
	}

	public void testTimer1() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        //timer.setCalcMode("mode");
        timer.setThroughput(60.0);// 1 per second
        long delay = timer.delay(); // Initialise
        assertEquals(0,delay);
        Thread.sleep(500);
        long diff=Math.abs(timer.delay()-500);
        assertTrue("Delay is approximately 500",diff<=50);

	}

    public void testTimerBSH() throws Exception {
        BeanShellTimer timer = new BeanShellTimer();
        long delay;
        
        timer.setScript("\"60\"");
        delay = timer.delay();
        assertEquals(60,delay);
        
        timer.setScript("60");
        delay = timer.delay();
        assertEquals(60,delay);
        
        timer.setScript("5*3*4");
        delay = timer.delay();
        assertEquals(60,delay);
    }

}
