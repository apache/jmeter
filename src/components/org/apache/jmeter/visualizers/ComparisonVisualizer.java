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

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.assertions.CompareAssertionResult;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

public class ComparisonVisualizer extends AbstractVisualizer implements Clearable {
    private static final long serialVersionUID = 240L;

    private JTree resultsTree;

    private DefaultTreeModel treeModel;

    private DefaultMutableTreeNode root;

    private JTextPane base;
    private JTextPane secondary;

    public ComparisonVisualizer() {
        super();
        init();
    }

    @Override
    public void add(final SampleResult sample) {
        JMeterUtils.runSafe(false, () -> {
            DefaultMutableTreeNode currNode = new DefaultMutableTreeNode(sample);
            treeModel.insertNodeInto(currNode, root, root.getChildCount());
            if (root.getChildCount() == 1) {
                resultsTree.expandPath(new TreePath(root));
            }
        });
    }

    @Override
    public String getLabelResource() {
        return "comparison_visualizer_title"; //$NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.add(getTreePanel());
        split.add(getSideBySidePanel());
        add(split, BorderLayout.CENTER);
    }

    private JComponent getSideBySidePanel() {
        JPanel main = new JPanel(new GridLayout(1, 2));
        JScrollPane base = new JScrollPane(getBaseTextPane());
        base.setPreferredSize(base.getMinimumSize());
        JScrollPane secondary = new JScrollPane(getSecondaryTextPane());
        secondary.setPreferredSize(secondary.getMinimumSize());
        main.add(base);
        main.add(secondary);
        main.setPreferredSize(main.getMinimumSize());
        return main;
    }

    private JTextPane getBaseTextPane() {
        base = new JTextPane();
        base.setEditable(false);
        base.setBackground(getBackground());
        return base;
    }

    private JTextPane getSecondaryTextPane() {
        secondary = new JTextPane();
        secondary.setEditable(false);
        return secondary;
    }

    private JComponent getTreePanel() {
        root = new DefaultMutableTreeNode("Root"); //$NON-NLS-1$
        treeModel = new DefaultTreeModel(root);
        resultsTree = new JTree(treeModel);
        resultsTree.setCellRenderer(new TreeNodeRenderer());
        resultsTree.setCellRenderer(new TreeNodeRenderer());
        resultsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        resultsTree.addTreeSelectionListener(new Selector());
        resultsTree.setRootVisible(false);
        resultsTree.setShowsRootHandles(true);

        JScrollPane treePane = new JScrollPane(resultsTree);
        treePane.setPreferredSize(new Dimension(150, 50));
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(treePane);
        return panel;
    }

    private class Selector implements TreeSelectionListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            try {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) resultsTree.getLastSelectedPathComponent();
                SampleResult sr = (SampleResult) node.getUserObject();
                AssertionResult[] results = sr.getAssertionResults();
                CompareAssertionResult result = null;
                for (AssertionResult r : results) {
                    if (r instanceof CompareAssertionResult) {
                        result = (CompareAssertionResult) r;
                        break;
                    }
                }
                if (result == null) {
                    result = new CompareAssertionResult(getName());
                }
                base.setText(result.getBaseResult());
                secondary.setText(result.getSecondaryResult());
            } catch (Exception err) {
                base.setText(JMeterUtils.getResString("comparison_invalid_node") + err); //$NON-NLS-1$
                secondary.setText(JMeterUtils.getResString("comparison_invalid_node") + err); //$NON-NLS-1$
            }
            base.setCaretPosition(0);
            secondary.setCaretPosition(0);
        }
    }

    @Override
    public void clearData() {
        while (root.getChildCount() > 0) {
            // the child to be removed will always be 0 'cos as the nodes are
            // removed the nth node will become (n-1)th
            treeModel.removeNodeFromParent((DefaultMutableTreeNode) root.getChildAt(0));
            base.setText(""); //$NON-NLS-1$
            secondary.setText(""); //$NON-NLS-1$
        }
    }

}
