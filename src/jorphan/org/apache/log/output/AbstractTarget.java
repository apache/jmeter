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
package org.apache.log.output;

import org.apache.log.ErrorAware;
import org.apache.log.ErrorHandler;
import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.util.Closeable;
import org.apache.log.util.DefaultErrorHandler;

/**
 * Abstract target.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Peter Donald
 * @deprecated
 */
@Deprecated
public abstract class AbstractTarget
    implements LogTarget, ErrorAware, Closeable
{
    private static final ErrorHandler DEFAULT_ERROR_HANDLER = new DefaultErrorHandler();

    ///ErrorHandler used by target to delegate Error handling
    private ErrorHandler m_errorHandler = DEFAULT_ERROR_HANDLER;

    ///Flag indicating that log session is finished (aka target has been closed)
    private boolean m_isOpen;

    /**
     * AbstractTarget constructor.
     */
    public AbstractTarget()
    {
    }

    /**
     * AbstractTarget constructor.
     * @param errorHandler the error handler
     */
    public AbstractTarget( final ErrorHandler errorHandler )
    {
        if( errorHandler == null )
        {
            throw new NullPointerException( "errorHandler specified cannot be null" );
        }
        setErrorHandler( errorHandler );
    }

    /**
     * Provide component with ErrorHandler.
     *
     * @param errorHandler the errorHandler
     */
    public synchronized void setErrorHandler( final ErrorHandler errorHandler )
    {
        m_errorHandler = errorHandler;
    }

    /**
     * Return the open state of the target.
     * @return TRUE if the target is open else FALSE
     */
    protected synchronized boolean isOpen()
    {
        return m_isOpen;
    }

    /**
     * Startup log session.
     */
    protected synchronized void open()
    {
        if( !isOpen() )
        {
            m_isOpen = true;
        }
    }

    /**
     * Process a log event, via formatting and outputting it.
     *
     * @param event the log event
     */
    public synchronized void processEvent( final LogEvent event )
    {
        if( !isOpen() )
        {
            getErrorHandler().error( "Writing event to closed stream.", null, event );
            return;
        }

        try
        {
            doProcessEvent( event );
        }
        catch( final Throwable throwable )
        {
            getErrorHandler().error( "Unknown error writing event.", throwable, event );
        }
    }

    /**
     * Process a log event, via formatting and outputting it.
     * This should be overidden by subclasses.
     *
     * @param event the log event
     * @exception Exception if an event processing error occurs
     */
    protected abstract void doProcessEvent( LogEvent event )
        throws Exception;

    /**
     * Shutdown target.
     * Attempting to write to target after close() will cause errors to be logged.
     *
     */
    public synchronized void close()
    {
        if( isOpen() )
        {
            m_isOpen = false;
        }
    }

    /**
     * Helper method to retrieve ErrorHandler for subclasses.
     *
     * @return the ErrorHandler
     */
    protected final ErrorHandler getErrorHandler()
    {
        return m_errorHandler;
    }

}
