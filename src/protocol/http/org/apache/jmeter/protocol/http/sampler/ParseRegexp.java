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
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

// TODO: look at using Java 1.4 regexp instead of ORO.
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 * Parser class using regular expressions to scan HTML documents for images etc.
 * <p>
 * For HTML files, this class will download binary files specified in the
 * following ways (where <b>url</b> represents the binary file to be
 * downloaded):
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
 * Note that files that are duplicated within the enclosing document will
 * only be downloaded once.
 * <ul>
 *  <li>&lt;base href=<b>url</b>&gt;
 * </ul>
 *
 * But not the following:
 * <ul>
 *  <li>&lt; ... codebase=<b>url</b> ... &gt;
 * </ul>
 *
 * The following parameters are not accounted for either (as the textbooks
 * say, they are left as an exercise for the interested reader):
 * <ul>
 *  <li>&lt;area href=<b>url</b> ... &gt;
 * </ul>
 *
 * <p>
 * Finally, this class does not process <b>Style Sheets</b> either.
 *
 * @author Jordi Salvat i Alabart <jsalvata@atg.com>
 * @version $Id$
 */
public class ParseRegexp
{
    /**
     * Regular expression used against the HTML code to find the URIs of
     * images, etc.:
     */
    private static final String REGEXP=
        "<BASE(?=\\s)[^\\>]*\\sHREF\\s*=\\s*\"([^\">]*)\""
        +"|<(?:IMG|SCRIPT)(?=\\s)[^\\>]*\\sSRC\\s*=\\s*\"([^\">]*)\""
        +"|<APPLET(?=\\s)[^\\>]*\\sCODE(?:BASE)?\\s*=\\s*\"([^\">]*)\""
        +"|<(?:EMBED|OBJECT)(?=\\s)[^\\>]*\\s(?:SRC|CODEBASE)\\s*=\\s*\"([^\">]*)\""
        +"|<(?:BODY|TABLE|TR|TD)(?=\\s)[^\\>]*\\sBACKGROUND\\s*=\\s*\"([^\">]*)\""
        +"|<INPUT(?=\\s)(?:[^\\>]*\\s(?:SRC\\s*=\\s*\"([^\">]*)\"|TYPE\\s*=\\s*\"image\")){2,}"
        +"|<LINK(?=\\s)(?:[^\\>]*\\s(?:HREF\\s*=\\s*\"([^\">]*)\"|REL\\s*=\\s*\"stylesheet\")){2,}";

    /**
     * Compiled regular expression.
     */
    static Pattern pattern;

    /**
     * Thread-local matcher:
     */
    private static ThreadLocal localMatcher = new ThreadLocal()
    {
        protected Object initialValue()
        {
            return new Perl5Matcher();
        }
    };

    /**
     * Thread-local input:
     */
    private static ThreadLocal localInput = new ThreadLocal()
    {
        protected Object initialValue()
        {
            return new PatternMatcherInput(new char[0]);
        }
    };

    /** Used to store the Logger (used for debug and error messages). */
    transient private static Logger log = LoggingManager.getLoggerForClass();

    /**
     * This is a singleton class:
     */
    static {
        // Compile the regular expression:
        try {
            Perl5Compiler c= new Perl5Compiler();
            pattern= c.compile(REGEXP,
                    c.CASE_INSENSITIVE_MASK
                    |c.SINGLELINE_MASK
                    |c.READ_ONLY_MASK);
        }
        catch(MalformedPatternException mpe)
        {
            log.error("Internal error compiling regular expression in ParseRegexp.");
            log.error("MalformedPatterException - " + mpe);
            throw new Error(mpe);
        }
    }

    protected static SampleResult parseForImages(SampleResult res, HTTPSampler sampler)
    {
        URL baseUrl;

        String displayName = res.getSampleLabel();

        try
        {
            baseUrl = sampler.getUrl();
            if(log.isDebugEnabled())
            {
                log.debug("baseUrl - " + baseUrl.toString());
            }
        }
        catch(MalformedURLException mfue)
        {
            log.error("Error creating URL '" + displayName + "'");
            log.error("MalformedURLException - " + mfue);
            res.setResponseData(mfue.toString().getBytes());
            res.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
            res.setResponseMessage(HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
            res.setSuccessful(false);
            return res;
        }
        
        // This is used to ignore duplicated binary files.
        // Using a LinkedHashSet to avoid unnecessary overhead in iterating
        // the elements in the set later on. As a side-effect, this will keep
        // them roughly in order, which should be a better model of browser
        // behaviour.
        Set uniqueRLs = new LinkedHashSet();
        
        // Look for unique RLs to be sampled.
        Perl5Matcher matcher = (Perl5Matcher) localMatcher.get();
        PatternMatcherInput input = (PatternMatcherInput) localInput.get();
        // TODO: find a way to avoid the cost of creating a String here --
        // probably a new PatternMatcherInput working on a byte[] would do
        // better.
        input.setInput(new String(res.getResponseData()));
        while (matcher.contains(input, pattern)) {
            MatchResult match= matcher.getMatch();
            String s;
            if (log.isDebugEnabled()) log.debug("match groups "+match.groups());
            // Check for a BASE HREF:
            s= match.group(1);
            if (s!=null) {
                try {
                    baseUrl= new URL(baseUrl, s);
                    log.debug("new baseUrl from - "+s+" - " + baseUrl.toString());
                }
                catch(MalformedURLException mfue)
                {
                    log.error("Error creating base URL from BASE HREF '" + displayName + "'");
                    log.error("MalformedURLException - " + mfue);
                    res.setResponseData(mfue.toString().getBytes());
                    res.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
                    res.setResponseMessage(HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
                    res.setSuccessful(false);
                    return res;
                }
            }
            for (int g= 2; g < match.groups(); g++) {
                s= match.group(g);
                if (log.isDebugEnabled()) log.debug("group "+g+" - "+match.group(g));
                if (s!=null) uniqueRLs.add(s);
            }
        }

        // Iterate through the RLs and download each image:
        Iterator rls= uniqueRLs.iterator();
        while (rls.hasNext()) {
            String binUrlStr= (String)rls.next();
            SampleResult binRes = new SampleResult();
            
            // set the baseUrl and binUrl so that if error occurs
            // due to MalformedException then at least the values will be
            // visible to the user to aid correction
            binRes.setSampleLabel(baseUrl + "," + binUrlStr);

            URL binUrl;
            try {
                binUrl= new URL(baseUrl, binUrlStr);
            }
            catch(MalformedURLException mfue)
            {
                log.error("Error creating URL '" + baseUrl +
                          " , " + binUrlStr + "'");
                log.error("MalformedURLException - " + mfue);
                binRes.setResponseData(mfue.toString().getBytes());
                binRes.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
                binRes.setResponseMessage(
                    HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
                binRes.setSuccessful(false);
                res.addSubResult(binRes);
                break;
            }
            if(log.isDebugEnabled())
            {
                log.debug("Binary url - " + binUrlStr);
                log.debug("Full Binary url - " + binUrl);
            }
            binRes.setSampleLabel(binUrl.toString());
            try
            {
                HTTPSamplerFull.loadBinary(binUrl, binRes, sampler);
            }
            catch(Exception ioe)
            {
                log.error("Error reading from URL - " + ioe);
                binRes.setResponseData(ioe.toString().getBytes());
                binRes.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
                binRes.setResponseMessage(
                    HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
                binRes.setSuccessful(false);
            }
            log.debug("Adding result");
            res.addSubResult(binRes);
            res.setTime(res.getTime() + binRes.getTime());
        }

        // Okay, we're all done now
        if(log.isDebugEnabled())
        {
            log.debug("Total time - " + res.getTime());
        }
        return res;
    }
}
