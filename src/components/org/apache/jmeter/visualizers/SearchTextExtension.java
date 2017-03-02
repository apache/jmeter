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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.utils.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchTextExtension implements ActionListener, DocumentListener {

    private static final Logger log = LoggerFactory.getLogger(SearchTextExtension.class);

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private static final String SEARCH_TEXT_COMMAND = "search_text"; // $NON-NLS-1$

    private JLabel label;

    private JButton findButton;

    private JTextField textToFindField;

    private JCheckBox caseChkBox;

    private JCheckBox regexpChkBox;

    private JPanel searchPanel;
    
    private String lastTextTofind;
    
    private ISearchTextExtensionProvider searchProvider;

    public void init(JPanel resultsPane) {}

    public void setResults(JEditorPane results) {
        setSearchProvider(new JEditorPaneSearchProvider(results));
    }
    
    public void setSearchProvider(ISearchTextExtensionProvider searchProvider) {
        if (this.searchProvider != null) {
            this.searchProvider.resetTextToFind();
        }
        
        this.searchProvider = searchProvider;
    }

    /**
     * Launch find text engine on response text
     */
    private void executeAndShowTextFind() {
        String textToFind = textToFindField.getText();
        if (this.searchProvider != null) {
            // new search?
            if (lastTextTofind != null && !lastTextTofind.equals(textToFind)) {
                searchProvider.resetTextToFind();
                textToFindField.setBackground(Color.WHITE);
                textToFindField.setForeground(Color.BLACK);
            }
            
            try {
                Pattern pattern = createPattern(textToFindField.getText());
                boolean found = searchProvider.executeAndShowTextFind(pattern);
                if(found) {
                    findButton.setText(JMeterUtils.getResString("search_text_button_next"));// $NON-NLS-1$
                    lastTextTofind = textToFind;
                    textToFindField.setBackground(Color.WHITE);
                    textToFindField.setForeground(Color.BLACK);
                }
                else {
                    findButton.setText(JMeterUtils.getResString("search_text_button_find"));// $NON-NLS-1$
                    textToFindField.setBackground(Colors.LIGHT_RED);
                    textToFindField.setForeground(Color.WHITE);
                }
            } catch (PatternSyntaxException pse) {
                JOptionPane.showMessageDialog(null, 
                        pse.toString(),// $NON-NLS-1$
                        JMeterUtils.getResString("error_title"), // $NON-NLS-1$
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Create the text find task pane
     *
     * @return Text find task pane
     */
    private JPanel createSearchTextPanel() {
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
        findButton.setFont(FONT_SMALL);
        findButton.setActionCommand(SEARCH_TEXT_COMMAND);
        findButton.addActionListener(this);
        searchPanel.add(findButton);

        // checkboxes
        caseChkBox = new JCheckBox(JMeterUtils
                .getResString("search_text_chkbox_case"), false); // $NON-NLS-1$
        caseChkBox.setFont(FONT_SMALL);
        searchPanel.add(caseChkBox);
        regexpChkBox = new JCheckBox(JMeterUtils
                .getResString("search_text_chkbox_regexp"), false); // $NON-NLS-1$
        regexpChkBox.setFont(FONT_SMALL);
        searchPanel.add(regexpChkBox);

        // when Enter is pressed, search start
        InputMap im = textToFindField
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStrokes.ENTER, SEARCH_TEXT_COMMAND);
        ActionMap am = textToFindField.getActionMap();
        am.put(SEARCH_TEXT_COMMAND, new EnterAction());

        // default not visible
        searchPanel.setVisible(true);
        return searchPanel;
    }

    public JPanel createSearchTextExtensionPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(createSearchTextPanel());
        return pane;
    }

    /**
     * Display the response as text or as rendered HTML. Change the text on the
     * button appropriate to the current display.
     *
     * @param e the ActionEvent being processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        // Search text in response data
        if (SEARCH_TEXT_COMMAND.equals(command)) {
            executeAndShowTextFind();
        }
    }

    private class EnterAction extends AbstractAction {
        private static final long serialVersionUID = 2L;
        @Override
        public void actionPerformed(ActionEvent ev) {
            executeAndShowTextFind();
        }
    }

    // DocumentListener method
    @Override
    public void changedUpdate(DocumentEvent e) {
        // do nothing
    }

    // DocumentListener method
    @Override
    public void insertUpdate(DocumentEvent e) {
        resetTextToFind();
    }

    // DocumentListener method
    @Override
    public void removeUpdate(DocumentEvent e) {
        resetTextToFind();
    }

    public void resetTextToFind() {
        if (this.searchProvider != null) {
            searchProvider.resetTextToFind();
        }
        lastTextTofind = null;
        findButton.setText(JMeterUtils.getResString("search_text_button_find"));// $NON-NLS-1$
    }

    private Pattern createPattern(String textToFind) {
        // desactivate or not specials regexp char
        String textToFindQ = regexpChkBox.isSelected() ? textToFind : Pattern.quote(textToFind);        
        return caseChkBox.isSelected() ? Pattern.compile(textToFindQ) :
            Pattern.compile(textToFindQ, Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Search provider definition
     * Allow the search extension to search on any component
     */
    public interface ISearchTextExtensionProvider {
        
        /**
         * reset the provider
         */
        void resetTextToFind();
        
        /**
         * Launch find text engine on target component
         * @param pattern text pattern to search
         * @return true if there was a match, false otherwise
         */
        boolean executeAndShowTextFind(Pattern pattern);
    }
    
    /**
     * JEditorPane search provider
     * Should probably be moved in its on file
     */
    private static class JEditorPaneSearchProvider implements ISearchTextExtensionProvider {

        private static volatile int LAST_POSITION_DEFAULT = 0;
        private static final Color HILIT_COLOR = Color.LIGHT_GRAY;
        private JEditorPane results;
        private Highlighter selection;
        private Highlighter.HighlightPainter painter;
        private int lastPosition = LAST_POSITION_DEFAULT;
        
        public JEditorPaneSearchProvider(JEditorPane results) {
            this.results = results;
            
            // prepare highlighter to show text find with search command
            selection = new DefaultHighlighter();
            painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);
            results.setHighlighter(selection);
        }

        @Override
        public void resetTextToFind() {
            // Reset search
            lastPosition = LAST_POSITION_DEFAULT;
            selection.removeAllHighlights();
            results.setCaretPosition(0);
        }

        @Override
        public boolean executeAndShowTextFind(Pattern pattern) {
            boolean found = false;
            if (results != null && results.getText().length() > 0
                    && pattern != null) {

                log.debug("lastPosition={}", lastPosition);

                Matcher matcher = null;
                try {
                    Document contentDoc = results.getDocument();
                    String body = contentDoc.getText(lastPosition, contentDoc.getLength() - lastPosition);
                    matcher = pattern.matcher(body);

                    if ((matcher != null) && (matcher.find())) {
                        selection.removeAllHighlights();
                        selection.addHighlight(lastPosition + matcher.start(),
                                lastPosition + matcher.end(), painter);
                        results.setCaretPosition(lastPosition + matcher.end());

                        // save search position
                        lastPosition = lastPosition + matcher.end();
                        found = true;
                    }
                    else {
                        // reset search
                        lastPosition = LAST_POSITION_DEFAULT;
                        results.setCaretPosition(0);
                    }
                } catch (BadLocationException ble) {
                    log.error("Location exception in text find", ble);// $NON-NLS-1$
                }
            }
            
            return found;
        }
        
    }
}
