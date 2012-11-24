/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;

/**
 * <p>
 * Function to log a message.
 * </p>
 *
 * <p>
 * Parameters:
 * <ul>
 * <li>string value</li>
 * <li>log level (optional; defaults to INFO; or DEBUG if unrecognised; or can use OUT or ERR)</li>
 * <li>throwable message (optional)</li>
 * <li>comment (optional)</li>
 * </ul>
 * </p>
 * Returns: - the input string
 * @since 2.2
 */
public class LogFunction extends AbstractFunction {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final List<String> desc = new LinkedList<String>();

    private static final String KEY = "__log"; //$NON-NLS-1$

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 1;

    private static final int MAX_PARAMETER_COUNT = 4;
    static {
        desc.add(JMeterUtils.getResString("log_function_string_ret"));    //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("log_function_level"));     //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("log_function_throwable")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("log_function_comment"));   //$NON-NLS-1$
    }

    private static final String DEFAULT_PRIORITY = "INFO"; //$NON-NLS-1$

    private static final String DEFAULT_SEPARATOR = " : "; //$NON-NLS-1$

    private Object[] values;

    public LogFunction() {
    }

    /** {@inheritDoc} */
    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String stringToLog = ((CompoundVariable) values[0]).execute();

        String priorityString;
        if (values.length > 1) { // We have a default
            priorityString = ((CompoundVariable) values[1]).execute();
            if (priorityString.length() == 0) {
                priorityString = DEFAULT_PRIORITY;
            }
        } else {
            priorityString = DEFAULT_PRIORITY;
        }

        Throwable t = null;
        if (values.length > 2) { // Throwable wanted
            String value = ((CompoundVariable) values[2]).execute();
            if (value.length() > 0) {
                t = new Throwable(value);
            }
        }

        String comment = "";
        if (values.length > 3) { // Comment wanted
            comment = ((CompoundVariable) values[3]).execute();
        }

        logDetails(log, stringToLog, priorityString, t, comment);

        return stringToLog;

    }

    // Common output function
    private static void printDetails(java.io.PrintStream ps, String s, Throwable t, String c) {
        String tn = Thread.currentThread().getName();

        StringBuilder sb = new StringBuilder(80);
        sb.append("Log: ");
        sb.append(tn);
        if (c.length()>0){
            sb.append(" ");
            sb.append(c);
        } else {
            sb.append(DEFAULT_SEPARATOR);
        }
        sb.append(s);
        if (t != null) {
            sb.append(" ");
            ps.print(sb.toString());
            t.printStackTrace(ps);
        } else {
            ps.println(sb.toString());
        }
    }

    // Routine to perform the output (also used by __logn() function)
    static void logDetails(Logger l, String s, String prio, Throwable t, String c) {
        if (prio.equalsIgnoreCase("OUT")) //$NON-NLS-1
        {
            printDetails(System.out, s, t, c);
        } else if (prio.equalsIgnoreCase("ERR")) //$NON-NLS-1
        {
            printDetails(System.err, s, t, c);
        } else {
            // N.B. if the string is not recognised, DEBUG is assumed
            Priority p = Priority.getPriorityForName(prio);
            if (log.isPriorityEnabled(p)) {// Thread method is potentially expensive
                String tn = Thread.currentThread().getName();
                StringBuilder sb = new StringBuilder(40);
                sb.append(tn);
                if (c.length()>0){
                    sb.append(" ");
                    sb.append(c);
                } else {
                    sb.append(DEFAULT_SEPARATOR);
                }
                sb.append(s);
                log.log(p, sb.toString(), t);
            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

}