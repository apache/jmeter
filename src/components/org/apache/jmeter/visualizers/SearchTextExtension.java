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

package org.apache.jmeter.visualizers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class SearchTextExtension implements ActionListener, DocumentListener {
    
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    public static final String SEARCH_TEXT_COMMAND = "search_text"; // $NON-NLS-1$
    
    public static final String DIALOG_SEARCH_SHOW_COMMAND = "show_search_dialog"; // $NON-NLS-1$
    
    public static final String DIALOG_SEARCH_HIDE_COMMAND = "close_search_dialog"; // $NON-NLS-1$

    private static final String SEARCH_SHOW_COMMAND = "show_search_text"; // $NON-NLS-1$

    // set default command to Text
    private String command = DIALOG_SEARCH_SHOW_COMMAND;
    
    private static int LAST_POSITION_DEFAULT = 0;

    private int lastPosition = LAST_POSITION_DEFAULT;
    
    private final static Color HILIT_COLOR = Color.LIGHT_GRAY;
    
    private Highlighter selection;
    
    private Highlighter.HighlightPainter painter;
    
    private JLabel label;

    private JButton findButton;

    private JTextField textToFindField;

    private  JCheckBox caseChkBox;

    private JCheckBox regexpChkBox;

    private String lastTextTofind;

    private boolean newSearch = false;
    
    private JEditorPane results;
    
    private JPanel searchPanel;

    private boolean enabled = true;
    
    private JCheckBox showSearch;

    
    public void init(JPanel resultsPane) {
        // when CTRL-T is pressed, (un-)show search dialog box (only in results pane)
        InputMap im = resultsPane
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 
                InputEvent.CTRL_MASK), SEARCH_SHOW_COMMAND);
        ActionMap am = resultsPane.getActionMap();
        am.put(SEARCH_SHOW_COMMAND, new DisplaySearchAction());
    }
    
    public void setResults(JEditorPane results) {
        if (this.results != null) {
            newSearch = true;
            resetTextToFind();
        }
        this.results = results;
        // prepare highlighter to show text find with search command
        selection = new DefaultHighlighter();
        painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);
        results.setHighlighter(selection);
    }

    /**
     * Launch find text engine on response text
     */
    public void executeAndShowTextFind() {
        if (results != null && results.getText().length() > 0
                && this.textToFindField.getText().length() > 0) {
            String textToFind = textToFindField.getText();

            // new search?
            if (lastTextTofind != null && !lastTextTofind.equals(textToFind)) {
                lastPosition = LAST_POSITION_DEFAULT;
            }

            log.debug("lastPosition=" + lastPosition);
            Matcher matcher = null;
            try {
                Pattern pattern = createPattern(textToFind);
                Document contentDoc = results.getDocument();
                String body = contentDoc.getText(lastPosition,
                        (contentDoc.getLength() - lastPosition));
                matcher = pattern.matcher(body);

                if ((matcher != null) && (matcher.find())) {
                    selection.removeAllHighlights();
                    selection.addHighlight(lastPosition + matcher.start(),
                            lastPosition + matcher.end(), painter);
                    results.setCaretPosition(lastPosition + matcher.end());                    

                    // save search position
                    lastPosition = lastPosition + matcher.end();
                    findButton.setText(JMeterUtils
                            .getResString("search_text_button_next"));// $NON-NLS-1$
                    lastTextTofind = textToFind;
                    newSearch = true;
                } else {
                    // Display not found message and reset search
                    JOptionPane.showMessageDialog(null, JMeterUtils
                            .getResString("search_text_msg_not_found"),// $NON-NLS-1$
                            JMeterUtils.getResString("search_text_title_not_found"), // $NON-NLS-1$
                            JOptionPane.INFORMATION_MESSAGE);
                    lastPosition = LAST_POSITION_DEFAULT;
                    findButton.setText(JMeterUtils
                            .getResString("search_text_button_find"));// $NON-NLS-1$
                    results.setCaretPosition(0);
                }
            } catch (BadLocationException ble) {
                log.error("Location exception in text find", ble);// $NON-NLS-1$
            }
        }
    }

    /**
     * Create the text find task pane
     * 
     * @return Text find task pane
     */
    public JPanel createSearchTextPanel() {
        Font font = new Font("SansSerif", Font.PLAIN, 10);
        
        // Search field
        searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        label = new JLabel(JMeterUtils.getResString("search_text_field")); // $NON-NLS-1$
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        searchPanel.add(label);
        textToFindField = new JTextField(); // $NON-NLS-1$
        searchPanel.add(textToFindField);
        searchPanel.add(Box.createRigidArea(new Dimension(5,0)));

        // add listener to intercept texttofind changes and reset search
        textToFindField.getDocument().addDocumentListener(this);

        // Buttons
        findButton = new JButton(JMeterUtils
                .getResString("search_text_button_find")); // $NON-NLS-1$
        findButton.setFont(font);
        findButton.setActionCommand(SEARCH_TEXT_COMMAND);
        findButton.addActionListener(this);
        searchPanel.add(findButton);

        // checkboxes
        caseChkBox = new JCheckBox(JMeterUtils
                .getResString("search_text_chkbox_case"), false); // $NON-NLS-1$
        caseChkBox.setFont(font);
        searchPanel.add(caseChkBox);
        regexpChkBox = new JCheckBox(JMeterUtils
                .getResString("search_text_chkbox_regexp"), false); // $NON-NLS-1$
        regexpChkBox.setFont(font);
        searchPanel.add(regexpChkBox);

        // when Enter is pressed, search start
        InputMap im = textToFindField
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("ENTER"), SEARCH_TEXT_COMMAND);
        ActionMap am = textToFindField.getActionMap();
        am.put(SEARCH_TEXT_COMMAND, new EnterAction());
        
        return searchPanel;
    }
    
    public JPanel createSearchTextExtensionPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(createShowSearchPanel());
        pane.add(createSearchTextPanel());
        return pane;
    }
    
    public JPanel createShowSearchPanel() {
        Font fontBold = new Font("SansSerif", Font.BOLD, 10);

        showSearch = new JCheckBox();
        showSearch.setFont(fontBold);
        showSearch.setAction(new DisplaySearchAction());
        showSearch.setText(JMeterUtils
                .getResString("view_results_search_pane")); // $NON-NLS-1$
        
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(showSearch);
        
        return pane;
    }


    /**
     * Display the response as text or as rendered HTML. Change the text on the
     * button appropriate to the current display.
     *
     * @param e
     *            the ActionEvent being processed
     */
    public void actionPerformed(ActionEvent e) {
        command = e.getActionCommand();

        // Search text in response data
        if (command != null 
                && (command.equals(SearchTextExtension.SEARCH_TEXT_COMMAND))) {
            executeAndShowTextFind();
        }
        // hide search dialog box
        else if (command != null && command.equals(
                SearchTextExtension.DIALOG_SEARCH_HIDE_COMMAND)) {
            hideSearchBox();
        }
    }

    private class EnterAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent ev) {
            executeAndShowTextFind();
        }
    }

    private class DisplaySearchAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent ev) {
            if (searchPanel != null) {
                resetTextToFind();
                if (searchPanel.isVisible()) {
                    searchPanel.setVisible(false);
                    showSearch.setSelected(false);
                } else {
                    searchPanel.setVisible(true);
                    showSearch.setSelected(true);
                }
            }
        }
    }
    
    // DocumentListener method
    public void changedUpdate(DocumentEvent e) {
        // do nothing
    }

    // DocumentListener method
    public void insertUpdate(DocumentEvent e) {
        resetTextToFind();
    }

    // DocumentListener method
    public void removeUpdate(DocumentEvent e) {
        resetTextToFind();
    }

    public void hideSearchBox() {
        searchPanel.setVisible(false);
    }
    
    public void resetTextToFind() {
        if (newSearch) {
            log.debug("reset pass");
            // Reset search
            lastPosition = LAST_POSITION_DEFAULT;
            lastTextTofind = null;
            findButton.setText(JMeterUtils
                    .getResString("search_text_button_find"));// $NON-NLS-1$
            selection.removeAllHighlights();
            results.setCaretPosition(0);
            newSearch = false;
        }
    }
    
    public boolean isEnabled() {
        return enabled ;
    }
    
    public void setEnabled(boolean b) {
        this.enabled = b;
        label.setEnabled(b);
        textToFindField.setEnabled(b);
        findButton.setEnabled(b);
        caseChkBox.setEnabled(b);
        regexpChkBox.setEnabled(b);
    }
    
    private Pattern createPattern(String textToFind) {
        // desactivate or not specials regexp char 
        String textToFindQ = Pattern.quote(textToFind);
        if (regexpChkBox.isSelected()) {
            textToFindQ = textToFind;
        }
        Pattern pattern = null;
        if (caseChkBox.isSelected()) {
            pattern = Pattern.compile(textToFindQ);
        } else {
            pattern = Pattern.compile(textToFindQ, Pattern.CASE_INSENSITIVE);
        }
        return pattern;
    }
}
