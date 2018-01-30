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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle text files as a single lump of text.
 * <p>
 * Note this is just as memory-inefficient as handling a text file can be. Use
 * with restraint.
 */
public class TextFile extends File {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggerFactory.getLogger(TextFile.class);

    /** File encoding. null means use the platform's default. */
    private String encoding = null;

    /**
     * Create a TextFile object to handle the named file with the given
     * encoding.
     *
     * @param filename File to be read and written through this object.
     * @param encoding Encoding to be used when reading and writing this file.
     */
    public TextFile(File filename, String encoding) {
        super(filename.toString());
        setEncoding(encoding);
    }

    /**
     * Create a TextFile object to handle the named file with the platform
     * default encoding.
     *
     * @param filename File to be read and written through this object.
     */
    public TextFile(File filename) {
        super(filename.toString());
    }

    /**
     * Create a TextFile object to handle the named file with the platform
     * default encoding.
     *
     * @param filename Name of the file to be read and written through this object.
     */
    public TextFile(String filename) {
        super(filename);
    }

    /**
     * Create a TextFile object to handle the named file with the given
     * encoding.
     *
     * @param filename Name of the file to be read and written through this object.
     * @param encoding Encoding to be used when reading and writing this file.
     */
    public TextFile(String filename, String encoding) {
        super(filename);
        setEncoding(encoding);
    }

    /**
     * Create the file with the given string as content -- or replace its
     * content with the given string if the file already existed.
     *
     * @param body New content for the file.
     */
    public void setText(String body) {
        try {
            Files.write(this.toPath(), body.getBytes(getCharset()));
        } catch (IOException ioe) {
            log.error("", ioe);
        }
    }

    /**
     * Read the whole file content and return it as a string.
     *
     * @return the content of the file
     */
    public String getText() {
        try {
            byte[] encoded = Files.readAllBytes(this.toPath());
            return new String(encoded, getCharset());
        } catch (IOException ioe) {
            log.error("Failed to getText", ioe);
            return "";
        }
    }

    private Charset getCharset() {
        return encoding != null
                    ? Charset.forName(encoding)
                    : Charset.defaultCharset();
    }

    /**
     * @return Encoding being used to read and write this file.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param string Encoding to be used to read and write this file.
     */
    public void setEncoding(String string) {
        encoding = string;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((encoding == null) ? 0 : encoding.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TextFile)) {
            return false;
        }
        TextFile other = (TextFile) obj;
        if (encoding == null) {
            return other.encoding == null;
        }
        return encoding.equals(other.encoding);
    }
}
