// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

package org.apache.jmeter.modifiers;

import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */
public class CounterConfig
    extends AbstractTestElement
    implements Serializable, LoopIterationListener, NoThreadClone
{
    private static Logger log = LoggingManager.getLoggerForClass();
    public final static String START = "CounterConfig.start";
    public final static String END = "CounterConfig.end";
    public final static String INCREMENT = "CounterConfig.incr";
    public final static String PER_USER = "CounterConfig.per_user";
    public final static String VAR_NAME = "CounterConfig.name";

    private int globalCounter = -1;
    
    /**
     * @see LoopIterationListener#iterationStart(LoopIterationEvent)
     */
    public synchronized void iterationStart(LoopIterationEvent event)
    {
    	// Cannot use getThreadContext() as not cloned per thread
        JMeterVariables variables = 
        	JMeterContextService.getContext().getVariables();
        int start = getStart(), end = getEnd(), increment = getIncrement();
        if (!isPerUser())
        {
            if (globalCounter == -1 || globalCounter > end)
            {
                globalCounter = start;
            }
            variables.put(getVarName(), Integer.toString(globalCounter));
            globalCounter += increment;
        }
        else
        {
            String value = variables.get(getVarName());
            if (value == null)
            {
                variables.put(getVarName(), Integer.toString(start));
            }
            else
            {
                try
                {
                    int current = Integer.parseInt(value);
                    current += increment;
                    if (current > end)
                    {
                        current = start;
                    }
                    variables.put(getVarName(), Integer.toString(current));
                }
                catch (NumberFormatException e)
                {
                    log.info("Bad number in Counter config", e);
                }
            }
        }
    }

    public void setStart(int start)
    {
        setProperty(new IntegerProperty(START, start));
    }

    public void setStart(String start)
    {
        setProperty(START, start);
    }

    public int getStart()
    {
        return getPropertyAsInt(START);
    }

    public void setEnd(int end)
    {
        setProperty(new IntegerProperty(END, end));
    }

    public void setEnd(String end)
    {
        setProperty(END, end);
    }

    public int getEnd()
    {
        return getPropertyAsInt(END);
    }

    public void setIncrement(int inc)
    {
        setProperty(new IntegerProperty(INCREMENT, inc));
    }

    public void setIncrement(String incr)
    {
        setProperty(INCREMENT, incr);
    }

    public int getIncrement()
    {
        return getPropertyAsInt(INCREMENT);
    }

    public void setIsPerUser(boolean isPer)
    {
        setProperty(new BooleanProperty(PER_USER, isPer));
    }

    public boolean isPerUser()
    {
        return getPropertyAsBoolean(PER_USER);
    }

    public void setVarName(String name)
    {
        setProperty(VAR_NAME, name);
    }

    public String getVarName()
    {
        return getPropertyAsString(VAR_NAME);
    }
}
