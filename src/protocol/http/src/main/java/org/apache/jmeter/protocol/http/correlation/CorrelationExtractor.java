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

package org.apache.jmeter.protocol.http.correlation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.protocol.http.correlation.extractordata.BoundaryExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.ExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.HtmlExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.JsonPathExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.RegexExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.XPath2ExtractorData;
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

    private static List<ExtractorData> listOfExtractors = new ArrayList<>();

    private CorrelationExtractor() {}

    /**
     * Read the response and create the extractor tags.
     *
     * @param parameters   List of correlation candidate parameters
     * @param parameterMap Map containing correlation candidates and their values
     */
    public static void readResponse(List<String> parameters, Map<String, String> parameterMap) {
        log.debug("Start finding parameters in response data and create extractors.");
        // only one extractor for one parameter to be created
        for (String parameter : parameters) {
            ExtractorData extractor = createExtractorForParameter(parameter, parameterMap.get(parameter));
            if (extractor != null) {
                getListOfExtractor().add(extractor);
            }
        }
        // update the JMX file with extractor tags
        log.debug("Processing of finding parameters in response data and creation of extractors ended.");
        updateJmxFile(parameterMap);
    }

    private static ExtractorData createExtractorForParameter(String parameter, String parameterValue) {
        // Buffer contains API response data, iterate it and find parameter values in
        // response data
        for (Object sampler : CorrelationRecorder.getBuffer()) {
            SampleResult sampleResult = (SampleResult) sampler;
            String contentType = sampleResult.getContentType();
            // Find parameter in current Response (Do not process null response
            // data(Body/Header))
            // parameter is in decoded form in parameters List and parameterMap.
            // parameterValue can be found in response body in both
            // encoded or decoded form hence find both encoded and decoded
            // value of parameter in response data
            String decodedParameterValue = parameterValue;
            String encodedParameterValue = "";
            try {
                // try to encode the parameter with response's encoding
                encodedParameterValue = URLEncoder.encode(parameterValue, sampleResult.getDataEncodingWithDefault());
            } catch (UnsupportedEncodingException e) {
                log.error("Cannot encode parameter {}", parameter);
            }
            String responseHeader = sampleResult.getResponseHeaders();
            String responseData = sampleResult.getResponseDataAsString();
            // Check if the parameter is found in response header
            // If found in response header then create regex extractor
            if (StringUtils.isNotBlank(responseHeader) && responseHeader.contains(decodedParameterValue)) {
                ExtractorCreatorData creatorData = new ExtractorCreatorData();
                creatorData.setContentType(HEADER);
                creatorData.setParameter(parameter);
                creatorData.setParameterValue(parameterValue);
                creatorData.setSampleResult(sampleResult);
                ExtractorData extractor = createExtractor(creatorData);
                if (extractor != null) {
                    return extractor;
                }
            }
            // Check if the parameter (encoded/decoded) is found in response body
            // and create extractors accordingly
            else if (StringUtils.isNotBlank(responseData)
                    && (responseData.contains(decodedParameterValue) || (responseData.contains(encodedParameterValue)
                            && StringUtils.isNotBlank(encodedParameterValue)))) {
                ExtractorData extractor = createExtractorParamInResponseBody(sampleResult, parameter, parameterValue,
                        contentType);
                if (extractor != null) {
                    return extractor;
                }
            }
        }
        // if parameter not found in any response data, e.g username, password
        return null;
    }

    private static ExtractorData createExtractorParamInResponseBody(SampleResult sampleResult, String parameter,
            String parameterValue, String contentType) {
        ExtractorData createdExtractor = null;
        ExtractorCreatorData creatorData = null;
        // create extractor
        if (contentType.contains(TEXT_HTML)) {
            log.debug("Try to create HTML extractor for parameters in response of {}", sampleResult.getSampleLabel());
            creatorData = new ExtractorCreatorData();
            creatorData.setContentType(TEXT_HTML);
            creatorData.setParameter(parameter);
            creatorData.setParameterValue(parameterValue);
            creatorData.setSampleResult(sampleResult);
            // Note: It checks for only URL decoded parameter value
            // If html contains URL encoded parameter value then it will be
            // correlated by regex or boundary extractor
            createdExtractor = createExtractor(creatorData);
        } else if (contentType.contains(APPLICATION_XML) || contentType.contains(TEXT_XML)) {
            log.debug("Try to create XPath2 extractor for parameters in response of {}", sampleResult.getSampleLabel());
            creatorData = new ExtractorCreatorData();
            creatorData.setContentType(APPLICATION_XML);
            creatorData.setParameter(parameter);
            creatorData.setParameterValue(parameterValue);
            creatorData.setSampleResult(sampleResult);
            createdExtractor = createExtractor(creatorData);
        } else if (contentType.contains(APPLICATION_JSON)) {
            log.debug("Try to create JSONPath extractor for parameters in response of {}",
                    sampleResult.getSampleLabel());
            creatorData = new ExtractorCreatorData();
            creatorData.setContentType(APPLICATION_JSON);
            creatorData.setParameter(parameter);
            creatorData.setParameterValue(parameterValue);
            creatorData.setSampleResult(sampleResult);
            createdExtractor = createExtractor(creatorData);
        }
        // check no extractor was created, if no then try to create Regex Extractor
        if (createdExtractor == null) {
            // create Regex extractor which matches by name and value both
            // More accurate than Boundary extractor which matches by value only
            log.debug("Try to create Regex extractor for parameters in response of {}", sampleResult.getSampleLabel());
            creatorData = new ExtractorCreatorData();
            creatorData.setContentType(OTHER);
            creatorData.setParameter(parameter);
            creatorData.setParameterValue(parameterValue);
            creatorData.setSampleResult(sampleResult);
            createdExtractor = createExtractor(creatorData);
        }
        // check no extractor was created, if no then add default Boundary extractor
        if (createdExtractor == null) {
            log.debug("Try to create Boundary extractor for parameters in response of {}",
                    sampleResult.getSampleLabel());
            creatorData = new ExtractorCreatorData();
            creatorData.setContentType(BOUNDARY);
            creatorData.setParameter(parameter);
            creatorData.setParameterValue(parameterValue);
            creatorData.setSampleResult(sampleResult);
            createdExtractor = createExtractor(creatorData);
        }
        return createdExtractor;
    }

    /**
     * Create the extractor based on the content type. eg. if
     * contentType:text/html then create the HTML extractor.
     * @param extractorCreatorData     ExtractorCreatorData object.
     * @return ExtractorData object or null
     */
    public static ExtractorData createExtractor(ExtractorCreatorData extractorCreatorData) {
        switch (extractorCreatorData.getContentType()) {
        case TEXT_HTML:
            // Create CSS Selector Extractor, also called HTML Extractor
            return createHtmlExtractor(extractorCreatorData);
        case APPLICATION_XML:
            try {
                // Create XPath2 Extractor
                return createXPath2Extractor(extractorCreatorData);
            } catch (TransformerException | IllegalArgumentException e) {
                log.error("Unable to create XPath2 Extractor for parameter {}, {}", extractorCreatorData.getParameter(), e.getMessage());
                return null;
            }
        case APPLICATION_JSON:
            try {
                // Create JSONPath Extractor
                return createJsonPathExtractor(extractorCreatorData);
            } catch (IllegalArgumentException e) {
                log.error("Unable to create JSONPath Extractor for parameter {}, {}", extractorCreatorData.getParameter(), e.getMessage());
                return null;
            }
        case OTHER:
            // Create Regex Extractor for parameter in response body
            return createRegexExtractor(extractorCreatorData);
        case HEADER:
            // Create Regex Extractor for parameter in response header
            return createRegexExtractorForHeaderParameter(extractorCreatorData);
        case BOUNDARY:
            // Create Boundary Extractor for parameter in response body
            return createBoundaryExtractor(extractorCreatorData);
        default:
            return null;
        }
    }


    private static HtmlExtractorData createHtmlExtractor(ExtractorCreatorData extractorCreatorData) {
        CreateExtractorInterface createExtractorInterface = new CreateCssSelectorExtractor();
        HtmlExtractorData htmlExtractorData = (HtmlExtractorData) createExtractorInterface.createExtractor(extractorCreatorData);
        if (htmlExtractorData != null) {
            log.debug("HTML Extractor created for {} in {}", extractorCreatorData.getParameter(), extractorCreatorData.getSampleResult().getSampleLabel());
        }
        return htmlExtractorData;
    }


    private static XPath2ExtractorData createXPath2Extractor(ExtractorCreatorData extractorCreatorData) throws TransformerException {
        CreateExtractorInterface createExtractorInterface =new CreateXPath2Extractor();
        XPath2ExtractorData xPath2Extractor = (XPath2ExtractorData) createExtractorInterface.createExtractor(extractorCreatorData);
        if (xPath2Extractor != null) {
            log.debug("XPath2 Extractor created for {} in {}", extractorCreatorData.getParameter(), extractorCreatorData.getSampleResult().getSampleLabel());
        }
        return xPath2Extractor;
    }


    private static JsonPathExtractorData createJsonPathExtractor(ExtractorCreatorData extractorCreatorData) {
        CreateExtractorInterface createExtractorInterface = new CreateJsonPathExtractor();
        JsonPathExtractorData jsonPathExtractor = (JsonPathExtractorData) createExtractorInterface.createExtractor(extractorCreatorData);
        if (jsonPathExtractor != null) {
            log.debug("JSONPath Extractor created for {} in {}", extractorCreatorData.getParameter(), extractorCreatorData.getSampleResult().getSampleLabel());
        }
        return jsonPathExtractor;
    }


    private static RegexExtractorData createRegexExtractor(ExtractorCreatorData extractorCreatorData) {
        CreateExtractorInterface createExtractorInterface = new CreateRegexExtractor();
        RegexExtractorData regexExtractor = (RegexExtractorData) createExtractorInterface.createExtractor(extractorCreatorData);
        if (regexExtractor != null) {
            log.debug("Regex Extractor created for {} in {}", extractorCreatorData.getParameter(), extractorCreatorData.getSampleResult().getSampleLabel());
        }
        return regexExtractor;
    }


    private static RegexExtractorData createRegexExtractorForHeaderParameter(ExtractorCreatorData extractorCreatorData) {
        RegexExtractorData regexExtractorForHeader = CreateRegexExtractor.createRegularExtractorForHeaderParameter(
                extractorCreatorData.getSampleResult(), extractorCreatorData.getParameter(),
                extractorCreatorData.getParameterValue());
        if (regexExtractorForHeader != null) {
            log.debug("Regex Extractor created for parameter in header {} in {}", extractorCreatorData.getParameter(),
                    extractorCreatorData.getSampleResult().getSampleLabel());
        }
        return regexExtractorForHeader;
    }

    private static BoundaryExtractorData createBoundaryExtractor(ExtractorCreatorData extractorCreatorData) {
        CreateExtractorInterface createExtractorInterface = new CreateBoundaryExtractor();
        BoundaryExtractorData boundaryExtractor =(BoundaryExtractorData) createExtractorInterface.createExtractor(extractorCreatorData);
        if (boundaryExtractor != null) {
            log.debug("Boundary Extractor created for {} in {}", extractorCreatorData.getParameter(), extractorCreatorData.getSampleResult().getSampleLabel());
        }
        return boundaryExtractor;
    }

    /**
     * Create the extractor TestElement based on the extractor class.
     *
     * @param guiPackage             Current TestPlan GUI object
     * @param extractorTypeClassName Extractor Type class name
     * @param extractor              ExtractorData object
     * @return TestElement object if extractor created else null
     */
    public static TestElement createExtractorTestElement(GuiPackage guiPackage, String extractorTypeClassName,
            ExtractorData extractor) {
        TestElement testElement = guiPackage.createTestElement(extractorTypeClassName);
        CreateExtractorInterface createExtractorInterface = null;
        // create the extractors TestElement objects based on their type
        if (testElement instanceof HtmlExtractor) {
            createExtractorInterface = new CreateCssSelectorExtractor();
            return createExtractorInterface.createExtractorTestElement((HtmlExtractorData) extractor,
                    testElement);
        } else if (testElement instanceof XPath2Extractor) {
            createExtractorInterface = new CreateXPath2Extractor();
            return createExtractorInterface.createExtractorTestElement((XPath2ExtractorData) extractor, testElement);
        } else if (testElement instanceof JSONPostProcessor) {
            createExtractorInterface = new CreateJsonPathExtractor();
            return createExtractorInterface.createExtractorTestElement((JsonPathExtractorData) extractor,
                    testElement);
        } else if (testElement instanceof RegexExtractor) {
            createExtractorInterface = new CreateRegexExtractor();
            return createExtractorInterface.createExtractorTestElement((RegexExtractorData) extractor, testElement);
        } else if (testElement instanceof BoundaryExtractor) {
            createExtractorInterface= new CreateBoundaryExtractor();
            return createExtractorInterface.createExtractorTestElement(extractor,
                    testElement);
        }
        return null;
    }

    /**
     * Update the GUI with all the created extractors present in listOfMap
     *
     * @param parameterMap Map containing parameter name and values which are a
     *                     candidate for correlation
     */
    private static void updateJmxFile(Map<String, String> parameterMap) {
        if (!getListOfExtractor().isEmpty()) {
            Correlation.updateJxmFileWithExtractors(getListOfExtractor(), parameterMap);
        } else {
            // show error to user if no extractors could be created for the selected
            // parameters
            JMeterUtils.reportErrorToUser("No extractors could be created for the selected parameters. Could not correlate the script.",
                    "Failure");
        }
    }

    /**
     * Get the list of Map containing Maps for extractors
     *
     * @return listOfExtractors
     */
    public static List<ExtractorData> getListOfExtractor() {
        return listOfExtractors;
    }
}
