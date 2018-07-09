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

import org.apache.jmeter.threads.JMeterContextService;

import java.util.*;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * Public API to access MongoDB {@link DB} object created by {@link MongoSourceElement}
 */
public final class MongoDBHolder {

    /**
     * Get access to MongoDB object
     * @param mongoUrl String MongoDB source
     * @param dbName Mongo DB database name
     * @return {@link DB}
     */
    public static DB getDBFromSource(String mongoUrl, String dbName) {
        return getDBFromSource(mongoUrl, dbName, null, null);
    }
    
    /**
     * Get access to MongoDB object
     * @param mongoUrl String MongoDB source
     * @param dbName Mongo DB database name
     * @param login name to use for login
     * @param password password to use for login
     * @return {@link DB}
     */
    public static DB getDBFromSource(String mongoUrl, String dbName, String login, String password) {
        MongoClientURI uri;
        MongoClient client;
        if (login==null || password==null) {
            uri = new MongoClientURI(mongoUrl);
            client = new MongoClient(uri);
        } else {
            MongoCredential credential = MongoCredential.createCredential(login, dbName, password.toCharArray());
            client = new MongoClient(new ServerAddress(mongoUrl), Arrays.asList(credential));
        }
        if(client==null) {
            throw new IllegalStateException("You didn't define variable:"+mongoUrl +" using MongoDB Source Config (property:MongoDB Source)");
        }
        return client.getDB(dbName);
    }
}
