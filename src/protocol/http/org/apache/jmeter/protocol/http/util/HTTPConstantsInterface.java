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

package org.apache.jmeter.protocol.http.util;


/**
 * Constants used in HTTP, mainly header names.
 */

public interface HTTPConstantsInterface { // CHECKSTYLE IGNORE InterfaceIsType
    String SC_MOVED_PERMANENTLY = "301";
    String SC_MOVED_TEMPORARILY = "302";
    String SC_SEE_OTHER = "303";
    String SC_TEMPORARY_REDIRECT = "307";
            
    int DEFAULT_HTTPS_PORT = 443;
    String DEFAULT_HTTPS_PORT_STRING = "443"; // $NON-NLS-1$
    int    DEFAULT_HTTP_PORT = 80;
    String DEFAULT_HTTP_PORT_STRING = "80"; // $NON-NLS-1$
    String PROTOCOL_HTTP = "http"; // $NON-NLS-1$
    String PROTOCOL_HTTPS = "https"; // $NON-NLS-1$
    String HEAD = "HEAD"; // $NON-NLS-1$
    String POST = "POST"; // $NON-NLS-1$
    String PUT = "PUT"; // $NON-NLS-1$
    String GET = "GET"; // $NON-NLS-1$
    String OPTIONS = "OPTIONS"; // $NON-NLS-1$
    String TRACE = "TRACE"; // $NON-NLS-1$
    String DELETE = "DELETE"; // $NON-NLS-1$
    String PATCH = "PATCH"; // $NON-NLS-1$
    String PROPFIND = "PROPFIND"; // $NON-NLS-1$
    String PROPPATCH = "PROPPATCH"; // $NON-NLS-1$
    String MKCOL = "MKCOL"; // $NON-NLS-1$
    String COPY = "COPY"; // $NON-NLS-1$
    String MOVE = "MOVE"; // $NON-NLS-1$
    String LOCK = "LOCK"; // $NON-NLS-1$
    String UNLOCK = "UNLOCK"; // $NON-NLS-1$
    String CONNECT = "CONNECT"; // $NON-NLS-1$
    String REPORT = "REPORT"; // $NON-NLS-1$
    String MKCALENDAR = "MKCALENDAR"; // $NON-NLS-1$
    String SEARCH = "SEARCH"; // $NON-NLS-1$
    String HEADER_AUTHORIZATION = "Authorization"; // $NON-NLS-1$
    String HEADER_COOKIE = "Cookie"; // $NON-NLS-1$
    String HEADER_CONNECTION = "Connection"; // $NON-NLS-1$
    String CONNECTION_CLOSE = "close"; // $NON-NLS-1$
    String KEEP_ALIVE = "keep-alive"; // $NON-NLS-1$
    // e.g. "Transfer-Encoding: chunked", which is processed automatically by the underlying protocol
    String TRANSFER_ENCODING = "transfer-encoding"; // $NON-NLS-1$
    String HEADER_CONTENT_ENCODING = "content-encoding"; // $NON-NLS-1$
    String HTTP_1_1 = "HTTP/1.1"; // $NON-NLS-1$
    String HEADER_SET_COOKIE = "set-cookie"; // $NON-NLS-1$
    // Brotli compression not supported yet by HC4 4.5.2 , but to be added
    String ENCODING_BROTLI = "br"; // $NON-NLS-1$
    String ENCODING_DEFLATE = "deflate"; // $NON-NLS-1$
    String ENCODING_GZIP = "gzip"; // $NON-NLS-1$

    String HEADER_CONTENT_DISPOSITION = "Content-Disposition"; // $NON-NLS-1$
    String HEADER_CONTENT_TYPE = "Content-Type"; // $NON-NLS-1$
    String HEADER_CONTENT_LENGTH = "Content-Length"; // $NON-NLS-1$
    String HEADER_HOST = "Host"; // $NON-NLS-1$
    String HEADER_LOCAL_ADDRESS = "X-LocalAddress"; // $NON-NLS-1$ pseudo-header for reporting Local Address
    String HEADER_LOCATION = "Location"; // $NON-NLS-1$
    String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded"; // $NON-NLS-1$
    String MULTIPART_FORM_DATA = "multipart/form-data"; // $NON-NLS-1$
    // For handling caching
    String IF_NONE_MATCH = "If-None-Match"; // $NON-NLS-1$
    String IF_MODIFIED_SINCE = "If-Modified-Since"; // $NON-NLS-1$
    String ETAG = "Etag"; // $NON-NLS-1$
    String LAST_MODIFIED = "Last-Modified"; // $NON-NLS-1$
    String EXPIRES = "Expires"; // $NON-NLS-1$
    String CACHE_CONTROL = "Cache-Control";  //e.g. public, max-age=259200
    String DATE = "Date";  //e.g. Date Header of response
    String VARY = "Vary"; // $NON-NLS-1$

}
