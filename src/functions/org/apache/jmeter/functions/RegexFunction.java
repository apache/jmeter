// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.functions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Util;

public class RegexFunction extends AbstractFunction implements Serializable
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    public static final String ALL = "ALL";
    public static final String RAND = "RAND";
    public static final String KEY = "__regexFunction";

    private Object[] values;//Parameters are stored here
    
    private static Random rand = new Random();
    private static final List desc = new LinkedList();

    private static PatternCacheLRU patternCache =
        new PatternCacheLRU(1000, new Perl5Compiler());
    private Pattern templatePattern;// initialised to the regex \$(\d+)\$

    private static ThreadLocal localMatcher = new ThreadLocal()
    {
        protected Object initialValue()
        {
            return new Perl5Matcher();
        }
    };

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 2;
    private static final int MAX_PARAMETER_COUNT = 6;
    static {
        desc.add(JMeterUtils.getResString("regexfunc_param_1"));// regex
        desc.add(JMeterUtils.getResString("regexfunc_param_2"));// template
        desc.add(JMeterUtils.getResString("regexfunc_param_3"));// which match
        desc.add(JMeterUtils.getResString("regexfunc_param_4"));// between text
        desc.add(JMeterUtils.getResString("regexfunc_param_5"));// default text
        desc.add(JMeterUtils.getResString("function_name_param"));
    }

    public RegexFunction()
    {
        templatePattern = patternCache.getPattern("\\$(\\d+)\\$", Perl5Compiler.READ_ONLY_MASK);
    }

    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
        throws InvalidVariableException
    {
    	String valueIndex="", defaultValue="", between="";
    	String name="";
    	Pattern searchPattern;
    	Object [] tmplt;
        try
        {
            searchPattern =
                patternCache.getPattern(((CompoundVariable) values[0]).execute(), Perl5Compiler.READ_ONLY_MASK);
            tmplt = generateTemplate(((CompoundVariable) values[1]).execute());

            if (values.length > 2)
            {
                valueIndex = ((CompoundVariable) values[2]).execute();
            }
            if (valueIndex.equals(""))
            {
                valueIndex = "1";
            }

            if (values.length > 3)
            {
                between = ((CompoundVariable) values[3]).execute();
            }

            if (values.length > 4)
            {
                String dv = ((CompoundVariable) values[4]).execute();
                if (!dv.equals(""))
                {
                    defaultValue = dv;
                }
            }

            if (values.length > 5)
            {
                name = ((CompoundVariable) values[values.length - 1]).execute();
            }
        }
        catch (Exception e)
        {
            throw new InvalidVariableException(e.toString());
        }

        JMeterVariables vars = getVariables();//Relatively expensive operation, so do it once
        vars.put(name, defaultValue);
        if (previousResult == null || previousResult.getResponseData() == null)
        {
            return defaultValue;
        }

        List collectAllMatches = new ArrayList();
        try
        {
            PatternMatcher matcher = (PatternMatcher) localMatcher.get();
            String responseText = new String(previousResult.getResponseData());
            PatternMatcherInput input = new PatternMatcherInput(responseText);
            while (matcher.contains(input, searchPattern))
            {
                MatchResult match = matcher.getMatch();
                collectAllMatches.add(match);
            }
        }
        catch (NumberFormatException e)
        {
            log.error("", e);
            return defaultValue;
        }
        catch (Exception e)
        {
            return defaultValue;
        }
        finally
		{
			vars.put(name+"_matchNr", ""+collectAllMatches.size());
        }

        if (collectAllMatches.size() == 0)
        {
            return defaultValue;
        }

        if (valueIndex.equals(ALL))
        {
            StringBuffer value = new StringBuffer();
            Iterator it = collectAllMatches.iterator();
            boolean first = true;
            while (it.hasNext())
            {
                if (!first)
                {
                    value.append(between);
                }
                else
                {
                    first = false;
                }
                value.append(generateResult((MatchResult) it.next(),name, tmplt, vars));
            }
            return value.toString();
        }
        else if (valueIndex.equals(RAND))
        {
            MatchResult result =
                (MatchResult) collectAllMatches.get(
                    rand.nextInt(collectAllMatches.size()));
            return generateResult(result,name, tmplt, vars);
        }
        else
        {
            try
            {
                int index = Integer.parseInt(valueIndex) - 1;
                MatchResult result = (MatchResult) collectAllMatches.get(index);
                return generateResult(result,name, tmplt, vars);
            }
            catch (NumberFormatException e)
            {
                float ratio = Float.parseFloat(valueIndex);
                MatchResult result =
                    (MatchResult) collectAllMatches.get(
                        (int) (collectAllMatches.size() * ratio + .5) - 1);
                return generateResult(result,name, tmplt, vars);
            }
            catch (IndexOutOfBoundsException e)
            {
                return defaultValue;
            }
        }

    }

    private void saveGroups(MatchResult result, String namep, JMeterVariables vars)
    {
        if (result != null)
        {
            for (int x = 0; x < result.groups(); x++)
            {
                vars.put(namep + "_g" + x, result.group(x));
            }
        }
    }

    public List getArgumentDesc()
    {
        return desc;
    }

    private String generateResult(MatchResult match, String namep, Object[] template
    		,JMeterVariables vars)
    {
        saveGroups(match, namep, vars);
        StringBuffer result = new StringBuffer();
        for (int a = 0; a < template.length; a++)
        {
            if (template[a] instanceof String)
            {
                result.append(template[a]);
            }
            else
            {
                result.append(match.group(((Integer) template[a]).intValue()));
            }
        }
        vars.put(namep, result.toString());
        return result.toString();
    }

    public String getReferenceKey()
    {
        return KEY;
    }

    public synchronized void setParameters(Collection parameters)
        throws InvalidVariableException
    {
        values = parameters.toArray();

        if ((values.length < MIN_PARAMETER_COUNT)
                || (values.length > MAX_PARAMETER_COUNT))
            {
                throw new InvalidVariableException(
                    "Parameter Count " //$NON-NLS-1$
                	+ values.length 
					+ " not between " //$NON-NLS-1$
                    + MIN_PARAMETER_COUNT
                    + " & " //$NON-NLS-1$
                    + MAX_PARAMETER_COUNT);
            }
    }

    private Object[] generateTemplate(String rawTemplate)
    {
        List pieces = new ArrayList();
        List combined = new LinkedList();
        PatternMatcher matcher = new Perl5Matcher();
        Util.split(pieces, new Perl5Matcher(), templatePattern, rawTemplate);
        PatternMatcherInput input = new PatternMatcherInput(rawTemplate);
        Iterator iter = pieces.iterator();
        boolean startsWith = isFirstElementGroup(rawTemplate);
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
            combined.add(new Integer(matcher.getMatch().group(1)));
        }
        return combined.toArray();
    }

    private boolean isFirstElementGroup(String rawData)
    {
        Pattern pattern = patternCache.getPattern("^\\$\\d+\\$", Perl5Compiler.READ_ONLY_MASK);
        return new Perl5Matcher().contains(rawData, pattern);
    }

/**/
    public static class Test extends JMeterTestCase
    {
        RegexFunction variable;
        SampleResult result;
        Collection params;
        private JMeterVariables vars;
        private JMeterContext jmctx = null;

        public Test(String name)
        {
            super(name);
        }

        public void setUp()
        {
            variable = new RegexFunction();
            result = new SampleResult();
        	jmctx = JMeterContextService.getContext();
           String data =
                "<company-xmlext-query-ret><row>"
                    + "<value field=\"RetCode\">"
                    + "LIS_OK</value><value"
                    + " field=\"RetCodeExtension\"></value>"
                    + "<value field=\"alias\"></value><value"
                    + " field=\"positioncount\"></value>"
                    + "<value field=\"invalidpincount\">0</value><value"
                    + " field=\"pinposition1\">1</value><value"
                    + " field=\"pinpositionvalue1\"></value><value"
                    + " field=\"pinposition2\">5</value><value"
                    + " field=\"pinpositionvalue2\"></value><value"
                    + " field=\"pinposition3\">6</value><value"
                    + " field=\"pinpositionvalue3\"></value>"
                    + "</row></company-xmlext-query-ret>";
            result.setResponseData(data.getBytes());
            vars = new JMeterVariables();
            jmctx.setVariables(vars);
            jmctx.setPreviousResult(result);
        }

        public void testVariableExtraction() throws Exception
        {
            params = new LinkedList();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$"));
            params.add(new CompoundVariable("2"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("5", match);
        }

        public void testVariableExtraction2() throws Exception
        {
            params = new LinkedList();
            params.add(new CompoundVariable(
                    "<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$1$"));
            params.add(new CompoundVariable("3"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("pinposition3", match);
        }

        public void testVariableExtraction5() throws Exception
        {
            params = new LinkedList();
            params.add(new CompoundVariable(
                    "<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$1$"));
            params.add(new CompoundVariable("ALL"));
            params.add(new CompoundVariable("_"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("pinposition1_pinposition2_pinposition3", match);
        }

        public void testVariableExtraction6() throws Exception
        {
            params = new LinkedList();
            params.add(new CompoundVariable(
                    "<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$"));
            params.add(new CompoundVariable("4"));
            params.add(new CompoundVariable(""));
            params.add(new CompoundVariable("default"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("default", match);
        }

        public void testComma() throws Exception
        {
            params = new LinkedList();
            params.add(new CompoundVariable(
                    "<value,? field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$1$"));
            params.add(new CompoundVariable("3"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("pinposition3", match);
        }

        public void testVariableExtraction3() throws Exception
        {
            params = new LinkedList();
            params.add(new CompoundVariable(
                    "<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("_$1$"));
            params.add(new CompoundVariable("2"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("_pinposition2", match);
        }

        public void testVariableExtraction4() throws Exception
        {
            params = new LinkedList();
            params.add(new CompoundVariable(
                    "<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$, "));
            params.add(new CompoundVariable(".333"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1, ", match);
        }

        public void testDefaultValue() throws Exception
        {
            params = new LinkedList();
            params.add(new CompoundVariable(
                    "<value,, field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$, "));
            params.add(new CompoundVariable(".333"));
            params.add(new CompoundVariable(""));
            params.add(new CompoundVariable("No Value Found"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("No Value Found", match);
        }
    }
}
