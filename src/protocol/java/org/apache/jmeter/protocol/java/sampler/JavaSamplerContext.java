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
package org.apache.jmeter.protocol.java.sampler;

import java.lang.Long; // Import needed for some versions of JavaDoc to
                       // properly handle @see tag below.
import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * JavaSamplerContext is used to provide context information to a
 * JavaSamplerClient implementation.  This currently consists of
 * the initialization parameters which were specified in the GUI.
 * Additional data may be accessible through JavaSamplerContext
 * in the future.
 * 
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Id$
 */
public class JavaSamplerContext
{
    /*
     * Implementation notes:
     * 
     * All of the methods in this class are currently read-only.
     * If update methods are included in the future, they should
     * be defined so that a single instance of JavaSamplerContext
     * can be associated with each thread.  Therefore, no
     * synchronization should be needed.  The same instance should
     * be used for the call to setupTest, all calls to runTest,
     * and the call to teardownTest.
     */

    /** Logging */
    private static transient Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.java");

    /**
     * Map containing the initialization parameters for the
     * JavaSamplerClient.
     */
    private Map params = null;

    /**
     * Create a new JavaSampler with the specified initialization
     * parameters.
     * 
     * @param args  the initialization parameters.
     */
    public JavaSamplerContext(Arguments args)
    {
        this.params = args.getArgumentsAsMap();
    }

    /**
     * Determine whether or not a value has been specified for the
     * parameter with this name.
     * 
     * @param name  the name of the parameter to test
     * @return      true if the parameter value has been specified,
     *               false otherwise.
     */
    public boolean containsParameter(String name)
    {
        return params.containsKey(name);
    }

    /**
     * Get an iterator of the parameter names.  Each entry in the
     * Iterator is a String.
     * 
     * @return  an Iterator of Strings listing the names of the
     *           parameters which have been specified for this
     *           test.
     */
    public Iterator getParameterNamesIterator()
    {
        return params.keySet().iterator();
    }

    /**
     * Get the value of a specific parameter as a String, or null
     * if the value was not specified.
     * 
     * @param name  the name of the parameter whose value should
     *               be retrieved
     * @return      the value of the parameter, or null if the
     *               value was not specified
     */
    public String getParameter(String name)
    {
        return getParameter(name, null);
    }

    /**
     * Get the value of a specified parameter as a String, or return
     * the specified default value if the value was not specified.
     * 
     * @param name          the name of the parameter whose value
     *                       should be retrieved
     * @param defaultValue  the default value to return if the
     *                       value of this parameter was not
     *                       specified
     * @return              the value of the parameter, or the
     *                       default value if the parameter was
     *                       not specified
     */
    public String getParameter(String name, String defaultValue)
    {
        if (params == null || !params.containsKey(name))
        {
            return defaultValue;
        }
        return (String) params.get(name);
    }

    /**
     * Get the value of a specified parameter as an integer. An
     * exception will be thrown if the parameter is not specified
     * or if it is not an integer. The value may be specified in
     * decimal, hexadecimal, or octal, as defined by
     * Integer.decode().
     * 
     * @param name          the name of the parameter whose value
     *                       should be retrieved
     * @return              the value of the parameter
     * 
     * @throws NumberFormatException if the parameter is not
     *                                specified or is not an integer
     * 
     * @see java.lang.Integer#decode(java.lang.String)
     */
    public int getIntParameter(String name) throws NumberFormatException
    {
        if (params == null || !params.containsKey(name))
        {
            throw new NumberFormatException(
                "No value for parameter named '" + name + "'.");
        }

        return Integer.decode((String) params.get(name)).intValue();
    }

    /**
     * Get the value of a specified parameter as an integer, or
     * return the specified default value if the value was not
     * specified or is not an integer.  A warning will be
     * logged if the value is not an integer.  The value may
     * be specified in decimal, hexadecimal, or octal, as defined
     * by Integer.decode().
     * 
     * @param name          the name of the parameter whose value
     *                       should be retrieved
     * @param defaultValue  the default value to return if the
     *                       value of this parameter was not
     *                       specified
     * @return              the value of the parameter, or the
     *                       default value if the parameter was
     *                       not specified
     * 
     * @see java.lang.Integer#decode(java.lang.String)
     */
    public int getIntParameter(String name, int defaultValue)
    {
        if (params == null || !params.containsKey(name))
        {
            return defaultValue;
        }

        try
        {
            return Integer.decode((String) params.get(name)).intValue();
        }
        catch (NumberFormatException e)
        {
            log.warn(
                "Value for parameter '"
                    + name
                    + "' not an integer: '"
                    + params.get(name)
                    + "'.  Using default: '"
                    + defaultValue
                    + "'.",
                e);
            return defaultValue;
        }
    }

    /**
     * Get the value of a specified parameter as a long. An
     * exception will be thrown if the parameter is not specified
     * or if it is not a long. The value may be specified in
     * decimal, hexadecimal, or octal, as defined by
     * Long.decode().
     * 
     * @param name          the name of the parameter whose value
     *                       should be retrieved
     * @return              the value of the parameter
     * 
     * @throws NumberFormatException if the parameter is not
     *                                specified or is not a long
     * 
     * @see Long#decode(String)
     */
    public long getLongParameter(String name) throws NumberFormatException
    {
        if (params == null || !params.containsKey(name))
        {
            throw new NumberFormatException(
                "No value for parameter named '" + name + "'.");
        }

        return Long.decode((String) params.get(name)).longValue();
    }

    /**
     * Get the value of a specified parameter as along, or
     * return the specified default value if the value was not
     * specified or is not a long.  A warning will be
     * logged if the value is not a long.  The value may
     * be specified in decimal, hexadecimal, or octal, as defined
     * by Long.decode().
     * 
     * @param name          the name of the parameter whose value
     *                       should be retrieved
     * @param defaultValue  the default value to return if the
     *                       value of this parameter was not
     *                       specified
     * @return              the value of the parameter, or the
     *                       default value if the parameter was
     *                       not specified
     * 
     * @see Long#decode(String)
     */
    public long getLongParameter(String name, long defaultValue)
    {
        if (params == null || !params.containsKey(name))
        {
            return defaultValue;
        }
        try
        {
            return Long.decode((String) params.get(name)).longValue();
        }
        catch (NumberFormatException e)
        {
            log.warn(
                "Value for parameter '"
                    + name
                    + "' not a long: '"
                    + params.get(name)
                    + "'.  Using default: '"
                    + defaultValue
                    + "'.",
                e);
            return defaultValue;
        }
    }
}
