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

package org.apache.jmeter.gui;

import java.util.function.Consumer;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public final class GlobalUndoableEdit extends AbstractUndoableEdit {

    private static final long serialVersionUID = -4964577622742131354L;
    private final UndoHistoryItem item;
    private final UndoHistoryItem previous;
    private final Consumer<UndoHistoryItem> loader;

    public GlobalUndoableEdit(UndoHistoryItem item, UndoHistoryItem previous, Consumer<UndoHistoryItem> loader) {
        this.item = item;
        this.previous = previous;
        this.loader = loader;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();

        loader.accept(previous);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();

        loader.accept(item);
    }
}
