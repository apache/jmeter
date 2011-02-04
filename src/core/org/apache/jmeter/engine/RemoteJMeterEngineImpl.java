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

package org.apache.jmeter.engine;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This is the JMeter server main code.
 */
public class RemoteJMeterEngineImpl extends java.rmi.server.UnicastRemoteObject implements RemoteJMeterEngine {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    static final String JMETER_ENGINE_RMI_NAME = "JMeterEngine"; // $NON-NLS-1$

    private transient JMeterEngine backingEngine;
    
    private transient Thread ownerThread;

    public static final int DEFAULT_RMI_PORT =
        JMeterUtils.getPropDefault("server.rmi.port", 1099); // $NON-NLS-1$

    private static final int DEFAULT_LOCAL_PORT =
        JMeterUtils.getPropDefault("server.rmi.localport", 0); // $NON-NLS-1$

    static{
        if (DEFAULT_LOCAL_PORT != 0){
            System.out.println("Using local port: "+DEFAULT_LOCAL_PORT);
        }
    }

    // Should we create our own copy of the RMI registry?
    private static final boolean createServer =
        JMeterUtils.getPropDefault("server.rmi.create", true); // $NON-NLS-1$

    private final Object LOCK = new Object();

    private RemoteJMeterEngineImpl(int port) throws RemoteException {
        super(port); // Create this object using the specified port (0 means anonymous)
        System.out.println("Created remote object: "+this.getRef().remoteToString());
    }

    public static void startServer(int port) throws RemoteException {
        RemoteJMeterEngineImpl engine = new RemoteJMeterEngineImpl(DEFAULT_LOCAL_PORT);
        engine.init(port == 0 ? DEFAULT_RMI_PORT : port);
    }

    private void init(int port) throws RemoteException {
        log.info("Starting backing engine on " + port);
        InetAddress localHost=null;
        try {
            // Bug 47980
            String host = System.getProperties().getProperty("java.rmi.server.hostname"); // $NON-NLS-1$
            if( host==null ) {
                localHost = InetAddress.getLocalHost();
            } else {
                localHost = InetAddress.getByName(host);
            }
        } catch (UnknownHostException e1) {
            throw new RemoteException("Cannot start. Unable to get local host IP address.");
        }
        log.info("IP address="+localHost.getHostAddress());
        String hostName = localHost.getHostName();
        if (localHost.isLoopbackAddress()){
            throw new RemoteException("Cannot start. "+hostName+" is a loopback address.");
        }
        log.debug("This = " + this);
        if (createServer){
            log.info("Creating RMI registry (server.rmi.create=true)");
            try {
                LocateRegistry.createRegistry(port);
            } catch (RemoteException e){
                String msg="Problem creating registry: "+e;
                log.warn(msg);
                System.err.println(msg);
                System.err.println("Continuing...");
            }
        }
        try {
            Registry reg = LocateRegistry.getRegistry(port);
            reg.rebind(JMETER_ENGINE_RMI_NAME, this);
            log.info("Bound to registry on port " + port);
        } catch (Exception ex) {
            log.error("rmiregistry needs to be running to start JMeter in server " + "mode\n\t" + ex.toString());
            // Throw an Exception to ensure caller knows ...
            throw new RemoteException("Cannot start. See server log file.");
        }
    }

    /**
     * Adds a feature to the ThreadGroup attribute of the RemoteJMeterEngineImpl
     * object.
     *
     * @param testTree
     *            the feature to be added to the ThreadGroup attribute
     */
    public void configure(HashTree testTree, String host, File jmxBase) throws RemoteException {
        log.info("Creating JMeter engine on host "+host+" base '"+jmxBase+"'");
        synchronized(LOCK) { // close window where another remote client might jump in
            if (backingEngine != null && backingEngine.isActive()) {
                log.warn("Engine is busy - cannot create JMeter engine");
                throw new IllegalStateException("Engine is busy - please try later");
            }
            ownerThread = Thread.currentThread();
            backingEngine = new StandardJMeterEngine(host);
            backingEngine.configure(testTree); // sets active = true
        }
        FileServer.getFileServer().setBase(jmxBase);
    }

    public void runTest() throws RemoteException, JMeterEngineException, IllegalStateException {
        log.info("Running test");
        checkOwner("runTest");
        backingEngine.runTest();
    }

    public void reset() throws RemoteException, IllegalStateException {
        // Mail on userlist reported NPE here - looks like only happens if there are network errors, but check anyway
        if (backingEngine != null) {
            log.info("Reset");
            checkOwner("reset");
            backingEngine.reset();
        } else {
            log.warn("Backing engine is null, ignoring reset");
        }
    }

    public void stopTest(boolean now) throws RemoteException {
        if (now) {
            log.info("Stopping test ...");
        } else {
            log.info("Shutting test ...");
        }
        backingEngine.stopTest(now);
        log.info("... stopped");
    }

    public void exit() throws RemoteException {
        log.info("Exitting");
        backingEngine.exit();
    }

    public void setProperties(Properties p) throws RemoteException, IllegalStateException {
        checkOwner("setProperties");
        backingEngine.setProperties(p);
    }

    /**
     * Check if the caller owns the engine.
     * @param methodName the name of the method for the log message
     * @throws IllegalStateException if the caller is not the owner.
     */
    private void checkOwner(String methodName) throws IllegalStateException {
        if (ownerThread != null && ownerThread != Thread.currentThread()){
            String msg = "The engine is not owned by this thread - cannot call "+methodName;
            log.warn(msg);
            throw new IllegalStateException(msg);            
        }
    }
}
