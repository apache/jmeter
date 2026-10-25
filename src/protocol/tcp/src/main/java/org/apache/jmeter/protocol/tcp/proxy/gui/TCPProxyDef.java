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

import org.apache.jmeter.protocol.tcp.sampler.BinaryTCPClientImpl;
import org.apache.jmeter.protocol.tcp.sampler.LengthPrefixedBinaryTCPClientImpl;
import org.apache.jmeter.protocol.tcp.sampler.TCPClientImpl;

public enum TCPProxyDef {
    TCPClientImpl_class(TCPClientImpl.class.getName()),
    BinaryTCPClientImpl_class(BinaryTCPClientImpl.class.getName()),
    LengthPrefixedBinaryTCPClientImpl_class(LengthPrefixedBinaryTCPClientImpl.class.getName());
    private final String classPath;

    TCPProxyDef(String classPath) {
        this.classPath = classPath;
    }

    public String getClassPath() {
        return classPath;
    }

    public static TCPProxyDef findByName(String str) {
        for (TCPProxyDef value : TCPProxyDef.values()) {
            if (value.name().equals(str)) {
                return value;
            }
        }
        return null;
    }

    public static TCPProxyDef findByClassPath(String classPath) {
        for (TCPProxyDef clientClass : TCPProxyDef.values()) {
            if (clientClass.classPath.equals(classPath)) {
                return clientClass;
            }
        }
        return null;
    }

    public static int getIndexByClassPath(String classPath) {
        for (int i = 0; i < TCPProxyDef.values().length; i++) {
            if (TCPProxyDef.values()[i].classPath.equals(classPath)) {
                return i;
            }
        }
        return -1;
    }
}
