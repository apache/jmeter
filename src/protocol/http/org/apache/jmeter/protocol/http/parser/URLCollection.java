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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Collection class designed for handling URLs
 * <p>
 * Before a URL is added to the collection, it is wrapped in a URLString class.
 * The iterator unwraps the URL before return.
 * <p>
 * N.B. Designed for use by HTMLParser, so is not a full implementation - e.g.
 * does not support remove()
 *
 */
public class URLCollection implements Iterable<URL> {
    private static final Logger log = LoggerFactory.getLogger(URLCollection.class);
    private final Collection<URLString> coll;

    /**
     * Creates a new URLCollection from an existing Collection
     *
     * @param c collection to start with (Must not be {@code null})
     */
    public URLCollection(Collection<URLString> c) {
        coll = Validate.notNull(c);
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

    /**
     * Convenience method for adding URLs to the collection. If the url
     * parameter is <code>null</code>, empty or URL is malformed, nothing is
     * done
     *
     * @param url
     *            String, may be null or empty
     * @param baseUrl
     *            base for <code>url</code> to add information, which might be
     *            missing in <code>url</code>
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
            // No WARN message to avoid performance impact
            if(log.isDebugEnabled()) {
                log.debug("Error occurred building relative url for:"+url+", message:"+mfue.getMessage());
            }
            // No point in adding the URL as String as it will result in null 
            // returned during iteration, see URLString
            // See https://bz.apache.org/bugzilla/show_bug.cgi?id=55092
            return false;
        }
        return b;
    }

    @Override
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

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        /*
         * Unwraps the URLString class to return the URL
         */
        @Override
        public URL next() {
            return iter.next().getURL();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
