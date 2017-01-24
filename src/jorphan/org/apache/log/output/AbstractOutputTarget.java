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

import org.apache.log.LogEvent;
import org.apache.log.format.Formatter;

/** Abstract output target.
 *
 * Any new output target that is writing to a single connected
 * resource should extend this class directly or indirectly.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author Peter Donald
 * @deprecated
 */
@Deprecated
public abstract class AbstractOutputTarget
    extends AbstractTarget
{
    /** Formatter for target. */
    private Formatter m_formatter;

    /** Parameterless constructor.  */
    public AbstractOutputTarget()
    {
    }

    /**
     * Creation of a new abstract output target instance.
     * @param formatter the formatter to apply
     */
    public AbstractOutputTarget( final Formatter formatter )
    {
        m_formatter = formatter;
    }

    /** Returns the Formatter.
     */
    protected Formatter getFormatter()
    {
        return m_formatter;
    }
    
    /**
     * Abstract method to write data.
     *
     * @param data the data to be output
     */
    protected void write( final String data )
    {
    }

    /**
     * Process a log event.
     * @param event the event to process
     */
    protected void doProcessEvent( LogEvent event )
    {
        final String data = format( event );
        write( data );
    }

    /**
     * Startup log session.
     *
     */
    protected synchronized void open()
    {
        if( !isOpen() )
        {
            super.open();
            writeHead();
        }
    }

    /**
     * Shutdown target.
     * Attempting to write to target after close() will cause errors to be logged.
     *
     */
    public synchronized void close()
    {
        if( isOpen() )
        {
            writeTail();
            super.close();
        }
    }

    /**
     * Helper method to format an event into a string, using the formatter if available.
     *
     * @param event the LogEvent
     * @return the formatted string
     */
    private String format( final LogEvent event )
    {
        if( null != m_formatter )
        {
            return m_formatter.format( event );
        }
        else
        {
            return event.toString();
        }
    }

    /**
     * Helper method to write out log head.
     * The head initiates a session of logging.
     */
    private void writeHead()
    {
        if( !isOpen() )
        {
            return;
        }

        final String head = getHead();
        if( null != head )
        {
            write( head );
        }
    }

    /**
     * Helper method to write out log tail.
     * The tail completes a session of logging.
     */
    private void writeTail()
    {
        if( !isOpen() )
        {
            return;
        }

        final String tail = getTail();
        if( null != tail )
        {
            write( tail );
        }
    }

    /**
     * Helper method to retrieve head for log session.
     * TODO: Extract from formatter
     *
     * @return the head string
     */
    private String getHead()
    {
        return null;
    }

    /**
     * Helper method to retrieve tail for log session.
     * TODO: Extract from formatter
     *
     * @return the head string
     */
    private String getTail()
    {
        return null;
    }
}
