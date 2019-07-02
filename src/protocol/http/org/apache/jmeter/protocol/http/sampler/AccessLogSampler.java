/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.protocol.http.sampler;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.accesslog.Filter;
import org.apache.jmeter.protocol.http.util.accesslog.LogParser;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestCloneable;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.util.JMeterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: <br>
 * <br>
 * AccessLogSampler is responsible for a couple of things:
 * <ul>
 * <li>creating instances of Generator
 * <li>creating instances of Parser
 * <li>triggering popup windows
 * <li>calling Generator.generateRequest()
 * <li>checking to make sure the classes are valid
 * <li>making sure a class can be instantiated
 * </ul>
 * The intent of this sampler is it uses the generator and parser to create a
 * HTTPSampler when it is needed. It does not contain logic about how to parse
 * the logs. It also doesn't care how Generator is implemented, as long as it
 * implements the interface. This means a person could simply implement a dummy
 * parser to generate random parameters and the generator consumes the results.
 * This wasn't the original intent of the sampler. I originally wanted to write
 * this sampler, so that I can take production logs to simulate production
 * traffic in a test environment. Doing so is desirable to study odd or unusual
 * behavior. It's also good to compare a new system against an existing system
 * to get near apples-to-apples comparison. I've been asked if benchmarks are
 * really fair comparisons just about every single time, so this helps me
 * accomplish that task.
 * <p>
 * Some bugs only appear under production traffic, so it is useful to generate
 * traffic using production logs. This way, JMeter can record when problems
 * occur and provide a way to match the server logs.
 * </p>
 *
 */
public class AccessLogSampler extends HTTPSampler implements TestBean,ThreadListener {
    private static final Logger log = LoggerFactory.getLogger(AccessLogSampler.class);

    private static final long serialVersionUID = 233L; // Remember to change this when the class changes ...

    public static final String DEFAULT_CLASS = "org.apache.jmeter.protocol.http.util.accesslog.TCLogParser"; // $NON-NLS-1$

    /* private members used by class */
    private transient LogParser parser = null;

    private String logFile;
    private String parserClassName;
    private String filterClassName;

    private transient Filter filter;

    private int count = 0;

    private boolean started = false;

    /**
     * Set the path where XML messages are stored for random selection.
     *
     * @param path path where to store XML messages
     */
    public void setLogFile(String path) {
        logFile = path;
    }

    /**
     * Get the path where XML messages are stored. this is the directory where
     * JMeter will randomly select a file.
     *
     * @return path where XML messages are stored
     */
    public String getLogFile() {
        return logFile;
    }

    /**
     * it's kinda obvious, but we state it anyways. Set the xml file with a
     * string path.
     *
     * @param classname -
     *            parser class name
     */
    public void setParserClassName(String classname) {
        parserClassName = classname;
    }

    /**
     * Get the file location of the xml file.
     *
     * @return String file path.
     */
    public String getParserClassName() {
        return parserClassName;
    }

    /**
     * sample gets a new HTTPSampler from the generator and calls it's sample()
     * method.
     *
     * @return newly generated and called sample
     */
    public SampleResult sampleWithParser() {
        initFilter();
        instantiateParser();
        SampleResult res = null;
        try {

            if (parser == null) {
                throw new JMeterException("No Parser available");
            }
            // we call parse with 1 to get only one.
            // this also means if we change the implementation
            // to use 2, it would use every other entry and
            // so on. Not that it is really useful, but a
            // person could use it that way if they have a
            // huge gigabyte log file and they only want to
            // use a quarter of the entries.
            int thisCount = parser.parseAndConfigure(1, this);
            if (thisCount < 0) // Was there an error?
            {
                return errorResult(new Error("Problem parsing the log file"), new HTTPSampleResult());
            }
            if (thisCount == 0) {
                if (count == 0 || filter == null) {
                    log.info("Stopping current thread");
                    JMeterContextService.getContext().getThread().stop();
                }
                if (filter != null) {
                    filter.reset();
                }
                CookieManager cm = getCookieManager();
                if (cm != null) {
                    cm.clear();
                }
                count = 0;
                return errorResult(new Error("No entries found"), new HTTPSampleResult());
            }
            count = thisCount;
            res = sample();
            if(res != null) {
                res.setSampleLabel(toString());
            }
        } catch (Exception e) {
            log.warn("Sampling failure", e);
            return errorResult(e, new HTTPSampleResult());
        }
        return res;
    }

    /**
     * sample(Entry e) simply calls sample().
     *
     * @param e -
     *            ignored
     * @return the new sample
     */
    @Override
    public SampleResult sample(Entry e) {
        return sampleWithParser();
    }

    /**
     * Method will instantiate the log parser based on the class in the text
     * field. This was done to make it easier for people to plugin their own log
     * parser and use different log parser.
     */
    public void instantiateParser() {
        if (parser == null) {
            try {
                if (StringUtils.isNotBlank(this.getParserClassName())) {
                    if (StringUtils.isNotBlank(this.getLogFile())) {
                        parser = (LogParser) Class.forName(getParserClassName()).getDeclaredConstructor().newInstance();
                        parser.setSourceFile(this.getLogFile());
                        parser.setFilter(filter);
                    } else {
                        log.error("No log file specified");
                    }
                }
            } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
                log.error("", e);
            }
        }
    }

    /**
     * @return Returns the filterClassName.
     */
    public String getFilterClassName() {
        return filterClassName;
    }

    /**
     * @param filterClassName
     *            The filterClassName to set.
     */
    public void setFilterClassName(String filterClassName) {
        this.filterClassName = filterClassName;
    }

    /**
     * @return Returns the domain.
     */
    @Override
    public String getDomain() { // N.B. Must be in this class for the TestBean code to work
        return super.getDomain();
    }

    /**
     * @param domain
     *            The domain to set.
     */
    @Override
    public void setDomain(String domain) { // N.B. Must be in this class for the TestBean code to work
        super.setDomain(domain);
    }

    /**
     * @return Returns the imageParsing.
     */
    public boolean isImageParsing() {
        return super.isImageParser();
    }

    /**
     * @param imageParsing
     *            The imageParsing to set.
     */
    public void setImageParsing(boolean imageParsing) {
        super.setImageParser(imageParsing);
    }

    /**
     * @return Returns the port.
     */
    public String getPortString() {
        return super.getPropertyAsString(HTTPSamplerBase.PORT);
    }

    /**
     * @param port
     *            The port to set.
     */
    public void setPortString(String port) {
        super.setProperty(HTTPSamplerBase.PORT, port);
    }

    /**
     * Sets the scheme, with default
     * @param value the protocol
     */
    @Override
    public void setProtocol(String value) {
        setProperty(PROTOCOL, value.toLowerCase(java.util.Locale.ENGLISH));
    }

    /**
     * Gets the protocol, with default.
     *
     * @return the protocol
     */
    @Override
    public String getProtocol() {
        String protocol = getPropertyAsString(PROTOCOL);
        if (StringUtils.isEmpty(protocol)) {
            return HTTPConstants.PROTOCOL_HTTP;
        }
        return protocol;
    }

    /**
     *
     */
    public AccessLogSampler() {
        super();
    }

    protected void initFilter() {
        if (filter == null && StringUtils.isNotBlank(filterClassName)) {
            try {
                filter = (Filter) Class.forName(filterClassName).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.warn("Couldn't instantiate filter '{}'", filterClassName, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        AccessLogSampler s = (AccessLogSampler) super.clone();
        if (started && StringUtils.isNotBlank(filterClassName)) {

            try {
                if (TestCloneable.class.isAssignableFrom(Class.forName(filterClassName))) {
                    initFilter();
                    s.filter = (Filter) ((TestCloneable) filter).clone();
                }
                if (TestCloneable.class.isAssignableFrom(Class.forName(parserClassName)))
                {
                    instantiateParser();
                    s.parser = (LogParser)((TestCloneable)parser).clone();
                    if (filter != null)
                    {
                        s.parser.setFilter(s.filter);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not clone cloneable filter", e);
            }
        }
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded() {
        if (parser != null) {
            parser.close();
        }
        filter = null;
        started = false;
        super.testEnded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted() {
        started = true;
        super.testStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadFinished() {
        if(parser instanceof ThreadListener) {
            ((ThreadListener)parser).threadFinished();
        }
        if(filter instanceof ThreadListener) {
            ((ThreadListener)filter).threadFinished();
        }
    }
}
