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

package org.apache.jmeter

import com.thoughtworks.xstream.converters.ConversionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.apache.jmeter.gui.GuiPackage
import org.apache.jmeter.gui.MainFrame
import org.apache.jmeter.gui.action.ActionNames
import org.apache.jmeter.gui.action.ActionRouter
import org.apache.jmeter.gui.action.Load
import org.apache.jmeter.gui.action.LookAndFeelCommand
import org.apache.jmeter.gui.tree.JMeterTreeListener
import org.apache.jmeter.gui.tree.JMeterTreeModel
import org.apache.jmeter.gui.util.FocusRequester
import org.apache.jmeter.save.SaveService
import org.apache.jmeter.services.FileServer
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.collections.HashTree
import org.apache.jorphan.gui.ComponentUtil
import org.apache.jorphan.gui.JMeterUIDefaults
import org.apache.jorphan.gui.ui.KerningOptimizer
import org.slf4j.LoggerFactory
import java.awt.event.ActionEvent
import java.io.File

public object JMeterGuiLauncher {
    private val log = LoggerFactory.getLogger(JMeterGuiLauncher::class.java)

    /**
     * Starts up JMeter in GUI mode
     */
    @JvmStatic
    public fun startGui(testFile: String?) {
        println("================================================================================") // NOSONAR
        println("Don't use GUI mode for load testing !, only for Test creation and Test debugging.") // NOSONAR
        println("For load testing, use CLI Mode (was NON GUI):") // NOSONAR
        println("   jmeter -n -t [jmx file] -l [results file] -e -o [Path to web report folder]") // NOSONAR
        println("& increase Java Heap to meet your test requirements:") // NOSONAR
        println("   Modify current env variable HEAP=\"-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m\" in the jmeter batch file") // NOSONAR
        println("Check : https://jmeter.apache.org/usermanual/best-practices.html") // NOSONAR
        println("================================================================================") // NOSONAR

        runBlocking {
            // See https://github.com/Kotlin/kotlinx.coroutines/blob/master/ui/coroutines-guide-ui.md
            launch(Dispatchers.Swing) {
                startGuiInternal(testFile)
            }
        }
    }

    private fun setupLaF() {
        KerningOptimizer.INSTANCE.maxTextLengthWithKerning =
            JMeterUtils.getPropDefault("text.kerning.max_document_size", 10000)
        JMeterUIDefaults.INSTANCE.install()
        val jMeterLaf = LookAndFeelCommand.getPreferredLafCommand()
        try {
            log.info("Setting LAF to: {}", jMeterLaf)
            LookAndFeelCommand.activateLookAndFeel(jMeterLaf)
        } catch (ex: IllegalArgumentException) {
            log.warn("Could not set LAF to: {}", jMeterLaf, ex)
        }
    }

    private suspend fun startGuiInternal(testFile: String?) {
        setupLaF()
        val splash = SplashScreen()
        splash.showScreen()
        yield()
        suspend fun setProgress(progress: Int) {
            splash.setProgress(progress)
            // Allow UI updates
            yield()
        }
        setProgress(1)
        JMeterUtils.applyHiDPIOnFonts()
        log.debug("Setup tree")
        setProgress(5)
        val treeModel = JMeterTreeModel()
        val treeLis = JMeterTreeListener(treeModel)
        val instance = ActionRouter.getInstance()
        setProgress(10)
        withContext(Dispatchers.Default) {
            log.debug("populate command map")
            instance.populateCommandMap()
        }
        setProgress(20)
        treeLis.setActionHandler(instance)
        log.debug("init instance")
        GuiPackage.initInstance(treeLis, treeModel)
        log.debug("constructing main frame")
        val main = MainFrame(treeModel, treeLis)
        setProgress(56)
        ComponentUtil.centerComponentInWindow(main, 80)
        setProgress(82)
        main.setLocationRelativeTo(splash)
        main.isVisible = true
        main.toFront()
        instance.actionPerformed(ActionEvent(main, 1, ActionNames.ADD_ALL))
        if (testFile != null) {
            loadFile(testFile)
        } else {
            val jTree = GuiPackage.getInstance().mainFrame.tree
            val path = jTree.getPathForRow(0)
            jTree.selectionPath = path
            FocusRequester.requestFocus(jTree)
        }
        setProgress(93)
        splash.close()
    }

    private suspend fun loadFile(testFile: String) {
        try {
            val f: File
            val tree: HashTree?
            withContext(Dispatchers.Default) {
                f = File(testFile)
                log.info("Loading file: {}", f)
                FileServer.getFileServer().setBaseForScript(f)
                tree = SaveService.loadTree(f)
            }
            GuiPackage.getInstance().testPlanFile = f.absolutePath
            Load.insertLoadedTree(1, tree)
        } catch (e: ConversionException) {
            log.error("Failure loading test file", e)
            JMeterUtils.reportErrorToUser(SaveService.CEtoString(e))
        } catch (e: Exception) {
            log.error("Failure loading test file", e)
            JMeterUtils.reportErrorToUser(e.toString())
        }
    }
}
