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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCPProxyServer is a tcp server.
 * TCP client message pass by,
 * and server will record the message and create TCPSamplers by TCPSamplerManager.
 * <p>
 * If you want use this server:
 * Please don't ues Thread.start(),
 * Set the proxy port and init a new TCPSamplerManager,use the TCPProxyServer.serverStart().
 * TCPProxyServer.serverStart() function will load some needs param.
 */
public class TCPProxyServer extends Thread {
    private static final Logger log = LoggerFactory.getLogger(TCPProxyServer.class);

    ServerSocket serverSocket;
    public static final int DEFAULT_PORT = 8899;
    int proxyPort = DEFAULT_PORT;
    int bufferSize = 4096;
    boolean runningFlag = true;

    private TCPSamplerManager samplerManager;

    public TCPProxyServer(int proxyPort, TCPSamplerManager samplerManager) {
        this.proxyPort = proxyPort;
        this.samplerManager = samplerManager;
    }

    public void serverStart() throws IOException {
        log.debug("proxy start");
        runningFlag = true;
        serverSocket = new ServerSocket(proxyPort);
        log.debug("proxy server socket open success");
        this.samplerManager.managerStart();
        start();
        log.debug("proxy start success");
    }

    public void serverStop() throws IOException {
        runningFlag = false;
        serverSocket.close();
        this.samplerManager.managerStop();
    }

    @Override
    public void run() {
        if (serverSocket == null || serverSocket.isClosed()) {
            try {
                serverSocket = new ServerSocket(proxyPort);
            } catch (IOException ioException) {
                log.error("start proxy fail " + ioException.getMessage());
            }
        }
        try {
            while (runningFlag) {
                Socket socket = serverSocket.accept();
                log.debug("new client connection " + socket.getInetAddress().getHostName() + ":" + socket.getPort());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            byte[] buffer = new byte[bufferSize];
                            int readFlag = socket.getInputStream().read(buffer);
                            if (readFlag != -1) {
                                byte[] dataRead = Arrays.copyOf(buffer, readFlag);
                                // read client message, copy message string, TCPSamplerManager will create a new TCPSampler
                                // and return the new TCPSampler.sample() result.
                                // sample result will be response to tcp client as byte array.
                                byte[] responseData = samplerManager.newTCPSampler(new String(dataRead));
                                socket.getOutputStream().write(responseData);
                            }
                        } catch (IOException e) {
                            log.debug("proxy socket exception " + e.getMessage());
                        } finally {
                            try {
                                socket.close();
                            } catch (IOException ignore) {
                            }
                        }
                    }
                }).start();
            }
        } catch (SocketException socketException) {
            log.debug("proxy closed ", socketException);
        } catch (IOException ioException) {
            log.debug("proxy io failed ", ioException);
        }
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
