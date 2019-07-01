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

package org.apache.jmeter.protocol.http.parser;

import java.net.URL;
import java.util.Iterator;

/**
 * Interface specifying contract of content parser that aims to extract links
 * @since 3.0
 */
public interface LinkExtractorParser {

    /**
     * Get the URLs for all the resources that a browser would automatically
     * download following the download of the content, that is: images,
     * stylesheets, javascript files, applets, etc...
     * <p>
     * URLs should not appear twice in the returned iterator.
     * <p>
     * Malformed URLs can be reported to the caller by having the Iterator
     * return the corresponding RL String. Overall problems parsing the html
     * should be reported by throwing an HTMLParseException.
     * @param userAgent
     *            User Agent
     * @param responseData Response data
     * @param baseUrl
     *            Base URL from which the HTML code was obtained
     * @param encoding Charset
     * @return an Iterator for the resource URLs
     * @throws LinkExtractorParseException when extracting the links fails
     */
    Iterator<URL> getEmbeddedResourceURLs(
            String userAgent, byte[] responseData, URL baseUrl, String encoding)
                    throws LinkExtractorParseException;

    boolean isReusable();
}
