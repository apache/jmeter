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

import java.io.IOException;

import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestCloneable;
import org.apache.jmeter.testelement.TestElement;

public class SharedTCLogParser extends TCLogParser implements TestCloneable {

    public SharedTCLogParser() {
        super();
    }

    public SharedTCLogParser(String source) {
        super(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        SharedTCLogParser parser = new SharedTCLogParser();
        parser.FILENAME = FILENAME;
        parser.FILTER = FILTER;
        return parser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int parse(TestElement el, int parseCount) {
        FileServer fileServer = FileServer.getFileServer();
        fileServer.reserveFile(FILENAME);
        try {
            return parse(fileServer, el, parseCount);
        } catch (Exception exception) {
            log.error("Problem creating samples", exception);
        }
        return -1;// indicate that an error occurred
    }

    /**
     * The method is responsible for reading each line, and breaking out of the
     * while loop if a set number of lines is given.
     *
     * @param breader
     *            reader to read lines from
     * @param el
     *            {@link TestElement} in which to add the parsed lines
     * @param parseCount
     *            max number of lines to parse
     * @return number of read lines
     */
    protected int parse(FileServer breader, TestElement el, int parseCount) {
        int actualCount = 0;
        String line = null;
        try {
            // read one line at a time using
            // BufferedReader
            line = breader.readLine(FILENAME);
            while (line != null) {
                if (line.length() > 0) {
                    actualCount += this.parseLine(line, el);
                }
                // we check the count to see if we have exceeded
                // the number of lines to parse. There's no way
                // to know where to stop in the file. Therefore
                // we use break to escape the while loop when
                // we've reached the count.
                if (parseCount != -1 && actualCount >= parseCount) {
                    break;
                }
                line = breader.readLine(FILENAME);
            }
            if (line == null) {
                breader.closeFile(FILENAME);
            }
        } catch (IOException ioe) {
            log.error("Error reading log file", ioe);
        }
        return actualCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            FileServer.getFileServer().closeFile(FILENAME);
        } catch (IOException e) {
            // do nothing
        }
    }

}
