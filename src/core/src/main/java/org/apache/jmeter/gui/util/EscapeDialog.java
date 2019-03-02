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

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;

import org.apache.jmeter.gui.action.KeyStrokes;

public class EscapeDialog extends JDialog {

    private static final long serialVersionUID = 1319421816741139938L;

    public EscapeDialog() {
        super();
    }

    public EscapeDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
    }

    @Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        Action escapeAction = new AbstractAction("ESCAPE") {
            /**
             *
             */
            private static final long serialVersionUID = 2208129319916921772L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStrokes.ESC, escapeAction.getValue(Action.NAME));
        rootPane.getActionMap().put(escapeAction.getValue(Action.NAME), escapeAction);
        return rootPane;
    }
}
