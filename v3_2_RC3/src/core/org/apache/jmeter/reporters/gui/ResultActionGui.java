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

package org.apache.jmeter.reporters.gui;

import java.awt.BorderLayout;

import javax.swing.Box;

import org.apache.jmeter.reporters.ResultAction;
import org.apache.jmeter.gui.OnErrorPanel;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.OnErrorTestElement;
import org.apache.jmeter.testelement.TestElement;

/**
 * Create a Result Action Test Element
 *
 */
public class ResultActionGui extends AbstractPostProcessorGui {

    private static final long serialVersionUID = 240L;

    private OnErrorPanel errorPanel;

    public ResultActionGui() {
        super();
        init();
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
     */
    @Override
    public String getLabelResource() {
        return "resultaction_title"; //$NON-NLS-1$
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        errorPanel.configure(((OnErrorTestElement) el).getErrorAction());
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        ResultAction resultAction = new ResultAction();
        modifyTestElement(resultAction);
        return resultAction;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement te) {
        super.configureTestElement(te);
        ((OnErrorTestElement) te).setErrorAction(errorPanel.getOnErrorSetting());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        errorPanel.configure(OnErrorTestElement.ON_ERROR_CONTINUE);
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        errorPanel = new OnErrorPanel();
        box.add(errorPanel);
        add(box, BorderLayout.NORTH);
    }
}
