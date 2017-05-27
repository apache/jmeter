/* 
 * Copyright 1999-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log;

/**
 * The object interacted with by client objects to perform logging.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Peter Donald
 * @deprecated Will be dropped in 3.3 
 */
@Deprecated
public abstract class Logger
{
    private static final Logger[] EMPTY_SET = new Logger[ 0 ];

    /**
     * Separator character use to separate different categories
     * @deprecated Will be dropped in 3.3
     */
    @Deprecated
    public static final char CATEGORY_SEPARATOR = '.';

    /**
     * Determine if messages of priority DEBUG will be logged.
     *
     * @return true if DEBUG messages will be logged
     */
    public abstract boolean isDebugEnabled();

    /**
     * Log a debug priority event.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public abstract void debug( final String message, final Throwable throwable );

    /**
     * Log a debug priority event.
     *
     * @param message the message
     */
    public abstract void debug( final String message );

    /**
     * Determine if messages of priority INFO will be logged.
     *
     * @return true if INFO messages will be logged
     */
    public abstract boolean isInfoEnabled();

    /**
     * Log a info priority event.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public abstract void info( final String message, final Throwable throwable );

    /**
     * Log a info priority event.
     *
     * @param message the message
     */
    public abstract void info( final String message );

    /**
     * Determine if messages of priority WARN will be logged.
     *
     * @return true if WARN messages will be logged
     */
    public abstract boolean isWarnEnabled();

    /**
     * Log a warn priority event.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public abstract void warn( final String message, final Throwable throwable );

    /**
     * Log a warn priority event.
     *
     * @param message the message
     */
    public abstract void warn( final String message );

    /**
     * Determine if messages of priority ERROR will be logged.
     *
     * @return true if ERROR messages will be logged
     */
    public abstract boolean isErrorEnabled();

    /**
     * Log a error priority event.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public abstract void error( final String message, final Throwable throwable );

    /**
     * Log a error priority event.
     *
     * @param message the message
     */
    public abstract void error( final String message );

    /**
     * Determine if messages of priority FATAL_ERROR will be logged.
     *
     * @return true if FATAL_ERROR messages will be logged
     */
    public abstract boolean isFatalErrorEnabled();

    /**
     * Log a fatalError priority event.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public abstract void fatalError( final String message, final Throwable throwable );

    /**
     * Log a fatalError priority event.
     *
     * @param message the message
     */
    public abstract void fatalError( final String message );

    /**
     * Make this logger additive. I.e. Send all log events to parent
     * loggers LogTargets regardless of whether or not the
     * LogTargets have been overridden.
     *
     * This is derived from Log4js notion of Additivity.
     *
     * @param additivity true to make logger additive, false otherwise
     * @deprecated Will be dropped in 3.3
     */
    @Deprecated
    public void setAdditivity( final boolean additivity )
    {
        // NOP
    }

    /**
     * Determine if messages of priority will be logged.
     * @param priority the priority
     * @return true if messages will be logged
     */
    public abstract boolean isPriorityEnabled( final Priority priority );

    /**
     * Log a event at specific priority with a certain message and throwable.
     *
     * @param priority the priority
     * @param message the message
     * @param throwable the throwable
     */
    public abstract void log( final Priority priority,
                     final String message,
                     final Throwable throwable );

    /**
     * Log a event at specific priority with a certain message.
     *
     * @param priority the priority
     * @param message the message
     */
    public abstract void log( final Priority priority, final String message );

    /**
     * Set the priority for this logger.
     *
     * @param priority the priority
     * @deprecated Will be dropped in 3.3
     */
    @Deprecated
    public void setPriority( final Priority priority )
    {
        // NOP
    }

    /**
     * Unset the priority of Logger.
     * (Thus it will use it's parent's priority or DEBUG if no parent.
     * @deprecated Will be dropped in 3.3
     */
    @Deprecated
    public void unsetPriority()
    {
        // NOP
    }

    /**
     * Unset the priority of Logger.
     * (Thus it will use it's parent's priority or DEBUG if no parent.
     * If recursive is true unset priorities of all child loggers.
     *
     * @param recursive true to unset priority of all child loggers
     * @deprecated Will be dropped in 3.3
     */
    @Deprecated
    public void unsetPriority( final boolean recursive )
    {
        // NOP
    }

    /**
     * Set the log targets for this logger.
     *
     * @param logTargets the Log Targets
     * @deprecated Will be dropped in 3.3
     */
    @Deprecated
    public void setLogTargets( final LogTarget[] logTargets )
    {
        // NOP
    }

    /**
     * Unset the logtargets for this logger.
     * This logger (and thus all child loggers who don't specify logtargets) will
     * inherit from the parents LogTargets.
     * @deprecated Will be dropped in 3.3
     */
    @Deprecated
    public void unsetLogTargets()
    {
        // NOP
    }

    /**
     * Unset the logtargets for this logger and all child loggers if recursive is set.
     * The loggers unset (and all child loggers who don't specify logtargets) will
     * inherit from the parents LogTargets.
     * @param recursive the recursion policy
     * @deprecated Will be dropped in 3.3
     */
    @Deprecated
    public void unsetLogTargets( final boolean recursive )
    {
        // NOP
    }

    /**
     * Get all the child Loggers of current logger.
     *
     * @return the child loggers
     * @deprecated Will be dropped in 3.3
     */
    @Deprecated
    public Logger[] getChildren()
    {
        // NOP
        return EMPTY_SET;
    }

    /**
     * Create a new child logger.
     * The category of child logger is [current-category].subcategory
     *
     * @param subCategory the subcategory of this logger
     * @return the new logger
     * @exception IllegalArgumentException if subCategory has an empty element name
     */
    public abstract Logger getChildLogger( final String subCategory );
}
