/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.http.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

public class HTTPArgument extends Argument implements Serializable
{
    private static final String ALWAYS_ENCODE = "HTTPArgument.always_encode";
    private static final String USE_EQUALS = "HTTPArgument.use_equals";

    private static EncoderCache cache = new EncoderCache(1000);

    /**
     * Constructor for the Argument object.
     */
    public HTTPArgument(String name, String value, String metadata)
    {
        this(name, value, false);
        this.setMetaData(metadata);
    }
    
    public void setUseEquals(boolean ue)
    {
        if(ue)
        {
            setMetaData("=");
        }
        else
        {
            setMetaData("");
        }
        setProperty(new BooleanProperty(USE_EQUALS,ue));
    }
    
    public boolean isUseEquals()
    {
        boolean eq = getPropertyAsBoolean(USE_EQUALS);
        if (getMetaData().equals("=")
            || (getValue() != null && getValue().toString().length() > 0))
        {
            setUseEquals(true);
            return true;
        }
        return eq;
        
    }

    public void setAlwaysEncoded(boolean ae)
    {
        setProperty(new BooleanProperty(ALWAYS_ENCODE, ae));
    }

    public boolean isAlwaysEncoded()
    {
        return getPropertyAsBoolean(ALWAYS_ENCODE);
    }

    /**
     * Constructor for the Argument object.
     */
    public HTTPArgument(String name, String value)
    {
        this(name, value, false);
    }

    public HTTPArgument(String name, String value, boolean alreadyEncoded)
    {
        setAlwaysEncoded(true);
        if (alreadyEncoded)
        {
            try
            {
                name = URLDecoder.decode(name, "UTF-8");
                value = URLDecoder.decode(value.toString(), "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                // UTF-8 unsupported? You must be joking!
                log.error("UTF-8 encoding not supported!");
                throw new Error(e);
            }
        }
        setName(name);
        setValue(value);
        setMetaData("=");
    }

    public HTTPArgument(
        String name,
        String value,
        String metaData,
        boolean alreadyEncoded)
    {
        this(name, value, alreadyEncoded);
        setMetaData(metaData);
    }

    public HTTPArgument(Argument arg)
    {
        this(arg.getName(), arg.getValue(), arg.getMetaData());
    }

    /**
     * Constructor for the Argument object
     */
    public HTTPArgument()
    {}

    /**
     * Sets the Name attribute of the Argument object.
     *
     * @param newName  the new Name value
     */
    public void setName(String newName)
    {
        if (newName == null || !newName.equals(getName()))
        {
            super.setName(newName);
        }
    }

    public String getEncodedValue()
    {
        if (isAlwaysEncoded())
        {
            return cache.getEncoded(getValue());
        }
        else
        {
            return getValue();
        }
    }

    public String getEncodedName()
    {
        if (isAlwaysEncoded())
        {
            return cache.getEncoded(getName());
        }
        else
        {
            return getName();
        }

    }

    public static void convertArgumentsToHTTP(Arguments args)
    {
        List newArguments = new LinkedList();
        PropertyIterator iter = args.getArguments().iterator();
        while (iter.hasNext())
        {
            Argument arg = (Argument) iter.next().getObjectValue();
            if (!(arg instanceof HTTPArgument))
            {
                newArguments.add(new HTTPArgument(arg));
            }
            else
            {
                newArguments.add(arg);
            }
        }
        args.removeAllArguments();
        args.setArguments(newArguments);
    }

    public static class Test extends TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testCloning() throws Exception
        {
            HTTPArgument arg = new HTTPArgument("name.?", "value_ here");
            assertEquals("name.%3F", arg.getEncodedName());
            assertEquals("value_+here", arg.getEncodedValue());
            HTTPArgument clone = (HTTPArgument) arg.clone();
            assertEquals("name.%3F", clone.getEncodedName());
            assertEquals("value_+here", clone.getEncodedValue());
        }

        public void testConversion() throws Exception
        {
            Arguments args = new Arguments();
            args.addArgument("name.?", "value_ here");
            args.addArgument("name$of property", "value_.+");
            HTTPArgument.convertArgumentsToHTTP(args);
            CollectionProperty argList = args.getArguments();
            HTTPArgument httpArg =
                (HTTPArgument) argList.get(0).getObjectValue();
            assertEquals("name.%3F", httpArg.getEncodedName());
            assertEquals("value_+here", httpArg.getEncodedValue());
            httpArg = (HTTPArgument) argList.get(1).getObjectValue();
            assertEquals("name%24of+property", httpArg.getEncodedName());
            assertEquals("value_.%2B", httpArg.getEncodedValue());
        }
    }
}
