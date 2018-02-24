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

package org.apache.jmeter.protocol.http.control;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread to handle one client request. Gets the request from the client and
 * sends the response back to the client.
 * The server responds to some header settings:
 * X-ResponseStatus - the response code/message; default "200 OK"
 * X-SetHeaders - pipe-separated list of headers to return
 * X-ResponseLength - truncates the response to the stated length
 * X-SetCookie - set a cookie
 * X-Sleep - sleep before returning
 *
 * It also responds to some query strings:
 * status=nnn Message (overrides X-ResponseStatus)
 * redirect=location - sends a temporary redirect
 * v - verbose, i.e. print some details to stdout
 */
public class HttpMirrorThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(HttpMirrorThread.class);

    private static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

    private static final byte[] CRLF = { 0x0d, 0x0a };

    private static final String REDIRECT = "redirect"; //$NON-NLS-1$

    private static final String STATUS = "status"; //$NON-NLS-1$

    private static final String VERBOSE = "v"; // $NON-NLS-1$

    /** Socket to client. */
    private final Socket clientSocket;

    public HttpMirrorThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Main processing method for the HttpMirror object
     */
    @Override
    public void run() {
        log.debug("Starting thread");
        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        try {
            in = new BufferedInputStream(clientSocket.getInputStream());

            // Read the header part, we will be looking for a content-length
            // header, so we know how much we should read.
            // We assume headers are in ISO_8859_1
            // If we do not find such a header, we will just have to read until
            // we have to block to read more, until we support chunked transfer
            int contentLength = -1;
            boolean isChunked = false;
            byte[] buffer = new byte[1024];
            StringBuilder headers = new StringBuilder();
            int length = 0;
            int positionOfBody = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while(positionOfBody <= 0 && ((length = in.read(buffer)) != -1)) {
                log.debug("Write body");
                baos.write(buffer, 0, length); // echo back
                headers.append(new String(buffer, 0, length, ISO_8859_1));
                // Check if we have read all the headers
                positionOfBody = getPositionOfBody(headers.toString());
            }

            baos.close();
            final String headerString = headers.toString();
            if(headerString.length() == 0 || headerString.indexOf('\r') < 0) {
                log.error("Invalid request received:'{}'", headerString);
                return;
            }
            log.debug("Received => '{}'", headerString);
            final String firstLine = headerString.substring(0, headerString.indexOf('\r'));
            final String[] requestParts = firstLine.split("\\s+");
            final String requestMethod = requestParts[0];
            final String requestPath = requestParts[1];
            final HashMap<String, String> parameters = new HashMap<>();
            if (HTTPConstants.GET.equals(requestMethod)) {
                int querypos = requestPath.indexOf('?');
                if (querypos >= 0) {
                    String query;
                    try {
                        URI uri = new URI(requestPath); // Use URI because it will decode the query
                        query = uri.getQuery();
                    } catch (URISyntaxException e) {
                        log.warn(e.getMessage());
                        query=requestPath.substring(querypos+1);
                    }
                    if (query != null) {
                        String[] params = query.split("&");
                        for(String param : params) {
                            String[] parts = param.split("=",2);
                            if (parts.length==2) {
                                parameters.put(parts[0], parts[1]);
                            } else { // allow for parameter name only
                                parameters.put(parts[0], "");
                            }
                        }
                    }
                }
            }

            final boolean verbose = parameters.containsKey(VERBOSE);
            
            if (verbose) {
                System.out.println(firstLine); // NOSONAR
                log.info(firstLine);
            }

            // Look for special Response Length header
            String responseStatusValue = getRequestHeaderValue(headerString, "X-ResponseStatus"); //$NON-NLS-1$
            if(responseStatusValue == null) {
                responseStatusValue = "200 OK";
            }
            // Do this before the status check so can override the status, e.g. with a different redirect type
            if (parameters.containsKey(REDIRECT)) {
                responseStatusValue = "302 Temporary Redirect";
            }
            if (parameters.containsKey(STATUS)) {
                responseStatusValue = parameters.get(STATUS);
            }

            log.debug("Write headers");
            out = new BufferedOutputStream(clientSocket.getOutputStream());
            // The headers are written using ISO_8859_1 encoding
            out.write(("HTTP/1.0 "+responseStatusValue).getBytes(ISO_8859_1)); //$NON-NLS-1$
            out.write(CRLF);
            out.write("Content-Type: text/plain".getBytes(ISO_8859_1)); //$NON-NLS-1$
            out.write(CRLF);

            if (parameters.containsKey(REDIRECT)) {
                StringBuilder sb = new StringBuilder();
                sb.append(HTTPConstants.HEADER_LOCATION);
                sb.append(": "); //$NON-NLS-1$
                sb.append(parameters.get(REDIRECT));
                final String redirectLocation = sb.toString();
                if (verbose) {
                    System.out.println(redirectLocation); // NOSONAR
                    log.info(redirectLocation);
                }
                out.write(redirectLocation.getBytes(ISO_8859_1));
                out.write(CRLF);
            }

            // Look for special Header request
            String headersValue = getRequestHeaderValue(headerString, "X-SetHeaders"); //$NON-NLS-1$
            if (headersValue != null) {
                String[] headersToSet = headersValue.split("\\|");
                for (String string : headersToSet) {
                    out.write(string.getBytes(ISO_8859_1));
                    out.write(CRLF);
                }
            }

            // Look for special Response Length header
            String responseLengthValue = getRequestHeaderValue(headerString, "X-ResponseLength"); //$NON-NLS-1$
            int responseLength=-1;
            if(responseLengthValue != null) {
                responseLength = Integer.parseInt(responseLengthValue);
            }

            // Look for special Cookie request
            String cookieHeaderValue = getRequestHeaderValue(headerString, "X-SetCookie"); //$NON-NLS-1$
            if (cookieHeaderValue != null) {
                out.write("Set-Cookie: ".getBytes(ISO_8859_1));
                out.write(cookieHeaderValue.getBytes(ISO_8859_1));
                out.write(CRLF);
            }
            out.write(CRLF);
            out.flush();

            if(responseLength>=0) {
                out.write(baos.toByteArray(), 0, Math.min(baos.toByteArray().length, responseLength));
            } else {
                out.write(baos.toByteArray());
            }
            // Check if we have found a content-length header
            String contentLengthHeaderValue = getRequestHeaderValue(headerString, HTTPConstants.HEADER_CONTENT_LENGTH);
            if(contentLengthHeaderValue != null) {
                contentLength = Integer.parseInt(contentLengthHeaderValue);
            }
            // Look for special Sleep request
            String sleepHeaderValue = getRequestHeaderValue(headerString, "X-Sleep"); //$NON-NLS-1$
            if(sleepHeaderValue != null) {
                TimeUnit.MILLISECONDS.sleep(Integer.parseInt(sleepHeaderValue));
            }
            String transferEncodingHeaderValue = getRequestHeaderValue(headerString, HTTPConstants.TRANSFER_ENCODING);
            if(transferEncodingHeaderValue != null) {
                isChunked = transferEncodingHeaderValue.equalsIgnoreCase("chunked"); //$NON-NLS-1$
                // We only support chunked transfer encoding
                if(!isChunked) {
                    log.error("Transfer-Encoding header set, the value is not supported : {}", transferEncodingHeaderValue);
                }
            }

            // If we know the content length, we can allow the reading of
            // the request to block until more data arrives.
            // If it is chunked transfer, we cannot allow the reading to
            // block, because we do not know when to stop reading, because
            // the chunked transfer is not properly supported yet
            length = 0;
            if(contentLength > 0) {
                // Check how much of the body we have already read as part of reading
                // the headers
                // We subtract two bytes for the crlf divider between header and body
                int totalReadBytes = headerString.length() - positionOfBody - 2;

                // We know when to stop reading, so we can allow the read method to block
                log.debug("Reading, {} < {}", totalReadBytes, contentLength);
                while((totalReadBytes < contentLength) && ((length = in.read(buffer)) != -1)) {
                    log.debug("Read bytes: {}", length);
                    out.write(buffer, 0, length);

                    totalReadBytes += length;
                    log.debug("totalReadBytes: {}", totalReadBytes);
                }
            }
            else if (isChunked) {
                // It is chunked transfer encoding, which we do not really support yet.
                // So we just read without blocking, because we do not know when to
                // stop reading, so we cannot block
                // TODO properly implement support for chunked transfer, i.e. to
                // know when we have read the whole request, and therefore allow
                // the reading to block
                log.debug("Chunked");
                while(in.available() > 0 && ((length = in.read(buffer)) != -1)) {
                    out.write(buffer, 0, length);
                }
            }
            else {
                // The request has no body, or it has a transfer encoding we do not support.
                // In either case, we read any data available
                log.debug("Other");
                while(in.available() > 0 && ((length = in.read(buffer)) != -1)) {
                    log.debug("Read bytes: {}", length);
                    out.write(buffer, 0, length);
                }
            }
            log.debug("Flush");
            out.flush();
        } catch (IOException | InterruptedException e) {
            log.error("", e);
        } finally {
            JOrphanUtils.closeQuietly(out);
            JOrphanUtils.closeQuietly(in);
            JOrphanUtils.closeQuietly(clientSocket);
        }
        log.debug("End of Thread");
    }

    private static String getRequestHeaderValue(String requestHeaders, String headerName) {
        Perl5Matcher localMatcher = JMeterUtils.getMatcher();
        // We use multi-line mask so can prefix the line with ^
        String expression = "^" + headerName + ":\\s+([^\\r\\n]+)"; // $NON-NLS-1$ $NON-NLS-2$
        Pattern pattern = JMeterUtils.getPattern(expression,
                Perl5Compiler.READ_ONLY_MASK
                        | Perl5Compiler.CASE_INSENSITIVE_MASK
                        | Perl5Compiler.MULTILINE_MASK);
        if(localMatcher.contains(requestHeaders, pattern)) {
            // The value is in the first group, group 0 is the whole match
            return localMatcher.getMatch().group(1);
        }
        else {
            return null;
        }
    }

    private static int getPositionOfBody(String stringToCheck) {
        Perl5Matcher localMatcher = JMeterUtils.getMatcher();
        // The headers and body are divided by a blank line (the \r is to allow for the CR before LF)
        String regularExpression = "^\\r$"; // $NON-NLS-1$
        Pattern pattern = JMeterUtils.getPattern(regularExpression,
                Perl5Compiler.READ_ONLY_MASK
                        | Perl5Compiler.CASE_INSENSITIVE_MASK
                        | Perl5Compiler.MULTILINE_MASK);

        PatternMatcherInput input = new PatternMatcherInput(stringToCheck);
        if(localMatcher.contains(input, pattern)) {
            MatchResult match = localMatcher.getMatch();
            return match.beginOffset(0);
        }
        // No divider was found
        return -1;
    }
}
