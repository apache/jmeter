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

package org.apache.jmeter.examples.testbeans.example2;

import java.util.Locale;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;

/**
 * This TestBean is just an example about how to write testbeans. The intent is
 * to demonstrate usage of the TestBean features to podential TestBean
 * developers. Note that only the class's introspector view matters: the methods
 * do nothing -- nothing useful, in any case.
 */
public class Example2 extends AbstractSampler implements TestBean {

    private static final long serialVersionUID = 240L;

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(myStringProperty);
        res.sampleStart();
        // Do something ...
        res.setResponseData(myStringProperty.toLowerCase(Locale.ENGLISH), null);
        res.setDataType(SampleResult.TEXT);
        res.sampleEnd();
        res.setSuccessful(true);
        return res;
    }

    private String myStringProperty;

    // A TestBean is a Java Bean. Just define some properties and they will
    // automagically show up in the GUI.
    // A String property:
    public void setMyStringProperty(String s) {
        myStringProperty=s;
    }

    public String getMyStringProperty() {
        return myStringProperty;
    }
}
