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

package org.apache.jmeter.protocol.http.gui.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.tree.TreePath;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser.Request;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse a CURL command line and creates a test plan from it
 *
 */
public class ParseCurlCommandAction extends AbstractAction implements MenuCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseCurlCommandAction.class);
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final Set<String> commands = new HashSet<>();
    public static final String IMPORT_CURL       = "import_curl";
    static {
        commands.add(IMPORT_CURL);
    }

    /**
     * 
     */
    public ParseCurlCommandAction() {
        super();
    }

    @Override
    public void doAction(ActionEvent e) {
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CLOSE));

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        DataFlavor df = new DataFlavor(String.class, String.class.getName());
        if (clipboard.isDataFlavorAvailable(df)) {
            String curlCommand = null;
            try {
                curlCommand = (String) clipboard.getData(df);
                LOGGER.info("Transforming CURL command {}", curlCommand);
                BasicCurlParser basicCurlParser = new BasicCurlParser();
                BasicCurlParser.Request request = basicCurlParser.parse(curlCommand);
                LOGGER.info("Parsed CURL command {} into {}", curlCommand, request);
                GuiPackage guiPackage = GuiPackage.getInstance();
                guiPackage.updateCurrentNode();
                createTestPlan(e, request);
            } catch (UnsupportedFlavorException | IOException ex) {
                JMeterUtils.reportErrorToUser("Clipboard does not contain a CURL command", ex);
            } catch (Exception ex) {
                JMeterUtils.reportErrorToUser("Error creating Test Plan from CURL command:"+ex.getMessage(), ex);
            }
        } else {
            JMeterUtils.reportErrorToUser("No CURL command in clipboard");
            return;
        }
    }

    private void createTestPlan(ActionEvent e, Request request) throws MalformedURLException, IllegalUserActionException {
        GuiPackage guiPackage = GuiPackage.getInstance();

        guiPackage.clearTestPlan();
        FileServer.getFileServer().setScriptName(null);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        threadGroup.setProperty(TestElement.NAME, "Thread Group");
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);

        LoopController loopCtrl = new LoopController();
        loopCtrl.setLoops(1);
        loopCtrl.setContinueForever(false);
        threadGroup.setSamplerController(loopCtrl);

        TestPlan testPlan = new TestPlan();
        testPlan.setProperty(TestElement.NAME, "Test Plan");
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());

        HashTree tree = new HashTree();
        HashTree testPlanHT = tree.add(testPlan);
        HashTree threadGroupHT = testPlanHT.add(threadGroup);

        createHttpRequest(request, threadGroupHT);

        ResultCollector resultCollector = new ResultCollector();
        resultCollector.setProperty(TestElement.NAME, "View Results Tree");
        resultCollector.setProperty(TestElement.GUI_CLASS, ViewResultsFullVisualizer.class.getName());
        tree.add(tree.getArray()[0], resultCollector);

        final HashTree newTree = guiPackage.addSubTree(tree);
        guiPackage.updateCurrentGui();
        guiPackage.getMainFrame().getTree().setSelectionPath(
                new TreePath(((JMeterTreeNode) newTree.getArray()[0]).getPath()));
        final HashTree subTree = guiPackage.getCurrentSubTree();
        // Send different event wether we are merging a test plan into another test plan,
        // or loading a testplan from scratch
        ActionEvent actionEvent =
            new ActionEvent(subTree.get(subTree.getArray()[subTree.size() - 1]), e.getID(), ActionNames.SUB_TREE_LOADED);
        ActionRouter.getInstance().actionPerformed(actionEvent);
    }
    
    private HTTPSamplerProxy createHttpRequest(Request request, HashTree parentHT) throws MalformedURLException {
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) HTTPSamplerFactory.newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.NAME, "HTTP Request");
        httpSampler.setProtocol(new URL(request.getUrl()).getProtocol());
        httpSampler.setPath(request.getUrl());
        httpSampler.setMethod(request.getMethod());
        
        HashTree samplerHT = parentHT.add(httpSampler);
        
        HeaderManager headerManager = new HeaderManager();
        headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        headerManager.setProperty(TestElement.NAME, "HTTP HeaderManager");
        Map<String, String> map = request.getHeaders();
        
        boolean hasAcceptEncoding = false;
        for (Map.Entry<String, String> header : map.entrySet()) {
            String key = header.getKey();
            hasAcceptEncoding = hasAcceptEncoding || key.equalsIgnoreCase(ACCEPT_ENCODING);
            headerManager.getHeaders().addItem(new Header(key, header.getValue()));
        }
        if(!hasAcceptEncoding) {
            headerManager.getHeaders().addItem(new Header(ACCEPT_ENCODING, "gzip, deflate"));
        }
        if (!"GET".equals(request.getMethod())) {
            Arguments arguments = new Arguments();
            httpSampler.setArguments(arguments);
            httpSampler.addNonEncodedArgument("", request.getPostData(), "");
        }
        httpSampler.addTestElement(headerManager);
        samplerHT.add(headerManager);
        return httpSampler;
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if(location == MENU_LOCATION.HELP) {
            JMenuItem menuItemIC = new JMenuItem(
                    "Import from cURL", KeyEvent.VK_UNDEFINED);
            menuItemIC.setName(IMPORT_CURL);
            menuItemIC.setActionCommand(IMPORT_CURL);
            menuItemIC.setAccelerator(null);
            menuItemIC.addActionListener(ActionRouter.getInstance());
            return new JMenuItem[]{menuItemIC};
        }
        return new JMenuItem[0];
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
