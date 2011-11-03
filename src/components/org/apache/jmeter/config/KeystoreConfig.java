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

package org.apache.jmeter.config;

import org.apache.commons.lang.StringUtils;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopTestException;
import org.apache.log.Logger;

/**
 * Configure Keystore
 */
public class KeystoreConfig extends ConfigTestElement implements TestBean, TestListener {
    /**
     * 
     */
    private static final long serialVersionUID = -5781402012242794890L;
    private Logger log = LoggingManager.getLoggerForClass();

    private static final String KEY_STORE_START_INDEX = "https.keyStoreStartIndex"; // $NON-NLS-1$
    private static final String KEY_STORE_END_INDEX   = "https.keyStoreEndIndex"; // $NON-NLS-1$

    private String startIndex;
    private String endIndex;
    private String preload;
    
    /**
     * 
     */
    public KeystoreConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void testEnded() {
        testEnded(null);
    }

    /**
     * {@inheritDoc}
     */
    public void testEnded(String host) {
        log.info("Destroying Keystore");         
        SSLManager.getInstance().destroyKeystore();
    }

    /**
     * {@inheritDoc}
     */
    public void testIterationStart(LoopIterationEvent event) {
        // NOOP        
    }

    /**
     * {@inheritDoc}
     */
    public void testStarted() {
        testStarted(null);
    }

    /**
     * {@inheritDoc}
     */
    public void testStarted(String host) {
        int startIndexAsInt = JMeterUtils.getPropDefault(KEY_STORE_START_INDEX, 0);
        int endIndexAsInt = JMeterUtils.getPropDefault(KEY_STORE_END_INDEX, 0);
        
        if(!StringUtils.isEmpty(this.startIndex)) {
        	try {
        		startIndexAsInt = Integer.parseInt(this.startIndex);
        	} catch(NumberFormatException e) {
        		log.warn("Failed parsing startIndex :'"+this.startIndex+"', will default to:'"+startIndexAsInt+"', error message:"+ e.getMessage(), e);
        	}
        } 
        
        if(!StringUtils.isEmpty(this.endIndex)) {
        	try {
        		endIndexAsInt = Integer.parseInt(this.endIndex);
        	} catch(NumberFormatException e) {
        		log.warn("Failed parsing endIndex :'"+this.endIndex+"', will default to:'"+endIndexAsInt+"', error message:"+ e.getMessage(), e);
        	}
        } 
        if(startIndexAsInt>endIndexAsInt) {
        	throw new JMeterStopTestException("Keystore Config error : Alias start index must be lower than Alias end index");
        }
        log.info("Configuring Keystore with (preload:"+preload+", startIndex:"+
                startIndexAsInt+", endIndex:"+endIndexAsInt+")");

        SSLManager.getInstance().configureKeystore(Boolean.valueOf(preload),
        		startIndexAsInt, 
                endIndexAsInt);
    }

    /**
     * @return the endIndex
     */
    public String getEndIndex() {
        return endIndex;
    }

    /**
     * @param endIndex the endIndex to set
     */
    public void setEndIndex(String endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * @return the startIndex
     */
    public String getStartIndex() {
        return startIndex;
    }

    /**
     * @param startIndex the startIndex to set
     */
    public void setStartIndex(String startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * @return the preload
     */
    public String getPreload() {
        return preload;
    }

    /**
     * @param preload the preload to set
     */
    public void setPreload(String preload) {
        this.preload = preload;
    }
}
