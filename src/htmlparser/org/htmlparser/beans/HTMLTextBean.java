/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
import java.net.URLConnection;

import javax.swing.JTextArea;

/**
 * Display the textual URL contents.
 * @author Derrick Oswald
 * Created on December 24, 2002, 3:49 PM
 */
public class HTMLTextBean
    extends JTextArea
    implements Serializable, PropertyChangeListener
{
    /**
     * The underlying bean that provides our htmlparser specific properties.
     */
    protected StringBean mBean;

    /**
     * Creates a new HTMLTextBean.
     * This uses an underlying StringBean and displays the text.
     */
    public HTMLTextBean()
    {
        getBean().addPropertyChangeListener(this);
    }

    /**
     * Return the minimum dimension for this visible bean.
     */
    public Dimension getMinimumSize()
    {
        FontMetrics metrics;
        int width;
        int height;

        metrics = getFontMetrics(getFont());
        width = metrics.stringWidth("Hello World");
        height =
            metrics.getLeading() + metrics.getHeight() + metrics.getDescent();

        return (new Dimension(width, height));
    }

    /**
     * Add a PropertyChangeListener to the listener list.
     * The listener is registered for all properties.
     * <p><em>Delegates to the underlying StringBean</em>
     * @param listener The PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        super.addPropertyChangeListener(listener);
        getBean().addPropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.
     * This removes a PropertyChangeListener that was registered for all properties.
     * <p><em>Delegates to the underlying StringBean</em>
     * @param the PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        super.addPropertyChangeListener(listener);
        getBean().removePropertyChangeListener(listener);
    }

    //
    // Properties
    //

    /**
     * Return the underlying bean object.
     * Creates a new one if it hasn't been initialized yet.
     * @return The StringBean this bean uses to fetch text.
     */
    public StringBean getBean()
    {
        if (null == mBean)
            mBean = new StringBean();

        return (mBean);
    }

    /**
     * Getter for property strings.
     * <p><em>Delegates to the underlying StringBean</em>
     * @return Value of property strings.
     */
    public String getStrings()
    {
        return (getBean().getStrings());
    }

    /**
     * Getter for property links.
     * <p><em>Delegates to the underlying StringBean</em>
     * @return Value of property links.
     */
    public boolean getLinks()
    {
        return (getBean().getLinks());
    }

    /**
     * Setter for property links.
     * <p><em>Delegates to the underlying StringBean</em>
     * @param links New value of property links.
     */
    public void setLinks(boolean links)
    {
        getBean().setLinks(links);
    }

    /**
     * Getter for property URL.
     * <p><em>Delegates to the underlying StringBean</em>
     * @return Value of property URL.
     */
    public String getURL()
    {
        return (getBean().getURL());
    }

    /**
     * Setter for property URL.
     * <p><em>Delegates to the underlying StringBean</em>
     * @param url New value of property URL.
     */
    public void setURL(String url)
    {
        getBean().setURL(url);
    }

    /**
     * Get the current 'replace non breaking spaces' state.
     * @return The <code>true</code> if non-breaking spaces (character '\u00a0',
     * numeric character reference &160; or character entity reference &nbsp;)
     * are to be replaced with normal spaces (character '\u0020').
     */
    public boolean getReplaceNonBreakingSpaces()
    {
        return (getBean().getReplaceNonBreakingSpaces());
    }

    /**
     * Set the 'replace non breaking spaces' state.
     * @param replace_space <code>true</code> if non-breaking spaces (character '\u00a0',
     * numeric character reference &160; or character entity reference &nbsp;)
     * are to be replaced with normal spaces (character '\u0020').
     */
    public void setReplaceNonBreakingSpaces(boolean replace_space)
    {
        getBean().setReplaceNonBreakingSpaces(replace_space);
    }

    /**
     * Get the current 'collapse whitespace' state.
     * If set to <code>true</code> this emulates the operation of browsers
     * in interpretting text where auser agents should collapse input white
     * space sequences when producing output inter-word space.
     * See HTML specification section 9.1 White space
     * http://www.w3.org/TR/html4/struct/text.html#h-9.1
     * @return <code>true</code> if sequences of whitespace (space '\u0020',
     * tab '\u0009', form feed '\u000C', zero-width space '\u200B',
     * carriage-return '\r' and newline '\n') are to be replaced with a single
     * space.
     */
    public boolean getCollapse()
    {
        return (getBean().getCollapse());
    }

    /**
     * Set the current 'collapse whitespace' state.
     * @param collapse_whitespace If <code>true</code>, sequences of whitespace
     * will be reduced to a single space.
     */
    public void setCollapse(boolean collapse_whitespace)
    {
        getBean().setCollapse(collapse_whitespace);
    }

    /**
     * Getter for property Connection.
     * @return Value of property Connection.
     */
    public URLConnection getConnection()
    {
        return (getBean().getConnection());
    }

    /**
     * Setter for property Connection.
     * @param url New value of property Connection.
     */
    public void setConnection(URLConnection connection)
    {
        getBean().setConnection(connection);
    }

    //
    // PropertyChangeListener inteface
    //

    /**
     * Responds to changes in the underlying bean's properties.
     * @param event The event triggering this listener method call.
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getPropertyName().equals(StringBean.PROP_STRINGS_PROPERTY))
        {
            setText(getBean().getStrings());
            setCaretPosition(0);
        }
    }
}
