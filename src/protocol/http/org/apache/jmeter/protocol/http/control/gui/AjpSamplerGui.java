/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jmeter.protocol.http.control.gui;

import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.AjpSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class AjpSamplerGui extends HttpTestSampleGui {

    private static final long serialVersionUID = 240L;

    public AjpSamplerGui() {
        super(true);
    }

    @Override
    public TestElement createTestElement() {
        AjpSampler sampler = new AjpSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    // Use this instead of getLabelResource() otherwise getDocAnchor() below does not work
    @Override
    public String getStaticLabel() {
        return JMeterUtils.getResString("ajp_sampler_title"); // $NON-NLS-1$
    }

    @Override
    public String getDocAnchor() {// reuse documentation
        return super.getStaticLabel().replace(' ', '_'); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
