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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

class AddUndoableEditListenerPropertyChangeListener implements PropertyChangeListener {
    private final UndoManager manager;

    public AddUndoableEditListenerPropertyChangeListener(UndoManager manager) {
        this.manager = manager;
    }

    public final UndoManager getUndoManager() {
        return manager;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        manager.discardAllEdits();
        if (evt.getOldValue() != null) {
            ((Document) evt.getOldValue()).removeUndoableEditListener(manager);
        }
        if (evt.getNewValue() != null) {
            ((Document) evt.getNewValue()).addUndoableEditListener(manager);
        }
    }
}
