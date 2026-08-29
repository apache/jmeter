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

import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.tcp.control.gui.TCPSamplerGui;
import org.apache.jmeter.protocol.tcp.proxy.gui.TCPProxyDef;
import org.apache.jmeter.protocol.tcp.sampler.BinaryTCPClientImpl;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCPSamplerManager is a worker for TCPProxyServer.
 * Create new TCPSampler ,return tcp client response, and save TCP samplers to jmeter tree node.
 * <p>
 * If you want use this class,
 * Please don't use Thread.start(), use the TCPSamplerManager.managerStart().
 * TCPSamplerManager.managerStart() will load some needs param before it work.
 */
public class TCPSamplerManager extends Thread {
    private static final Logger log = LoggerFactory.getLogger(TCPSamplerManager.class);

    private final JMeterTreeNode targetNode;
    private final TestElement tcpSamplerElement;
    private boolean workStatus = false;

    private final Queue<TCPSampler> tcpSamplerInfoQueue = new ArrayDeque<>();

    public TCPSamplerManager(TestElement tcpSamplerElement, JMeterTreeNode targetNode) {
        this.tcpSamplerElement = tcpSamplerElement;
        this.targetNode = targetNode;
    }

    /**
     * create a new TCPSampler and put into TCPSampler queue.
     * manager will store all TCPSampler to target JmeterTreeNode.
     *
     * @param data tcp payload
     * @return target server response payload
     */
    public byte[] newTCPSampler(String data) {
        log.debug("new sampler , payload {}", data);
        TCPSampler tcpSampler = new TCPSampler();
        TestElement element = (TestElement) tcpSamplerElement.clone();
        element.removeProperty(TCPSampler.PROXY_SERVER_PORT);
        if (useHexPayload(element)) {
            data = JOrphanUtils.baToHexString(data.getBytes());
        }
        element.setProperty(TCPSampler.REQUEST, data);
        element.setProperty(TestElement.GUI_CLASS, TCPSamplerGui.class.getName());
        element.setName("TCP Sampler");
        tcpSampler.addTestElement(element);
        tcpSamplerInfoQueue.add(tcpSampler);
        SampleResult tcpSampleResult = tcpSampler.sample();
        if (useHexPayload(element)) {
            return BinaryTCPClientImpl.hexStringToByteArray(new String(tcpSampleResult.getResponseData()));
        }
        return tcpSampleResult.getResponseData();
    }

    private boolean useHexPayload(TestElement element) {
        String tcpSamplerClassPath = element.getPropertyAsString(TCPSampler.CLASSNAME);
        TCPProxyDef samplerClass = TCPProxyDef.findByClassPath(tcpSamplerClassPath);
        if (samplerClass != null) {
            switch (samplerClass) {
                case BinaryTCPClientImpl_class:
                case LengthPrefixedBinaryTCPClientImpl_class:
                    return true;
                case TCPClientImpl_class:
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * start TCPSamplerManager working.
     */
    public synchronized void managerStart() {
        log.debug("start, target server {}", this.tcpSamplerElement.getPropertyAsString(TCPSampler.SERVER));
        workStatus = true;
        start();
    }

    /**
     * manager will stop after samplerQueue is empty
     */
    public void managerStop() {
        workStatus = false;
    }

    @Override
    public void run() {
        while (workStatus) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.warn("sleep fail,tcp samplers maybe lost,check the jmeter tree node", e);
            }
            while (!tcpSamplerInfoQueue.isEmpty()) {
                TCPSampler sampler = tcpSamplerInfoQueue.poll();
                putSamplesIntoModel(sampler);
            }
        }
    }

    /**
     * save tcp sampler to target jmeter node
     *
     * @param sampler create by TCPSamplerManager
     */
    private void putSamplesIntoModel(TCPSampler sampler) {
        try {
            GuiPackage.getInstance().getTreeModel().addComponent(sampler, targetNode);
        } catch (IllegalUserActionException ignore) {
        }
    }

}
