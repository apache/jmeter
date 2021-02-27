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

package org.apache.jmeter.gui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * This class decides if we need to set/clear asterix on 
 * the main window title after tree changes
 *
 * @since 5.5
 */
public class DirtyTitleHandler implements TreeModelListener {

    /**
     * Record the changes in the node
     *
     * @param tme {@link TreeModelEvent} with event details
     */
    @Override
    public void treeNodesChanged(TreeModelEvent tme) {
        // Do nothing, this gets fired after saving
    }

    /**
     * Record adding nodes
     *
     * @param tme {@link TreeModelEvent} with event details
     */
    @Override
    public void treeNodesInserted(TreeModelEvent tme) {
        handleChangedTree();
    }

    /**
     * Record deleting nodes
     *
     * @param tme {@link TreeModelEvent} with event details
     */
    @Override
    public void treeNodesRemoved(TreeModelEvent tme) {
        handleChangedTree();
    }

    /**
     * Record some other change
     *
     * @param tme {@link TreeModelEvent} with event details
     */
    @Override
    public void treeStructureChanged(TreeModelEvent tme) {
        handleChangedTree();
    }
    
    /**
     * Handles the title change
     *
     */
    public void handleChangedTree() {
        GuiPackage guiPackage = GuiPackage.getInstance();
        if(guiPackage != null){
            guiPackage.setDirty(true);
        }
    }

}
