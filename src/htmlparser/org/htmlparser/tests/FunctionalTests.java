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

package org.htmlparser.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.scanners.ImageScanner;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class FunctionalTests extends TestCase
{

    public FunctionalTests(String arg0)
    {
        super(arg0);
    }

    /**
     * Based on a suspected bug report by Annette Doyle,
     * to check if the no of image tags are correctly 
     * identified by the parser
     */
    public void testNumImageTagsInYahooWithoutRegisteringScanners()
        throws ParserException
    {
        // First count the image tags as is
        int imgTagCount;
        imgTagCount = findImageTagCount();
        try
        {
            int parserImgTagCount = countImageTagsWithHTMLParser();
            assertEquals("Image Tag Count", imgTagCount, parserImgTagCount);
        }
        catch (ParserException e)
        {
            throw new ParserException(
                "Error thrown in call to countImageTagsWithHTMLParser()",
                e);
        }

    }

    public int findImageTagCount()
    {
        int imgTagCount = 0;
        try
        {
            URL url = new URL("http://www.yahoo.com");
            InputStream is = url.openStream();
            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(is));
            imgTagCount = countImageTagsWithoutHTMLParser(reader);
            is.close();
        }
        catch (MalformedURLException e)
        {
            System.err.println("URL was malformed!");
        }
        catch (IOException e)
        {
            System.err.println(
                "IO Exception occurred while trying to open stream");
        }
        return imgTagCount;
    }

    public int countImageTagsWithHTMLParser() throws ParserException
    {
        Parser parser =
            new Parser("http://www.yahoo.com", new DefaultParserFeedback());
        parser.addScanner(new ImageScanner("-i", new LinkProcessor()));
        int parserImgTagCount = 0;
        Node node;
        for (NodeIterator e = parser.elements(); e.hasMoreNodes();)
        {
            node = (Node) e.nextNode();
            if (node instanceof ImageTag)
            {
                parserImgTagCount++;
            }
        }
        return parserImgTagCount;
    }

    public int countImageTagsWithoutHTMLParser(BufferedReader reader)
        throws IOException
    {
        String line;
        int imgTagCount = 0;
        do
        {
            line = reader.readLine();
            if (line != null)
            {
                // Check the line for image tags
                String newline = line.toUpperCase();
                int fromIndex = -1;
                do
                {
                    fromIndex = newline.indexOf("<IMG", fromIndex + 1);
                    if (fromIndex != -1)
                    {
                        imgTagCount++;
                    }
                }
                while (fromIndex != -1);
            }
        }
        while (line != null);
        return imgTagCount;
    }

    public static TestSuite suite()
    {
        return new TestSuite(FunctionalTests.class);
    }
}
