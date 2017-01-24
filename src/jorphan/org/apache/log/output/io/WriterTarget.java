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
package org.apache.log.output.io;

import java.io.IOException;
import java.io.Writer;
import org.apache.log.format.Formatter;
import org.apache.log.output.AbstractOutputTarget;

/**
 * This target outputs to a writer.
 *
 * @author Peter Donald
 * @deprecated
 */
@Deprecated
public class WriterTarget
    extends AbstractOutputTarget
{
    private Writer m_output;

    /**
     * Construct target with a specific writer and formatter.
     *
     * @param writer the writer
     * @param formatter the formatter
     */
    public WriterTarget( final Writer writer, final Formatter formatter )
    {
        super( formatter );

        if( null != writer )
        {
            setWriter( writer );
            open();
        }
    }

    /**
     * Set the writer.
     * Close down writer and write tail if appropriate.
     *
     * @param writer the new writer
     */
    protected synchronized void setWriter( final Writer writer )
    {
        if( null == writer )
        {
            throw new NullPointerException( "writer property must not be null" );
        }

        m_output = writer;
    }

    /**
     * Concrete implementation of output that writes out to underlying writer.
     *
     * @param data the data to output
     */
    protected void write( final String data )
    {
        try
        {
            m_output.write( data );
            m_output.flush();
        }
        catch( final IOException ioe )
        {
            getErrorHandler().error( "Caught an IOException", ioe, null );
        }
    }

    /**
     * Shutdown target.
     * Attempting to write to target after close() will cause errors to be logged.
     */
    public synchronized void close()
    {
        super.close();
        shutdownWriter();
    }

    /**
     * Shutdown Writer.
     */
    protected synchronized void shutdownWriter()
    {
        final Writer writer = m_output;
        m_output = null;

        try
        {
            if( null != writer )
            {
                writer.close();
            }
        }
        catch( final IOException ioe )
        {
            getErrorHandler().error( "Error closing Writer", ioe, null );
        }
    }
}
