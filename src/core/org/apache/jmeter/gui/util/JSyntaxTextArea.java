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

import java.util.Properties;

import org.apache.jmeter.util.JMeterUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RUndoManager;

/**
 * Utility class to handle RSyntaxTextArea code
 */
public class JSyntaxTextArea extends RSyntaxTextArea {

    private static final long serialVersionUID = 210L;

    private final Properties languageProperties = JMeterUtils.loadProperties("org/apache/jmeter/gui/util/textarea.properties"); //$NON-NLS-1$;

    private final boolean disableUndo;

    private static final boolean WRAP_STYLE_WORD = JMeterUtils.getPropDefault("jsyntaxtextarea.wrapstyleword", true);
    private static final boolean LINE_WRAP       = JMeterUtils.getPropDefault("jsyntaxtextarea.linewrap", true);
    private static final boolean CODE_FOLDING    = JMeterUtils.getPropDefault("jsyntaxtextarea.codefolding", true);
    private static final int MAX_UNDOS           = JMeterUtils.getPropDefault("jsyntaxtextarea.maxundos", 50);

    @Deprecated
    public JSyntaxTextArea() {
        // For use by test code only
        this(30, 50, false);
    }

    /**
     * Creates the default syntax highlighting text area.
     * The following are set:
     * <ul>
     * <li>setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA)</li>
     * <li>setCodeFoldingEnabled(true)</li>
     * <li>setAntiAliasingEnabled(true)</li>
     * <li>setLineWrap(true)</li>
     * <li>setWrapStyleWord(true)</li>
     * </ul>
     * @param rows
     * @param cols
     */
    public JSyntaxTextArea(int rows, int cols) {
        this(rows, cols, false);
    }

    /**
     * Creates the default syntax highlighting text area.
     * The following are set:
     * <ul>
     * <li>setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA)</li>
     * <li>setCodeFoldingEnabled(true)</li>
     * <li>setAntiAliasingEnabled(true)</li>
     * <li>setLineWrap(true)</li>
     * <li>setWrapStyleWord(true)</li>
     * </ul>
     * @param rows
     * @param cols
     * @param disableUndo true to disable undo manager, defaults to false
     */
    public JSyntaxTextArea(int rows, int cols, boolean disableUndo) {
        super(rows, cols);
        super.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        super.setCodeFoldingEnabled(CODE_FOLDING);
        super.setAntiAliasingEnabled(true);
        super.setLineWrap(LINE_WRAP);
        super.setWrapStyleWord(WRAP_STYLE_WORD);
        this.disableUndo = disableUndo;
    }

    /**
     * Sets the language of the text area.
     * @param language
     */
    public void setLanguage(String language) {
        final String style = languageProperties.getProperty(language);
        if (style == null) {
            super.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        } else {
            super.setSyntaxEditingStyle(style);
        }
    }

    /**
     * Override UndoManager to allow disabling if feature causes issues
     * See https://github.com/bobbylight/RSyntaxTextArea/issues/19
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
     * @param string
     */
    public void setInitialText(String string) {
        setText(string);
        discardAllEdits();
    }
}
