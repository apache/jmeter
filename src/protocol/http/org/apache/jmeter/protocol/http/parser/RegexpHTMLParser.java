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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

// NOTE: Also looked at using Java 1.4 regexp instead of ORO. The change was
// trivial. Performance did not improve -- at least not significantly.
// Finally decided for ORO following advise from Stefan Bodewig (message
// to jmeter-dev dated 25 Nov 2003 8:52 CET) [Jordi]
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 * HtmlParser implementation using regular expressions.
 * <p>
 * This class will find RLs specified in the following ways (where 
 * <b>url</b> represents the RL being found:
 * <ul>
 *  <li>&lt;img src=<b>url</b> ... &gt;
 *  <li>&lt;script src=<b>url</b> ... &gt;
 *  <li>&lt;applet code=<b>url</b> ... &gt;
 *  <li>&lt;input type=image src=<b>url</b> ... &gt;
 *  <li>&lt;body background=<b>url</b> ... &gt;
 *  <li>&lt;table background=<b>url</b> ... &gt;
 *  <li>&lt;td background=<b>url</b> ... &gt;
 *  <li>&lt;tr background=<b>url</b> ... &gt;
 *  <li>&lt;applet ... codebase=<b>url</b> ... &gt;
 *  <li>&lt;embed src=<b>url</b> ... &gt;
 *  <li>&lt;embed codebase=<b>url</b> ... &gt;
 *  <li>&lt;object codebase=<b>url</b> ... &gt;
 * </ul>
 *
 * <p>
 * This class will take into account the following construct:
 * <ul>
 *  <li>&lt;base href=<b>url</b>&gt;
 * </ul>
 *
 * <p>
 * But not the following:
 * <ul>
 *  <li>&lt; ... codebase=<b>url</b> ... &gt;
 * </ul>
 * 
 * <p>
 * This HtmlParser implementation 
 */
class RegexpHTMLParser extends HTMLParser
{

    /**
     * Regexp fragment matching a tag attribute's value (including
     * the equals sign and any spaces before it). Note it matches
     * unquoted values, which to my understanding, are not conformant
     * to any of the HTML specifications, but are still quite common
     * in the web and all browsers seem to understand them.
     */
    private static final String VALUE=
        "\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\"'\\s>][^\\s>]*)(?=[\\s>]))";
            // Note there's 3 capturing groups per value

    /**
     * Regexp fragment matching the separation between two tag attributes. 
     */
    private static final String SEP=
        "\\s(?:[^>]*\\s)?";

    /**
     * Regular expression used against the HTML code to find the URIs of
     * images, etc.:
     */
    private static final String REGEXP=
        "<(?:"
            + "!--.*?-->"
            + "|BASE"+SEP+"HREF"+VALUE
            + "|(?:IMG|SCRIPT|FRAME|IFRAME)"+SEP+"SRC"+VALUE
            + "|APPLET"+SEP+"CODE(?:BASE)?"+VALUE
            + "|(?:EMBED|OBJECT)"+SEP+"(?:SRC|xxxxxxCODEBASE)"+VALUE//URGENT:UNDO THIS!!
            + "|(?:BODY|TABLE|TR|TD)"+SEP+"BACKGROUND"+VALUE
            + "|INPUT(?:"+SEP+"(?:SRC"+VALUE+"|TYPE\\s*=\\s*(?:\"image\"|'image'|image(?=[\\s>])))){2,}"
            + "|LINK(?:"+SEP+"(?:HREF"+VALUE+"|REL\\s*=\\s*(?:\"stylesheet\"|'stylesheet'|stylesheet(?=[\\s>])))){2,}"
            + ")";

    // Number of capturing groups possibly containing Base HREFs:
    private static final int NUM_BASE_GROUPS= 3;

    /**
     * Compiled regular expression.
     */
    static Pattern pattern;

    /**
     * Thread-local matcher:
     */
    private static ThreadLocal localMatcher= new ThreadLocal()
    {
        protected Object initialValue()
        {
            return new Perl5Matcher();
        }
    };

    /**
     * Thread-local input:
     */
    private static ThreadLocal localInput= new ThreadLocal()
    {
        protected Object initialValue()
        {
            return new PatternMatcherInput(new char[0]);
        }
    };

    /** Used to store the Logger (used for debug and error messages). */
    transient private static Logger log;

	protected boolean isReusable()
	{
		return true;
	}

    /**
     * Make sure to compile the regular expression upon instantiation:
     */
    protected RegexpHTMLParser() {
        super();

        // Define this here to ensure it's ready to report any trouble
        // with the regexp:
        log= LoggingManager.getLoggerForClass();
        
        // Compile the regular expression:
        try
        {
            Perl5Compiler c= new Perl5Compiler();
            pattern=
                c.compile(
                    REGEXP,
                    Perl5Compiler.CASE_INSENSITIVE_MASK
                        | Perl5Compiler.SINGLELINE_MASK
                        | Perl5Compiler.READ_ONLY_MASK);
        }
        catch (MalformedPatternException mpe)
        {
            log.error(
                "Internal error compiling regular expression in ParseRegexp.");
            log.error("MalformedPatterException - " + mpe);
            throw new Error(mpe);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.parser.HtmlParser#getEmbeddedResourceURLs(byte[], java.net.URL)
     */
    public Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl, Collection urls)
    {

        Perl5Matcher matcher= (Perl5Matcher)localMatcher.get();
        PatternMatcherInput input= (PatternMatcherInput)localInput.get();
        // TODO: find a way to avoid the cost of creating a String here --
        // probably a new PatternMatcherInput working on a byte[] would do
        // better.
        input.setInput(new String(html));
        while (matcher.contains(input, pattern))
        {
            MatchResult match= matcher.getMatch();
            String s;
            if (log.isDebugEnabled())
                log.debug("match groups " + match.groups());
            // Check for a BASE HREF:
            for (int g=1; g <= NUM_BASE_GROUPS && g <= match.groups(); g++)
            {
                s= match.group(g);
                if (s != null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("new baseUrl: " + s + " - " + baseUrl.toString());
                    }
                    try
                    {
                        baseUrl= new URL(baseUrl, s);
                    }
                    catch (MalformedURLException e)
                    {
                        // Doesn't even look like a URL?
                        // Maybe it isn't: Ignore the exception.
                        if (log.isDebugEnabled())
                        {
                            log.debug(
                                "Can't build base URL from RL "
                                    + s
                                    + " in page "
                                    + baseUrl,
                                e);
                        }
                    }
                }
            }
            for (int g= NUM_BASE_GROUPS+1; g <= match.groups(); g++)
            {
                s= match.group(g);
                if (log.isDebugEnabled())
                {
                    log.debug("group " + g + " - " + match.group(g));
                }
                if (s != null)
                {
                    try
                    {
                        if (s.endsWith("/p.gif")) continue;//URGENT:UNDO THIS!
                        urls.add(new URL(baseUrl, s));
                    }
                    catch (MalformedURLException e)
                    {
                        // Doesn't even look like a URL? It may be a site
                        // error: return the string.
                        if (log.isDebugEnabled())
                        {
                            log.debug(
                                "Can't build URL from RL "
                                    + s
                                    + " in page "
                                    + baseUrl);
                        }
                        urls.add(s);
                    }
                }
            }
        }
        return urls.iterator();
    }
}
