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


import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HTMLParser} subclasses can parse HTML content to obtain URLs.
 */
public abstract class HTMLParser extends BaseParser {

    private static final Logger log = LoggerFactory.getLogger(HTMLParser.class);

    protected static final String ATT_BACKGROUND    = "background";// $NON-NLS-1$
    protected static final String ATT_CODE          = "code";// $NON-NLS-1$
    protected static final String ATT_CODEBASE      = "codebase";// $NON-NLS-1$
    protected static final String ATT_DATA          = "data";// $NON-NLS-1$
    protected static final String ATT_HREF          = "href";// $NON-NLS-1$
    protected static final String ATT_REL           = "rel";// $NON-NLS-1$
    protected static final String ATT_SRC           = "src";// $NON-NLS-1$
    protected static final String ATT_STYLE         = "style";// $NON-NLS-1$
    protected static final String ATT_TYPE          = "type";// $NON-NLS-1$
    protected static final String ATT_IS_IMAGE      = "image";// $NON-NLS-1$
    protected static final String TAG_APPLET        = "applet";// $NON-NLS-1$
    protected static final String TAG_BASE          = "base";// $NON-NLS-1$
    protected static final String TAG_BGSOUND       = "bgsound";// $NON-NLS-1$
    protected static final String TAG_BODY          = "body";// $NON-NLS-1$
    protected static final String TAG_EMBED         = "embed";// $NON-NLS-1$
    protected static final String TAG_FRAME         = "frame";// $NON-NLS-1$
    protected static final String TAG_IFRAME        = "iframe";// $NON-NLS-1$
    protected static final String TAG_IMAGE         = "img";// $NON-NLS-1$
    protected static final String TAG_INPUT         = "input";// $NON-NLS-1$
    protected static final String TAG_LINK          = "link";// $NON-NLS-1$
    protected static final String TAG_OBJECT        = "object";// $NON-NLS-1$
    protected static final String TAG_SCRIPT        = "script";// $NON-NLS-1$
    protected static final String STYLESHEET        = "stylesheet";// $NON-NLS-1$

    protected static final String SHORTCUT_ICON     = "shortcut icon";
    protected static final String ICON              = "icon";

    protected static final String IE_UA             = "MSIE ([0-9]+.[0-9]+)";// $NON-NLS-1$
    protected static final Pattern IE_UA_PATTERN    = Pattern.compile(IE_UA);
    private   static final float IE_10                = 10.0f;

    public static final String PARSER_CLASSNAME = "htmlParser.className"; // $NON-NLS-1$

    public static final String DEFAULT_PARSER =
        "org.apache.jmeter.protocol.http.parser.LagartoBasedHtmlParser"; // $NON-NLS-1$

    private static final Pattern NORMALIZE_URL_PATTERN = Pattern.compile("[\n\r\b\f]+"); //$NON-NLS-1$

    /**
     * Protected constructor to prevent instantiation except from within
     * subclasses.
     */
    protected HTMLParser() {
    }

    /**
     * Get the URLs for all the resources that a browser would automatically
     * download following the download of the HTML content, that is: images,
     * stylesheets, javascript files, applets, etc...
     * <p>
     * URLs should not appear twice in the returned iterator.
     * <p>
     * Malformed URLs can be reported to the caller by having the Iterator
     * return the corresponding RL String. Overall problems parsing the html
     * should be reported by throwing an HTMLParseException.
     * @param userAgent
     *            User Agent
     *
     * @param html
     *            HTML code
     * @param baseUrl
     *            Base URL from which the HTML code was obtained
     * @param encoding Charset
     * @return an Iterator for the resource URLs
     * @throws HTMLParseException when parsing the <code>html</code> fails
     */
    @Override
    public Iterator<URL> getEmbeddedResourceURLs(
            String userAgent, byte[] html, URL baseUrl, String encoding) throws HTMLParseException {
        // The Set is used to ignore duplicated binary files.
        // Using a LinkedHashSet to avoid unnecessary overhead in iterating
        // the elements in the set later on. As a side-effect, this will keep
        // them roughly in order, which should be a better model of browser
        // behaviour.

        Collection<URLString> col = new LinkedHashSet<>();
        return getEmbeddedResourceURLs(userAgent, html, baseUrl, new URLCollection(col),encoding);

        // An additional note on using HashSets to store URLs: I just
        // discovered that obtaining the hashCode of a java.net.URL implies
        // a domain-name resolution process. This means significant delays
        // can occur, even more so if the domain name is not resolvable.
        // Whether this can be a problem in practical situations I can't tell,
        // but
        // thought I'd keep a note just in case...
        // BTW, note that using a List and removing duplicates via scan
        // would not help, since URL.equals requires name resolution too.
        // The above problem has now been addressed with the URLString and
        // URLCollection classes.

    }

    /**
     * Get the URLs for all the resources that a browser would automatically
     * download following the download of the HTML content, that is: images,
     * stylesheets, javascript files, applets, etc...
     * <p>
     * All URLs should be added to the Collection.
     * <p>
     * Malformed URLs can be reported to the caller by having the Iterator
     * return the corresponding RL String. Overall problems parsing the html
     * should be reported by throwing an HTMLParseException.
     * <p>
     * N.B. The Iterator returns URLs, but the Collection will contain objects
     * of class URLString.
     *
     * @param userAgent
     *            User Agent
     * @param html
     *            HTML code
     * @param baseUrl
     *            Base URL from which the HTML code was obtained
     * @param coll
     *            URLCollection
     * @param encoding Charset
     * @return an Iterator for the resource URLs
     * @throws HTMLParseException when parsing the <code>html</code> fails
     */
    public abstract Iterator<URL> getEmbeddedResourceURLs(
            String userAgent, byte[] html, URL baseUrl, URLCollection coll, String encoding)
            throws HTMLParseException;

    /**
     * Get the URLs for all the resources that a browser would automatically
     * download following the download of the HTML content, that is: images,
     * stylesheets, javascript files, applets, etc...
     * <p>
     * N.B. The Iterator returns URLs, but the Collection will contain objects
     * of class URLString.
     *
     * @param userAgent
     *            User Agent
     * @param html
     *            HTML code
     * @param baseUrl
     *            Base URL from which the HTML code was obtained
     * @param coll
     *            Collection - will contain URLString objects, not URLs
     * @param encoding Charset
     * @return an Iterator for the resource URLs
     * @throws HTMLParseException when parsing the <code>html</code> fails
     */
    public Iterator<URL> getEmbeddedResourceURLs(
            String userAgent, byte[] html, URL baseUrl, Collection<URLString> coll, String encoding)
                    throws HTMLParseException {
        return getEmbeddedResourceURLs(userAgent, html, baseUrl, new URLCollection(coll), encoding);
    }

    /**
     *
     * @param ieVersion Float IE version
     * @return true if IE version &lt; IE v10
     */
    protected final boolean isEnableConditionalComments(Float ieVersion) {
        // Conditional comment have been dropped in IE10
        // http://msdn.microsoft.com/en-us/library/ie/hh801214%28v=vs.85%29.aspx
        return ieVersion != null && ieVersion.floatValue() < IE_10;
    }

    /**
     *
     * @param userAgent User Agent
     * @return version null if not IE or the version after MSIE
     */
    protected Float extractIEVersion(String userAgent) {
        if (StringUtils.isEmpty(userAgent)) {
            log.info("userAgent is null");
            return null;
        }
        Matcher matcher = IE_UA_PATTERN.matcher(userAgent);
        String ieVersion = null;
        if (matcher.find()) {
            if (matcher.groupCount() > 0) {
                ieVersion = matcher.group(1);
            } else {
                ieVersion = matcher.group();
            }
        }
        if (ieVersion != null) {
            return Float.valueOf(ieVersion);
        } else {
            return null;
        }
    }

    /**
     * Normalizes URL as browsers do
     * @param url {@link CharSequence}
     * @return normalized url
     */
    protected static String normalizeUrlValue(CharSequence url) {
        if (!StringUtils.isEmpty(url)) {
            String trimmed = NORMALIZE_URL_PATTERN.matcher(url.toString().trim()).replaceAll("");
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return null;
    }
}
