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
import org.apache.jmeter.protocol.http.correlation.extractordata.XPath2ExtractorData;
import org.apache.jmeter.testelement.TestElement;

import net.sf.saxon.TransformerFactoryImpl;

public class CreateXPath2Extractor {

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
     * @throws TransformerException when XSL transform failed
     */
    public static XPath2ExtractorData createXPath2Extractor(String xml, String value, String correlationVariableName,
            String requestUrl, String contentType) throws TransformerException {
        XPath2ExtractorData xPath2Extractor = null;
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
        String xPathQuery = getXPath(value, xmlResponse, xslt);
        if (xPathQuery != null) {
            // Match No. = 1, as we are getting first occurrence of the element
            xPath2Extractor = new XPath2ExtractorData(correlationVariableName, xPathQuery, ONE, contentType, requestUrl);
        }
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
     * @param extractor Map containing extractor data
     * @param testElement empty testElement object
     * @return XPath2 extractor TestElement
     */
    public static TestElement createXPath2ExtractorTestElement(XPath2ExtractorData extractor, TestElement testElement) {
        XPath2Extractor xPath2Extractor = (XPath2Extractor) testElement;
        xPath2Extractor.setName(extractor.getRefname());
        xPath2Extractor.setRefName(extractor.getRefname());
        xPath2Extractor.setXPathQuery(extractor.getxPathQuery());
        xPath2Extractor.setMatchNumber(extractor.getMatchNumber());
        return xPath2Extractor;
    }
}
