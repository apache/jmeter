/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.bolt.sampler;

import java.time.Duration;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionConfig;

public abstract class AbstractBoltTestElement extends AbstractTestElement {

    private String cypher;
    private String params;
    private String database;
    private String accessMode;
    private boolean recordQueryResults;
    private int txTimeout;

    public int getTxTimeout() {
        return txTimeout;
    }

    public void setTxTimeout(int txTimeout) {
        this.txTimeout = txTimeout;
    }

    public String getAccessMode() {
        if (accessMode == null) {
            return AccessMode.WRITE.toString();
        }
        return accessMode;
    }

    public void setAccessMode(String accessMode) {
        if (EnumUtils.isValidEnum(AccessMode.class, accessMode)) {
            this.accessMode = accessMode;
        }
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getCypher() {
        return cypher;
    }

    public void setCypher(String cypher) {
        this.cypher = cypher;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public boolean isRecordQueryResults() {
        return recordQueryResults;
    }

    public void setRecordQueryResults(boolean recordQueryResults) {
        this.recordQueryResults = recordQueryResults;
    }

    //returns a SessionConfig object that can be passed to the driver session
    public SessionConfig getSessionConfig() {
        SessionConfig.Builder sessionConfigBuilder = SessionConfig.builder()
                .withDefaultAccessMode(Enum.valueOf(AccessMode.class, getAccessMode()));

        if (StringUtils.isNotBlank(database)) {
            sessionConfigBuilder.withDatabase(database);
        }

        return sessionConfigBuilder.build();
    }

    //returns a TransactionConfig object that can be passed to the driver transaction
    public TransactionConfig getTransactionConfig() {
        TransactionConfig.Builder txConfigBuilder = TransactionConfig.builder();

        if (txTimeout > 0) {
            txConfigBuilder.withTimeout(Duration.ofSeconds(txTimeout));
        }

        return txConfigBuilder.build();
    }
}
