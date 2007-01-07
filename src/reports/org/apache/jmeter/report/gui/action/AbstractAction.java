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

package org.apache.jmeter.report.gui.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.report.gui.tree.ReportTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 * @version $Revision$
 */
public abstract class AbstractAction implements Command {
	private static final Logger log = LoggingManager.getLoggerForClass();

	/**
	 * @see Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e) {
	}

	/**
	 * @see Command#getActionNames()
	 */
	abstract public Set getActionNames();

	protected void convertSubTree(HashTree tree) {
		Iterator iter = new LinkedList(tree.list()).iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if(o instanceof TestElement)
				continue; //hey, no need to convert
			ReportTreeNode item = (ReportTreeNode) o;
			if (item.isEnabled()) {
				if (item.getUserObject() instanceof ReplaceableController) {
					ReplaceableController rc = (ReplaceableController) item.getTestElement();
					HashTree subTree = tree.getTree(item);

					if (subTree != null) {
						HashTree replacementTree = rc.getReplacementSubTree();
						convertSubTree(replacementTree);
						tree.replace(item,rc);
						tree.set(rc,replacementTree);
					}
				} else {
					convertSubTree(tree.getTree(item));
					TestElement testElement = item.getTestElement();
					tree.replace(item, testElement);
				}
			} else {
				tree.remove(item);
			}

		}
	}

	/**
	 * @param e
	 */
	protected void popupShouldSave(ActionEvent e) {
		log.debug("popupShouldSave");
		if (ReportGuiPackage.getInstance().getReportPlanFile() == null) {
			if (JOptionPane.showConfirmDialog(ReportGuiPackage.getInstance().getMainFrame(), JMeterUtils
					.getResString("should_save"), JMeterUtils.getResString("warning"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				ReportActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ReportSave.SAVE));
			}
		}
	}
}
