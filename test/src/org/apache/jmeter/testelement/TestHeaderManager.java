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

package org.apache.jmeter.testelement;

import static org.junit.Assert.assertEquals;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.junit.Test;

public class TestHeaderManager extends JMeterTestCase {
    @Test
    public void testReplace() throws Exception {
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("Referer", "https://jmeter.apache.org/changes.html"));
        headerManager.add(new Header("JSESSIONID", "AZAZDZDAFEFZEVZEZEVZEVZEVZZ"));

        int numberOfReplacements = headerManager.replace("jmeter.apache.org", "${host}", true);

        assertEquals(1, numberOfReplacements);
        assertEquals("Referer", headerManager.getHeader(0).getName());
        assertEquals("JSESSIONID", headerManager.getHeader(1).getName());
        assertEquals("https://${host}/changes.html", headerManager.getHeader(0).getValue());
        assertEquals("AZAZDZDAFEFZEVZEZEVZEVZEVZZ", headerManager.getHeader(1).getValue());

        headerManager = new HeaderManager();
        headerManager.add(new Header("Referer", "https://JMeter.apache.org/changes.html"));
        headerManager.add(new Header("JSESSIONID", "AZAZDZDAFEFZEVZEZEVZEVZEVZZ"));

        numberOfReplacements = headerManager.replace("jmeter.apache.org", "${host}", false);

        assertEquals(1, numberOfReplacements);
        assertEquals("Referer", headerManager.getHeader(0).getName());
        assertEquals("JSESSIONID", headerManager.getHeader(1).getName());
        assertEquals("https://${host}/changes.html", headerManager.getHeader(0).getValue());
        assertEquals("AZAZDZDAFEFZEVZEZEVZEVZEVZZ", headerManager.getHeader(1).getValue());
    }

    @Test
    public void testReplaceNoMatch() throws Exception {
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("Referer", "https://jmeter.apache.org/changes.html"));
        headerManager.add(new Header("JSESSIONID", "AZAZDZDAFEFZEVZEZEVZEVZEVZZ"));

        int numberOfReplacements = headerManager.replace("JMeter.apache.org", "${host}", true);

        assertEquals(0, numberOfReplacements);
        assertEquals("Referer", headerManager.getHeader(0).getName());
        assertEquals("JSESSIONID", headerManager.getHeader(1).getName());
        assertEquals("https://jmeter.apache.org/changes.html", headerManager.getHeader(0).getValue());
        assertEquals("AZAZDZDAFEFZEVZEZEVZEVZEVZZ", headerManager.getHeader(1).getValue());

        headerManager = new HeaderManager();
        headerManager.add(new Header("Referer", "https://jmeter.apache.org/changes.html"));
        headerManager.add(new Header("JSESSIONID", "AZAZDZDAFEFZEVZEZEVZEVZEVZZ"));

        numberOfReplacements = headerManager.replace("JMeterx.apache.org", "${host}", false);

        assertEquals(0, numberOfReplacements);
        assertEquals("Referer", headerManager.getHeader(0).getName());
        assertEquals("JSESSIONID", headerManager.getHeader(1).getName());
        assertEquals("https://jmeter.apache.org/changes.html", headerManager.getHeader(0).getValue());
        assertEquals("AZAZDZDAFEFZEVZEZEVZEVZEVZZ", headerManager.getHeader(1).getValue());
    }
}
