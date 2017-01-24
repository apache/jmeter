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
package org.apache.log.util;

import org.apache.log.ErrorHandler;
import org.apache.log.LogEvent;

/**
 * Handle unrecoverable errors that occur during logging by
 * writing to standard error.
 *
 * @author Peter Donald
 * @deprecated
 */
@Deprecated
public class DefaultErrorHandler
    implements ErrorHandler
{
    /**
     * Log an unrecoverable error.
     *
     * @param message the error message
     * @param throwable the exception associated with error (may be null)
     * @param event the LogEvent that caused error, if any (may be null)
     */
    public void error( final String message,
                       final Throwable throwable,
                       final LogEvent event )
    {
        System.err.println( "Logging Error: " + message );
        if( null != throwable )
        {
            throwable.printStackTrace();
        }
    }
}
