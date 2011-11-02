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

package org.apache.jmeter.protocol.http.control;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;

//For unit tests, @see TestHttpMirrorControl

/**
 * Test element that implements the Workbench HTTP Mirror function
 */
public class HttpMirrorControl extends AbstractTestElement {

    private static final long serialVersionUID = 233L;

    private transient HttpMirrorServer server;

    private static final int DEFAULT_PORT = 8080;

    // and as a string
    public static final String DEFAULT_PORT_S =
        Integer.toString(DEFAULT_PORT);// Used by GUI

    public static final String PORT = "HttpMirrorControlGui.port"; // $NON-NLS-1$

    public HttpMirrorControl() {
        initPort(DEFAULT_PORT);
    }

    public HttpMirrorControl(int port) {
        initPort(port);
    }

    private void initPort(int port){
        setProperty(new IntegerProperty(PORT, port));
    }

    public void setPort(int port) {
        initPort(port);
    }

    public void setPort(String port) {
        setProperty(PORT, port);
    }

    public int getPort() {
        return getPropertyAsInt(PORT);
    }

    public String getPortString() {
        return getPropertyAsString(PORT);
    }

    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    public void startHttpMirror() {
        server = new HttpMirrorServer(getPort());
        server.start();
        GuiPackage instance = GuiPackage.getInstance();
        if (instance != null) {
            instance.register(server);
        }
    }

    public void stopHttpMirror() {
        if (server != null) {
            server.stopServer();
            GuiPackage instance = GuiPackage.getInstance();
            if (instance != null) {
                instance.unregister(server);
            }
            try {
                server.join(1000); // wait for server to stop
            } catch (InterruptedException e) {
            }
            server = null;
        }
    }

    @Override
    public boolean canRemove() {
        return null == server;
    }

    public boolean isServerAlive(){
        return server != null && server.isAlive();
    }
}
