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

import org.apache.jmeter.protocol.mongodb.mongo.MongoDB;
import org.apache.jmeter.threads.JMeterContextService;

import com.mongodb.DB;

/**
 * Public API to access MongoDB {@link DB} object created by {@link MongoSourceElement}
 */
public final class MongoDBHolder {

    /**
     * Get access to MongoDB object
     * @param varName String MongoDB source
     * @param dbName Mongo DB database name
     * @return {@link DB}
     */
    public static DB getDBFromSource(String varName, String dbName) {
        return getDBFromSource(varName, dbName, null, null);
    }
    
    /**
     * Get access to MongoDB object
     * @param varName String MongoDB source
     * @param dbName Mongo DB database name
     * @param login name to use for login
     * @param password password to use for login
     * @return {@link DB}
     */
    public static DB getDBFromSource(String varName, String dbName, String login, String password) {
        MongoDB mongodb = (MongoDB) JMeterContextService.getContext().getVariables().getObject(varName);
        if(mongodb==null) {
            throw new IllegalStateException("You didn't define variable:"+varName +" using MongoDB Source Config (property:MongoDB Source)");
        }
        return mongodb.getDB(dbName, login, password);
    }
}
