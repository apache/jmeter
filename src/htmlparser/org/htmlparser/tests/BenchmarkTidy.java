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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * Title:		Apache Jakarta JMeter<br>
 * Copyright:	Copyright (c) Apache<br>
 * Company:		Apache<br>
 * License:<br>
 * <br>
 * The license is at the top!<br>
 * <br>
 * Description:<br>
 * <br>
 * This is a quick class to benchmark tidy against htmlparser.
 * It is pretty basic and uses the same process as the original
 * image parsing code in JMeter 1.9.0 and earlier.
 * <p>
 * Author:	pete<br>
 * Version: 	0.1<br>
 * Created on:	Sep 30, 2003<br>
 * Last Modified:	7:41:39 AM<br>
 */
public class BenchmarkTidy
{

    protected static String utfEncodingName;

    /**
     * 
     */
    public BenchmarkTidy(String data)
    {
        try
        {
            Document doc = (Document) getDOM(data);
            parseNodes(doc, "img", false, "src");
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
    }

    protected void parseNodes(
        Document html,
        String htmlTag,
        boolean type,
        String srcTag)
    {

        NodeList nodeList = html.getElementsByTagName(htmlTag);
        boolean uniqueBinary;

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            uniqueBinary = true;
            Node tempNode = nodeList.item(i);

            // get the url of the Binary
            NamedNodeMap nnm = tempNode.getAttributes();
            Node namedItem = null;

            if (type)
            {
                // if type is set, we need 'type=image'
                namedItem = nnm.getNamedItem("type");
                if (namedItem == null)
                {
                    break;
                }
                String inputType = namedItem.getNodeValue();

                if (inputType != null && inputType.equalsIgnoreCase("image"))
                {
                    // then we need to download the binary
                }
                else
                {
                    break;
                }
            }
            namedItem = nnm.getNamedItem(srcTag);
            System.out.println("Image Tag: " + htmlTag + " src=" + namedItem);
        }
    }

    protected static Tidy getParser()
    {
        Tidy tidy = new Tidy();
        tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);

        return tidy;
    }

    protected static Node getDOM(String text) throws SAXException
    {

        try
        {
            Node node =
                getParser().parseDOM(
                    new ByteArrayInputStream(
                        text.getBytes(getUTFEncodingName())),
                    null);

            return node;
        }
        catch (UnsupportedEncodingException e)
        {

            throw new RuntimeException("UTF-8 encoding failed - " + e);
        }
    }

    protected static String getUTFEncodingName()
    {
        if (utfEncodingName == null)
        {
            String versionNum = System.getProperty("java.version");
            if (versionNum.startsWith("1.1"))
            {
                utfEncodingName = "UTF8";
            }
            else
            {
                utfEncodingName = "UTF-8";
            }
        }
        return utfEncodingName;
    }

    public static void main(String[] args)
    {
        if (args != null && args.length > 0)
        {
            try
            {
                File input = new File(args[0]);

                StringBuffer buff = new StringBuffer();
                BufferedReader reader =
                    new BufferedReader(new FileReader(input));
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    buff.append(line);
                }
                long start = System.currentTimeMillis();
                BenchmarkTidy test = new BenchmarkTidy(buff.toString());
                System.out.println(
                    "Elapsed time ms: " + (System.currentTimeMillis() - start));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("Please provide a filename");
        }
    }
}
