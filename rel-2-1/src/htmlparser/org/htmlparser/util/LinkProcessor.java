// $Header$
/*
 * ====================================================================
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.
package org.htmlparser.util;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Processor class for links, is present basically as a utility class.
 */
public class LinkProcessor implements Serializable {
	/**
	 * Overriding base URL. If set, this is used instead of a provided base URL
	 * in extract().
	 */
	private String baseUrl;

	/**
	 * Create an HTMLLinkProcessor.
	 */
	public LinkProcessor() {
		baseUrl = null;
	}

	/**
	 * Create an absolute URL from a possibly relative link and a base URL.
	 * 
	 * @param link
	 *            The reslative portion of a URL.
	 * @param base
	 *            The base URL unless overridden by the current baseURL
	 *            property.
	 * @return The fully qualified URL or the original link if a failure
	 *         occured.
	 */
	public String extract(String link, String base) throws ParserException {
		String path; // path portion of constructed URL
		boolean modified; // true if path is modified by us
		boolean absolute; // true if link starts with "/"
		int index;
		String ret;

		try {
			if (null == link)
				link = "";
			if (null != getBaseUrl())
				base = getBaseUrl();
			if ((null == base) || ("".equals(link)))
				ret = link;
			else {
				URL url = constructUrl(link, base);
				ret = url.toExternalForm();
			}
		} catch (MalformedURLException murle) {
			ret = link;
		}

		return (Translate.decode(ret));
	}

	public URL constructUrl(String link, String base) throws MalformedURLException {
		String path;
		boolean modified;
		boolean absolute;
		int index;
		URL url; // constructed URL combining relative link and base
		url = new URL(new URL(base), link);
		path = url.getFile();
		modified = false;
		absolute = link.startsWith("/");
		if (!absolute) { // we prefer to fix incorrect relative links
			// this doesn't fix them all, just the ones at the start
			while (path.startsWith("/.")) {
				if (path.startsWith("/../")) {
					path = path.substring(3);
					modified = true;
				} else if (path.startsWith("/./") || path.startsWith("/.")) {
					path = path.substring(2);
					modified = true;
				} else
					break;
			}
		}
		// fix backslashes
		while (-1 != (index = path.indexOf("/\\"))) {
			path = path.substring(0, index + 1) + path.substring(index + 2);
			modified = true;
		}
		if (modified)
			url = new URL(url, path);
		return url;
	}

	/**
	 * Turn spaces into %20.
	 * 
	 * @param url
	 *            The url containing spaces.
	 * @return The URL with spaces as %20 sequences.
	 */
	public static String fixSpaces(String url) {
		int index;
		int length;
		char ch;
		StringBuffer returnURL;

		index = url.indexOf(' ');
		if (-1 != index) {
			length = url.length();
			returnURL = new StringBuffer(length * 3);
			returnURL.append(url.substring(0, index));
			for (int i = index; i < length; i++) {
				ch = url.charAt(i);
				if (ch == ' ')
					returnURL.append("%20");
				else
					returnURL.append(ch);
			}
			url = returnURL.toString();
		}

		return (url);
	}

	/**
	 * Check if a resource is a valid URL.
	 * 
	 * @param resourceLocn
	 *            The resource to test.
	 * @return <code>true</code> if the resource is a valid URL.
	 */
	public static boolean isURL(String resourceLocn) {
		URL url;
		boolean ret;

		try {
			url = new URL(resourceLocn);
			ret = true;
		} catch (MalformedURLException murle) {
			ret = false;
		}

		return (ret);
	}

	/**
	 * Returns the baseUrl.
	 * 
	 * @return String
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Sets the baseUrl.
	 * 
	 * @param baseUrl
	 *            The baseUrl to set
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public static String removeLastSlash(String baseUrl) {
		if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
			return baseUrl.substring(0, baseUrl.length() - 1);
		} else {
			return baseUrl;
		}
	}

}
