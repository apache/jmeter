// $Header$
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

package org.apache.jmeter.report.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Michael Stover Created March 1, 2001
 * @version $Revision$ Last updated: $Date$
 */
public class ReportStart extends AbstractAction {
	private static Logger log = LoggingManager.getLoggerForClass();

	private static Set commands = new HashSet();
	static {
		commands.add(JMeterMenuBar.ACTION_START);
		commands.add(JMeterMenuBar.ACTION_STOP);
		commands.add(JMeterMenuBar.ACTION_SHUTDOWN);
	}

	private StandardJMeterEngine engine;

	/**
	 * Constructor for the Start object.
	 */
	public ReportStart() {
	}

	/**
	 * Gets the ActionNames attribute of the Start object.
	 * 
	 * @return the ActionNames value
	 */
	public Set getActionNames() {
		return commands;
	}

	public void doAction(ActionEvent e) {
		if (e.getActionCommand().equals(JMeterMenuBar.ACTION_START)) {
			popupShouldSave(e);
			startEngine();
		} else if (e.getActionCommand().equals(JMeterMenuBar.ACTION_STOP)) {
			if (engine != null) {
				ReportGuiPackage.getInstance().getMainFrame().showStoppingMessage("");
				engine.stopTest();
				engine = null;
			}
		} else if (e.getActionCommand().equals(JMeterMenuBar.ACTION_SHUTDOWN)) {
			if (engine != null) {
				ReportGuiPackage.getInstance().getMainFrame().showStoppingMessage("");
				engine.askThreadsToStop();
				engine = null;
			}
		}
	}

	protected void startEngine() {
		ReportGuiPackage gui = ReportGuiPackage.getInstance();
        /**
         * this will need to be changed
		engine = new StandardJMeterEngine();
		HashTree testTree = gui.getTreeModel().getTestPlan();
		convertSubTree(testTree);
		DisabledComponentRemover remover = new DisabledComponentRemover(testTree);
		testTree.traverse(remover);
		testTree.add(testTree.getArray()[0], gui.getMainFrame());
		log.debug("test plan before cloning is running version: "
				+ ((TestPlan) testTree.getArray()[0]).isRunningVersion());
		TreeCloner cloner = new TreeCloner(false);
		testTree.traverse(cloner);
		engine.configure(cloner.getClonedTree());
		try {
			engine.runTest();
		} catch (JMeterEngineException e) {
			JOptionPane.showMessageDialog(gui.getMainFrame(), e.getMessage(), JMeterUtils
					.getResString("Error Occurred"), JOptionPane.ERROR_MESSAGE);
		}
		log.debug("test plan after cloning and running test is running version: "
				+ ((TestPlan) testTree.getArray()[0]).isRunningVersion());
         */
	}
}
