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

package org.apache.jmeter.threads;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thad Smith
 * @version $Revision$
 */
public final class JMeterContextService implements Serializable
{
    static private JMeterContextService _instance = null;
    static private Map contextMap = new HashMap();

//TODO: consider using ThreadLocal instead?

    /**
     * Private constructor to prevent instantiation.
     */
    private JMeterContextService()
    {
    }

    static private void init()
    {
        if (_instance == null)
        {
            _instance = new JMeterContextService();
        }
    }

    static public JMeterContext getContext()
    {

        init();

        JMeterContext context =
            (JMeterContext) contextMap.get(Thread.currentThread().getName());

        if (context == null)
        {
            context = new JMeterContext();
            setContext(context);
        }

        return context;

    }

    static void setContext(JMeterContext context)
    {
        init();
        contextMap.put(Thread.currentThread().getName(), context);
    }

}
