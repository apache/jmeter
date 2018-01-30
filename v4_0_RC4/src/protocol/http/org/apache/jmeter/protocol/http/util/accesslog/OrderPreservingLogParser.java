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

package org.apache.jmeter.protocol.http.util.accesslog;

import org.apache.jmeter.testelement.TestElement;

public class OrderPreservingLogParser extends SharedTCLogParser {

    public OrderPreservingLogParser() {
        super();
    }

    public OrderPreservingLogParser(String source) {
        super(source);
    }

    /**
     * parse a set number of lines from the access log. Keep in mind the number
     * of lines parsed will depend the filter and number of lines in the log.
     * The method returns the actual lines parsed.
     * 
     * @param count number of max lines to read
     * @return lines parsed
     */
    @Override
    public synchronized int parseAndConfigure(int count, TestElement el) {
        return this.parse(el, count);
    }

}
