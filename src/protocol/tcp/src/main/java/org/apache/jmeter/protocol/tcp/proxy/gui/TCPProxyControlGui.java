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

package org.apache.jmeter.protocol.tcp.proxy.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.net.BindException;
import java.util.Collection;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.ServerPanel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.TristateCheckBox;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.tcp.proxy.TCPProxyController;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;


public class TCPProxyControlGui extends LogicControllerGui
        implements JMeterGUIComponent, ActionListener {

    private final TCPProxyController proxyController = new TCPProxyController();
    private JMeterTreeNode defaultTreeNode;
    //+ action names
    private static final String ACTION_STOP = "stop"; // $NON-NLS-1$
    private static final String ACTION_START = "start"; // $NON-NLS-1$
    private static final String ACTION_RESTART = "restart"; // $NON-NLS-1$

    private JTextField portField;

    private JButton stop;
    private JButton start;
    private JButton restart;

    private ServerPanel serverPanel;
    private JLabeledChoice className;

    private JPanel targetNodePanel;
    private DefaultComboBoxModel<JMeterTreeNodeAndPath> targetNodesModel;
    private JComboBox<JMeterTreeNodeAndPath> targetNodes;

    private HorizontalPanel tcpSamplerOptionsPanel;

    private JCheckBox reUseConnection;
    private TristateCheckBox setNoDelay;
    private TristateCheckBox closeConnection;
    private JTextField soLinger;
    private JTextField eolByte;

    public TCPProxyControlGui() {
        super();
        init();
    }

    private void init() {
        defaultTreeNode = GuiPackage.getInstance().getCurrentNode();
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        JPanel mainPanel = new VerticalPanel();
        mainPanel.add(createControlsPanel());
        mainPanel.add(createClassNamePanel());
        mainPanel.add(createTargetNodePanel());
        mainPanel.add(createProxyPanel());
        mainPanel.add(createTcpSamplerConfigPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    public String getStaticLabel() {
        return "TCP Record Proxy";
    }


    @Override
    public TestElement createTestElement() {
        modifyTestElement(proxyController);
        return proxyController;
    }

    @Override
    public Collection<String> getMenuCategories() {
        return Collections.singletonList(MenuFactory.NON_TEST_ELEMENTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        portField.setText(element.getPropertyAsString(TCPSampler.PROXY_SERVER_PORT));
        // N.B. this will be a config element, so we cannot use the getXXX() methods
        int classChoiceIndex = TCPProxyDef.getIndexByClassPath(element.getPropertyAsString(TCPSampler.CLASSNAME));
        classChoiceIndex = Math.max(classChoiceIndex, 0);
        className.setSelectedIndex(classChoiceIndex);
        freshTargetNodeComBox();
        serverPanel.setServer(element.getPropertyAsString(TCPSampler.SERVER));
        // Default to original behaviour, i.e. re-use connection
        reUseConnection.setSelected(element.getPropertyAsBoolean(TCPSampler.RE_USE_CONNECTION, TCPSampler.RE_USE_CONNECTION_DEFAULT));
        serverPanel.setPort(element.getPropertyAsString(TCPSampler.PORT));
        serverPanel.setResponseTimeout(element.getPropertyAsString(TCPSampler.TIMEOUT));
        serverPanel.setConnectTimeout(element.getPropertyAsString(TCPSampler.TIMEOUT_CONNECT));
        setNoDelay.setTristateFromProperty(element, TCPSampler.NODELAY);
        closeConnection.setTristateFromProperty(element, TCPSampler.CLOSE_CONNECTION);
        soLinger.setText(element.getPropertyAsString(TCPSampler.SO_LINGER));
        eolByte.setText(element.getPropertyAsString(TCPSampler.EOL_BYTE));
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        element.setProperty(TCPSampler.PROXY_SERVER_PORT, portField.getText());
        String classPath = TCPProxyDef.TCPClientImpl_class.getClassPath();
        if (className.getText() != null && className.getText().trim().length() != 0) {
            TCPProxyDef classChoice = TCPProxyDef.findByClassPath(className.getText());
            if (classChoice != null) {
                classPath = classChoice.getClassPath();
            } else {
                classPath = className.getText();
            }
        }
        element.setProperty(TCPSampler.CLASSNAME, classPath);
        element.setProperty(TCPSampler.SERVER, serverPanel.getServer());
        element.setProperty(TCPSampler.RE_USE_CONNECTION, reUseConnection.isSelected());
        element.setProperty(TCPSampler.PORT, serverPanel.getPort());
        setNoDelay.setPropertyFromTristate(element, TCPSampler.NODELAY);
        element.setProperty(TCPSampler.TIMEOUT, serverPanel.getResponseTimeout());
        element.setProperty(TCPSampler.TIMEOUT_CONNECT, serverPanel.getConnectTimeout(), "");
        closeConnection.setPropertyFromTristate(element, TCPSampler.CLOSE_CONNECTION); // Don't use default for saving tristates
        element.setProperty(TCPSampler.SO_LINGER, soLinger.getText(), "");
        element.setProperty(TCPSampler.EOL_BYTE, eolByte.getText(), "");
        ((TCPProxyController) element).setTcpSamplerElement(element);
        JMeterTreeNode targetNode = defaultTreeNode;
        if (targetNodesModel.getSelectedItem() != null) {
            targetNode = ((JMeterTreeNodeAndPath) targetNodesModel.getSelectedItem()).getTreeNode();
        }
        ((TCPProxyController) element).setTargetNode(targetNode);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        portField.setText("");
        serverPanel.clear();
        className.setSelectedIndex(0);
        reUseConnection.setSelected(true);
        setNoDelay.setSelected(false); // TODO should this be indeterminate?
        closeConnection.setSelected(TCPSampler.CLOSE_CONNECTION_DEFAULT); // TODO should this be indeterminate?
        soLinger.setText(""); //$NON-NLS-1$
        eolByte.setText(""); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent action) {
        String command = action.getActionCommand();
        switch (command) {
            case ACTION_START:
                try {
                    startProxy();
                } catch (BindException bindException) {
                    stopStatus();
                    showErrorJOptionPane(bindException.getMessage() + "\n please set other port", "proxy server error");
                } catch (IOException ioException) {
                    stopStatus();
                    showErrorJOptionPane(ioException.getMessage(), "start proxy server error");
                } catch (IllegalThreadStateException threadStateException) {
                    stopStatus();
                    showErrorJOptionPane(threadStateException.getMessage() + "\nIllegalThreadStateException ", "start proxy server error");
                }
                break;
            case ACTION_STOP:
                try {
                    stopProxy();
                } catch (IOException ioException) {
                    showErrorJOptionPane(ioException.getMessage() + "\nplease contact to administrator", "stop proxy server error");
                }
                break;
            case ACTION_RESTART:
                try {
                    reStartProxy();
                } catch (BindException bindException) {
                    showErrorJOptionPane(bindException.getMessage() + "\n please set other port", "proxy server error");
                } catch (IOException ioException) {
                    showErrorJOptionPane(ioException.getMessage() + "\nplease contact to administrator", "restart proxy server error");
                }
                break;
            default:
                break;
        }
    }

    public void startProxy() throws IOException {
        if (runEnvironmentCheck()) {
            this.runningStatus();
            JMeterTreeNode treeNode = GuiPackage.getInstance().getCurrentNode();
            modifyTestElement(proxyController);
            proxyController.proxyServerStart();
        }
    }

    public void stopProxy() throws IOException {
        this.stopStatus();
        proxyController.proxyServerStop();
    }

    public void reStartProxy() throws IOException {
        stopProxy();
        startProxy();
    }

    private boolean runEnvironmentCheck() {
        String portFieldText = portField.getText();
        try {
            int port = Integer.parseInt(portFieldText);
            if (port <= 0 || port > 65535) {
                return false;
            }
        } catch (Exception e) {
            showErrorJOptionPane("Proxy Port error use 0-65535 , or default 8899", "Not Support");
            return false;
        }
        return true;
    }

    private void showErrorJOptionPane(String err, String title) {
        JOptionPane.showMessageDialog(this, err, title, JOptionPane.WARNING_MESSAGE);
    }

    private void runningStatus() {
        portField.setEditable(false);
        start.setEnabled(false);
        stop.setEnabled(true);
        restart.setEnabled(true);

        targetNodePanel.setVisible(false);
        className.setVisible(false);
        serverPanel.setVisible(false);
        tcpSamplerOptionsPanel.setVisible(false);
    }

    private void stopStatus() {
        portField.setEditable(true);
        start.setEnabled(true);
        stop.setEnabled(false);
        restart.setEnabled(false);

        targetNodePanel.setVisible(true);
        className.setVisible(true);
        serverPanel.setVisible(true);
        tcpSamplerOptionsPanel.setVisible(true);
    }

    private void freshTargetNodeComBox() {
        if (GuiPackage.getInstance() == null) {
            return;
        }
        JMeterTreeNode root = (JMeterTreeNode) GuiPackage.getInstance().getTreeModel().getRoot();
        targetNodesModel.removeAllElements();
        freshTargetNodeComBox(root, root.getName(), 0);
        for (int i = 0; i < targetNodesModel.getSize(); i++) {
            JMeterTreeNodeAndPath jMeterTreeNodeAndPath = (JMeterTreeNodeAndPath) targetNodesModel.getElementAt(i);
            if (jMeterTreeNodeAndPath.getTreeNode().equals(proxyController.getTargetNode())) {
                targetNodes.setSelectedItem(jMeterTreeNodeAndPath);
                return;
            }
        }
    }


    private void freshTargetNodeComBox(JMeterTreeNode node, String parentName, int level) {
        String separator = " > ";
        if (node != null) {
            for (int i = 0; i < node.getChildCount(); i++) {
                StringBuilder name = new StringBuilder();
                JMeterTreeNode cur = (JMeterTreeNode) node.getChildAt(i);
                TestElement te = cur.getTestElement();
                if (te instanceof Controller) {
                    name.append(parentName);
                    name.append(cur.getName());
                    JMeterTreeNodeAndPath jMeterTreeNodeAndPath = new JMeterTreeNodeAndPath(cur, name.toString());
                    targetNodesModel.addElement(jMeterTreeNodeAndPath);
                    name.append(separator);
                    freshTargetNodeComBox(cur, name.toString(), level + 1);
                } else if (te instanceof TestPlan) {
                    name.append(cur.getName());
                    name.append(separator);
                    freshTargetNodeComBox(cur, name.toString(), 0);
                }
                // Ignore everything else
            }
        }
    }

    private JPanel createControlsPanel() {
        JLabel jLabel = new JLabel(JMeterUtils.getResString("port"));
        portField = new JTextField("", 5);

        start = new JButton(JMeterUtils.getResString("start")); // $NON-NLS-1$
        start.addActionListener(this);
        start.setActionCommand(ACTION_START);
        start.setEnabled(true);

        stop = new JButton(JMeterUtils.getResString("stop")); // $NON-NLS-1$
        stop.setActionCommand(ACTION_STOP);
        stop.setEnabled(false);
        stop.addActionListener(this);

        restart = new JButton(JMeterUtils.getResString("restart")); // $NON-NLS-1$
        restart.addActionListener(this);
        restart.setActionCommand(ACTION_RESTART);
        restart.setEnabled(false);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("proxy_general_lifecycle"))); // $NON-NLS-1$
        panel.add(jLabel);
        panel.add(portField);
        panel.add(start);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(stop);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(restart);
        return panel;
    }

    private JPanel createClassNamePanel() {
        String[] classNameStrArr = new String[TCPProxyDef.values().length];
        for (int i = 0; i < TCPProxyDef.values().length; i++) {
            classNameStrArr[i] = TCPProxyDef.values()[i].getClassPath();
        }
        className = new JLabeledChoice(JMeterUtils.getResString("tcp_classname"), classNameStrArr);
        className.setEditable(true);
        className.setSelectedIndex(0);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(className);
        return panel;
    }

    private JPanel createTargetNodePanel() {
        targetNodesModel = new DefaultComboBoxModel<>();
        targetNodes = new JComboBox<>(targetNodesModel);
        JLabel label = new JLabel(JMeterUtils.getResString("proxy_target")); // $NON-NLS-1$
        label.setLabelFor(targetNodes);
        targetNodePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetNodePanel.add(label);
        targetNodePanel.add(targetNodes, "growx, span");
        return targetNodePanel;
    }

    private JPanel createProxyPanel() {
        serverPanel = new ServerPanel();
        return serverPanel;
    }

    private HorizontalPanel createTcpSamplerConfigPanel() {
        tcpSamplerOptionsPanel = new HorizontalPanel();
        tcpSamplerOptionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
        tcpSamplerOptionsPanel.add(createClosePortPanel());
        tcpSamplerOptionsPanel.add(createCloseConnectionPanel());
        tcpSamplerOptionsPanel.add(createNoDelayPanel());
        tcpSamplerOptionsPanel.add(createSoLingerOption());
        tcpSamplerOptionsPanel.add(createEolBytePanel());
        return tcpSamplerOptionsPanel;
    }

    private JPanel createClosePortPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("reuseconnection")); //$NON-NLS-1$
        reUseConnection = new JCheckBox("", true);
        reUseConnection.addItemListener(e -> {
            closeConnection.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        });
        label.setLabelFor(reUseConnection);
        JPanel closePortPanel = new JPanel(new FlowLayout());
        closePortPanel.add(label);
        closePortPanel.add(reUseConnection);
        return closePortPanel;
    }

    private JPanel createCloseConnectionPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("closeconnection")); // $NON-NLS-1$
        closeConnection = new TristateCheckBox("", TCPSampler.CLOSE_CONNECTION_DEFAULT);
        label.setLabelFor(closeConnection);
        JPanel closeConnectionPanel = new JPanel(new FlowLayout());
        closeConnectionPanel.add(label);
        closeConnectionPanel.add(closeConnection);
        return closeConnectionPanel;
    }

    private JPanel createSoLingerOption() {
        JLabel label = new JLabel(JMeterUtils.getResString("solinger")); //$NON-NLS-1$
        soLinger = new JTextField(5); // 5 columns size
        soLinger.setMaximumSize(new Dimension(soLinger.getPreferredSize()));
        label.setLabelFor(soLinger);
        JPanel soLingerPanel = new JPanel(new FlowLayout());
        soLingerPanel.add(label);
        soLingerPanel.add(soLinger);
        return soLingerPanel;
    }

    private JPanel createNoDelayPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("tcp_nodelay")); // $NON-NLS-1$
        setNoDelay = new TristateCheckBox();
        label.setLabelFor(setNoDelay);
        JPanel nodelayPanel = new JPanel(new FlowLayout());
        nodelayPanel.add(label);
        nodelayPanel.add(setNoDelay);
        return nodelayPanel;
    }

    private JPanel createEolBytePanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("eolbyte")); //$NON-NLS-1$
        eolByte = new JTextField(3); // 3 columns size
        eolByte.setMaximumSize(new Dimension(eolByte.getPreferredSize()));
        label.setLabelFor(eolByte);
        JPanel eolBytePanel = new JPanel(new FlowLayout());
        eolBytePanel.add(label);
        eolBytePanel.add(eolByte);
        return eolBytePanel;
    }

    public static class JMeterTreeNodeAndPath {
        private final JMeterTreeNode treeNode;
        private final String path;

        public JMeterTreeNodeAndPath(JMeterTreeNode treeNode, String path) {
            this.treeNode = treeNode;
            this.path = path;
        }

        public JMeterTreeNode getTreeNode() {
            return treeNode;
        }

        @Override
        public String toString() {
            return path;
        }
    }
}
