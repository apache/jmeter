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

package org.apache.jmeter.protocol.http.proxy;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.config.GraphQLRequestParams;
import org.apache.jmeter.protocol.http.config.MultipartUrlConfig;
import org.apache.jmeter.protocol.http.config.gui.GraphQLUrlConfigGui;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.gui.GraphQLHTTPSamplerGui;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.PostWriter;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.apache.jmeter.protocol.http.util.GraphQLRequestParamUtils;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;

/**
 * Default implementation that handles classical HTTP textual + Multipart requests
 */
@AutoService(SamplerCreator.class)
public class DefaultSamplerCreator extends AbstractSamplerCreator {
    private static final Logger log = LoggerFactory.getLogger(DefaultSamplerCreator.class);

    /*
    * Must be the same order than in org.apache.jmeter.protocol.http.proxy.gui.ProxyControlGui class in createHTTPSamplerPanel method
    */
    private static final int SAMPLER_NAME_NAMING_MODE_PREFIX = 0;  // $NON-NLS-1$
    private static final int SAMPLER_NAME_NAMING_MODE_COMPLETE = 1;  // $NON-NLS-1$
    private static final int SAMPLER_NAME_NAMING_MODE_SUFFIX = 2; // $NON-NLS-1$
    private static final int SAMPLER_NAME_NAMING_MODE_FORMATTER = 3; // $NON_NLS-1$

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /**
     *
     */
    public DefaultSamplerCreator() {
        super();
    }

    /**
     * @see org.apache.jmeter.protocol.http.proxy.SamplerCreator#getManagedContentTypes()
     */
    @Override
    public String[] getManagedContentTypes() {
        return new String[0];
    }

    /**
     *
     * @see org.apache.jmeter.protocol.http.proxy.SamplerCreator#createSampler(org.apache.jmeter.protocol.http.proxy.HttpRequestHdr,
     *      java.util.Map, java.util.Map)
     */
    @Override
    public HTTPSamplerBase createSampler(HttpRequestHdr request,
            Map<String, String> pageEncodings, Map<String, String> formEncodings) {
        // Instantiate the sampler
        HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance(request.getHttpSamplerName());

        sampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        // Defaults
        sampler.setFollowRedirects(false);
        sampler.setUseKeepAlive(true);

        if (log.isDebugEnabled()) {
            log.debug("getSampler: sampler path = {}", sampler.getPath());
        }
        return sampler;
    }

    /**
     * @see org.apache.jmeter.protocol.http.proxy.SamplerCreator#populateSampler(org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase,
     *      org.apache.jmeter.protocol.http.proxy.HttpRequestHdr, java.util.Map,
     *      java.util.Map)
     */
    @Override
    public final void populateSampler(HTTPSamplerBase sampler,
            HttpRequestHdr request, Map<String, String> pageEncodings,
            Map<String, String> formEncodings) throws Exception{
        computeFromHeader(sampler, request, pageEncodings, formEncodings);

        computeFromPostBody(sampler, request);
        if (log.isDebugEnabled()) {
            log.debug("sampler path = {}", sampler.getPath());
        }
        Arguments arguments = sampler.getArguments();
        if(arguments.getArgumentCount() == 1 && arguments.getArgument(0).getName().length()==0) {
            sampler.setPostBodyRaw(true);
        }

        if (request.isDetectGraphQLRequest()) {
            detectAndModifySamplerOnGraphQLRequest(sampler, request);
        }
    }

    private static void detectAndModifySamplerOnGraphQLRequest(final HTTPSamplerBase sampler, final HttpRequestHdr request) {
        final String method = request.getMethod();
        final Header header = request.getHeaderManager().getFirstHeaderNamed("Content-Type");
        final boolean graphQLContentType = header != null
                && GraphQLRequestParamUtils.isGraphQLContentType(header.getValue());

        GraphQLRequestParams params = null;

        if (HTTPConstants.POST.equals(method) && graphQLContentType) {
            try {
                byte[] postData = request.getRawPostData();
                if (postData != null && postData.length > 0) {
                    params = GraphQLRequestParamUtils.toGraphQLRequestParams(request.getRawPostData(),
                            sampler.getContentEncoding());
                }
            } catch (Exception e) {
                log.debug("Ignoring request, '{}' as it's not a valid GraphQL post data.", request);
            }
        } else if (HTTPConstants.GET.equals(method)) {
            try {
                params = GraphQLRequestParamUtils.toGraphQLRequestParams(sampler.getArguments(),
                        sampler.getContentEncoding());
            } catch (Exception e) {
                log.debug("Ignoring request, '{}' as it does not valid GraphQL arguments.", request);
            }
        }

        if (params != null) {
            sampler.setProperty(TestElement.GUI_CLASS, GraphQLHTTPSamplerGui.class.getName());
            sampler.setProperty(GraphQLUrlConfigGui.OPERATION_NAME, params.getOperationName());
            sampler.setProperty(GraphQLUrlConfigGui.QUERY, params.getQuery());
            sampler.setProperty(GraphQLUrlConfigGui.VARIABLES, params.getVariables());
        }
    }

    /**
     * Compute sampler information from Request Header
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     * @param pageEncodings Map of page encodings
     * @param formEncodings Map of form encodings
     * @throws Exception when something fails
     */
    protected void computeFromHeader(HTTPSamplerBase sampler,
            HttpRequestHdr request, Map<String, String> pageEncodings,
            Map<String, String> formEncodings) throws Exception {
        computeDomain(sampler, request);

        computeMethod(sampler, request);

        computePort(sampler, request);

        computeProtocol(sampler, request);

        computeContentEncoding(sampler, request,
                pageEncodings, formEncodings);

        computePath(sampler, request);

        computeSamplerName(sampler, request);
    }

    /**
     * Compute sampler information from Request Header
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     * @throws Exception when something fails
     */
    protected void computeFromPostBody(HTTPSamplerBase sampler,
            HttpRequestHdr request) throws Exception {
        // If it was a HTTP GET request, then all parameters in the URL
        // has been handled by the sampler.setPath above, so we just need
        // to do parse the rest of the request if it is not a GET request
        if(!HTTPConstants.CONNECT.equals(request.getMethod()) && !HTTPConstants.GET.equals(request.getMethod())) {
            // Check if it was a multipart http post request
            final String contentType = request.getContentType();
            MultipartUrlConfig urlConfig = request.getMultipartConfig(contentType);
            String contentEncoding = sampler.getContentEncoding();
            // Get the post data using the content encoding of the request
            String postData = null;
            if (log.isDebugEnabled()) {
                if(!StringUtils.isEmpty(contentEncoding)) {
                    log.debug("Using encoding {} for request body", contentEncoding);
                }
                else {
                    log.debug("No encoding found, using JRE default encoding for request body");
                }
            }


            if (!StringUtils.isEmpty(contentEncoding)) {
                postData = new String(request.getRawPostData(), contentEncoding);
            } else {
                // Use default encoding
                postData = new String(request.getRawPostData(), PostWriter.ENCODING);
            }

            if (urlConfig != null) {
                urlConfig.parseArguments(postData);
                // Tell the sampler to do a multipart post
                sampler.setDoMultipart(true);
                // Remove the header for content-type and content-length, since
                // those values will most likely be incorrect when the sampler
                // performs the multipart request, because the boundary string
                // will change
                request.getHeaderManager().removeHeaderNamed(HttpRequestHdr.CONTENT_TYPE);
                request.getHeaderManager().removeHeaderNamed(HttpRequestHdr.CONTENT_LENGTH);

                // Set the form data
                sampler.setArguments(urlConfig.getArguments());
                // Set the file uploads
                sampler.setHTTPFiles(urlConfig.getHTTPFileArgs().asArray());
                sampler.setDoBrowserCompatibleMultipart(true); // we are parsing browser input here
            // used when postData is pure xml (eg. an xml-rpc call) or for PUT
            } else if (postData.trim().startsWith("<?")
                    || HTTPConstants.PUT.equals(sampler.getMethod())
                    || isPotentialXml(postData)
                    || isPotentialJson(postData)) {
                sampler.addNonEncodedArgument("", postData, "");
            } else if (contentType == null ||
                    (contentType.startsWith(HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED) &&
                            !isBinaryContent(contentType))) {
                // It is the most common post request, with parameter name and values
                // We also assume this if no content type is present, to be most backwards compatible,
                // but maybe we should only parse arguments if the content type is as expected
                sampler.parseArguments(postData.trim(), contentEncoding); //standard name=value postData
            } else if (postData.length() > 0) {
                if (isBinaryContent(contentType)) {
                    try {
                        File tempDir = new File(getBinaryDirectory());
                        File out = File.createTempFile(request.getMethod(), getBinaryFileSuffix(), tempDir);
                        FileUtils.writeByteArrayToFile(out,request.getRawPostData());
                        HTTPFileArg [] files = {new HTTPFileArg(out.getPath(),"",contentType)};
                        sampler.setHTTPFiles(files);
                    } catch (IOException e) {
                        log.warn("Could not create binary file: {}", e);
                    }
                } else {
                    // Just put the whole postbody as the value of a parameter
                    sampler.addNonEncodedArgument("", postData, ""); //used when postData is pure xml (ex. an xml-rpc call)
                }
            }
        }
    }

    /**
     * Tries parsing to see if content is JSON
     * @param postData String
     * @return boolean
     */
    public static boolean isPotentialJson(final String postData) {
        boolean valid = true;
        try{
            OBJECT_MAPPER.readTree(postData);
        } catch(JsonProcessingException e){
            valid = false;
        }
        log.debug("Is Post data {} JSON ? {}", postData, valid);
        return valid;
    }

    /**
     * Tries parsing to see if content is xml
     * @param postData String
     * @return boolean
     */
    private static boolean isPotentialXml(String postData) {
        boolean isXml;
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            ErrorDetectionHandler detectionHandler =
                    new ErrorDetectionHandler();
            xmlReader.setContentHandler(detectionHandler);
            xmlReader.setErrorHandler(detectionHandler);
            xmlReader.parse(new InputSource(new StringReader(postData)));
            isXml = !detectionHandler.isErrorDetected();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            isXml = false;
        }
        log.debug("Is Post data {} XML ? {}", postData, isXml);
        return isXml;
    }

    private static final class ErrorDetectionHandler extends DefaultHandler {
        private boolean errorDetected = false;
        public ErrorDetectionHandler() {
            super();
        }
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
         */
        @Override
        public void error(SAXParseException e) throws SAXException {
            this.errorDetected = true;
        }

        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
         */
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            this.errorDetected = true;
        }
        /**
         * @return the errorDetected
         */
        public boolean isErrorDetected() {
            return errorDetected;
        }
    }
    /**
     * Compute sampler name
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     */
    protected static void computeSamplerName(HTTPSamplerBase sampler,
            HttpRequestHdr request) {
        String prefix = StringUtils.defaultString(request.getPrefix(), "");
        int httpSampleNameMode = request.getHttpSampleNameMode();
        String format = getFormat(httpSampleNameMode, request.getHttpSampleNameFormat());
        String url;
        try {
            url = sampler.getUrl().toString();
        } catch (MalformedURLException e) {
            // If path could not be converted to URL, retain path component
            url = sampler.getPath();
            log.warn("Could not get URL to name sample", e);
        }
        List<Object> values = Arrays.asList(
                prefix,
                sampler.getPath(),
                sampler.getMethod(),
                sampler.getDomain(),
                sampler.getProtocol(),
                sampler.getPort(),
                url
        );
        Object[] valuesArray;
        if (!HTTPConstants.CONNECT.equals(request.getMethod()) && isNumberRequests()) {
            valuesArray = values.toArray(new Object[values.size() + 1]);
            valuesArray[values.size()] = incrementRequestNumberAndGet();
        } else {
            valuesArray = values.toArray();
        }
        sampler.setName(MessageFormat.format(format,valuesArray));
    }

    private static String getFormat(int httpSampleNameMode, String format) {
        if (httpSampleNameMode == SAMPLER_NAME_NAMING_MODE_FORMATTER) {
            return format.replaceAll("\\{(\\d+(,[^}]*)?)\\}", "'{'$1'}'")
                    .replaceAll("#\\{name([,}])", "{0$1")
                    .replaceAll("#\\{path([,}])", "{1$1")
                    .replaceAll("#\\{method([,}])", "{2$1")
                    .replaceAll("#\\{host([,}])", "{3$1")
                    .replaceAll("#\\{scheme([,}])", "{4$1")
                    .replaceAll("#\\{port([,}])", "{5$1")
                    .replaceAll("#\\{url([,}])", "{6$1")
                    .replaceAll("#\\{counter([,}])", "{7$1");
        }
        if (isNumberRequests()) {
            return getNumberedFormat(httpSampleNameMode);
        }
        if (httpSampleNameMode == SAMPLER_NAME_NAMING_MODE_PREFIX) {
            return "{0}{1}";
        }
        if (httpSampleNameMode == SAMPLER_NAME_NAMING_MODE_COMPLETE) {
            return "{0}";
        }
        if (httpSampleNameMode == SAMPLER_NAME_NAMING_MODE_SUFFIX) {
            return "{0} {1}";
        }
        return "{1}";
    }

    private static String getNumberedFormat(int httpSampleNameMode) {
        if (httpSampleNameMode == SAMPLER_NAME_NAMING_MODE_PREFIX) {
            return "{0}{1}-{7}";
        }
        if (httpSampleNameMode == SAMPLER_NAME_NAMING_MODE_COMPLETE) {
            return "{0}-{7}";
        }
        if (httpSampleNameMode == SAMPLER_NAME_NAMING_MODE_SUFFIX) {
            return "{0}-{7} {1}";
        }
        return "{1}";
    }

    /**
     * Set path on sampler
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     */
    protected void computePath(HTTPSamplerBase sampler, HttpRequestHdr request) {
        if(sampler.getContentEncoding() != null) {
            sampler.setPath(request.getPath(), sampler.getContentEncoding());
        }
        else {
            // Although the spec says UTF-8 should be used for encoding URL parameters,
            // most browser use ISO-8859-1 for default if encoding is not known.
            // We use null for contentEncoding, then the url parameters will be added
            // with the value in the URL, and the "encode?" flag set to false
            sampler.setPath(request.getPath(), null);
        }
        if (log.isDebugEnabled()) {
            log.debug("Proxy: finished setting path: {}", sampler.getPath());
        }
    }

    /**
     * Compute content encoding
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     * @param pageEncodings Map of page encodings
     * @param formEncodings Map of form encodings
     * @throws MalformedURLException when no {@link URL} could be built from
     *         <code>sampler</code> and <code>request</code>
     */
    protected void computeContentEncoding(HTTPSamplerBase sampler,
            HttpRequestHdr request, Map<String, String> pageEncodings,
            Map<String, String> formEncodings) throws MalformedURLException {
        URL pageUrl;
        if(sampler.isProtocolDefaultPort()) {
            pageUrl = new URL(sampler.getProtocol(), sampler.getDomain(), request.getPath());
        }
        else {
            pageUrl = new URL(sampler.getProtocol(), sampler.getDomain(),
                    sampler.getPort(), request.getPath());
        }
        String urlWithoutQuery = request.getUrlWithoutQuery(pageUrl);


        String contentEncoding = computeContentEncoding(request, pageEncodings,
                formEncodings, urlWithoutQuery);

        // Set the content encoding
        if(!StringUtils.isEmpty(contentEncoding)) {
            sampler.setContentEncoding(contentEncoding);
        }
    }

    /**
     * Computes content encoding from request and if not found uses pageEncoding
     * and formEncoding to see if URL was previously computed with a content type
     * @param request {@link HttpRequestHdr}
     * @param pageEncodings Map of page encodings
     * @param formEncodings Map of form encodings
     * @param urlWithoutQuery the request URL without the query parameters
     * @return String content encoding
     */
    protected String computeContentEncoding(HttpRequestHdr request,
            Map<String, String> pageEncodings,
            Map<String, String> formEncodings, String urlWithoutQuery) {
        // Check if the request itself tells us what the encoding is
        String contentEncoding;
        String requestContentEncoding = ConversionUtils.getEncodingFromContentType(
                request.getContentType());
        if(requestContentEncoding != null) {
            contentEncoding = requestContentEncoding;
        }
        else {
            // Check if we know the encoding of the page
            contentEncoding = pageEncodings.get(urlWithoutQuery);
            log.debug("Computed encoding:{} for url:{}", contentEncoding, urlWithoutQuery);
            // Check if we know the encoding of the form
            String formEncoding = formEncodings.get(urlWithoutQuery);
            // Form encoding has priority over page encoding
            if (formEncoding != null) {
                contentEncoding = formEncoding;
                log.debug("Computed encoding:{} for url:{}", contentEncoding, urlWithoutQuery);
            }
        }
        if (contentEncoding == null) {
            contentEncoding = pageEncodings.get(DEFAULT_ENCODING_KEY);
            log.debug("Defaulting to encoding:{} for url:{}", contentEncoding, urlWithoutQuery);
        }
        return contentEncoding;
    }

    /**
     * Set protocol on sampler
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     */
    protected void computeProtocol(HTTPSamplerBase sampler,
            HttpRequestHdr request) {
        sampler.setProtocol(request.getProtocol(sampler));
    }

    /**
     * Set Port on sampler
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     */
    protected void computePort(HTTPSamplerBase sampler, HttpRequestHdr request) {
        sampler.setPort(request.serverPort());
        if (log.isDebugEnabled()) {
            log.debug("Proxy: setting port: {}", Integer.toString(sampler.getPort()));
        }
    }

    /**
     * Set method on sampler
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     */
    protected void computeMethod(HTTPSamplerBase sampler, HttpRequestHdr request) {
        sampler.setMethod(request.getMethod());
        log.debug("Proxy: setting method: {}", sampler.getMethod());
    }

    /**
     * Set domain on sampler
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     */
    protected void computeDomain(HTTPSamplerBase sampler, HttpRequestHdr request) {
        sampler.setDomain(request.serverName());
        if (log.isDebugEnabled()) {
            log.debug("Proxy: setting server: {}", sampler.getDomain());
        }
    }
}
