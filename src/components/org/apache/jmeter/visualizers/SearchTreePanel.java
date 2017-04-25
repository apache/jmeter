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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.gui.action.RawTextSearcher;
import org.apache.jmeter.gui.action.RegexpSearcher;
import org.apache.jmeter.gui.action.Searcher;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.utils.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel used by {@link ViewResultsFullVisualizer} to search for data within the Tree
 * @since 3.0
 */
public class SearchTreePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(SearchTreePanel.class);

    private static final String SEARCH_TEXT_COMMAND = "search_text"; // $NON-NLS-1$

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private JButton searchButton;

    private JTextField searchTF;

    private JCheckBox isRegexpCB;

    private JCheckBox isCaseSensitiveCB;

    private JButton resetButton;

    private DefaultMutableTreeNode defaultMutableTreeNode;

    public SearchTreePanel(DefaultMutableTreeNode defaultMutableTreeNode) {
        super(); 
        init();
        this.defaultMutableTreeNode = defaultMutableTreeNode;
    }

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public SearchTreePanel(){
//        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }

    private class EnterAction extends AbstractAction {
        private static final long serialVersionUID = 2L;
        @Override
        public void actionPerformed(ActionEvent ev) {
            boolean found = doSearch();
            if(found) {
                searchTF.setBackground(Color.WHITE);
                searchTF.setForeground(Color.BLACK);
            }
            else {
                searchTF.setBackground(Colors.LIGHT_RED);
                searchTF.setForeground(Color.WHITE);
            }
        }
    }
    
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(10,10));
        
        searchTF = new JTextField(20); //$NON-NLS-1$
        InputMap im = searchTF
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStrokes.ENTER, SEARCH_TEXT_COMMAND);
        ActionMap am = searchTF.getActionMap();
        am.put(SEARCH_TEXT_COMMAND, new EnterAction());

        isRegexpCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"), false); //$NON-NLS-1$
        isCaseSensitiveCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"), false); //$NON-NLS-1$
        
        isRegexpCB.setFont(FONT_SMALL);
        isCaseSensitiveCB.setFont(FONT_SMALL);

        searchButton = new JButton(JMeterUtils.getResString("search")); //$NON-NLS-1$
        searchButton.addActionListener(this);
        resetButton = new JButton(JMeterUtils.getResString("reset")); //$NON-NLS-1$
        resetButton.addActionListener(this);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        searchPanel.add(new JLabel(JMeterUtils.getResString("search_text_field")));
        searchPanel.add(searchTF);
        searchPanel.add(isCaseSensitiveCB);
        searchPanel.add(isRegexpCB);        
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);
        add(searchPanel);
    }

    /**
     * Do search
     * @param e {@link ActionEvent}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == searchButton) {
            doSearch();
        } else if (e.getSource() == resetButton) {
            doResetSearch((SearchableTreeNode)defaultMutableTreeNode);
        }
    }

    /**
     * @param searchableTreeNode
     */
    private void doResetSearch(SearchableTreeNode searchableTreeNode) {
        searchableTreeNode.reset();
        searchableTreeNode.updateState();
        for (int i = 0; i < searchableTreeNode.getChildCount(); i++) {
            doResetSearch((SearchableTreeNode)searchableTreeNode.getChildAt(i));
        }
    }


    /**
     * return true if a match occurred
     */
    private boolean doSearch() {
        String wordToSearch = searchTF.getText();
        if (StringUtils.isEmpty(wordToSearch)) {
            return false;
        }
        Searcher searcher = isRegexpCB.isSelected() ?
            new RegexpSearcher(isCaseSensitiveCB.isSelected(), searchTF.getText()) : 
            new RawTextSearcher(isCaseSensitiveCB.isSelected(), searchTF.getText());        
        return searchInNode(searcher, (SearchableTreeNode)defaultMutableTreeNode);
    }

    /**
     * @param searcher
     * @param node
     */
    private boolean searchInNode(Searcher searcher, SearchableTreeNode node) {
        node.reset();
        Object userObject = node.getUserObject();
        
        try {
            Searchable searchable;
            if(userObject instanceof Searchable) {
                searchable = (Searchable) userObject;
            } else {
                return false;
            }
            if(searcher.search(searchable.getSearchableTokens())) {
                node.setNodeHasMatched(true);
            }
            boolean foundInChildren = false;
            for (int i = 0; i < node.getChildCount(); i++) {
                searchInNode(searcher, (SearchableTreeNode)node.getChildAt(i));
                foundInChildren =  
                        searchInNode(searcher, (SearchableTreeNode)node.getChildAt(i))
                        || foundInChildren; // Must be the last in condition
            }
            if(!node.isNodeHasMatched()) {
                node.setChildrenNodesHaveMatched(foundInChildren);
            }
            node.updateState();
            return node.isNodeHasMatched() || node.isChildrenNodesHaveMatched();
        } catch (Exception e) {
            log.error("Error extracting data from tree node");
            return false;
        }
    }
}
