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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.StringProperty;
//NOTUSED import org.apache.jorphan.logging.LoggingManager;
//NOTUSED import org.apache.log.Logger;

/**
 * @version   $Revision$
 */
public class RunTime extends GenericController implements Serializable
{
    //NOTUSED private static Logger log = LoggingManager.getLoggerForClass();

    private final static String SECONDS = "RunTime.seconds";
    private long startTime = 0;

    public RunTime()
    {
    }

    public void setRuntime(long seconds)
    {
        setProperty(new LongProperty(SECONDS, seconds));
    }

    public void setRuntime(String seconds)
    {
        setProperty(new StringProperty(SECONDS, seconds));
    }

    public long getRuntime()
    {
        try
        {
            return Long.parseLong(getPropertyAsString(SECONDS));
        }
        catch (NumberFormatException e)
        {
            return 0L;
        }
    }

    public String getRuntimeString()
    {
        return getPropertyAsString(SECONDS);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.Controller#isDone()
     */
    public boolean isDone()
    {
        if (System.currentTimeMillis()-startTime < 1000*getRuntime())
        {
            return super.isDone();
        }
        else
        {
            return true;
        }
    }

    private boolean endOfLoop()
    {
        return System.currentTimeMillis()-startTime >= 1000*getRuntime();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#nextIsNull()
     */
    protected Sampler nextIsNull() throws NextIsNullException
    {
        reInitialize();
        if (endOfLoop())
        {
            setDone(true);
            return null;
        }
        else
        {
            return next();
        }
    }

	protected void resetIterCount()
	{
		startTime=System.currentTimeMillis();
	}
}