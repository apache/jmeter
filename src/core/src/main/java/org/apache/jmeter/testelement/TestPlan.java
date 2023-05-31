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

package org.apache.jmeter.testelement;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.NewDriver;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.schema.PropertiesAccessor;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPlan extends AbstractTestElement implements Serializable, TestStateListener {
    private static final long serialVersionUID = 234L;

    private static final Logger log = LoggerFactory.getLogger(TestPlan.class);

    private static final String CLASSPATH_SEPARATOR = ","; //$NON-NLS-1$

    private static final String BASEDIR = "basedir";

    private transient List<AbstractThreadGroup> threadGroups = new ArrayList<>();

    // There's only 1 test plan, so can cache the mode here
    private static volatile boolean functionalMode = false;

    public TestPlan() {
        super();
    }

    public TestPlan(String name) {
        setName(name);
    }

    @Override
    public TestPlanSchema getSchema() {
        return TestPlanSchema.INSTANCE;
    }

    @Override
    public PropertiesAccessor<? extends TestPlan, ? extends TestPlanSchema> getProps() {
        return new PropertiesAccessor<>(this, getSchema());
    }

    // create transient item
    protected Object readResolve(){
        threadGroups = new ArrayList<>();
        return this;
    }

    public void prepareForPreCompile()
    {
        getVariables().setRunningVersion(true);
    }

    /**
     * Fetches the functional mode property
     *
     * @return functional mode
     */
    public boolean isFunctionalMode() {
        return get(getSchema().getFunctionalMode());
    }

    public void setUserDefinedVariables(Arguments vars) {
        set(getSchema().getUserDefinedVariables(), vars);
    }

    public JMeterProperty getUserDefinedVariablesAsProperty() {
        return getProperty(getSchema().getUserDefinedVariables().getName());
    }

    public String getBasedir() {
        return getPropertyAsString(BASEDIR);
    }

    // Does not appear to be used yet
    public void setBasedir(String b) {
        setProperty(BASEDIR, b);
    }

    public Arguments getArguments() {
        return getVariables();
    }

    public Map<String, String> getUserDefinedVariables() {
        Arguments args = getVariables();
        return args.getArgumentsAsMap();
    }

    private Arguments getVariables() {
        return getSchema().getUserDefinedVariables().getOrCreate(this, Arguments::new);
    }

    public void setFunctionalMode(boolean funcMode) {
        set(getSchema().getFunctionalMode(), funcMode);
        setGlobalFunctionalMode(funcMode);
    }

    /**
     * Set JMeter in functional mode
     * @param funcMode boolean functional mode
     */
    private static void setGlobalFunctionalMode(boolean funcMode) {
        functionalMode = funcMode;
    }

    /**
     * Gets the static copy of the functional mode
     *
     * @return mode
     */
    public static boolean getFunctionalMode() {
        return functionalMode;
    }

    public void setSerialized(boolean serializeTGs) {
        set(getSchema().getSerializeThreadgroups(), serializeTGs);
    }

    public void setTearDownOnShutdown(boolean tearDown) {
        set(getSchema().getTearDownOnShutdown(), tearDown);
    }

    public boolean isTearDownOnShutdown() {
        return get(getSchema().getTearDownOnShutdown());
    }

    /**
     * Set the classpath for the test plan. If the classpath is made up from
     * more then one path, the parts must be separated with
     * {@link TestPlan#CLASSPATH_SEPARATOR}.
     *
     * @param text
     *            the classpath to be set
     */
    public void setTestPlanClasspath(String text) {
        set(getSchema().getTestPlanClasspath(), text);
    }

    public void setTestPlanClasspathArray(String[] text) {
        StringBuilder cat = new StringBuilder();
        for (int idx=0; idx < text.length; idx++) {
            if (idx > 0) {
                cat.append(CLASSPATH_SEPARATOR);
            }
            cat.append(text[idx]);
        }
        this.setTestPlanClasspath(cat.toString());
    }

    public String[] getTestPlanClasspathArray() {
        return JOrphanUtils.split(this.getTestPlanClasspath(),CLASSPATH_SEPARATOR);
    }

    /**
     * Returns the classpath
     * @return classpath
     */
    public String getTestPlanClasspath() {
        return get(getSchema().getTestPlanClasspath());
    }

    /**
     * Fetch the serialize threadgroups property
     *
     * @return serialized setting
     */
    public boolean isSerialized() {
        return get(getSchema().getSerializeThreadgroups());
    }

    public void addParameter(String name, String value) {
        getVariables().addArgument(name, value);
    }

    @Override
    public void addTestElement(TestElement tg) {
        super.addTestElement(tg);
        if (tg instanceof AbstractThreadGroup && !isRunningVersion()) {
            addThreadGroup((AbstractThreadGroup) tg);
        }
    }

    /**
     * Adds a feature to the AbstractThreadGroup attribute of the TestPlan object.
     *
     * @param group
     *            the feature to be added to the AbstractThreadGroup attribute
     */
    public void addThreadGroup(AbstractThreadGroup group) {
        threadGroups.add(group);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded() {
        try {
            FileServer.getFileServer().closeFiles();
        } catch (IOException e) {
            log.error("Problem closing files at end of test", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded(String host) {
        testEnded();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted() {
        if (getBasedir() != null && getBasedir().length() > 0) {
            try {
                FileServer.getFileServer().setBasedir(FileServer.getFileServer().getBaseDir() + getBasedir());
            } catch (IllegalStateException e) {
                log.error("Failed to set file server base dir with {}", getBasedir(), e);
            }
        }
        // we set the classpath
        String[] paths = this.getTestPlanClasspathArray();
        for (String path : paths) {
            try {
                NewDriver.addURL(path);
                log.info("added {} to classpath", path);
            } catch (MalformedURLException e) {
                // TODO Should we continue the test or fail ?
                log.error("Error adding {} to classpath", path, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String host) {
        testStarted();
    }

}
