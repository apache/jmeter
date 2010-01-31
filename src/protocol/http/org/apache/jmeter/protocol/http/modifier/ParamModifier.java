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

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * This modifier will replace any single http sampler's url parameter value with
 * a value from a given range - thereby "masking" the value set in the http
 * sampler. The parameter names must match exactly, and the parameter value must
 * be preset to "*" to diferentiate between duplicate parameter names.
 * <P>
 * For example, if you set up the modifier with a lower bound of 1, an upper
 * bound of 10, and an increment of 2, and run the loop 12 times, the parameter
 * will have the following values (one per loop): 1, 3, 5, 7, 9, 1, 3, 5, 7, 9,
 * 1, 3
 * <P>
 * The {@link ParamMask} object contains most of the logic for stepping through
 * this loop. You can make large modifications to this modifier's behaviour by
 * changing one or two method implementations there.
 *
 * @see ParamMask
 * @version $Revision$
 */
public class ParamModifier extends AbstractTestElement implements TestListener, PreProcessor, Serializable {

    private static final long serialVersionUID = 240L;

    /*
     * ------------------------------------------------------------------------
     * Fields
     * ------------------------------------------------------------------------
     */

    /**
     * The key used to find the ParamMask object in the HashMap.
     */
    private final static String MASK = "ParamModifier.mask";

    /*
     * ------------------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------------------
     */

    /**
     * Default constructor.
     */
    public ParamModifier() {
        setProperty(new TestElementProperty(MASK, new ParamMask()));
    }

    public ParamMask getMask() {
        return (ParamMask) getProperty(MASK).getObjectValue();
    }

    public void testStarted() {
        getMask().resetValue();
    }

    public void testStarted(String host) {
        getMask().resetValue();
    }

    public void testEnded() {
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
     * matches a defined mask.
     *
     */
    public void process() {
        Sampler sam = getThreadContext().getCurrentSampler();
        HTTPSamplerBase sampler = null;
        if (!(sam instanceof HTTPSamplerBase)) {
            return;
        } else {
            sampler = (HTTPSamplerBase) sam;
        }
        boolean modified = false;
        PropertyIterator iter = sampler.getArguments().iterator();
        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next().getObjectValue();
            modified = modifyArgument(arg);
            if (modified) {
                break;
            }
        }
    }

    /*
     * ------------------------------------------------------------------------
     * Methods
     * ------------------------------------------------------------------------
     */

    /**
     * Helper method for {@link #modifyEntry} Replaces a parameter's value if
     * the parameter name matches the mask name and the value is a '*'.
     *
     * @param arg
     *            an {@link Argument} representing a http parameter
     * @return <code>true</code>if the value was replaced
     */
    private boolean modifyArgument(Argument arg) {
        // if a mask for this argument exists
        if (arg.getName().equals(getMask().getFieldName())) {
            // values to be masked must be set in the WebApp to "*"
            if ("*".equals(arg.getValue())) {
                arg.setValue(getMask().getNextValue());
                return true;
            }
        }
        return false;
    }

    /**
     * @see TestListener#testIterationStart(LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event) {
    }
}
