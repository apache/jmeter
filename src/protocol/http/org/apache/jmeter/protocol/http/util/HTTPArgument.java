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

package org.apache.jmeter.protocol.http.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/*
 * 
 * @author unattributed
 * @version $Revision$ $Date$
 */
public class HTTPArgument extends Argument implements Serializable
{
	private static final Logger log = LoggingManager.getLoggerForClass();
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
            || (getValue() != null && getValue().length() > 0))
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
                name = JOrphanUtils.decode(name, "UTF-8");
                value = JOrphanUtils.decode(value, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                // UTF-8 unsupported? You must be joking!
                log.error("UTF-8 encoding not supported!");
                throw new Error(e.toString());
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
