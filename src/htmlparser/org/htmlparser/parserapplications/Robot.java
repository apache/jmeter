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

package org.htmlparser.parserapplications;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;
/**
 * The Robot Crawler application will crawl through urls recursively, based on a depth value.
 */
public class Robot
{
    private org.htmlparser.Parser parser;
    /**
     * Robot crawler - Provide the starting url 
     */
    public Robot(String resourceLocation)
    {
        try
        {
            parser = new Parser(resourceLocation, new DefaultParserFeedback());
            parser.registerScanners();
        }
        catch (ParserException e)
        {
            System.err.println("Error, could not create parser object");
            e.printStackTrace();
        }
    }
    /**
     * Crawl using a given crawl depth.
     * @param crawlDepth Depth of crawling
     */
    public void crawl(int crawlDepth) throws ParserException
    {
        try
        {
            crawl(parser, crawlDepth);
        }
        catch (ParserException e)
        {
            throw new ParserException(
                "HTMLParserException at crawl(" + crawlDepth + ")",
                e);
        }
    }
    /**
     * Crawl using a given parser object, and a given crawl depth.
     * @param parser Parser object
     * @param crawlDepth Depth of crawling
     */
    public void crawl(Parser parser, int crawlDepth) throws ParserException
    {
        System.out.println(" crawlDepth = " + crawlDepth);
        for (NodeIterator e = parser.elements(); e.hasMoreNodes();)
        {
            Node node = e.nextNode();
            if (node instanceof LinkTag)
            {
                LinkTag linkTag = (LinkTag) node;
                {
                    if (!linkTag.isMailLink())
                    {
                        if (linkTag.getLink().toUpperCase().indexOf("HTM")
                            != -1
                            || linkTag.getLink().toUpperCase().indexOf("COM")
                                != -1
                            || linkTag.getLink().toUpperCase().indexOf("ORG")
                                != -1)
                        {
                            if (crawlDepth > 0)
                            {
                                Parser newParser =
                                    new Parser(
                                        linkTag.getLink(),
                                        new DefaultParserFeedback());
                                newParser.registerScanners();
                                System.out.print(
                                    "Crawling to " + linkTag.getLink());
                                crawl(newParser, crawlDepth - 1);
                            }
                            else
                                System.out.println(linkTag.getLink());
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args)
    {
        System.out.println("Robot Crawler v" + Parser.getVersion());
        if (args.length < 2 || args[0].equals("-help"))
        {
            System.out.println();
            System.out.println(
                "Syntax : java -classpath htmlparser.jar org.htmlparser.parserapplications.Robot <resourceLocn/website> <depth>");
            System.out.println();
            System.out.println(
                "   <resourceLocn> the name of the file to be parsed (with complete path ");
            System.out.println(
                "                  if not in current directory)");
            System.out.println(
                "   <depth> No of links to be followed from each link");
            System.out.println("   -help This screen");
            System.out.println();
            System.out.println(
                "HTML Parser home page : http://htmlparser.sourceforge.net");
            System.out.println();
            System.out.println(
                "Example : java -classpath htmlparser.jar com.kizna.parserapplications.Robot http://www.google.com 3");
            System.out.println();
            System.out.println(
                "If you have any doubts, please join the HTMLParser mailing list (user/developer) from the HTML Parser home page instead of mailing any of the contributors directly. You will be surprised with the quality of open source support. ");
            System.exit(-1);
        }
        String resourceLocation = "";
        int crawlDepth = 1;
        if (args.length != 0)
            resourceLocation = args[0];
        if (args.length == 2)
            crawlDepth = Integer.valueOf(args[1]).intValue();

        Robot robot = new Robot(resourceLocation);
        System.out.println("Crawling Site " + resourceLocation);
        try
        {
            robot.crawl(crawlDepth);
        }
        catch (ParserException e)
        {
            e.printStackTrace();
        }
    }
}
