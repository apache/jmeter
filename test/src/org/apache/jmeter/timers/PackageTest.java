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

package org.apache.jmeter.timers;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.TestJMeterContextService;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class PackageTest extends JMeterTestCase {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public PackageTest(String arg0) {
        super(arg0);
    }

    public void testTimer1() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        assertEquals(0,timer.getCalcMode());// Assume this thread only
        timer.setThroughput(60.0);// 1 per second
        long delay = timer.delay(); // Initialise
        assertEquals(0,delay);
        Thread.sleep(500);
        assertEquals("Expected delay of approx 500",500, timer.delay(), 50);
    }

    public void testTimer2() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        assertEquals(0,timer.getCalcMode());// Assume this thread only
        timer.setThroughput(60.0);// 1 per second
        assertEquals(1000,timer.calculateCurrentTarget(0)); // Should delay for 1 second
        timer.setThroughput(60000.0);// 1 per milli-second
        assertEquals(1,timer.calculateCurrentTarget(0)); // Should delay for 1 milli-second
    }

    public void testTimer3() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        timer.setMode(ConstantThroughputTimer.Mode.AllActiveThreads); //$NON-NLS-1$ - all threads
        assertEquals(1,timer.getCalcMode());// All threads
        for(int i=1; i<=10; i++){
            TestJMeterContextService.incrNumberOfThreads();
        }
        assertEquals(10,JMeterContextService.getNumberOfThreads());
        timer.setThroughput(600.0);// 10 per second
        assertEquals(1000,timer.calculateCurrentTarget(0)); // Should delay for 1 second
        timer.setThroughput(600000.0);// 10 per milli-second
        assertEquals(1,timer.calculateCurrentTarget(0)); // Should delay for 1 milli-second
        for(int i=1; i<=990; i++){
            TestJMeterContextService.incrNumberOfThreads();
        }
        assertEquals(1000,JMeterContextService.getNumberOfThreads());
        timer.setThroughput(60000000.0);// 1000 per milli-second
        assertEquals(1,timer.calculateCurrentTarget(0)); // Should delay for 1 milli-second
    }

    public void testTimerBSH() throws Exception {
        if (!BeanShellInterpreter.isInterpreterPresent()){
            final String msg = "BeanShell jar not present, test ignored";
            log.warn(msg);
            return;
        }
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
