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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class provides a few utility methods for dealing with XML/XPath.
 */
public class XPathUtil {
    private static final Logger log = LoggerFactory.getLogger(XPathUtil.class);

    private XPathUtil() {
        super();
    }

    private static DocumentBuilderFactory documentBuilderFactory;

    /**
     * Returns a suitable document builder factory.
     * Caches the factory in case the next caller wants the same options.
     *
     * @param validate should the parser validate documents?
     * @param whitespace should the parser eliminate whitespace in element content?
     * @param namespace should the parser be namespace aware?
     *
     * @return javax.xml.parsers.DocumentBuilderFactory
     */
    private static synchronized DocumentBuilderFactory makeDocumentBuilderFactory(boolean validate, boolean whitespace,
            boolean namespace) {
        if (XPathUtil.documentBuilderFactory == null || documentBuilderFactory.isValidating() != validate
                || documentBuilderFactory.isNamespaceAware() != namespace
                || documentBuilderFactory.isIgnoringElementContentWhitespace() != whitespace) {
            // configure the document builder factory
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setValidating(validate);
            documentBuilderFactory.setNamespaceAware(namespace);
            documentBuilderFactory.setIgnoringElementContentWhitespace(whitespace);
        }
        return XPathUtil.documentBuilderFactory;
    }

    /**
     * Create a DocumentBuilder using the makeDocumentFactory func.
     *
     * @param validate should the parser validate documents?
     * @param whitespace should the parser eliminate whitespace in element content?
     * @param namespace should the parser be namespace aware?
     * @param downloadDTDs if true, parser should attempt to resolve external entities
     * @return document builder
     * @throws ParserConfigurationException if {@link DocumentBuilder} can not be created for the wanted configuration
     */
    public static DocumentBuilder makeDocumentBuilder(boolean validate, boolean whitespace, boolean namespace, boolean downloadDTDs)
            throws ParserConfigurationException {
        DocumentBuilder builder = makeDocumentBuilderFactory(validate, whitespace, namespace).newDocumentBuilder();
        builder.setErrorHandler(new MyErrorHandler(validate, false));
        if (!downloadDTDs){
            EntityResolver er = new EntityResolver(){
                @Override
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException {
                    return new InputSource(new ByteArrayInputStream(new byte[]{}));
                }
            };
            builder.setEntityResolver(er);
        }
        return builder;
    }

    /**
     * Utility function to get new Document
     *
     * @param stream - Document Input stream
     * @param validate - Validate Document (not Tidy)
     * @param whitespace - Element Whitespace (not Tidy)
     * @param namespace - Is Namespace aware. (not Tidy)
     * @param tolerant - Is tolerant - i.e. use the Tidy parser
     * @param quiet - set Tidy quiet
     * @param showWarnings - set Tidy warnings
     * @param reportErrors - throw TidyException if Tidy detects an error
     * @param isXml - is document already XML (Tidy only)
     * @param downloadDTDs - if true, try to download external DTDs
     * @return document
     * @throws ParserConfigurationException when no {@link DocumentBuilder} can be constructed for the wanted configuration
     * @throws SAXException if parsing fails
     * @throws IOException if an I/O error occurs while parsing
     * @throws TidyException if a ParseError is detected and <code>report_errors</code> is <code>true</code>
     */
    public static Document makeDocument(InputStream stream, boolean validate, boolean whitespace, boolean namespace,
            boolean tolerant, boolean quiet, boolean showWarnings, boolean reportErrors, boolean isXml, boolean downloadDTDs)
            throws ParserConfigurationException, SAXException, IOException, TidyException {
        return makeDocument(stream, validate, whitespace, namespace,
                tolerant, quiet, showWarnings, reportErrors, isXml, downloadDTDs, null);
    }

    /**
     * Utility function to get new Document
     *
     * @param stream - Document Input stream
     * @param validate - Validate Document (not Tidy)
     * @param whitespace - Element Whitespace (not Tidy)
     * @param namespace - Is Namespace aware. (not Tidy)
     * @param tolerant - Is tolerant - i.e. use the Tidy parser
     * @param quiet - set Tidy quiet
     * @param showWarnings - set Tidy warnings
     * @param report_errors - throw TidyException if Tidy detects an error
     * @param isXml - is document already XML (Tidy only)
     * @param downloadDTDs - if true, try to download external DTDs
     * @param tidyOut OutputStream for Tidy pretty-printing
     * @return document
     * @throws ParserConfigurationException if {@link DocumentBuilder} can not be created for the wanted configuration
     * @throws SAXException if parsing fails
     * @throws IOException if I/O error occurs while parsing
     * @throws TidyException if a ParseError is detected and <code>report_errors</code> is <code>true</code>
     */
    public static Document makeDocument(InputStream stream, boolean validate, boolean whitespace, boolean namespace,
            boolean tolerant, boolean quiet, boolean showWarnings, boolean report_errors, boolean isXml, boolean downloadDTDs, 
            OutputStream tidyOut)
            throws ParserConfigurationException, SAXException, IOException, TidyException {
        Document doc;
        if (tolerant) {
            doc = tidyDoc(stream, quiet, showWarnings, report_errors, isXml, tidyOut);
        } else {
            doc = makeDocumentBuilder(validate, whitespace, namespace, downloadDTDs).parse(stream);
        }
        return doc;
    }

    /**
     * Create a document using Tidy
     *
     * @param stream - input
     * @param quiet - set Tidy quiet?
     * @param showWarnings - show Tidy warnings?
     * @param report_errors - log errors and throw TidyException?
     * @param isXML - treat document as XML?
     * @param out OutputStream, null if no output required
     * @return the document
     *
     * @throws TidyException if a ParseError is detected and report_errors is true
     */
    private static Document tidyDoc(InputStream stream, boolean quiet, boolean showWarnings, boolean report_errors,
            boolean isXML, OutputStream out) throws TidyException {
        StringWriter sw = new StringWriter();
        Tidy tidy = makeTidyParser(quiet, showWarnings, isXML, sw);
        Document doc = tidy.parseDOM(stream, out);
        doc.normalize();
        if (tidy.getParseErrors() > 0) {
            if (report_errors) {
                log.error("TidyException: {}", sw);
                throw new TidyException(tidy.getParseErrors(),tidy.getParseWarnings());
            }
            log.warn("Tidy errors: {}", sw);
        }
        return doc;
    }

    /**
     * Create a Tidy parser with the specified settings.
     *
     * @param quiet - set the Tidy quiet flag?
     * @param showWarnings - show Tidy warnings?
     * @param isXml - treat the content as XML?
     * @param stringWriter - if non-null, use this for Tidy errorOutput
     * @return the Tidy parser
     */
    public static Tidy makeTidyParser(boolean quiet, boolean showWarnings, boolean isXml, StringWriter stringWriter) {
        Tidy tidy = new Tidy();
        tidy.setInputEncoding(StandardCharsets.UTF_8.name());
        tidy.setOutputEncoding(StandardCharsets.UTF_8.name());
        tidy.setQuiet(quiet);
        tidy.setShowWarnings(showWarnings);
        tidy.setMakeClean(true);
        tidy.setXmlTags(isXml);
        if (stringWriter != null) {
            tidy.setErrout(new PrintWriter(stringWriter));
        }
        return tidy;
    }

    static class MyErrorHandler implements ErrorHandler {
        private final boolean val;
        private final boolean tol;

        private final String type;

        MyErrorHandler(boolean validate, boolean tolerate) {
            val = validate;
            tol = tolerate;
            type = "Val=" + val + " Tol=" + tol;
        }

        @Override
        public void warning(SAXParseException ex) throws SAXException {
            if (log.isInfoEnabled()) {
                log.info("Type={}. {}", type, ex.toString());
            }
            if (val && !tol){
                throw new SAXException(ex);
            }
        }

        @Override
        public void error(SAXParseException ex) throws SAXException {
            if (log.isWarnEnabled()) {
                log.warn("Type={}. {}", type, ex.toString());
            }
            if (val && !tol) {
                throw new SAXException(ex);
            }
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException {
            log.error("Type={}. {}", type, ex.toString());
            if (val && !tol) {
                throw new SAXException(ex);
            }
        }
    }
    
    /**
     * Return value for node including node element
     * @param node Node
     * @return String
     */
    private static String getNodeContent(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException e) {
            sw.write(e.getMessageAndLocation());
        }
        return sw.toString();
    }
    
    /**
     * @param node {@link Node}
     * @return String content of node
     */
    public static String getValueForNode(Node node) {
        // elements have empty nodeValue, but we are usually interested in their content
        final Node firstChild = node.getFirstChild();
        if (firstChild != null) {
            return firstChild.getNodeValue();
        } else {
            return node.getNodeValue();
        }
    }

    /**
     * Extract NodeList using expression
     * @param document {@link Document}
     * @param xPathExpression XPath expression
     * @return {@link NodeList}
     * @throws TransformerException when the internally used xpath engine fails
     */
    public static NodeList selectNodeList(Document document, String xPathExpression) throws TransformerException {
        XObject xObject = XPathAPI.eval(document, xPathExpression, getPrefixResolver(document));
        return xObject.nodelist();
    }

    /**
     * Put in matchStrings results of evaluation
     * @param document XML document
     * @param xPathQuery XPath Query
     * @param matchStrings List of strings that will be filled
     * @param fragment return fragment
     * @throws TransformerException when the internally used xpath engine fails
     */
    public static void putValuesForXPathInList(Document document, 
            String xPathQuery,
            List<String> matchStrings, boolean fragment) throws TransformerException {
        putValuesForXPathInList(document, xPathQuery, matchStrings, fragment, -1);
    }
    
    
    /**
     * Put in matchStrings results of evaluation
     * @param document XML document
     * @param xPathQuery XPath Query
     * @param matchStrings List of strings that will be filled
     * @param fragment return fragment
     * @param matchNumber match number
     * @throws TransformerException when the internally used xpath engine fails
     */
    public static void putValuesForXPathInList(Document document, 
            String xPathQuery,
            List<String> matchStrings, boolean fragment, 
            int matchNumber) throws TransformerException {
        String val = null;
        XObject xObject = XPathAPI.eval(document, xPathQuery, getPrefixResolver(document));
        final int objectType = xObject.getType();
        if (objectType == XObject.CLASS_NODESET) {
            NodeList matches = xObject.nodelist();
            int length = matches.getLength();
            int indexToMatch = matchNumber;
            if(matchNumber == 0 && length>0) {
                indexToMatch = JMeterUtils.getRandomInt(length)+1;
            } 
            for (int i = 0 ; i < length; i++) {
                Node match = matches.item(i);
                if(indexToMatch >= 0 && indexToMatch != (i+1)) {
                    continue;
                }
                if ( match instanceof Element ){
                    if (fragment){
                        val = getNodeContent(match);
                    } else {
                        val = getValueForNode(match);
                    }
                } else {
                   val = match.getNodeValue();
                }
                matchStrings.add(val);
            }
        } else if (objectType == XObject.CLASS_NULL
                || objectType == XObject.CLASS_UNKNOWN
                || objectType == XObject.CLASS_UNRESOLVEDVARIABLE) {
            if (log.isWarnEnabled()) {
                log.warn("Unexpected object type: {} returned for: {}", xObject.getTypeString(), xPathQuery);
            }
        } else {
            val = xObject.toString();
            matchStrings.add(val);
      }
    }

    /**
     * 
     * @param document XML Document
     * @return {@link PrefixResolver}
     */
    private static PrefixResolver getPrefixResolver(Document document) {
        return new PropertiesBasedPrefixResolver(document.getDocumentElement());
    }

    /**
     * Validate xpathString is a valid XPath expression
     * @param document XML Document
     * @param xpathString XPATH String
     * @throws TransformerException if expression fails to evaluate
     */
    public static void validateXPath(Document document, String xpathString) throws TransformerException {
        if (XPathAPI.eval(document, xpathString, getPrefixResolver(document)) == null) {
            // We really should never get here
            // because eval will throw an exception
            // if xpath is invalid, but whatever, better
            // safe
            throw new IllegalArgumentException("xpath eval of '" + xpathString + "' was null");
        }
    }

    /**
     * Fills result
     * @param result {@link AssertionResult}
     * @param doc XML Document
     * @param xPathExpression XPath expression
     * @param isNegated flag whether a non-match should be considered a success
     */
    public static void computeAssertionResult(AssertionResult result,
            Document doc, 
            String xPathExpression,
            boolean isNegated) {
        try {
            XObject xObject = XPathAPI.eval(doc, xPathExpression, getPrefixResolver(doc));
            switch (xObject.getType()) {
                case XObject.CLASS_NODESET:
                    NodeList nodeList = xObject.nodelist();
                    final int len = (nodeList != null) ? nodeList.getLength() : 0;
                    log.debug("nodeList length {}", len);
                    // length == 0 means nodelist is null 
                    if (len == 0) {
                        log.debug("nodeList is null or empty. No match by xpath expression: {}", xPathExpression);
                        result.setFailure(!isNegated);
                        result.setFailureMessage("No Nodes Matched " + xPathExpression);
                        return;
                    }
                    if (log.isDebugEnabled() && !isNegated) {
                        for (int i = 0; i < len; i++) {
                            log.debug("nodeList[{}]: {}", i, nodeList.item(i));
                        }
                    }
                    result.setFailure(isNegated);
                    if (isNegated) {
                        result.setFailureMessage("Specified XPath was found... Turn off negate if this is not desired");
                    }
                    return;
                case XObject.CLASS_BOOLEAN:
                    if (!xObject.bool()){
                        result.setFailure(!isNegated);
                        result.setFailureMessage("No Nodes Matched " + xPathExpression);
                    }
                    return;
                default:
                    result.setFailure(true);
                    result.setFailureMessage("Cannot understand: " + xPathExpression);
                    return;
            }
        } catch (TransformerException e) {
            result.setError(true);
            result.setFailureMessage(
                    new StringBuilder("TransformerException: ")
                    .append(e.getMessage())
                    .append(" for:")
                    .append(xPathExpression)
                    .toString());
        }
    }
    
    /**
     * Formats XML
     * @param xml string to format
     * @return String formatted XML
     */
    public static String formatXml(String xml){
        try {
            Transformer serializer= TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source xmlSource = new SAXSource(new InputSource(new StringReader(xml)));
            StringWriter stringWriter = new StringWriter();
            StreamResult res = new StreamResult(stringWriter);
            serializer.transform(xmlSource, res);
            return stringWriter.toString();
        } catch (Exception e) {
            return xml;
        }
    }

}
