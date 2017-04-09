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

package org.apache.jmeter.threads;

import org.junit.rules.ExternalResource;

public class JMeterContextServiceHelper extends ExternalResource {

    @Override
    protected void after() {
        JMeterContextService.removeContext();
    }

    private JMeterContext instance;
    public JMeterContext get() {
        if (instance == null) {
            JMeterContext jMeterContext = new JMeterContext();
            JMeterContextService.replaceContext(jMeterContext);
            initContext(jMeterContext);
            instance = jMeterContext;
        }
        return instance;
    }

    protected void initContext(JMeterContext jMeterContext) {}
}
