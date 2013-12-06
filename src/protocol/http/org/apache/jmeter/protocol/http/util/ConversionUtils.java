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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

// @see TestHTTPUtils for unit tests

/**
 * General purpose conversion utilities related to HTTP/HTML
 */
public class ConversionUtils {

    private static final String CHARSET_EQ = "charset="; // $NON-NLS-1$
    private static final int CHARSET_EQ_LEN = CHARSET_EQ.length();
    
    private static final String SLASHDOTDOT = "/..";
    private static final String DOTDOT = "..";
    private static final String SLASH = "/";
    private static final String COLONSLASHSLASH = "://";
    private static final String COLON = ":";

    /**
     * Extract the encoding (charset) from the Content-Type,
     * e.g. "text/html; charset=utf-8".
     *
     * @param contentType
     * @return the charset encoding - or null, if none was found or the charset is not supported
     * @throws IllegalCharsetNameException 
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
     * Generate a relative URL, allowing for extraneous leading "../" segments.
     * The Java {@link URL#URL(URL, String)} constructor does not remove these.
     *
     * @param baseURL
     * @param location relative location, possibly with extraneous leading "../"
     * @return URL with extraneous ../ removed
     * @throws MalformedURLException
     */
    public static URL makeRelativeURL(URL baseURL, String location) throws MalformedURLException{
        URL initial = new URL(baseURL,location);
        
        // skip expensive processing if it cannot apply
        if (!location.startsWith("../")){// $NON-NLS-1$
            return initial;
        }
        String path = initial.getPath();
        // Match /../[../] etc.
        Pattern p = Pattern.compile("^/((?:\\.\\./)+)"); // $NON-NLS-1$
        Matcher m = p.matcher(path);
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
     * @throws Exception
     */
    public static String escapeIllegalURLCharacters(String url) throws Exception{
        String decodeUrl = URLDecoder.decode(url,"UTF-8");
        URL urlString = new URL(decodeUrl);
        URI uri = new URI(urlString.getProtocol(), urlString.getUserInfo(), urlString.getHost(), urlString.getPort(), urlString.getPath(), urlString.getQuery(), urlString.getRef());
        return uri.toString();
    }
    
    /**
     * Escapes reserved chars in a non-encoded URL or partially encoded one.
     * Warning: it won't work on all unencoded URLs.
     * For example, the unencoded URL http://localhost/% will cause an Exception.
     * Any instances of % must have been encoded as %25 within the path portion.
     * @param url non-encoded or partially encoded URL
     * @return URI which has been encoded as necessary
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException 
     */
    public static final URI sanitizeUrl(URL url) throws URISyntaxException, UnsupportedEncodingException {
        URIBuilder builder = 
                new URIBuilder()
            .setScheme(url.getProtocol())
            .setHost(url.getHost())
            .setPort(url.getPort())
            .setUserInfo(url.getUserInfo())
            .setPath(url.getPath() != null ? URLDecoder.decode(url.getPath(), "UTF-8") : null) // $NON-NLS-1$
            .setQuery(url.getQuery());
        URI uri = builder.build();
        return uri;
    }

    /**
     * collapses absolute or relative URLs containing '/..' converting
     * http://host/path1/../path2 to http://host/path2 or /one/two/../three to
     * /one/three
     * 
     * @param url
     * @return collapsed URL
     */
    public static String removeSlashDotDot(String url)
    {
        if (url == null || (url = url.trim()).length() < 4 || !url.contains(SLASHDOTDOT))
        {
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
        List<String> tokens = new ArrayList<String>();
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

    /**
     * Builds Full url (containing scheme, host,port) from relative URL
     * as per RFC http://tools.ietf.org/html/rfc3986#section-4.2
     * @param lastUrl URL
     * @param redirectLocation absolute URL
     * @return Full URL
     * 
     */
    public static final String buildFullUrlFromRelative(URL lastUrl,
            String redirectLocation) {
        StringBuilder builder = new StringBuilder();
        builder.append(lastUrl.getProtocol())
            .append(COLONSLASHSLASH)
            .append(lastUrl.getHost());
        if(lastUrl.getPort()!= -1) {
            builder.append(COLON).append(lastUrl.getPort());
        }
        if(redirectLocation.startsWith(SLASH)) {
            // A relative reference that begins with a single slash 
            // character is termed an absolute-path reference
            builder.append(redirectLocation);
        } else {
            // A relative reference that does not begin with a
            // slash character is termed a relative-path reference
            // We need to merge a relative-path reference with the path of the base URI
            // http://tools.ietf.org/html/rfc3986#section-5.2.3
            if(lastUrl.getPath().isEmpty()) {
                // If the base URI has a defined authority component and an empty
                // path, then return a string consisting of "/" concatenated with the
                // reference's path; otherwise,
                builder.append(SLASH).append(redirectLocation);
            } else {
                // string consisting of the reference's path component
                // appended to all but the last segment of the base URI's path (i.e.,
                // excluding any characters after the right-most "/" in the base URI
                // path, or excluding the entire base URI path if it does not contain
                // any "/" characters).     
                String path = lastUrl.getPath();
                int index = path.lastIndexOf(SLASH);
                if(index == -1) {
                    builder.append(SLASH).append(redirectLocation);
                } else {
                    builder.append(path.substring(0, index+1))
                        .append(redirectLocation);
                }
            }
        }
        return builder.toString();
    }
}
