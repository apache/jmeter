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

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * This class encapsulates each individual log event.
 * LogEvents usually originate at a Logger and are routed
 * to LogTargets.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Peter Donald
 * @deprecated Will be dropped in 3.3 
 */
@Deprecated
public final class LogEvent
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    //A Constant used when retrieving time relative to start of application start
    private static final long START_TIME = System.currentTimeMillis();

    ///The category that this LogEvent concerns. (Must not be null)
    private String m_category;

    ///The message to be logged. (Must not be null)
    private String m_message;

    ///The exception that caused LogEvent if any. (May be null)
    private Throwable m_throwable;

    ///The time in millis that LogEvent occurred
    private long m_time;

    ///The priority of LogEvent. (Must not be null)
    private Priority m_priority;

    ///The context map associated with LogEvent. (May be null).
    private ContextMap m_contextMap;

    /**
     * Get Priority for LogEvent.
     *
     * @return the LogEvent Priority
     */
    public final Priority getPriority()
    {
        return m_priority;
    }

    /**
     * Set the priority of LogEvent.
     *
     * @param priority the new LogEvent priority
     */
    public final void setPriority( final Priority priority )
    {
        m_priority = priority;
    }

    /**
     * Get ContextMap associated with LogEvent
     *
     * @return the ContextMap
     */
    public final ContextMap getContextMap()
    {
        return m_contextMap;
    }

    /**
     * Set the ContextMap for this LogEvent.
     *
     * @param contextMap the context map
     */
    public final void setContextMap( final ContextMap contextMap )
    {
        m_contextMap = contextMap;
    }

    /**
     * Get the category that LogEvent relates to.
     *
     * @return the name of category
     */
    public final String getCategory()
    {
        return m_category;
    }

    /**
     * Get the message associated with event.
     *
     * @return the message
     */
    public final String getMessage()
    {
        return m_message;
    }

    /**
     * Get throwable instance associated with event.
     *
     * @return the Throwable
     */
    public final Throwable getThrowable()
    {
        return m_throwable;
    }

    /**
     * Get the absolute time of the log event.
     *
     * @return the absolute time
     */
    public final long getTime()
    {
        return m_time;
    }

    /**
     * Get the time of the log event relative to start of application.
     *
     * @return the time
     */
    public final long getRelativeTime()
    {
        return m_time - START_TIME;
    }

    /**
     * Set the LogEvent category.
     *
     * @param category the category
     */
    public final void setCategory( final String category )
    {
        m_category = category;
    }

    /**
     * Set the message for LogEvent.
     *
     * @param message the message
     */
    public final void setMessage( final String message )
    {
        m_message = message;
    }

    /**
     * Set the throwable for LogEvent.
     *
     * @param throwable the instance of Throwable
     */
    public final void setThrowable( final Throwable throwable )
    {
        m_throwable = throwable;
    }

    /**
     * Set the absolute time of LogEvent.
     *
     * @param time the time
     */
    public final void setTime( final long time )
    {
        m_time = time;
    }

    /**
     * Helper method that replaces deserialized priority with correct singleton.
     *
     * @return the singleton version of object
     * @exception ObjectStreamException if an error occurs
     */
    private Object readResolve()
        throws ObjectStreamException
    {
        if( null == m_category )
        {
            m_category = "";
        }
        if( null == m_message )
        {
            m_message = "";
        }

        String priorityName = "";
        if( null != m_priority )
        {
            priorityName = m_priority.getName();
        }

        m_priority = Priority.getPriorityForName( priorityName );

        return this;
    }
}
