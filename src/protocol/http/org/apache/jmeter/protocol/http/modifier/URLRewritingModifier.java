// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.apache.jmeter.protocol.http.modifier;
import java.io.Serializable;

import junit.framework.TestCase;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
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

/**
 * @author mstover
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Revision$ updated on $Date$
 */
public class URLRewritingModifier
    extends AbstractTestElement
    implements Serializable, PreProcessor
{

    private Pattern pathExtensionEqualsQuestionmarkRegexp;
    private Pattern pathExtensionEqualsNoQuestionmarkRegexp;
    private Pattern parameterRegexp;
    private Pattern pathExtensionNoEqualsQuestionmarkRegexp;
    private Pattern pathExtensionNoEqualsNoQuestionmarkRegexp;
    //transient Perl5Compiler compiler = new Perl5Compiler();
    private final static String ARGUMENT_NAME = "argument_name";
    private final static String PATH_EXTENSION = "path_extension";
    private final static String PATH_EXTENSION_NO_EQUALS =
        "path_extension_no_equals";
    private final static String PATH_EXTENSION_NO_QUESTIONMARK =
        "path_extension_no_questionmark";

    public void process()
    {
    	JMeterContext ctx = getThreadContext();
        Sampler sampler = ctx.getCurrentSampler();
        SampleResult responseText = ctx.getPreviousResult();
        if(responseText == null)
        {
            return;
        }
        initRegex(getArgumentName());
        String text = new String(responseText.getResponseData());
        Perl5Matcher matcher = JMeterUtils.getMatcher();
        String value = "";
        if (isPathExtension() && isPathExtensionNoEquals() && isPathExtensionNoQuestionmark())
        {
            if (matcher.contains(text, pathExtensionNoEqualsNoQuestionmarkRegexp))
            {
                MatchResult result = matcher.getMatch();
                value = result.group(1);
            }
        }
        else if (isPathExtension() && isPathExtensionNoEquals()) // && ! isPathExtensionNoQuestionmark
        {
            if (matcher.contains(text, pathExtensionNoEqualsQuestionmarkRegexp))
            {
                MatchResult result = matcher.getMatch();
                value = result.group(1);
            }
        }
        else if (isPathExtension() && isPathExtensionNoQuestionmark()) // && ! isPathExtensionNoEquals
        {
            if (matcher.contains(text, pathExtensionEqualsNoQuestionmarkRegexp))
            {
                MatchResult result = matcher.getMatch();
                value = result.group(1);
            }
        }
        else if (isPathExtension()) // && ! isPathExtensionNoEquals && ! isPathExtensionNoQuestionmark
        {
            if (matcher.contains(text, pathExtensionEqualsQuestionmarkRegexp))
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

        modify((HTTPSamplerBase) sampler, value);
    }
    private void modify(HTTPSamplerBase sampler, String value)
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
        pathExtensionEqualsQuestionmarkRegexp =
            JMeterUtils.getPatternCache().getPattern(
                ";"+argName + "=([^\"'>&\\s;]*)[&\\s\"'>;]?$?",
                Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK);

        pathExtensionEqualsNoQuestionmarkRegexp =
            JMeterUtils.getPatternCache().getPattern(
                ";"+argName + "=([^\"'>&\\s;?]*)[&\\s\"'>;?]?$?",
                Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK);

        pathExtensionNoEqualsQuestionmarkRegexp =
            JMeterUtils.getPatternCache().getPattern(
                ";"+argName + "([^\"'>&\\s;]*)[&\\s\"'>;]?$?",
                Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK);

        pathExtensionNoEqualsNoQuestionmarkRegexp =
            JMeterUtils.getPatternCache().getPattern(
                ";"+argName + "([^\"'>&\\s;?]*)[&\\s\"'>;?]?$?",
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
    public void setPathExtensionNoQuestionmark(boolean pathExtNoQuestionmark)
    {
        setProperty(
            new BooleanProperty(PATH_EXTENSION_NO_QUESTIONMARK, pathExtNoQuestionmark));
    }
    public boolean isPathExtension()
    {
        return getPropertyAsBoolean(PATH_EXTENSION);
    }
    public boolean isPathExtensionNoEquals()
    {
        return getPropertyAsBoolean(PATH_EXTENSION_NO_EQUALS);
    }
    public boolean isPathExtensionNoQuestionmark()
    {
        return getPropertyAsBoolean(PATH_EXTENSION_NO_QUESTIONMARK);
    }
    
    // TODO: add test cases for new jakarta commons http client
    public static class Test extends TestCase
    {
        private SampleResult response = null;
        
        private JMeterContext context = null;
        private URLRewritingModifier mod = null;

        public Test(String name)
        {
            super(name);
        }
        public void setUp()
        {
            context = JMeterContextService.getContext();
            mod = new URLRewritingModifier();
            mod.setThreadContext(context);
        }
        public void testGrabSessionId() throws Exception
        {
            String html =
                "location: http://server.com/index.html"
                    + "?session_id=jfdkjdkf%20jddkfdfjkdjfdf%22;";
            response = new SampleResult();
            response.setResponseData(html.getBytes());
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
                mod.setThreadContext(context);
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
