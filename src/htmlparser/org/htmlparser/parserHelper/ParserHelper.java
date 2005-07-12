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
package org.htmlparser.parserHelper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserFeedback;

public class ParserHelper implements Serializable {

	public ParserHelper() {
		super();
	}

	/**
	 * Opens a connection using the given url.
	 * 
	 * @param url
	 *            The url to open.
	 * @param feedback
	 *            The ibject to use for messages or <code>null</code>.
	 * @exception ParserException
	 *                if an i/o exception occurs accessing the url.
	 */
	public static URLConnection openConnection(URL url, ParserFeedback feedback) throws ParserException {
		URLConnection ret;

		try {
			ret = url.openConnection();
		} catch (IOException ioe) {
			String msg = "HTMLParser.openConnection() : Error in opening a connection to " + url.toExternalForm();
			ParserException ex = new ParserException(msg, ioe);
			if (null != feedback)
				feedback.error(msg, ex);
			throw ex;
		}

		return (ret);
	}

	/**
	 * Opens a connection based on a given string. The string is either a file,
	 * in which case <code>file://localhost</code> is prepended to a canonical
	 * path derived from the string, or a url that begins with one of the known
	 * protocol strings, i.e. <code>http://</code>. Embedded spaces are
	 * silently converted to %20 sequences.
	 * 
	 * @param string
	 *            The name of a file or a url.
	 * @param feedback
	 *            The object to use for messages or <code>null</code> for no
	 *            feedback.
	 * @exception ParserException
	 *                if the string is not a valid url or file.
	 */
	public static URLConnection openConnection(String string, ParserFeedback feedback) throws ParserException {
		final String prefix = "file://localhost";
		String resource;
		URL url;
		StringBuffer buffer;
		URLConnection ret;

		try {
			url = new URL(LinkProcessor.fixSpaces(string));
			ret = ParserHelper.openConnection(url, feedback);
		} catch (MalformedURLException murle) { // try it as a file
			try {
				File file = new File(string);
				resource = file.getCanonicalPath();
				buffer = new StringBuffer(prefix.length() + resource.length());
				buffer.append(prefix);
				buffer.append(resource);
				url = new URL(LinkProcessor.fixSpaces(buffer.toString()));
				ret = ParserHelper.openConnection(url, feedback);
				if (null != feedback)
					feedback.info(url.toExternalForm());
			} catch (MalformedURLException murle2) {
				String msg = "HTMLParser.openConnection() : Error in opening a connection to " + string;
				ParserException ex = new ParserException(msg, murle2);
				if (null != feedback)
					feedback.error(msg, ex);
				throw ex;
			} catch (IOException ioe) {
				String msg = "HTMLParser.openConnection() : Error in opening a connection to " + string;
				ParserException ex = new ParserException(msg, ioe);
				if (null != feedback)
					feedback.error(msg, ex);
				throw ex;
			}
		}

		return (ret);
	}

	/**
	 * Lookup a character set name.
	 * <em>Vacuous for JVM's without <code>java.nio.charset</code>.</em>
	 * This uses reflection so the code will still run under prior JDK's but in
	 * that case the default is always returned.
	 * 
	 * @param name
	 *            The name to look up. One of the aliases for a character set.
	 * @param _default
	 *            The name to return if the lookup fails.
	 */
	public static String findCharset(String name, String _default) {
		String ret;

		try {
			Class cls;
			java.lang.reflect.Method method;
			Object object;

			cls = Class.forName("java.nio.charset.Charset");
			method = cls.getMethod("forName", new Class[] { String.class });
			object = method.invoke(null, new Object[] { name });
			method = cls.getMethod("name", new Class[] {});
			object = method.invoke(object, new Object[] {});
			ret = (String) object;
		} catch (ClassNotFoundException cnfe) {
			// for reflection exceptions, assume the name is correct
			ret = name;
		} catch (NoSuchMethodException nsme) {
			// for reflection exceptions, assume the name is correct
			ret = name;
		} catch (IllegalAccessException ia) {
			// for reflection exceptions, assume the name is correct
			ret = name;
		} catch (java.lang.reflect.InvocationTargetException ita) {
			// java.nio.charset.IllegalCharsetNameException
			// and java.nio.charset.UnsupportedCharsetException
			// return the default
			ret = _default;
		}

		return (ret);
	}

}
