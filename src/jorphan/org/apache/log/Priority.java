/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log;

import java.io.Serializable;

/**
 * Class representing and holding constants for priority.
 *
 * @author Peter Donald
 * @deprecated Will be dropped in 3.3
 */
@Deprecated
public final class Priority
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Developer orientated messages, usually used during development of product.
     */
    public static final Priority DEBUG = new Priority( "DEBUG", 5 );

    /**
     * Useful information messages such as state changes, client connection, user login etc.
     */
    public static final Priority INFO = new Priority( "INFO", 10 );

    /**
     * A problem or conflict has occurred but it may be recoverable, then
     * again it could be the start of the system failing.
     */
    public static final Priority WARN = new Priority( "WARN", 15 );

    /**
     * A problem has occurred but it is not fatal. The system will still function.
     */
    public static final Priority ERROR = new Priority( "ERROR", 20 );

    /**
     * Something caused whole system to fail. This indicates that an administrator
     * should restart the system and try to fix the problem that caused the failure.
     */
    public static final Priority FATAL_ERROR = new Priority( "FATAL_ERROR", 25 );

    /**
     * Do not log anything.
     */
    public static final Priority NONE = new Priority( "NONE", Integer.MAX_VALUE );

    private final String m_name;
    private final int m_priority;

    /**
     * Retrieve a Priority object for the name parameter.
     *
     * @param priority the priority name
     * @return the Priority for name
     */
    public static Priority getPriorityForName( final String priority )
    {
        if( Priority.DEBUG.getName().equals( priority ) )
        {
            return Priority.DEBUG;
        }
        else if( Priority.INFO.getName().equals( priority ) )
        {
            return Priority.INFO;
        }
        else if( Priority.WARN.getName().equals( priority ) )
        {
            return Priority.WARN;
        }
        else if( Priority.ERROR.getName().equals( priority ) )
        {
            return Priority.ERROR;
        }
        else if( Priority.FATAL_ERROR.getName().equals( priority ) )
        {
            return Priority.FATAL_ERROR;
        }
        else if( Priority.NONE.getName().equals( priority ) )
        {
            return Priority.NONE;
        }
        else
        {
            return Priority.DEBUG;
        }
    }

    /**
     * Private Constructor to block instantiation outside class.
     *
     * @param name the string name of priority
     * @param priority the numerical code of priority
     */
    private Priority( final String name, final int priority )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }

        m_name = name;
        m_priority = priority;
    }

    /**
     * Overridden string to display Priority in human readable form.
     *
     * @return the string describing priority
     */
    @Override
    public String toString()
    {
        return "Priority[" + getName() + "/" + getValue() + "]";
    }

    /**
     * Get numerical value associated with priority.
     *
     * @return the numerical value
     */
    public int getValue()
    {
        return m_priority;
    }

    /**
     * Get name of priority.
     *
     * @return the priorities name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Test whether this priority is greater than other priority.
     *
     * @param other the other Priority
     * @return TRUE if the priority is greater else FALSE
     */
    public boolean isGreater( final Priority other )
    {
        return m_priority > other.getValue();
    }

    /**
     * Test whether this priority is lower than other priority.
     *
     * @param other the other Priority
     * @return TRUE if the priority is lower else FALSE
     */
    public boolean isLower( final Priority other )
    {
        return m_priority < other.getValue();
    }

    /**
     * Test whether this priority is lower or equal to other priority.
     *
     * @param other the other Priority
     * @return TRUE if the priority is lower or equal else FALSE
     */
    public boolean isLowerOrEqual( final Priority other )
    {
        return m_priority <= other.getValue();
    }

    /**
     * Helper method that replaces deserialized object with correct singleton.
     *
     * @return the singleton version of object
     */
    private Object readResolve()
    {
        return getPriorityForName( m_name );
    }
}
