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

//import java.io.UnsupportedEncodingException;
import java.util.Collection;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.StringTokenizer;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
//import org.apache.jorphan.util.JOrphanUtils;

/**
 * @version $Revision$ on $Date$
 */
public abstract class AbstractFunction implements Function
{
    
    /**
     * @see Function#execute(SampleResult, Sampler)
     */
    abstract public String execute(
        SampleResult previousResult,
        Sampler currentSampler)
        throws InvalidVariableException;

    public String execute() throws InvalidVariableException
    {
        JMeterContext context = JMeterContextService.getContext();
        SampleResult previousResult = context.getPreviousResult();
        Sampler currentSampler = context.getCurrentSampler();
        return execute(previousResult, currentSampler);
    }


    /**
     * @see Function#setParameters(Collection)
     */
    abstract public void setParameters(Collection parameters)
        throws InvalidVariableException;

    /**
     * @see Function#getReferenceKey()
     */
    abstract public String getReferenceKey();

// Not used    
//    /**
//     * Provides a convenient way to parse the given argument string into a
//     * collection of individual arguments.  Takes care of splitting the string
//     * based on commas, generates blank strings for values between adjacent
//     * commas, and decodes the string using URLDecoder.
//     * 
//     * @deprecated
//     */
//    protected Collection parseArguments(String params)
//    {
//        StringTokenizer tk = new StringTokenizer(params, ",", true);
//        List arguments = new LinkedList();
//        String previous = "";
//        while (tk.hasMoreTokens())
//        {
//            String arg = tk.nextToken();
//
//            if (arg.equals(",") && previous.equals(","))
//            {
//                arguments.add("");
//            }
//            else if (!arg.equals(","))
//            {
//                try
//                {
//                    arguments.add(JOrphanUtils.decode(arg, "UTF-8"));
//                }
//                catch (UnsupportedEncodingException e)
//                {
//                    // UTF-8 unsupported? You must be joking!
//                    throw new Error("Should not happen: "+e.toString());
//                }
//            }
//            previous = arg;
//        }
//        return arguments;
//    }

    /**
     * Provides a convenient way to parse the given argument string into a
     * collection of individual arguments.  Takes care of splitting the string
     * based on commas, generates blank strings for values between adjacent
     * commas, and decodes the string using URLDecoder.
     */
/*
    protected Collection parseArguments2(String params)
    {
        StringTokenizer tk = new StringTokenizer(params, ",", true);
        List arguments = new LinkedList();
        String previous = "";
        while (tk.hasMoreTokens())
        {
            String arg = tk.nextToken();

            if (arg.equals(",")
                && (previous.equals(",") || previous.length() == 0))
            {
                arguments.add(new CompoundVariable());
            }
            else if (!arg.equals(","))
            {
                try
                {
                    CompoundVariable compoundArg = new CompoundVariable();
                    compoundArg.setParameters(URLDecoder.decode(arg));
                    arguments.add(compoundArg);
                }
                catch (InvalidVariableException e)
                {
                }
            }
            previous = arg;
        }

        if (previous.equals(","))
        {
            arguments.add(new CompoundVariable());
        }

        return arguments;
    }
*/

    protected JMeterVariables getVariables()
    {
        return JMeterContextService.getContext().getVariables();
    }
}
