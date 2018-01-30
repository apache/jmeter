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

package org.apache.jmeter.protocol.http.proxy;

/**
 * Utility class to generate HTTP responses of various types.
 *
 */
public final class HttpReplyHdr {
    /** String representing a carriage-return/line-feed pair. */
    private static final String CR = "\r\n";

    /** A HTTP protocol version string. */
    private static final String HTTP_PROTOCOL = "HTTP/1.0";

    /** The HTTP server name. */
    private static final String HTTP_SERVER = "Java Proxy Server";

    /**
     * Don't allow instantiation of this utility class.
     */
    private HttpReplyHdr() {
    }

    /**
     * Forms a http ok reply header
     *
     * @param contentType
     *            the mime-type of the content
     * @param contentLength
     *            the length of the content
     * @return a string with the header in it
     */
    public static String formOk(String contentType, long contentLength) {
        StringBuilder out = new StringBuilder();

        out.append(HTTP_PROTOCOL).append(" 200 Ok").append(CR);
        out.append("Server: ").append(HTTP_SERVER).append(CR);
        out.append("MIME-version: 1.0").append(CR);

        if (0 < contentType.length()) {
            out.append("Content-Type: ").append(contentType).append(CR);
        } else {
            out.append("Content-Type: text/html").append(CR);
        }

        if (0 != contentLength) {
            out.append("Content-Length: ").append(contentLength).append(CR);
        }

        out.append(CR);

        return out.toString();
    }

    /**
     * private! builds an http document describing a headers reason.
     *
     * @param error
     *            Error name.
     * @param description
     *            Errors description.
     * @return A string with the HTML description body
     */
    private static String formErrorBody(String error, String description) {
        StringBuilder out = new StringBuilder();
        // Generate Error Body
        out.append("<HTML><HEAD><TITLE>");
        out.append(error);
        out.append("</TITLE></HEAD>");
        out.append("<BODY><H2>").append(error).append("</H2>\n");
        out.append("</P></H3>");
        out.append(description);
        out.append("</BODY></HTML>");
        return out.toString();
    }

    /**
     * builds an http document describing an error.
     *
     * @param error
     *            Error name.
     * @param description
     *            Errors description.
     * @return A string with the HTML description body
     */
    private static String formError(String error, String description) {
        /*
         * A HTTP RESPONSE HEADER LOOKS ALOT LIKE:
         *
         * HTTP/1.0 200 OK Date: Wednesday, 02-Feb-94 23:04:12 GMT Server:
         * NCSA/1.1 MIME-version: 1.0 Last-modified: Monday, 15-Nov-93 23:33:16
         * GMT Content-Type: text/html Content-Length: 2345 \r\n
         */

        String body = formErrorBody(error, description);
        StringBuilder header = new StringBuilder();

        header.append(HTTP_PROTOCOL).append(" ").append(error).append(CR);
        header.append("Server: ").append(HTTP_SERVER).append(CR);
        header.append("MIME-version: 1.0").append(CR);
        header.append("Content-Type: text/html").append(CR);

        header.append("Content-Length: ").append(body.length()).append(CR);

        header.append(CR);
        header.append(body);

        return header.toString();
    }

    /**
     * Indicates a new file was created.
     *
     * @return The header in a string;
     */
    public static String formCreated() {
        return formError("201 Created", "Object was created");
    }

    /**
     * Indicates the document was accepted.
     *
     * @return The header in a string;
     */
    public static String formAccepted() {
        return formError("202 Accepted", "Object checked in");
    }

    /**
     * Indicates only a partial response was sent.
     *
     * @return The header in a string;
     */
    public static String formPartial() {
        return formError("203 Partial", "Only partial document available");
    }

    /**
     * Indicates a requested URL has moved to a new address or name.
     *
     * @return The header in a string;
     */
    public static String formMoved() {
        // 300 codes tell client to do actions
        return formError("301 Moved", "File has moved");
    }

    /**
     * Never seen this used.
     *
     * @return The header in a string;
     */
    public static String formFound() {
        return formError("302 Found", "Object was found");
    }

    /**
     * The requested method is not implemented by the server.
     *
     * @return The header in a string;
     */
    public static String formMethod() {
        return formError("303 Method unseported", "Method unseported");
    }

    /**
     * Indicates remote copy of the requested object is current.
     *
     * @return The header in a string;
     */
    public static String formNotModified() {
        return formError("304 Not modified", "Use local copy");
    }

    /**
     * Client not authorized for the request.
     *
     * @return The header in a string;
     */
    public static String formUnauthorized() {
        return formError("401 Unathorized", "Unathorized use of this service");
    }

    /**
     * Payment is required for service.
     *
     * @return The header in a string;
     */
    public static String formPaymentNeeded() {
        return formError("402 Payment required", "Payment is required");
    }

    /**
     * Client if forbidden to get the request service.
     *
     * @return The header in a string;
     */
    public static String formForbidden() {
        return formError("403 Forbidden", "You need permission for this service");
    }

    /**
     * The requested object was not found.
     *
     * @return The header in a string;
     */
    public static String formNotFound() {
        return formError("404 Not_found", "Requested object was not found");
    }

    /**
     * The server had a problem and could not fulfill the request.
     *
     * @return The header in a string;
     */
    public static String formInternalError() {
        return formError("500 Internal server error", "Server broke");
    }

    /**
     * Server does not do the requested feature.
     *
     * @return The header in a string;
     */
    public static String formNotImplemented() {
        return formError("501 Method not implemented", "Service not implemented");
    }

    /**
     * Server does not do the requested feature.
     *
     * @param reason detailed information for causing the failure
     * @return The header in a string;
     */
    public static String formNotImplemented(String reason) {
        return formError("501 Method not implemented", "Service not implemented. " + reason);
    }

    /**
     * Server is overloaded, client should try again latter.
     *
     * @return The header in a string;
     */
    public static String formOverloaded() {
        return formError("502 Server overloaded", "Try again latter");
    }

    /**
     * Indicates the request took to long.
     *
     * @return The header in a string;
     */
    public static String formTimeout() {
        return formError("503 Gateway timeout", "The connection timed out");
    }

    /**
     * Indicates the client's proxies could not locate a server.
     *
     * @return The header in a string;
     */
    public static String formServerNotFound() {
        return formError("503 Gateway timeout", "The requested server was not found");
    }

    /**
     * Indicates the client is not allowed to access the object.
     *
     * @return The header in a string;
     */
    public static String formNotAllowed() {
        return formError("403 Access Denied", "Access is not allowed");
    }
}
