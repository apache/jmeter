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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JList;

/**
 * Display the links from a URL.
 * 
 * @author Derrick Oswald Created on December 24, 2002, 3:49 PM
 */
public class HTMLLinkBean extends JList implements Serializable, PropertyChangeListener {
	/**
	 * The underlying bean that provides our htmlparser specific properties.
	 */
	protected LinkBean mBean;

	/**
	 * Creates a new HTMLTextBean. This uses an underlying StringBean and
	 * displays the text.
	 */
	public HTMLLinkBean() {
		getBean().addPropertyChangeListener(this);
	}

	/**
	 * Return the underlying bean object. Creates a new one if it hasn't been
	 * initialized yet.
	 * 
	 * @return The StringBean this bean uses to fetch text.
	 */
	protected LinkBean getBean() {
		if (null == mBean)
			mBean = new LinkBean();

		return (mBean);
	}

	/**
	 * Return the minimum dimension for this visible bean.
	 */
	public Dimension getMinimumSize() {
		FontMetrics metrics;
		int width;
		int height;

		metrics = getFontMetrics(getFont());
		width = metrics.stringWidth("http://localhost");
		height = metrics.getLeading() + metrics.getHeight() + metrics.getDescent();

		return (new Dimension(width, height));
	}

	/**
	 * Add a PropertyChangeListener to the listener list. The listener is
	 * registered for all properties.
	 * <p>
	 * <em>Delegates to the underlying StringBean</em>
	 * 
	 * @param listener
	 *            The PropertyChangeListener to be added.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
		getBean().addPropertyChangeListener(listener);
	}

	/**
	 * Remove a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties.
	 * <p>
	 * <em>Delegates to the underlying StringBean</em>
	 * 
	 * @param the
	 *            PropertyChangeListener to be removed.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
		getBean().removePropertyChangeListener(listener);
	}

	//
	// Properties
	//

	/**
	 * Getter for property links.
	 * <p>
	 * <em>Delegates to the underlying StringBean</em>
	 * 
	 * @return Value of property links.
	 */
	public URL[] getLinks() {
		return (getBean().getLinks());
	}

	/**
	 * Getter for property URL.
	 * <p>
	 * <em>Delegates to the underlying StringBean</em>
	 * 
	 * @return Value of property URL.
	 */
	public String getURL() {
		return (getBean().getURL());
	}

	/**
	 * Setter for property URL.
	 * <p>
	 * <em>Delegates to the underlying StringBean</em>
	 * 
	 * @param url
	 *            New value of property URL.
	 */
	public void setURL(String url) {
		getBean().setURL(url);
	}

	/**
	 * Getter for property Connection.
	 * 
	 * @return Value of property Connection.
	 */
	public URLConnection getConnection() {
		return (getBean().getConnection());
	}

	/**
	 * Setter for property Connection.
	 * 
	 * @param url
	 *            New value of property Connection.
	 */
	public void setConnection(URLConnection connection) {
		getBean().setConnection(connection);
	}

	//
	// PropertyChangeListener inteface
	//

	/**
	 * Responds to changes in the underlying bean's properties.
	 * 
	 * @param event
	 *            The event triggering this listener method call.
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getPropertyName().equals(LinkBean.PROP_LINKS_PROPERTY)) {
			setListData(getBean().getLinks());
		}
	}
}
