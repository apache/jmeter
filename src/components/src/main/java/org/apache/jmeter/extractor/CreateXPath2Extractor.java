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

package org.apache.jmeter.extractor;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.testelement.TestElement;

import net.sf.saxon.TransformerFactoryImpl;

public class CreateXPath2Extractor {

    public static final String XPATH2_EXTRACTOR_VARIABLE_NAME = "XPathExtractor2.refname"; //$NON-NLS-1$
    public static final String XPATH2_EXTRACTOR_MATCH_NO = "XPathExtractor2.matchNumber"; //$NON-NLS-1$
    public static final String XPATH2_EXTRACTOR_XPATHQUERY = "XPathExtractor2.xpathQuery"; //$NON-NLS-1$

    private static final String CONTENT_TYPE = "contentType"; //$NON-NLS-1$
    private static final String TEST_NAME = "testname"; //$NON-NLS-1$
    private static final String ONE = "1"; //$NON-NLS-1$

    private CreateXPath2Extractor() {}

    /**
     * Create XPath2 Extractor
     *
     * @param xml                     response as string
     * @param value                   of Attribute/Text content in XML to create
     *                                XPath
     * @param correlationVariableName alias of the correlated variable
     * @param requestUrl              URL of the request whose response yields the
     *                                parameter required to correlate
     * @param contentType             responseData content type
     * @return XPath2Extractor values in a map
     * @throws TransformerException when XLS transform failed
     */
    public static Map<String, String> createXPath2Extractor(String xml, String value, String correlationVariableName,
            String requestUrl, String contentType) throws TransformerException {
        Map<String, String> xPath2Extractor = new HashMap<>();
        if (xml == null || value == null) {
            throw new IllegalArgumentException("Response Data or value to be searched is null"); //$NON-NLS-1$
        }
        StringReader xmlResponse = new StringReader(xml);
        InputStream xslt = null;
        // fetch XLST resource
        xslt = CreateXPath2Extractor.class.getResourceAsStream("CreateXPath2ExtractorXLSTransform.xml"); //$NON-NLS-1$
        if (xslt == null) {
            throw new IllegalArgumentException("Cannot find XLS Transform");
        }
        String xPathQuery = getXPath(value, xmlResponse, xslt);
        if (xPathQuery == null) {
            // return empty map
            return xPath2Extractor;
        } else {
            xPath2Extractor.put(XPATH2_EXTRACTOR_VARIABLE_NAME, correlationVariableName);
            xPath2Extractor.put(XPATH2_EXTRACTOR_XPATHQUERY, xPathQuery);
            // Match No. = 1, as we are getting first occurrence of the element
            xPath2Extractor.put(XPATH2_EXTRACTOR_MATCH_NO, ONE);
            xPath2Extractor.put(CONTENT_TYPE, contentType);
            xPath2Extractor.put(TEST_NAME, requestUrl);
            return xPath2Extractor;
        }
    }

    /**
     * Perform XLS transform
     *
     * @param xml  document
     * @param xslt is XLS transform which creates XPath for all XML nodes
     * @return transformed XML
     * @throws TransformerException when XLS transformation failed
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
     * @param xslt  is XLS transform which creates XPath for all XML nodes
     * @return XPath query expression or null if unable to find value in xml
     * @throws TransformerException when XLS transformation failed
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
     * @param extractor Map containing extractor data
     * @param testElement empty testElement object
     * @return XPath2 extractor TestElement
     */
    public static TestElement createXPath2ExtractorTestElement(Map<String, String> extractor, TestElement testElement) {
        XPath2Extractor xPath2Extractor = (XPath2Extractor) testElement;
        xPath2Extractor.setName(extractor.get(XPATH2_EXTRACTOR_VARIABLE_NAME));
        xPath2Extractor.setRefName(extractor.get(XPATH2_EXTRACTOR_VARIABLE_NAME));
        xPath2Extractor.setXPathQuery(extractor.get(XPATH2_EXTRACTOR_XPATHQUERY));
        xPath2Extractor.setMatchNumber(extractor.get(XPATH2_EXTRACTOR_MATCH_NO));
        return xPath2Extractor;
    }
}
