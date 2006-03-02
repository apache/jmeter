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

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;

/**
 * @author mstover
 * @version $Revision$
 */
public class DragNDrop extends AbstractAction {
	private static Set commands = new HashSet();
	static {
		commands.add(ActionNames.DRAG_ADD);
		commands.add(ActionNames.INSERT_BEFORE);
		commands.add(ActionNames.INSERT_AFTER);
	}

	/**
	 * @see Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e) {
		String action = e.getActionCommand();
		GuiPackage guiPackage = GuiPackage.getInstance();
		JMeterTreeNode[] draggedNodes = guiPackage.getTreeListener().getDraggedNodes();
		JMeterTreeListener treeListener = guiPackage.getTreeListener();
		JMeterTreeNode currentNode = treeListener.getCurrentNode();
		JMeterTreeNode parentNode = (JMeterTreeNode) currentNode.getParent();
		TestElement te = currentNode.getTestElement();
		if (te instanceof TestPlan || te instanceof WorkBench) {
			parentNode = null; // So elements can only be added as children
		}
		// System.out.println(action+" "+te.getClass().getName());

		if (ActionNames.DRAG_ADD.equals(action) && canAddTo(currentNode)) {
			removeNodesFromParents(draggedNodes);
			for (int i = 0; i < draggedNodes.length; i++) {
				GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], currentNode,
						currentNode.getChildCount());
			}
		} else if (ActionNames.INSERT_BEFORE.equals(action) && canAddTo(parentNode)) {
			removeNodesFromParents(draggedNodes);
			for (int i = 0; i < draggedNodes.length; i++) {
				int index = parentNode.getIndex(currentNode);
				GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], parentNode, index);
			}
		} else if (ActionNames.INSERT_AFTER.equals(action) && canAddTo(parentNode)) {
			removeNodesFromParents(draggedNodes);
			for (int i = 0; i < draggedNodes.length; i++) {
				int index = parentNode.getIndex(currentNode) + 1;
				GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], parentNode, index);
			}
		}
		GuiPackage.getInstance().getMainFrame().repaint();
	}

	/**
	 * Determine whether or not dragged nodes can be added to this parent. Also
	 * used by Paste TODO tighten rules TODO move to MenuFactory?
	 * 
	 * @param parentNode
	 * @return whether it is OK to add the dragged nodes to this parent
	 */
	static boolean canAddTo(JMeterTreeNode parentNode) {
		if (null == parentNode)
			return false;
		TestElement te = parentNode.getTestElement();
		// System.out.println("Add to: "+te.getClass().getName());
		if (te instanceof Controller)
			return true;
		if (te instanceof Sampler)
			return true;
		if (te instanceof WorkBench)
			return true;
		if (te instanceof TestPlan)
			return true;
		return false;
	}

	protected void removeNodesFromParents(JMeterTreeNode[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			GuiPackage.getInstance().getTreeModel().removeNodeFromParent(nodes[i]);
		}
	}

	/**
	 * @see Command#getActionNames()
	 */
	public Set getActionNames() {
		return commands;
	}
}
