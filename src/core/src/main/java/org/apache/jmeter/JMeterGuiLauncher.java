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

package org.apache.jmeter;

import com.thoughtworks.xstream.converters.ConversionException;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.Load;
import org.apache.jmeter.gui.action.LookAndFeelCommand;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.util.FocusRequester;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JMeterUIDefaults;
import org.apache.jorphan.gui.ui.KerningOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

public class JMeterGuiLauncher {
    private static final Logger log = LoggerFactory.getLogger(JMeterGuiLauncher.class);

    /**
     * Starts up JMeter in GUI mode
     */
    static void startGui(String testFile) {
        System.out.println("================================================================================");//NOSONAR
        System.out.println("Don't use GUI mode for load testing !, only for Test creation and Test debugging.");//NOSONAR
        System.out.println("For load testing, use CLI Mode (was NON GUI):");//NOSONAR
        System.out.println("   jmeter -n -t [jmx file] -l [results file] -e -o [Path to web report folder]");//NOSONAR
        System.out.println("& increase Java Heap to meet your test requirements:");//NOSONAR
        System.out.println("   Modify current env variable HEAP=\"-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m\" in the jmeter batch file");//NOSONAR
        System.out.println("Check : https://jmeter.apache.org/usermanual/best-practices.html");//NOSONAR
        System.out.println("================================================================================");//NOSONAR
        invokeAndWait("LaF", JMeterGuiLauncher::setupLaF);

        // SplashScreen is created after LaF activation, otherwise it would cause splash flicker.
        // bug 66044 split showing splash screen from the other parts to have content in the window
        SplashScreen splash = new SplashScreen();
        splash.showScreen();
        splash.setProgress(10);
        invokeAndWait("HiDPI settings", JMeterUtils::applyHiDPIOnFonts);
        splash.setProgress(20);
        invokeAndWait("main part", () -> startGuiPartTwo(testFile, splash));
    }

    private static void invokeAndWait(String part, Runnable doRun) {
        try {
            log.debug("Setting up {}", part);
            SwingUtilities.invokeAndWait(doRun);
        } catch (InterruptedException e) {
            log.warn("Interrupted while setting up {}", part, e);
        } catch (InvocationTargetException e) {
            log.warn("Problem while setting up {}", part, e);
        }
    }

    private static void setupLaF() {
        KerningOptimizer.INSTANCE.setMaxTextLengthWithKerning(
                JMeterUtils.getPropDefault("text.kerning.max_document_size", 10000)
        );
        JMeterUIDefaults.INSTANCE.install();

        String jMeterLaf = LookAndFeelCommand.getPreferredLafCommand();
        try {
            log.info("Setting LAF to: {}", jMeterLaf);
            LookAndFeelCommand.activateLookAndFeel(jMeterLaf);
        } catch (IllegalArgumentException ex) {
            log.warn("Could not set LAF to: {}", jMeterLaf, ex);
        }
    }

    private static void startGuiPartTwo(String testFile, SplashScreen splash) {
        log.debug("Configure PluginManager");
        splash.setProgress(30);
        log.debug("Setup tree");
        JMeterTreeModel treeModel = new JMeterTreeModel();
        JMeterTreeListener treeLis = new JMeterTreeListener(treeModel);
        final ActionRouter instance = ActionRouter.getInstance();
        splash.setProgress(40);
        log.debug("populate command map");
        instance.populateCommandMap();
        splash.setProgress(60);
        treeLis.setActionHandler(instance);
        log.debug("init instance");
        splash.setProgress(70);
        GuiPackage.initInstance(treeLis, treeModel);
        splash.setProgress(80);
        log.debug("constructing main frame");
        MainFrame main = new MainFrame(treeModel, treeLis);
        splash.setProgress(100);
        ComponentUtil.centerComponentInWindow(main, 80);
        main.setLocationRelativeTo(splash);
        main.setVisible(true);
        main.toFront();
        instance.actionPerformed(new ActionEvent(main, 1, ActionNames.ADD_ALL));
        if (testFile != null) {
            try {
                File f = new File(testFile);
                log.info("Loading file: {}", f);
                FileServer.getFileServer().setBaseForScript(f);

                HashTree tree = SaveService.loadTree(f);

                GuiPackage.getInstance().setTestPlanFile(f.getAbsolutePath());

                Load.insertLoadedTree(1, tree);
            } catch (ConversionException e) {
                log.error("Failure loading test file", e);
                splash.close();
                JMeterUtils.reportErrorToUser(SaveService.CEtoString(e));
            } catch (Exception e) {
                log.error("Failure loading test file", e);
                splash.close();
                JMeterUtils.reportErrorToUser(e.toString());
            }
        } else {
            JTree jTree = GuiPackage.getInstance().getMainFrame().getTree();
            TreePath path = jTree.getPathForRow(0);
            jTree.setSelectionPath(path);
            FocusRequester.requestFocus(jTree);
        }
        splash.setProgress(100);
        splash.close();
    }
}
