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

import java.awt.Component;
import java.awt.TextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

import org.apache.jmeter.util.JMeterUtils;

public class NumberFieldErrorListener extends FocusAdapter {

    private static final NumberFieldErrorListener listener = new NumberFieldErrorListener();

    public static NumberFieldErrorListener getNumberFieldErrorListener() {
        return listener;
    }

    @Override
    public void focusLost(FocusEvent e) {
        Component source = (Component) e.getSource();
        String text = "";
        if (source instanceof JTextComponent) {
            text = ((JTextComponent) source).getText();
        } else if (source instanceof TextComponent) {
            text = ((TextComponent) source).getText();
        }
        try {
            Integer.parseInt(text);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(source,
                    JMeterUtils.getResString("you_must_enter_a_valid_number"), //$NON-NLS-1$
                    JMeterUtils.getResString("invalid_data"),  //$NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
            FocusRequester.requestFocus(source);
        }
    }
}
