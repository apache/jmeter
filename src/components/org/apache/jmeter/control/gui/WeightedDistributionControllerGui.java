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
import java.util.Map;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.WeightedDistributionController;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
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

    /** The Constant ERRMSG. */
    private static final String ERRMSG = "Seed must be an integer";
    
    /** The Constant log. */
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    /** The table. */
    private JTable table;
    
    /** The seed field. */
    private JTextField seedField;
    
    /** The value replacer for evaluating variable properties. */
    private transient ValueReplacer replacer;

    
    /**
     * Checks if is current element is a weighted distribution controller.
     *
     * @return true, if is current element weighted distribution controller
     */
    public static boolean isCurrentElementWeightedDistributionController() {
        return GuiPackage.getInstance()
                .getCurrentElement() instanceof WeightedDistributionController;
    }
    
    /**
     * Builds and populates the jmeter variables object.
     */
    public static void buildJMeterVariables() {
        HashTree  testPlan = GuiPackage.getInstance().getTreeModel().getTestPlan();
        JMeterVariablesBuilder varBuilder = new JMeterVariablesBuilder();
        testPlan.traverse(varBuilder);
    }
    
    /**
     * Instantiates a new weighted distribution controller gui.
     */
    public WeightedDistributionControllerGui() {
        super();
        init();
        replacer = GuiPackage.getInstance().getReplacer();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        WeightedDistributionController wdc = new WeightedDistributionController();
        modifyTestElement(wdc);
        return wdc;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
     */
    @Override
    public String getLabelResource() {
        return getClass().getName();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#getStaticLabel()
     */
    @Override
    public String getStaticLabel() {
        return "Weighted Distribution Controller";
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(getTable());
        Data model = ((PowerTableModel) getTable().getModel()).getData();
        model.reset();
        if (el instanceof WeightedDistributionController && model.size() > 0) {
            WeightedDistributionController wdc = (WeightedDistributionController) el;
            
            // Determine if the seed has been set
            if (seedField.getText().length() > 0) {
                try {
                    wdc.setSeed(Long.parseLong(seedField.getText()));
                } catch (NumberFormatException nfe) {
                    JMeterUtils.reportErrorToUser(ERRMSG);
                }
            }
            
            if (wdc.getNode() != null) {
                while (model.next()) {
                    // Find the index value of the element from wdc.children
                    int childNodeIdx = (int) model.getColumnValue(
                            WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN);
                    // Retrieve that element
                    TestElement currTestElement = ((JMeterTreeNode) wdc
                            .getNode().getChildAt(childNodeIdx))
                                    .getTestElement();
                    //Update element with values from the table
                    currTestElement.setProperty(WeightedDistributionController.WEIGHT,
                            (String) model.getColumnValue(
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

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#configure(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        ((PowerTableModel) getTable().getModel()).clearData();
        if (el instanceof WeightedDistributionController) {
            WeightedDistributionController wdc = (WeightedDistributionController) el;
            
            // Set the seed field with the current value from the WDC
            if (wdc.getSeed() != WeightedDistributionController.DFLT_SEED) {
                seedField.setText(Long.toString(wdc.getSeed()));
            } else {
                seedField.setText("");
            }

            if (wdc.getNode() != null) {
                wdc.resetCumulativeProbability();
                buildJMeterVariables();
                
                // iterate through child test emeb
                for (int childNodeIdx = 0; childNodeIdx < wdc.getNode()
                        .getChildCount(); childNodeIdx++) {
                    
                    TestElement currTestElement = null;
                    try {
                        currTestElement = ((JMeterTreeNode)wdc.getNode().getChildAt(childNodeIdx)).getTestElement();
                    } catch (Exception ex) {
                        log.error("error retrieving TestElement corresponding to child node #" + childNodeIdx, ex);
                        continue;
                    }
                    
                    // filter only controllers & samplers
                    if (currTestElement instanceof Controller
                            || currTestElement instanceof Sampler) {
                        
                        // Evaluate any expressions in the weight variable
                        TestElement currEvalTestElement = evaluateTestElement(currTestElement);
                        
                        // Add data to the table
                        ((PowerTableModel) getTable().getModel()).addRow(
                                new Object[] {
                                        currTestElement.isEnabled(),
                                        currTestElement.getName(),
                                        currTestElement.getPropertyAsString(WeightedDistributionController.WEIGHT,
                                                Integer.toString(WeightedDistributionController.DFLT_WEIGHT)),
                                        currEvalTestElement.getPropertyAsString(WeightedDistributionController.WEIGHT),
                                        currTestElement.isEnabled() ? wdc
                                                .calculateProbability(currEvalTestElement.getPropertyAsInt(WeightedDistributionController.WEIGHT,
                                                        WeightedDistributionController.DFLT_WEIGHT))
                                                : 0.0f,
                                        childNodeIdx });
                    }
                }
            }
        }
    }

    
    /**
     * Returns a clone of the test element with any variable properties evaluated.
     *
     * @param testElement the source test element
     * @return the cloned and evaluated test element
     */
    public TestElement evaluateTestElement(TestElement testElement) {
        TestElement clonedTestElem = (TestElement) testElement.clone();
        
        try {
            replacer.replaceValues(clonedTestElem);
        } catch (InvalidVariableException e) {
            return testElement;
        }
        
        clonedTestElem.setRunningVersion(true);
        return clonedTestElem;
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
     * Inits the gui.
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
        JLabel seedLabel = new JLabel("Seed for Random function: ");//$NON-NLS-1$
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
                ((TestPlan)userObj).prepareForPreCompile(); 
                Map<String, String> args = ((TestPlan) userObj).getUserDefinedVariables();
                JMeterVariables vars = new JMeterVariables();
                vars.putAll(args);
                JMeterContextService.getContext().setVariables(vars);
            }
    
            if (userObj instanceof Arguments) {
                Arguments cloneArgs = (Arguments)((Arguments)userObj).clone();
                cloneArgs.setRunningVersion(true);
                Map<String, String> args = cloneArgs.getArgumentsAsMap();
                JMeterContextService.getContext().getVariables().putAll(args);
            }
        }
    }

    @Override
    public void subtractNode() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void processPath() {
        // TODO Auto-generated method stub
        
    }
    
}