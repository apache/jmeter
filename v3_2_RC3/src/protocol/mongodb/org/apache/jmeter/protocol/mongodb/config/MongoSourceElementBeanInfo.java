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

package org.apache.jmeter.protocol.mongodb.config;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
  */
public class MongoSourceElementBeanInfo
        extends BeanInfoSupport {

    private static final Logger log = LoggerFactory.getLogger(MongoSourceElementBeanInfo.class);

    public MongoSourceElementBeanInfo() {
        super(MongoSourceElement.class);

        //http://api.mongodb.org/java/2.7.2/com/mongodb/Mongo.html
        createPropertyGroup("mongodb", new String[] {
                "connection",
                "source"});

        //http://api.mongodb.org/java/2.7.2/com/mongodb/MongoOptions.html/
        createPropertyGroup("options", new String[]{
                "autoConnectRetry",
                "connectionsPerHost",
                "connectTimeout",
                "maxAutoConnectRetryTime",
                "maxWaitTime",
                "socketTimeout",
                "socketKeepAlive",
                "threadsAllowedToBlockForConnectionMultiplier"});

        //http://api.mongodb.org/java/2.7.2/com/mongodb/MongoOptions.html/
        createPropertyGroup("writeConcern", new String[] {
                "safe",
                "fsync",
                "waitForJournaling",
                "writeOperationNumberOfServers",
                "writeOperationTimeout",
                "continueOnInsertError"});

        PropertyDescriptor p = property("connection");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("source");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");

        p = property("autoConnectRetry");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
        p = property("connectionsPerHost");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Integer.valueOf(10));
        p = property("connectTimeout");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Integer.valueOf(0));
        p = property("threadsAllowedToBlockForConnectionMultiplier");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Integer.valueOf(5));
        p = property("maxAutoConnectRetryTime");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Long.valueOf(0));
        p = property("maxWaitTime");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Integer.valueOf(120000));
        p = property("socketTimeout");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Integer.valueOf(0));
        p = property("socketKeepAlive");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);

        p = property("fsync");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
        p = property("safe");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
        p = property("waitForJournaling");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
        p = property("writeOperationNumberOfServers");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Integer.valueOf(0));
        p = property("writeOperationTimeout");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Integer.valueOf(0));
        p = property("continueOnInsertError");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);

        if(log.isDebugEnabled()) {
            for (PropertyDescriptor pd : getPropertyDescriptors()) {
                log.debug(pd.getName());
                log.debug(pd.getDisplayName());
            }
        }
    }
}
