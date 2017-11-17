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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JMeterError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ActionRouter implements ActionListener {
    private static final Logger log = LoggerFactory.getLogger(ActionRouter.class);

    // This is cheap, so no need to resort to IODH or lazy init
    private static final ActionRouter INSTANCE = new ActionRouter();

    private final Map<String, Set<Command>> commands = new HashMap<>();

    private final Map<String, Set<ActionListener>> preActionListeners =
            new HashMap<>();

    private final Map<String, Set<ActionListener>> postActionListeners =
            new HashMap<>();

    // New action will clear undo, no point in having a transaction. Same with open
    // EMI: XXX: Commands could also have an annotation to sigal the Undo preference
    private final List<String> NO_TRANSACTION_ACTIONS = Arrays.asList(ActionNames.CLOSE, ActionNames.OPEN, ActionNames.OPEN_RECENT);

    private ActionRouter() {
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        SwingUtilities.invokeLater(() -> performAction(e));
    }

    private void performAction(final ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if(!NO_TRANSACTION_ACTIONS.contains(actionCommand)) {
            GuiPackage.getInstance().beginUndoTransaction();
        }
        try {
            try {
                GuiPackage.getInstance().updateCurrentGui();
            } catch (Exception err){
                log.error("performAction({}) updateCurrentGui() on{} caused", actionCommand, e, err);
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
                    log.error("Error processing {}", c, err);
                }
            }
        } catch (NullPointerException er) {
            log.error("performAction({}) {} caused", actionCommand, e, er);
            JMeterUtils.reportErrorToUser("Sorry, this feature (" + actionCommand + ") not yet implemented");
        } finally {
            if(!NO_TRANSACTION_ACTIONS.contains(actionCommand)) {
                GuiPackage.getInstance().endUndoTransaction();
            }
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

    /**
     * Get the set of {@link Command}s registered under the name
     * <code>actionName</code>
     * 
     * @param actionName
     *            The name the {@link Command}s were registered
     * @return a set with all registered {@link Command}s for
     *         <code>actionName</code>
     */
    public Set<Command> getAction(String actionName) {
        Set<Command> set = new HashSet<>();
        for (Command c : commands.get(actionName)) {
            try {
                set.add(c);
            } catch (Exception err) {
                log.error("Could not add Command", err);
            }
        }
        return set;
    }

    /**
     * Get the {@link Command} registered under the name <code>actionName</code>,
     * that is of {@link Class} <code>actionClass</code>
     * 
     * @param actionName
     *            The name the {@link Command}s were registered
     * @param actionClass
     *            The class the {@link Command}s should be equal to
     * @return The registered {@link Command} for <code>actionName</code>, or
     *         <code>null</code> if none could be found
     */
    public Command getAction(String actionName, Class<?> actionClass) {
        for (Command com : commands.get(actionName)) {
            if (com.getClass().equals(actionClass)) {
                return com;
            }
        }
        return null;
    }

    /**
     * Get the {@link Command} registered under the name <code>actionName</code>
     * , which class names are equal to <code>className</code>
     * 
     * @param actionName
     *            The name the {@link Command}s were registered
     * @param className
     *            The name of the class the {@link Command}s should be equal to
     * @return The {@link Command} for <code>actionName</code> or
     *         <code>null</code> if none could be found
     */
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
        addActionListener(action, listener, preActionListeners);
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
        removeActionListener(action, listener, preActionListeners);
    }

    /**
     * Remove listener from actionsListeners associated to action
     * @param action {@link Class}
     * @param listener {@link ActionListener}
     * @param actionListeners {@link Set} of {@link ActionListener}
     */
    private void removeActionListener(Class<?> action, ActionListener listener, Map<String, Set<ActionListener>> actionListeners) {
        if (action != null) {
            Set<ActionListener> set = actionListeners.get(action.getName());
            if (set != null) {
                set.remove(listener);
                actionListeners.put(action.getName(), set);
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
     *            The {@link ActionListener} to be registered
     */
    public void addPostActionListener(Class<?> action, ActionListener listener) {
        addActionListener(action, listener, postActionListeners);
    }

    /**
     * @param action {@link Class}
     * @param listener {@link ActionListener}
     * @param actionListeners {@link Set}
     */
    private void addActionListener(Class<?> action, ActionListener listener, Map<String, Set<ActionListener>> actionListeners) {
        if (action != null) {
            Set<ActionListener> set = actionListeners.get(action.getName());
            if (set == null) {
                set = new HashSet<>();
            }
            set.add(listener);
            actionListeners.put(action.getName(), set);
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
     * @param listener The {@link ActionListener} that should be deregistered
     */
    public void removePostActionListener(Class<?> action, ActionListener listener) {
        removeActionListener(action, listener, postActionListeners);
    }

    /**
     * @param action {@link Class}
     * @param e {@link ActionEvent}
     */
    protected void preActionPerformed(Class<? extends Command> action, ActionEvent e) {
        actionPerformed(action, e, preActionListeners);
    }

    /**
     * @param action {@link Class}
     * @param e {@link ActionEvent}
     */
    protected void postActionPerformed(Class<? extends Command> action, ActionEvent e) {
        actionPerformed(action, e, postActionListeners);
    }

    /**
     * @param action {@link Class}
     * @param e {@link ActionEvent}
     * @param actionListeners {@link Set}
     */
    private void actionPerformed(Class<? extends Command> action, ActionEvent e, Map<String, Set<ActionListener>> actionListeners) {
        if (action != null) {
            Set<ActionListener> listenerSet = actionListeners.get(action.getName());
            if (listenerSet != null && !listenerSet.isEmpty()) {
                ActionListener[] listeners = listenerSet.toArray(new ActionListener[listenerSet.size()]);
                for (ActionListener listener : listeners) {
                    listener.actionPerformed(e);
                }
            }
        }
    }

    private static List<String> findClassesThatExtend(String className, String excluding, String[] searchPath) throws IOException, ClassNotFoundException {

        return ClassFinder.findClassesThatExtend(
                searchPath, // strPathsOrJars - pathNames or jar files to search for classes
                new Class[] { Class.forName(className) },
                false, // innerClasses - should we include inner classes?
                null, // contains - className should contain this string
                // Ignore the classes which are specific to the reporting tool
                excluding, // notContains - className should not contain this string
                false); // annotations - true if classNames are annotations
    }

    private static Optional<String[]> getCodeSourceSearchPath() {
        CodeSource codeSource = ActionRouter.class.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            try {
                URL ownLocation = codeSource.getLocation();
                File ownPath = new File(ownLocation.toURI());
                if (ownPath.exists()) {
                    return Optional.of(new String[] { ownPath.getAbsolutePath() });
                }
            } catch (URISyntaxException ex) {
                log.debug("Can't get location for class sources", ex);
            }
        }
        return Optional.empty();
    }

    /**
     * Only for use by the JMeter.startGui.
     * This method must not be called by getInstance() as was done previously.
     * See <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=58790">Bug 58790</a> 
     */
    public void populateCommandMap() {
        if (!commands.isEmpty()) {
            return; // already done
        }
        try {
            List<String> listClasses = findClassesThatExtend("org.apache.jmeter.gui.action.Command", // $NON-NLS-1$
                    "org.apache.jmeter.report.gui", // $NON-NLS-1$
                    JMeterUtils.getSearchPaths());

            if (listClasses.isEmpty()) {
                //fallback
                Optional<String[]> codeSourceSearchPath = getCodeSourceSearchPath();
                if (codeSourceSearchPath.isPresent()) {
                    log.info("Using fallback search path");
                    listClasses = findClassesThatExtend("org.apache.jmeter.gui.action.Command", // $NON-NLS-1$
                            "org.apache.jmeter.report.gui", // $NON-NLS-1$
                            codeSourceSearchPath.get());
                }
            }

            if (listClasses.isEmpty()) {
                log.error("!!!!!Uh-oh, didn't find any action handlers!!!!!");
                throw new JMeterError("No action handlers found - check JMeterHome and libraries");
            }
            for (String strClassName : listClasses) {
                Class<?> commandClass = Class.forName(strClassName);
                Command command = (Command) commandClass.newInstance();
                for (String commandName : command.getActionNames()) {
                    Set<Command> commandObjects = commands.computeIfAbsent(commandName, k -> new HashSet<>());
                    commandObjects.add(command);
                }
            }
        } catch (HeadlessException e) {
            if (log.isWarnEnabled()) {
                log.warn("AWT headless exception occurred. {}", e.toString());
            }
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
        return INSTANCE;
    }
}

