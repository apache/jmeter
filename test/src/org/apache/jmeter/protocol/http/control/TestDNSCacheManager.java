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

package org.apache.jmeter.protocol.http.control;

import java.net.UnknownHostException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.Test;

public class TestDNSCacheManager extends JMeterTestCase {

    @Test
    public void testCloneWithCustomResolverAndInvalidNameserver() throws UnknownHostException {
        DNSCacheManager original = new DNSCacheManager();
        original.setCustomResolver(true);
        original.addServer("127.0.0.99");
        DNSCacheManager clone = (DNSCacheManager) original.clone();
        try {
            clone.resolve("jmeter.apache.org");
            fail();
        } catch (UnknownHostException e) {
            // OK
        }
    }

}
