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

    private final int CPS; // Characters per second to emulate

    private final SSLSocket sslSock; // Save the actual socket
    
    // Ensure we can't be called without suitable parameters
    private SlowSSLSocket(){
    	CPS=0;
    	throw new IllegalArgumentException("No such constructor");
    }

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
    	CPS=cps;
    }

	// Override so we can intercept the stream
    public OutputStream getOutputStream() throws IOException {
        return new SlowOutputStream(sslSock.getOutputStream(), CPS);
    }
    
    // Override so we can intercept the stream
    public InputStream getInputStream() throws IOException {
        return new SlowInputStream(sslSock.getInputStream(), CPS);
    }    

    // Forward all the SSLSocket methods to the input socket
    
	public void addHandshakeCompletedListener(HandshakeCompletedListener arg0) {
		sslSock.addHandshakeCompletedListener(arg0);
	}

	public boolean getEnableSessionCreation() {
		return sslSock.getEnableSessionCreation();
	}

	public String[] getEnabledCipherSuites() {
		return sslSock.getEnabledCipherSuites();
	}

	public String[] getEnabledProtocols() {
		return sslSock.getEnabledProtocols();
	}

	public boolean getNeedClientAuth() {
		return sslSock.getNeedClientAuth();
	}

	public SSLSession getSession() {
		return sslSock.getSession();
	}

	public String[] getSupportedCipherSuites() {
		return sslSock.getSupportedCipherSuites();
	}

	public String[] getSupportedProtocols() {
		return sslSock.getSupportedProtocols();
	}

	public boolean getUseClientMode() {
		return sslSock.getUseClientMode();
	}

	public boolean getWantClientAuth() {
		return sslSock.getWantClientAuth();
	}

	public void removeHandshakeCompletedListener(HandshakeCompletedListener arg0) {
		sslSock.removeHandshakeCompletedListener(arg0);
	}

	public void setEnableSessionCreation(boolean arg0) {
		sslSock.setEnableSessionCreation(arg0);
	}

	public void setEnabledCipherSuites(String[] arg0) {
		sslSock.setEnabledCipherSuites(arg0);
	}

	public void setEnabledProtocols(String[] arg0) {
		sslSock.setEnabledProtocols(arg0);
	}

	public void setNeedClientAuth(boolean arg0) {
		sslSock.setNeedClientAuth(arg0);
	}

	public void setUseClientMode(boolean arg0) {
		sslSock.setUseClientMode(arg0);
	}

	public void setWantClientAuth(boolean arg0) {
		sslSock.setWantClientAuth(arg0);
	}

	public void startHandshake() throws IOException {
		sslSock.startHandshake();		
	}

	// Also forward all the Socket methods.
	
	public void bind(SocketAddress bindpoint) throws IOException {
		sslSock.bind(bindpoint);
	}

	public synchronized void close() throws IOException {
		sslSock.close();
	}

	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		sslSock.connect(endpoint, timeout);
	}

	public void connect(SocketAddress endpoint) throws IOException {
		sslSock.connect(endpoint);
	}

	public SocketChannel getChannel() {
		return sslSock.getChannel();
	}

	public InetAddress getInetAddress() {
		return sslSock.getInetAddress();
	}

	public boolean getKeepAlive() throws SocketException {
		return sslSock.getKeepAlive();
	}

	public InetAddress getLocalAddress() {
		return sslSock.getLocalAddress();
	}

	public int getLocalPort() {
		return sslSock.getLocalPort();
	}

	public SocketAddress getLocalSocketAddress() {
		return sslSock.getLocalSocketAddress();
	}

	public boolean getOOBInline() throws SocketException {
		return sslSock.getOOBInline();
	}

	public int getPort() {
		return sslSock.getPort();
	}

	public synchronized int getReceiveBufferSize() throws SocketException {
		return sslSock.getReceiveBufferSize();
	}

	public SocketAddress getRemoteSocketAddress() {
		return sslSock.getRemoteSocketAddress();
	}

	public boolean getReuseAddress() throws SocketException {
		return sslSock.getReuseAddress();
	}

	public synchronized int getSendBufferSize() throws SocketException {
		return sslSock.getSendBufferSize();
	}

	public int getSoLinger() throws SocketException {
		return sslSock.getSoLinger();
	}

	public synchronized int getSoTimeout() throws SocketException {
		return sslSock.getSoTimeout();
	}

	public boolean getTcpNoDelay() throws SocketException {
		return sslSock.getTcpNoDelay();
	}

	public int getTrafficClass() throws SocketException {
		return sslSock.getTrafficClass();
	}

	public boolean isBound() {
		return sslSock.isBound();
	}

	public boolean isClosed() {
		return sslSock.isClosed();
	}

	public boolean isConnected() {
		return sslSock.isConnected();
	}

	public boolean isInputShutdown() {
		return sslSock.isInputShutdown();
	}

	public boolean isOutputShutdown() {
		return sslSock.isOutputShutdown();
	}

	public void sendUrgentData(int data) throws IOException {
		sslSock.sendUrgentData(data);
	}

	public void setKeepAlive(boolean on) throws SocketException {
		sslSock.setKeepAlive(on);
	}

	public void setOOBInline(boolean on) throws SocketException {
		sslSock.setOOBInline(on);
	}

	public synchronized void setReceiveBufferSize(int size) throws SocketException {
		sslSock.setReceiveBufferSize(size);
	}

	public void setReuseAddress(boolean on) throws SocketException {
		sslSock.setReuseAddress(on);
	}

	public synchronized void setSendBufferSize(int size) throws SocketException {
		sslSock.setSendBufferSize(size);
	}

	public void setSoLinger(boolean on, int linger) throws SocketException {
		sslSock.setSoLinger(on, linger);
	}

	public synchronized void setSoTimeout(int timeout) throws SocketException {
		sslSock.setSoTimeout(timeout);
	}

	public void setTcpNoDelay(boolean on) throws SocketException {
		sslSock.setTcpNoDelay(on);
	}

	public void setTrafficClass(int tc) throws SocketException {
		sslSock.setTrafficClass(tc);
	}

	public void shutdownInput() throws IOException {
		sslSock.shutdownInput();
	}

	public void shutdownOutput() throws IOException {
		sslSock.shutdownOutput();
	}

	public String toString() {
		return sslSock.toString();
	}
}