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
import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * @author Thad Smith
 * @version $Revision$
 */
public class Cut extends AbstractAction {
	private static Set commands = new HashSet();
	static {
		commands.add(ActionNames.CUT);
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
		GuiPackage guiPack = GuiPackage.getInstance();
		JMeterTreeNode[] currentNodes = guiPack.getTreeListener().getSelectedNodes();

		Copy.setCopiedNodes(currentNodes);
		for (int i = 0; i < currentNodes.length; i++) {
			guiPack.getTreeModel().removeNodeFromParent(currentNodes[i]);
		}
		guiPack.getMainFrame().repaint();
	}
}