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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Node;
import org.w3c.tidy.Tidy;

/**
 * Assertion to validate the response of a Sample with Tidy.
 */
public class HTMLAssertion extends AbstractTestElement implements Serializable, Assertion {

    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(HTMLAssertion.class);

    public static final String DEFAULT_DOCTYPE = "omit"; //$NON-NLS-1$

    public static final String DOCTYPE_KEY = "html_assertion_doctype"; //$NON-NLS-1$

    public static final String ERRORS_ONLY_KEY = "html_assertion_errorsonly"; //$NON-NLS-1$

    public static final String ERROR_THRESHOLD_KEY = "html_assertion_error_threshold"; //$NON-NLS-1$

    public static final String WARNING_THRESHOLD_KEY = "html_assertion_warning_threshold"; //$NON-NLS-1$

    public static final String FORMAT_KEY = "html_assertion_format"; //$NON-NLS-1$

    public static final String FILENAME_KEY = "html_assertion_filename"; //$NON-NLS-1$

    /**
     * 
     */
    public HTMLAssertion() {
        log.debug("HTMLAssertion(): called");
    }

    /**
     * Returns the result of the Assertion. If so an AssertionResult containing
     * a FailureMessage will be returned. Otherwise the returned AssertionResult
     * will reflect the success of the Sample.
     */
    @Override
    public AssertionResult getResult(SampleResult inResponse) {
        log.debug("HTMLAssertions.getResult() called");

        // no error as default
        AssertionResult result = new AssertionResult(getName());

        if (inResponse.getResponseData().length == 0) {
            return result.setResultForNull();
        }

        result.setFailure(false);

        // create parser
        Tidy tidy = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Setting up tidy... doctype: {}, errors only: {}, error threshold: {}, warning threshold: {}, html mode: {}, xhtml mode: {}, xml mode: {}.",
                        getDoctype(), isErrorsOnly(), getErrorThreshold(), getWarningThreshold(), isHTML(), isXHTML(),
                        isXML());
            }
            tidy = new Tidy();
            tidy.setInputEncoding(StandardCharsets.UTF_8.name());
            tidy.setOutputEncoding(StandardCharsets.UTF_8.name());
            tidy.setQuiet(false);
            tidy.setShowWarnings(true);
            tidy.setOnlyErrors(isErrorsOnly());
            tidy.setDocType(getDoctype());
            if (isXHTML()) {
                tidy.setXHTML(true);
            } else if (isXML()) {
                tidy.setXmlTags(true);
            }
            tidy.setErrfile(getFilename());

            if (log.isDebugEnabled()) {
                log.debug("Tidy instance created... err file: {}, tidy parser: {}", getFilename(), tidy);
            }

        } catch (Exception e) {
            log.error("Unable to instantiate tidy parser", e);
            result.setFailure(true);
            result.setFailureMessage("Unable to instantiate tidy parser");
            // return with an error
            return result;
        }

        /*
         * Run tidy.
         */
        try {
            log.debug("HTMLAssertions.getResult(): start parsing with tidy ...");

            StringWriter errbuf = new StringWriter();
            tidy.setErrout(new PrintWriter(errbuf));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            log.debug("Parsing with tidy starting...");
            Node node = tidy.parse(new ByteArrayInputStream(inResponse.getResponseData()), os);
            log.debug("Parsing with tidy done! node: {}, output: {}", node, os);

            // write output to file
            writeOutput(errbuf.toString());

            // evaluate result
            if ((tidy.getParseErrors() > getErrorThreshold())
                    || (!isErrorsOnly() && (tidy.getParseWarnings() > getWarningThreshold()))) {
                log.debug("Errors/warnings detected while parsing with tidy: {}", errbuf);
                result.setFailure(true);
                result.setFailureMessage(MessageFormat.format("Tidy Parser errors:   " + tidy.getParseErrors()
                        + " (allowed " + getErrorThreshold() + ") " + "Tidy Parser warnings: "
                        + tidy.getParseWarnings() + " (allowed " + getWarningThreshold() + ")", new Object[0]));
                // return with an error

            } else if ((tidy.getParseErrors() > 0) || (tidy.getParseWarnings() > 0)) {
                // return with no error
                log.debug("HTMLAssertions.getResult(): there were errors/warnings but threshold to high");
                result.setFailure(false);
            } else {
                // return with no error
                log.debug("HTMLAssertions.getResult(): no errors/warnings detected:");
                result.setFailure(false);
            }

        } catch (Exception e) {
            // return with an error
            log.warn("Cannot parse result content", e);
            result.setFailure(true);
            result.setFailureMessage(e.getMessage());
        }
        return result;
    }

    /**
     * Writes the output of tidy to file.
     * 
     * @param inOutput The String to write to file
     */
    private void writeOutput(String inOutput) {
        String lFilename = getFilename();

        // check if filename defined
        if ((lFilename != null) && (!"".equals(lFilename.trim()))) {
            
            try (FileWriter lOutputWriter = new FileWriter(lFilename, false)){
                // write to file
                lOutputWriter.write(inOutput);
                log.debug("writeOutput() -> output successfully written to file: {}", lFilename);
            } catch (IOException ex) {
                log.warn("writeOutput() -> could not write output to file: {}", lFilename, ex);
            }
        }
    }

    /**
     * Gets the doctype
     * 
     * @return the document type
     */
    public String getDoctype() {
        return getPropertyAsString(DOCTYPE_KEY);
    }

    /**
     * Check if errors will be reported only
     * 
     * @return boolean - report errors only?
     */
    public boolean isErrorsOnly() {
        return getPropertyAsBoolean(ERRORS_ONLY_KEY);
    }

    /**
     * Gets the threshold setting for errors
     * 
     * @return long error threshold
     */
    public long getErrorThreshold() {
        return getPropertyAsLong(ERROR_THRESHOLD_KEY);
    }

    /**
     * Gets the threshold setting for warnings
     * 
     * @return long warning threshold
     */
    public long getWarningThreshold() {
        return getPropertyAsLong(WARNING_THRESHOLD_KEY);
    }

    /**
     * Sets the doctype setting
     * 
     * @param inDoctype
     *            The doctype to be set. If <code>doctype</code> is
     *            <code>null</code> or a blank string, {@link HTMLAssertion#DEFAULT_DOCTYPE} will be
     *            used
     */
    public void setDoctype(String inDoctype) {
        if ((inDoctype == null) || (inDoctype.trim().isEmpty())) {
            setProperty(new StringProperty(DOCTYPE_KEY, DEFAULT_DOCTYPE));
        } else {
            setProperty(new StringProperty(DOCTYPE_KEY, inDoctype));
        }
    }

    /**
     * Sets if errors should be tracked only
     * 
     * @param inErrorsOnly Flag whether only errors should be tracked
     */
    public void setErrorsOnly(boolean inErrorsOnly) {
        setProperty(new BooleanProperty(ERRORS_ONLY_KEY, inErrorsOnly));
    }

    /**
     * Sets the threshold on error level
     * 
     * @param inErrorThreshold
     *            The max number of parse errors which are to be tolerated
     * @throws IllegalArgumentException
     *             if <code>inErrorThreshold</code> is less or equals zero
     */
    public void setErrorThreshold(long inErrorThreshold) {
        if (inErrorThreshold < 0L) {
            throw new IllegalArgumentException(JMeterUtils.getResString("argument_must_not_be_negative")); //$NON-NLS-1$
        }
        if (inErrorThreshold == Long.MAX_VALUE) {
            setProperty(new LongProperty(ERROR_THRESHOLD_KEY, 0));
        } else {
            setProperty(new LongProperty(ERROR_THRESHOLD_KEY, inErrorThreshold));
        }
    }

    /**
     * Sets the threshold on warning level
     * 
     * @param inWarningThreshold
     *            The max number of warnings which are to be tolerated
     * @throws IllegalArgumentException
     *             if <code>inWarningThreshold</code> is less or equal zero
     */
    public void setWarningThreshold(long inWarningThreshold) {
        if (inWarningThreshold < 0L) {
            throw new IllegalArgumentException(JMeterUtils.getResString("argument_must_not_be_negative")); //$NON-NLS-1$
        }
        if (inWarningThreshold == Long.MAX_VALUE) {
            setProperty(new LongProperty(WARNING_THRESHOLD_KEY, 0));
        } else {
            setProperty(new LongProperty(WARNING_THRESHOLD_KEY, inWarningThreshold));
        }
    }

    /**
     * Enables html validation mode
     */
    public void setHTML() {
        setProperty(new LongProperty(FORMAT_KEY, 0));
    }

    /**
     * Check if html validation mode is set
     * 
     * @return boolean
     */
    public boolean isHTML() {
        return getPropertyAsLong(FORMAT_KEY) == 0;
    }

    /**
     * Enables xhtml validation mode
     */
    public void setXHTML() {
        setProperty(new LongProperty(FORMAT_KEY, 1));
    }

    /**
     * Check if xhtml validation mode is set
     * 
     * @return boolean
     */
    public boolean isXHTML() {
        return getPropertyAsLong(FORMAT_KEY) == 1;
    }

    /**
     * Enables xml validation mode
     */
    public void setXML() {
        setProperty(new LongProperty(FORMAT_KEY, 2));
    }

    /**
     * Check if xml validation mode is set
     * 
     * @return boolean
     */
    public boolean isXML() {
        return getPropertyAsLong(FORMAT_KEY) == 2;
    }

    /**
     * Sets the name of the file where tidy writes the output to
     * 
     * @return name of file
     */
    public String getFilename() {
        return getPropertyAsString(FILENAME_KEY);
    }

    /**
     * Sets the name of the tidy output file
     * 
     * @param inName The name of the file tidy will put its output to
     */
    public void setFilename(String inName) {
        setProperty(FILENAME_KEY, inName);
    }
}
