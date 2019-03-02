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

package org.apache.jmeter.protocol.mongodb.mongo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

/**
 */
public class MongoDB {

    private static final Logger log = LoggerFactory.getLogger(MongoDB.class);

    // Mongo is Thread Safe
    private Mongo mongo = null;

    public MongoDB(
            List<ServerAddress> serverAddresses,
            MongoClientOptions mongoOptions) {
        mongo = new MongoClient(serverAddresses, mongoOptions);
    }

    public DB getDB(String database, String username, String password) {

        if(log.isDebugEnabled()) {
            log.debug("username: " + username+", password: " + password+", database: " + database);
        }
        DB db = mongo.getDB(database);
        boolean authenticated = db.isAuthenticated();

        if(!authenticated) {
            if(username != null && password != null && username.length() > 0 && password.length() > 0) {
                authenticated = db.authenticate(username, password.toCharArray());
            }
        }
        if(log.isDebugEnabled()) {
            log.debug("authenticated: " + authenticated);
        }
        return db;
    }

    public void clear() {
        if(log.isDebugEnabled()) {
            log.debug("clearing");
        }

        mongo.close();
        //there is no harm in trying to clear up
        mongo = null;
    }
}
