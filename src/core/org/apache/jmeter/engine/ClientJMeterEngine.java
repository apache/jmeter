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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author unattributed
 * @version $Revision$ Updated on: $Date$
 */
public class ClientJMeterEngine implements JMeterEngine, Runnable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	RemoteJMeterEngine remote;

	HashTree test;

	SearchByClass testListeners;

	ConvertListeners sampleListeners;

	private String host;

	private static RemoteJMeterEngine getEngine(String h) throws MalformedURLException, RemoteException,
			NotBoundException {
		return (RemoteJMeterEngine) Naming.lookup("//" + h + "/JMeterEngine"); // $NON-NLS-1$ $NON-NLS-2$
	}

	public ClientJMeterEngine(String host) throws MalformedURLException, NotBoundException, RemoteException {
		this(getEngine(host));
		this.host = host;
	}

	public ClientJMeterEngine(RemoteJMeterEngine remote) {
		this.remote = remote;
	}

	protected HashTree getTestTree() {
		return test;
	}

	public void configure(HashTree testTree) {
		test = testTree;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void runTest() {
		log.info("about to run remote test");
		new Thread(this).start();
		log.info("done initiating run command");
	}

	public void stopTest() {
		try {
			remote.stopTest();
		} catch (Exception ex) {
			log.error("", ex); // $NON-NLS-1$
		}
	}

	public void reset() {
		try {
			try {
				remote.reset();
			} catch (java.rmi.ConnectException e) {
				remote = getEngine(host);
				remote.reset();
			}
		} catch (Exception ex) {
			log.error("", ex); // $NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		log.info("running clientengine run method");
		testListeners = new SearchByClass(TestListener.class);
		HashTree testTree = getTestTree();
		 // TODO see bug 39792; should not do any harm to synch the code here
		// @see http://issues.apache.org/bugzilla/show_bug.cgi?id=39792
		synchronized(testTree) {
			testTree.traverse(testListeners);
			sampleListeners = new ConvertListeners();
	
			// TODO this is a temporary fix - see bug 23487
			// @see http://issues.apache.org/bugzilla/show_bug.cgi?id=23487
			try { // probably no longer needed, now that the code is synchronised
				testTree.traverse(sampleListeners);
			} catch (IndexOutOfBoundsException e) {
				log.warn("Error replacing sample listeners", e);
			}
		}
		
		try {
			JMeterContextService.startTest();
			remote.setHost(host);
			log.info("sent host =" + host);
			remote.configure(test);
			log.info("sent test");
			remote.runTest();
			log.info("sent run command");
		} catch (Exception ex) {
			log.error("", ex); // $NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.engine.JMeterEngine#exit()
	 */
	public void exit() {
		try {
			remote.exit();
		} catch (RemoteException e) {
			log.warn("Could not perform remote exit: " + e.toString());
		}
	}
}
