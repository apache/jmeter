/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

// Mark Walsh, 2002-08-03 add method:
//    addArgument(String name, Object value, Object metadata)
// Modify methods:
//    toString(), addEmptyArgument(), addArgument(String name, Object value)

/**
 * A set of Argument objects.
 *
 * @author    Michael Stover
 * @author    Mark Walsh
 * @version   $Revision$
 */
public class Arguments extends ConfigTestElement implements Serializable
{
    /** The name of the property used to store the arguments. */
    public static final String ARGUMENTS = "Arguments.arguments";

    /**
     * Create a new Arguments object with no arguments.
     */
    public Arguments()
    {
        setProperty(new CollectionProperty(ARGUMENTS, new ArrayList()));
    }

    /**
     * Get the arguments.
     * 
     * @return the arguments
     */
    public CollectionProperty getArguments()
    {
        return (CollectionProperty) getProperty(ARGUMENTS);
    }

    /**
     * Clear the arguments.
     */
    public void clear()
    {
        super.clear();
        setProperty(new CollectionProperty(ARGUMENTS, new ArrayList()));
    }

    /**
     * Set the list of arguments.  Any existing arguments will be lost.
     * 
     * @param arguments the new arguments
     */
    public void setArguments(List arguments)
    {
        setProperty(new CollectionProperty(ARGUMENTS, arguments));
    }

    /**
     * Get the arguments as a Map.  Each argument name is used as the key, and
     * its value as the value.
     * 
     * @return a new Map with String keys and values containing the arguments
     */
    public Map getArgumentsAsMap()
    {
        PropertyIterator iter = getArguments().iterator();
        Map argMap = new HashMap();
        while (iter.hasNext())
        {
            Argument arg = (Argument) iter.next().getObjectValue();
            argMap.put(arg.getName(), arg.getValue());
        }
        return argMap;
    }

    /**
     * Add a new argument with the given name and value.
     * 
     * @param name  the name of the argument
     * @param value the value of the argument
     */
    public void addArgument(String name, String value)
    {
        addArgument(new Argument(name, value, null));
    }

    /**
     * Add a new argument.
     * 
     * @param arg the new argument
     */
    public void addArgument(Argument arg)
    {
        TestElementProperty newArg =
            new TestElementProperty(arg.getName(), arg);
        if (isRunningVersion())
        {
            newArg.setTemporary(true, this);
        }
        getArguments().addItem(newArg);
    }

    /**
     * Add a new argument with the given name, value, and metadata.
     * 
     * @param name     the name of the argument
     * @param value    the value of the argument
     * @param metadata the metadata for the argument
     */
    public void addArgument(String name, String value, String metadata)
    {
        addArgument(new Argument(name, value, metadata));
    }

    /**
     * Get a PropertyIterator of the arguments.
     * 
     * @return an iteration of the arguments
     */
    public PropertyIterator iterator()
    {
        return getArguments().iterator();
    }

    /**
     * Create a string representation of the arguments.
     * 
     * @return the string representation of the arguments
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        PropertyIterator iter = getArguments().iterator();
        while (iter.hasNext())
        {
            Argument arg = (Argument) iter.next().getObjectValue();
            if (arg.getMetaData() == null)
            {
                str.append(arg.getName() + "=" + arg.getValue());
            }
            else
            {
                str.append(arg.getName() + arg.getMetaData() + arg.getValue());
            }
            if (iter.hasNext())
            {
                str.append("&");
            }
        }
        return str.toString();
    }

    /**
     * Remove the specified argument from the list.
     * 
     * @param row the index of the argument to remove
     */
    public void removeArgument(int row)
    {
        if (row < getArguments().size())
        {
            getArguments().remove(row);
        }
    }

    /**
     * Remove the specified argument from the list.
     * 
     * @param arg the argument to remove
     */
    public void removeArgument(Argument arg)
    {
        PropertyIterator iter = getArguments().iterator();
        while (iter.hasNext())
        {
            Argument item = (Argument) iter.next().getObjectValue();
            if (arg.equals(item))
            {
                iter.remove();
            }
        }
    }

    /**
     * Remove the argument with the specified name.
     * 
     * @param argName the name of the argument to remove
     */
    public void removeArgument(String argName)
    {
        PropertyIterator iter = getArguments().iterator();
        while (iter.hasNext())
        {
            Argument arg = (Argument) iter.next().getObjectValue();
            if (arg.getName().equals(argName))
            {
                iter.remove();
            }
        }
    }

    /**
     * Remove all arguments from the list.
     */
    public void removeAllArguments()
    {
        getArguments().clear();
    }

    /**
     * Add a new empty argument to the list.  The new argument will have the
     * empty string as its name and value, and null metadata.
     */
    public void addEmptyArgument()
    {
        addArgument(new Argument("", "", null));
    }

    /**
     * Get the number of arguments in the list.
     * 
     * @return the number of arguments
     */
    public int getArgumentCount()
    {
        return getArguments().size();
    }

    /**
     * Get a single argument.
     * 
     * @param row the index of the argument to return.
     * @return    the argument at the specified index, or null if no argument
     *            exists at that index.
     */
    public Argument getArgument(int row)
    {
        Argument argument = null;

        if (row < getArguments().size())
        {
            argument = (Argument) getArguments().get(row).getObjectValue();
        }

        return argument;
    }
}
