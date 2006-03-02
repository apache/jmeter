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
import java.awt.event.ActionListener;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.log.Logger;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public final class ActionRouter implements ActionListener {
	private Map commands = new HashMap();

	private static ActionRouter router;

	private static final Logger log = LoggingManager.getLoggerForClass();

	private Map preActionListeners = new HashMap();

	private Map postActionListeners = new HashMap();

	private ActionRouter() {
	}

	public void actionPerformed(final ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				performAction(e);
			}

		});
	}

	private void performAction(final ActionEvent e) {
		try {
			GuiPackage.getInstance().updateCurrentNode();
			Set commandObjects = (Set) commands.get(e.getActionCommand());
			Iterator iter = commandObjects.iterator();
			while (iter.hasNext()) {
				try {
					Command c = (Command) iter.next();
					preActionPerformed(c.getClass(), e);
					c.doAction(e);
					postActionPerformed(c.getClass(), e);
				} catch (IllegalUserActionException err) {
					JMeterUtils.reportErrorToUser(err.toString());
				} catch (Exception err) {
					log.error("", err);
				}
			}
		} catch (NullPointerException er) {
			log.error("performAction(" + e.getActionCommand() + ") " + e.toString() + " caused", er);
			JMeterUtils.reportErrorToUser("Sorry, this feature (" + e.getActionCommand() + ") not yet implemented");
		}
	}

	/**
	 * To execute an action immediately in the current thread.
	 * 
	 * @param e
	 *            the action to execute
	 */
	public void doActionNow(ActionEvent e) {
		performAction(e);
	}

	public Set getAction(String actionName) {
		Set set = new HashSet();
		Set commandObjects = (Set) commands.get(actionName);
		Iterator iter = commandObjects.iterator();
		while (iter.hasNext()) {
			try {
				set.add(iter.next());
			} catch (Exception err) {
				log.error("", err);
			}
		}
		return set;
	}

	public Command getAction(String actionName, Class actionClass) {
		Set commandObjects = (Set) commands.get(actionName);
		Iterator iter = commandObjects.iterator();
		while (iter.hasNext()) {
			try {
				Command com = (Command) iter.next();
				if (com.getClass().equals(actionClass)) {
					return com;
				}
			} catch (Exception err) {
				log.error("", err);
			}
		}
		return null;
	}

	public Command getAction(String actionName, String className) {
		Set commandObjects = (Set) commands.get(actionName);
		Iterator iter = commandObjects.iterator();
		while (iter.hasNext()) {
			try {
				Command com = (Command) iter.next();
				if (com.getClass().getName().equals(className)) {
					return com;
				}
			} catch (Exception err) {
				log.error("", err);
			}
		}
		return null;
	}

	/**
	 * Allows an ActionListener to receive notification of a command being
	 * executed prior to the actual execution of the command.
	 * 
	 * @param action
	 *            the Class of the command for which the listener will
	 *            notifications for. Class must extend
	 *            org.apache.jmeter.gui.action.Command.
	 * @param listener
	 *            the ActionListener to receive the notifications
	 */
	public void addPreActionListener(Class action, ActionListener listener) {
		if (action != null) {
			HashSet set = (HashSet) preActionListeners.get(action.getName());
			if (set == null) {
				set = new HashSet();
			}
			set.add(listener);
			preActionListeners.put(action.getName(), set);
		}
	}

	/**
	 * Allows an ActionListener to be removed from receiving notifications of a
	 * command being executed prior to the actual execution of the command.
	 * 
	 * @param action
	 *            the Class of the command for which the listener will
	 *            notifications for. Class must extend
	 *            org.apache.jmeter.gui.action.Command.
	 * @param listener
	 *            the ActionListener to receive the notifications
	 */
	public void removePreActionListener(Class action, ActionListener listener) {
		if (action != null) {
			HashSet set = (HashSet) preActionListeners.get(action.getName());
			if (set != null) {
				set.remove(listener);
				preActionListeners.put(action.getName(), set);
			}
		}
	}

	/**
	 * Allows an ActionListener to receive notification of a command being
	 * executed after the command has executed.
	 * 
	 * @param action
	 *            the Class of the command for which the listener will
	 *            notifications for. Class must extend
	 *            org.apache.jmeter.gui.action.Command.
	 * @param listener
	 */
	public void addPostActionListener(Class action, ActionListener listener) {
		if (action != null) {
			HashSet set = (HashSet) postActionListeners.get(action.getName());
			if (set == null) {
				set = new HashSet();
			}
			set.add(listener);
			postActionListeners.put(action.getName(), set);
		}
	}

	/**
	 * Allows an ActionListener to be removed from receiving notifications of a
	 * command being executed after the command has executed.
	 * 
	 * @param action
	 *            the Class of the command for which the listener will
	 *            notifications for. Class must extend
	 *            org.apache.jmeter.gui.action.Command.
	 * @param listener
	 */
	public void removePostActionListener(Class action, ActionListener listener) {
		if (action != null) {
			HashSet set = (HashSet) postActionListeners.get(action.getName());
			if (set != null) {
				set.remove(listener);
				postActionListeners.put(action.getName(), set);
			}
		}
	}

	protected void preActionPerformed(Class action, ActionEvent e) {
		if (action != null) {
			HashSet listenerSet = (HashSet) preActionListeners.get(action.getName());
			if (listenerSet != null && listenerSet.size() > 0) {
				Object[] listeners = listenerSet.toArray();
				for (int i = 0; i < listeners.length; i++) {
					((ActionListener) listeners[i]).actionPerformed(e);
				}
			}
		}
	}

	protected void postActionPerformed(Class action, ActionEvent e) {
		if (action != null) {
			HashSet listenerSet = (HashSet) postActionListeners.get(action.getName());
			if (listenerSet != null && listenerSet.size() > 0) {
				Object[] listeners = listenerSet.toArray();
				for (int i = 0; i < listeners.length; i++) {
					((ActionListener) listeners[i]).actionPerformed(e);
				}
			}
		}
	}

	private void populateCommandMap() {
		List listClasses;
		Command command;
		Iterator iterClasses;
		Class commandClass;
		try {
			listClasses = ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(), new Class[] { Class
					.forName("org.apache.jmeter.gui.action.Command") });
			commands = new HashMap(listClasses.size());
			if (listClasses.size() == 0) {
				log.warn("!!!!!Uh-oh, didn't find any action handlers!!!!!");
			}
			iterClasses = listClasses.iterator();
			while (iterClasses.hasNext()) {
				String strClassName = (String) iterClasses.next();
                if (strClassName.startsWith("org.apache.jmeter.gui")) {
                    commandClass = Class.forName(strClassName);
                    if (!Modifier.isAbstract(commandClass.getModifiers())) {
                        command = (Command) commandClass.newInstance();
                        Iterator iter = command.getActionNames().iterator();
                        while (iter.hasNext()) {
                            String commandName = (String) iter.next();
                            Set commandObjects = (Set) commands.get(commandName);
                            if (commandObjects == null) {
                                commandObjects = new HashSet();
                                commands.put(commandName, commandObjects);
                            }
                            commandObjects.add(command);
                        }
                    }
                }
			}
		} catch (Exception e) {
			if ("java.awt.HeadlessException".equals(e.getClass().getName())) // JDK1.4:
			{
				log.warn(e.toString());
			} else {
				log.error("exception finding action handlers", e);
			}
		}
	}

	/**
	 * Gets the Instance attribute of the ActionRouter class
	 * 
	 * @return The Instance value
	 */
	public static ActionRouter getInstance() {
		if (router == null) {
			router = new ActionRouter();
			router.populateCommandMap();
		}
		return router;
	}
}
