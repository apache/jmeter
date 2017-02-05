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

package org.apache.jmeter.protocol.java.config;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * The <code>JavaConfig</code> class contains the configuration data necessary
 * for the Java protocol. This data is used to configure a
 * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient} instance to
 * perform performance test samples.
 *
 */
public class JavaConfig extends ConfigTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    /**
     * Constructor for the JavaConfig object
     */
    public JavaConfig() {
        setArguments(new Arguments());
    }

    /**
     * Sets the class name attribute of the JavaConfig object. This is the class
     * name of the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation which will be used to execute the test.
     *
     * @param classname
     *            the new classname value
     */
    public void setClassname(String classname) {
        setProperty(JavaSampler.CLASSNAME, classname);
    }

    /**
     * Gets the class name attribute of the JavaConfig object. This is the class
     * name of the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation which will be used to execute the test.
     *
     * @return the classname value
     */
    public String getClassname() {
        return getPropertyAsString(JavaSampler.CLASSNAME);
    }

    /**
     * Adds an argument to the list of arguments for this JavaConfig object. The
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation can access these arguments through the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerContext}.
     *
     * @param name
     *            the name of the argument to be added
     * @param value
     *            the value of the argument to be added
     */
    public void addArgument(String name, String value) {
        Arguments args = this.getArguments();
        args.addArgument(name, value);
    }

    /**
     * Removes all of the arguments associated with this JavaConfig object.
     */
    public void removeArguments() {
        setProperty(new TestElementProperty(JavaSampler.ARGUMENTS, new Arguments()));
    }

    /**
     * Set all of the arguments for this JavaConfig object. This will replace
     * any previously added arguments. The
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation can access these arguments through the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerContext}.
     *
     * @param args
     *            the new arguments
     */
    public void setArguments(Arguments args) {
        setProperty(new TestElementProperty(JavaSampler.ARGUMENTS, args));
    }

    /**
     * Gets the arguments for this JavaConfig object. The
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerClient}
     * implementation can access these arguments through the
     * {@link org.apache.jmeter.protocol.java.sampler.JavaSamplerContext}.
     *
     * @return the arguments
     */
    public Arguments getArguments() {
        return (Arguments) getProperty(JavaSampler.ARGUMENTS).getObjectValue();
    }
}
