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

package org.apache.jmeter.gui.util;

import java.awt.Font;
import java.awt.HeadlessException;
import java.util.Properties;

import org.apache.jmeter.util.JMeterUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RUndoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle RSyntaxTextArea code
 * It's not currently possible to instantiate the RSyntaxTextArea class when running headless.
 * So we use getInstance methods to create the class and allow for headless testing.
 */
public class JSyntaxTextArea extends RSyntaxTextArea {

    private static final long serialVersionUID = 211L;

    private final Properties languageProperties = JMeterUtils.loadProperties("org/apache/jmeter/gui/util/textarea.properties"); //$NON-NLS-1$

    private final boolean disableUndo;
    private static final boolean WRAP_STYLE_WORD = JMeterUtils.getPropDefault("jsyntaxtextarea.wrapstyleword", true);
    private static final boolean LINE_WRAP       = JMeterUtils.getPropDefault("jsyntaxtextarea.linewrap", true);
    private static final boolean CODE_FOLDING    = JMeterUtils.getPropDefault("jsyntaxtextarea.codefolding", true);
    private static final int MAX_UNDOS           = JMeterUtils.getPropDefault("jsyntaxtextarea.maxundos", 50);
    private static final String USER_FONT_FAMILY = JMeterUtils.getPropDefault("jsyntaxtextarea.font.family", null);
    private static final int USER_FONT_SIZE      = JMeterUtils.getPropDefault("jsyntaxtextarea.font.size", -1);
    private static final Logger log              = LoggerFactory.getLogger(JSyntaxTextArea.class);

    /**
     * Creates the default syntax highlighting text area. The following are set:
     * <ul>
     * <li>setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA)</li>
     * <li>setCodeFoldingEnabled(true)</li>
     * <li>setAntiAliasingEnabled(true)</li>
     * <li>setLineWrap(true)</li>
     * <li>setWrapStyleWord(true)</li>
     * </ul>
     * 
     * @param rows
     *            The number of rows for the text area
     * @param cols
     *            The number of columns for the text area
     * @param disableUndo
     *            true to disable undo manager
     * @return {@link JSyntaxTextArea}
     */
    public static JSyntaxTextArea getInstance(int rows, int cols, boolean disableUndo) {
        try {
            return new JSyntaxTextArea(rows, cols, disableUndo);
        } catch (HeadlessException e) {
            // Allow override for unit testing only
            if ("true".equals(System.getProperty("java.awt.headless"))) { // $NON-NLS-1$ $NON-NLS-2$
                return new JSyntaxTextArea(disableUndo) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    protected void init() {
                        try {
                            super.init();
                        } catch (HeadlessException|NullPointerException e) {
                            // ignored
                        }
                    }
                    // Override methods that would fail
                    @Override
                    public void setCodeFoldingEnabled(boolean b) {  }
                    @Override
                    public void setCaretPosition(int b) { }
                    @Override
                    public void discardAllEdits() { }
                    @Override
                    public void setText(String t) { }
                    @Override
                    public boolean isCodeFoldingEnabled(){ return true; }
                };
            } else {
                throw e;
            }
        }
    }

    /**
     * Creates the default syntax highlighting text area. The following are set:
     * <ul>
     * <li>setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA)</li>
     * <li>setCodeFoldingEnabled(true)</li>
     * <li>setAntiAliasingEnabled(true)</li>
     * <li>setLineWrap(true)</li>
     * <li>setWrapStyleWord(true)</li>
     * </ul>
     * 
     * @param rows
     *            The number of rows for the text area
     * @param cols
     *            The number of columns for the text area
     * @return {@link JSyntaxTextArea}
     */
    public static JSyntaxTextArea getInstance(int rows, int cols) {
        return getInstance(rows, cols, false);
    }

    @Deprecated
    public JSyntaxTextArea() {
        // For use by test code only
        this(30, 50, false);
    }

    // for use by headless tests only
    private JSyntaxTextArea(boolean dummy) {
        disableUndo = dummy;
    }

    /**
     * Creates the default syntax highlighting text area. The following are set:
     * <ul>
     * <li>setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA)</li>
     * <li>setCodeFoldingEnabled(true)</li>
     * <li>setAntiAliasingEnabled(true)</li>
     * <li>setLineWrap(true)</li>
     * <li>setWrapStyleWord(true)</li>
     * </ul>
     * 
     * @param rows
     *            The number of rows for the text area
     * @param cols
     *            The number of columns for the text area
     * @deprecated use {@link #getInstance(int, int)} instead
     */
    @Deprecated
    public JSyntaxTextArea(int rows, int cols) {
        this(rows, cols, false);
    }

    /**
     * Creates the default syntax highlighting text area. The following are set:
     * <ul>
     * <li>setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA)</li>
     * <li>setCodeFoldingEnabled(true)</li>
     * <li>setAntiAliasingEnabled(true)</li>
     * <li>setLineWrap(true)</li>
     * <li>setWrapStyleWord(true)</li>
     * </ul>
     * 
     * @param rows
     *            The number of rows for the text area
     * @param cols
     *            The number of columns for the text area
     * @param disableUndo
     *            true to disable undo manager, defaults to false
     * @deprecated use {@link #getInstance(int, int, boolean)} instead
     */
    @Deprecated
    public JSyntaxTextArea(int rows, int cols, boolean disableUndo) {
        super(rows, cols);
        super.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        super.setCodeFoldingEnabled(CODE_FOLDING);
        super.setAntiAliasingEnabled(true);
        super.setLineWrap(LINE_WRAP);
        super.setWrapStyleWord(WRAP_STYLE_WORD);
        this.disableUndo = disableUndo;
        if (USER_FONT_FAMILY != null) {
            int fontSize = USER_FONT_SIZE > 0 ? USER_FONT_SIZE : getFont().getSize();
            setFont(new Font(USER_FONT_FAMILY, Font.PLAIN, fontSize));
            if (log.isDebugEnabled()) {
                log.debug("Font is set to: {}", getFont());
            }
        }
        if(disableUndo) {
            // We need to do this to force recreation of undoManager which
            // will use the disableUndo otherwise it would always be false
            // See BUG 57440
            discardAllEdits();
        }
    }

    /**
     * Sets the language of the text area.
     * 
     * @param language
     *            The language to be set
     */
    public void setLanguage(String language) {
        if(language == null) {
          // TODO: Log a message?
          // But how to find the name of the offending GUI element in the case of a TestBean?
          super.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        } else {
          final String style = languageProperties.getProperty(language);
          if (style == null) {
              super.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
          } else {
              super.setSyntaxEditingStyle(style);
          }
        }
    }

    /**
     * Override UndoManager to allow disabling if feature causes issues
     * See <a href="https://github.com/bobbylight/RSyntaxTextArea/issues/19">Issue 19 on RSyntaxTextArea</a>
     */
    @Override
    protected RUndoManager createUndoManager() {
        RUndoManager undoManager = super.createUndoManager();
        if(disableUndo) {
            undoManager.setLimit(0);
        } else {
            undoManager.setLimit(MAX_UNDOS);
        }
        return undoManager;
    }

    /**
     * Sets initial text resetting undo history
     * 
     * @param string
     *            The initial text to be set
     */
    public void setInitialText(String string) {
        setText(string);
        discardAllEdits();
    }
}
