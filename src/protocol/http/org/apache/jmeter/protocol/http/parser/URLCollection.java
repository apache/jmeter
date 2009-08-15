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

package org.apache.jmeter.protocol.http.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jmeter.protocol.http.util.ConversionUtils;

/**
 * Collection class designed for handling URLs
 *
 * Before a URL is added to the collection, it is wrapped in a URLString class.
 * The iterator unwraps the URL before return.
 *
 * N.B. Designed for use by HTMLParser, so is not a full implementation - e.g.
 * does not support remove()
 *
 */
public class URLCollection {
    private final Collection<URLString> coll;

    /**
     * Creates a new URLCollection from an existing Collection
     *
     */
    public URLCollection(Collection<URLString> c) {
        coll = c;
    }

    /**
     * Adds the URL to the Collection, first wrapping it in the URLString class
     *
     * @param u
     *            URL to add
     * @return boolean condition returned by the add() method of the underlying
     *         collection
     */
    public boolean add(URL u) {
        return coll.add(new URLString(u));
    }

    /*
     * Adds the string to the Collection, first wrapping it in the URLString
     * class
     *
     * @param s string to add @return boolean condition returned by the add()
     * method of the underlying collection
     */
    private boolean add(String s) {
        return coll.add(new URLString(s));
    }

    /**
     * Convenience method for adding URLs to the collection If the url parameter
     * is null or empty, nothing is done
     *
     * @param url
     *            String, may be null or empty
     * @param baseUrl
     * @return boolean condition returned by the add() method of the underlying
     *         collection
     */
    public boolean addURL(String url, URL baseUrl) {
        if (url == null || url.length() == 0) {
            return false;
        }
        //url.replace('+',' ');
        url=StringEscapeUtils.unescapeXml(url);
        boolean b = false;
        try {
            b = this.add(ConversionUtils.makeRelativeURL(baseUrl, url));
        } catch (MalformedURLException mfue) {
            // TODO log a warning message?
            b = this.add(url);// Add the string if cannot create the URL
        }
        return b;
    }

    public Iterator<URL> iterator() {
        return new UrlIterator(coll.iterator());
    }

    /*
     * Private iterator used to unwrap the URL from the URLString class
     *
     */
    private static class UrlIterator implements Iterator<URL> {
        private final Iterator<URLString> iter;

        UrlIterator(Iterator<URLString> i) {
            iter = i;
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        /*
         * Unwraps the URLString class to return the URL
         */
        public URL next() {
            return iter.next().getURL();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
