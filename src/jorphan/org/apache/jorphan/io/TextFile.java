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

package org.apache.jorphan.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Utility class to handle text files as a single lump of text.
 * <p>
 * Note this is just as memory-inefficient as handling a text file can be. Use
 * with restraint.
 *
 * @version $Revision$
 */
public class TextFile extends File {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * File encoding. null means use the platform's default.
     */
    private String encoding = null;

    /**
     * Create a TextFile object to handle the named file with the given
     * encoding.
     *
     * @param filename
     *            File to be read & written through this object.
     * @param encoding
     *            Encoding to be used when reading & writing this file.
     */
    public TextFile(File filename, String encoding) {
        super(filename.toString());
        setEncoding(encoding);
    }

    /**
     * Create a TextFile object to handle the named file with the platform
     * default encoding.
     *
     * @param filename
     *            File to be read & written through this object.
     */
    public TextFile(File filename) {
        super(filename.toString());
    }

    /**
     * Create a TextFile object to handle the named file with the platform
     * default encoding.
     *
     * @param filename
     *            Name of the file to be read & written through this object.
     */
    public TextFile(String filename) {
        super(filename);
    }

    /**
     * Create a TextFile object to handle the named file with the given
     * encoding.
     *
     * @param filename
     *            Name of the file to be read & written through this object.
     * @param encoding
     *            Encoding to be used when reading & writing this file.
     */
    public TextFile(String filename, String encoding) {
        super(filename);
    }

    /**
     * Create the file with the given string as content -- or replace it's
     * content with the given string if the file already existed.
     *
     * @param body
     *            New content for the file.
     */
    public void setText(String body) {
        Writer writer = null;
        try {
            if (encoding == null) {
                writer = new FileWriter(this);
            } else {
                writer = new OutputStreamWriter(new FileOutputStream(this), encoding);
            }
            writer.write(body);
            writer.flush();
        } catch (IOException ioe) {
            log.error("", ioe);
        } finally {
            JOrphanUtils.closeQuietly(writer);
        }
    }

    /**
     * Read the whole file content and return it as a string.
     *
     * @return the content of the file
     */
    public String getText() {
        String lineEnd = System.getProperty("line.separator"); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        Reader reader = null;
        BufferedReader br = null;
        try {
            if (encoding == null) {
                reader = new FileReader(this);
            } else {
                reader = new InputStreamReader(new FileInputStream(this), encoding);
            }
            br = new BufferedReader(reader);
            String line = "NOTNULL"; //$NON-NLS-1$
            while (line != null) {
                line = br.readLine();
                if (line != null) {
                    sb.append(line + lineEnd);
                }
            }
        } catch (IOException ioe) {
            log.error("", ioe); //$NON-NLS-1$
        } finally {
            JOrphanUtils.closeQuietly(br); // closes reader as well
        }

        return sb.toString();
    }

    /**
     * @return Encoding being used to read & write this file.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param string
     *            Encoding to be used to read & write this file.
     */
    public void setEncoding(String string) {
        encoding = string;
    }
}
