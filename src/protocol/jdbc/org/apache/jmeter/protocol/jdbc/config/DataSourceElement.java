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
 */
package org.apache.jmeter.protocol.jdbc.config;

import java.io.ObjectStreamException;

import org.apache.avalon.excalibur.datasource.ResourceLimitingJdbcDataSource;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Michael Stover
 * 
 */
public class DataSourceElement extends AbstractTestElement implements ConfigElement, TestListener, TestBean {
	private static final Logger log = LoggingManager.getLoggerForClass();

	transient String dataSource, driver, dbUrl, username, password, checkQuery, poolMax, connectionAge, timeout,
			trimInterval;

	transient boolean keepAlive, autocommit;

	transient ResourceLimitingJdbcDataSource excaliburSource;

	// TODO: why is this an object, and not a plain boolean?
	transient boolean[] started;

	public DataSourceElement() {
		started = new boolean[] { false };
	}
	
	// For serialised objects, do the same work as the constructor:
	private Object readResolve() throws ObjectStreamException {
		started = new boolean[] { false };
		return this;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded()
	 */
	public void testEnded() {
		if (started[0]) {
			synchronized (excaliburSource) {
				if (started[0]) {
					excaliburSource.dispose();
				}
			}
		}
		excaliburSource = null;
		started[0] = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
	 */
	public void testEnded(String host) {
		testEnded();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
	 */
	public void testIterationStart(LoopIterationEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted()
	 */
	public void testStarted() {
		if (!started[0]) {
			try {
                this.setRunningVersion(true);
                TestBeanHelper.prepare(this);
				initPool();
			} catch (Exception e) {
				log.error("Unable to start database connection pool.", e);
			}
		}
		getThreadContext().getVariables().putObject(getDataSource(), excaliburSource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
	 */
	public void testStarted(String host) {
		testStarted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		DataSourceElement el = (DataSourceElement) super.clone();
		el.excaliburSource = excaliburSource;
		el.started = started;
		return el;
	}

	private void initPool() throws Exception {
		excaliburSource = new ResourceLimitingJdbcDataSource();
		DefaultConfiguration config = new DefaultConfiguration("rl-jdbc");

		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer(40);
			sb.append("MaxPool: ");
			sb.append(getPoolMax());
			sb.append(" Timeout: ");
			sb.append(getTimeout());
			sb.append(" TrimInt: ");
			sb.append(getTrimInterval());
			log.debug(sb.toString());
		}
		DefaultConfiguration poolController = new DefaultConfiguration("pool-controller");
		poolController.setAttribute("max", getPoolMax());
		poolController.setAttribute("max-strict", "true");
		poolController.setAttribute("blocking", "true");
		poolController.setAttribute("timeout", getTimeout());
		poolController.setAttribute("trim-interval", getTrimInterval());
		config.addChild(poolController);

		DefaultConfiguration autoCommit = new DefaultConfiguration("auto-commit");
		autoCommit.setValue(String.valueOf(isAutocommit()));
		config.addChild(autoCommit);
		
//		config.setAttribute("auto-commit", String.valueOf(isAutocommit()));

		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer(40);
			sb.append("KeepAlive: ");
			sb.append(isKeepAlive());
			sb.append(" Age: ");
			sb.append(getConnectionAge());
			sb.append(" CheckQuery: ");
			sb.append(getCheckQuery());
			log.debug(sb.toString());
		}
		DefaultConfiguration cfgKeepAlive = new DefaultConfiguration("keep-alive");
		cfgKeepAlive.setAttribute("disable", String.valueOf(!isKeepAlive()));
		cfgKeepAlive.setAttribute("age", getConnectionAge());
		cfgKeepAlive.setValue(getCheckQuery());
		poolController.addChild(cfgKeepAlive);

		String _username = getUsername();
		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer(40);
			sb.append("Driver: ");
			sb.append(getDriver());
			sb.append(" DbUrl: ");
			sb.append(getDbUrl());
			sb.append(" User: ");
			sb.append(_username);
			log.debug(sb.toString());
		}
		DefaultConfiguration cfgDriver = new DefaultConfiguration("driver");
		cfgDriver.setValue(getDriver());
		config.addChild(cfgDriver);
		DefaultConfiguration cfgDbUrl = new DefaultConfiguration("dburl");
		cfgDbUrl.setValue(getDbUrl());
		config.addChild(cfgDbUrl);

		if (_username.length() > 0){
			DefaultConfiguration cfgUsername = new DefaultConfiguration("user");
			cfgUsername.setValue(_username);
			config.addChild(cfgUsername);
			DefaultConfiguration cfgPassword = new DefaultConfiguration("password");
			cfgPassword.setValue(getPassword());
			config.addChild(cfgPassword);
		}

		// log is required to ensure errors are available
		excaliburSource.enableLogging(new LogKitLogger(log));
		excaliburSource.configure(config);
		excaliburSource.setInstrumentableName(getDataSource());
		started[0] = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.config.ConfigElement#addConfigElement(org.apache.jmeter.config.ConfigElement)
	 */
	public void addConfigElement(ConfigElement config) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.config.ConfigElement#expectsModification()
	 */
	public boolean expectsModification() {
		return false;
	}

	/**
	 * @return Returns the checkQuery.
	 */
	public String getCheckQuery() {
		return checkQuery;
	}

	/**
	 * @param checkQuery
	 *            The checkQuery to set.
	 */
	public void setCheckQuery(String checkQuery) {
		this.checkQuery = checkQuery;
	}

	/**
	 * @return Returns the connectionAge.
	 */
	public String getConnectionAge() {
		return connectionAge;
	}

	/**
	 * @param connectionAge
	 *            The connectionAge to set.
	 */
	public void setConnectionAge(String connectionAge) {
		this.connectionAge = connectionAge;
	}

	/**
	 * @return Returns the dataSource.
	 */
	public String getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource
	 *            The dataSource to set.
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return Returns the dbUrl.
	 */
	public String getDbUrl() {
		return dbUrl;
	}

	/**
	 * @param dbUrl
	 *            The dbUrl to set.
	 */
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	/**
	 * @return Returns the driver.
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver
	 *            The driver to set.
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return Returns the poolMax.
	 */
	public String getPoolMax() {
		return poolMax;
	}

	/**
	 * @param poolMax
	 *            The poolMax to set.
	 */
	public void setPoolMax(String poolMax) {
		this.poolMax = poolMax;
	}

	/**
	 * @return Returns the timeout.
	 */
	public String getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            The timeout to set.
	 */
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return Returns the trimInterval.
	 */
	public String getTrimInterval() {
		return trimInterval;
	}

	/**
	 * @param trimInterval
	 *            The trimInterval to set.
	 */
	public void setTrimInterval(String trimInterval) {
		this.trimInterval = trimInterval;
	}

	/**
	 * @return Returns the username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            The username to set.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return Returns the autocommit.
	 */
	public boolean isAutocommit() {
		return autocommit;
	}

	/**
	 * @param autocommit
	 *            The autocommit to set.
	 */
	public void setAutocommit(boolean autocommit) {
		this.autocommit = autocommit;
	}

	/**
	 * @return Returns the keepAlive.
	 */
	public boolean isKeepAlive() {
		return keepAlive;
	}

	/**
	 * @param keepAlive
	 *            The keepAlive to set.
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
}
