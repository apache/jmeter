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

package org.htmlparser.tests.utilTests;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class BeanTest extends TestCase
{
    public BeanTest(String name)
    {
        super(name);
    }

    protected byte[] pickle(Object object) throws IOException
    {
        ByteArrayOutputStream bos;
        ObjectOutputStream oos;
        byte[] ret;

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.close();
        ret = bos.toByteArray();

        return (ret);
    }

    protected Object unpickle(byte[] data)
        throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bis;
        ObjectInputStream ois;
        Object ret;

        bis = new ByteArrayInputStream(data);
        ois = new ObjectInputStream(bis);
        ret = ois.readObject();
        ois.close();

        return (ret);
    }

    public void testZeroArgConstructor()
        throws IOException, ClassNotFoundException, ParserException
    {
        Parser parser;
        byte[] data;

        parser = new Parser();
        data = pickle(parser);
        parser = (Parser) unpickle(data);
    }

    public void testSerializable()
        throws IOException, ClassNotFoundException, ParserException
    {
        Parser parser;
        Vector vector;
        NodeIterator enumeration;
        byte[] data;

        parser =
            new Parser("http://htmlparser.sourceforge.net/test/example.html");
        enumeration = parser.elements();
        vector = new Vector(50);
        while (enumeration.hasMoreNodes())
            vector.addElement(enumeration.nextNode());

        data = pickle(parser);
        parser = (Parser) unpickle(data);

        enumeration = parser.elements();
        while (enumeration.hasMoreNodes())
            assertEquals(
                "Nodes before and after serialization differ",
                ((Node) vector.remove(0)).toHtml(),
                ((Node) enumeration.nextNode()).toHtml());
    }

    public void testSerializableScanners()
        throws IOException, ClassNotFoundException, ParserException
    {
        Parser parser;
        Vector vector;
        NodeIterator enumeration;
        byte[] data;

        parser =
            new Parser("http://htmlparser.sourceforge.net/test/example.html");
        parser.registerScanners();
        enumeration = parser.elements();
        vector = new Vector(50);
        while (enumeration.hasMoreNodes())
            vector.addElement(enumeration.nextNode());

        data = pickle(parser);
        parser = (Parser) unpickle(data);

        enumeration = parser.elements();
        while (enumeration.hasMoreNodes())
            assertEquals(
                "Nodes before and after serialization differ",
                ((Node) vector.remove(0)).toHtml(),
                ((Node) enumeration.nextNode()).toHtml());
    }

    public void testSerializableStringBean()
        throws IOException, ClassNotFoundException, ParserException
    {
        StringBean sb;
        String text;
        byte[] data;

        sb = new StringBean();
        sb.setURL("http://htmlparser.sourceforge.net/test/example.html");
        text = sb.getStrings();

        data = pickle(sb);
        sb = (StringBean) unpickle(data);

        assertEquals(
            "Strings before and after serialization differ",
            text,
            sb.getStrings());
    }

    public void testSerializableLinkBean()
        throws IOException, ClassNotFoundException, ParserException
    {
        LinkBean lb;
        URL[] links;
        byte[] data;
        URL[] links2;

        lb = new LinkBean();
        lb.setURL("http://htmlparser.sourceforge.net/test/example.html");
        links = lb.getLinks();

        data = pickle(lb);
        lb = (LinkBean) unpickle(data);

        links2 = lb.getLinks();
        assertEquals(
            "Number of links after serialization differs",
            links.length,
            links2.length);
        for (int i = 0; i < links.length; i++)
        {
            assertEquals(
                "Links before and after serialization differ",
                links[i],
                links2[i]);
        }
    }

    public void testStringBeanListener()
    {
        final StringBean sb;
        final Boolean hit[] = new Boolean[1];

        sb = new StringBean();
        hit[0] = Boolean.FALSE;
        sb.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getSource().equals(sb))
                    if (event
                        .getPropertyName()
                        .equals(StringBean.PROP_STRINGS_PROPERTY))
                        hit[0] = Boolean.TRUE;
            }
        });

        hit[0] = Boolean.FALSE;
        sb.setURL("http://htmlparser.sourceforge.net/test/example.html");
        assertTrue(
            "Strings property change not fired for URL change",
            hit[0].booleanValue());

        hit[0] = Boolean.FALSE;
        sb.setLinks(true);
        assertTrue(
            "Strings property change not fired for links change",
            hit[0].booleanValue());
    }

    public void testLinkBeanListener()
    {
        final LinkBean lb;
        final Boolean hit[] = new Boolean[1];

        lb = new LinkBean();
        hit[0] = Boolean.FALSE;
        lb.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getSource().equals(lb))
                    if (event
                        .getPropertyName()
                        .equals(LinkBean.PROP_LINKS_PROPERTY))
                        hit[0] = Boolean.TRUE;
            }
        });

        hit[0] = Boolean.FALSE;
        lb.setURL("http://htmlparser.sourceforge.net/test/example.html");
        assertTrue(
            "Links property change not fired for URL change",
            hit[0].booleanValue());
    }
}
