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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public class DataSourceElement extends AbstractTestElement
    implements ConfigElement, TestStateListener, TestBean {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 234L;

    private transient String dataSource;
    private transient String driver;
    private transient String dbUrl;
    private transient String username;
    private transient String password;
    private transient String checkQuery;
    private transient String poolMax;
    private transient String connectionAge;
    private transient String timeout;
    private transient String trimInterval;
    private transient String transactionIsolation;

    private transient boolean keepAlive;
    private transient boolean autocommit;

    /*
     *  The datasource is set up by testStarted and cleared by testEnded.
     *  These are called from different threads, so access must be synchronized.
     *  The same instance is called in each case.
    */
    private transient BasicDataSource dbcpDataSource;

    // Keep a record of the pre-thread pools so that they can be disposed of at the end of a test
    private transient Set<BasicDataSource> perThreadPoolSet;

    public DataSourceElement() {
    }

    @Override
    public void testEnded() {
        synchronized (this) {
            if (dbcpDataSource != null) {
                try {
                    dbcpDataSource.close();
                } catch (SQLException ex) {
                    log.error("Error closing pool:"+getName(), ex);
                }
            }
            dbcpDataSource = null;
        }
        if (perThreadPoolSet != null) {// in case
            for(BasicDataSource dsc : perThreadPoolSet){
                log.debug("Closing pool: "+ getDataSourceName()+" @"+System.identityHashCode(dsc));
                try {
                    dsc.close();
                } catch (SQLException ex) {
                    log.error("Error closing pool:"+getName(), ex);
                }
            }
            perThreadPoolSet=null;
        }
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        JMeterVariables variables = getThreadContext().getVariables();
        String poolName = getDataSource();
        if(JOrphanUtils.isBlank(poolName)) {
            throw new IllegalArgumentException("Variable Name must not be empty for element:"+getName());
        } else if (variables.getObject(poolName) != null) {
            log.error("JDBC data source already defined for: "+poolName);
        } else {
            String maxPool = getPoolMax();
            perThreadPoolSet = Collections.synchronizedSet(new HashSet<BasicDataSource>());
            if (maxPool.equals("0")){ // i.e. if we want per thread pooling
                variables.putObject(poolName, new DataSourceComponentImpl()); // pool will be created later
            } else {
                BasicDataSource src = initPool(maxPool);
                synchronized(this){
                    dbcpDataSource = src;
                    variables.putObject(poolName, new DataSourceComponentImpl(dbcpDataSource));
                }
            }
        }
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public Object clone() {
        DataSourceElement el = (DataSourceElement) super.clone();
        synchronized (this) {
            el.dbcpDataSource = dbcpDataSource;
            el.perThreadPoolSet = perThreadPoolSet;            
        }
        return el;
    }

    /*
     * Utility routine to get the connection from the pool.
     * Purpose:
     * - allows JDBCSampler to be entirely independent of the pooling classes
     * - allows the pool storage mechanism to be changed if necessary
     */
    public static Connection getConnection(String poolName) throws SQLException{
        Object poolObject = 
                JMeterContextService.getContext().getVariables().getObject(poolName);
        if (poolObject == null) {
            throw new SQLException("No pool found named: '" + poolName + "', ensure Variable Name matches Variable Name of JDBC Connection Configuration");
        } else {
            if(poolObject instanceof DataSourceComponentImpl) {
                DataSourceComponentImpl pool = (DataSourceComponentImpl) poolObject;
                return pool.getConnection();    
            } else {
                String errorMsg = "Found object stored under variable:'"+poolName
                        +"' with class:"+poolObject.getClass().getName()+" and value: '"+poolObject+" but it's not a DataSourceComponent, check you're not already using this name as another variable";
                log.error(errorMsg);
                throw new SQLException(errorMsg); 
            }
        }
    }

    /*
     * Set up the DataSource - maxPool is a parameter, so the same code can
     * also be used for setting up the per-thread pools.
    */
    private BasicDataSource initPool(String maxPool) {
        BasicDataSource dataSource = new BasicDataSource();

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder(40);
            sb.append("MaxPool: ");
            sb.append(maxPool);
            sb.append(" Timeout: ");
            sb.append(getTimeout());
            sb.append(" TrimInt: ");
            sb.append(getTrimInterval());
            sb.append(" Auto-Commit: ");
            sb.append(isAutocommit());
            log.debug(sb.toString());
        }
        int poolSize = Integer.parseInt(maxPool);
        dataSource.setMinIdle(0);
        dataSource.setInitialSize(poolSize);
        dataSource.setMaxIdle(poolSize);
        dataSource.setMaxTotal(poolSize);
        dataSource.setMaxWaitMillis(Long.parseLong(getTimeout()));

        dataSource.setDefaultAutoCommit(Boolean.valueOf(isAutocommit()));

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder(40);
            sb.append("KeepAlive: ");
            sb.append(isKeepAlive());
            sb.append(" Age: ");
            sb.append(getConnectionAge());
            sb.append(" CheckQuery: ");
            sb.append(getCheckQuery());
            log.debug(sb.toString());
        }
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setTestOnCreate(false);
        dataSource.setTestWhileIdle(false);

        if(isKeepAlive()) {
            dataSource.setTestWhileIdle(true);
            String validationQuery = getCheckQuery();
            if (StringUtils.isBlank(validationQuery)) {
                dataSource.setValidationQuery(null);
            } else {
                dataSource.setValidationQuery(validationQuery);
            }
            dataSource.setSoftMinEvictableIdleTimeMillis(Long.parseLong(getConnectionAge()));
            dataSource.setTimeBetweenEvictionRunsMillis(Integer.parseInt(getTrimInterval()));
        }

        int transactionIsolation = DataSourceElementBeanInfo.getTransactionIsolationMode(getTransactionIsolation());
        if (transactionIsolation >= 0) {
            dataSource.setDefaultTransactionIsolation(transactionIsolation);
        }

        String _username = getUsername();
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder(40);
            sb.append("Driver: ");
            sb.append(getDriver());
            sb.append(" DbUrl: ");
            sb.append(getDbUrl());
            sb.append(" User: ");
            sb.append(_username);
            log.debug(sb.toString());
        }
        dataSource.setDriverClassName(getDriver());
        dataSource.setUrl(getDbUrl());

        if (_username.length() > 0){
            dataSource.setUsername(_username);
            dataSource.setPassword(getPassword());
        }

        // log is required to ensure errors are available
        //source.enableLogging(new LogKitLogger(log));
        if(log.isDebugEnabled()) {
            log.debug("PoolConfiguration:"+this.dataSource);
        }
        return dataSource;
    }

    // used to hold per-thread singleton connection pools
    private static final ThreadLocal<Map<String, BasicDataSource>> perThreadPoolMap =
        new ThreadLocal<Map<String, BasicDataSource>>(){
        @Override
        protected Map<String, BasicDataSource> initialValue() {
            return new HashMap<>();
        }
    };

    /*
     * Wrapper class to allow getConnection() to be implemented for both shared
     * and per-thread pools.
     *
     */
    private class DataSourceComponentImpl {

        private final BasicDataSource sharedDSC;

        DataSourceComponentImpl(){
            sharedDSC=null;
        }

        DataSourceComponentImpl(BasicDataSource p_dsc){
            sharedDSC=p_dsc;
        }

        /**
         * @return Connection
         * @throws SQLException if database access error occurrs
         */
        public Connection getConnection() throws SQLException {
            Connection conn = null;
            BasicDataSource dsc = null;
            if (sharedDSC != null){ // i.e. shared pool
                dsc = sharedDSC;
            } else {
                Map<String, BasicDataSource> poolMap = perThreadPoolMap.get();
                dsc = poolMap.get(getDataSourceName());
                if (dsc == null){
                    dsc = initPool("1");
                    poolMap.put(getDataSourceName(),dsc);
                    log.debug("Storing pool: "+getName()+" @"+System.identityHashCode(dsc));
                    perThreadPoolSet.add(dsc);
                }
            }
            if (dsc != null) {
                conn=dsc.getConnection();
                int transactionIsolation = DataSourceElementBeanInfo.getTransactionIsolationMode(getTransactionIsolation());
                if (transactionIsolation >= 0 && conn.getTransactionIsolation() != transactionIsolation) {
                    try {
                        // make sure setting the new isolation mode is done in an auto committed transaction
                        conn.setTransactionIsolation(transactionIsolation);
                        log.debug("Setting transaction isolation: " + transactionIsolation + " @"
                                + System.identityHashCode(dsc));
                    } catch (SQLException ex) {
                        log.error("Could not set transaction isolation: " + transactionIsolation + " @"
                                + System.identityHashCode(dsc));
                    }   
                }
            }
            return conn;
        }
    }

    @Override
    public void addConfigElement(ConfigElement config) {
    }

    @Override
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
     * @return Returns the poolname.
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource
     *            The poolname to set.
     */
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    private String getDataSourceName() {
        return getDataSource();
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

    /**
     * @return the transaction isolation level
     */
    public String getTransactionIsolation() {
        return transactionIsolation;
    }

    /**
     * @param transactionIsolation The transaction isolation level to set. <code>NULL</code> to
     * use the default of the driver.
     */
    public void setTransactionIsolation(String transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }
}
