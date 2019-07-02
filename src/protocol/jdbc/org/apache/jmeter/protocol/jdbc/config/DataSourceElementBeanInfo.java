/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

/*
 * Created on May 15, 2004
 */
package org.apache.jmeter.protocol.jdbc.config;

import java.beans.PropertyDescriptor;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceElementBeanInfo extends BeanInfoSupport {
    private static final Logger log = LoggerFactory.getLogger(DataSourceElementBeanInfo.class);
    private static final Map<String,Integer> TRANSACTION_ISOLATION_MAP = new HashMap<>(5);
    static {
        // Will use default isolation
        TRANSACTION_ISOLATION_MAP.put("DEFAULT", Integer.valueOf(-1));
        TRANSACTION_ISOLATION_MAP.put("TRANSACTION_NONE", Integer.valueOf(Connection.TRANSACTION_NONE));
        TRANSACTION_ISOLATION_MAP.put("TRANSACTION_READ_COMMITTED", Integer.valueOf(Connection.TRANSACTION_READ_COMMITTED));
        TRANSACTION_ISOLATION_MAP.put("TRANSACTION_READ_UNCOMMITTED", Integer.valueOf(Connection.TRANSACTION_READ_UNCOMMITTED));
        TRANSACTION_ISOLATION_MAP.put("TRANSACTION_REPEATABLE_READ", Integer.valueOf(Connection.TRANSACTION_REPEATABLE_READ));
        TRANSACTION_ISOLATION_MAP.put("TRANSACTION_SERIALIZABLE", Integer.valueOf(Connection.TRANSACTION_SERIALIZABLE));
    }

    public DataSourceElementBeanInfo() {
        super(DataSourceElement.class);

        createPropertyGroup("varName", new String[] { "dataSource" });

        createPropertyGroup("pool", new String[] { "poolMax", "timeout",
                "trimInterval", "autocommit", "transactionIsolation", "initQuery"  });

        createPropertyGroup("keep-alive", new String[] { "keepAlive", "connectionAge", "checkQuery" });

        createPropertyGroup("database", new String[] { "dbUrl", "driver", "username", "password" });

        PropertyDescriptor p = property("dataSource");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("poolMax");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "0");
        p = property("timeout");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "10000");
        p = property("trimInterval");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "60000");
        p = property("autocommit");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);
        p = property("transactionIsolation");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "DEFAULT");
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        Set<String> modesSet = TRANSACTION_ISOLATION_MAP.keySet();
        String[] modes = modesSet.toArray(new String[modesSet.size()]);
        p.setValue(TAGS, modes);
        p = property("initQuery", TypeEditor.TextAreaEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("keepAlive");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);
        p = property("connectionAge");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "5000");
        p = property("checkQuery", TypeEditor.ComboStringEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setValue(TAGS, getListCheckQuery());
        p = property("dbUrl");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("driver", TypeEditor.ComboStringEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setValue(TAGS, getListJDBCDriverClass());
        p = property("username");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("password", TypeEditor.PasswordEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
    }

    /**
     * Converts a string description of a valid transaction isolation mode to the respective integer value.
     * Currently supported tags and their values are:
     * <dl>
     * <dt>DEFAULT</dt><dd>-1</dd>
     * <dt>TRANSACTION_NONE</dt><dd>{@value java.sql.Connection#TRANSACTION_NONE}</dd>
     * <dt>TRANSACTION_READ_COMMITTED</dt><dd>{@value java.sql.Connection#TRANSACTION_READ_COMMITTED}</dd>
     * <dt>TRANSACTION_READ_UNCOMMITTED</dt><dd>{@value java.sql.Connection#TRANSACTION_READ_UNCOMMITTED}</dd>
     * <dt>TRANSACTION_REPEATABLE_READ</dt><dd>{@value java.sql.Connection#TRANSACTION_REPEATABLE_READ}</dd>
     * <dt>TRANSACTION_SERIALIZABLE</dt><dd>{@value java.sql.Connection#TRANSACTION_SERIALIZABLE}</dd>
     * </dl>
     * @param tag name of the transaction isolation mode
     * @return integer value of the given transaction isolation mode
     */
    public static int getTransactionIsolationMode(String tag) {
        if (!StringUtils.isEmpty(tag)) {
            Integer isolationMode = TRANSACTION_ISOLATION_MAP.get(tag);
            if (isolationMode == null) {
                try {
                    return Integer.parseInt(tag);
                } catch (NumberFormatException e) {
                    log.warn("Illegal transaction isolation configuration '" + tag + "'");
                }
            } else {
                return isolationMode.intValue();
            }
        }
        return -1;
    }

    /**
     * Get the list of JDBC driver classname for the main databases
     * @return a String[] with the list of JDBC driver classname
     */
    private String[] getListJDBCDriverClass() {
        return JOrphanUtils.split(JMeterUtils.getPropDefault("jdbc.config.jdbc.driver.class", ""), "|"); //$NON-NLS-1$
    }

    /**
     * Get the check queries for the main databases
     * Based in https://stackoverflow.com/questions/10684244/dbcp-validationquery-for-different-databases
     * @return a String[] with the list of check queries
     */
    private String[] getListCheckQuery() {
        return JOrphanUtils.split(JMeterUtils.getPropDefault("jdbc.config.check.query", ""), "|"); //$NON-NLS-1$
    }

}
