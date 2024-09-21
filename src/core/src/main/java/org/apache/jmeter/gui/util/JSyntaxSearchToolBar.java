/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.util;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JFactory;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

/**
 * Search toolbar associated to {@link JSyntaxTextArea}
 * @since 5.0
 */
public final class JSyntaxSearchToolBar implements ActionListener {
    public static final Color LIGHT_RED = new Color(0xFF, 0x80, 0x80);

    public static final String FIND_ACTION = "Find";

    private JToolBar toolBar;

    private JTextField searchField;

    private JCheckBox regexCB;

    private JCheckBox matchCaseCB;

    /**
     * The component where we Search
     */
    private final JSyntaxTextArea dataField;

    /**
     * @param dataField {@link JSyntaxTextArea} to use for searching
     */
    public JSyntaxSearchToolBar(JSyntaxTextArea dataField) {
        this.dataField = dataField;
        init();
    }

    private void init() {
        this.searchField = new JTextField(30);
        JFactory.small(searchField);
        final JButton findButton = new JButton(JMeterUtils.getResString("search_text_button_find"));
        JFactory.small(findButton);
        findButton.setActionCommand(FIND_ACTION);
        findButton.addActionListener(this);
        regexCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"));
        JFactory.small(regexCB);

        matchCaseCB = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"));
        JFactory.small(matchCaseCB);

        this.toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JFactory.small(toolBar);
        toolBar.add(searchField);
        toolBar.add(findButton);
        toolBar.add(matchCaseCB);
        toolBar.add(regexCB);
        searchField.addActionListener(e -> findButton.doClick(0));
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String text = searchField.getText();
        toggleSearchField(searchField, true);

        if (!text.isEmpty()) {
            SearchContext context = createSearchContext(
                    text, true, matchCaseCB.isSelected(), regexCB.isSelected());
            boolean found = SearchEngine.find(dataField, context).wasFound();
            toggleSearchField(searchField, found);
            if(!found) {
                dataField.setCaretPosition(0);
            }
        }
    }

    void toggleSearchField(JTextField textToFindField, boolean matchFound) {
        if(!matchFound) {
            textToFindField.setBackground(LIGHT_RED);
            textToFindField.setForeground(Color.WHITE);
        } else {
            textToFindField.setBackground(Color.WHITE);
            textToFindField.setForeground(Color.BLACK);
        }
    }

    private static SearchContext createSearchContext(String text, boolean forward, boolean matchCase,
            boolean isRegex) {
        SearchContext context = new SearchContext();
        context.setSearchFor(text);
        context.setMatchCase(matchCase);
        context.setRegularExpression(isRegex);
        context.setSearchForward(forward);
        context.setMarkAll(false);
        context.setSearchSelectionOnly(false);
        context.setWholeWord(false);
        return context;
    }
}
