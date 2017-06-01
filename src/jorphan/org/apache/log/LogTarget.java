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
 * LogTarget is a class to encapsulate outputting LogEvent's.
 * This provides the base for all output and filter targets.
 *
 * Warning: If performance becomes a problem then this
 * interface will be rewritten as a abstract class.
 *
 * @author Peter Donald
 * @deprecated Will be dropped in 3.3 
 */
@Deprecated
public interface LogTarget
{
    /**
     * Process a log event.
     * In NO case should this method ever throw an exception/error.
     * The reason is that logging is usually added for debugging/auditing
     * purposes and it would be unacceptable to have your debugging
     * code cause more errors.
     *
     * @param event the event
     */
    void processEvent( LogEvent event );
}
