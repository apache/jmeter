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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
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
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens a popup where user can enter a cURL command line and create a test plan from it
 * @since 5.1
 */
public class ParseCurlCommandAction extends AbstractAction implements MenuCreator, ActionListener { // NOSONAR 

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseCurlCommandAction.class);
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final Set<String> commands = new HashSet<>();
    public static final String IMPORT_CURL       = "import_curl";
    private static final String CREATE_REQUEST = "CREATE_REQUEST";
    
    static {
        commands.add(IMPORT_CURL);
    }

    private JSyntaxTextArea cURLCommandTA;
    private JLabel statusText;

    /**
     * 
     */
    public ParseCurlCommandAction() {
        super();
    }

    @Override
    public void doAction(ActionEvent e) {
        showInputDialog(e);
    }
    
    /**
     * Show popup where user can import cURL command
     * @param event {@link ActionEvent}
     */
    private final void showInputDialog(ActionEvent event) {
        EscapeDialog messageDialog = new EscapeDialog(getParentFrame(event),
                JMeterUtils.getResString("curl_import"), false); //$NON-NLS-1$
        Container contentPane = messageDialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        statusText = new JLabel("", JLabel.CENTER);
        statusText.setForeground(Color.RED);
        contentPane.add(statusText, BorderLayout.NORTH);
        
        cURLCommandTA = JSyntaxTextArea.getInstance(10, 80, false);
        cURLCommandTA.setCaretPosition(0);
        contentPane.add(JTextScrollPane.getInstance(cURLCommandTA), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
        JButton button = new JButton(JMeterUtils.getResString("curl_create_request"));
        button.setActionCommand(CREATE_REQUEST);
        button.addActionListener(this);
        buttonPanel.add(button);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        messageDialog.pack();
        ComponentUtil.centerComponentInComponent(GuiPackage.getInstance().getMainFrame(), messageDialog);
        SwingUtilities.invokeLater(() -> messageDialog.setVisible(true));
    }
    
    /**
     * Finds the first enabled node of a given type in the tree.
     *
     * @param type      class of the node to be found
     * @return the first node of the given type in the test component tree, or
     * <code>null</code> if none was found.
     */
    private JMeterTreeNode findFirstNodeOfType(Class<?> type) {
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        return treeModel.getNodesOfType(type).stream()
                .filter(JMeterTreeNode::isEnabled)
                .findFirst()
                .orElse(null);
    }

    private void createTestPlan(ActionEvent e, Request request) throws MalformedURLException, IllegalUserActionException {
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CLOSE));
        GuiPackage guiPackage = GuiPackage.getInstance();

        guiPackage.clearTestPlan();
        FileServer.getFileServer().setScriptName(null);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        threadGroup.setProperty(TestElement.NAME, "Thread Group");
        threadGroup.setNumThreads(10);
        threadGroup.setRampUp(10);
        threadGroup.setScheduler(true);
        threadGroup.setDuration(3600);
        threadGroup.setDelay(5);

        LoopController loopCtrl = new LoopController();
        loopCtrl.setLoops(-1);
        loopCtrl.setContinueForever(true);
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
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.EXPAND_ALL));
    }
    
    private HTTPSamplerProxy createHttpRequest(Request request, HashTree parentHT) throws MalformedURLException {
        HTTPSamplerProxy httpSampler = createSampler(request);

        HashTree samplerHT = parentHT.add(httpSampler);
        samplerHT.add(httpSampler.getHeaderManager());
        return httpSampler;
    }

    /**
     * @param request {@link Request}
     * @return {@link HTTPSamplerProxy}
     * @throws MalformedURLException
     */
    private HTTPSamplerProxy createSampler(Request request) throws MalformedURLException {
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) HTTPSamplerFactory.newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.NAME, "HTTP Request");
        httpSampler.setProperty(TestElement.COMMENTS, "Created from cURL on "+LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        httpSampler.setProtocol(new URL(request.getUrl()).getProtocol());
        httpSampler.setPath(request.getUrl());
        httpSampler.setUseKeepAlive(true);
        httpSampler.setFollowRedirects(true);
        httpSampler.setMethod(request.getMethod());
        if (!"GET".equals(request.getMethod())) {
            Arguments arguments = new Arguments();
            httpSampler.setArguments(arguments);
            httpSampler.addNonEncodedArgument("", request.getPostData(), "");
        }
        
        HeaderManager headerManager = createHeaderManager(request);
        httpSampler.addTestElement(headerManager);

        return httpSampler;
    }

    /**
     * @param request {@link Request}
     * @return {@link HeaderManager} element
     */
    private HeaderManager createHeaderManager(Request request) {
        HeaderManager headerManager = new HeaderManager();
        headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        headerManager.setProperty(TestElement.NAME, "HTTP HeaderManager");
        headerManager.setProperty(TestElement.COMMENTS, "Created from cURL on "+LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        Map<String, String> map = request.getHeaders();
        
        boolean hasAcceptEncoding = false;
        for (Map.Entry<String, String> header : map.entrySet()) {
            String key = header.getKey();
            hasAcceptEncoding = hasAcceptEncoding || key.equalsIgnoreCase(ACCEPT_ENCODING);
            headerManager.getHeaders().addItem(new Header(key, header.getValue()));
        }
        if(!hasAcceptEncoding && request.isCompressed()) {
            headerManager.getHeaders().addItem(new Header(ACCEPT_ENCODING, "gzip, deflate"));
        }
        return headerManager;
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if(location == MENU_LOCATION.TOOLS) {
            JMenuItem menuItemIC = new JMenuItem(
                    JMeterUtils.getResString("curl_import_menu"), KeyEvent.VK_UNDEFINED);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        statusText.setText("");
        statusText.setForeground(Color.GREEN);
        if(e.getActionCommand().equals(CREATE_REQUEST)) {
            String curlCommand = cURLCommandTA.getText();
            try {
                LOGGER.info("Transforming CURL command {}", curlCommand);
                BasicCurlParser basicCurlParser = new BasicCurlParser();
                BasicCurlParser.Request request = basicCurlParser.parse(curlCommand);
                LOGGER.info("Parsed CURL command {} into {}", curlCommand, request);
                GuiPackage guiPackage = GuiPackage.getInstance();
                guiPackage.updateCurrentNode();
                JMeterTreeNode treeNode = findFirstNodeOfType(AbstractThreadGroup.class);
                if(treeNode == null) {
                    LOGGER.info("No AbstractThreadGroup found, potentially empty plan, creating a new plan");
                    createTestPlan(e, request);
                } else {
                    JMeterTreeNode currentNode = guiPackage.getCurrentNode();
                    Object userObject = currentNode.getUserObject();
                    if (userObject instanceof Controller &&
                            ! (userObject instanceof ReplaceableController)) {
                        LOGGER.info("Newly created element will be placed under current selected node {}", currentNode.getName());
                        addToTestPlan(currentNode, request);
                    } else {
                        LOGGER.info("Newly created element will be placed under first AbstractThreadGroup node {}", treeNode.getName());
                        addToTestPlan(treeNode, request);
                    }
                }
                statusText.setText(JMeterUtils.getResString("curl_create_success"));
            } catch (Exception ex) {
                LOGGER.error("Error creating test plan from cURL command:{}, error:{}", curlCommand, ex.getMessage(), ex);
                statusText.setText(MessageFormat.format(JMeterUtils.getResString("curl_create_failure"), ex.getMessage()));
                statusText.setForeground(Color.RED);
            }
        }
    }

    private void addToTestPlan(final JMeterTreeNode currentNode, Request request) 
            throws MalformedURLException {
        final HTTPSamplerProxy sampler = createSampler(request);
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        JMeterUtils.runSafe(true, () -> {
            try {
                // We get the HeaderManager before adding component otherwise addComponent would remove it 
                HeaderManager headerManager = sampler.getHeaderManager();
                // 
                final JMeterTreeNode newNode = treeModel.addComponent(sampler, currentNode);
                treeModel.addComponent(headerManager, newNode);
            } catch (IllegalUserActionException ex) {
                LOGGER.error("Error placing sampler", ex);
                JMeterUtils.reportErrorToUser(ex.getMessage());
            }
        });
    }
}
