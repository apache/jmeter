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
 */
package org.apache.jmeter.protocol.jini.config;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;

public class JiniTestElement extends AbstractTestElement implements ConfigElement, TestStateListener, TestBean {

    private static final long serialVersionUID = 1L;

    @Override
    public void testStarted() {
        // TODO Auto-generated method stub

    }

    @Override
    public void testStarted(String host) {
        // TODO Auto-generated method stub

    }

    @Override
    public void testEnded() {
        // TODO Auto-generated method stub

    }

    @Override
    public void testEnded(String host) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addConfigElement(ConfigElement config) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean expectsModification() {
        // TODO Auto-generated method stub
        return false;
    }

}
