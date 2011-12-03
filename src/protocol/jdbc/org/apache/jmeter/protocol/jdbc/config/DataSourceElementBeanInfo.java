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

import org.apache.commons.lang.StringUtils;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class DataSourceElementBeanInfo extends BeanInfoSupport {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static Map<String,Integer> TRANSACTION_ISOLATION_MAP = new HashMap<String, Integer>(5);
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
                "trimInterval", "autocommit", "transactionIsolation"  });

        createPropertyGroup("keep-alive", new String[] { "keepAlive", "connectionAge", "checkQuery" });

        createPropertyGroup("database", new String[] { "dbUrl", "driver", "username", "password" });

        PropertyDescriptor p = property("dataSource");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("poolMax");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "10");
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
        p = property("keepAlive");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);
        p = property("connectionAge");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "5000");
        p = property("checkQuery");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "Select 1");
        p = property("dbUrl");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("driver");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("username");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("password", TypeEditor.PasswordEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
    }

    /**
     * @param tag 
     * @return int value for String
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
            }
        }
        return -1;
    }
}
