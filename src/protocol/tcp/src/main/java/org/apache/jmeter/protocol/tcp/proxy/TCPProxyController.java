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

import java.io.IOException;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.testelement.NonTestElement;
import org.apache.jmeter.testelement.TestElement;

/**
 * Don't use Thread.start() to start a TCPProxyController.
 * use proxyServerStart()
 */
public class TCPProxyController extends GenericController implements NonTestElement {

    private TCPProxyServer tcpProxyServer;
    private TestElement tcpSamplerElement;
    private JMeterTreeNode targetNode;

    public TCPProxyController() {
    }

    private void load() {
        TCPSamplerManager samplerManager = new TCPSamplerManager(tcpSamplerElement, targetNode);
        tcpProxyServer = new TCPProxyServer(getPropertyAsInt(TCPSampler.PROXY_SERVER_PORT), samplerManager);
    }

    public void proxyServerStart() throws IOException {
        load();
        tcpProxyServer.serverStart();
    }

    public void proxyServerStop() throws IOException {
        if (tcpProxyServer == null) {
            return;
        }
        tcpProxyServer.serverStop();
    }

    public void setTcpSamplerElement(TestElement tcpSamplerElement) {
        this.tcpSamplerElement = tcpSamplerElement;
    }

    public JMeterTreeNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(JMeterTreeNode targetNode) {
        this.targetNode = targetNode;
    }
}
