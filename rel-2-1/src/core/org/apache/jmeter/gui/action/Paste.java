/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * Places a copied JMeterTreeNode under the selected node.
 * 
 * @author Thad Smith
 * @version $Revision$
 */
public class Paste extends AbstractAction {

	private static Set commands = new HashSet();
	static {
		commands.add(ActionNames.PASTE);
	}

	/**
	 * @see Command#getActionNames()
	 */
	public Set getActionNames() {
		return commands;
	}

	/**
	 * @see Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e) {
		JMeterTreeNode draggedNodes[] = Copy.getCopiedNodes();
		JMeterTreeListener treeListener = GuiPackage.getInstance().getTreeListener();
		JMeterTreeNode currentNode = treeListener.getCurrentNode();
		if (DragNDrop.canAddTo(currentNode)) {
			for (int i = 0; i < draggedNodes.length; i++) {
				if (draggedNodes[i] != null) {
					GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], currentNode,
							currentNode.getChildCount());
				}
			}
		}
		GuiPackage.getInstance().getMainFrame().repaint();
	}
}