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
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class URLRewritingModifier extends AbstractTestElement implements Serializable, PreProcessor
{
    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.http");
    private Pattern case1, case2, case3, case4;
    transient Perl5Compiler compiler = new Perl5Compiler();
    private final static String ARGUMENT_NAME = "argument_name";
    private final static String PATH_EXTENSION = "path_extension";
    private final static String PATH_EXTENSION_NO_EQUALS = "path_extension_no_equals";

    public void process()
    {
        Sampler sampler = JMeterContextService.getContext().getCurrentSampler();
        SampleResult responseText = JMeterContextService.getContext().getPreviousResult();
        initRegex(getArgumentName());
        String text = new String(responseText.getResponseData());
        Perl5Matcher matcher = JMeterUtils.getMatcher();
        String value = "";
        if (matcher.contains(text, case1))
        {
            MatchResult result = matcher.getMatch();
            value = result.group(1);
        }
        else if (matcher.contains(text, case2))
        {
            MatchResult result = matcher.getMatch();
            value = result.group(1);
        }
        else if (matcher.contains(text, case3))
        {
            MatchResult result = matcher.getMatch();
            value = result.group(1);
        }
        else if (matcher.contains(text, case4))
        {
            MatchResult result = matcher.getMatch();
            value = result.group(1);
        }

        modify((HTTPSampler) sampler, value);
    }
    private void modify(HTTPSampler sampler, String value)
    {
        if (isPathExtension())
        {
            if (isPathExtensionNoEquals())
            {
                sampler.setPath(sampler.getPath() + ";" + getArgumentName() + value);
            }
            else
            {
                sampler.setPath(sampler.getPath() + ";" + getArgumentName() + "=" + value);
            }
        }
        else
        {
            sampler.getArguments().removeArgument(getArgumentName());
            sampler.getArguments().addArgument(new HTTPArgument(getArgumentName(), value, true));
        }
    }
    public void setArgumentName(String argName)
    {
        setProperty(ARGUMENT_NAME, argName);
    }
    private void initRegex(String argName)
    {
        case1 = JMeterUtils.getPatternCache().getPattern(argName + "=([^\"'>& \n\r;]*)[& \\n\\r\"'>;]?$?", Perl5Compiler.MULTILINE_MASK);
        case2 =
            JMeterUtils.getPatternCache().getPattern(
                "[Nn][Aa][Mm][Ee]=\"" + argName + "\"[^>]+[vV][Aa][Ll][Uu][Ee]=\"([^\"]*)\"",
                Perl5Compiler.MULTILINE_MASK);
        case3 =
            JMeterUtils.getPatternCache().getPattern(
                "[vV][Aa][Ll][Uu][Ee]=\"([^\"]*)\"[^>]+[Nn][Aa][Mm][Ee]=\"" + argName + "\"",
                Perl5Compiler.MULTILINE_MASK);
        // case1 could be re-written "=?([^..."  instead of creating a new pattern?
        case4 = JMeterUtils.getPatternCache().getPattern(argName + "([^\"'>& \n\r]*)[& \\n\\r\"'>]?$?", Perl5Compiler.MULTILINE_MASK);
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
        setProperty(new BooleanProperty(PATH_EXTENSION_NO_EQUALS, pathExtNoEquals));
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
            String html = "location: http://server.com/index.html?session_id=jfdkjdkf%20jddkfdfjkdjfdf%22;";
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
            assertEquals("jfdkjdkf jddkfdfjkdjfdf\"", ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
            assertEquals("http://server.com/index.html?session_id=jfdkjdkf+jddkfdfjkdjfdf%22", sampler.toString());
        }
        public void testGrabSessionId2() throws Exception
        {
            String html = "<a href=\"http://server.com/index.html?session_id=jfdkjdkfjddkfdfjkdjfdf\">";
            response = new SampleResult();
            response.setResponseData(html.getBytes());
            URLRewritingModifier mod = new URLRewritingModifier();
            mod.setArgumentName("session_id");
            HTTPSampler sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("jfdkjdkfjddkfdfjkdjfdf", ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
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
            assertEquals("jfdkjdkfjddkfdfjkdjfdf", ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
        }

        public void testGrabSessionId4() throws Exception
        {
            String html = "href='index.html;%24sid%24KQNq3AAADQZoEQAxlkX8uQV5bjqVBPbT'";
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
            Arguments args = sampler.getArguments();
            assertEquals("index.html;%24sid%24KQNq3AAADQZoEQAxlkX8uQV5bjqVBPbT", sampler.getPath());
        }

    }
}
