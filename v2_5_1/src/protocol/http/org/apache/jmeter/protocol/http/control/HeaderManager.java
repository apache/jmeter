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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * This class provides an interface to headers file to pass HTTP headers along
 * with a request.
 *
 * @version $Revision$
 */
public class HeaderManager extends ConfigTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    public static final String HEADERS = "HeaderManager.headers";// $NON-NLS-1$

    private final static String[] COLUMN_RESOURCE_NAMES = {
          "name",             // $NON-NLS-1$
          "value"             // $NON-NLS-1$
        };

    private final static int COLUMN_COUNT = COLUMN_RESOURCE_NAMES.length;


    /**
     * Apache SOAP driver does not provide an easy way to get and set the cookie
     * or HTTP header. Therefore it is necessary to store the SOAPHTTPConnection
     * object and reuse it.
     */
    private Object SOAPHeader = null;

    public HeaderManager() {
        setProperty(new CollectionProperty(HEADERS, new ArrayList<Object>()));
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(HEADERS, new ArrayList<Object>()));
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
     */
    public void save(String headFile) throws IOException {
        File file = new File(headFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir")// $NON-NLS-1$
                    + File.separator + headFile);
        }
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        writer.println("# JMeter generated Header file");// $NON-NLS-1$
        final CollectionProperty hdrs = getHeaders();
        for (int i = 0; i < hdrs.size(); i++) {
            final JMeterProperty hdr = hdrs.get(i);
            Header head = (Header) hdr.getObjectValue();
            writer.println(head.toString());
        }
        writer.flush();
        writer.close();
    }

    /**
     * Add header data from a file.
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

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    if (line.startsWith("#") || line.trim().length() == 0) {// $NON-NLS-1$
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
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Add a header.
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
     */
    public void remove(int index) {
        getHeaders().remove(index);
    }

    /**
     * Return the number of headers.
     */
    public int size() {
        return getHeaders().size();
    }

    /**
     * Return the header at index i.
     */
    public Header get(int i) {
        return (Header) getHeaders().get(i).getObjectValue();
    }

    /*
     * public String getHeaderHeaderForURL(URL url) { if
     * (!url.getProtocol().toUpperCase().trim().equals("HTTP") &&
     * !url.getProtocol().toUpperCase().trim().equals("HTTPS")) { return null; }
     *
     * StringBuilder sbHeader = new StringBuilder(); for (Iterator enum =
     * headers.iterator(); enum.hasNext();) { Header header = (Header)
     * enum.next(); if (url.getHost().endsWith(header.getDomain()) &&
     * url.getFile().startsWith(header.getPath()) && (System.currentTimeMillis() /
     * 1000) <= header.getExpires()) { if (sbHeader.length() > 0) {
     * sbHeader.append("; "); }
     * sbHeader.append(header.getName()).append("=").append( header.getValue()); } }
     *
     * if (sbHeader.length() != 0) { return sbHeader.toString(); } else { return
     * null; } }
     */

    /*
     * public void addHeaderFromHeader(String headerHeader, URL url) {
     * StringTokenizer st = new StringTokenizer(headerHeader, ";"); String nvp;
     *  // first n=v is name=value nvp = st.nextToken(); int index =
     * nvp.indexOf("="); String name = nvp.substring(0, index); String value =
     * nvp.substring(index + 1); String domain = url.getHost();
     *
     * Header newHeader = new Header(name, value); // check the rest of the
     * headers while (st.hasMoreTokens()) { nvp = st.nextToken(); nvp =
     * nvp.trim(); index = nvp.indexOf("="); if (index == -1) { index =
     * nvp.length(); } String key = nvp.substring(0, index);
     *
     * Vector removeIndices = new Vector(); for (int i = headers.size() - 1; i >=
     * 0; i--) { Header header = (Header) headers.get(i); if (header == null) {
     * continue; } if (header.getName().equals(newHeader.getName())) {
     * removeIndices.addElement(Integer.valueOf(i)); } }
     *
     * for (Enumeration e = removeIndices.elements(); e.hasMoreElements(); ) {
     * index = ((Integer) e.nextElement()).intValue(); headers.remove(index); }
     *  }
     */
    public void removeHeaderNamed(String name) {
        Vector<Integer> removeIndices = new Vector<Integer>();
        for (int i = getHeaders().size() - 1; i >= 0; i--) {
            Header header = (Header) getHeaders().get(i).getObjectValue();
            if (header == null) {
                continue;
            }
            if (header.getName().equalsIgnoreCase(name)) {
                removeIndices.addElement(Integer.valueOf(i));
            }
        }

        for (Enumeration<Integer> e = removeIndices.elements(); e.hasMoreElements();) {
            getHeaders().remove(e.nextElement().intValue());
        }
    }

    /**
     * Added support for SOAP related header stuff. 1-29-04 Peter Lin
     *
     * @return the SOAP header Object
     */
    public Object getSOAPHeader() {
        return this.SOAPHeader;
    }

    /**
     * Set the SOAPHeader with the SOAPHTTPConnection object. We may or may not
     * want to rename this to setHeaderObject(Object). Concievably, other
     * samplers may need this kind of functionality. 1-29-04 Peter Lin
     *
     * @param header
     */
    public void setSOAPHeader(Object header) {
        this.SOAPHeader = header;
    }

    /**
     * Merge the attributes with a another HeaderManager's attributes.
     * @param element The object to be merged with
     * @param preferLocalValues When both objects have a value for the
     *        same attribute, this flag determines which value is preferresd.
     */
    public HeaderManager merge(TestElement element, boolean preferLocalValues) {
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
                    if (!preferLocalValues) {
                        // prefer values from the other object
                        if ( (otherHeader.getValue() == null) || (otherHeader.getValue().length() == 0) ) {
                            // the other object has an empty value, so remove this value from the merged object
                            merged.remove(j);
                        } else {
                            // use the other object's value
                            mergedHeader.setValue(otherHeader.getValue());
                        }
                    }
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
}
