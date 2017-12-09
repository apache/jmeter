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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>StringFromFile Function to read a String from a text file.</p>
 * 
 * Parameters:
 * <ul>
 *   <li>file name</li>
 *   <li>variable name (optional - defaults to {@code StringFromFile_})</li>
 *   <li>sequence start</li>
 *   <li>sequence end</li>
 * </ul>
 *
 * Returns:
 * <ul>
 *   <li>the next line from the file</li>
 *   <li>or {@code **ERR**} if an error occurs</li>
 *   <li>value is also saved in the variable for later re-use.</li>
 * </ul>
 *
 * <p>Ensure that different variable names are used for each call to the function</p>
 *
 *
 * Notes:
 * <ul>
 * <li>JMeter instantiates a single copy of each function for every reference in the test plan</li>
 * <li>Function instances are shared between threads.</li>
 * <li>Each StringFromFile instance reads the file independently. The output variable can be used to save the
 * value for later use in the same thread.</li>
 * <li>The file name is resolved at file (re-)open time; the file is initially opened on first execution (which could be any thread)</li>
 * <li>the output variable name is resolved every time the function is invoked</li>
 * </ul>
 * Because function instances are shared, it does not make sense to use the thread number as part of the file name.
 * @since 1.9
 */
public class StringFromFile extends AbstractFunction implements TestStateListener {
    private static final Logger log = LoggerFactory.getLogger(StringFromFile.class);

    // Only modified by static block so no need to synchronize subsequent read-only access
    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__StringFromFile";//$NON-NLS-1$

    static final String ERR_IND = "**ERR**";//$NON-NLS-1$

    static {
        desc.add(JMeterUtils.getResString("string_from_file_file_name"));//$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt"));//$NON-NLS-1$
        desc.add(JMeterUtils.getResString("string_from_file_seq_start"));//$NON-NLS-1$
        desc.add(JMeterUtils.getResString("string_from_file_seq_final"));//$NON-NLS-1$
    }

    private static final int MIN_PARAM_COUNT = 1;

    private static final int PARAM_NAME = 2;

    private static final int PARAM_START = 3;

    private static final int PARAM_END = 4;

    private static final int MAX_PARAM_COUNT = 4;

    private static final int COUNT_UNUSED = -2;

    // @GuardedBy("this")
    private Object[] values;

    // @GuardedBy("this")
    private FileReader myFileReader = null; // File reader

    // @GuardedBy("this")
    private BufferedReader myBread = null; // Buffered reader

    // @GuardedBy("this")
    private boolean firstTime = false; // should we try to open the file?

    // @GuardedBy("this")
    private String fileName; // needed for error messages

    // @GuardedBy("this")
    private int myStart = COUNT_UNUSED;

    // @GuardedBy("this")
    private int myCurrent = COUNT_UNUSED;

    // @GuardedBy("this")
    private int myEnd = COUNT_UNUSED;

    public StringFromFile() {
        if (log.isDebugEnabled()) {
            log.debug("++++++++ Construct " + this);
        }
    }

    /**
     * Close file and log
     */
    private synchronized void closeFile() {
        if (myBread == null) {
            return;
        }
        String tn = Thread.currentThread().getName();
        log.info(tn + " closing file " + fileName);//$NON-NLS-1$
        try {
            myBread.close();
        } catch (IOException e) {
            log.error("closeFile() error: " + e.toString(), e);//$NON-NLS-1$
        }
        
        try {
            myFileReader.close();
        } catch (IOException e) {
            log.error("closeFile() error: " + e.toString(), e);//$NON-NLS-1$
        }
    }
    
    private synchronized void openFile() {
        String tn = Thread.currentThread().getName();
        fileName = ((CompoundVariable) values[0]).execute();

        String start = "";
        if (values.length >= PARAM_START) {
            start = ((CompoundVariable) values[PARAM_START - 1]).execute();
            try {
                // Low chances to be non numeric, we parse
                myStart = Integer.parseInt(start);
            } catch(NumberFormatException e) {
                myStart = COUNT_UNUSED;// Don't process invalid numbers
                log.warn("Exception parsing "+start + " as int, value will not be considered as Start Number sequence");
            }
        }
        // Have we used myCurrent yet?
        // Set to 1 if start number is missing (to allow for end without start)
        if (myCurrent == COUNT_UNUSED) {
            myCurrent = myStart == COUNT_UNUSED ? 1 : myStart;
        }

        if (values.length >= PARAM_END) {
            String tmp = ((CompoundVariable) values[PARAM_END - 1]).execute();
            try {
                // Low chances to be non numeric, we parse
                myEnd = Integer.parseInt(tmp);
            } catch(NumberFormatException e) {
                myEnd = COUNT_UNUSED;// Don't process invalid numbers (including "")
                log.warn("Exception parsing "+tmp + " as int, value will not be considered as End Number sequence");
            }
        }

        if (values.length >= PARAM_START) {
            log.info(tn + " Start = " + myStart + " Current = " + myCurrent + " End = " + myEnd);//$NON-NLS-1$
            if (myEnd != COUNT_UNUSED) {
                if (myCurrent > myEnd) {
                    log.info(tn + " No more files to process, " + myCurrent + " > " + myEnd);//$NON-NLS-1$
                    myBread = null;
                    return;
                }
            }
            /*
             * DecimalFormat adds the number to the end of the format if there
             * are no formatting characters, so we need a way to prevent this
             * from messing up the file name.
             *
             */
            if (myStart != COUNT_UNUSED) // Only try to format if there is a
                                            // number
            {
                log.info(tn + " using format " + fileName);
                try {
                    DecimalFormat myFormatter = new DecimalFormat(fileName);
                    fileName = myFormatter.format(myCurrent);
                } catch (NumberFormatException e) {
                    log.warn("Bad file name format ", e);
                }
            }
            myCurrent++;// for next time
        }

        log.info(tn + " opening file " + fileName);//$NON-NLS-1$
        try {
            myFileReader = new FileReader(fileName);
            myBread = new BufferedReader(myFileReader);
        } catch (Exception e) {
            log.error("openFile() error: " + e.toString());//$NON-NLS-1$
            IOUtils.closeQuietly(myFileReader);
            IOUtils.closeQuietly(myBread);
            myBread = null;
            myFileReader = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String myValue = ERR_IND;
        String myName = "StringFromFile_";//$NON-NLS-1$
        if (values.length >= PARAM_NAME) {
            myName = ((CompoundVariable) values[PARAM_NAME - 1]).execute().trim();
        }

        /*
         * To avoid re-opening the file repeatedly after an error, only try to
         * open it in the first execute() call (It may be re=opened at EOF, but
         * that will cause at most one failure.)
         */
        if (firstTime) {
            openFile();
            firstTime = false;
        }

        if (null != myBread) { // Did we open the file?
            try {
                String line = myBread.readLine();
                if (line == null) { // EOF, re-open file
                    String tn = Thread.currentThread().getName();
                    log.info(tn + " EOF on  file " + fileName);//$NON-NLS-1$
                    closeFile();
                    openFile();
                    if (myBread != null) {
                        line = myBread.readLine();
                    } else {
                        line = ERR_IND;
                        if (myEnd != COUNT_UNUSED) {// Are we processing a file
                                                    // sequence?
                            log.info(tn + " Detected end of sequence.");
                            throw new JMeterStopThreadException("End of sequence");
                        }
                    }
                }
                myValue = line;
            } catch (IOException e) {
                String tn = Thread.currentThread().getName();
                log.error(tn + " error reading file " + e.toString());//$NON-NLS-1$
            }
        } else { // File was not opened successfully
            if (myEnd != COUNT_UNUSED) {// Are we processing a file sequence?
                String tn = Thread.currentThread().getName();
                log.info(tn + " Detected end of sequence.");
                throw new JMeterStopThreadException("End of sequence");
            }
        }

        if (myName.length() > 0) {
            JMeterVariables vars = getVariables();
            if (vars != null) {// Can be null if called from Config item testEnded() method
                vars.put(myName, myValue);
            }
        }

        if (log.isDebugEnabled()) {
            String tn = Thread.currentThread().getName();
            log.debug(tn + " name:" //$NON-NLS-1$
                    + myName + " value:" + myValue);//$NON-NLS-1$
        }

        return myValue;

    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {

        log.debug(this + "::StringFromFile.setParameters()");//$NON-NLS-1$
        checkParameterCount(parameters, MIN_PARAM_COUNT, MAX_PARAM_COUNT);
        values = parameters.toArray();

        StringBuilder sb = new StringBuilder(40);
        sb.append("setParameters(");//$NON-NLS-1$
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(((CompoundVariable) values[i]).getRawParameters());
        }
        sb.append(')');//$NON-NLS-1$
        log.info(sb.toString());

        // N.B. setParameters is called before the test proper is started,
        // and thus variables are not interpreted at this point
        // So defer the file open until later to allow variable file names to be
        // used.
        firstTime = true;
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

    /** {@inheritDoc} */
    @Override
    public void testStarted() {
        //
    }

    /** {@inheritDoc} */
    @Override
    public void testStarted(String host) {
        //
    }

    /** {@inheritDoc} */
    @Override
    public void testEnded() {
        this.testEnded(""); //$NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void testEnded(String host) {
        closeFile();
    }

}
