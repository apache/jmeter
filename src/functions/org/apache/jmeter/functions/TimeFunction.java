/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.jmeter.functions;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;

// See org.apache.jmeter.functions.TestTimeFunction for unit tests

/**
 * __time() function - returns the current time in milliseconds
 */
public class TimeFunction extends AbstractFunction implements Serializable {

    private static final String KEY = "__time"; // $NON-NLS-1$

    private static final List desc = new LinkedList();

    private static final Map aliases = new HashMap();
    
    static {
        desc.add(JMeterUtils.getResString("time_format")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
        aliases.put("YMD", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.YMD", //$NON-NLS-1$
                        "yyyyMMdd")); //$NON-NLS-1$
        aliases.put("HMS", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.HMS", //$NON-NLS-1$
                        "HHmmss")); //$NON-NLS-1$
        aliases.put("YMDHMS", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.YMDHMS", //$NON-NLS-1$
                        "yyyyMMdd-HHmmss")); //$NON-NLS-1$
        aliases.put("USER1", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.USER1","")); //$NON-NLS-1$
        aliases.put("USER2", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.USER2","")); //$NON-NLS-1$
    }

    // Ensure that these are set, even if no paramters are provided
    transient private String format   = ""; //$NON-NLS-1$
    transient private String variable = ""; //$NON-NLS-1$
    
    public TimeFunction(){
        super();
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
     */
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        String datetime;
        if (format.length() == 0){// Default to milliseconds
            datetime = Long.toString(System.currentTimeMillis());
        } else {
            // Resolve any aliases
            String fmt = (String) aliases.get(format);
            if (fmt == null) fmt = format;// Not found
            SimpleDateFormat df = new SimpleDateFormat(fmt);// Not synchronised, so can't be shared
            datetime = df.format(new Date());            
        }
        
        if (variable.length() > 0) {
            JMeterVariables vars = getVariables();
            vars.put(variable, datetime);
        }
        return datetime;
    }

    /*
     * (non-Javadoc)
     * 
     * It appears that this is not called if no parameters are provided.
     * 
     * @see org.apache.jmeter.functions.Function#setParameters(Collection)
     */
    public void setParameters(Collection parameters) throws InvalidVariableException {

        checkParameterCount(parameters, 0, 2);
        
        Object []values = parameters.toArray();
        int count = values.length;
        
        if (count > 0) {
            format = ((CompoundVariable) values[0]).execute();
        }
        
        if (count > 1) {
            variable = ((CompoundVariable)values[1]).execute();
        }
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.functions.Function#getReferenceKey()
     */
    public String getReferenceKey() {
        return KEY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.functions.Function#getArgumentDesc()
     */
    public List getArgumentDesc() {
        return desc;
    }
}
