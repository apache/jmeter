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

package org.apache.jorphan.gui.ui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import org.apiguardian.api.API;

/**
 * Configures undo manager for a text component.
 */
@API(since = "5.3", status = API.Status.INTERNAL)
public class TextComponentUI {
    private static final String OS_NAME = System.getProperty("os.name");// $NON-NLS-1$

    private static final boolean IS_MAC =
            Pattern.compile("mac os x|darwin|osx", Pattern.CASE_INSENSITIVE)
                    .matcher(OS_NAME)
                    .find();

    private static final int COMMAND_KEY =
            IS_MAC ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

    @API(since = "5.3", status = API.Status.INTERNAL)
    public static final TextComponentUI INSTANCE = new TextComponentUI();

    private final AtomicInteger undoEpoch = new AtomicInteger();

    /**
     * Causes all undo managers to invalidate their history.
     */
    @API(since = "5.3", status = API.Status.INTERNAL)
    public void resetUndoHistory() {
        undoEpoch.getAndIncrement();
    }

    /**
     * Installs an undo manager and keyboard shortcuts to a text component
     * @param component JTextField or JTextArea
     */
    @API(since = "5.3", status = API.Status.INTERNAL)
    public void installUndo(JTextComponent component) {
        // JMeter reuses Swing JComponents, so when user switches to another component,
        // JComponent#name is updated. However, we don't want user to be able to "undo" that
        // So when tree selection is changed, we increase undoEpoch. That enables
        // UndoManagers to treat that as "end of undo history"
        UndoManager manager = new DefaultUndoManager(undoEpoch);
        manager.setLimit(200);
        component.addPropertyChangeListener("document",
                new AddUndoableEditListenerPropertyChangeListener(manager));
        component.getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manager.canUndo()) {
                    manager.undo();
                }
            }
        });
        component.getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manager.canRedo()) {
                    manager.redo();
                }
            }
        });

        KeyStroke commandZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, COMMAND_KEY);
        component.getInputMap().put(commandZ, "undo");
        KeyStroke shiftCommandZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, COMMAND_KEY | InputEvent.SHIFT_DOWN_MASK);
        component.getInputMap().put(shiftCommandZ, "redo");
    }

    /**
     * Removes the default undo manager.
     * By default, JMeter installs undo manager to all text fields via {@code Swing -> createUI},
     * however, undo is not always needed (e.g. log panel), so here's an API to remove it.
     * @param component JTextField or JTextArea
     */
    @API(since = "5.5", status = API.Status.INTERNAL)
    public static void uninstallUndo(JTextComponent component) {
        List<PropertyChangeListener> listenersToRemove = new ArrayList<>();
        for (PropertyChangeListener listener : component.getPropertyChangeListeners("document")) {
            if (listener instanceof AddUndoableEditListenerPropertyChangeListener) {
                AddUndoableEditListenerPropertyChangeListener v =
                        (AddUndoableEditListenerPropertyChangeListener) listener;
                listenersToRemove.add(v);

                UndoManager undoManager = v.getUndoManager();
                undoManager.discardAllEdits();
                Document document = component.getDocument();
                if (document != null) {
                    document.removeUndoableEditListener(undoManager);
                }
            }
        }
        for (PropertyChangeListener listener : listenersToRemove) {
            component.removePropertyChangeListener("document", listener);
        }
    }
}
