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

package org.apache.jmeter.protocol.http.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.ListedHashTree;

public class NonGuiProxySample {
    public static void main(String[] args) throws IllegalUserActionException,
            IOException {
        JMeterUtils.setJMeterHome("./"); // Or wherever you put it.
        JMeterUtils.loadJMeterProperties(JMeterUtils.getJMeterBinDir()
                + "/jmeter.properties");
        JMeterUtils.initLocale();

        TestPlan testPlan = new TestPlan();
        ThreadGroup threadGroup = new ThreadGroup();
        ListedHashTree testPlanTree = new ListedHashTree();
        testPlanTree.add(testPlan);
        testPlanTree.add(threadGroup, testPlan);

        @SuppressWarnings("deprecation") // deliberate use of deprecated ctor
        JMeterTreeModel treeModel = new JMeterTreeModel(new Object());

        JMeterTreeNode root = (JMeterTreeNode) treeModel.getRoot();
        treeModel.addSubTree(testPlanTree, root);

        ProxyControl proxy = new ProxyControl();
        proxy.setNonGuiTreeModel(treeModel);
        proxy.setTarget(treeModel.getNodeOf(threadGroup));
        proxy.setPort(8282);

        treeModel.addComponent(proxy, (JMeterTreeNode) root.getChildAt(1));

        proxy.startProxy();
        HttpHost proxyHost = new HttpHost("localhost", 8282);
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(
                proxyHost);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRoutePlanner(routePlanner).build();

        try {
            httpclient.execute(new HttpGet("http://example.invalid"));
        } catch (Exception e) {
            //
        }
        proxy.stopProxy();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            SaveService.saveTree(treeModel.getTestPlan(), out);
            out.close();
            System.out.println(out.toString());
        }

    }
}

