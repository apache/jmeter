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
package org.htmlparser.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.ObjectFindingVisitor;

/**
 * Extract strings from a URL.
 * 
 * @author Derrick Oswald Created on December 23, 2002, 5:01 PM
 */
public class LinkBean extends Object implements Serializable {
	/**
	 * Property name in event where the URL contents changes.
	 */
	public static final String PROP_LINKS_PROPERTY = "Links";

	/**
	 * Property name in event where the URL changes.
	 */
	public static final String PROP_URL_PROPERTY = "URL";

	/**
	 * Bound property support.
	 */
	protected PropertyChangeSupport mPropertySupport;

	/**
	 * The strings extracted from the URL.
	 */
	protected URL[] mLinks;

	/**
	 * The parser used to extract strings.
	 */
	protected Parser mParser;

	/** Creates new StringBean */
	public LinkBean() {
		mPropertySupport = new PropertyChangeSupport(this);
		mLinks = null;
		mParser = new Parser();
	}

	//
	// internals
	//

	protected URL[] extractLinks(String url) throws ParserException {
		Parser parser;
		Vector vector;
		Node node;
		LinkTag link;
		URL[] ret;

		parser = new Parser(url);
		parser.registerScanners();
		ObjectFindingVisitor visitor = new ObjectFindingVisitor(LinkTag.class);
		parser.visitAllNodesWith(visitor);
		Node[] nodes = visitor.getTags();
		vector = new Vector();
		for (int i = 0; i < nodes.length; i++)
			try {
				link = (LinkTag) nodes[i];
				vector.add(new URL(link.getLink()));
			} catch (MalformedURLException murle) {
				// vector.remove (i);
				// i--;
			}
		ret = new URL[vector.size()];
		vector.copyInto(ret);

		return (ret);
	}

	/**
	 * Determine if two arrays of URL's are the same.
	 * 
	 * @param array1
	 *            One array of URL's
	 * @param array2
	 *            Another array of URL's
	 * @return <code>true</code> if the URL's match in number and value,
	 *         <code>false</code> otherwise.
	 */
	protected boolean equivalent(URL[] array1, URL[] array2) {
		boolean ret;

		ret = false;
		if ((null == array1) && (null == array2))
			ret = true;
		else if ((null != array1) && (null != array2))
			if (array1.length == array2.length) {
				ret = true;
				for (int i = 0; i < array1.length && ret; i++)
					if (!(array1[i] == array2[i]))
						ret = false;
			}

		return (ret);
	}

	//
	// Property change support.
	//

	/**
	 * Add a PropertyChangeListener to the listener list. The listener is
	 * registered for all properties.
	 * 
	 * @param listener
	 *            The PropertyChangeListener to be added.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		mPropertySupport.addPropertyChangeListener(listener);
	}

	/**
	 * Remove a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties.
	 * 
	 * @param the
	 *            PropertyChangeListener to be removed.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		mPropertySupport.removePropertyChangeListener(listener);
	}

	//
	// Properties
	//

	/**
	 * Refetch the URL contents.
	 */
	private void setLinks() {
		String url;
		URL[] urls;
		URL[] oldValue;

		url = getURL();
		if (null != url)
			try {
				urls = extractLinks(getURL());
				if (!equivalent(mLinks, urls)) {
					oldValue = mLinks;
					mLinks = urls;
					mPropertySupport.firePropertyChange(PROP_LINKS_PROPERTY, oldValue, mLinks);
				}
			} catch (ParserException hpe) {
				mLinks = null;
			}
	}

	/**
	 * Getter for property links.
	 * 
	 * @return Value of property links.
	 */
	public URL[] getLinks() {
		if (null == mLinks)
			try {
				mLinks = extractLinks(getURL());
				mPropertySupport.firePropertyChange(PROP_LINKS_PROPERTY, null, mLinks);
			} catch (ParserException hpe) {
				mLinks = null;
			}

		return (mLinks);
	}

	/**
	 * Getter for property URL.
	 * 
	 * @return Value of property URL.
	 */
	public String getURL() {
		return (mParser.getURL());
	}

	/**
	 * Setter for property URL.
	 * 
	 * @param url
	 *            New value of property URL.
	 */
	public void setURL(String url) {
		String old;

		old = getURL();
		if (((null == old) && (null != url)) || ((null != old) && !old.equals(url))) {
			try {
				mParser.setURL(url);
				mPropertySupport.firePropertyChange(PROP_URL_PROPERTY, old, getURL());
				setLinks();
			} catch (ParserException hpe) {
				// failed... now what
			}
		}
	}

	/**
	 * Getter for property Connection.
	 * 
	 * @return Value of property Connection.
	 */
	public URLConnection getConnection() {
		return (mParser.getConnection());
	}

	/**
	 * Setter for property Connection.
	 * 
	 * @param url
	 *            New value of property Connection.
	 */
	public void setConnection(URLConnection connection) {
		try {
			mParser.setConnection(connection);
			setLinks();
		} catch (ParserException hpe) {
			// failed... now what
		}
	}
}
