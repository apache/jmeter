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
package org.apache.jmeter.report.core;

import java.io.File;
import java.net.URI;

/**
 * Represents a CSV file<br>
 * <p>
 * Basically holds the CSV separator
 * </p>
 * 
 * @since 3.0
 */
public class CsvFile extends File {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4721600093557427167L;
    private char separator;

    public CsvFile(File parent, String child, char separator) {
        super(parent, child);
        this.separator = separator;
    }

    public CsvFile(String parent, String child, char separator) {
        super(parent, child);
        this.separator = separator;
    }

    public CsvFile(String pathname, char separator) {
        super(pathname);
        this.separator = separator;
    }

    public CsvFile(URI uri, char separator) {
        super(uri);
        this.separator = separator;
    }

    /**
     * DO NOT USE - UNIT TEST ONLY
     * @deprecated UNIT TEST ONLY
     */
    @Deprecated // only for use by unit tests
    public CsvFile() {
        super("");
        this.separator = 0;
    }

    public char getSeparator() {
        return separator;
    }

}
