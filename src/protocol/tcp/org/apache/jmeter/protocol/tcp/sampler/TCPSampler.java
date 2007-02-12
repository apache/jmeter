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

package org.apache.jmeter.protocol.tcp.sampler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * A sampler which understands Tcp requests.
 * 
 */
public class TCPSampler extends AbstractSampler implements ThreadListener {
	private static final Logger log = LoggingManager.getLoggerForClass();

	public final static String SERVER = "TCPSampler.server"; //$NON-NLS-1$

	public final static String PORT = "TCPSampler.port"; //$NON-NLS-1$

	public final static String FILENAME = "TCPSampler.filename"; //$NON-NLS-1$

	public final static String CLASSNAME = "TCPSampler.classname";//$NON-NLS-1$

	public final static String NODELAY = "TCPSampler.nodelay"; //$NON-NLS-1$

	public final static String TIMEOUT = "TCPSampler.timeout"; //$NON-NLS-1$

	public final static String REQUEST = "TCPSampler.request"; //$NON-NLS-1$

	public final static String RE_USE_CONNECTION = "TCPSampler.reUseConnection"; //$NON-NLS-1$

	private final static String TCPKEY = "TCP"; //$NON-NLS-1$ key for HashMap

	private final static String ERRKEY = "ERR"; //$NON-NLS-1$ key for HashMap

	// If set, this is the regex that is used to extract the status from the
	// response
	// NOT implemented yet private final static String STATUS_REGEX =
	// JMeterUtils.getPropDefault("tcp.status.regex","");

	// Otherwise, the response is scanned for these strings
	private final static String STATUS_PREFIX = JMeterUtils.getPropDefault("tcp.status.prefix", "");

	private final static String STATUS_SUFFIX = JMeterUtils.getPropDefault("tcp.status.suffix", "");

	private final static String STATUS_PROPERTIES = JMeterUtils.getPropDefault("tcp.status.properties", "");

	private final static Properties statusProps = new Properties();

	private static boolean haveStatusProps = false;

	static {
		log.debug("Protocol Handler name=" + getClassname());
		log.debug("Status prefix=" + STATUS_PREFIX);
		log.debug("Status suffix=" + STATUS_SUFFIX);
		log.debug("Status properties=" + STATUS_PROPERTIES);
		if (STATUS_PROPERTIES.length() > 0) {
			File f = new File(STATUS_PROPERTIES);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				statusProps.load(fis);
				log.debug("Successfully loaded properties");
				haveStatusProps = true;
			} catch (FileNotFoundException e) {
				log.debug("Property file not found");
			} catch (IOException e) {
				log.debug("Property file error " + e.toString());
			} finally {
				JOrphanUtils.closeQuietly(fis);
			}
		}
	}

	/** the cache of TCP Connections */
	private static ThreadLocal tp = new ThreadLocal() {
		protected Object initialValue() {
			return new HashMap();
		}
	};

	private transient TCPClient protocolHandler;

	public TCPSampler() {
		log.debug("Created " + this);
		protocolHandler = getProtocol();
		log.debug("Using Protocol Handler: " + protocolHandler.getClass().getName());
	}

	private String getError() {
		Map cp = (Map) tp.get();
		return (String) cp.get(ERRKEY);
	}

	private Socket getSocket() {
		Map cp = (Map) tp.get();
		Socket con = null;
		if (isReUseConnection()) {
			con = (Socket) cp.get(TCPKEY);
			if (con != null) {
				log.debug(this + " Reusing connection " + con); //$NON-NLS-1$
				return con;
			}
		}

		// Not in cache, so create new one and cache it
		try {
			con = new Socket(getServer(), getPort());
			con.setSoTimeout(getTimeout());
			con.setTcpNoDelay(getNoDelay());

			log.debug(this + "  Timeout " + getTimeout() + " NoDelay " + getNoDelay()); //$NON-NLS-1$
			log.debug("Created new connection " + con); //$NON-NLS-1$
			cp.put(TCPKEY, con);
		} catch (UnknownHostException e) {
			log.warn("Unknown host for " + getLabel(), e);//$NON-NLS-1$
			cp.put(ERRKEY, e.toString());
		} catch (IOException e) {
			log.warn("Could not create socket for " + getLabel(), e); //$NON-NLS-1$
			cp.put(ERRKEY, e.toString());
		}
		return con;
	}

	public String getUsername() {
		return getPropertyAsString(ConfigTestElement.USERNAME);
	}

	public String getPassword() {
		return getPropertyAsString(ConfigTestElement.PASSWORD);
	}

	public void setServer(String newServer) {
		this.setProperty(SERVER, newServer);
	}

	public String getServer() {
		return getPropertyAsString(SERVER);
	}

	public void setReUseConnection(String newServer) {
		this.setProperty(RE_USE_CONNECTION, newServer);
	}

	public boolean isReUseConnection() {
		return getPropertyAsBoolean(RE_USE_CONNECTION);
	}

	public void setPort(String newFilename) {
		this.setProperty(PORT, newFilename);
	}

	public int getPort() {
		return getPropertyAsInt(PORT);
	}

	public void setFilename(String newFilename) {
		this.setProperty(FILENAME, newFilename);
	}

	public String getFilename() {
		return getPropertyAsString(FILENAME);
	}

	public void setRequestData(String newRequestData) {
		this.setProperty(REQUEST, newRequestData);
	}

	public String getRequestData() {
		return getPropertyAsString(REQUEST);
	}

	public void setTimeout(String newTimeout) {
		this.setProperty(FILENAME, newTimeout);
	}

	public int getTimeout() {
		return getPropertyAsInt(TIMEOUT);
	}

	public void setNoDelay(String newNoDelay) {
		this.setProperty(NODELAY, newNoDelay);
	}

	public boolean getNoDelay() {
		return getPropertyAsBoolean(NODELAY);
	}

	/**
	 * Returns a formatted string label describing this sampler Example output:
	 * Tcp://Tcp.nowhere.com/pub/README.txt
	 * 
	 * @return a formatted string label describing this sampler
	 */
	public String getLabel() {
		return ("tcp://" + this.getServer() + ":" + this.getPort());//$NON-NLS-1$
	}

	private static String getClassname() {
		String className = JMeterUtils.getPropDefault("tcp.handler", "TCPClientImpl");
		return className;
	}

	private static final String protoPrefix = "org.apache.jmeter.protocol.tcp.sampler.";

	private Class getClass(String className) {
		Class c = null;
		try {
			c = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			try {
				c = Class.forName(protoPrefix + className, false, Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException e1) {
				log.error("Could not find protocol class " + className);
			}
		}
		return c;

	}

	private TCPClient getProtocol() {
		TCPClient TCPClient = null;
		Class javaClass = getClass(getClassname());
		try {
			TCPClient = (TCPClient) javaClass.newInstance();
			if (log.isDebugEnabled()) {
				log.debug(this + "Created: " + getClassname() + "@" + Integer.toHexString(TCPClient.hashCode()));
			}
		} catch (Exception e) {
			log.error(this + " Exception creating: " + getClassname(), e);
		}
		return TCPClient;
	}

	public SampleResult sample(Entry e)// Entry tends to be ignored ...
	{
		log.debug(getLabel() + " " + getFilename() + " " + getUsername() + " " + getPassword());
		SampleResult res = new SampleResult();
		boolean isSuccessful = false;
		res.setSampleLabel(getName());// Use the test element name for the
										// label
		res.setSamplerData("Host: " + getServer() + " Port: " + getPort());
		res.sampleStart();
		try {
			Socket sock = getSocket();
			if (sock == null) {
				res.setResponseCode("500");
				res.setResponseMessage(getError());
			} else {
				InputStream is = sock.getInputStream();
				OutputStream os = sock.getOutputStream();
				String req = getRequestData();
				// TODO handle filenames
				res.setSamplerData(req);
				protocolHandler.write(os, req);
				String in = protocolHandler.read(is);
				res.setResponseData(in.getBytes());
				res.setDataType(SampleResult.TEXT);
				res.setResponseCodeOK();
				res.setResponseMessage("OK");
				isSuccessful = true;
				// Reset the status code if the message contains one
				if (STATUS_PREFIX.length() > 0) {
					int i = in.indexOf(STATUS_PREFIX);
					int j = in.indexOf(STATUS_SUFFIX, i + STATUS_PREFIX.length());
					if (i != -1 && j > i) {
						String rc = in.substring(i + STATUS_PREFIX.length(), j);
						res.setResponseCode(rc);
						isSuccessful = checkResponseCode(rc);
						if (haveStatusProps) {
							res.setResponseMessage(statusProps.getProperty(rc, "Status code not found in properties"));
						} else {
							res.setResponseMessage("No status property file");
						}
					} else {
						res.setResponseCode("999");
						res.setResponseMessage("Status value not found");
						isSuccessful = false;
					}
				}
			}
		} catch (IOException ex) {
			log.debug("", ex);
			res.setResponseCode("500");
			res.setResponseMessage(ex.toString());
            closeSocket();
		} finally {
    		// Calculate response time
    		res.sampleEnd();
    
    		// Set if we were successful or not
    		res.setSuccessful(isSuccessful);

			if (!isReUseConnection()) {
				closeSocket();
			}
        }

		return res;
	}

	/**
	 * @param rc response code
	 * @return whether this represents success or not
	 */
	private boolean checkResponseCode(String rc) {
		if (rc.compareTo("400") >= 0 && rc.compareTo("499") <= 0) {
			return false;
		}
		if (rc.compareTo("500") >= 0 && rc.compareTo("599") <= 0) {
			return false;
		}
		return true;
	}

    public void threadStarted() {
        log.debug("Thread Started");
    }

    private void closeSocket() {
        Map cp = (Map) tp.get();
        Socket con = (Socket) cp.remove(TCPKEY);
        if (con != null) {
            log.debug(this + " Closing connection " + con); //$NON-NLS-1$
            try {
                con.close();
            } catch (IOException e) {
                log.warn("Error closing socket "+e);
            }
        }
    }

    public void threadFinished() {
        log.debug("Thread Finished");
        closeSocket();
    }
}
