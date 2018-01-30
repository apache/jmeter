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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jorphan.collections.HashTree;

/**
 * Menu command to serve Undo/Redo
 * @since 2.12
 */
public class UndoCommand extends AbstractAction {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.UNDO);
        commands.add(ActionNames.REDO);
    }

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
        GuiPackage guiPackage = GuiPackage.getInstance();
        final String command = e.getActionCommand();

        if (command.equals(ActionNames.UNDO)) {
            guiPackage.undo();
        } else if (command.equals(ActionNames.REDO)) {
            guiPackage.redo();
        } else {
            throw new IllegalArgumentException("Wrong action called: " + command);
        }
    }

    /**
     * @return Set of all action names
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    /**
     * wrapper to use package-visible method
     * and clone tree for saving
     *
     * @param tree to be converted and cloned
     * @return converted and cloned tree
     */
    public static HashTree convertAndCloneSubTree(HashTree tree) {
        Save executor = new Save();
        executor.convertSubTree(tree);

        // convert before clone
        TreeCloner cloner = new TreeCloner(false);
        tree.traverse(cloner);
        return cloner.getClonedTree();
    }
}
