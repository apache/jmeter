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

package org.apache.jmeter.reporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Save Result responseData to a set of files
 *
 *
 * This is mainly intended for validation tests
 *
 */
// TODO - perhaps save other items such as headers?
public class ResultSaver extends AbstractTestElement implements Serializable, SampleListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    private static final Object LOCK = new Object();

    // File name sequence number
    //@GuardedBy("LOCK")
    private static long sequenceNumber = 0;

    //@GuardedBy("LOCK")
    private static String timeStamp;

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd-HHmm_"; // $NON-NLS-1$

    //@GuardedBy("LOCK")
    private static int numberPadLength;

    //+ JMX property names; do not change

    public static final String FILENAME = "FileSaver.filename"; // $NON-NLS-1$

    public static final String VARIABLE_NAME = "FileSaver.variablename"; // $NON-NLS-1$

    public static final String ERRORS_ONLY = "FileSaver.errorsonly"; // $NON-NLS-1$

    public static final String SUCCESS_ONLY = "FileSaver.successonly"; // $NON-NLS-1$

    public static final String SKIP_AUTO_NUMBER = "FileSaver.skipautonumber"; // $NON-NLS-1$

    public static final String SKIP_SUFFIX = "FileSaver.skipsuffix"; // $NON-NLS-1$

    public static final String ADD_TIMESTAMP = "FileSaver.addTimstamp"; // $NON-NLS-1$

    public static final String NUMBER_PAD_LENGTH = "FileSaver.numberPadLen"; // $NON-NLS-1$

    //- JMX property names

    private synchronized long nextNumber() {
        return ++sequenceNumber;
    }

    /*
     * Constructor is initially called once for each occurrence in the test plan
     * For GUI, several more instances are created Then clear is called at start
     * of test Called several times during test startup The name will not
     * necessarily have been set at this point.
     */
    public ResultSaver() {
        super();
        // log.debug(Thread.currentThread().getName());
        // System.out.println(">> "+me+" "+this.getName()+"
        // "+Thread.currentThread().getName());
    }

    /*
     * Constructor for use during startup (intended for non-GUI use) @param name
     * of summariser
     */
    public ResultSaver(String name) {
        this();
        setName(name);
    }

    /*
     * This is called once for each occurrence in the test plan, before the
     * start of the test. The super.clear() method clears the name (and all
     * other properties), so it is called last.
     */
    @Override
    public void clear() {
        synchronized(LOCK){
            sequenceNumber = 0; // TODO is this the right thing to do?
            if (getAddTimeStamp()) {
                DateFormat format = new SimpleDateFormat(TIMESTAMP_FORMAT);
                timeStamp = format.format(new Date());
            } else {
                timeStamp = "";
            }
            numberPadLength=getNumberPadLen();
        }
        super.clear();
    }

    /**
     * Saves the sample result (and any sub results) in files
     *
     * @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.jmeter.samplers.SampleEvent)
     */
    @Override
    public void sampleOccurred(SampleEvent e) {
      processSample(e.getResult(), new Counter());
   }

   /**
    * Recurse the whole (sub)result hierarchy.
    *
    * @param s Sample result
    * @param c sample counter
    */
   private void processSample(SampleResult s, Counter c) {
        saveSample(s, c.num++);
        SampleResult[] sr = s.getSubResults();
        for (int i = 0; i < sr.length; i++) {
            processSample(sr[i], c);
        }
    }

    /**
     * @param s SampleResult to save
     * @param num number to append to variable (if >0)
     */
    private void saveSample(SampleResult s, int num) {
        // Should we save the sample?
        if (s.isSuccessful()){
            if (getErrorsOnly()){
                return;
            }
        } else {
            if (getSuccessOnly()){
                return;
            }
        }

        String fileName = makeFileName(s.getContentType(), getSkipAutoNumber(), getSkipSuffix());
        log.debug("Saving " + s.getSampleLabel() + " in " + fileName);
        s.setResultFileName(fileName);// Associate sample with file name
        String variable = getVariableName();
        if (variable.length()>0){
            if (num > 0) {
                StringBuilder sb = new StringBuilder(variable);
                sb.append(num);
                variable=sb.toString();
            }
            JMeterContextService.getContext().getVariables().put(variable, fileName);
        }
        File out = new File(fileName);
        FileOutputStream pw = null;
        try {
            pw = new FileOutputStream(out);
            JOrphanUtils.write(s.getResponseData(), pw); // chunk the output if necessary
        } catch (FileNotFoundException e1) {
            log.error("Error creating sample file for " + s.getSampleLabel(), e1);
        } catch (IOException e1) {
            log.error("Error saving sample " + s.getSampleLabel(), e1);
        } finally {
            JOrphanUtils.closeQuietly(pw);
        }
    }

    /**
     * @return fileName composed of fixed prefix, a number, and a suffix derived
     *         from the contentType e.g. Content-Type:
     *         text/html;charset=ISO-8859-1
     */
    private String makeFileName(String contentType, boolean skipAutoNumber, boolean skipSuffix) {
        StringBuilder sb = new StringBuilder(FileServer.resolveBaseRelativeName(getFilename()));
        sb.append(timeStamp); // may be the empty string
        if (!skipAutoNumber){
            String number = Long.toString(nextNumber());
            for(int i=number.length(); i < numberPadLength; i++) {
                sb.append('0');
            }
            sb.append(number);
        }
        if (!skipSuffix){
            sb.append('.');
            if (contentType != null) {
                int i = contentType.indexOf('/'); // $NON-NLS-1$
                if (i != -1) {
                    int j = contentType.indexOf(';'); // $NON-NLS-1$
                    if (j != -1) {
                        sb.append(contentType.substring(i + 1, j));
                    } else {
                        sb.append(contentType.substring(i + 1));
                    }
                } else {
                    sb.append("unknown");
                }
            } else {
                sb.append("unknown");
            }
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sampleStarted(SampleEvent e) {
        // not used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sampleStopped(SampleEvent e) {
        // not used
    }

    private String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    private String getVariableName() {
        return getPropertyAsString(VARIABLE_NAME,""); // $NON-NLS-1$
    }

    private boolean getErrorsOnly() {
        return getPropertyAsBoolean(ERRORS_ONLY);
    }

    private boolean getSkipAutoNumber() {
        return getPropertyAsBoolean(SKIP_AUTO_NUMBER);
    }

    private boolean getSkipSuffix() {
        return getPropertyAsBoolean(SKIP_SUFFIX);
    }

    private boolean getSuccessOnly() {
        return getPropertyAsBoolean(SUCCESS_ONLY);
    }

    private boolean getAddTimeStamp() {
        return getPropertyAsBoolean(ADD_TIMESTAMP);
    }

    private int getNumberPadLen() {
        return getPropertyAsInt(NUMBER_PAD_LENGTH, 0);
    }

    // Mutable int to keep track of sample count
    private static class Counter{
        int num;
    }
}
