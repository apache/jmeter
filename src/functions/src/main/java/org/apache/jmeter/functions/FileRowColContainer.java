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

package org.apache.jmeter.functions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File data container for CSV (and similar delimited) files Data is accessible
 * via row and column number
 *
 */
public class FileRowColContainer {

    private static final Logger log = LoggerFactory.getLogger(FileRowColContainer.class);

    private final List<List<String>> fileData; // Lines in the file, split into columns

    private final String fileName; // name of the file

    public static final String DELIMITER
        = JMeterUtils.getPropDefault("csvread.delimiter",  // $NON-NLS-1$
                ","); // $NON-NLS-1$

    /** Keeping track of which row is next to be read. */
    private int nextRow;

    /** Delimiter for this file */
    private final String delimiter;

    public FileRowColContainer(String file, String delim) throws IOException, FileNotFoundException {
        log.debug("FRCC({},{})", file, delim);
        fileName = file;
        delimiter = delim;
        nextRow = 0;
        fileData = new ArrayList<>();
        load();
    }

    public FileRowColContainer(String file) throws IOException, FileNotFoundException {
        log.debug("FRCC({})[{}]", file, DELIMITER);
        fileName = file;
        delimiter = DELIMITER;
        nextRow = 0;
        fileData = new ArrayList<>();
        load();
    }

    private void load() throws IOException, FileNotFoundException {
        try (BufferedReader myBread = 
                Files.newBufferedReader(FileServer.getFileServer().getResolvedFile(fileName).toPath(), 
                        Charset.defaultCharset())) {
            String line = myBread.readLine();
            /*
             * N.B. Stop reading the file if we get a blank line: This allows
             * for trailing comments in the file
             */
            while (line != null && line.length() > 0) {
                fileData.add(splitLine(line, delimiter));
                line = myBread.readLine();
            }
        } catch (IOException e) {
            fileData.clear();
            log.warn(e.toString());
            throw e;
        }
    }

    /**
     * Get the string for the column from the current row
     *
     * @param row
     *            row number (from 0)
     * @param col
     *            column number (from 0)
     * @return the string (empty if out of bounds)
     * @throws IndexOutOfBoundsException
     *             if the column number is out of bounds
     */
    public String getColumn(int row, int col) throws IndexOutOfBoundsException {
        String colData;
        colData = fileData.get(row).get(col);
        log.debug("{}({},{}):{}", fileName, row, col, colData);
        return colData;
    }

    /**
     * Returns the next row to the caller, and updates it, allowing for wrap
     * round
     *
     * @return the first free (unread) row
     *
     */
    public int nextRow() {
        int row = nextRow;
        nextRow++;
        if (nextRow >= fileData.size()) {// 0-based
            nextRow = 0;
        }
        log.debug("Row: {}", row);
        return row;
    }

    /**
     * Splits the line according to the specified delimiter
     *
     * @return a List of Strings containing one element for each value in
     *         the line
     */
    private static List<String> splitLine(String theLine, String delim) {
        List<String> result = new ArrayList<>();
        StringTokenizer tokener = new StringTokenizer(theLine, delim, true);
        /*
         * the beginning of the line is a "delimiter" so that ,a,b,c returns ""
         * "a" "b" "c"
         */
        boolean lastWasDelim = true;
        while (tokener.hasMoreTokens()) {
            String token = tokener.nextToken();
            if (token.equals(delim)) {
                if (lastWasDelim) {
                    // two delimiters in a row; add an empty String
                    result.add("");
                }
                lastWasDelim = true;
            } else {
                lastWasDelim = false;
                result.add(token);
            }
        }
        if (lastWasDelim) // Catch the trailing delimiter
        {
            result.add(""); // $NON-NLS-1$
        }
        return result;
    }

    /**
     * @return the file name for this class
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return Returns the delimiter.
     */
    final String getDelimiter() {
        return delimiter;
    }

    // Added to support external testing
    public int getSize(){
        return fileData.size();
    }
}
