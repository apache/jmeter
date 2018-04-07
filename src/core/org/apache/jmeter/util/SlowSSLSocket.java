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

package org.apache.jmeter.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 * "Slow" SSLsocket implementation to emulate dial-up modems etc
 *
 * WARNING: the class relies on overriding all superclass methods in order to apply them to the input socket.
 * Any missing methods will access the superclass socket, which will probably be in the wrong state.
 *
 */
public class SlowSSLSocket extends SSLSocket {

    private final int charactersPerSecond; // Characters per second to emulate

    private final SSLSocket sslSock; // Save the actual socket

    /**
     * Wrap an SSLSocket with slow input and output streams
     * @param sock SSLSocket to be wrapped
     * @param cps characters per second to emulate
     */
    public SlowSSLSocket(final SSLSocket sock, final int cps){
        if (cps <=0) {
            throw new IllegalArgumentException("Speed (cps) <= 0");
        }
        sslSock=sock;
        charactersPerSecond=cps;
    }

    // Override so we can intercept the stream
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new SlowOutputStream(sslSock.getOutputStream(), charactersPerSecond);
    }

    // Override so we can intercept the stream
    @Override
    public InputStream getInputStream() throws IOException {
        return new SlowInputStream(sslSock.getInputStream(), charactersPerSecond);
    }

    // Forward all the SSLSocket methods to the input socket

    @Override
    public void addHandshakeCompletedListener(HandshakeCompletedListener arg0) {
        sslSock.addHandshakeCompletedListener(arg0);
    }

    @Override
    public boolean getEnableSessionCreation() {
        return sslSock.getEnableSessionCreation();
    }

    @Override
    public String[] getEnabledCipherSuites() {
        return sslSock.getEnabledCipherSuites();
    }

    @Override
    public String[] getEnabledProtocols() {
        return sslSock.getEnabledProtocols();
    }

    @Override
    public boolean getNeedClientAuth() {
        return sslSock.getNeedClientAuth();
    }

    @Override
    public SSLSession getSession() {
        return sslSock.getSession();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslSock.getSupportedCipherSuites();
    }

    @Override
    public String[] getSupportedProtocols() {
        return sslSock.getSupportedProtocols();
    }

    @Override
    public boolean getUseClientMode() {
        return sslSock.getUseClientMode();
    }

    @Override
    public boolean getWantClientAuth() {
        return sslSock.getWantClientAuth();
    }

    @Override
    public void removeHandshakeCompletedListener(HandshakeCompletedListener arg0) {
        sslSock.removeHandshakeCompletedListener(arg0);
    }

    @Override
    public void setEnableSessionCreation(boolean arg0) {
        sslSock.setEnableSessionCreation(arg0);
    }

    @Override
    public void setEnabledCipherSuites(String[] arg0) {
        sslSock.setEnabledCipherSuites(arg0);
    }

    @Override
    public void setEnabledProtocols(String[] arg0) {
        sslSock.setEnabledProtocols(arg0);
    }

    @Override
    public void setNeedClientAuth(boolean arg0) {
        sslSock.setNeedClientAuth(arg0);
    }

    @Override
    public void setUseClientMode(boolean arg0) {
        sslSock.setUseClientMode(arg0);
    }

    @Override
    public void setWantClientAuth(boolean arg0) {
        sslSock.setWantClientAuth(arg0);
    }

    @Override
    public void startHandshake() throws IOException {
        sslSock.startHandshake();
    }

    // Also forward all the Socket methods.

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        sslSock.bind(bindpoint);
    }

    @Override
    public synchronized void close() throws IOException {
        sslSock.close();
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        sslSock.connect(endpoint, timeout);
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        sslSock.connect(endpoint);
    }

    @Override
    public SocketChannel getChannel() {
        return sslSock.getChannel();
    }

    @Override
    public InetAddress getInetAddress() {
        return sslSock.getInetAddress();
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return sslSock.getKeepAlive();
    }

    @Override
    public InetAddress getLocalAddress() {
        return sslSock.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return sslSock.getLocalPort();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return sslSock.getLocalSocketAddress();
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return sslSock.getOOBInline();
    }

    @Override
    public int getPort() {
        return sslSock.getPort();
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return sslSock.getReceiveBufferSize();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return sslSock.getRemoteSocketAddress();
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return sslSock.getReuseAddress();
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return sslSock.getSendBufferSize();
    }

    @Override
    public int getSoLinger() throws SocketException {
        return sslSock.getSoLinger();
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return sslSock.getSoTimeout();
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return sslSock.getTcpNoDelay();
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return sslSock.getTrafficClass();
    }

    @Override
    public boolean isBound() {
        return sslSock.isBound();
    }

    @Override
    public boolean isClosed() {
        return sslSock.isClosed();
    }

    @Override
    public boolean isConnected() {
        return sslSock.isConnected();
    }

    @Override
    public boolean isInputShutdown() {
        return sslSock.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return sslSock.isOutputShutdown();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        sslSock.sendUrgentData(data);
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        sslSock.setKeepAlive(on);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        sslSock.setOOBInline(on);
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        sslSock.setReceiveBufferSize(size);
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        sslSock.setReuseAddress(on);
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        sslSock.setSendBufferSize(size);
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        sslSock.setSoLinger(on, linger);
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        sslSock.setSoTimeout(timeout);
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        sslSock.setTcpNoDelay(on);
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        sslSock.setTrafficClass(tc);
    }

    @Override
    public void shutdownInput() throws IOException {
        sslSock.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        sslSock.shutdownOutput();
    }

    @Override
    public String toString() {
        return sslSock.toString();
    }
}
