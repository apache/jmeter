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
 * @author mstover
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */

package org.apache.jmeter.protocol.http.modifier;
import java.io.Serializable;

import junit.framework.TestCase;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class URLRewritingModifier
    extends AbstractTestElement
    implements Serializable, PreProcessor
{

    private Pattern pathExtensionEqualsRegexp, parameterRegexp, pathExtensionNoEqualsRegexp;
    //transient Perl5Compiler compiler = new Perl5Compiler();
    private final static String ARGUMENT_NAME = "argument_name";
    private final static String PATH_EXTENSION = "path_extension";
    private final static String PATH_EXTENSION_NO_EQUALS =
        "path_extension_no_equals";

    public void process()
    {
        Sampler sampler = JMeterContextService.getContext().getCurrentSampler();
        SampleResult responseText =
            JMeterContextService.getContext().getPreviousResult();
        if(responseText == null)
        {
            return;
        }
        initRegex(getArgumentName());
        String text = new String(responseText.getResponseData());
        Perl5Matcher matcher = JMeterUtils.getMatcher();
        String value = "";
        if (isPathExtension() && isPathExtensionNoEquals())
        {
            if (matcher.contains(text, pathExtensionNoEqualsRegexp))
            {
                MatchResult result = matcher.getMatch();
                value = result.group(1);
            }
        }
        else if (isPathExtension()) // && ! isPathExtensionNoEquals
        {
            if (matcher.contains(text, pathExtensionEqualsRegexp))
            {
                MatchResult result = matcher.getMatch();
                value = result.group(1);
            }
        }
        else // if ! isPathExtension()
        {
            if (matcher.contains(text, parameterRegexp))
            {
                MatchResult result = matcher.getMatch();
                for (int i=1; i<result.groups(); i++)
                {
                    value = result.group(i);
                    if (value != null) break;
                }
            }
        }

        modify((HTTPSampler) sampler, value);
    }
    private void modify(HTTPSampler sampler, String value)
    {
        if (isPathExtension())
        {
            if (isPathExtensionNoEquals())
            {
                sampler.setPath(
                    sampler.getPath() + ";" + getArgumentName() + value);
            }
            else
            {
                sampler.setPath(
                    sampler.getPath() + ";" + getArgumentName() + "=" + value);
            }
        }
        else
        {
            sampler.getArguments().removeArgument(getArgumentName());
            sampler.getArguments().addArgument(
                new HTTPArgument(getArgumentName(), value, true));
        }
    }
    public void setArgumentName(String argName)
    {
        setProperty(ARGUMENT_NAME, argName);
    }
    private void initRegex(String argName)
    {
        pathExtensionEqualsRegexp =
            JMeterUtils.getPatternCache().getPattern(
                ";"+argName + "=([^\"'>&\\s;]*)[&\\s\"'>;]?$?",
                Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK);

        pathExtensionNoEqualsRegexp =
            JMeterUtils.getPatternCache().getPattern(
                ";"+argName + "([^\"'>&\\s;]*)[&\\s\"'>;]?$?",
                Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK);

        parameterRegexp =
            JMeterUtils.getPatternCache().getPattern(
                "[;\\?&]"+argName + "=([^\"'>&\\s;]*)[&\\s\"'>;]?$?"
                + "|\\s[Nn][Aa][Mm][Ee]\\s*=\\s*[\"']"
                    + argName
                    + "[\"']"
                    + "[^>]*"
                    + "\\s[vV][Aa][Ll][Uu][Ee]\\s*=\\s*[\"']"
                    + "([^\"']*)"
                    + "[\"']"
                + "|\\s[vV][Aa][Ll][Uu][Ee]\\s*=\\s*[\"']"
                    + "([^\"']*)"
                    + "[\"']"
                    + "[^>]*"
                    + "\\s[Nn][Aa][Mm][Ee]\\s*=\\s*[\"']"
                    + argName
                    + "[\"']",
                Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK);
            // NOTE: the handling of simple- vs. double-quotes could be formally
            // more accurate, but I can't imagine a session id containing
            // either, so we should be OK. The whole set of expressions is a
            // quick hack anyway, so who cares.
    }
    public String getArgumentName()
    {
        return getPropertyAsString(ARGUMENT_NAME);
    }
    public void setPathExtension(boolean pathExt)
    {
        setProperty(new BooleanProperty(PATH_EXTENSION, pathExt));
    }
    public void setPathExtensionNoEquals(boolean pathExtNoEquals)
    {
        setProperty(
            new BooleanProperty(PATH_EXTENSION_NO_EQUALS, pathExtNoEquals));
    }
    public boolean isPathExtension()
    {
        return getPropertyAsBoolean(PATH_EXTENSION);
    }
    public boolean isPathExtensionNoEquals()
    {
        return getPropertyAsBoolean(PATH_EXTENSION_NO_EQUALS);
    }
    public static class Test extends TestCase
    {
        SampleResult response;
        JMeterContext context;
        public Test(String name)
        {
            super(name);
        }
        public void setUp()
        {
            context = JMeterContextService.getContext();
        }
        public void testGrabSessionId() throws Exception
        {
            String html =
                "location: http://server.com/index.html"
                    + "?session_id=jfdkjdkf%20jddkfdfjkdjfdf%22;";
            response = new SampleResult();
            response.setResponseData(html.getBytes());
            URLRewritingModifier mod = new URLRewritingModifier();
            mod.setArgumentName("session_id");
            HTTPSampler sampler = createSampler();
            sampler.addArgument("session_id", "adfasdfdsafasdfasd");
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals(
                "jfdkjdkf jddkfdfjkdjfdf\"",
                ((Argument) args.getArguments().get(0).getObjectValue())
                    .getValue());
            assertEquals(
                "http://server.com/index.html?"
                    + "session_id=jfdkjdkf+jddkfdfjkdjfdf%22",
                sampler.toString());
        }
        public void testGrabSessionId2() throws Exception
        {
            String html =
                "<a href=\"http://server.com/index.html?"
                    + "session_id=jfdkjdkfjddkfdfjkdjfdf\">";
            response = new SampleResult();
            response.setResponseData(html.getBytes());
            URLRewritingModifier mod = new URLRewritingModifier();
            mod.setArgumentName("session_id");
            HTTPSampler sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals(
                "jfdkjdkfjddkfdfjkdjfdf",
                ((Argument) args.getArguments().get(0).getObjectValue())
                    .getValue());
        }
        private HTTPSampler createSampler()
        {
            HTTPSampler sampler = new HTTPSampler();
            sampler.setDomain("server.com");
            sampler.setPath("index.html");
            sampler.setMethod(HTTPSampler.GET);
            sampler.setProtocol("http");
            return sampler;
        }

        public void testGrabSessionId3() throws Exception
        {
            String html = "href='index.html?session_id=jfdkjdkfjddkfdfjkdjfdf'";
            response = new SampleResult();
            response.setResponseData(html.getBytes());
            URLRewritingModifier mod = new URLRewritingModifier();
            mod.setArgumentName("session_id");
            HTTPSampler sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals(
                "jfdkjdkfjddkfdfjkdjfdf",
                ((Argument) args.getArguments().get(0).getObjectValue())
                    .getValue());
        }

        public void testGrabSessionIdEndedInTab() throws Exception
        {
            String html = "href='index.html?session_id=jfdkjdkfjddkfdfjkdjfdf\t";
            response = new SampleResult();
            response.setResponseData(html.getBytes());
            URLRewritingModifier mod = new URLRewritingModifier();
            mod.setArgumentName("session_id");
            HTTPSampler sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals(
                "jfdkjdkfjddkfdfjkdjfdf",
                ((Argument) args.getArguments().get(0).getObjectValue())
                    .getValue());
        }
        
        public void testGrabSessionId4() throws Exception
        {
            String html =
                "href='index.html;%24sid%24KQNq3AAADQZoEQAxlkX8uQV5bjqVBPbT'";
            response = new SampleResult();
            response.setResponseData(html.getBytes());
            URLRewritingModifier mod = new URLRewritingModifier();
            mod.setArgumentName("%24sid%24");
            mod.setPathExtension(true);
            mod.setPathExtensionNoEquals(true);
            HTTPSampler sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            //Arguments args = sampler.getArguments();
            assertEquals(
                "index.html;%24sid%24KQNq3AAADQZoEQAxlkX8uQV5bjqVBPbT",
                sampler.getPath());
        }

        public void testGrabSessionIdFromForm() throws Exception
        {
            String[] html = new String[] {
                "<input name=\"sid\" value=\"myId\">",
                "<input name='sid' value='myId'>",
                "<input value=\"myId\" NAME='sid'>",
                "<input VALUE='myId' name=\"sid\">",
                "<input blah blah value=\"myId\" yoda yoda NAME='sid'>",
            };
            for (int i=0; i<html.length; i++)
            {
                response = new SampleResult();
                response.setResponseData(html[i].getBytes());
                URLRewritingModifier mod = new URLRewritingModifier();
                mod.setArgumentName("sid");
                mod.setPathExtension(false);
                HTTPSampler sampler = createSampler();
                context.setCurrentSampler(sampler);
                context.setPreviousResult(response);
                mod.process();
                Arguments args = sampler.getArguments();
                assertEquals(
                    "For case i="+i,
                    "myId",
                    ((Argument) args.getArguments().get(0).getObjectValue())
                        .getValue());
            }
        }
    }
}
