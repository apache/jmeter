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

import javax.swing.JFrame;

/**
 * Search nodes for a text
 * TODO Enhance search dialog to select kind of nodes ....
 */
public class SearchTreeCommand extends AbstractAction {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.SEARCH_TREE);
    }

    private SearchTreeDialog dialog;

    /**
     * <p>
     * Create the search dialog from the specified source component.<br>
     * This method tries to find a JFrame ancestor from the specified source in
     * order to be the parent of the search dialog.<br>
     * With no parent set the search dialog might be hidden by the main JFrame when
     * focus is transfered to that JFrame.
     * </p>
     * <p>
     * If no parent if found, then we give up and build a search dialog with no
     * parent.
     * </p>
     * 
     * @param source The source object that originated the display of the dialog
     * @return A freshly created search dialog with the parent frame that could be
     *         found, or no parent otherwise.
     */
    private SearchTreeDialog createSearchDialog(ActionEvent event) {
        JFrame parent = getParentFrame(event);
        return new SearchTreeDialog(parent);
    }

    
    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        // we create the dialog upon first display event only
        if (dialog == null) {
            dialog = createSearchDialog(e);
        }
        dialog.setVisible(true);
    }


    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
