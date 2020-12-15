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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.protocol.bolt.config.BoltConnectionElement;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

@TestElementMetadata(labelResource = "displayName")
public class BoltSampler extends AbstractBoltTestElement implements Sampler, TestBean, ConfigMergabilityIndicator {

    private static final Set<String> APPLICABLE_CONFIG_CLASSES = new HashSet<>(
            Collections.singletonList("org.apache.jmeter.config.gui.SimpleConfigGui")); // $NON-NLS-1$

    // Enables to initialize object mapper on demand
    private static class Holder {
        private static final ObjectReader OBJECT_READER = new ObjectMapper().readerFor(new TypeReference<HashMap<String, Object>>() {});
    }

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(request());
        res.setDataType(SampleResult.TEXT);
        res.setContentType("text/plain"); // $NON-NLS-1$
        res.setDataEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> params;
        try {
            params = getParamsAsMap();
        } catch (IOException ex) {
            return handleException(res, ex);
        }

        // Assume we will be successful
        res.setSuccessful(true);
        res.setResponseMessageOK();
        res.setResponseCodeOK();

        res.sampleStart();

        try {
            res.setResponseHeaders("Cypher request: " + getCypher());
            res.setResponseData(
                    execute(
                        BoltConnectionElement.getDriver(),
                        getCypher(),
                        params,
                        getSessionConfig(),
                        getTransactionConfig()),
                    StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            res = handleException(res, ex);
        } finally {
            res.sampleEnd();
        }
        return res;
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLICABLE_CONFIG_CLASSES.contains(guiClass);
    }

    private String execute(Driver driver, String cypher, Map<String, Object> params,
                           SessionConfig sessionConfig, TransactionConfig txConfig) {
        try (Session session = driver.session(sessionConfig)) {
            Result statementResult = session.run(cypher, params, txConfig);
            return response(statementResult);
        }
    }

    private SampleResult handleException(SampleResult res, Exception ex) {
        res.setResponseMessage(ex.toString());
        if (ex instanceof Neo4jException) {
            res.setResponseCode(((Neo4jException)ex).code());
        } else {
            res.setResponseCode("500");
        }
        res.setResponseData(
                ObjectUtils.defaultIfNull(ex.getMessage(), "NO MESSAGE"),
                res.getDataEncodingNoDefault());
        res.setSuccessful(false);
        return res;
    }

    private Map<String, Object> getParamsAsMap() throws IOException {
        if (getParams() != null && getParams().length() > 0) {
            return Holder.OBJECT_READER.readValue(getParams());
        } else {
            return Collections.emptyMap();
        }
    }

    private String request() {
        StringBuilder request = new StringBuilder();
        request.append("Query: \n")
                .append(getCypher())
                .append("\n")
                .append("Parameters: \n")
                .append(getParams())
                .append("\n")
                .append("Database: \n")
                .append(getDatabase())
                .append("\n")
                .append("Access Mode: \n")
                .append(getAccessMode());
        return request.toString();
    }

    private String response(Result result) {
        StringBuilder response = new StringBuilder();
        List<Record> records;
        if (isRecordQueryResults()) {
            //get records already as consume() will exhaust the stream
            records = result.list();
        } else {
            records = Collections.emptyList();
        }
        response.append("\nSummary:");
        ResultSummary summary = result.consume();
        response.append("\nConstraints Added: ")
                .append(summary.counters().constraintsAdded())
                .append("\nConstraints Removed: ")
                .append(summary.counters().constraintsRemoved())
                .append("\nContains Updates: ")
                .append(summary.counters().containsUpdates())
                .append("\nIndexes Added: ")
                .append(summary.counters().indexesAdded())
                .append("\nIndexes Removed: ")
                .append(summary.counters().indexesRemoved())
                .append("\nLabels Added: ")
                .append(summary.counters().labelsAdded())
                .append("\nLabels Removed: ")
                .append(summary.counters().labelsRemoved())
                .append("\nNodes Created: ")
                .append(summary.counters().nodesCreated())
                .append("\nNodes Deleted: ")
                .append(summary.counters().nodesDeleted())
                .append("\nRelationships Created: ")
                .append(summary.counters().relationshipsCreated())
                .append("\nRelationships Deleted: ")
                .append(summary.counters().relationshipsDeleted());
        response.append("\n\nRecords: ");
        if (isRecordQueryResults()) {
            for (Record record : records) {
                response.append("\n").append(record);
            }
        } else {
            response.append("Skipped");
            result.consume();
        }


        return response.toString();
    }
}
