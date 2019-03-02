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

import org.fife.ui.rtextarea.RTextScrollPane;


/**
 * Utility class to handle RSyntaxTextArea code
 * It's not currently possible to instantiate the RTextScrollPane class when running headless.
 * So we use getInstance methods to create the class and allow for headless testing.
 */
public class JTextScrollPane extends RTextScrollPane {

    private static final long serialVersionUID = 210L;

    @Deprecated
    public JTextScrollPane() {
        // for use by test code only
    }

    public static JTextScrollPane getInstance(JSyntaxTextArea scriptField, boolean foldIndicatorEnabled) {
        try {
            return new JTextScrollPane(scriptField, foldIndicatorEnabled);
        } catch (NullPointerException npe) { // for headless unit testing
            if ("true".equals(System.getProperty("java.awt.headless"))) { // $NON-NLS-1$ $NON-NLS-2$
                return new JTextScrollPane();                
            } else {
                throw npe;
            }
        }
    }

    public static JTextScrollPane getInstance(JSyntaxTextArea scriptField) {
        try {
            return new JTextScrollPane(scriptField);
        } catch (NullPointerException npe) { // for headless unit testing
            if ("true".equals(System.getProperty("java.awt.headless"))) { // $NON-NLS-1$ $NON-NLS-2$
                return new JTextScrollPane();                
            } else {
                throw npe;
            }
        }
    }

    /**
     * @param scriptField syntax text are to wrap
     * @deprecated use {@link #getInstance(JSyntaxTextArea)} instead
     */
    @Deprecated
    public JTextScrollPane(JSyntaxTextArea scriptField) {
        super(scriptField);
    }

    /**
     * 
     * @param scriptField syntax text are to wrap
     * @param foldIndicatorEnabled flag, whether fold indicator should be enabled
     * @deprecated use {@link #getInstance(JSyntaxTextArea, boolean)} instead
     */
    @Deprecated
    public JTextScrollPane(JSyntaxTextArea scriptField, boolean foldIndicatorEnabled) {
        super(scriptField);
        super.setFoldIndicatorEnabled(foldIndicatorEnabled);
    }

}
