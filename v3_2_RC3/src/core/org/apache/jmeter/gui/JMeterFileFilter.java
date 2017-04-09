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

package org.apache.jmeter.gui;

import java.io.File;
import java.util.Arrays;

/**
 * A file filter which allows files to be filtered based on a list of allowed
 * extensions.
 *
 * Optionally returns directories.
 *
 */
public class JMeterFileFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {
    /** The list of extensions allowed by this filter. */
    private final String[] exts;

    private final boolean allowDirs; // Should we allow directories?

    /**
     * Create a new JMeter file filter which allows the specified extensions. If
     * the array of extensions contains no elements, any file will be allowed.
     *
     * This constructor will also return all directories
     *
     * @param extensions
     *            non-null array of allowed file extensions
     */
    public JMeterFileFilter(String[] extensions) {
        this(extensions,true);
    }

    /**
     * Create a new JMeter file filter which allows the specified extensions. If
     * the array of extensions contains no elements, any file will be allowed.
     *
     * @param extensions non-null array of allowed file extensions
     * @param allow should directories be returned ?
     */
    public JMeterFileFilter(String[] extensions, boolean allow) {
        exts = extensions;
        allowDirs = allow;
    }

    /**
     * Determine if the specified file is allowed by this filter. The file will
     * be allowed if it is a directory, or if the end of the filename matches
     * one of the extensions allowed by this filter. The filename is converted
     * to lower-case before making the comparison.
     *
     * @param f
     *            the File being tested
     *
     * @return true if the file should be allowed, false otherwise
     */
    @Override
    public boolean accept(File f) {
        return (allowDirs && f.isDirectory()) || accept(f.getName().toLowerCase());
        // TODO - why lower case? OK to use the default Locale?
    }

    /**
     * Determine if the specified filename is allowed by this filter. The file
     * will be allowed if the end of the filename matches one of the extensions
     * allowed by this filter. The comparison is case-sensitive. If no
     * extensions were provided for this filter, the file will always be
     * allowed.
     *
     * @param filename
     *            the filename to test
     * @return true if the file should be allowed, false otherwise
     */
    public boolean accept(String filename) {
        if (exts.length == 0) {
            return true;
        }

        for (String ext : exts) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get a description for this filter.
     *
     * @return a description for this filter
     */
    @Override
    public String getDescription() {
        return "JMeter " + Arrays.asList(exts).toString();
    }
}
