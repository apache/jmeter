/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.protocol.http.parser;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.jmeter.util.JMeterUtils;

/**
 * HtmlParsers can parse HTML content to obtain URLs.
 */
public abstract class HTMLParser
{
    /** Singleton */
    static HTMLParser parser;

    /**
     * Create the single instance.
     */
    private static void initialize()
    {
        String htmlParserClassName=
            JMeterUtils.getPropDefault(
                "htmlParser.className",
                "org.apache.jmeter.protocol.http.parser.HtmlParserHTMLParser");

        try
        {
            parser=
                (HTMLParser)Class.forName(htmlParserClassName).newInstance();
        }
        catch (InstantiationException e)
        {
            throw new Error(e);
        }
        catch (IllegalAccessException e)
        {
            throw new Error(e);
        }
        catch (ClassNotFoundException e)
        {
            throw new Error(e);
        }
    }

    /**
     * Obtain the (singleton) HtmlParser. 
     * 
     * @return The single HtmlParser instance.
     */
    public static HTMLParser getParser()
    {
        if (parser == null)
            initialize();
        return parser;
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
     * 
     * @param html HTML code
     * @param url  Base URL from which the HTML code was obtained
     * @return an Iterator for the resource URLs 
     */
    public abstract Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl)
        throws HTMLParseException;

    public static class HTMLParserTest extends TestCase
    {
        public HTMLParserTest() {
            super();
        }
        public static void testParser(HTMLParser parser) throws Exception
        {
            final String[] EXPECTED_RESULT= new String[] {
                "http://myhost/mydir/images/image-a.gif",
                "http://myhost/mydir/images/image-b.gif",
                "http://myhost/mydir/images/image-c.gif",
                "http://myhost/mydir/images/image-d.gif",
                "http://myhost/mydir/images/image-e.gif",
                "http://myhost/mydir/images/image-f.gif",
                "http://myhost/mydir/images/image-a2.gif",
                "http://myhost/mydir/images/image-b2.gif",
                "http://myhost/mydir/images/image-c2.gif",
                "http://myhost/mydir/images/image-d2.gif",
                "http://myhost/mydir/images/image-e2.gif",
                "http://myhost/mydir/images/image-f2.gif",
            };
            File f= new File("testfiles/HTMLParserTestCase.html");
            byte[] buffer= new byte[(int)f.length()];
            int len= new FileInputStream(f).read(buffer);
            assertEquals(len, buffer.length);
            Iterator result=
                parser.getEmbeddedResourceURLs(
                    buffer,
                    new URL("http://myhost/mydir/myfile.html"));
            Iterator expected= Arrays.asList(EXPECTED_RESULT).iterator();
            while (expected.hasNext()) {
                assertTrue(result.hasNext());
                assertEquals(expected.next(), result.next().toString());
            }
            assertFalse(result.hasNext());
        }

        public void testDefaultParser() throws Exception {
            testParser(getParser());
        }
    }
}
