package org.apache.jmeter.gui;

import java.util.function.Consumer;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public final class GlobalUndoableEdit extends AbstractUndoableEdit {

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
