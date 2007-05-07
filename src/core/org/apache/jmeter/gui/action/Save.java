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
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.save.OldSaveService;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Save the current test plan; implements:
 * Save
 * Save TestPlan As
 * Save (Selection) As
 */
public class Save implements Command {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private static Set commands = new HashSet();
	static {
		commands.add(ActionNames.SAVE_AS); // Save (Selection) As
		commands.add(ActionNames.SAVE_ALL_AS); // Save TestPlan As
		commands.add(ActionNames.SAVE); // Save
	}

	/**
	 * Constructor for the Save object.
	 */
	public Save() {
	}

	/**
	 * Gets the ActionNames attribute of the Save object.
	 * 
	 * @return the ActionNames value
	 */
	public Set getActionNames() {
		return commands;
	}

	public void doAction(ActionEvent e) throws IllegalUserActionException {
		HashTree subTree = null;
		if (!commands.contains(e.getActionCommand())) {
			throw new IllegalUserActionException("Invalid user command:" + e.getActionCommand());
		}
		if (e.getActionCommand().equals(ActionNames.SAVE_AS)) {
			subTree = GuiPackage.getInstance().getCurrentSubTree();
		} else {
			subTree = GuiPackage.getInstance().getTreeModel().getTestPlan();
		}

		String updateFile = GuiPackage.getInstance().getTestPlanFile();
		if (!ActionNames.SAVE.equals(e.getActionCommand()) || updateFile == null) {
			JFileChooser chooser = FileDialoger.promptToSaveFile(GuiPackage.getInstance().getTreeListener()
					.getCurrentNode().getName()
					+ ".jmx"); // $NON-NLS-1$
			if (chooser == null) {
				return;
			}
			updateFile = chooser.getSelectedFile().getAbsolutePath();
			if (!e.getActionCommand().equals(ActionNames.SAVE_AS)) {
				GuiPackage.getInstance().setTestPlanFile(updateFile);
			}
		}
		// TODO: doesn't putting this here mark the tree as
		// saved even though a failure may occur later?

		ActionRouter.getInstance().doActionNow(new ActionEvent(subTree, e.getID(), ActionNames.SUB_TREE_SAVED));
		try {
			convertSubTree(subTree);
		} catch (Exception err) {
		}
		FileOutputStream ostream = null;
		try {
			ostream = new FileOutputStream(updateFile);
			if (SaveService.isSaveTestPlanFormat20()) {
				OldSaveService.saveSubTree(subTree, ostream);
			} else {
				SaveService.saveTree(subTree, ostream);
			}
		} catch (Throwable ex) {
			GuiPackage.getInstance().setTestPlanFile(null);
			log.error("", ex);
			throw new IllegalUserActionException("Couldn't save test plan to file: " + updateFile);
		} finally {
            JOrphanUtils.closeQuietly(ostream);
		}
	}

	// package protected to all for separate test code
	void convertSubTree(HashTree tree) {
		Iterator iter = new LinkedList(tree.list()).iterator();
		while (iter.hasNext()) {
			JMeterTreeNode item = (JMeterTreeNode) iter.next();
			convertSubTree(tree.getTree(item));
			TestElement testElement = item.getTestElement();
			tree.replace(item, testElement);
		}
	}
}
