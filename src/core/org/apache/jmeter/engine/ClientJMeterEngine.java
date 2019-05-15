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
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.jmeter.rmi.RmiUtils;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to run remote tests from the client JMeter and collect remote samples
 */
public class ClientJMeterEngine implements JMeterEngine {
    private static final Logger log = LoggerFactory.getLogger(ClientJMeterEngine.class);

    private static final Object LOCK = new Object();

    private RemoteJMeterEngine remote;

    private HashTree test;

    /**
     * Maybe only host or host:port
     */
    private final String hostAndPort;
    
    private static RemoteJMeterEngine getEngine(String hostAndPort) 
            throws RemoteException, NotBoundException {
        final String name = RemoteJMeterEngineImpl.JMETER_ENGINE_RMI_NAME; // $NON-NLS-1$ $NON-NLS-2$
        String host = hostAndPort;
        int port = RmiUtils.DEFAULT_RMI_PORT;
        int indexOfSeparator = hostAndPort.indexOf(':');
        if (indexOfSeparator >= 0) {
            host = hostAndPort.substring(0, indexOfSeparator);
            String portAsString = hostAndPort.substring(indexOfSeparator+1);
            port = Integer.parseInt(portAsString);
        }
        Registry registry = LocateRegistry.getRegistry(
               host, 
               port,
               RmiUtils.createClientSocketFactory());
        Remote remobj = registry.lookup(name);
        if (remobj instanceof RemoteJMeterEngine){
            final RemoteJMeterEngine rje = (RemoteJMeterEngine) remobj;
            if (remobj instanceof RemoteObject){
                RemoteObject robj = (RemoteObject) remobj;
                System.out.println("Using remote object: "+robj.getRef().remoteToString()); // NOSONAR
            }
            return rje;
        }
        throw new RemoteException("Could not find "+name);
    }

    public ClientJMeterEngine(String hostAndPort) throws NotBoundException, RemoteException {
        this.remote = getEngine(hostAndPort);
        this.hostAndPort = hostAndPort;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(HashTree testTree) {
        TreeCloner cloner = new TreeCloner(false);
        testTree.traverse(cloner);
        test = cloner.getClonedTree();
    }

    /** {@inheritDoc} */
    @Override
    public void stopTest(boolean now) {
        log.info("About to {} remote test on {}", now ? "stop" : "shutdown", hostAndPort);
        try {
            remote.rstopTest(now);
        } catch (Exception ex) {
            log.error("", ex); // $NON-NLS-1$
        }
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        try {
            try {
                remote.rreset();
            } catch (java.rmi.ConnectException e) {
                log.info("Retry reset after: {}", e.getMessage());
                remote = getEngine(hostAndPort);
                remote.rreset();
            }
        } catch (Exception ex) {
            log.error("Failed to reset remote engine", ex); // $NON-NLS-1$
        }
    }

    @Override
    public void runTest() throws JMeterEngineException {
        log.info("running clientengine run method");
        
        // See https://bz.apache.org/bugzilla/show_bug.cgi?id=55510
        JMeterContextService.clearTotalThreads();
        HashTree testTree = test;

        synchronized(testTree) {
            PreCompiler compiler = new PreCompiler(true);
            testTree.traverse(compiler);  // limit the changes to client only test elements
            JMeterContextService.initClientSideVariables(compiler.getClientSideVariables());
            testTree.traverse(new TurnElementsOn());
            testTree.traverse(new ConvertListeners());
        }

        String methodName="unknown";
        try {
            JMeterContextService.startTest();
            /*
             * Add fix for Deadlocks, see:
             * 
             * See https://bz.apache.org/bugzilla/show_bug.cgi?id=48350
            */
            File baseDirRelative = FileServer.getFileServer().getBaseDirRelative();
            String scriptName = FileServer.getFileServer().getScriptName();
            synchronized(LOCK)
            {
                methodName="rconfigure()"; // NOSONAR Used for tracing
                remote.rconfigure(testTree, hostAndPort, baseDirRelative, scriptName);
            }
            log.info("sent test to {} basedir='{}'", hostAndPort, baseDirRelative); // $NON-NLS-1$
            if(savep == null) {
                savep = new Properties();
            }
            log.info("Sending properties {}", savep);
            try {
                methodName="rsetProperties()";// NOSONAR Used for tracing
                remote.rsetProperties(toHashMapOfString(savep));
            } catch (RemoteException e) {
                log.warn("Could not set properties: {}, error:{}", savep, e.getMessage(), e);
            }
            methodName="rrunTest()";
            remote.rrunTest();
            log.info("sent run command to {}", hostAndPort);
        } catch (IllegalStateException ex) {
            log.error("Error in {} method ", methodName, ex); // $NON-NLS-1$ $NON-NLS-2$
            tidyRMI(log);
            throw ex; // Don't wrap this error - display it as is
        } catch (Exception ex) {
            log.error("Error in {} method", methodName, ex); // $NON-NLS-1$ $NON-NLS-2$
            tidyRMI(log);
            throw new JMeterEngineException("Error in "+methodName+" method "+ex, ex); // $NON-NLS-1$ $NON-NLS-2$
        }
    }

    private static final HashMap<String, String> toHashMapOfString(Properties properties) {
        return new HashMap<>(
                properties.entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey().toString(), 
                        e -> e.getValue().toString())));
    }

    /**
     * Tidy up RMI access to allow JMeter client to exit.
     * Currently just interrupts the "RMI Reaper" thread.
     * @param logger where to log the information
     */
    public static void tidyRMI(Logger logger) {
        String reaperRE = JMeterUtils.getPropDefault("rmi.thread.name", "^RMI Reaper$");
        for(Thread t : Thread.getAllStackTraces().keySet()){
            String name = t.getName();
            if (name.matches(reaperRE)) {
                logger.info("Interrupting {}", name);
                t.interrupt();
            }
        }
    }

    /** {@inheritDoc} */
    // Called by JMeter ListenToTest if remoteStop is true
    @Override
    public void exit() {
        log.info("about to exit remote server on {}", hostAndPort);
        try {
            remote.rexit();
        } catch (RemoteException e) {
            log.warn("Could not perform remote exit: " + e.toString());
        }
    }
    
    private Properties savep;
    
    /** {@inheritDoc} */
    @Override
    public void setProperties(Properties p) {
        savep = p;
        // Sent later
    }

    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * @return host or host:port
     */
    public String getHost() {
        return hostAndPort;
    }
}
