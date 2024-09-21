/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.MenuElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

/**
 * Restart JMeter
 * Based on https://dzone.com/articles/programmatically-restart-java
 * @since 5.0
 */
@AutoService({
        Command.class,
        MenuCreator.class
})
public class Restart extends AbstractActionWithNoRunningTest implements MenuCreator {
    private static final Logger log = LoggerFactory.getLogger(Restart.class);

    /**
     * Sun property pointing the main class and its arguments.
     * Might not be defined on non Hotspot VM implementations.
     */
    public static final String SUN_JAVA_COMMAND = "sun.java.command";

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.RESTART);
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doActionAfterCheck(ActionEvent e) {
        try {
            GuiPackage guiPackage = GuiPackage.getInstance();
            ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CHECK_DIRTY));
            if (guiPackage.isDirty()) {
                int chosenOption =
                        JOptionPane.showConfirmDialog(guiPackage.getMainFrame(), JMeterUtils
                                .getResString("cancel_exit_to_save"), // $NON-NLS-1$
                                JMeterUtils.getResString("save?"), // $NON-NLS-1$
                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (chosenOption == JOptionPane.NO_OPTION) {
                    restartApplication(null);
                } else if (chosenOption == JOptionPane.YES_OPTION) {
                    ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SAVE));
                    if (!guiPackage.isDirty()) {
                        restartApplication(null);
                    }
                }
            } else {
                restartApplication(null);
            }
        } catch (Exception ex) {
            log.error("Error trying to restart: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("restart_error")+":\n" + ex.getLocalizedMessage(),  //$NON-NLS-1$  //$NON-NLS-2$
                    JMeterUtils.getResString("error_title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$

        }
    }

    /**
     * Restart the current Java application
     *
     * @param runBeforeRestart
     *            some custom code to be run before restarting
     */
    public static void restartApplication(Runnable runBeforeRestart) {
        String javaCommand = System.getProperty(SUN_JAVA_COMMAND);
        List<String> processArgs = new ArrayList<>();
        if(StringUtils.isEmpty(javaCommand)) {
            JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("restart_error")+":\n This command is only supported on Open JDK or Oracle JDK" ,  //$NON-NLS-1$  //$NON-NLS-2$
                    JMeterUtils.getResString("error_title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            return;
        }
        // java binary
        processArgs.add(System.getProperty("java.home") + "/bin/java");
        // vm arguments
        List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String arg : vmArguments) {
            // if it's the agent argument : we ignore it otherwise the
            // address of the old application and the new one will be in
            // conflict
            if (!arg.contains("-agentlib")) {
                processArgs.add(arg);
            }
        }

        // program main and program arguments
        String[] mainCommand = javaCommand.split(" ");
        // program main is a jar
        if (mainCommand[0].endsWith(".jar")) {
            // if it's a jar, add -jar mainJar
            processArgs.add("-jar");
            processArgs.add(new File(mainCommand[0]).getPath());
        } else {
            // else it's a .class, add the classpath and mainClass
            processArgs.add("-cp");
            processArgs.add(System.getProperty("java.class.path"));
            processArgs.add(mainCommand[0]);
        }
        // finally add program arguments
        processRemainingArgs(processArgs, mainCommand);
        log.debug("Restart with {} from [{}]", processArgs, javaCommand);
        // execute the command in a shutdown hook, to be sure that all the
        // resources have been disposed before restarting the application
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    new ProcessBuilder(processArgs).start();
                } catch (IOException e) {
                    log.error("Error calling restart command {}", processArgs, e);
                }
            }
        });
        // execute some custom code before restarting
        if (runBeforeRestart != null) {
            runBeforeRestart.run();
        }
        // exit
        System.exit(0); // NOSONAR Required

    }

    /**
     * Try to re-combine the parameters to regard spaces in file names
     * <p>
     * Java command line has no knowledge of the 'real' parameters and
     * we have to do a bit of guessing to re-assemble the parameters with
     * spaces and the drop the spaces, that should split the parameters.
     * <p>
     * So we guess, that each parameter starts with a dash ({@code -}) and
     * everything else are values, that should be stitched together.
     *
     * @param processArgs arguments to be given to ProcessBuilder
     * @param mainCommand original command line split at spaces
     */
    private static void processRemainingArgs(List<? super String> processArgs, String[] mainCommand) {
        boolean paramValue = false;
        StringBuilder partialParamValue = new StringBuilder();
        for (int i = 1; i < mainCommand.length; i++) {
            String currentPart = mainCommand[i];
            if (paramValue) {
                if (currentPart.startsWith("-")) {
                    paramValue = false;
                    processArgs.add(partialParamValue.toString());
                    partialParamValue.setLength(0);
                    processArgs.add(currentPart);
                } else {
                    partialParamValue.append(" ");
                    partialParamValue.append(currentPart);
                }
            } else {
                if (currentPart.startsWith("-")) {
                    processArgs.add(currentPart);
                } else {
                    paramValue = true;
                    partialParamValue.setLength(0);
                    partialParamValue.append(currentPart);
                }
            }
        }
        if (paramValue) {
            processArgs.add(partialParamValue.toString());
        }
    }

    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if(location == MENU_LOCATION.FILE) {

            JMenuItem menuItemIC = new JMenuItem(
                    JMeterUtils.getResString(ActionNames.RESTART), KeyEvent.VK_UNDEFINED);
            menuItemIC.setName(ActionNames.RESTART);
            menuItemIC.setActionCommand(ActionNames.RESTART);
            menuItemIC.setAccelerator(null);
            menuItemIC.addActionListener(ActionRouter.getInstance());

            return new JMenuItem[]{menuItemIC};
        }
        return new JMenuItem[0];
    }
    /**
     *
     */
    public Restart() {
        super();
    }

    @Override
    public JMenu[] getTopLevelMenus() {
        return new JMenu[0];
    }

    @Override
    public boolean localeChanged(MenuElement menu) {
        return false;
    }

    @Override
    public void localeChanged() {
        // NOOP
    }
}
