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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.Replaceable;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog to search in tree of element
 */
public class SearchTreeDialog extends JDialog implements ActionListener { // NOSONAR

    private static final long serialVersionUID = -4436834972710248247L;

    private static final Logger logger = LoggerFactory.getLogger(SearchTreeDialog.class);

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private JButton searchButton;

    private JButton nextButton;
    
    private JButton previousButton;
    
    private JButton searchAndExpandButton;
    
    private JButton replaceButton;
    
    private JButton replaceAllButton;
    
    private JButton replaceAndFindButton;

    private JButton cancelButton;

    private JLabeledTextField searchTF;

    private JLabeledTextField replaceTF;

    private JLabel statusLabel;

    private JCheckBox isRegexpCB;

    private JCheckBox isCaseSensitiveCB;


    private transient Triple<String, Boolean, Boolean> lastSearchConditions = null;

    private List<JMeterTreeNode> lastSearchResult = new ArrayList<>();
    private int currentSearchIndex;

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
        searchTF.setAlignmentY(TOP_ALIGNMENT);
        if (lastSearchConditions != null) {
            searchTF.setText(lastSearchConditions.getLeft());
            isCaseSensitiveCB.setSelected(lastSearchConditions.getMiddle());
            isRegexpCB.setSelected(lastSearchConditions.getRight());
        }

        replaceTF = new JLabeledTextField(JMeterUtils.getResString("search_text_replace"), 20); //$NON-NLS-1$
        replaceTF.setAlignmentX(TOP_ALIGNMENT);
        statusLabel = new JLabel(" ");
        statusLabel.setPreferredSize(new Dimension(100, 20));
        statusLabel.setMinimumSize(new Dimension(100, 20));
        isRegexpCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"), false); //$NON-NLS-1$
        isCaseSensitiveCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"), true); //$NON-NLS-1$
        
        isRegexpCB.setFont(FONT_SMALL);
        isCaseSensitiveCB.setFont(FONT_SMALL);

        JPanel searchCriterionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchCriterionPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("search_matching"))); //$NON-NLS-1$
        searchCriterionPanel.add(isCaseSensitiveCB);
        searchCriterionPanel.add(isRegexpCB);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new GridLayout(4, 1));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(7, 3, 3, 3));
        searchPanel.add(searchTF);
        searchPanel.add(replaceTF);
        searchPanel.add(statusLabel);
        searchPanel.add(searchCriterionPanel);

        JPanel buttonsPanel = new JPanel(new GridLayout(9, 1));
        searchButton = createButton("search_search_all"); //$NON-NLS-1$
        searchButton.addActionListener(this);
        nextButton = createButton("search_next"); //$NON-NLS-1$
        nextButton.addActionListener(this);
        previousButton = createButton("search_previous"); //$NON-NLS-1$
        previousButton.addActionListener(this);
        searchAndExpandButton = createButton("search_search_all_expand"); //$NON-NLS-1$
        searchAndExpandButton.addActionListener(this);
        replaceButton = createButton("search_replace"); //$NON-NLS-1$
        replaceButton.addActionListener(this);
        replaceAllButton = createButton("search_replace_all"); //$NON-NLS-1$
        replaceAllButton.addActionListener(this);
        replaceAndFindButton = createButton("search_replace_and_find"); //$NON-NLS-1$
        replaceAndFindButton.addActionListener(this);
        cancelButton = createButton("cancel"); //$NON-NLS-1$
        cancelButton.addActionListener(this);
        buttonsPanel.add(nextButton);
        buttonsPanel.add(previousButton);
        buttonsPanel.add(searchButton);
        buttonsPanel.add(searchAndExpandButton);
        buttonsPanel.add(Box.createVerticalStrut(30));
        buttonsPanel.add(replaceButton);
        buttonsPanel.add(replaceAllButton);
        buttonsPanel.add(replaceAndFindButton);
        buttonsPanel.add(cancelButton);

        JPanel searchAndReplacePanel = new JPanel();
        searchAndReplacePanel.setLayout(new BorderLayout());
        searchAndReplacePanel.add(searchPanel, BorderLayout.CENTER);
        searchAndReplacePanel.add(buttonsPanel, BorderLayout.EAST);
        this.getContentPane().add(searchAndReplacePanel);
        searchTF.requestFocusInWindow();

        this.pack();
        ComponentUtil.centerComponentInWindow(this);
    }

    private JButton createButton(String messageKey) {
        return new JButton(JMeterUtils.getResString(messageKey));
    }

    /**
     * Do search
     * @param e {@link ActionEvent}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        statusLabel.setText("");
        if (source == cancelButton) {
            searchTF.requestFocusInWindow();
            this.setVisible(false);
        } else if (source == searchButton
                || source == searchAndExpandButton) {
            doSearch(e);
        } else if (source == nextButton ||
                source == previousButton) {
            doNavigateToSearchResult(source == nextButton);
        } else if (source == replaceAllButton){
            doReplaceAll(e);
        } else if (!lastSearchResult.isEmpty() && source == replaceButton){
            doReplace();
        } else if (source == replaceAndFindButton){
            if(!lastSearchResult.isEmpty()) {
                doReplace();
            }
            doNavigateToSearchResult(true);
        }
    }

    /**
     * 
     */
    private void doReplace() {
        GuiPackage.getInstance().updateCurrentNode();
        JMeterTreeNode currentNode = lastSearchResult.get(currentSearchIndex);
        if(currentNode != null) {
            String wordToSearch = searchTF.getText();
            String wordToReplace = replaceTF.getText();
            String regex = isRegexpCB.isSelected() ? wordToSearch : Pattern.quote(wordToSearch);
            boolean caseSensitiveReplacement = isCaseSensitiveCB.isSelected();
            Pair<Integer, JMeterTreeNode> pair = doReplacementInCurrentNode(currentNode, regex, wordToReplace, caseSensitiveReplacement);
            int nbReplacements = 0;
            if(pair != null) {
                nbReplacements = pair.getLeft();
                GuiPackage.getInstance().updateCurrentGui();
                GuiPackage.getInstance().getMainFrame().repaint();
            } 
            statusLabel.setText(MessageFormat.format("Replaced {0} occurrences", nbReplacements));
        }
    }

    private JMeterTreeNode doNavigateToSearchResult(boolean isNext) {
        boolean doSearchAgain = 
                lastSearchConditions == null || 
                !Triple.of(searchTF.getText(), isCaseSensitiveCB.isSelected(), isRegexpCB.isSelected())
                .equals(lastSearchConditions);
        if(doSearchAgain) {
            String wordToSearch = searchTF.getText();
            if (StringUtils.isEmpty(wordToSearch)) {
                this.lastSearchConditions = null;
                return null;
            } else {
                this.lastSearchConditions = Triple.of(wordToSearch, isCaseSensitiveCB.isSelected(), isRegexpCB.isSelected());
            }
            Searcher searcher = createSearcher(wordToSearch);
            searchInTree(GuiPackage.getInstance(), searcher, wordToSearch);
        }
        if(!lastSearchResult.isEmpty()) {
            if(isNext) {
                currentSearchIndex = ++currentSearchIndex % lastSearchResult.size();
            } else {
                currentSearchIndex = currentSearchIndex > 0 ? --currentSearchIndex : lastSearchResult.size()-1;
            }
            JMeterTreeNode selectedNode = lastSearchResult.get(currentSearchIndex);
            TreePath selection = new TreePath(selectedNode.getPath());
            GuiPackage.getInstance().getMainFrame().getTree().setSelectionPath(selection);
            GuiPackage.getInstance().getMainFrame().getTree().scrollPathToVisible(selection);
            return selectedNode;
        }
        return null;
    }

    /**
     * @param e {@link ActionEvent}
     */
    private void doSearch(ActionEvent e) {
        boolean expand = e.getSource()==searchAndExpandButton;
        String wordToSearch = searchTF.getText();
        if (StringUtils.isEmpty(wordToSearch)) {
            this.lastSearchConditions = null;
            return;
        } else {
            this.lastSearchConditions = Triple.of(wordToSearch, isCaseSensitiveCB.isSelected(), isRegexpCB.isSelected());
        }

        // reset previous result
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SEARCH_RESET));
        // do search
        Searcher searcher = createSearcher(wordToSearch);
        GuiPackage guiPackage = GuiPackage.getInstance();
        guiPackage.beginUndoTransaction();
        int numberOfMatches = 0;
        try {
            Pair<Integer, Set<JMeterTreeNode>> result = searchInTree(guiPackage, searcher, wordToSearch);
            numberOfMatches = result.getLeft();
            markConcernedNodes(expand, result.getRight());
        } finally {
            guiPackage.endUndoTransaction();
        }
        GuiPackage.getInstance().getMainFrame().repaint();
        searchTF.requestFocusInWindow();
        statusLabel.setText(
                MessageFormat.format(
                        JMeterUtils.getResString("search_tree_matches"), numberOfMatches));
    }

    /**
     * @param wordToSearch
     * @return
     */
    private Searcher createSearcher(String wordToSearch) {
        if (isRegexpCB.isSelected()) {
            return new RegexpSearcher(isCaseSensitiveCB.isSelected(), wordToSearch);
        } else {
            return new RawTextSearcher(isCaseSensitiveCB.isSelected(), wordToSearch);
        }
    }
    
    private Pair<Integer, Set<JMeterTreeNode>> searchInTree(GuiPackage guiPackage, Searcher searcher, String wordToSearch) {
        int numberOfMatches = 0;
        JMeterTreeModel jMeterTreeModel = guiPackage.getTreeModel();
        Set<JMeterTreeNode> nodes = new LinkedHashSet<>();
        for (JMeterTreeNode jMeterTreeNode : jMeterTreeModel.getNodesOfType(Searchable.class)) {
            try {
                Searchable searchable = (Searchable) jMeterTreeNode.getUserObject();
                List<String> searchableTokens = searchable.getSearchableTokens();
                boolean result = searcher.search(searchableTokens);
                if (result) {
                    numberOfMatches++;
                    nodes.add(jMeterTreeNode);
                }
            } catch (Exception ex) {
                logger.error("Error occurred searching for word:{} in node:{}", wordToSearch, jMeterTreeNode.getName(), ex);
            }
        }
        this.currentSearchIndex = -1;
        this.lastSearchResult.clear();
        this.lastSearchResult.addAll(nodes);
        return Pair.of(numberOfMatches, nodes);
    }

    /**
     * @param expand true if we want to expand
     * @param nodes Set of {@link JMeterTreeNode} to mark
     */
    private void markConcernedNodes(boolean expand, Set<JMeterTreeNode> nodes) {
        GuiPackage guiInstance = GuiPackage.getInstance();
        JTree jTree = guiInstance.getMainFrame().getTree();
        for (JMeterTreeNode jMeterTreeNode : nodes) {
            jMeterTreeNode.setMarkedBySearch(true);
            if (expand) {
                if(jMeterTreeNode.isLeaf()) {
                    jTree.expandPath(new TreePath(((JMeterTreeNode)jMeterTreeNode.getParent()).getPath()));
                } else {
                    jTree.expandPath(new TreePath(jMeterTreeNode.getPath()));
                }
            }
        }
    }
    
    /**
     * Replace all occurrences in nodes that contain {@link Replaceable} Test Elements
     * @param e {@link ActionEvent}
     */
    private void doReplaceAll(ActionEvent e) {
        boolean expand = e.getSource()==searchAndExpandButton;
        String wordToSearch = searchTF.getText();
        String wordToReplace = replaceTF.getText();
        if (StringUtils.isEmpty(wordToReplace)) {
            return;
        }
        // Save any change to current node
        GuiPackage.getInstance().updateCurrentNode();
        // reset previous result
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SEARCH_RESET));
        Searcher searcher = createSearcher(wordToSearch);
        String regex = isRegexpCB.isSelected() ? wordToSearch : Pattern.quote(wordToSearch);
        GuiPackage guiPackage = GuiPackage.getInstance();
        boolean caseSensitiveReplacement = isCaseSensitiveCB.isSelected();
        int totalReplaced = 0;
        Pair<Integer, Set<JMeterTreeNode>> result = searchInTree(guiPackage, searcher, wordToSearch);
        Set<JMeterTreeNode> matchingNodes = result.getRight();
        Set<JMeterTreeNode> replacedNodes = new HashSet<>();
        for (JMeterTreeNode jMeterTreeNode : matchingNodes) {
            Pair<Integer, JMeterTreeNode> pair = doReplacementInCurrentNode(jMeterTreeNode, regex, wordToReplace, caseSensitiveReplacement);
            if(pair != null) {
                totalReplaced += pair.getLeft();
                replacedNodes.add(pair.getRight());
            }
        }
        statusLabel.setText(MessageFormat.format("Replaced {0} occurrences", totalReplaced));
        markConcernedNodes(expand, replacedNodes);
        // Update GUI as current node may be concerned by changes
        if (totalReplaced > 0) {
            GuiPackage.getInstance().refreshCurrentGui();
        }
        GuiPackage.getInstance().getMainFrame().repaint();

        searchTF.requestFocusInWindow();
    }

    /**
     * Replace in jMeterTreeNode regex by replaceBy
     * @param jMeterTreeNode Current {@link JMeterTreeNode}
     * @param regex Text to search (can be regex)
     * @param replaceBy Replacement text
     * @param caseSensitiveReplacement boolean if search is case sensitive
     * @return null if no replacement occurred or Pair of (number of replacement, current tree node)
     */
    private Pair<Integer, JMeterTreeNode> doReplacementInCurrentNode(JMeterTreeNode jMeterTreeNode,
            String regex, String replaceBy, boolean caseSensitiveReplacement) {
        try {
            if (jMeterTreeNode.getUserObject() instanceof Replaceable) {
                Replaceable replaceable = (Replaceable) jMeterTreeNode.getUserObject();
                int numberOfReplacements = replaceable.replace(regex, replaceBy, caseSensitiveReplacement);
                if (numberOfReplacements > 0) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Replaced {} in element:{}", numberOfReplacements,
                                ((TestElement) jMeterTreeNode.getUserObject()).getName());
                    }
                    return Pair.of(numberOfReplacements, jMeterTreeNode);
                }
            }
        } catch (Exception ex) {
            logger.error("Error occurred replacing data in node:{}", jMeterTreeNode.getName(), ex);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Dialog#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        searchTF.requestFocusInWindow();
    }
}
