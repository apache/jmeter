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

package org.apache.jmeter.util;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jorphan.util.JMeterStopTestException;
import org.apache.jorphan.util.JMeterStopTestNowException;
import org.apache.jorphan.util.JMeterStopThreadException;

/**
 * Common parent class for the {@link BSFTestElement} and {@link JSR223TestElement} scripting test elements.
 * These also share the {@link ScriptingBeanInfoSupport} class for configuration.
 */
public abstract class ScriptingTestElement extends AbstractTestElement {

    private static final long serialVersionUID = 282L;

    //++ For TestBean implementations only
    private String parameters = ""; // passed to file or script

    private String filename = ""; // file to source (overrides script)

    private String script = ""; // script (if file not provided)

    protected String scriptLanguage = ""; // BSF/JSR223 language to use

    public final static String DEFAULT_SCRIPT_LANGUAGE = "groovy"; // if no language is chosen in GUI

    //-- For TestBean implementations only

    public ScriptingTestElement() {
        super();
    }

    /**
     * Return the script (TestBean version).
     * Must be overridden for subclasses that don't implement TestBean
     * otherwise the clone() method won't work.
     *
     * @return the script to execute
     */
    public String getScript(){
        return script;
    }

    /**
     * Set the script (TestBean version).
     * Must be overridden for subclasses that don't implement TestBean
     * otherwise the clone() method won't work.
     *
     * @param s the script to execute (may be blank)
     */
    public void setScript(String s){
        script=s;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String s) {
        parameters = s;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String s) {
        filename = s;
    }

    /**
     * @param rootCause Throwable
     * @return true if Throwable is an Exception that impacts test state
     */
    protected boolean isStopCondition(Throwable rootCause) {
        return rootCause instanceof JMeterStopTestNowException
                || rootCause instanceof JMeterStopTestException
                || rootCause instanceof JMeterStopThreadException;
    }
}
