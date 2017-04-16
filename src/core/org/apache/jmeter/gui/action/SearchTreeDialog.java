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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.Replaceable;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME Why is searchTF not getting focus correctly after having been setVisible(false) once
 */
public class SearchTreeDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = -4436834972710248247L;

    private static final Logger logger = LoggerFactory.getLogger(SearchTreeDialog.class);

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private JButton searchButton;

    private JLabeledTextField searchTF;

    private JLabeledTextField replaceTF;

    private JLabel statusLabel;

    private JCheckBox isRegexpCB;

    private JCheckBox isCaseSensitiveCB;

    private JButton cancelButton;

    /**
     * Store last search
     */
    private transient String lastSearch = null;

    private JButton searchAndExpandButton;

    private JButton replaceButton;

    public SearchTreeDialog() {
        super((JFrame) null, JMeterUtils.getResString("search_tree_title"), false); //$NON-NLS-1$
        init();
    }

    @Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        // Hide Window on ESC
        Action escapeAction = new AbstractAction("ESCAPE") {

            private static final long serialVersionUID = -6543764044868772971L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        // Do search on Enter
        Action enterAction = new AbstractAction("ENTER") {

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

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.getContentPane().setLayout(new BorderLayout(10,10));

        searchTF = new JLabeledTextField(JMeterUtils.getResString("search_text_field"), 20); //$NON-NLS-1$
        if(!StringUtils.isEmpty(lastSearch)) {
            searchTF.setText(lastSearch);
        }

        replaceTF = new JLabeledTextField(JMeterUtils.getResString("search_text_replace"), 20); //$NON-NLS-1$
        statusLabel = new JLabel(" ");
        statusLabel.setPreferredSize(new Dimension(100, 20));
        statusLabel.setMinimumSize(new Dimension(100, 20));
        isRegexpCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"), false); //$NON-NLS-1$
        isCaseSensitiveCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"), true); //$NON-NLS-1$
        
        isRegexpCB.setFont(FONT_SMALL);
        isCaseSensitiveCB.setFont(FONT_SMALL);

        JPanel searchCriterionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchCriterionPanel.add(isCaseSensitiveCB);
        searchCriterionPanel.add(isRegexpCB);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(7, 3, 3, 3));        
        searchPanel.add(searchTF);
        searchPanel.add(replaceTF);
        searchPanel.add(statusLabel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchButton = new JButton(JMeterUtils.getResString("search")); //$NON-NLS-1$
        searchButton.addActionListener(this);
        searchAndExpandButton = new JButton(JMeterUtils.getResString("search_expand")); //$NON-NLS-1$
        searchAndExpandButton.addActionListener(this);
        replaceButton = new JButton(JMeterUtils.getResString("search_replace_all")); //$NON-NLS-1$
        replaceButton.addActionListener(this);
        cancelButton = new JButton(JMeterUtils.getResString("cancel")); //$NON-NLS-1$
        cancelButton.addActionListener(this);
        buttonsPanel.add(searchButton);
        buttonsPanel.add(searchAndExpandButton);
        buttonsPanel.add(replaceButton);
        buttonsPanel.add(cancelButton);

        JPanel searchAndReplacePanel = new JPanel();
        searchAndReplacePanel.setLayout(new VerticalLayout());
        searchAndReplacePanel.add(searchPanel);
        searchAndReplacePanel.add(searchCriterionPanel);
        searchAndReplacePanel.add(buttonsPanel);
        this.getContentPane().add(searchAndReplacePanel);
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
        statusLabel.setText("");
        if (e.getSource()==cancelButton) {
            searchTF.requestFocusInWindow();
            this.setVisible(false);
            return;
        } else if(e.getSource() == searchButton ||
                e.getSource() == searchAndExpandButton) {
            doSearch(e);
        } else {
            doReplaceAll(e);
        }
    }

    /**
     * @param e {@link ActionEvent}
     */
    private void doSearch(ActionEvent e) {
        boolean expand = e.getSource()==searchAndExpandButton;
        String wordToSearch = searchTF.getText();
        if (StringUtils.isEmpty(wordToSearch)) {
            return;
        } else {
            this.lastSearch = wordToSearch;
        }

        // reset previous result
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SEARCH_RESET));
        // do search
        Searcher searcher = null;
        if (isRegexpCB.isSelected()) {
            searcher = new RegexpSearcher(isCaseSensitiveCB.isSelected(), searchTF.getText());
        } else {
            searcher = new RawTextSearcher(isCaseSensitiveCB.isSelected(), searchTF.getText());
        }
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeModel jMeterTreeModel = guiPackage.getTreeModel();
        Set<JMeterTreeNode> nodes = new HashSet<>();
        int numberOfMatches = 0;
        for (JMeterTreeNode jMeterTreeNode : jMeterTreeModel.getNodesOfType(Searchable.class)) {
            try {
                Searchable searchable = (Searchable) jMeterTreeNode.getUserObject();
                List<JMeterTreeNode> matchingNodes = jMeterTreeNode.getPathToThreadGroup();
                List<String> searchableTokens = searchable.getSearchableTokens();
                boolean result = searcher.search(searchableTokens);
                if (result) {
                    numberOfMatches++;
                    nodes.addAll(matchingNodes);
                }
            } catch (Exception ex) {
                logger.error("Error occurred searching for word:"+ wordToSearch+ " in node:"+jMeterTreeNode.getName(), ex);
            }
        }
        GuiPackage guiInstance = GuiPackage.getInstance();
        JTree jTree = guiInstance.getMainFrame().getTree();

        for (JMeterTreeNode jMeterTreeNode : nodes) {
            jMeterTreeNode.setMarkedBySearch(true);
            if (expand) {
                jTree.expandPath(new TreePath(jMeterTreeNode.getPath()));
            }
        }
        GuiPackage.getInstance().getMainFrame().repaint();
        searchTF.requestFocusInWindow();
        statusLabel.setText(
                MessageFormat.format(
                        JMeterUtils.getResString("search_tree_matches"),new Object[]{numberOfMatches}));
    }
    
    /**
     * Replace all occurrences in nodes that contain {@link Replaceable} Test Elements
     * @param e {@link ActionEvent}
     */
    private void doReplaceAll(ActionEvent e) {
        String wordToSearch = searchTF.getText();
        String wordToReplace = replaceTF.getText();
        
        if (StringUtils.isEmpty(wordToReplace)) {
            return;
        } 
        // Save any change to current node
        GuiPackage.getInstance().updateCurrentNode();
        // reset previous result
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SEARCH_RESET));
        Searcher searcher;
        String regex;
        if (isRegexpCB.isSelected()) {
            regex = wordToSearch;
            searcher = new RegexpSearcher(isCaseSensitiveCB.isSelected(), wordToSearch);
        } else {
            regex = Pattern.quote(wordToSearch);
            searcher = new RawTextSearcher(isCaseSensitiveCB.isSelected(), wordToSearch);
        }
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeModel jMeterTreeModel = guiPackage.getTreeModel();
        Set<JMeterTreeNode> nodes = new HashSet<>();
        boolean caseSensitiveReplacement = isCaseSensitiveCB.isSelected();
        int totalReplaced = 0;
        for (JMeterTreeNode jMeterTreeNode : jMeterTreeModel.getNodesOfType(Searchable.class)) {
            try {
                Searchable searchable = (Searchable) jMeterTreeNode.getUserObject();
                List<String> searchableTokens = searchable.getSearchableTokens();
                boolean result = searcher.search(searchableTokens);
                if (result && jMeterTreeNode.getUserObject() instanceof Replaceable) {
                    Replaceable replaceable = (Replaceable) jMeterTreeNode.getUserObject();
                    int numberOfReplacements = replaceable.replace(regex, wordToReplace, caseSensitiveReplacement);
                    if(logger.isInfoEnabled()) {
                        logger.info("Replaced "+numberOfReplacements+" in element:"
                                +((TestElement)jMeterTreeNode.getUserObject()).getName());
                    }
                    totalReplaced += numberOfReplacements;
                    if(numberOfReplacements > 0) {
                        List<JMeterTreeNode> matchingNodes = jMeterTreeNode.getPathToThreadGroup();
                        nodes.addAll(matchingNodes);
                    }
                }
            } catch (Exception ex) {
                logger.error("Error occurred replacing data in node:"+jMeterTreeNode.getName(), ex);
            }
        }
        statusLabel.setText(MessageFormat.format("Replaced {0} occurrences", new Object[]{totalReplaced}));
        GuiPackage guiInstance = GuiPackage.getInstance();
        JTree jTree = guiInstance.getMainFrame().getTree();

        for (JMeterTreeNode jMeterTreeNode : nodes) {
            jMeterTreeNode.setMarkedBySearch(true);
            jTree.expandPath(new TreePath(jMeterTreeNode.getPath()));
        }
        // Update GUI as current node may be concerned by changes
        if(totalReplaced>0) {
            GuiPackage.getInstance().refreshCurrentGui();
        }
        GuiPackage.getInstance().getMainFrame().repaint();

        searchTF.requestFocusInWindow();
    }
}
