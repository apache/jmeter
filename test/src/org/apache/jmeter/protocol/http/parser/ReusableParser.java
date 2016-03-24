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

import org.apache.commons.lang3.NotImplementedException;

/**
 * Test class, that implements an dummy {@link LinkExtractorParser} that is
 * reusable
 */
public class ReusableParser implements LinkExtractorParser {

    @Override
    public Iterator<URL> getEmbeddedResourceURLs(String userAgent,
            byte[] responseData, URL baseUrl, String encoding)
            throws LinkExtractorParseException {
        throw new NotImplementedException("Test class");
    }

    @Override
    public boolean isReusable() {
        return true;
    }

}
