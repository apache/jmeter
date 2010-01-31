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

package org.apache.jmeter.protocol.http.modifier;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This modifier will replace any http sampler's url parameter values with
 * parameter values defined in a XML file for each simulated user.
 * <P>
 * For example if userid and password are defined in the XML parameter file for
 * each user (ie thread), then simulated multiple user activity can occur.
 *
 * This test element is deprecated. Test plans should use User Parameters instead.
 * @deprecated
 */
@Deprecated
public class UserParameterModifier extends ConfigTestElement implements PreProcessor, Serializable, TestListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    private static final String XMLURI = "UserParameterModifier.xmluri"; // $NON-NLS-1$

    private UserSequence allAvailableUsers;

    /**
     * Default constructor.
     */
    public UserParameterModifier() {
    } // end constructor

    /**
     * Runs before the start of every test. Reload the Sequencer with the latest
     * parameter data for each user
     */
    public void testStarted() {
        // try to populate allUsers, if fail, leave as any empty set
        List<Map<String, String>> allUsers = new LinkedList<Map<String, String>>();
        try {
            UserParameterXMLParser readXMLParameters = new UserParameterXMLParser();
            allUsers = readXMLParameters.getXMLParameters(getXmlUri());
        } catch (Exception e) {
            // do nothing, now object allUsers contains an empty set
            log.error("Unable to read parameters from xml file " + getXmlUri());
            log.error("No unique values for http requests will be substituted for " + "each thread", e);
        }
        allAvailableUsers = new UserSequence(allUsers);
    }

    public void testEnded() {
    }

    public void testStarted(String host) {
        testStarted();
    }

    public void testEnded(String host) {
    }

    /*
     * ------------------------------------------------------------------------
     * Methods implemented from interface org.apache.jmeter.config.Modifier
     * ------------------------------------------------------------------------
     */

    /**
     * Modifies an entry object to replace the value of any url parameter that
     * matches a parameter name in the XML file.
     *
     */
    public void process() {
        Sampler entry = getThreadContext().getCurrentSampler();
        if (!(entry instanceof HTTPSamplerBase)) {
            return;
        }
        HTTPSamplerBase config = (HTTPSamplerBase) entry;
        Map<String, String> currentUser = allAvailableUsers.getNextUserMods();
        PropertyIterator iter = config.getArguments().iterator();
        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next().getObjectValue();
            // if parameter name exists in http request
            // then change its value
            // (Note: each jmeter thread (ie user) gets to have unique values)
            if (currentUser.containsKey(arg.getName())) {
                arg.setValue(currentUser.get(arg.getName()));
            }
        }
    }

    /*
     * ------------------------------------------------------------------------
     * Methods (used by UserParameterModifierGui to get/set the name of XML
     * parameter file)
     * ------------------------------------------------------------------------
     */

    /**
     * Return the current XML file name to be read to obtain the parameter data
     * for all users
     *
     * @return the name of the XML file containing parameter data for each user
     */
    public String getXmlUri() {
        return this.getPropertyAsString(XMLURI);
    }

    /**
     * From the GUI screen, set file name of XML to read
     *
     * @param xmlURI
     *            the name of the XML file containing the HTTP name value pair
     *            parameters per user
     */
    public void setXmlUri(String xmlURI) {
        setProperty(XMLURI, xmlURI);
    }

    /** {@inheritDoc} */
    public void testIterationStart(LoopIterationEvent event) {
    }

    /** {@inheritDoc} */
    @Override
    public Object clone() {
        UserParameterModifier clone = (UserParameterModifier) super.clone();
        clone.allAvailableUsers = allAvailableUsers;
        return clone;
    }
}