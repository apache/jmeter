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

package org.apache.jmeter.control.gui.wdc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.WeightedDistributionController;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class WeightedDistributionControllerGui.
 */
public class WeightedDistributionControllerGui extends AbstractControllerGui {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2245012323333943250L;

    /** The log. */
    private static final Logger log = LoggingManager.getLoggerForClass();

    /** The Constant LABEL_RESOURCE. */
    private static final String LABEL_RESOURCE = "weighted_distribution_controller_title";

    /** The Constant SEED_LABEL_RESOURCE. */
    private static final String SEED_LABEL_RESOURCE = "weighted_distribution_controller_seed";

    /** The Constant SEED_ERR_MRG_RES. */
    private static final String SEED_ERR_MRG_RES = "weighted_distribution_controller_seed_error";

    /** The table. */
    private JTable table;

    /** The seed field. */
    private JTextField seedField;

    /**
     * Checks if is current element is a weighted distribution controller.
     *
     * @return true, if is current element weighted distribution controller
     */
    public static WeightedDistributionController getCurrentWeightedDistributionController() {
        TestElement currElem = GuiPackage.getInstance().getCurrentElement();
        return (WeightedDistributionController) (currElem instanceof WeightedDistributionController
                ? currElem : null);
    }

    /**
     * Builds and populates the jmeter variables object.
     */
    public static void buildJMeterVariables() {
        HashTree testPlan = GuiPackage.getInstance().getTreeModel()
                .getTestPlan();
        JMeterVariablesBuilder varBuilder = new JMeterVariablesBuilder();
        testPlan.traverse(varBuilder);
    }

    /**
     * Instantiates a new weighted distribution controller gui.
     */
    public WeightedDistributionControllerGui() {
        super();
        init();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        WeightedDistributionController wdc = new WeightedDistributionController();
        modifyTestElement(wdc);
        return wdc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
     */
    @Override
    public String getLabelResource() {
        return LABEL_RESOURCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.
     * jmeter.testelement.TestElement)
     */
    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(getTable());
        Data model = ((PowerTableModel) getTable().getModel()).getData();
        model.reset();
        if (el instanceof WeightedDistributionController && model.size() > 0) {
            WeightedDistributionController wdc = (WeightedDistributionController) el;
            setControllerSeedFromGuiField(wdc);

            if (wdc.getNode() != null) {
                setSubControllersFromTableModel(model, wdc);
            }
        }
        configureTestElement(el);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.gui.AbstractJMeterGuiComponent#configure(org.apache.
     * jmeter.testelement.TestElement)
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        ((PowerTableModel) getTable().getModel()).clearData();

        if (el instanceof WeightedDistributionController) {
            WeightedDistributionController wdc = (WeightedDistributionController) el;

            setGuiSeedFieldFromController(wdc);

            if (wdc.getNode() != null) {
                wdc.resetCumulativeProbability();
                buildJMeterVariables();

                // iterate through child test emeb
                for (int childNodeIdx = 0; childNodeIdx < wdc.getNode()
                        .getChildCount(); childNodeIdx++) {
                    setTableModelRowFromController(wdc, childNodeIdx);
                }
            }
        }
    }

    /**
     * Gets the table.
     *
     * @return the table
     */
    protected JTable getTable() {
        return this.table;
    }

    /**
     * Initialize the gui.
     */
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        // Force the table to be at least 70 pixels high
        add(Box.createVerticalStrut(70), BorderLayout.WEST);
        add(createRandomSeedPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates the seed panel.
     *
     * @return the component
     */
    private Component createRandomSeedPanel() {
        Box seedPanel = Box.createHorizontalBox();
        JLabel seedLabel = new JLabel(
                JMeterUtils.getResString(SEED_LABEL_RESOURCE));// $NON-NLS-1$
        seedPanel.add(seedLabel);

        seedField = new JTextField(0);
        seedField.setName("seed field");
        seedPanel.add(seedField);

        return seedPanel;
    }

    /**
     * Creates the table panel.
     *
     * @return the component
     */
    private Component createTablePanel() {
        table = WeightedDistributionTableModel.buildWeightedDistributionTable();
        return makeScrollPane(table);
    }

    /**
     * Sets the controller seed from gui field.
     *
     * @param wdc
     *            the new controller seed from gui field
     */
    private void setControllerSeedFromGuiField(
            WeightedDistributionController wdc) {
        // Determine if the seed has been set
        if (seedField.getText().length() > 0) {
            try {
                wdc.setGeneratorSeed(Long.parseLong(seedField.getText()));
            } catch (NumberFormatException nfe) {
                JMeterUtils.reportErrorToUser(
                        JMeterUtils.getResString(SEED_ERR_MRG_RES));
            }
        }
    }

    /**
     * Sets the gui seed field from controller.
     *
     * @param wdc
     *            the new gui seed field from controller
     */
    private void setGuiSeedFieldFromController(
            WeightedDistributionController wdc) {
        if (wdc.getGeneratorSeed() != WeightedDistributionController.DFLT_GENERATOR_SEED) {
            seedField.setText(Long.toString(wdc.getGeneratorSeed()));
        } else {
            seedField.setText("");
        }
    }

    /**
     * Sets the sub controllers from table model.
     *
     * @param model
     *            the model
     * @param wdc
     *            the wdc
     */
    private void setSubControllersFromTableModel(Data model,
            WeightedDistributionController wdc) {
        while (model.next()) {

            // Find the index value of the element from wdc.children
            int childNodeIdx = (int) model.getColumnValue(
                    WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN);
            // Retrieve that element
            TestElement currTestElement = ((JMeterTreeNode) wdc.getNode()
                    .getChildAt(childNodeIdx)).getTestElement();

            // Update element with weight from the table
            String weight = ((String) model.getColumnValue(
                    WeightedDistributionTableModel.WEIGHT_COLUMN)).trim();
            if (weight.equals("") || weight.equals("0")) {
                currTestElement
                        .removeProperty(WeightedDistributionController.WEIGHT);
            } else if (!weight.trim()
                    .equals(currTestElement.getPropertyAsString(
                            WeightedDistributionController.WEIGHT))) {
                currTestElement.setProperty(
                        WeightedDistributionController.WEIGHT, weight);
            }

            // Update element name if needed
            String elemName = ((String) model.getColumnValue(
                    WeightedDistributionTableModel.ELEMENT_NAME_COLUMN)).trim();
            if (!elemName.equals(currTestElement.getName())) {
                currTestElement.setName(elemName);
            }

            // update enabled if needed
            boolean enabled = (boolean) model.getColumnValue(
                    WeightedDistributionTableModel.ENABLED_COLUMN);
            if (enabled != currTestElement.isEnabled()) {
                currTestElement.setEnabled(enabled);
            }
        }
    }

    /**
     * Sets the table model row from controller.
     *
     * @param wdc
     *            the wdc
     * @param childNodeIdx
     *            the child node idx
     */
    private void setTableModelRowFromController(
            WeightedDistributionController wdc, int childNodeIdx) {
        TestElement currTestElement = null;
        try {
            currTestElement = wdc.getChildTestElement(childNodeIdx);
        } catch (Exception ex) {
            log.error(
                    "error retrieving TestElement corresponding to child node #"
                            + childNodeIdx,
                    ex);
            return;
        }

        // filter only controllers & samplers
        if (currTestElement instanceof Controller
                || currTestElement instanceof Sampler) {

            // Evaluate any expressions in the weight variable
            TestElement currEvalTestElement = wdc
                    .evaluateTestElement(currTestElement);

            // Add data to the table
            boolean enabled = currTestElement.isEnabled();
            String name = currTestElement.getName();
            String weight = currTestElement.getPropertyAsString(
                    WeightedDistributionController.WEIGHT, Integer.toString(
                            WeightedDistributionController.DFLT_WEIGHT));
            String evalWeight = currEvalTestElement.getPropertyAsString(
                    WeightedDistributionController.WEIGHT, Integer.toString(
                            WeightedDistributionController.DFLT_WEIGHT));
            float probability = currTestElement.isEnabled()
                    ? wdc.calculateProbability(
                            currEvalTestElement.getPropertyAsInt(
                                    WeightedDistributionController.WEIGHT,
                                    WeightedDistributionController.DFLT_WEIGHT))
                    : 0.0f;

            ((PowerTableModel) getTable().getModel())
                    .addRow(new Object[] { enabled, name, weight, evalWeight,
                            probability, childNodeIdx });
        }
    }
}

/**
 * Walks the TestPlan tree and adds User Defined Variables to JMeter variables
 */
class JMeterVariablesBuilder implements HashTreeTraverser {

    @Override
    public void addNode(Object node, HashTree subTree) {
        if (node instanceof JMeterTreeNode) {
            Object userObj = ((JMeterTreeNode) node).getUserObject();

            if (userObj instanceof TestPlan) {
                ((TestPlan) userObj).prepareForPreCompile();
                Map<String, String> args = ((TestPlan) userObj)
                        .getUserDefinedVariables();
                JMeterVariables vars = new JMeterVariables();
                vars.putAll(args);
                JMeterContextService.getContext().setVariables(vars);
            }

            if (userObj instanceof Arguments) {
                Arguments cloneArgs = (Arguments) ((Arguments) userObj).clone();
                cloneArgs.setRunningVersion(true);
                Map<String, String> args = cloneArgs.getArgumentsAsMap();
                JMeterContextService.getContext().getVariables().putAll(args);
            }
        }
    }

    @Override
    public void subtractNode() {
        // NOT IMPLEMENTED
    }

    @Override
    public void processPath() {
        // NOT IMPLEMENTED
    }

}
