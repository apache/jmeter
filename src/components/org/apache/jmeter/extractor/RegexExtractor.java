package org.apache.jmeter.extractor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Util;
/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class RegexExtractor extends AbstractTestElement implements PostProcessor, Serializable
{
    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(JMeterUtils.ELEMENTS);
    public static final String REGEX = "RegexExtractor.regex";
    public static final String REFNAME = "RegexExtractor.refname";
    public static final String MATCH_NUMBER = "RegexExtractor.match_number";
    public static final String DEFAULT = "RegexExtractor.default";
    public static final String TEMPLATE = "RegexExtractor.template";
    private Object[] template = null;

    private static PatternCacheLRU patternCache = new PatternCacheLRU(1000, new Perl5Compiler());
    private static ThreadLocal localMatcher = new ThreadLocal()
    {
        protected Object initialValue()
        {
            return new Perl5Matcher();
        }
    };
    /**
     * Parses the response data using regular expressions and saving the results
     * into variables for use later in the test.
     * @see org.apache.jmeter.processor.PostProcessor#process()
     */
    public void process()
    {
        initTemplate();
        JMeterContext context = JMeterContextService.getContext();
        if(context.getPreviousResult() == null || context.getPreviousResult().getResponseData() == null)
        {
            return;
        }
        log.debug("RegexExtractor processing result");
        context.getVariables().put(getRefName(), getDefaultValue());
        Perl5Matcher matcher = (Perl5Matcher) localMatcher.get();
        PatternMatcherInput input = new PatternMatcherInput(new String(context.getPreviousResult().getResponseData()));
        log.debug("Regex = " + getRegex());
        Pattern pattern = patternCache.getPattern(getRegex(), Perl5Compiler.READ_ONLY_MASK);
        List matches = new ArrayList();
        int x = 0;
        boolean done = false;
        do
        {
            if (matcher.contains(input, pattern))
            {
                log.debug("RegexExtractor: Match found!");
                matches.add(matcher.getMatch());
            }
            else
            {
                done = true;
            }
            x++;
        }
        while (x != getMatchNumber() && !done);        
        try
        {
            MatchResult match = getCorrectMatch(matches);
            if(match != null)
            {
                context.getVariables().put(getRefName(), generateResult(match));
                saveGroups(context.getVariables(),getRefName(),match);
            }
        }
        catch (RuntimeException e)
        {
            log.warn("Error while generating result");
        }
    }
    
    private void saveGroups(JMeterVariables vars,String basename,MatchResult match)
    {
        StringBuffer buf = new StringBuffer();
        for(int x = 0;x < match.groups();x++)
        {
            buf.append(basename);
            buf.append("_g");
            buf.append(x);
            vars.put(buf.toString(),match.group(x));
            buf.setLength(0);
        }
    }
    
    public Object clone()
    {
        RegexExtractor cloned = (RegexExtractor)super.clone();
        cloned.template = this.template;
        return cloned;
    }

    protected String generateResult(MatchResult match)
    {
        StringBuffer result = new StringBuffer();
        for (int a = 0; a < template.length; a++)
        {
            log.debug("RegexExtractor: Template piece #" + a + " = " + template[a]);
            if (template[a] instanceof String)
            {
                result.append(template[a]);
            }
            else
            {
                result.append(match.group(((Integer) template[a]).intValue()));
            }
        }
        log.debug("Regex Extractor result = " + result.toString());
        return result.toString();
    }

    private void initTemplate()
    {
        if(template != null)
        {
            return;
        }
        List pieces = new ArrayList();
        List combined = new LinkedList();
        String rawTemplate = getTemplate();
        PatternMatcher matcher = (Perl5Matcher) localMatcher.get();
        Pattern templatePattern = patternCache.getPattern("\\$(\\d+)\\$", Perl5Compiler.READ_ONLY_MASK & Perl5Compiler.SINGLELINE_MASK);
        log.debug("Pattern = " + templatePattern);
        log.debug("template = " + rawTemplate);
        Util.split(pieces, matcher, templatePattern, rawTemplate);
        PatternMatcherInput input = new PatternMatcherInput(rawTemplate);
        int count = 0;
        Iterator iter = pieces.iterator();
        boolean startsWith = isFirstElementGroup(rawTemplate);
        log.debug("template split into " + pieces.size() + " pieces, starts with = " + startsWith);
        while (iter.hasNext())
        {
            boolean matchExists = matcher.contains(input, templatePattern);
            if (startsWith)
            {
                if (matchExists)
                {
                    combined.add(new Integer(matcher.getMatch().group(1)));
                }
                combined.add(iter.next());
            }
            else
            {
                combined.add(iter.next());
                if (matchExists)
                {
                    combined.add(new Integer(matcher.getMatch().group(1)));
                }
            }
        }
        if (matcher.contains(input, templatePattern))
        {
            log.debug("Template does end with template pattern");
            combined.add(new Integer(matcher.getMatch().group(1)));
        }
        template = combined.toArray();
    }

    private boolean isFirstElementGroup(String rawData)
    {
        try
        {
            Pattern pattern = patternCache.getPattern("^\\$\\d+\\$", Perl5Compiler.READ_ONLY_MASK & Perl5Compiler.SINGLELINE_MASK);
            return ((Perl5Matcher) localMatcher.get()).contains(rawData, pattern);
        }
        catch (RuntimeException e)
        {
            log.error("", e);
            return false;
        }
    }

    /**
     * Grab the appropriate result from the list.
     * @param matches
     * @return MatchResult
     */
    protected MatchResult getCorrectMatch(List matches)
    {
        if (getMatchNumber() == matches.size() && matches.size() > 0)
        {
            return (MatchResult) matches.get(matches.size() - 1);
        }
        else if (getMatchNumber() == 0 && matches.size() > 0)
        {
            return (MatchResult) matches.get(JMeterUtils.getRandomInt(matches.size()));
        }
        else
        {
            return null;
        }
    }
    
    public void setRegex(String regex)
    {
        setProperty(REGEX, regex);
    }
    public String getRegex()
    {
        return getPropertyAsString(REGEX);
    }
    public void setRefName(String refName)
    {
        setProperty(REFNAME, refName);
    }
    public String getRefName()
    {
        return getPropertyAsString(REFNAME);
    }
    /**
     * Set which Match to use.  This can be any positive number, indicating the
     * exact match to use, or 0, which is interpreted as meaning random.
     * @param matchNumber
     */
    public void setMatchNumber(int matchNumber)
    {
        setProperty(new IntegerProperty(MATCH_NUMBER,matchNumber));
    }

    public int getMatchNumber()
    {
        return getPropertyAsInt(MATCH_NUMBER);
    }

    /**
     * Sets the value of the variable if no matches are found
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue)
    {
        setProperty(DEFAULT, defaultValue);
    }

    public String getDefaultValue()
    {
        return getPropertyAsString(DEFAULT);
    }

    public void setTemplate(String template)
    {
        setProperty(TEMPLATE, template);
    }

    public String getTemplate()
    {
        return getPropertyAsString(TEMPLATE);
    }

    public static class Test extends TestCase
    {
        RegexExtractor extractor;
        SampleResult result;
        JMeterVariables vars;

        public Test(String name)
        {
            super(name);
        }

        public void setUp()
        {
            extractor = new RegexExtractor();
            extractor.setRefName("regVal");
            result = new SampleResult();
            String data =
                "<company-xmlext-query-ret><row><value field=\"RetCode\">LIS_OK</value><value"
                    + " field=\"RetCodeExtension\"></value><value field=\"alias\"></value><value"
                    + " field=\"positioncount\"></value><value field=\"invalidpincount\">0</value><value"
                    + " field=\"pinposition1\">1</value><value"
                    + " field=\"pinpositionvalue1\"></value><value"
                    + " field=\"pinposition2\">5</value><value"
                    + " field=\"pinpositionvalue2\"></value><value"
                    + " field=\"pinposition3\">6</value><value"
                    + " field=\"pinpositionvalue3\"></value></row></company-xmlext-query-ret>";
            result.setResponseData(data.getBytes());
            vars = new JMeterVariables();
            JMeterContextService.getContext().setVariables(vars);
            JMeterContextService.getContext().setPreviousResult(result);
        }

        public void testVariableExtraction() throws Exception
        {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(2);
            extractor.process();
            assertEquals("5", vars.get("regVal"));
        }

        public void testVariableExtraction2() throws Exception
        {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(3);
            extractor.process();
            assertEquals("pinposition3", vars.get("regVal"));
        }

        public void testVariableExtraction6() throws Exception
        {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(4);
            extractor.setDefaultValue("default");
            extractor.process();
            assertEquals("default", vars.get("regVal"));
        }

        public void testVariableExtraction3() throws Exception
        {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("_$1$");
            extractor.setMatchNumber(2);
            extractor.process();
            assertEquals("_pinposition2", vars.get("regVal"));
        }
    }
}
