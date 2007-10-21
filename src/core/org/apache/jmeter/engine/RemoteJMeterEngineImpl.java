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

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.Properties;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This is the JMeter server main code.
 */
public class RemoteJMeterEngineImpl extends java.rmi.server.UnicastRemoteObject implements RemoteJMeterEngine {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private JMeterEngine backingEngine;

	private String hostName;

	public static final int DEFAULT_RMI_PORT = 
		JMeterUtils.getPropDefault("server.rmi.port", 1099); // $NON-NLS-1$

	// Should we create our own copy of the RMI registry?
	private static final boolean createServer =
		JMeterUtils.getPropDefault("server.rmi.create", true); // $NON-NLS-1$
	
	public RemoteJMeterEngineImpl() throws RemoteException {
		init(DEFAULT_RMI_PORT);
	}

	public RemoteJMeterEngineImpl(int port) throws RemoteException {
		init(port == 0 ? DEFAULT_RMI_PORT : port);
	}

	private void init(int port) throws RemoteException {
		log.info("Starting backing engine on " + port);
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
			hostName = InetAddress.getLocalHost().getHostName();
			log.info("Creating JMeter engine on host "+hostName);
			backingEngine = new StandardJMeterEngine(hostName);// see setHost()
			reg.rebind("JMeterEngine", this); // $NON-NLS-1$
			log.info("Bound to registry on port " + port);
		} catch (Exception ex) {
			log.error("rmiregistry needs to be running to start JMeter in server " + "mode\n\t" + ex.toString());
			// Throw an Exception to ensure caller knows ...
			throw new RemoteException("Cannot start. See server log file.");
		}
	}

	// TODO: is this really needed? The hostname is passed in when the engine is created
	public void setHost(String host) {
		hostName=host;
		log.info("received host: " + host);
		backingEngine.setHost(host);
	}

	/**
	 * Adds a feature to the ThreadGroup attribute of the RemoteJMeterEngineImpl
	 * object.
	 * 
	 * @param testTree
	 *            the feature to be added to the ThreadGroup attribute
	 */
	public void configure(HashTree testTree) throws RemoteException {
		log.info("received test tree");
		backingEngine.configure(testTree);
	}

	public void runTest() throws RemoteException, JMeterEngineException {
		log.info("running test");
		log.debug("This = " + this);
		long now=System.currentTimeMillis();
		System.out.println("Starting the test on host " + hostName + " @ "+new Date(now)+" ("+now+")");
		backingEngine.runTest();
	}

	public void reset() throws RemoteException {
		log.info("Reset");
		backingEngine.reset();
	}

	public void stopTest() throws RemoteException {
		log.info("Stopping test");
		backingEngine.stopTest();// TODO: askThreadsToStop() instead?
	}

	public void exit() throws RemoteException {
		log.info("Exitting");
		backingEngine.exit();
	}

	public void setProperties(Properties p) throws RemoteException {
		log.info("Applying properties "+p);
		JMeterUtils.getJMeterProperties().putAll(p);
	}
}
