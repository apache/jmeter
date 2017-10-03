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

package org.apache.jmeter.protocol.http.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.Replaceable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * This class provides an interface to headers file to pass HTTP headers along
 * with a request.
 *
 */
public class HeaderManager extends ConfigTestElement implements Serializable, Replaceable {

    private static final long serialVersionUID = 240L;

    public static final String HEADERS = "HeaderManager.headers";// $NON-NLS-1$

    private static final String[] COLUMN_RESOURCE_NAMES = {
          "name",             // $NON-NLS-1$
          "value"             // $NON-NLS-1$
        };

    private static final int COLUMN_COUNT = COLUMN_RESOURCE_NAMES.length;

    public HeaderManager() {
        setProperty(new CollectionProperty(HEADERS, new ArrayList<>()));
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(HEADERS, new ArrayList<>()));
    }

    /**
     * Get the collection of JMeterProperty entries representing the headers.
     *
     * @return the header collection property
     */
    public CollectionProperty getHeaders() {
        return (CollectionProperty) getProperty(HEADERS);
    }

    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    public String getColumnName(int column) {
        return COLUMN_RESOURCE_NAMES[column];
    }

    public Class<? extends String> getColumnClass(int column) {
        return COLUMN_RESOURCE_NAMES[column].getClass();
    }

    public Header getHeader(int row) {
        return (Header) getHeaders().get(row).getObjectValue();
    }

    /**
     * Save the header data to a file.
     *
     * @param headFile
     *            name of the file to store headers into. If name is relative
     *            the system property <code>user.dir</code> will be prepended
     * @throws IOException
     *             if writing the headers fails
     */
    public void save(String headFile) throws IOException {
        File file = new File(headFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir")// $NON-NLS-1$
                    + File.separator + headFile);
        }
        try ( FileWriter fw = new FileWriter(file);
                PrintWriter writer = new PrintWriter(fw);) { // TODO Charset ? 
            writer.println("# JMeter generated Header file");// $NON-NLS-1$
            final CollectionProperty hdrs = getHeaders();
            for (int i = 0; i < hdrs.size(); i++) {
                final JMeterProperty hdr = hdrs.get(i);
                Header head = (Header) hdr.getObjectValue();
                writer.println(head.toString());
            }
        }
    }

    /**
     * Add header data from a file.
     *
     * @param headerFile
     *            name of the file to read headers from. If name is relative the
     *            system property <code>user.dir</code> will be prepended
     * @throws IOException
     *             if reading headers fails
     */
    public void addFile(String headerFile) throws IOException {
        File file = new File(headerFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir")// $NON-NLS-1$
                    + File.separator + headerFile);
        }
        if (!file.canRead()) {
            throw new IOException("The file you specified cannot be read.");
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), Charset.defaultCharset())) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    if (line.startsWith("#") || JOrphanUtils.isBlank(line)) {// $NON-NLS-1$
                        continue;
                    }
                    String[] st = JOrphanUtils.split(line, "\t", " ");// $NON-NLS-1$ $NON-NLS-2$
                    int name = 0;
                    int value = 1;
                    Header header = new Header(st[name], st[value]);
                    getHeaders().addItem(header);
                } catch (Exception e) {
                    throw new IOException("Error parsing header line\n\t'" + line + "'\n\t" + e);
                }
            }
        }
    }

    /**
     * Add a header.
     *
     * @param h {@link Header} to add
     */
    public void add(Header h) {
        getHeaders().addItem(h);
    }

    /**
     * Add an empty header.
     */
    public void add() {
        getHeaders().addItem(new Header());
    }

    /**
     * Remove a header.
     *
     * @param index index from the header to remove
     */
    public void remove(int index) {
        getHeaders().remove(index);
    }

    /**
     * Return the number of headers.
     *
     * @return number of headers
     */
    public int size() {
        return getHeaders().size();
    }

    /**
     * Return the header at index i.
     *
     * @param i
     *            index of the header to get
     * @return {@link Header} at index <code>i</code>
     */
    public Header get(int i) {
        return (Header) getHeaders().get(i).getObjectValue();
    }

    /**
     * Remove from Headers the header named name
     * @param name header name
     */
    public void removeHeaderNamed(String name) {
        List<Integer> removeIndices = new ArrayList<>();
        for (int i = getHeaders().size() - 1; i >= 0; i--) {
            Header header = (Header) getHeaders().get(i).getObjectValue();
            if (header == null) {
                continue;
            }
            if (header.getName().equalsIgnoreCase(name)) {
                removeIndices.add(Integer.valueOf(i));
            }
        }
        for (Integer indice : removeIndices) {
            getHeaders().remove(indice.intValue());
        }
    }

    /**
     * Merge the attributes with a another HeaderManager's attributes.
     * 
     * @param element
     *            The object to be merged with
     * @param preferLocalValues Not used
     * @return merged HeaderManager
     * @throws IllegalArgumentException
     *             if <code>element</code> is not an instance of
     *             {@link HeaderManager}
     *             
     * @deprecated since 3.2, use {@link HeaderManager#merge(TestElement)} as this method will be removed in a future version
     */
    @Deprecated
    public HeaderManager merge(TestElement element, boolean preferLocalValues) {
        return merge(element);
    }
        
    /**
     * Merge the attributes with a another HeaderManager's attributes.
     * 
     * @param element
     *            The object to be merged with
     * @return merged HeaderManager
     * @throws IllegalArgumentException
     *             if <code>element</code> is not an instance of
     *             {@link HeaderManager}
     */
    public HeaderManager merge(TestElement element) {
        if (!(element instanceof HeaderManager)) {
            throw new IllegalArgumentException("Cannot merge type:" + this.getClass().getName() + " with type:" + element.getClass().getName());
        }

        // start off with a merged object as a copy of the local object
        HeaderManager merged = (HeaderManager)this.clone();

        HeaderManager other = (HeaderManager)element;
        // iterate thru each of the other headers
        for (int i = 0; i < other.getHeaders().size(); i++) {
            Header otherHeader = other.get(i);
            boolean found = false;
            // find the same property in the local headers
            for (int j = 0; j < merged.getHeaders().size(); j++) {
                Header mergedHeader = merged.get(j);
                if (mergedHeader.getName().equalsIgnoreCase(otherHeader.getName())) {
                    // we have a match
                    found = true;
                    // break out of the inner loop
                    break;
                }
            }
            if (!found) {
                // the other object has a new value to be added to the merged
                merged.add(otherHeader);
            }
        }

        // finally, merge the names so it's clear they've been merged
        merged.setName(merged.getName() + ":" + other.getName());

        return merged;
    }

    @Override
    public int replace(String regex, String replaceBy, boolean caseSensitive) throws Exception {
        final CollectionProperty hdrs = getHeaders();
        int totalReplaced = 0;
        for (int i = 0; i < hdrs.size(); i++) {
            final JMeterProperty hdr = hdrs.get(i);
            Header head = (Header) hdr.getObjectValue();
            String value = head.getValue();
            if(!StringUtils.isEmpty(value)) {
                Object[] result = JOrphanUtils.replaceAllWithRegex(value, regex, replaceBy, caseSensitive);
                // check if there is anything to replace
                int nbReplaced = ((Integer)result[1]).intValue();
                if (nbReplaced>0) {
                    String replacedText = (String) result[0];
                    head.setValue(replacedText);
                    totalReplaced += nbReplaced;
                }
            }
        }
        return totalReplaced;
    }
}
