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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The function represented by this class allows data to be read from CSV files.
 * Syntax is similar to StringFromFile function. The function allows the test to
 * line-thru the data in the CSV file - one line per each test. E.g. inserting
 * the following in the test scripts :
 *
 * <pre>{@code
 * ${__CSVRead(c:/BOF/abcd.csv,0)} // read (first) line of 'c:/BOF/abcd.csv' ,
 * return the 1<sup>st</sup> column ( represented by the '0'),
 * ${__CSVRead(c:/BOF/abcd.csv,1)} // read (first) line of 'c:/BOF/abcd.csv' ,
 * return the 2<sup>nd</sup> column ( represented by the '1'),
 * ${__CSVRead(c:/BOF/abcd.csv,next())} // Go to next line of 'c:/BOF/abcd.csv'
 * }</pre>
 * NOTE: A single instance of each different file is opened and used for all
 * threads.<br><br>
 *
 * To open the same file twice, use the alias function: {@code __CSVRead(abc.csv,*ONE);
 * __CSVRead(abc.csv,*TWO);}
 *<br>
 * {@code __CSVRead(*ONE,1);} etc
 * @since 1.9
 */
public class CSVRead extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(CSVRead.class);

    private static final String KEY = "__CSVRead"; // Function name //$NON-NLS-1$

    private static final List<String> desc = new LinkedList<>();

    private Object[] values; // Parameter list

    static {
        desc.add(JMeterUtils.getResString("csvread_file_file_name")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("column_number")); //$NON-NLS-1$
    }

    public CSVRead() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String myValue = ""; //$NON-NLS-1$

        String fileName = ((org.apache.jmeter.engine.util.CompoundVariable) values[0]).execute();
        String columnOrNext = ((org.apache.jmeter.engine.util.CompoundVariable) values[1]).execute();

        if (log.isDebugEnabled()) {
            log.debug("execute (" + fileName + " , " + columnOrNext + ")   ");
        }

        // Process __CSVRead(filename,*ALIAS)
        if (columnOrNext.startsWith("*")) { //$NON-NLS-1$
            FileWrapper.open(fileName, columnOrNext);
            /*
             * All done, so return
             */
            return ""; //$NON-NLS-1$
        }

        // if argument is 'next' - go to the next line
        if (columnOrNext.equals("next()") || columnOrNext.equals("next")) { //$NON-NLS-1$ //$NON-NLS-2$
            FileWrapper.endRow(fileName);

            /*
             * All done now, so return the empty string - this allows the caller
             * to append __CSVRead(file,next) to the last instance of
             * __CSVRead(file,col)
             *
             * N.B. It is important not to read any further lines at this point,
             * otherwise the wrong line can be retrieved when using multiple
             * threads.
             */
            return ""; //$NON-NLS-1$
        }

        try {
            int columnIndex = Integer.parseInt(columnOrNext); // what column
                                                                // is wanted?
            myValue = FileWrapper.getColumn(fileName, columnIndex);
        } catch (NumberFormatException e) {
            log.warn(Thread.currentThread().getName() + " - can't parse column number: " + columnOrNext + " "
                    + e.toString());
        } catch (IndexOutOfBoundsException e) {
            log.warn(Thread.currentThread().getName() + " - invalid column number: " + columnOrNext + " at row "
                    + FileWrapper.getCurrentRow(fileName) + " " + e.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("execute value: " + myValue);
        }

        return myValue;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        log.debug("setParameter - Collection.size=" + parameters.size());

        values = parameters.toArray();

        if (log.isDebugEnabled()) {
            for (int i = 0; i < parameters.size(); i++) {
                log.debug("i:" + ((CompoundVariable) values[i]).execute());
            }
        }

        checkParameterCount(parameters, 2);

        /*
         * Need to reset the containers for repeated runs; about the only way
         * for functions to detect that a run is starting seems to be the
         * setParameters() call.
         */
        FileWrapper.clearAll();// TODO only clear the relevant entry - if possible...

    }
}
