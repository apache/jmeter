/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 W3C RuleML Taskforce
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
 * World Wide Web Consortium W3C (http://www.w3c.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "W3C" and "World Wide Web Consortium" and
 * "RuleML" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact .
 *
 * 5. Products derived from this software may not be called "W3C",
 * "RuleML", nor may "RuleML" or "W3C" appear in their name, without
 * prior written permission of W3C.
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
 * individuals on behalf of W3C.  For more information on W3C, please
 * see <http://www.w3c.org/> or
 * <http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231>.
 */

package org.htmlparser.tests;

import java.io.File;
import java.net.MalformedURLException;

import org.htmlparser.*;
import org.htmlparser.scanners.*;
import org.htmlparser.tags.*;
import org.htmlparser.util.*;


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
 * <p>
 * Author:	pete<br>
 * Version: 	0.1<br>
 * Created on:	Sep 30, 2003<br>
 * Last Modified:	4:45:28 PM<br>
 */

public class BenchmarkP
{

    /**
     * 
     */
    public BenchmarkP()
    {
        super();
    }

    public static void main(String[] args)
    {
        if (args != null & args.length > 0)
        {
            String strurl = args[0];
            boolean addLink = true;
            if (args.length == 2)
            {
                if (args[1].equals("f"))
                {
                    addLink = false;
                }
            }
            if (strurl.indexOf("http") < 0)
            {
                File input = new File(strurl);
                try
                {
                    strurl = input.toURL().toString();
                    System.out.println("file converted to URL: " + args[0]);
                }
                catch (MalformedURLException e)
                {
                    e.printStackTrace();
                }
            }
            try
            {
                Parser parser = new Parser(strurl, new DefaultParserFeedback());

                LinkScanner linkScanner =
                    new LinkScanner(LinkTag.LINK_TAG_FILTER);
                if (addLink)
                {
                    parser.addScanner(linkScanner);
                }
                parser.addScanner(
                    linkScanner.createImageScanner(ImageTag.IMAGE_TAG_FILTER));
                parser.addScanner(new BodyScanner());
                long start = System.currentTimeMillis();
                for (NodeIterator e = parser.elements(); e.hasMoreNodes();)
                {
                    Node node = e.nextNode();
                    if (node instanceof BodyTag)
                    {
                        BodyTag btag = (BodyTag) node;
                        System.out.println(
                            "body url: " + btag.getAttribute("background"));
                        for (NodeIterator ee = btag.elements();
                            ee.hasMoreNodes();
                            )
                        {
                            Node cnode = ee.nextNode();
                            if (cnode instanceof ImageTag)
                            {
                                ImageTag iTag = (ImageTag) cnode;
                                System.out.println(
                                    "image url: " + iTag.getImageURL());
                            }
                        }
                    }
                }
                System.out.println(
                    "Elapsed Time ms: " + (System.currentTimeMillis() - start));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
