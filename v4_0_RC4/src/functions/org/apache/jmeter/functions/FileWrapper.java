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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps the FileRowColContainer for use across multiple threads.
 *
 * It does this by maintaining a list of open files, keyed by file name (or
 * alias, if used). A list of open files is also maintained for each thread,
 * together with the current line number.
 *
 */
public final class FileWrapper {

    private static final Logger log = LoggerFactory.getLogger(FileWrapper.class);

    private static final int NO_LINE = -1;

    private static volatile String defaultFile = ""; // for omitted file names //$NON-NLS-1$

    /*
     * This Map serves two purposes:
     * - maps file names to  containers
     * - ensures only one container per file across all threads
     */
    private static final Map<String, FileRowColContainer> fileContainers = new HashMap<>();

    /* The cache of file packs - used to improve thread access */
    private static final ThreadLocal<Map<String, FileWrapper>> filePacks = 
        new ThreadLocal<Map<String, FileWrapper>>() {
        @Override
        protected Map<String, FileWrapper> initialValue() {
            return new HashMap<>();
        }
    };

    private final FileRowColContainer container;

    private int currentRow;

    /*
     * Only needed locally
     */
    private FileWrapper(FileRowColContainer fdc) {
        super();
        container = fdc;
        currentRow = -1;
    }

    private static String checkDefault(String file) {
        if (file.length() == 0) {
            if (fileContainers.size() == 1 && defaultFile.length() > 0) {
                log.warn("Using default: " + defaultFile);
                file = defaultFile;
            } else {
                log.error("Cannot determine default file name");
            }
        }
        return file;
    }

    /*
     * called by CSVRead(file,alias)
     */
    public static synchronized void open(String file, String alias) {
        log.info("Opening " + file + " as " + alias);
        file = checkDefault(file);
        if (alias.length() == 0) {
            log.error("Alias cannot be empty");
            return;
        }
        Map<String, FileWrapper> m = filePacks.get();
        if (m.get(alias) == null) {
            FileRowColContainer frcc;
            try {
                frcc = getFile(file, alias);
                log.info("Stored " + file + " as " + alias);
                m.put(alias, new FileWrapper(frcc));
            } catch (IOException e) {
                // Already logged
            }
        }
    }

    private static FileRowColContainer getFile(String file, String alias) throws FileNotFoundException, IOException {
        FileRowColContainer frcc;
        if ((frcc = fileContainers.get(alias)) == null) {
            frcc = new FileRowColContainer(file);
            fileContainers.put(alias, frcc);
            log.info("Saved " + file + " as " + alias + " delimiter=<" + frcc.getDelimiter() + ">");
            if (defaultFile.length() == 0) {
                defaultFile = file;// Save in case needed later
            }
        }
        return frcc;
    }

    /*
     * Called by CSVRead(x,next) - sets the row to nil so the next row will be
     * picked up the next time round
     *
     */
    public static void endRow(String file) {
        file = checkDefault(file);
        Map<String, FileWrapper> my = filePacks.get();
        FileWrapper fw = my.get(file);
        if (fw == null) {
            log.warn("endRow(): no entry for " + file);
        } else {
            fw.endRow();
        }
    }

    private void endRow() {
        if (currentRow == NO_LINE) {
            log.warn("endRow() called twice in succession");
        }
        currentRow = NO_LINE;
    }

    public static String getColumn(String file, int col) {
        Map<String, FileWrapper> my = filePacks.get();
        FileWrapper fw = my.get(file);
        if (fw == null) // First call
        {
            if (file.startsWith("*")) { //$NON-NLS-1$
                log.warn("Cannot perform initial open using alias " + file);
            } else {
                file = checkDefault(file);
                log.info("Attaching " + file);
                open(file, file);
                fw = my.get(file);
            }
            // TODO improve the error handling
            if (fw == null) {
                return "";  //$NON-NLS-1$
            }
        }
        return fw.getColumn(col);
    }

    private String getColumn(int col) {
        if (currentRow == NO_LINE) {
            currentRow = container.nextRow();

        }
        return container.getColumn(currentRow, col);
    }

    /**
     * Gets the current row number (mainly for error reporting)
     *
     * @param file
     *            name of the file for which the row number is asked
     * @return the current row number for this thread, or <code>-1</code> if
     *         <code>file</code> was not opened yet
     */
    public static int getCurrentRow(String file) {

        Map<String, FileWrapper> my = filePacks.get();
        FileWrapper fw = my.get(file);
        if (fw == null) // Not yet open
        {
            return -1;
        }
        return fw.currentRow;
    }

    /**
     *
     */
    public static void clearAll() {
        log.debug("clearAll()");
        Map<String, FileWrapper> my = filePacks.get();
        for (Iterator<Map.Entry<String, FileWrapper>>  i = my.entrySet().iterator(); i.hasNext();) {
            Map.Entry<String, FileWrapper> fw = i.next();
            log.info("Removing " + fw.toString());
            i.remove();
        }
        fileContainers.clear();
        defaultFile = ""; //$NON-NLS-1$
    }
}
