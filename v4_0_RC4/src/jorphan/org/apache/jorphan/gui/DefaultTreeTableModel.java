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

package org.apache.jorphan.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.jorphan.reflect.Functor;

public class DefaultTreeTableModel extends AbstractTreeTableModel {

    private static final long serialVersionUID = 240L;

    public DefaultTreeTableModel() {
        this(new DefaultMutableTreeNode());
    }

    /**
     * @param root the {@link TreeNode} to use as root
     */
    public DefaultTreeTableModel(TreeNode root) {
        super(root);
    }

    /**
     * @param headers the headers to use
     * @param readFunctors the read functors to use
     * @param writeFunctors the write functors to use
     * @param editorClasses the editor classes to use
     */
    public DefaultTreeTableModel(String[] headers, Functor[] readFunctors,
            Functor[] writeFunctors, Class<?>[] editorClasses) {
        super(headers, readFunctors, writeFunctors, editorClasses);
    }

}
