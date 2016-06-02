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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.WeightedDistributionController;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.gui.GuiUtils;

public class WeightedDistributionControllerGui extends AbstractControllerGui {
    private static final long serialVersionUID = 2245012323333943250L;

    private static final String ERRMSG = "Seed must be an integer";

    private JTable table;
    JTextField seedField;
    JCheckBox perThreadCheckbox;

    public static boolean isCurrentElementWeightedDistributionController() {
        return GuiPackage.getInstance()
                .getCurrentElement() instanceof WeightedDistributionController;
    }
    
    public WeightedDistributionControllerGui() {
        super();
        init();
    }

    protected JTable getTable() {
        return this.table;
    }

    @Override
    public TestElement createTestElement() {
        WeightedDistributionController wdc = new WeightedDistributionController();
        modifyTestElement(wdc);
        return wdc;
    }

    @Override
    public String getLabelResource() {
        return getClass().getName();
    }

    @Override
    public String getStaticLabel() {
        return "Weighted Distribution Controller";
    }

    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(getTable());
        Data model = ((PowerTableModel) getTable().getModel()).getData();
        model.reset();
        if (el instanceof WeightedDistributionController && model.size() > 0) {
            WeightedDistributionController wdc = (WeightedDistributionController) el;
            if (seedField.getText().length() > 0) {
                try {
                    wdc.setSeed(Long.parseLong(seedField.getText()));
                } catch (NumberFormatException nfe) {
                    JMeterUtils.reportErrorToUser(ERRMSG);
                }

            }
            wdc.setPerThread(perThreadCheckbox.isSelected());
            if (wdc.getNode() != null) {
                while (model.next()) {
                    int childNodeIdx = (int) model.getColumnValue(
                            WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN);
                    TestElement currTestElement = ((JMeterTreeNode) wdc
                            .getNode().getChildAt(childNodeIdx))
                                    .getTestElement();
                    currTestElement.setProperty(
                            WeightedDistributionController.WEIGHT,
                            (int) model.getColumnValue(
                                    WeightedDistributionTableModel.WEIGHT_COLUMN));
                    currTestElement.setName((String) model.getColumnValue(
                            WeightedDistributionTableModel.ELEMENT_NAME_COLUMN));
                    currTestElement.setEnabled((boolean) model.getColumnValue(
                            WeightedDistributionTableModel.ENABLED_COLUMN));
                }
            }
        }
        this.configureTestElement(el);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        ((PowerTableModel) getTable().getModel()).clearData();
        if (el instanceof WeightedDistributionController) {
            WeightedDistributionController wdc = (WeightedDistributionController) el;
            if (wdc.getSeed() != WeightedDistributionController.DFLT_SEED) {
                seedField.setText(Long.toString(wdc.getSeed()));
            } else {
                seedField.setText("");
            }
            perThreadCheckbox.setSelected(wdc.isPerThread());

            if (wdc.getNode() != null) {
                wdc.resetCumulativeProbability();
                for (int childNodeIdx = 0; childNodeIdx < wdc.getNode()
                        .getChildCount(); childNodeIdx++) {
                    JMeterTreeNode currNode = (JMeterTreeNode) wdc.getNode()
                            .getChildAt(childNodeIdx);
                    TestElement currTestElement = currNode.getTestElement();
                    if (currTestElement instanceof Controller
                            || currTestElement instanceof Sampler) {
                        int weight = currTestElement.getPropertyAsInt(
                                WeightedDistributionController.WEIGHT,
                                WeightedDistributionController.DFLT_WEIGHT);
                        ((PowerTableModel) getTable().getModel()).addRow(
                                new Object[] { currTestElement.isEnabled(),
                                        currTestElement.getName(), weight,
                                        currTestElement.isEnabled() ? wdc
                                                .calculateProbability(weight)
                                                : 0.0f,
                                        childNodeIdx });
                    }
                }
            }
        }
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        // Force the table to be at least 70 pixels high
        add(Box.createVerticalStrut(70), BorderLayout.WEST);
        add(createRandomSeedPanel(), BorderLayout.SOUTH);
    }

    private Component createRandomSeedPanel() {
        Box seedPanel = Box.createHorizontalBox();
        JLabel seedLabel = new JLabel("Seed for Random function: ");//$NON-NLS-1$
        seedPanel.add(seedLabel);

        seedField = new JTextField(0);
        seedField.setName("seed field");
        seedPanel.add(seedField);

        perThreadCheckbox = new JCheckBox();
        perThreadCheckbox.setName("per thread");
        perThreadCheckbox.setText("Per Thread (User)?");
        seedPanel.add(perThreadCheckbox);

        return seedPanel;
    }

    private Component createTablePanel() {
        table = WeightedDistributionTableModel.buildWeightedDistributionTable();
        return makeScrollPane(table);
    }
}