/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class AddParent implements Command {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private static Set commands = new HashSet();
	static {
		commands.add(ActionNames.ADD_PARENT);
	}

	public AddParent() {
	}

	public void doAction(ActionEvent e) {
		String name = ((Component) e.getSource()).getName();
		try {
			TestElement controller = GuiPackage.getInstance().createTestElement(name);
			addParentToTree(controller);
		} catch (Exception err) {
			log.error("", err);
		}

	}

	public Set getActionNames() {
		return commands;
	}

	protected void addParentToTree(TestElement newParent) {
		GuiPackage guiPackage = GuiPackage.getInstance();
		JMeterTreeNode newNode = new JMeterTreeNode(newParent, guiPackage.getTreeModel());
		JMeterTreeNode currentNode = guiPackage.getTreeListener().getCurrentNode();
		JMeterTreeNode parentNode = (JMeterTreeNode) currentNode.getParent();
		int index = parentNode.getIndex(currentNode);
		guiPackage.getTreeModel().insertNodeInto(newNode, parentNode, index);
		JMeterTreeNode[] nodes = guiPackage.getTreeListener().getSelectedNodes();
		for (int i = 0; i < nodes.length; i++) {
			moveNode(guiPackage, nodes[i], newNode);
		}
	}

	private void moveNode(GuiPackage guiPackage, JMeterTreeNode node, JMeterTreeNode newParentNode) {
		guiPackage.getTreeModel().removeNodeFromParent(node);
		guiPackage.getTreeModel().insertNodeInto(node, newParentNode, newParentNode.getChildCount());
	}
}
