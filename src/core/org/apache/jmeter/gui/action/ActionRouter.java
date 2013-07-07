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

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JMeterError;
import org.apache.log.Logger;

public final class ActionRouter implements ActionListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final Object LOCK = new Object();

    private static volatile ActionRouter router;

    private Map<String, Set<Command>> commands = new HashMap<String, Set<Command>>();

    private final Map<String, HashSet<ActionListener>> preActionListeners =
        new HashMap<String, HashSet<ActionListener>>();

    private final Map<String, HashSet<ActionListener>> postActionListeners =
        new HashMap<String, HashSet<ActionListener>>();

    private ActionRouter() {
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                performAction(e);
            }

        });
    }

    private void performAction(final ActionEvent e) {
        String actionCommand = e.getActionCommand();
        try {
            try {
                GuiPackage.getInstance().updateCurrentGui();
            } catch (Exception err){
                log.error("performAction(" + actionCommand + ") updateCurrentGui() on" + e.toString() + " caused", err);
                JMeterUtils.reportErrorToUser("Problem updating GUI - see log file for details");
            }
            for (Command c : commands.get(actionCommand)) {
                try {
                    preActionPerformed(c.getClass(), e);
                    c.doAction(e);
                    postActionPerformed(c.getClass(), e);
                } catch (IllegalUserActionException err) {
                    String msg = err.getMessage();
                    if (msg == null) {
                        msg = err.toString();
                    }
                    Throwable t = err.getCause();
                    if (t != null) {
                        String cause = t.getMessage();
                        if (cause == null) {
                            cause = t.toString();
                        }
                        msg = msg + "\n" + cause;
                    }
                    JMeterUtils.reportErrorToUser(msg);
                } catch (Exception err) {
                    log.error("Error processing "+c.toString(), err);
                }
            }
        } catch (NullPointerException er) {
            log.error("performAction(" + actionCommand + ") " + e.toString() + " caused", er);
            JMeterUtils.reportErrorToUser("Sorry, this feature (" + actionCommand + ") not yet implemented");
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

    public Set<Command> getAction(String actionName) {
        Set<Command> set = new HashSet<Command>();
        for (Command c : commands.get(actionName)) {
            try {
                set.add(c);
            } catch (Exception err) {
                log.error("Could not add Command", err);
            }
        }
        return set;
    }

    public Command getAction(String actionName, Class<?> actionClass) {
        for (Command com : commands.get(actionName)) {
            if (com.getClass().equals(actionClass)) {
                return com;
            }
        }
        return null;
    }

    public Command getAction(String actionName, String className) {
        for (Command com : commands.get(actionName)) {
            if (com.getClass().getName().equals(className)) {
                return com;
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
    public void addPreActionListener(Class<?> action, ActionListener listener) {
        if (action != null) {
            HashSet<ActionListener> set = preActionListeners.get(action.getName());
            if (set == null) {
                set = new HashSet<ActionListener>();
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
    public void removePreActionListener(Class<?> action, ActionListener listener) {
        if (action != null) {
            HashSet<ActionListener> set = preActionListeners.get(action.getName());
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
    public void addPostActionListener(Class<?> action, ActionListener listener) {
        if (action != null) {
            HashSet<ActionListener> set = postActionListeners.get(action.getName());
            if (set == null) {
                set = new HashSet<ActionListener>();
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
    public void removePostActionListener(Class<?> action, ActionListener listener) {
        if (action != null) {
            HashSet<ActionListener> set = postActionListeners.get(action.getName());
            if (set != null) {
                set.remove(listener);
                postActionListeners.put(action.getName(), set);
            }
        }
    }

    protected void preActionPerformed(Class<? extends Command> action, ActionEvent e) {
        if (action != null) {
            Set<ActionListener> listenerSet = preActionListeners.get(action.getName());
            if (listenerSet != null && listenerSet.size() > 0) {
                ActionListener[] listeners = listenerSet.toArray(new ActionListener[listenerSet.size()]);
                for (ActionListener listener : listeners) {
                    listener.actionPerformed(e);
                }
            }
        }
    }

    protected void postActionPerformed(Class<? extends Command> action, ActionEvent e) {
        if (action != null) {
            Set<ActionListener> listenerSet = postActionListeners.get(action.getName());
            if (listenerSet != null && listenerSet.size() > 0) {
                ActionListener[] listeners = listenerSet.toArray(new ActionListener[listenerSet.size()]);
                for (ActionListener listener : listeners) {
                    listener.actionPerformed(e);
                }
            }
        }
    }

    private void populateCommandMap() {
        try {
            List<String> listClasses = ClassFinder.findClassesThatExtend(
                    JMeterUtils.getSearchPaths(), // strPathsOrJars - pathnames or jarfiles to search for classes
                    // classNames - required parent class(es) or annotations
                    new Class[] {Class.forName("org.apache.jmeter.gui.action.Command") }, // $NON-NLS-1$
                    false, // innerClasses - should we include inner classes?
                    null, // contains - classname should contain this string
                    // Ignore the classes which are specific to the reporting tool
                    "org.apache.jmeter.report.gui", // $NON-NLS-1$ // notContains - classname should not contain this string
                    false); // annotations - true if classnames are annotations
            commands = new HashMap<String, Set<Command>>(listClasses.size());
            if (listClasses.isEmpty()) {
                log.fatalError("!!!!!Uh-oh, didn't find any action handlers!!!!!");
                throw new JMeterError("No action handlers found - check JMeterHome and libraries");
            }
            for (String strClassName : listClasses) {
                Class<?> commandClass = Class.forName(strClassName);
                Command command = (Command) commandClass.newInstance();
                for (String commandName : command.getActionNames()) {
                    Set<Command> commandObjects = commands.get(commandName);
                    if (commandObjects == null) {
                        commandObjects = new HashSet<Command>();
                        commands.put(commandName, commandObjects);
                    }
                    commandObjects.add(command);
                }
            }
        } catch (HeadlessException e){
            log.warn(e.toString());
        } catch (Exception e) {
            log.error("exception finding action handlers", e);
        }
    }

    /**
     * Gets the Instance attribute of the ActionRouter class
     *
     * @return The Instance value
     */
    public static ActionRouter getInstance() {
        if (router == null) {
            synchronized (LOCK) {
                if(router == null) {
                    router = new ActionRouter();
                    router.populateCommandMap();
                }
            }
        }
        return router;
    }
}
