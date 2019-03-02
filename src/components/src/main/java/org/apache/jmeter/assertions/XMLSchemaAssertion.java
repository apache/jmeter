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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// See Bug 34383

/**
 * XMLSchemaAssertion.java Validate response against an XML Schema author
 */
public class XMLSchemaAssertion extends AbstractTestElement implements Serializable, Assertion {

    private static final long serialVersionUID = 234L;

    public static final String FILE_NAME_IS_REQUIRED = "FileName is required";

    public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    private static final Logger log = LoggerFactory.getLogger(XMLSchemaAssertion.class);

    public static final String XSD_FILENAME_KEY = "xmlschema_assertion_filename";

    /**
     * getResult
     *
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        AssertionResult result = new AssertionResult(getName());
        // Note: initialised with error = failure = false

        String resultData = response.getResponseDataAsString();
        if (resultData.length() == 0) {
            return result.setResultForNull();
        }

        String xsdFileName = getXsdFileName();
        log.debug("xmlString: {}, xsdFileName: {}", resultData, xsdFileName);
        if (xsdFileName == null || xsdFileName.length() == 0) {
            result.setResultForFailure(FILE_NAME_IS_REQUIRED);
        } else {
            setSchemaResult(result, resultData, xsdFileName);
        }
        return result;
    }

    public void setXsdFileName(String xmlSchemaFileName) throws IllegalArgumentException {
        setProperty(XSD_FILENAME_KEY, xmlSchemaFileName);
    }

    public String getXsdFileName() {
        return getPropertyAsString(XSD_FILENAME_KEY);
    }

    /**
     * set Schema result
     *
     * @param result
     * @param xmlStr
     * @param xsdFileName
     */
    private void setSchemaResult(AssertionResult result, String xmlStr, String xsdFileName) {
        try {
            DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
            parserFactory.setValidating(true);
            parserFactory.setNamespaceAware(true);
            parserFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            parserFactory.setAttribute(JAXP_SCHEMA_SOURCE, xsdFileName);

            // create a parser:
            DocumentBuilder parser = parserFactory.newDocumentBuilder();
            parser.setErrorHandler(new SAXErrorHandler(result));
            parser.parse(new InputSource(new StringReader(xmlStr)));
            // if everything went fine then xml schema validation is valid
        } catch (SAXParseException e) {

            // Only set message if error not yet flagged
            if (!result.isError() && !result.isFailure()) {
                result.setError(true);
                result.setFailureMessage(errorDetails(e));
            }

        } catch (SAXException e) {
            if (log.isWarnEnabled()) {
                log.warn(e.toString());
            }
            result.setResultForFailure(e.getMessage());

        } catch (IOException e) {

            log.warn("IO error", e);
            result.setResultForFailure(e.getMessage());

        } catch (ParserConfigurationException e) {

            log.warn("Problem with Parser Config", e);
            result.setResultForFailure(e.getMessage());

        }

    }

    // Helper method to construct SAX error details
    private static String errorDetails(SAXParseException spe) {
        StringBuilder str = new StringBuilder(80);
        int i;
        i = spe.getLineNumber();
        if (i != -1) {
            str.append("line=");
            str.append(i);
            str.append(" col=");
            str.append(spe.getColumnNumber());
            str.append(" ");
        }
        str.append(spe.getLocalizedMessage());
        return str.toString();
    }

    /**
     * SAXErrorHandler class
     */
    private static class SAXErrorHandler implements ErrorHandler {
        private final AssertionResult result;

        public SAXErrorHandler(AssertionResult result) {
            this.result = result;
        }

        /*
         * Can be caused by: - failure to read XSD file - xml does not match XSD
         */
        @Override
        public void error(SAXParseException exception) throws SAXParseException {

            String msg = "error: " + errorDetails(exception);
            log.debug(msg);
            result.setFailureMessage(msg);
            result.setError(true);
            throw exception;
        }

        /*
         * Can be caused by: - premature end of file - non-whitespace content
         * after trailer
         */
        @Override
        public void fatalError(SAXParseException exception) throws SAXParseException {
            String msg = "fatal: " + errorDetails(exception);
            log.debug(msg);
            result.setFailureMessage(msg);
            result.setError(true);
            throw exception;
        }

        /*
         * Not clear what can cause this ? conflicting versions perhaps
         */
        @Override
        public void warning(SAXParseException exception) throws SAXParseException {
            String msg = "warning: " + errorDetails(exception);
            log.debug(msg);
            result.setFailureMessage(msg);
        }
    }
}
