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

package org.apache.jmeter.protocol.http.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

// @see TestHTTPUtils for unit tests

/**
 * General purpose conversion utilities related to HTTP/HTML
 */
public class ConversionUtils {

    private static final String CHARSET_EQ = "charset="; // $NON-NLS-1$
    private static final int CHARSET_EQ_LEN = CHARSET_EQ.length();
    
    private static final String SLASHDOTDOT = "/.."; // $NON-NLS-1$
    private static final String DOTDOT = ".."; // $NON-NLS-1$
    private static final String SLASH = "/"; // $NON-NLS-1$
    private static final String COLONSLASHSLASH = "://"; // $NON-NLS-1$
    
    /**
     * Match /../[../] etc.
     */
    private static final Pattern MAKE_RELATIVE_PATTERN = Pattern.compile("^/((?:\\.\\./)+)"); // $NON-NLS-1$

    /**
     * Extract the encoding (charset) from the Content-Type, e.g.
     * "text/html; charset=utf-8".
     *
     * @param contentType
     *            string from which the encoding should be extracted
     * @return the charset encoding - or <code>null</code>, if none was found or
     *         the charset is not supported
     * @throws IllegalCharsetNameException
     *             if the found charset is not supported
     */
    public static String getEncodingFromContentType(String contentType){
        String charSet = null;
        if (contentType != null) {
            int charSetStartPos = contentType.toLowerCase(java.util.Locale.ENGLISH).indexOf(CHARSET_EQ);
            if (charSetStartPos >= 0) {
                charSet = contentType.substring(charSetStartPos + CHARSET_EQ_LEN);
                if (charSet != null) {
                    // Remove quotes from charset name, see bug 55852
                    charSet = StringUtils.replaceChars(charSet, "\'\"", null);
                    charSet = charSet.trim();
                    if (charSet.length() > 0) {
                        // See Bug 44784
                        int semi = charSet.indexOf(';');
                        if (semi == 0){
                            return null;
                        }
                        if (semi != -1) {
                            charSet = charSet.substring(0,semi);
                        }
                        if (!Charset.isSupported(charSet)){
                            return null;
                        }
                        return charSet;
                    }
                    return null;
                }
            }
        }
        return charSet;
    }

    /**
     * Generate an absolute URL from a possibly relative location,
     * allowing for extraneous leading "../" segments.
     * The Java {@link URL#URL(URL, String)} constructor does not remove these.
     *
     * @param baseURL the base URL which is used to resolve missing protocol/host in the location
     * @param location the location, possibly with extraneous leading "../"
     * @return URL with extraneous ../ removed
     * @throws MalformedURLException when the given <code>URL</code> is malformed
     * @see <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=46690">Bug 46690 - handling of 302 redirects with invalid relative paths</a>
     */
    public static URL makeRelativeURL(URL baseURL, String location) throws MalformedURLException{
        URL initial = new URL(baseURL,location);
        
        // skip expensive processing if it cannot apply
        if (!location.startsWith("../")){// $NON-NLS-1$
            return initial;
        }
        String path = initial.getPath();
        Matcher m = MAKE_RELATIVE_PATTERN.matcher(path);
        if (m.lookingAt()){
            String prefix = m.group(1); // get ../ or ../../ etc.
            if (location.startsWith(prefix)){
                return new URL(baseURL, location.substring(prefix.length()));
            }
        }
        return initial;
    }
    
    /**
     * @param url String Url to escape
     * @return String cleaned up url
     * @throws Exception when given <code>url</code> leads to a malformed URL or URI
     */
    public static String escapeIllegalURLCharacters(String url) throws Exception{
        String decodeUrl = URLDecoder.decode(url,StandardCharsets.UTF_8.name());
        URL urlString = new URL(decodeUrl);
        URI uri = new URI(urlString.getProtocol(), urlString.getUserInfo(), urlString.getHost(), urlString.getPort(), urlString.getPath(), urlString.getQuery(), urlString.getRef());
        return uri.toString();
    }
    
    /**
     * Checks a URL and encodes it if necessary,
     * i.e. if it is not currently correctly encoded.
     * Warning: it may not work on all unencoded URLs.
     * @param url non-encoded URL
     * @return URI which has been encoded as necessary
     * @throws URISyntaxException if parts of the url form a non valid URI
     */
    public static URI sanitizeUrl(URL url) throws URISyntaxException {
        try {
            return url.toURI(); // Assume the URL is already encoded
        } catch (URISyntaxException e) { // it's not, so encode it
          return new URI(
          url.getProtocol(),
          url.getUserInfo(),
          url.getHost(),
          url.getPort(),
          url.getPath(),
          url.getQuery(),
          url.getRef()); // anchor or fragment
        }
    }

    /**
     * collapses absolute or relative URLs containing '/..' converting
     * <code>http://host/path1/../path2</code> to <code>http://host/path2</code>
     * or <code>/one/two/../three</code> to
     * <code>/one/three</code>
     * 
     * @param url in which the '/..'s should be removed
     * @return collapsed URL
     * @see <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=49083">Bug 49083 - collapse /.. in redirect URLs</a>
     */
    public static String removeSlashDotDot(String url)
    {
        if (url == null) {
            return url;
        }
        
        url = url.trim();
        if(url.length() < 4 || !url.contains(SLASHDOTDOT)) {
            return url;
        }
        
        /**
         * http://auth@host:port/path1/path2/path3/?query#anchor
         */

        // get to 'path' part of the URL, preserving schema, auth, host if
        // present

        // find index of path start

        int dotSlashSlashIndex = url.indexOf(COLONSLASHSLASH);
        final int pathStartIndex;
        if (dotSlashSlashIndex >= 0)
        {
            // absolute URL
            pathStartIndex = url.indexOf(SLASH, dotSlashSlashIndex + COLONSLASHSLASH.length());
        } else
        {
            // document or context-relative URL like:
            // '/path/to'
            // OR '../path/to'
            // OR '/path/to/../path/'
            pathStartIndex = 0;
        }

        // find path endIndex
        int pathEndIndex = url.length();

        int questionMarkIdx = url.indexOf('?');
        if (questionMarkIdx > 0)
        {
            pathEndIndex = questionMarkIdx;
        } else {
            int anchorIdx = url.indexOf('#');
            if (anchorIdx > 0)
            {
                pathEndIndex = anchorIdx;
            }
        }

        // path is between idx='pathStartIndex' (inclusive) and
        // idx='pathEndIndex' (exclusive)
        String currentPath = url.substring(pathStartIndex, pathEndIndex);

        final boolean startsWithSlash = currentPath.startsWith(SLASH);
        final boolean endsWithSlash = currentPath.endsWith(SLASH);

        StringTokenizer st = new StringTokenizer(currentPath, SLASH);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens())
        {
            tokens.add(st.nextToken());
        }

        for (int i = 0; i < tokens.size(); i++)
        {
            if (i < tokens.size() - 1)
            {
                final String thisToken = tokens.get(i);

                // Verify for a ".." component at next iteration
                if (thisToken.length() > 0 && !thisToken.equals(DOTDOT) && tokens.get(i + 1).equals(DOTDOT))
                {
                    tokens.remove(i);
                    tokens.remove(i);
                    i = i - 2;
                    if (i < -1)
                    {
                        i = -1;
                    }
                }
            }

        }

        StringBuilder newPath = new StringBuilder();
        if (startsWithSlash) {
            newPath.append(SLASH);
        }
        for (int i = 0; i < tokens.size(); i++)
        {
            newPath.append(tokens.get(i));

            // append '/' if this isn't the last token or it is but the original
            // path terminated w/ a '/'
            boolean appendSlash = i < (tokens.size() - 1) ? true : endsWithSlash;
            if (appendSlash)
            {
                newPath.append(SLASH);
            }
        }

        // install new path
        StringBuilder s = new StringBuilder(url);
        s.replace(pathStartIndex, pathEndIndex, newPath.toString());
        return s.toString();
    }

}
