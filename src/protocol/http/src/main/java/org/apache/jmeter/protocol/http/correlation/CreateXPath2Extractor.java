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

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Optional;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.protocol.http.correlation.extractordata.ExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.XPath2ExtractorData;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.TransformerFactoryImpl;

public class CreateXPath2Extractor implements CreateExtractorInterface {

    private static final Logger log = LoggerFactory.getLogger(CreateXPath2Extractor.class);

    private static final String ONE = "1"; //$NON-NLS-1$

    CreateXPath2Extractor() {
    }

    /**
     * Create XPath2 Extractor
     *
     * @param extractorCreatorData ExtractorCreatorData object.
     * @return ExtractorData object
     */
    @Override
    public ExtractorData createExtractor(ExtractorCreatorData extractorCreatorData) {
        log.debug("Create ExtractorData data from ExtractorCreatorData "+ extractorCreatorData);
        XPath2ExtractorData xPath2Extractor = null;
        String xml = extractorCreatorData.getSampleResult().getResponseDataAsString();
        String value = extractorCreatorData.getParameterValue();
        String correlationVariableName = extractorCreatorData.getParameter();

        String requestUrl = extractorCreatorData.getSampleResult().getSampleLabel();
        String contentType = extractorCreatorData.getSampleResult().getContentType();

        if (xml == null || value == null) {
            throw new IllegalArgumentException("Response Data or value to be searched is null"); //$NON-NLS-1$
        }
        StringReader xmlResponse = new StringReader(xml);
        InputStream xslt = null;
        // fetch XSLT resource
        xslt = CreateXPath2Extractor.class.getResourceAsStream("CreateXPath2ExtractorXSLTransform.xml"); //$NON-NLS-1$
        if (xslt == null) {
            throw new IllegalArgumentException("Cannot find XSL Transform");
        }
        String xPathQuery = null;
        try {
            xPathQuery = getXPath(value, xmlResponse, xslt);
        } catch (TransformerException e) {
             log.error("XSL transform failed {}", e.getCause());
        }
        if (xPathQuery != null) {
            // Match No. = 1, as we are getting first occurrence of the element
            xPath2Extractor = new XPath2ExtractorData(correlationVariableName, xPathQuery, ONE, contentType,
                    requestUrl);
        }
        log.debug("XPath2ExtractorData data created from ExtractorCreatorData "+ xPath2Extractor);
        return xPath2Extractor;
    }

    /**
     * Perform XSL transform
     *
     * @param xml  document
     * @param xslt is XSL transform which creates XPath for all XML nodes
     * @return transformed XML
     * @throws TransformerException when XSL transformation failed
     */
    public static String transform(StringReader xml, InputStream xslt) throws TransformerException {
        Source xmlSource = new javax.xml.transform.stream.StreamSource(xml);
        Source xsltSource = new javax.xml.transform.stream.StreamSource(xslt);
        StringWriter sw = new StringWriter();
        Result result = new javax.xml.transform.stream.StreamResult(sw);
        TransformerFactoryImpl transFact = new TransformerFactoryImpl();
        Transformer trans = transFact.newTransformer(xsltSource);
        trans.transform(xmlSource, result);
        return sw.toString();
    }

    /**
     * Get XPath query expression of the value in xml
     *
     * @param value of Attribute/Text content in XML to create XPath
     * @param xml   document
     * @param xslt  is XSL transform which creates XPath for all XML nodes
     * @return XPath query expression or null if unable to find value in xml
     * @throws TransformerException when XSL transformation failed
     */
    public static String getXPath(String value, StringReader xml, InputStream xslt) throws TransformerException {
        // split the transformed xml and convert it to an array
        // Find the value in the the array and return its XPath
        // if present else return null
        Optional<String> xPath = Arrays.asList(transform(xml, xslt).split("\\r?\\n")).stream() //$NON-NLS-1$
                .filter(xpath -> xpath.contains("='" + value + "'")).findFirst(); //$NON-NLS-1$ //$NON-NLS-2$
        if (xPath.isPresent()) {
            return xPath.get().split("='" + value + "'")[0]; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return null;
        }
    }

    /**
     * Create XPath2 extractor TestElement
     *
     * @param extractordata   ExtractorData object.
     * @param testElement  TestElement object
     * @return xPath2extractor TestElement
     */
    @Override
    public TestElement createExtractorTestElement(ExtractorData extractordata, TestElement testElement) {
        XPath2ExtractorData extractor = (XPath2ExtractorData) extractordata;
        XPath2Extractor xPath2Extractor = (XPath2Extractor) testElement;
        xPath2Extractor.setName(extractor.getRefname());
        xPath2Extractor.setRefName(extractor.getRefname());
        xPath2Extractor.setXPathQuery(extractor.getxPathQuery());
        xPath2Extractor.setMatchNumber(extractor.getMatchNumber());
        return xPath2Extractor;
    }
}
