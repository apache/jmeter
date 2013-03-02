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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.mongodb.ServerAddress;

/**
 */
public class MongoUtils {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static ArrayList<ServerAddress> toServerAddresses(String connections) {

        ArrayList<ServerAddress> addresses = new ArrayList<ServerAddress>();
        try {
            for(String connection : Arrays.asList(connections.split(","))) {
                int port = 27017;
                String[] hostPort = connection.split(":");
                if(hostPort.length > 1 && hostPort[1] != null) {
                    port = Integer.parseInt(hostPort[1].trim());
                }
                addresses.add(new ServerAddress(hostPort[0], port));
            }
        }
        catch(UnknownHostException uhe) {
            if(log.isWarnEnabled()) {
                log.warn("", uhe);
            }
        }
        return addresses;
    }
}
