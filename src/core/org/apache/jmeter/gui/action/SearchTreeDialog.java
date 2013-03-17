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

package org.apache.jmeter.gui.action;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * FIXME Why is searchTF not getting focus correctly after having been setVisible(false) once
 */
public class SearchTreeDialog extends JDialog implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -4436834972710248247L;

    private static final Logger logger = LoggingManager.getLoggerForClass();

    private JButton searchButton;

    private JLabeledTextField searchTF;

    private JCheckBox isRegexpCB;

    private JCheckBox isCaseSensitiveCB;

    private JButton cancelButton;

    /**
     * Store last search
     */
    private transient String lastSearch = null;

    private JButton searchAndExpandButton;

    public SearchTreeDialog() {
        super((JFrame) null, JMeterUtils.getResString("search_tree_title"), true); //$NON-NLS-1$
        init();
    }

    @Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        // Hide Window on ESC
        Action escapeAction = new AbstractAction("ESCAPE") {
            /**
             *
             */
            private static final long serialVersionUID = -6543764044868772971L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        // Do search on Enter
        Action enterAction = new AbstractAction("ENTER") {
            /**
             *
             */
            private static final long serialVersionUID = -3661361497864527363L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                doSearch(actionEvent);
            }
        };
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put(escapeAction.getValue(Action.NAME), escapeAction);
        actionMap.put(enterAction.getValue(Action.NAME), enterAction);
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStrokes.ESC, escapeAction.getValue(Action.NAME));
        inputMap.put(KeyStrokes.ENTER, enterAction.getValue(Action.NAME));

        return rootPane;
    }

    private void init() {
        this.getContentPane().setLayout(new BorderLayout(10,10));

        searchTF = new JLabeledTextField(JMeterUtils.getResString("search_text_field"), 20); //$NON-NLS-1$
        if(!StringUtils.isEmpty(lastSearch)) {
            searchTF.setText(lastSearch);
        }
        isRegexpCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"), false); //$NON-NLS-1$
        isCaseSensitiveCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"), false); //$NON-NLS-1$
        Font font = new Font("SansSerif", Font.PLAIN, 10); // reduce font
        isRegexpCB.setFont(font);
        isCaseSensitiveCB.setFont(font);

        JPanel searchCriterionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchCriterionPanel.add(isCaseSensitiveCB);
        searchCriterionPanel.add(isRegexpCB);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(7, 3, 3, 3));
        searchPanel.add(searchTF, BorderLayout.NORTH);
        searchPanel.add(searchCriterionPanel, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        searchButton = new JButton(JMeterUtils.getResString("search")); //$NON-NLS-1$
        searchButton.addActionListener(this);
        searchAndExpandButton = new JButton(JMeterUtils.getResString("search_expand")); //$NON-NLS-1$
        searchAndExpandButton.addActionListener(this);
        cancelButton = new JButton(JMeterUtils.getResString("cancel")); //$NON-NLS-1$
        cancelButton.addActionListener(this);
        buttonsPanel.add(searchButton);
        buttonsPanel.add(searchAndExpandButton);
        buttonsPanel.add(cancelButton);
        searchPanel.add(buttonsPanel, BorderLayout.SOUTH);
        this.getContentPane().add(searchPanel);
        searchTF.requestFocusInWindow();

        this.pack();
        ComponentUtil.centerComponentInWindow(this);
    }

    /**
     * Do search
     * @param e {@link ActionEvent}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==cancelButton) {
            searchTF.requestFocusInWindow();
            this.setVisible(false);
            return;
        }
        doSearch(e);
    }

    /**
     * @param e {@link ActionEvent}
     */
    private void doSearch(ActionEvent e) {
        boolean expand = e.getSource()==searchAndExpandButton;
        String wordToSearch = searchTF.getText();
        if(StringUtils.isEmpty(wordToSearch)) {
            return;
        } else {
            this.lastSearch = wordToSearch;
        }

        // reset previous result
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SEARCH_RESET));
        // do search
        Searcher searcher = null;
        if(isRegexpCB.isSelected()) {
            searcher = new RegexpSearcher(isCaseSensitiveCB.isSelected(), searchTF.getText());
        } else {
            searcher = new RawTextSearcher(isCaseSensitiveCB.isSelected(), searchTF.getText());
        }
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeModel jMeterTreeModel = guiPackage.getTreeModel();
        Set<JMeterTreeNode> nodes = new HashSet<JMeterTreeNode>();
        for (JMeterTreeNode jMeterTreeNode : jMeterTreeModel.getNodesOfType(Searchable.class)) {
            try {
                if (jMeterTreeNode.getUserObject() instanceof Searchable){
                    Searchable searchable = (Searchable) jMeterTreeNode.getUserObject();
                    List<JMeterTreeNode> matchingNodes = jMeterTreeNode.getPathToThreadGroup();
                    List<String> searchableTokens = searchable.getSearchableTokens();
                    boolean result = searcher.search(searchableTokens);
                    if(result) {
                        nodes.addAll(matchingNodes);
                    }
                }
            } catch (Exception ex) {
                logger.error("Error occured searching for word:"+ wordToSearch, ex);
            }
        }
        GuiPackage guiInstance = GuiPackage.getInstance();
        JTree jTree = guiInstance.getMainFrame().getTree();

        for (Iterator<JMeterTreeNode> iterator = nodes.iterator(); iterator.hasNext();) {
            JMeterTreeNode jMeterTreeNode = iterator.next();
            jMeterTreeNode.setMarkedBySearch(true);
            if (expand) {
                jTree.expandPath(new TreePath(jMeterTreeNode.getPath()));
            }
        }
        GuiPackage.getInstance().getMainFrame().repaint();
        searchTF.requestFocusInWindow();
        this.setVisible(false);
    }
}