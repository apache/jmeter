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

package org.apache.jmeter.assertions;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Checks if the result is a well-formed XML content using {@link XMLReader}
 * 
 */
public class XMLAssertion extends AbstractTestElement implements Serializable, Assertion, ThreadListener {
    private static final Logger log = LoggerFactory.getLogger(XMLAssertion.class);

    private static final long serialVersionUID = 242L;

    // one builder for all requests in a thread
    private static final ThreadLocal<XMLReader> XML_READER = new ThreadLocal<XMLReader>() {
        @Override
        protected XMLReader initialValue() {
            try {
                return XMLReaderFactory.createXMLReader();
            } catch (SAXException e) {
                log.error("Error initializing XMLReader in XMLAssertion", e); 
                return null;
            }
        }
    };

    /**
     * Returns the result of the Assertion. 
     * Here it checks whether the Sample data is XML. 
     * If so an AssertionResult containing a FailureMessage will be returned. 
     * Otherwise the returned AssertionResult will reflect the success of the Sample.
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        // no error as default
        AssertionResult result = new AssertionResult(getName());
        String resultData = response.getResponseDataAsString();
        if (resultData.length() == 0) {
            return result.setResultForNull();
        }
        result.setFailure(false);
        XMLReader builder = XML_READER.get();
        if(builder != null) {
            try {
                builder.setErrorHandler(new LogErrorHandler());
                builder.parse(new InputSource(new StringReader(resultData)));
            } catch (SAXException | IOException e) {
                result.setError(true);
                result.setFailure(true);
                result.setFailureMessage(e.getMessage());
            }
        } else {
            result.setError(true);
            result.setFailureMessage("Cannot initialize XMLReader in element:"+getName()+", check jmeter.log file");
        }

        return result;
    }

    @Override
    public void threadStarted() {
    }

    @Override
    public void threadFinished() {
        XML_READER.set(null);
    }
}
