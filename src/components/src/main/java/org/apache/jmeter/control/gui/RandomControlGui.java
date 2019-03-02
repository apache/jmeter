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

package org.apache.jmeter.control.gui;

import javax.swing.JCheckBox;

import org.apache.jmeter.control.InterleaveControl;
import org.apache.jmeter.control.RandomController;
import org.apache.jmeter.gui.util.CheckBoxPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

public class RandomControlGui extends AbstractControllerGui {
    private static final long serialVersionUID = 240L;

    private JCheckBox style;

    public RandomControlGui() {
        init();
    }

    @Override
    public TestElement createTestElement() {
        RandomController ic = new RandomController();
        modifyTestElement(ic);
        return ic;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement ic) {
        configureTestElement(ic);
        if (style.isSelected()) {
            ((RandomController) ic).setStyle(InterleaveControl.IGNORE_SUB_CONTROLLERS);
        } else {
            ((RandomController) ic).setStyle(InterleaveControl.USE_SUB_CONTROLLERS);
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        style.setSelected(false);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (((RandomController) el).getStyle() == InterleaveControl.IGNORE_SUB_CONTROLLERS) {
            style.setSelected(true);
        } else {
            style.setSelected(false);
        }
    }

    @Override
    public String getLabelResource() {
        return "random_control_title"; // $NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());

        style = new JCheckBox(JMeterUtils.getResString("ignore_subcontrollers")); // $NON-NLS-1$
        add(CheckBoxPanel.wrap(style));
    }
}
