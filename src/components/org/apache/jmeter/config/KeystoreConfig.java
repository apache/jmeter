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

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
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
        log.info("Configuring Keystore with (preload:"+preload+", startIndex:"+
                startIndex+", endIndex:"+endIndex);
        SSLManager.getInstance().configureKeystore(Boolean.valueOf(preload),
        		Integer.parseInt(startIndex), 
                Integer.parseInt(endIndex));
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
