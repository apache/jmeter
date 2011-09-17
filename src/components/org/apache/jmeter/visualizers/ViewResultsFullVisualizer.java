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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 */
package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Base for ViewResults
 *
 */
public class ViewResultsFullVisualizer extends AbstractVisualizer
implements ActionListener, TreeSelectionListener, Clearable, ItemListener {

    private static final long serialVersionUID = 7338676747296593842L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final Color SERVER_ERROR_COLOR = Color.red;

    public static final Color CLIENT_ERROR_COLOR = Color.blue;

    public static final Color REDIRECT_COLOR = Color.green;

    private  JSplitPane mainSplit;

    private DefaultMutableTreeNode root;

    private DefaultTreeModel treeModel;

    private JTree jTree;

    private Component leftSide;

    private JTabbedPane rightSide;

    private JComboBox selectRenderPanel;

    private int selectedTab;

    protected static final String COMBO_CHANGE_COMMAND = "change_combo"; // $NON-NLS-1$

    private static final ImageIcon imageSuccess = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.success",  //$NON-NLS-1$
                    "icon_success_sml.gif")); //$NON-NLS-1$

    private static final ImageIcon imageFailure = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.failure",  //$NON-NLS-1$
                    "icon_warning_sml.gif")); //$NON-NLS-1$

    // Maximum size that we will display
    private static final int MAX_DISPLAY_SIZE =
        JMeterUtils.getPropDefault("view.results.tree.max_size", 200 * 1024); // $NON-NLS-1$

    private ResultRenderer resultsRender = null;

    private TreeSelectionEvent lastSelectionEvent;

    private JCheckBox autoScrollCB;

    /**
     * Constructor
     */
    public ViewResultsFullVisualizer() {
        super();
        init();
    }

    /** {@inheritDoc} */
    public void add(SampleResult sample) {
        updateGui(sample);
    }

    /**
     * Update the visualizer with new data.
     */
    private synchronized void updateGui(SampleResult res) {
        // Add sample
        DefaultMutableTreeNode currNode = new DefaultMutableTreeNode(res);
        treeModel.insertNodeInto(currNode, root, root.getChildCount());
        addSubResults(currNode, res);
        // Add any assertion that failed as children of the sample node
        AssertionResult assertionResults[] = res.getAssertionResults();
        int assertionIndex = currNode.getChildCount();
        for (int j = 0; j < assertionResults.length; j++) {
            AssertionResult item = assertionResults[j];

            if (item.isFailure() || item.isError()) {
                DefaultMutableTreeNode assertionNode = new DefaultMutableTreeNode(item);
                treeModel.insertNodeInto(assertionNode, currNode, assertionIndex++);
            }
        }

        if (root.getChildCount() == 1) {
            jTree.expandPath(new TreePath(root));
        }
        if (autoScrollCB.isSelected() && root.getChildCount() > 1) {
            jTree.scrollRowToVisible(root.getChildCount() - 1);
        }
    }

    private void addSubResults(DefaultMutableTreeNode currNode, SampleResult res) {
        SampleResult[] subResults = res.getSubResults();

        int leafIndex = 0;

        for (int i = 0; i < subResults.length; i++) {
            SampleResult child = subResults[i];

            if (log.isDebugEnabled()) {
                log.debug("updateGui1 : child sample result - " + child);
            }
            DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(child);

            treeModel.insertNodeInto(leafNode, currNode, leafIndex++);
            addSubResults(leafNode, child);
            // Add any assertion that failed as children of the sample node
            AssertionResult assertionResults[] = child.getAssertionResults();
            int assertionIndex = leafNode.getChildCount();
            for (int j = 0; j < assertionResults.length; j++) {
                AssertionResult item = assertionResults[j];

                if (item.isFailure() || item.isError()) {
                    DefaultMutableTreeNode assertionNode = new DefaultMutableTreeNode(item);
                    treeModel.insertNodeInto(assertionNode, leafNode, assertionIndex++);
                }
            }
        }
    }

    /** {@inheritDoc} */
    public void clearData() {
        while (root.getChildCount() > 0) {
            // the child to be removed will always be 0 'cos as the nodes are
            // removed the nth node will become (n-1)th
            treeModel.removeNodeFromParent((DefaultMutableTreeNode) root.getChildAt(0));
        }
        resultsRender.clearData();
    }

    /** {@inheritDoc} */
    public String getLabelResource() {
        return "view_results_tree_title"; // $NON-NLS-1$
    }

    /**
     * Initialize this visualizer
     */
    protected void init() {
        log.debug("init() - pass");
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        leftSide = createLeftPanel();
        // Prepare the common tab
        rightSide = new JTabbedPane();

        // Create the split pane
        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, rightSide);
        add(mainSplit, BorderLayout.CENTER);
        // init right side with first render
        resultsRender.setRightSide(rightSide);
        resultsRender.init();
    }

    /** {@inheritDoc} */
    public void valueChanged(TreeSelectionEvent e) {
        lastSelectionEvent = e;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();

        if (node != null) {
            // to restore last tab used
            if (rightSide.getTabCount() > selectedTab) {
                resultsRender.setLastSelectedTab(rightSide.getSelectedIndex());
            }
            Object userObject = node.getUserObject();
            resultsRender.setSamplerResult(userObject);
            resultsRender.setupTabPane(); // Processes Assertions
            // display a SampleResult
            if (userObject instanceof SampleResult) {
                SampleResult sampleResult = (SampleResult) userObject;
                if ((SampleResult.TEXT).equals(sampleResult.getDataType())){
                    resultsRender.renderResult(sampleResult);
                } else {
                    byte[] responseBytes = sampleResult.getResponseData();
                    if (responseBytes != null) {
                        resultsRender.renderImage(sampleResult);
                    }
                }
            }
        }
    }

    private Component createLeftPanel() {
        SampleResult rootSampleResult = new SampleResult();
        rootSampleResult.setSampleLabel("Root");
        rootSampleResult.setSuccessful(true);
        root = new DefaultMutableTreeNode(rootSampleResult);

        treeModel = new DefaultTreeModel(root);
        jTree = new JTree(treeModel);
        jTree.setCellRenderer(new ResultsNodeRenderer());
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.addTreeSelectionListener(this);
        jTree.setRootVisible(false);
        jTree.setShowsRootHandles(true);
        JScrollPane treePane = new JScrollPane(jTree);
        treePane.setPreferredSize(new Dimension(200, 300));

        VerticalPanel leftPane = new VerticalPanel();
        leftPane.add(treePane, BorderLayout.CENTER);
        VerticalPanel leftDownPane = new VerticalPanel();
        leftDownPane.add(createComboRender(), BorderLayout.NORTH);
        autoScrollCB = new JCheckBox(JMeterUtils.getResString("view_results_autoscroll"));
        autoScrollCB.setSelected(false);
        autoScrollCB.addItemListener(this);
        leftDownPane.add(autoScrollCB, BorderLayout.SOUTH);
        leftPane.add(leftDownPane, BorderLayout.SOUTH);
        return leftPane;
    }

    /**
     * Create the drop-down list to changer render
     * @return List of all render (implement ResultsRender)
     */
    private Component createComboRender() {
        ComboBoxModel nodesModel = new DefaultComboBoxModel();
        // drop-down list for renderer
        selectRenderPanel = new JComboBox(nodesModel);
        selectRenderPanel.setActionCommand(COMBO_CHANGE_COMMAND);
        selectRenderPanel.addActionListener(this);

        // if no results render in jmeter.properties, load Standard (default)
        List<String> classesToAdd = Collections.<String>emptyList();
        try {
            classesToAdd = JMeterUtils.findClassesThatExtend(ResultRenderer.class);
        } catch (IOException e1) {
            // ignored
        }
        String textRenderer = JMeterUtils.getResString("view_results_render_text"); // $NON-NLS-1$
        Object textObject = null;
        for (String clazz : classesToAdd) {
            try {
                // Instantiate render classes
                final ResultRenderer renderer = (ResultRenderer) Class.forName(clazz).newInstance();
                if (textRenderer.equals(renderer.toString())){
                    textObject=renderer;
                }
                renderer.setBackgroundColor(getBackground());
                selectRenderPanel.addItem(renderer);
            } catch (Exception e) {
                log.warn("Error in load result render:" + clazz, e);
            }
        }
        nodesModel.setSelectedItem(textObject); // preset to "Text" option
        return selectRenderPanel;
    }

    /** {@inheritDoc} */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (COMBO_CHANGE_COMMAND.equals(command)) {
            JComboBox jcb = (JComboBox) event.getSource();

            if (jcb != null) {
                resultsRender = (ResultRenderer) jcb.getSelectedItem();
                if (rightSide != null) {
                    // to restore last selected tab (better user-friendly)
                    selectedTab = rightSide.getSelectedIndex();
                    // Remove old right side
                    mainSplit.remove(rightSide);

                    // create and add a new right side
                    rightSide = new JTabbedPane();
                    mainSplit.add(rightSide);
                    resultsRender.setRightSide(rightSide);
                    resultsRender.setLastSelectedTab(selectedTab);
                    log.debug("selectedTab=" + selectedTab);
                    resultsRender.init();
                    // To display current sampler result before change
                    this.valueChanged(lastSelectionEvent);
                }
            }
        }
    }

    public static String getResponseAsString(SampleResult res) {
        String response = null;
        if ((SampleResult.TEXT).equals(res.getDataType())) {
            // Showing large strings can be VERY costly, so we will avoid
            // doing so if the response
            // data is larger than 200K. TODO: instead, we could delay doing
            // the result.setText
            // call until the user chooses the "Response data" tab. Plus we
            // could warn the user
            // if this happens and revert the choice if he doesn't confirm
            // he's ready to wait.
            int len = res.getResponseData().length;
            if (MAX_DISPLAY_SIZE > 0 && len > MAX_DISPLAY_SIZE) {
                response = JMeterUtils.getResString("view_results_response_too_large_message") //$NON-NLS-1$
                    + len + " > Max: "+MAX_DISPLAY_SIZE;
                log.warn(response);
            } else {
                response = res.getResponseDataAsString();
            }
        }
        return response;
    }

    private static class ResultsNodeRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 4159626601097711565L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean focus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
            boolean failure = true;
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof SampleResult) {
                failure = !(((SampleResult) userObject).isSuccessful());
            } else if (userObject instanceof AssertionResult) {
                AssertionResult assertion = (AssertionResult) userObject;
                failure = assertion.isError() || assertion.isFailure();
            }

            // Set the status for the node
            if (failure) {
                this.setForeground(Color.red);
                this.setIcon(imageFailure);
            } else {
                this.setIcon(imageSuccess);
            }
            return this;
        }
    }

    /**
     * Handler for Checkbox
     */
    public void itemStateChanged(ItemEvent e) {
        // NOOP state is held by component
    }
}
