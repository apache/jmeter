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

package org.apache.jmeter.protocol.tcp.proxy;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.ListedHashTree;

public class TCPProxyTest {
    private JMeterTreeModel treeModel;
    private ListedHashTree testPlanTree;

    private TCPProxyController controller;
    private TestElement tcpSamplerElement;

    public static void main(String[] args) {
        TCPProxyTest tcpProxyTest = new TCPProxyTest();
        tcpProxyTest.setUp();
        try {
            tcpProxyTest.startProxy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUp() {
        // TODO: add your jmeter home
        JMeterUtils.setJMeterHome("./"); // Or wherever you put it.
        JMeterUtils.loadJMeterProperties(JMeterUtils.getJMeterBinDir()
                + "/jmeter.properties");

        JMeterUtils.initLocale();
        TestPlan testPlan = new TestPlan();
        ThreadGroup threadGroup = new ThreadGroup();
        testPlanTree = new ListedHashTree();
        testPlanTree.add(testPlan);
        testPlanTree.add(threadGroup, testPlan);
        treeModel = GuiPackage.getInstance().getTreeModel();

        tcpSamplerElement = new ConfigTestElement();
        tcpSamplerElement.setProperty(TCPSampler.SERVER, "127.0.0.1");
        tcpSamplerElement.setProperty(TCPSampler.PORT, 8899);
        tcpSamplerElement.setProperty(TCPSampler.PROXY_SERVER_PORT, 8898);
        controller = new TCPProxyController();
        controller.setTcpSamplerElement(tcpSamplerElement);
    }

    public void startProxy() throws Exception {
        JMeterTreeNode root = (JMeterTreeNode) treeModel.getRoot();
        treeModel.addSubTree(testPlanTree, root);
        treeModel.addComponent(controller, (JMeterTreeNode) root.getChildAt(1));
        controller.setTargetNode((JMeterTreeNode) root.getChildAt(1));
        controller.proxyServerStart();
        // TODO: now ,start your tcp client connect to the proxy
    }
}
