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

package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterError;
import org.apache.log.Logger;

/**
 * Packages information regarding the target of a sample event, such as the
 * result from that event and the thread group it ran in.
 */
public class SampleEvent implements Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 232L;

    /** The property {@value} is used to define additional variables to be saved */
    public static final String SAMPLE_VARIABLES = "sample_variables"; // $NON-NLS-1$

    public static final String HOSTNAME;

    // List of variable names to be saved in JTL files
    private static volatile String[] variableNames = new String[0];

    // The values. Entries may be null, but there will be the correct number.
    private final String[] values;

    // The hostname cannot change during a run, so safe to cache it just once
    static {
        String hn="";
        try {
            hn = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Cannot obtain local host name "+e);
        }
        HOSTNAME=hn;
        initSampleVariables();
    }

    /**
     * Set up the additional variable names to be saved
     * from the value in the {@link #SAMPLE_VARIABLES} property
     */
    public static void initSampleVariables() {
        String vars = JMeterUtils.getProperty(SAMPLE_VARIABLES);
        variableNames=vars != null ? vars.split(",") : new String[0];
        log.info("List of sample_variables: " + Arrays.toString(variableNames));
    }

    private final SampleResult result;

    private final String threadGroup; // TODO appears to duplicate the threadName field in SampleResult

    private final String hostname;

    private final boolean isTransactionSampleEvent;

    /*
     * Only for Unit tests
     */
    public SampleEvent() {
        this(null, null);
    }

    /**
     * Creates SampleEvent without saving any variables.
     *
     * Use by Proxy and StatisticalSampleSender.
     *
     * @param result SampleResult
     * @param threadGroup name
     */
    public SampleEvent(SampleResult result, String threadGroup) {
        this(result, threadGroup, HOSTNAME, false);
    }

    /**
     * Contructor used for normal samples, saves variable values if any are defined.
     *
     * @param result
     * @param threadGroup name
     * @param jmvars Jmeter variables
     */
    public SampleEvent(SampleResult result, String threadGroup, JMeterVariables jmvars) {
        this(result, threadGroup, jmvars, false);
    }

    /**
     * Only intended for use when loading results from a file.
     *
     * @param result
     * @param threadGroup
     * @param hostname
     */
    public SampleEvent(SampleResult result, String threadGroup, String hostname) {
       this(result, threadGroup, hostname, false);
    }
    
    private SampleEvent(SampleResult result, String threadGroup, String hostname, boolean isTransactionSampleEvent) {
        this.result = result;
        this.threadGroup = threadGroup;
        this.hostname = hostname;
        this.values = new String[variableNames.length];
        this.isTransactionSampleEvent = isTransactionSampleEvent;
    }

    /**
     * @param result
     * @param threadGroup
     * @param jmvars
     * @param isTransactionSampleEvent
     */
    public SampleEvent(SampleResult result, String threadGroup, JMeterVariables jmvars, boolean isTransactionSampleEvent) {
        this(result, threadGroup, HOSTNAME, isTransactionSampleEvent);
        saveVars(jmvars);
    }

    private void saveVars(JMeterVariables vars){
        for(int i = 0; i < variableNames.length; i++){
            values[i] = vars.get(variableNames[i]);
        }
    }

    /** Return the number of variables defined */
    public static int getVarCount(){
        return variableNames.length;
    }

    /** Get the nth variable name (zero-based) */
    public static String getVarName(int i){
        return variableNames[i];
    }

    /** Get the nth variable value (zero-based) */
    public String getVarValue(int i){
        try {
            return values[i];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JMeterError("Check the sample_variable settings!", e);
        }
    }

    public SampleResult getResult() {
        return result;
    }

    public String getThreadGroup() {
        return threadGroup;
    }

    public String getHostname() {
        return hostname;
    }

    /**
     * @return the isTransactionSampleEvent
     */
    public boolean isTransactionSampleEvent() {
        return isTransactionSampleEvent;
    }

}
