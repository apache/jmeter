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

package org.apache.jorphan.gui;

// jorphan cannot call methods in core because it is built first
//import org.apache.jmeter.util.JMeterUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * Utility class to handle RSyntaxTextArea code
 */
public class JSyntaxTextArea extends RSyntaxTextArea {

    private static final long serialVersionUID = 210L;

    private final boolean WRAP_STYLE_WORD = true;//JMeterUtils.getPropDefault("jsyntaxtextarea.wrapstyleword", true);
    private final boolean LINE_WRAP = true;//JMeterUtils.getPropDefault("jsyntaxtextarea.linewrap", true);
    private final boolean CODE_FOLDING = true;//JMeterUtils.getPropDefault("jsyntaxtextarea.codefolding", true);

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
        super(rows, cols);
        setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        setCodeFoldingEnabled(CODE_FOLDING);
        setAntiAliasingEnabled(true);
        setLineWrap(LINE_WRAP);
        setWrapStyleWord(WRAP_STYLE_WORD);
    }

    @Override
    public void setSyntaxEditingStyle(String style) {
        if (style == null) {
            super.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        } else {
            super.setSyntaxEditingStyle(style);
        }
    }

}
