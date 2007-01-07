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
import java.util.Iterator;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class Clear implements Command {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private static Set commands = new HashSet();
	static {
		commands.add(ActionNames.CLEAR);
		commands.add(ActionNames.CLEAR_ALL);
	}

	public Clear() {
	}

	public Set getActionNames() {
		return commands;
	}

	public void doAction(ActionEvent e) {
		GuiPackage guiPackage = GuiPackage.getInstance();
		if (e.getActionCommand().equals(ActionNames.CLEAR)) {
			JMeterGUIComponent model = guiPackage.getCurrentGui();
			try {
				((Clearable) model).clear();
			} catch (Throwable ex) {
				log.error("", ex);
			}
		} else {
			Iterator iter = guiPackage.getTreeModel().getNodesOfType(Clearable.class).iterator();
			while (iter.hasNext()) {
                JMeterTreeNode node = null;
                JMeterGUIComponent guiComp = null;
				try {
					Object next = iter.next();
                    node = (JMeterTreeNode) next;
                    guiComp = guiPackage.getGui(node.getTestElement());
                    Clearable item = (Clearable) guiComp;
					item.clear();
				} catch (Exception ex) {
					log.error("Can't clear: "+node+" "+guiComp, ex);
				}
			}
		}
	}
}
