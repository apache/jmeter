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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Util;

/**
 * @version $Revision$
 */
public class RegexExtractor
    extends AbstractTestElement
    implements PostProcessor, Serializable
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    public static final String USEHEADERS = "RegexExtractor.useHeaders";
    public static final String REGEX = "RegexExtractor.regex";
    public static final String REFNAME = "RegexExtractor.refname";
    public static final String MATCH_NUMBER = "RegexExtractor.match_number";
    public static final String DEFAULT = "RegexExtractor.default";
    public static final String TEMPLATE = "RegexExtractor.template";
    private Object[] template = null;

    private static PatternCacheLRU patternCache =
        new PatternCacheLRU(1000, new Perl5Compiler());
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
        JMeterContext context = getThreadContext();
        if (context.getPreviousResult() == null
            || context.getPreviousResult().getResponseData() == null)
        {
            return;
        }
        log.debug("RegexExtractor processing result");

        // Fetch some variables
		JMeterVariables vars = context.getVariables();
		String refName = getRefName();
		int matchNumber = getMatchNumber();

        vars.put(refName, getDefaultValue());
        
        Perl5Matcher matcher = (Perl5Matcher) localMatcher.get();
        PatternMatcherInput input =
            new PatternMatcherInput(
            		useHeaders() ? context.getPreviousResult().getResponseHeaders()
                                 : new String(context.getPreviousResult().getResponseData())
				);
        log.debug("Regex = " + getRegex());
		try {
			Pattern pattern =
			    patternCache.getPattern(getRegex(), Perl5Compiler.READ_ONLY_MASK);
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
			while (x != matchNumber && !done);

			try
			{
			    MatchResult match;
			    if (matchNumber >= 0){// Original match behaviour
				    match = getCorrectMatch(matches, matchNumber);
				    if (match != null)
				    {
				        vars.put(refName, generateResult(match));
				        saveGroups(vars, refName, match);
				    }
				}
				else // < 0 means we save all the matches
				{
					int prevCount = 0;
					String prevString=vars.get(refName+"_matchNr");
					if (prevString != null)
                    {
                    try
                    {
                        prevCount = Integer.parseInt(prevString);
                    }
                    catch (NumberFormatException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
					}
					vars.put(refName+"_matchNr", ""+matches.size());// Save the count
					for (int i=1;i<=matches.size();i++) {
						match = getCorrectMatch(matches, i);
						if (match != null)
						{
							vars.put(refName+"_"+i, generateResult(match));
							saveGroups(vars, refName+"_"+i, match);
						}
					}
					for (int i = matches.size()+1;i<=prevCount;i++)
					{
						vars.remove(refName+"_"+i);
						vars.remove(refName+"_"+i+"_g0");// Remove known groups ...
						vars.remove(refName+"_"+i+"_g1");// ...
						//TODO remove other groups if present?
					}
				}
			}
			catch (RuntimeException e)
			{
			    log.warn("Error while generating result");
			}
		} catch (MalformedCachePatternException e) {
			log.warn("Error in pattern: "+ getRegex());
		}
    }

    private void saveGroups(
        JMeterVariables vars,
        String basename,
        MatchResult match)
    {
        StringBuffer buf = new StringBuffer();
        for (int x = 0; x < match.groups(); x++)
        {
            buf.append(basename);
            buf.append("_g");
            buf.append(x);
            vars.put(buf.toString(), match.group(x));
            buf.setLength(0);
        }
    }

    public Object clone()
    {
        RegexExtractor cloned = (RegexExtractor) super.clone();
        cloned.template = this.template;
        return cloned;
    }

    private String generateResult(MatchResult match)
    {
        StringBuffer result = new StringBuffer();
        for (int a = 0; a < template.length; a++)
        {
            log.debug(
                "RegexExtractor: Template piece #" + a + " = " + template[a]);
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
        if (template != null)
        {
            return;
        }
        List pieces = new ArrayList();
        List combined = new LinkedList();
        String rawTemplate = getTemplate();
        PatternMatcher matcher = (Perl5Matcher) localMatcher.get();
        Pattern templatePattern =
            patternCache.getPattern(
                "\\$(\\d+)\\$",
                Perl5Compiler.READ_ONLY_MASK & Perl5Compiler.SINGLELINE_MASK);
        log.debug("Pattern = " + templatePattern);
        log.debug("template = " + rawTemplate);
        Util.split(pieces, matcher, templatePattern, rawTemplate);
        PatternMatcherInput input = new PatternMatcherInput(rawTemplate);
        boolean startsWith = isFirstElementGroup(rawTemplate);
        log.debug(
            "template split into "
                + pieces.size()
                + " pieces, starts with = "
                + startsWith);
        if (startsWith){
            pieces.remove(0);// Remove initial empty entry
        }
        Iterator iter = pieces.iterator();
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
            Pattern pattern =
                patternCache.getPattern(
                    "^\\$\\d+\\$",
                    Perl5Compiler.READ_ONLY_MASK
                        & Perl5Compiler.SINGLELINE_MASK);
            return ((Perl5Matcher) localMatcher.get()).contains(
                rawData,
                pattern);
        }
        catch (RuntimeException e)
        {
            log.error("", e);
            return false;
        }
    }

    /**
     * Grab the appropriate result from the list.
     * @param matches list of matches
     * @param entry the entry number in the list
     * @return MatchResult
     */
    private MatchResult getCorrectMatch(List matches, int entry)
    {
        int matchSize = matches.size();

        if (matchSize <= 0 || entry > matchSize) return null;
        
		if (entry == 0) // Random match
		{
			return (MatchResult) matches.get(
				JMeterUtils.getRandomInt(matchSize));
		}
        
        return (MatchResult) matches.get(entry - 1);
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
        setProperty(new IntegerProperty(MATCH_NUMBER, matchNumber));
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

    private boolean useHeaders()
    {
    	return "true".equalsIgnoreCase(getPropertyAsString(USEHEADERS));
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

        private JMeterContext jmctx = null;

        public void setUp()
        {
        	jmctx = JMeterContextService.getContext();
            extractor = new RegexExtractor();
            extractor.setThreadContext(jmctx);// This would be done by the run command
            extractor.setRefName("regVal");
            result = new SampleResult();
            String data =
                "<company-xmlext-query-ret>" +
                  "<row>" +
                    "<value field=\"RetCode\">LIS_OK</value>" +
                    "<value field=\"RetCodeExtension\"></value>" +
                    "<value field=\"alias\"></value>" +
                    "<value field=\"positioncount\"></value>" +
                    "<value field=\"invalidpincount\">0</value>" +
                    "<value field=\"pinposition1\">1</value>" +
                    "<value field=\"pinpositionvalue1\"></value>" +
                    "<value field=\"pinposition2\">5</value>" +
                    "<value field=\"pinpositionvalue2\"></value>" +
                    "<value field=\"pinposition3\">6</value>" +
                    "<value field=\"pinpositionvalue3\"></value>" +
                  "</row>" +
                "</company-xmlext-query-ret>";
            result.setResponseData(data.getBytes());
            result.setResponseHeaders("Header1: Value1\nHeader2: Value2");
            vars = new JMeterVariables();
            jmctx.setVariables(vars);
            jmctx.setPreviousResult(result);
        }

        public void testVariableExtraction() throws Exception
        {
            extractor.setRegex(
                "<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(2);
            extractor.process();
            assertEquals("5", vars.get("regVal"));
			assertEquals("pinposition2", vars.get("regVal_g1"));
			assertEquals("5", vars.get("regVal_g2"));
			assertEquals("<value field=\"pinposition2\">5</value>", vars.get("regVal_g0"));
        }

        static void templateSetup(RegexExtractor rex,String tmp){
            rex.setRegex("<company-(\\w+?)-(\\w+?)-(\\w+?)>");
            rex.setMatchNumber(1);
            rex.setTemplate(tmp);
            rex.process();        	
        }
        public void testTemplate1() throws Exception
        {
        	templateSetup(extractor,"");
			assertEquals("<company-xmlext-query-ret>", vars.get("regVal_g0"));
			assertEquals("xmlext", vars.get("regVal_g1"));
			assertEquals("query", vars.get("regVal_g2"));
			assertEquals("ret", vars.get("regVal_g3"));
            assertEquals("", vars.get("regVal"));
        }

        public void testTemplate2() throws Exception
        {
        	templateSetup(extractor,"ABC");
            assertEquals("ABC", vars.get("regVal"));
        }

        public void testTemplate3() throws Exception
        {
        	templateSetup(extractor,"$2$");
            assertEquals("query", vars.get("regVal"));
        }

        public void testTemplate4() throws Exception
        {
        	templateSetup(extractor,"PRE$2$");
            assertEquals("PREquery", vars.get("regVal"));
        }

        public void testTemplate5() throws Exception
        {
        	templateSetup(extractor,"$2$POST");
            assertEquals("queryPOST", vars.get("regVal"));
        }

        public void testTemplate6() throws Exception
        {
        	templateSetup(extractor,"$2$$1$");
            assertEquals("queryxmlext", vars.get("regVal"));
        }

        public void testTemplate7() throws Exception
        {
        	templateSetup(extractor,"$2$MID$1$");
            assertEquals("queryMIDxmlext", vars.get("regVal"));
        }

        public void testVariableExtraction2() throws Exception
        {
            extractor.setRegex(
                "<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(3);
            extractor.process();
            assertEquals("pinposition3", vars.get("regVal"));
        }

        public void testVariableExtraction6() throws Exception
        {
            extractor.setRegex(
                "<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(4);
            extractor.setDefaultValue("default");
            extractor.process();
            assertEquals("default", vars.get("regVal"));
        }

        public void testVariableExtraction3() throws Exception
        {
            extractor.setRegex(
                "<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("_$1$");
            extractor.setMatchNumber(2);
            extractor.process();
            assertEquals("_pinposition2", vars.get("regVal"));
        }
		public void testVariableExtraction5() throws Exception
		{
			extractor.setRegex(
				"<value field=\"(pinposition\\d+)\">(\\d+)</value>");
			extractor.setTemplate("$1$");
			extractor.setMatchNumber(-1);
			extractor.process();
			assertEquals("3",vars.get("regVal_matchNr"));
			assertEquals("pinposition1", vars.get("regVal_1"));
			assertEquals("pinposition2", vars.get("regVal_2"));
			assertEquals("pinposition3", vars.get("regVal_3"));
			assertEquals("pinposition1", vars.get("regVal_1_g1"));
			assertEquals("1", vars.get("regVal_1_g2"));
			assertEquals("<value field=\"pinposition1\">1</value>", vars.get("regVal_1_g0"));
			assertNull(vars.get("regVal_4"));

            // Check old values don't hang around:
			extractor.setRegex("(\\w+)count"); // fewer matches
			extractor.process();
			assertEquals("2",vars.get("regVal_matchNr"));
			assertEquals("position", vars.get("regVal_1"));
			assertEquals("invalidpin", vars.get("regVal_2"));
			assertNull("Unused variables should be null",vars.get("regVal_3"));
			assertNull("Unused variables should be null",vars.get("regVal_3_g0"));
			assertNull("Unused variables should be null",vars.get("regVal_3_g1"));
		}
        public void testVariableExtraction7() throws Exception
        {
            extractor.setRegex(
                "Header1: (\\S+)");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(1);
            assertFalse("useHdrs should be false",extractor.useHeaders());
            extractor.setProperty(USEHEADERS,"true");
            assertTrue("useHdrs should be true",extractor.useHeaders());
            extractor.process();
            assertEquals("Value1", vars.get("regVal"));
        }
    }
}
