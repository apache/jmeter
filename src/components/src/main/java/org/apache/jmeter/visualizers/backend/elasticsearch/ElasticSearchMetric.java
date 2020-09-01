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

package org.apache.jmeter.visualizers.backend.elasticsearch;

import static org.apache.commons.lang3.math.NumberUtils.isCreatable;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchMetric {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchMetric.class);
    private SampleResult sampleResult;
    private String esTestMode;
    private String esTimestamp;
    private int ciBuildNumber;
    private HashMap<String, Object> json;
    private Set<String> fields;
    private boolean allReqHeaders;
    private boolean allResHeaders;

    public ElasticSearchMetric(
            SampleResult sr, String testMode, String timeStamp, int buildNumber,
            boolean parseReqHeaders, boolean parseResHeaders, Set<String> fields) {
        this.sampleResult = sr;
        this.esTestMode = testMode.trim();
        this.esTimestamp = timeStamp.trim();
        this.ciBuildNumber = buildNumber;
        this.json = new HashMap<>();
        this.allReqHeaders = parseReqHeaders;
        this.allResHeaders = parseResHeaders;
        this.fields = fields;
    }

    /**
     * This method returns the current metric as a Map(String, Object) for the provided sampleResult
     *
     * @param context BackendListenerContext
     * @return a JSON Object as Map(String, Object)
     */
    public Map<String, Object> getMetric(BackendListenerContext context) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(this.esTimestamp);

        //add all the default SampleResult parameters
        addFilteredJSON("AllThreads", this.sampleResult.getAllThreads());
        addFilteredJSON("BodySize", this.sampleResult.getBodySizeAsLong());
        addFilteredJSON("Bytes", this.sampleResult.getBytesAsLong());
        addFilteredJSON("SentBytes", this.sampleResult.getSentBytes());
        addFilteredJSON("ConnectTime", this.sampleResult.getConnectTime());
        addFilteredJSON("ContentType", this.sampleResult.getContentType());
        addFilteredJSON("DataType", this.sampleResult.getDataType());
        addFilteredJSON("ErrorCount", this.sampleResult.getErrorCount());
        addFilteredJSON("GrpThreads", this.sampleResult.getGroupThreads());
        addFilteredJSON("IdleTime", this.sampleResult.getIdleTime());
        addFilteredJSON("Latency", this.sampleResult.getLatency());
        addFilteredJSON("ResponseTime", this.sampleResult.getTime());
        addFilteredJSON("SampleCount", this.sampleResult.getSampleCount());
        addFilteredJSON("SampleLabel", this.sampleResult.getSampleLabel());
        addFilteredJSON("ThreadName", this.sampleResult.getThreadName());
        addFilteredJSON("URL", this.sampleResult.getURL());
        addFilteredJSON("ResponseCode", this.sampleResult.getResponseCode());
        addFilteredJSON("TestStartTime", JMeterContextService.getTestStartTime());
        addFilteredJSON("SampleStartTime", sdf.format(new Date(this.sampleResult.getStartTime())));
        addFilteredJSON("SampleEndTime", sdf.format(new Date(this.sampleResult.getEndTime())));
        addFilteredJSON("Timestamp", this.sampleResult.getTimeStamp());
        addFilteredJSON("InjectorHostname", InetAddress.getLocalHost().getHostName());

        // Add the details according to the mode that is set
        switch (this.esTestMode) {
            case "debug":
                addDetails();
                break;
            case "error":
                addDetails();
                break;
            case "info":
                if (!this.sampleResult.isSuccessful()) {
                    addDetails();
                }
                break;
            default:
                break;
        }

        addAssertions();
        addElapsedTime();
        addCustomFields(context);
        parseHeadersAsJsonProps(this.allReqHeaders, this.allResHeaders);

        return this.json;
    }

    /**
     * This method adds all the assertions for the current sampleResult
     */
    private void addAssertions() {
        AssertionResult[] assertionResults = this.sampleResult.getAssertionResults();
        if (assertionResults != null) {
            Map<String, Object>[] assertionArray = new HashMap[assertionResults.length];
            Integer i = 0;
            String failureMessage = "";
            boolean isFailure = false;
            for (AssertionResult assertionResult : assertionResults) {
                HashMap<String, Object> assertionMap = new HashMap<>();
                boolean failure = assertionResult.isFailure() || assertionResult.isError();
                isFailure = isFailure || assertionResult.isFailure() || assertionResult.isError();
                assertionMap.put("failure", failure);
                assertionMap.put("failureMessage", assertionResult.getFailureMessage());
                failureMessage += assertionResult.getFailureMessage() + "\n";
                assertionMap.put("name", assertionResult.getName());
                assertionArray[i] = assertionMap;
                i++;
            }
            addFilteredJSON("AssertionResults", assertionArray);
            addFilteredJSON("FailureMessage", failureMessage);
            addFilteredJSON("Success", !isFailure);
        }
    }

    /**
     * This method adds the ElapsedTime as a key:value pair in the JSON object. Also, depending on whether or not the
     * tests were launched from a CI tool (i.e Jenkins), it will add a hard-coded version of the ElapsedTime for results
     * comparison purposes
     */
    private void addElapsedTime() {
        Date elapsedTime;

        if (this.ciBuildNumber != 0) {
            elapsedTime = getElapsedTime(true);
            addFilteredJSON("BuildNumber", this.ciBuildNumber);

            if (elapsedTime != null) {
                addFilteredJSON("ElapsedTimeComparison", elapsedTime.getTime());
            }
        }

        elapsedTime = getElapsedTime(false);
        if (elapsedTime != null) {
            addFilteredJSON("ElapsedTime", elapsedTime.getTime());
        }
    }

    /**
     * Methods that add all custom fields added by the user in the Backend Listener's GUI panel
     *
     * @param context BackendListenerContext
     */
    private void addCustomFields(BackendListenerContext context) {
        Iterator<String> pluginParameters = context.getParameterNamesIterator();
        String parameter;
        while (pluginParameters.hasNext()) {
            String parameterName = pluginParameters.next();

            if (!parameterName.startsWith("es.") && context.containsParameter(parameterName)
                    && !"".equals(parameter = context.getParameter(parameterName).trim())) {
                if (isCreatable(parameter)) {
                    addFilteredJSON(parameterName, Long.parseLong(parameter));
                } else {
                    addFilteredJSON(parameterName, parameter);
                }
            }
        }
    }

    /**
     * Method that adds the request and response's body/headers
     */
    private void addDetails() {
        addFilteredJSON("RequestHeaders", this.sampleResult.getRequestHeaders());
        addFilteredJSON("RequestBody", this.sampleResult.getSamplerData());
        addFilteredJSON("ResponseHeaders", this.sampleResult.getResponseHeaders());
        addFilteredJSON("ResponseBody", this.sampleResult.getResponseDataAsString());
        addFilteredJSON("ResponseMessage", this.sampleResult.getResponseMessage());
    }

    /**
     * This method will parse the headers and look for custom variables passed through as header. It can also seperate
     * all headers into different ElasticSearch document properties by passing "true" This is a work-around the native
     * behaviour of JMeter where variables are not accessible within the backend listener.
     *
     * @param allReqHeaders boolean to determine if the user wants to separate ALL request headers into different ES JSON
     *                      properties.
     * @param allResHeaders boolean to determine if the user wants to separate ALL response headers into different ES JSON
     *                      properties.
     *                      <p>
     *                      NOTE: This will be fixed as soon as a patch comes in for JMeter to change the behaviour.
     */
    private void parseHeadersAsJsonProps(boolean allReqHeaders, boolean allResHeaders) {
        LinkedList<String[]> headersArrayList = new LinkedList<String[]>();

        if (allReqHeaders) {
            headersArrayList.add(this.sampleResult.getRequestHeaders().split("\n"));
        }

        if (allResHeaders) {
            headersArrayList.add(this.sampleResult.getResponseHeaders().split("\n"));
        }

        if (!allReqHeaders && !allResHeaders) {
            headersArrayList.add(this.sampleResult.getRequestHeaders().split("\n"));
            headersArrayList.add(this.sampleResult.getResponseHeaders().split("\n"));
        }

        for (String[] lines : headersArrayList) {
            for (int i = 0; i < lines.length; i++) {
                String[] header = lines[i].split(":", 2);

                // if not all res/req headers and header contains special X-tag
                if (!allReqHeaders && !allResHeaders && header.length > 1) {
                    if (header[0].startsWith("X-es-backend-")) {
                        this.json.put(header[0].replaceAll("X-es-backend-", "").trim(), header[1].trim());
                    }
                }

                if ((allReqHeaders || allResHeaders) && header.length > 1) {
                    this.json.put(header[0].trim(), header[1].trim());
                }
            }
        }
    }

    /**
     * Adds a given key-value pair to JSON if the key is contained in the field filter or in case of empty field filter
     *
     * @param key
     * @param value
     */
    private void addFilteredJSON(String key, Object value) {
        if (this.fields.size() == 0 || this.fields.contains(key.toLowerCase())) {
            this.json.put(key, value);
        }
    }

    /**
     * This method is meant to return the elapsed time in a human readable format. The purpose of this is mostly for
     * build comparison in Kibana. By doing this, the user is able to set the X-axis of his graph to this date and split
     * the series by build numbers. It allows him to overlap test results and see if there is regression or not.
     *
     * @param forBuildComparison boolean to determine if there is CI (continuous integration) or not
     * @return The elapsed time in YYYY-MM-dd HH:mm:ss format
     */
    public Date getElapsedTime(boolean forBuildComparison) {
        String sElapsed;
        //Calculate the elapsed time (Starting from midnight on a random day - enables us to compare of two loads over their duration)
        long start = JMeterContextService.getTestStartTime();
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        long minutes = (elapsed / 1000) / 60;
        long seconds = (elapsed / 1000) % 60;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); //If there is more than an hour of data, the number of minutes/seconds will increment this
        cal.set(Calendar.MINUTE, (int) minutes);
        cal.set(Calendar.SECOND, (int) seconds);

        if (forBuildComparison) {
            sElapsed = String.format("2017-01-01 %02d:%02d:%02d", cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        } else {
            sElapsed = String.format("%s %02d:%02d:%02d",
                    DateTimeFormatter.ofPattern("yyyy-mm-dd").format(LocalDateTime.now()),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        try {
            return formatter.parse(sElapsed);
        } catch (ParseException e) {
            logger.error("Unexpected error occured computing elapsed date", e);
            return null;
        }
    }

}
