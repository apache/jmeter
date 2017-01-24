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
package org.apache.log.format;

import org.apache.log.LogEvent;

/**
 * Basic formatter that just returns raw
 * message string and ignores remainder of LogEvent.
 *
 * @author Peter Donald
 * @deprecated
 */
@Deprecated
public class RawFormatter
    implements Formatter
{
    /**
     * Format log event into string.
     *
     * @param event the event
     * @return the formatted string
     */
    public String format( final LogEvent event )
    {
        final String message = event.getMessage();
        if( null == message )
        {
            return "";
        }
        else
        {
            return message;
        }
    }
}
