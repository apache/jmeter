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

/**
 * Description:<br>
 * <br>
 * LogParser is the base interface for classes implementing concrete parse
 * logic. For an example of how to use the interface, look at the Tomcat access
 * log parser.
 * <p>
 * The original log parser was written in 2 hours to parse access logs. Since
 * then, the design and implementation has been rewritten from scratch several
 * times to make it more generic and extensible. The first version was hard
 * coded and written over the weekend.
 * </p>
 *
 */

public interface LogParser {

    /**
     * close the any streams or readers.
     */
    void close();

    /**
     * the method will parse the given number of lines. Pass "-1" to parse the
     * entire file. If the end of the file is reached without parsing a line, a
     * 0 is returned. If the method is subsequently called again, it will
     * restart parsing at the beginning.
     *
     * @param count max lines to parse, or <code>-1</code> for the entire file
     * @param el {@link TestElement} to read lines into
     * @return number of lines parsed
     */
    int parseAndConfigure(int count, TestElement el);

    /**
     * We allow for filters, so that users can simply point to an Access log
     * without having to clean it up. This makes it significantly easier and
     * reduces the amount of work. Plus I'm lazy, so going through a log file to
     * clean it up is a bit tedious. One example of this is using the filter to
     * exclude any log entry that has a 505 response code.
     *
     * @param filter {@link Filter} to use
     */
    void setFilter(Filter filter);

    /**
     * The method is provided to make it easy to dynamically create new classes
     * using Class.newInstance(). Then the access log file is set using this
     * method.
     *
     * @param source name of the access log file
     */
    void setSourceFile(String source);
}
