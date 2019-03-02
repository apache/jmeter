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

package org.apache.jmeter.assertions.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.assertions.DurationAssertion;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * GUI for {@link DurationAssertion}
 */
public class DurationAssertionGui extends AbstractAssertionGui {

    private static final long serialVersionUID = 240L;

    private JTextField duration;

    public DurationAssertionGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "duration_assertion_title"; // $NON-NLS-1$
    }

    public String getDurationAttributesTitle() {
        return JMeterUtils.getResString("duration_assertion_duration_test"); // $NON-NLS-1$
    }

    @Override
    public TestElement createTestElement() {
        DurationAssertion el = new DurationAssertion();
        modifyTestElement(el);
        return el;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement el) {
        configureTestElement(el);
        if (el instanceof DurationAssertion) {
            DurationAssertion assertion = (DurationAssertion) el;
            assertion.setProperty(DurationAssertion.DURATION_KEY,duration.getText());
            saveScopeSettings(assertion);
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        duration.setText(""); //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof DurationAssertion){
            DurationAssertion da = (DurationAssertion) el;
            duration.setText(da.getPropertyAsString(DurationAssertion.DURATION_KEY));
            showScopeSettings(da);
        }
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 10));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new VerticalPanel();
        mainPanel.add(createScopePanel());

        // USER_INPUT
        VerticalPanel durationPanel = new VerticalPanel();
        durationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                getDurationAttributesTitle()));

        JPanel labelPanel = new JPanel(new BorderLayout(5, 0));
        JLabel durationLabel =
            new JLabel(JMeterUtils.getResString("duration_assertion_label")); // $NON-NLS-1$
        labelPanel.add(durationLabel, BorderLayout.WEST);

        duration = new JTextField();
        labelPanel.add(duration, BorderLayout.CENTER);
        durationLabel.setLabelFor(duration);
        durationPanel.add(labelPanel);

        mainPanel.add(durationPanel);
        add(mainPanel, BorderLayout.CENTER);
    }
}
