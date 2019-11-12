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

package org.apache.jmeter.protocol.http.gui.action;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.extractor.CreateBoundaryExtractor;
import org.apache.jmeter.extractor.CreateCssSelectorExtractor;
import org.apache.jmeter.extractor.CreateJsonPathExtractor;
import org.apache.jmeter.extractor.CreateRegexExtractor;
import org.apache.jmeter.extractor.CreateXPath2Extractor;
import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.CorrelationRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorrelationExtractor {

    private static final Logger log = LoggerFactory.getLogger(CorrelationExtractor.class);

    private static final String APPLICATION_JSON = "application/json"; //$NON-NLS-1$
    private static final String APPLICATION_XML = "application/xml"; //$NON-NLS-1$
    private static final String TEXT_HTML = "text/html"; //$NON-NLS-1$
    private static final String TEXT_XML = "text/xml"; //$NON-NLS-1$

    private static final String OTHER = "other"; //$NON-NLS-1$
    private static final String HEADER = "header"; //$NON-NLS-1$
    private static final String BOUNDARY = "boundary"; //$NON-NLS-1$

    private static List<Map<String, String>> listOfMap = new ArrayList<>();

    private CorrelationExtractor() {}

    /**
     * Read the response and create the extractor tags.
     *
     * @param parameters   List of correlation candidate parameters
     * @param parameterMap Map containing correlation candidates and their values
     */
    public static void readResponse(List<String> parameters, Map<String, String> parameterMap) {
        log.debug("Start Processing sample results in buffer object.");
        // Buffer contains API response data, iterate it and find parameter values in
        // response data
        for (Object sampler : CorrelationRecorder.buffer) {
            findParametersInResponse(sampler, parameters, parameterMap);
        }
        // update the JMX file with extractor tags
        log.debug("Processing sample results in buffer object ended.");
        updateJmxFile(parameterMap);
    }

    /**
     * Process the sampler and create extractors if the parameter was found in the response
     *
     * @param sampler      HTTP Sample Result
     * @param parameters   List of correlation candidate parameters
     * @param parameterMap Map containing correlation candidates and their values
     */
    public static void findParametersInResponse(Object sampler, List<String> parameters, Map<String, String> parameterMap) {
        SampleResult sampleResult = (SampleResult) sampler;
        String contentType = sampleResult.getContentType();
        // Find parameter in current Response (Do not process null response data(Body/Header))
        for (String parameter : parameters) {
            // TODO: support more content encodings
            String decodedParameter = "";
            try {
                decodedParameter = URLDecoder.decode(parameterMap.get(parameter),
                        sampleResult.getDataEncodingWithDefault());
            } catch (UnsupportedEncodingException e) {
                log.error("Cannot decode parameter {}", parameter);
                continue;
            }
            // Check if the parameter is found in response header
            // If found in response header then create regex extractor
            if (sampleResult.getResponseHeaders() != null
                    && sampleResult.getResponseHeaders().contains(decodedParameter)) {
                createExtractor(sampleResult, parameter, parameterMap, HEADER);
            }
            // Check if the parameter is found in response body and create extractors
            // accordingly
            else if (sampleResult.getResponseDataAsString() != null
                    && sampleResult.getResponseDataAsString().contains(decodedParameter)) {
                int numberOfExtractors = getListOfMap().size();
                // create extractor tag list
                if (contentType.contains(TEXT_HTML)) {
                    log.debug("Try to create HTML extractor for parameters in response of {}",
                            sampleResult.getSampleLabel());
                    createExtractor(sampleResult, parameter, parameterMap, TEXT_HTML);
                } else if (contentType.contains(APPLICATION_XML) || contentType.contains(TEXT_XML)) {
                    log.debug("Try to create XPath2 extractor for parameters in response of {}",
                            sampleResult.getSampleLabel());
                    createExtractor(sampleResult, parameter, parameterMap, APPLICATION_XML);
                } else if (contentType.contains(APPLICATION_JSON)) {
                    log.debug("Try to create JSONPath extractor for parameters in response of {}",
                            sampleResult.getSampleLabel());
                    createExtractor(sampleResult, parameter, parameterMap, APPLICATION_JSON);
                } else {
                    log.debug("Try to create Regex extractor for parameters in response of {}",
                            sampleResult.getSampleLabel());
                    createExtractor(sampleResult, parameter, parameterMap, OTHER);
                }
                // check if no extractor was added, if no then add default Boundary extractor
                if(getListOfMap().size() == numberOfExtractors) {
                    log.debug("Try to create Boundary extractor for parameters in response of {}",
                            sampleResult.getSampleLabel());
                    createExtractor(sampleResult, parameter, parameterMap, BOUNDARY);
                }
            }
        }
    }

    /**
     * Create the extractor tags based on the content type. eg. if
     * contentType:text/html then create the HTML extractor tag.
     *
     * @param sampleResult Result of the sampler containing response data
     * @param parameter    Parameter for which extractor is to be created
     * @param parameterMap Map containing correlation candidates and their values
     * @param contentType  Response content type
     */
    public static void createExtractor(SampleResult sampleResult, String parameter, Map<String, String> parameterMap,
            String contentType) {
        switch (contentType) {
        case TEXT_HTML:
            try {
                // Create CSS Selector Extractor, also called HTML Extractor
                createHtmlExtractor(sampleResult, parameter, parameterMap);
            } catch (UnsupportedEncodingException e) {
                log.error("Unable to create HTML Extractor for parameter {}, {}", parameter, e.getMessage());
            }
            break;
        case APPLICATION_XML:
            try {
                // Create XPath2 Extractor
                createXPath2Extractor(sampleResult, parameter, parameterMap);
            } catch (TransformerException | IllegalArgumentException e) {
                log.error("Unable to create XPath2 Extractor for parameter {}, {}", parameter, e.getMessage());
            }
            break;
        case APPLICATION_JSON:
            try {
                // Create JSONPath Extractor
                createJsonPathExtractor(sampleResult, parameter, parameterMap);
            } catch (IllegalArgumentException e) {
                log.error("Unable to create JSONPath Extractor for parameter {}, {}", parameter, e.getMessage());
            }
            break;
        case OTHER:
            // Create Regex Extractor for parameter in response body
            createRegexExtractor(sampleResult, parameter, parameterMap);
            break;
        case HEADER:
            // Create Regex Extractor for parameter in response header
            createRegexExtractorForHeaderParameter(sampleResult, parameter, parameterMap);
            break;
        case BOUNDARY:
            // Create Boundary Extractor for parameter in response body
            createBoundaryExtractor(sampleResult, parameter, parameterMap);
            break;
        default:
            return;
        }
    }

    private static void createHtmlExtractor(SampleResult sampleResult, String parameter,
            Map<String, String> parameterMap) throws UnsupportedEncodingException {
        Map<String, String> htmlExtractor = CreateCssSelectorExtractor.createCssSelectorExtractor(
                sampleResult.getResponseDataAsString(), parameterMap.get(parameter), parameter,
                sampleResult.getSampleLabel(), sampleResult.getContentType());
        if (htmlExtractor != null && htmlExtractor.size() > 0) {
            getListOfMap().add(htmlExtractor);
            log.debug("HTML Extractor created for {} in {}", parameter, sampleResult.getSampleLabel());
        }
    }

    private static void createXPath2Extractor(SampleResult sampleResult, String parameter,
            Map<String, String> parameterMap) throws TransformerException {
        Map<String, String> xPath2Extractor = CreateXPath2Extractor.createXPath2Extractor(
                sampleResult.getResponseDataAsString(), parameterMap.get(parameter), parameter,
                sampleResult.getSampleLabel(), sampleResult.getContentType());
        if (xPath2Extractor != null && xPath2Extractor.size() > 0) {
            getListOfMap().add(xPath2Extractor);
            log.debug("XPath2 Extractor created for {} in {}", parameter, sampleResult.getSampleLabel());
        }
    }

    private static void createJsonPathExtractor(SampleResult sampleResult, String parameter,
            Map<String, String> parameterMap) {
        Map<String, String> jsonPathExtractor = CreateJsonPathExtractor.createJsonPathExtractor(
                sampleResult.getResponseDataAsString(), parameterMap.get(parameter), parameter,
                sampleResult.getSampleLabel(), sampleResult.getContentType());
        if (jsonPathExtractor != null && jsonPathExtractor.size() > 0) {
            getListOfMap().add(jsonPathExtractor);
            log.debug("JSONPath Extractor created for {} in {}", parameter, sampleResult.getSampleLabel());
        }
    }

    private static void createRegexExtractor(SampleResult sampleResult, String parameter,
            Map<String, String> parameterMap) {
        List<Map<String, String>> regexExtractors = CreateRegexExtractor.createRegularExtractor(sampleResult, parameter,
                parameterMap);
        if (!regexExtractors.isEmpty()) {
            getListOfMap().addAll(regexExtractors);
            log.debug("Regex Extractor created for {} in {}", parameter, sampleResult.getSampleLabel());
        }
    }

    private static void createRegexExtractorForHeaderParameter(SampleResult sampleResult, String parameter,
            Map<String, String> parameterMap) {
        List<Map<String, String>> regexExtractorsForHeader = CreateRegexExtractor
                .createRegularExtractorForHeaderParameter(sampleResult, parameter, parameterMap);
        if (!regexExtractorsForHeader.isEmpty()) {
            getListOfMap().addAll(regexExtractorsForHeader);
            log.debug("Regex Extractor created for parameter in header {} in {}", parameter,
                    sampleResult.getSampleLabel());
        }
    }

    private static void createBoundaryExtractor(SampleResult sampleResult, String parameter,
            Map<String, String> parameterMap) {
        Map<String, String> boundaryExtractor = CreateBoundaryExtractor.createBoundaryExtractor(
                sampleResult.getResponseDataAsString(), parameterMap.get(parameter), parameter,
                sampleResult.getSampleLabel());
        if (!boundaryExtractor.isEmpty()) {
            getListOfMap().add(boundaryExtractor);
            log.debug("Boundary Extractor created for {} in {}", parameter,
                    sampleResult.getSampleLabel());
        }
    }

    /**
     * Create the extractor TestElement based on the extractor class.
     *
     * @param guiPackage             Current TestPlan GUI object
     * @param extractorTypeClassName Type of extractor to create
     * @param extractor              Map containing extractor data
     * @return TestElement object if extractor created else null
     */
    public static TestElement createExtractorTestElement(GuiPackage guiPackage, String extractorTypeClassName,
            Map<String, String> extractor) {
        TestElement testElement = guiPackage.createTestElement(extractorTypeClassName);
        // create the extractors TestElement objects based on their type
        if (testElement instanceof HtmlExtractor) {
            return CreateCssSelectorExtractor.createHtmlExtractorTestElement(extractor, testElement);
        } else if (testElement instanceof XPath2Extractor) {
            return CreateXPath2Extractor.createXPath2ExtractorTestElement(extractor, testElement);
        } else if (testElement instanceof JSONPostProcessor) {
            return CreateJsonPathExtractor.createJsonExtractorTestElement(extractor, testElement);
        } else if (testElement instanceof RegexExtractor) {
            return CreateRegexExtractor.createRegexExtractorTestElement(extractor, testElement);
        } else if (testElement instanceof BoundaryExtractor) {
            return CreateBoundaryExtractor.createBoundaryExtractorTestElement(extractor, testElement);
        }
        return null;
    }

    /**
     * Update the GUI with all the created extractors present in listOfMap
     *
     * @param parameterMap Map containing parameter name and values which are a
     *                         candidate for correlation
     */
    private static void updateJmxFile(Map<String, String> parameterMap) {
        try {
            if (!getListOfMap().isEmpty()) {
                Correlation.updateJxmFileWithExtractors(getListOfMap(), parameterMap);
            } else {
                JMeterUtils.reportErrorToUser(
                        "Could not find parameters in response data. Please check the logs for more information.", "Failure");
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Could not update the JMX file. {}", e.getMessage());
        }
    }

    /**
     * Get the list of Map containing Maps for extractors
     *
     * @return listOfExtractors
     */
    public static List<Map<String, String>> getListOfMap() {
        return listOfMap;
    }
}
