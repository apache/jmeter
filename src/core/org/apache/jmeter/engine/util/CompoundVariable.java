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
 */

package org.apache.jmeter.engine.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.log.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;


/**
 * CompoundFunction.
 *
 * @author mstover
 * @version $Id$
 */
public class CompoundVariable implements Function
{
    transient private static Logger log =
        LoggingManager.getLoggerForClass();
        
    private String rawParameters;
    
    //private JMeterVariables threadVars;
    //private Map varMap = new HashMap();

    static Map functions = new HashMap();
    private boolean hasFunction, isDynamic;
    private String staticSubstitution;
    //private Perl5Util util = new Perl5Util();
    private Perl5Compiler compiler = new Perl5Compiler();
    private static final String unescapePattern = "[\\\\]([${}\\,])";
    private String permanentResults = "";
    
    LinkedList compiledComponents = new LinkedList();

    static {
        try
        {
            List classes =
                ClassFinder.findClassesThatExtend(
                    JMeterUtils.getSearchPaths(),
                    new Class[] { Function.class },
                    true);
            Iterator iter = classes.iterator();
            while (iter.hasNext())
            {
                Function tempFunc =
                    (Function) Class
                        .forName((String) iter.next())
                        .newInstance();
                functions.put(tempFunc.getReferenceKey(), tempFunc.getClass());
            }
        }
        catch (Exception err)
        {
            log.error("", err);
        }
    }


    public CompoundVariable()
    {
        super();
        isDynamic = true;
        hasFunction = false;
        staticSubstitution = "";
    }
    
    public CompoundVariable(String parameters)
    {
        this();
        try
        {
            setParameters(parameters);
        }
        catch (InvalidVariableException e)
        {
        }
    }

    public String execute()
    {
        if (isDynamic)
        {
            JMeterContext context = JMeterContextService.getContext();
            SampleResult previousResult = context.getPreviousResult();
            Sampler currentSampler = context.getCurrentSampler();
            return execute(previousResult, currentSampler);
        }
        else
        {
            return permanentResults;
        }
    }
    
    /**
     * Allows the retrieval of the original String prior to it being compiled.
     * @return String
     */
    public String getRawParameters()
    {
        return rawParameters;
    }
 
    /* (non-Javadoc)
     * @see Function#execute(SampleResult, Sampler)
     */
    public String execute(SampleResult previousResult, Sampler currentSampler)
    {
        if (compiledComponents == null || compiledComponents.size() == 0)
        {
            return "";
        }
        boolean testDynamic = false;
        StringBuffer results = new StringBuffer();
        Iterator iter = compiledComponents.iterator();
        while (iter.hasNext())
        {
            Object item = iter.next();
            log.debug("executing object: " + item);
            if (item instanceof Function)
            {
                testDynamic = true;
                try
                {
                    results.append(
                        ((Function) item).execute(
                            previousResult,
                            currentSampler));
                }
                catch (InvalidVariableException e)
                {
                }
            }
            else if (item instanceof SimpleVariable)
            {
                testDynamic = true;
                results.append(((SimpleVariable) item).toString());
            }
            else
            {
                results.append(item);
            }
        }
        if(!testDynamic)
        {
            isDynamic = false;
            permanentResults = results.toString();
        }
        return results.toString();
    }

    public CompoundVariable getFunction()
    {
        CompoundVariable func = new CompoundVariable();
        func.compiledComponents = (LinkedList) compiledComponents.clone();
        func.rawParameters = rawParameters;
        return func;
    }

    public List getArgumentDesc()
    {
        return new LinkedList();
    }

    public void clear()
    {
        hasFunction = false;
        compiledComponents.clear();
        staticSubstitution = "";
    }

    public void setParameters(String parameters)
        throws InvalidVariableException
    {
        this.rawParameters = parameters;
        if (parameters == null || parameters.length() == 0)
            return;

        compiledComponents = buildComponents(parameters);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#setParameters(Collection)
     */
    public void setParameters(Collection parameters)
        throws InvalidVariableException
    {
    }

    private LinkedList buildComponents(String parameters)
        throws InvalidVariableException
    {
        LinkedList components = new LinkedList();
        String current, pre, functionStr;
        int funcStartIndex, funcEndIndex;

        current = parameters;
        funcStartIndex = current.indexOf("${");

        while (funcStartIndex > -1)
        {
            pre = current.substring(0, funcStartIndex);
            if (!pre.equals(""))
            {
                components.addLast(unescape(pre));
            }

            funcEndIndex = findMatching("${", "}", current);
            functionStr = current.substring(funcStartIndex + 2, funcEndIndex);
            Function newFunction = null;
            try
            {
                newFunction = buildFunction(functionStr);
            }
            catch (InvalidVariableException e)
            { // Don't abandon processing if function fails
            }

            if (newFunction == null)
            {
                components.addLast(new SimpleVariable(functionStr));
            }
            else
            {
                components.addLast(newFunction);
            }

            hasFunction = true;
            current = current.substring(funcEndIndex + 1);
            funcStartIndex = current.indexOf("${");
        }

        if (!current.equals(""))
        {
            components.addLast(unescape(current));
        }

        return components;
    }

    private Function buildFunction(String functionStr)
        throws InvalidVariableException
    {
        Function returnFunction = null;
        //LinkedList parameterList;
        String functionName, params;
        int paramsStart = functionStr.indexOf("(");

        if (paramsStart > -1)
            functionName = functionStr.substring(0, functionStr.indexOf("("));
        else
            functionName = functionStr;

        if (functions.containsKey(functionName))
        {
            Object replacement = functions.get(functionName);
            params = extractParams(functionStr);

            try
            {
                returnFunction = (Function) ((Class) replacement).newInstance();
                Collection paramList = parseParams(params);
                returnFunction.setParameters(paramList);
            }
            catch (Exception e)
            {
                log.error("", e);
                throw new InvalidVariableException();
            }
        }

        return returnFunction;

    }

    private String extractParams(String functionStr)
    {
        String params;
        int startIndex, endIndex, embeddedStartIndex;

        params = "";
        startIndex = functionStr.indexOf("(");
        endIndex = findMatching("(", ")", functionStr);
        embeddedStartIndex = functionStr.indexOf("${");

        if (startIndex != -1 && endIndex != -1)
        {
            if (embeddedStartIndex == -1
                || (embeddedStartIndex != -1 &&
                    startIndex < embeddedStartIndex))
            {
                params = functionStr.substring(startIndex + 1, endIndex);
            }
        }

        return params;
    }

    private LinkedList parseParams(String params)
        throws InvalidVariableException
    {
        LinkedList uncompiled = new LinkedList();
        LinkedList compiled = new LinkedList();
        StringTokenizer st = new StringTokenizer(params, ",", true);
        StringBuffer buffer = new StringBuffer();
        String token, previous;

        previous = token = "";

        while (st.hasMoreElements())
        {
            buffer.append(st.nextElement());
            token = buffer.toString();
            boolean foundOpen = false;
            int searchIndex = -1;

            while (!foundOpen)
            {
                searchIndex = token.indexOf("(", searchIndex + 1);
                if (searchIndex == -1)
                    break;
                else if (
                    searchIndex == 0 || token.charAt(searchIndex - 1) != '\\')
                    foundOpen = true;
            }

            if (foundOpen)
            {
                if (findMatching("(", ")", token) != -1)
                {
                    uncompiled.add(token);
                    previous = token;
                    buffer = new StringBuffer();
                }
            }
            else
            {
                if (token.equals(",")
                    && (previous.equals(",") || previous.length() == 0))
                {
                    uncompiled.add("");
                }
                else if (!token.equals(","))
                {
                    uncompiled.add(token);
                }

                previous = token;
                buffer = new StringBuffer();
            }

        }

        if (token.equals(","))
        {
            uncompiled.add("");
        }

        for (int i = 0; i < uncompiled.size(); i++)
        {
            CompoundVariable c = new CompoundVariable();
            c.setParameters((String) uncompiled.get(i));
            compiled.addLast(c);
        }

        return compiled;
    }

    private static int findMatching(
        String openStr,
        String closeStr,
        String searchString)
    {
        //int count;
        int openIndex, closeIndex, previousMatch;
        boolean found = false;

        openIndex = closeIndex = previousMatch = -1;

        while (!found)
        {
            openIndex = searchString.indexOf(openStr, previousMatch + 1);
            if (openIndex == -1)
                break;
            else if (
                openIndex == 0 || searchString.charAt(openIndex - 1) != '\\')
                found = true;
            else
            {
                previousMatch = openIndex;
                openIndex = -1;
            }
        }

        if (openIndex < searchString.indexOf(closeStr))
        {
            if (openIndex != -1)
            {
                String subSearch;

                subSearch =
                    searchString.substring(
                        openIndex + 1,
                        searchString.length());
                int subMatch = findMatching(openStr, closeStr, subSearch);

                while (subMatch != -1)
                {
                    if (previousMatch == -1)
                        previousMatch = openIndex + subMatch + 1;
                    else
                        previousMatch += subMatch + 1;

                    subSearch =
                        searchString.substring(
                            previousMatch + 1,
                            searchString.length());
                    subMatch = findMatching(openStr, closeStr, subSearch);
                }

                found = false;
                while (!found)
                {
                    closeIndex =
                        searchString.indexOf(closeStr, previousMatch + 1);
                    if (closeIndex == -1)
                        break;
                    else if (searchString.charAt(closeIndex - 1) != '\\')
                        found = true;
                    else
                        previousMatch = closeIndex;
                }
            }
        }

        return closeIndex;
    }

    private String unescape(String input)
    {
        String result = input;
        try
        {
            result =
                Util.substitute(
                    new Perl5Matcher(),
                    compiler.compile(unescapePattern),
                    new Perl5Substitution("$1"),
                    input,
                    Util.SUBSTITUTE_ALL);
        }
        catch (MalformedPatternException e)
        {
        }
        return result;
    }

    public boolean hasFunction()
    {
        return hasFunction;
    }

    /**
     * @see Function#getReferenceKey()
     */
    public String getReferenceKey()
    {
        return "";
    }
       
    private JMeterVariables getVariables() //TODO: not used
    {
        return JMeterContextService.getContext().getVariables();
    }


/*    public static class Test extends TestCase
    {
        CompoundVariable function;
        SampleResult result;

        public Test(String name)
        {
            super(name);
        }

        public void setUp()
        {
            Map userDefinedVariables = new HashMap();
            userDefinedVariables.put("my_regex", ".*");
            userDefinedVariables.put("server", "jakarta.apache.org");
            function = new CompoundVariable();
            function.setUserDefinedVariables(userDefinedVariables);
            result = new SampleResult();
            result.setResponseData("<html>hello world</html>".getBytes());
        }

        public void testParseExample1() throws Exception
        {
            function.setParameters(
                "${__regexFunction(<html>\\(.*\\)</html>,$1$)}");
            function.setJMeterVariables(new JMeterVariables());
            assertEquals(1, function.compiledComponents.size());
            assertEquals(
                "org.apache.jmeter.functions.RegexFunction",
                function.compiledComponents.getFirst().getClass().getName());
            assertTrue(function.hasFunction());
//            assertTrue(!function.hasStatics());
            assertEquals(
                "hello world",
                ((Function) function.compiledComponents.getFirst()).execute(
                    result,
                    null));
            assertEquals("hello world", function.execute(result, null));
        }

        public void testParseExample2() throws Exception
        {
            function.setParameters(
                "It should say:${${__regexFunction("
                    + ArgumentEncoder.encode("<html>(.*)</html>")
                    + ",$1$)}}");
            function.setJMeterVariables(new JMeterVariables());
            assertEquals(3, function.compiledComponents.size());
            assertEquals(
                "It should say:${",
                function.compiledComponents.getFirst().toString());
            assertTrue(function.hasFunction());
//            assertTrue(!function.hasStatics());
            assertEquals(
                "hello world",
                ((Function) function.compiledComponents.get(1)).execute(
                    result,
                    null));
            assertEquals("}", function.compiledComponents.get(2).toString());
            assertEquals(
                "It should say:${hello world}",
                function.execute(result, null));
            assertEquals(
                "It should say:${<html>(.*)</html>,$1$}",
                function.execute(null, null));
        }

        public void testParseExample3() throws Exception
        {
            function.setParameters(
                "${__regexFunction(<html>\\(.*\\)</html>,$1$)}" +
                "${__regexFunction(<html>\\(.*o\\)\\(.*o\\)\\(.*\\)</html>," +
                "$1$$3$)}");
            function.setJMeterVariables(new JMeterVariables());
            assertEquals(2, function.compiledComponents.size());
            assertTrue(function.hasFunction());
//            assertTrue(!function.hasStatics());
            assertEquals(
                "hello world",
                ((Function) function.compiledComponents.get(0)).execute(
                    result,
                    null));
            assertEquals(
                "hellorld",
                ((Function) function.compiledComponents.get(1)).execute(
                    result,
                    null));
            assertEquals("hello worldhellorld", function.execute(result, null));
//            assertEquals(
//                "<html>(.*)</html>,$1$<html>(.*o)(.*o)(.*)</html>,$1$$3$",
//                function.execute(null, null));
        }

        public void testParseExample4() throws Exception
        {
            function.setParameters("${non-existing function}");
            function.setJMeterVariables(new JMeterVariables());
            assertEquals(1, function.compiledComponents.size());
            assertTrue(function.hasFunction());
//            assertTrue(!function.hasStatics());
            assertEquals(
                "${non-existing function}",
                function.execute(result, null));
            assertEquals(
                "${non-existing function}",
                function.execute(null, null));
        }

        public void testParseExample6() throws Exception
        {
            function.setParameters("${server}");
            function.setJMeterVariables(new JMeterVariables());
            assertEquals(1, function.compiledComponents.size());
//            assertTrue(!function.hasFunction());
//            assertTrue(function.hasStatics());
            assertEquals("jakarta.apache.org", function.execute(null, null));
        }

        public void testParseExample5() throws Exception
        {
            function.setParameters("");
            function.setJMeterVariables(new JMeterVariables());
            assertEquals(0, function.compiledComponents.size());
            assertTrue(!function.hasFunction());
//            assertTrue(!function.hasStatics());
        }

        public void testNestedExample1() throws Exception
        {
            function.setParameters(
                "${__regexFunction(<html>\\(\\$\\{my_regex\\}\\)</html>," +
                "$1$)}${__regexFunction(<html>\\(.*o\\)\\(.*o\\)\\(.*\\)" +
                "</html>,$1$$3$)}");
            function.setJMeterVariables(new JMeterVariables());
            assertEquals(2, function.compiledComponents.size());
            assertTrue(function.hasFunction());
//            assertTrue(function.hasStatics());
            assertEquals(
                "hello world",
                ((Function) function.compiledComponents.get(0)).execute(
                    result,
                    null));
            assertEquals(
                "hellorld",
                ((Function) function.compiledComponents.get(1)).execute(
                    result,
                    null));
            assertEquals("hello worldhellorld", function.execute(result, null));
            assertEquals(
                "<html>(.*)</html>,$1$<html>(.*o)(.*o)(.*)</html>,$1$$3$",
                function.execute(null, null));
        }

        public void testNestedExample2() throws Exception
        {
            function.setParameters(
                "${__regexFunction(<html>(\\$\\{my_regex\\})</html>,$1$)}");
            function.setJMeterVariables(new JMeterVariables());
            assertEquals(1, function.compiledComponents.size());
            assertEquals(
                "org.apache.jmeter.functions.RegexFunction",
                function.compiledComponents.getFirst().getClass().getName());
            assertTrue(function.hasFunction());
//            assertTrue(function.hasStatics());
            assertEquals(
                "hello world",
                ((Function) function.compiledComponents.getFirst()).execute(
                    result,
                    null));
            assertEquals("hello world", function.execute(result, null));
        }
    }*/

}
