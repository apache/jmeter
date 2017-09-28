package org.apache.jmeter.gui;

import javax.swing.undo.CompoundEdit;

public class SimpleCompoundEdit extends CompoundEdit {

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return edits.size();
    }
}
