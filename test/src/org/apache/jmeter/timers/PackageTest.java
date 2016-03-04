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

import static org.junit.Assert.assertEquals;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.junit.Test;

public class PackageTest extends JMeterTestCase {

    private static final Logger LOG = LoggingManager.getLoggerForClass();


    @Test
    public void testTimerBSH() throws Exception {
        if (!BeanShellInterpreter.isInterpreterPresent()){
            final String msg = "BeanShell jar not present, test ignored";
            LOG.warn(msg);
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
