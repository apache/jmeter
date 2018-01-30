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

package org.apache.jmeter.protocol.http.util.accesslog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:<br>
 * <br>
 * StandardGenerator will be the default generator used to pre-process logs. It
 * uses JMeter classes to generate the .jmx file. The first version of the
 * utility only generated the HTTP requests as XML, but it required users to
 * copy and paste it into a blank jmx file. Doing that way isn't flexible and
 * would require changes to keep the format in sync.
 * <p>
 * This version is a completely new class with a totally different
 * implementation, since generating the XML is no longer handled by the
 * generator. The generator is only responsible for handling the parsed results
 * and passing it to the appropriate JMeter class.
 * <p>
 * Notes:<br>
 * the class needs to first create a thread group and add it to the HashTree.
 * Then the samplers should be added to the thread group. Listeners shouldn't be
 * added and should be left up to the user. One option is to provide parameters,
 * so the user can pass the desired listener to the tool.
 */
public class StandardGenerator implements Generator, Serializable {

    private static final long serialVersionUID = 234L;

    private static final Logger log = LoggerFactory.getLogger(StandardGenerator.class);

    protected HTTPSamplerBase SAMPLE = null;
    protected transient OutputStream OUTPUT = null;
    protected String FILENAME = null;
    protected File FILE = null;

    /**
     * The constructor is used by GUI and samplers to generate request objects.
     */
    public StandardGenerator() {
        super();
        init();
    }

    /**
     *
     * @param file name of a file (TODO seems not to be used anywhere)
     */
    public StandardGenerator(String file) {
        FILENAME = file;
        init();
    }

    /**
     * initialize the generator. It should create the following objects.
     * <p>
     * <ol>
     * <li> ListedHashTree</li>
     * <li> ThreadGroup</li>
     * <li> File object</li>
     * <li> Writer</li>
     * </ol>
     */
    private void init() {// called from ctor, so must not be overridable
        generateRequest();
    }

    /**
     * Create the OutputStream to save the JMX file.
     */
    protected void initStream() {
        try {
            this.OUTPUT = new FileOutputStream(FILE);
        } catch (IOException exception) {
            log.error(exception.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        JOrphanUtils.closeQuietly(OUTPUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHost(String host) {
        SAMPLE.setDomain(host);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLabel(String label) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMethod(String post_get) {
        SAMPLE.setMethod(post_get);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParams(NVPair[] params) {
        for (NVPair param : params) {
            SAMPLE.addArgument(param.getName(), param.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPath(String path) {
        SAMPLE.setPath(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPort(int port) {
        SAMPLE.setPort(port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueryString(String querystring) {
        SAMPLE.parseArguments(querystring);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceLogs(String sourcefile) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTarget(Object target) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object generateRequest() {
        SAMPLE = HTTPSamplerFactory.newInstance();
        return SAMPLE;
    }

    /**
     * save must be called to write the jmx file, otherwise it will not be
     * saved.
     */
    @Override
    public void save() {
        // no implementation at this time, since
        // we bypass the idea of having a console
        // tool to generate test plans. Instead
        // I decided to have a sampler that uses
        // the generator and parser directly
    }

    /**
     * Reset the HTTPSampler to make sure it is a new instance.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        SAMPLE = null;
        generateRequest();
    }
}
