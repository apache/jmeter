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

import java.awt.event.ItemEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.ThroughputController;
import org.apache.jmeter.gui.util.CheckBoxPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

public class ThroughputControllerGui extends AbstractControllerGui {
    private static final long serialVersionUID = 240L;

    private JComboBox<String> styleBox;

    private int style;

    private JTextField throughput;

    private JCheckBox perthread;

    private boolean isPerThread = true;

    // These must not be static, otherwise Language change does not work
    private final String BYNUMBER_LABEL = JMeterUtils.getResString("throughput_control_bynumber_label"); // $NON-NLS-1$

    private final String BYPERCENT_LABEL = JMeterUtils.getResString("throughput_control_bypercent_label"); // $NON-NLS-1$

    private final String THROUGHPUT_LABEL = JMeterUtils.getResString("throughput_control_tplabel"); // $NON-NLS-1$

    private final String PERTHREAD_LABEL = JMeterUtils.getResString("throughput_control_perthread_label"); // $NON-NLS-1$

    public ThroughputControllerGui() {
        init();
    }

    @Override
    public TestElement createTestElement() {
        ThroughputController tc = new ThroughputController();
        modifyTestElement(tc);
        return tc;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement tc) {
        configureTestElement(tc);
        ((ThroughputController) tc).setStyle(style);
        ((ThroughputController) tc).setPerThread(isPerThread);
        if (style == ThroughputController.BYNUMBER) {
            try {
                ((ThroughputController) tc).setMaxThroughput(Integer.parseInt(throughput.getText().trim()));
            } catch (NumberFormatException e) {
                // In case we are converting back from floating point, drop the decimal fraction
                ((ThroughputController) tc).setMaxThroughput(throughput.getText().trim().split("\\.")[0]); // $NON-NLS-1$
            }
        } else {
            try {
                ((ThroughputController) tc).setPercentThroughput(Float.parseFloat(throughput.getText().trim()));
            } catch (NumberFormatException e) {
                ((ThroughputController) tc).setPercentThroughput(throughput.getText());
            }
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        styleBox.setSelectedIndex(1);
        throughput.setText("1"); // $NON-NLS-1$
        perthread.setSelected(false);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (((ThroughputController) el).getStyle() == ThroughputController.BYNUMBER) {
            styleBox.getModel().setSelectedItem(BYNUMBER_LABEL);
            throughput.setText(((ThroughputController) el).getMaxThroughput());
        } else {
            styleBox.setSelectedItem(BYPERCENT_LABEL);
            throughput.setText(((ThroughputController) el).getPercentThroughput());
        }
        perthread.setSelected(((ThroughputController) el).isPerThread());
    }

    @Override
    public String getLabelResource() {
        return "throughput_control_title"; // $NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());

        DefaultComboBoxModel<String> styleModel = new DefaultComboBoxModel<>();
        styleModel.addElement(BYNUMBER_LABEL);
        styleModel.addElement(BYPERCENT_LABEL);
        styleBox = new JComboBox<>(styleModel);
        styleBox.addActionListener(evt -> {
            if (((String) styleBox.getSelectedItem()).equals(BYNUMBER_LABEL)) {
                style = ThroughputController.BYNUMBER;
            } else {
                style = ThroughputController.BYPERCENT;
            }
        });
        add(styleBox);

        // TYPE FIELD
        JPanel tpPanel = new JPanel();
        JLabel tpLabel = new JLabel(THROUGHPUT_LABEL);
        tpPanel.add(tpLabel);

        // TEXT FIELD
        throughput = new JTextField(15);
        tpPanel.add(throughput);
        throughput.setText("1"); // $NON-NLS-1$
        tpPanel.add(throughput);
        add(tpPanel);

        // PERTHREAD FIELD
        perthread = new JCheckBox(PERTHREAD_LABEL, isPerThread);
        perthread.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                isPerThread = true;
            } else {
                isPerThread = false;
            }
        });
        add(CheckBoxPanel.wrap(perthread));
    }
}
