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

package org.apache.jorphan.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Provides actions for moving focus forward and backward.
 */
class FocusActions {
    static final AbstractAction TRANSFER_FOCUS = new TransferFocusAction();
    static final AbstractAction TRANSFER_FOCUS_BACKWARD = new TransferFocusBackwardAction();

    /**
     * Binds the given key stokes to focus transfer actions.
     * @param c component for adding key strokes
     * @param focusForward keystroke for forward focus transfer, or null
     * @param focusBackward keystroke for backward focus transfer, or null
     */
    static void bind(JComponent c, KeyStroke focusForward, KeyStroke focusBackward) {
        Object transferFocusName = TRANSFER_FOCUS.getValue(Action.NAME);
        Object transferFocusBackward = TRANSFER_FOCUS_BACKWARD.getValue(Action.NAME);
        InputMap inputMap = c.getInputMap();
        ActionMap actionMap = c.getActionMap();
        if (focusForward != null) {
            inputMap.put(focusForward, transferFocusName);
            actionMap.put(transferFocusName, TRANSFER_FOCUS);
        }
        if (focusBackward != null) {
            inputMap.put(focusBackward, transferFocusBackward);
            actionMap.put(transferFocusBackward, TRANSFER_FOCUS_BACKWARD);
        }
    }

    private static class TransferFocusAction extends AbstractAction {
        public TransferFocusAction() {
            super("Transfer focus forward"); // $NON-NLS-1$
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Component comp = (Component) e.getSource();
            if (comp != null) {
                comp.transferFocus();
            }
        }
    }

    private static class TransferFocusBackwardAction extends AbstractAction {
        public TransferFocusBackwardAction() {
            super("Transfer focus backward"); // $NON-NLS-1$
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Component comp = (Component) e.getSource();
            if (comp != null) {
                comp.transferFocusBackward();
            }
        }
    }
}
