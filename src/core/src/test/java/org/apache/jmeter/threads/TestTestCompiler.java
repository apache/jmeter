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

package org.apache.jmeter.threads;

import static org.junit.Assert.assertEquals;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.Test;

public class TestTestCompiler {

        @Test
        public void testConfigGathering() throws Exception {
            ListedHashTree testing = new ListedHashTree();
            GenericController controller = new GenericController();
            ConfigTestElement config1 = new ConfigTestElement();
            config1.setName("config1");
            config1.setProperty("test.property", "A test value");
            TestSampler sampler = new TestSampler();
            sampler.setName("sampler");
            testing.add(controller, config1);
            testing.add(controller, sampler);
            TestCompiler.initialize();

            TestCompiler compiler = new TestCompiler(testing);
            testing.traverse(compiler);
            sampler = (TestSampler) compiler.configureSampler(sampler).getSampler();
            assertEquals("A test value", sampler.getPropertyAsString("test.property"));
        }

        class TestSampler extends AbstractSampler {
            private static final long serialVersionUID = 240L;

            @Override
            public SampleResult sample(org.apache.jmeter.samplers.Entry e) {
                return null;
            }

            @Override
            public Object clone() {
                return new TestSampler();
            }
        }
}
