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

package org.apache.jmeter.config;

import java.io.IOException;
import java.util.List;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Read lines from a file and split int variables.
 *
 * The iterationStart() method is used to set up each set of values.
 *
 * By default, the same file is shared between all threads
 * (and other thread groups, if they use the same file name).
 *
 * The shareMode can be set to:
 * <ul>
 * <li>All threads - default, as described above</li>
 * <li>Current thread group</li>
 * <li>Current thread</li>
 * <li>Identifier - all threads sharing the same identifier</li>
 * </ul>
 *
 * The class uses the FileServer alias mechanism to provide the different share modes.
 * For all threads, the file alias is set to the file name.
 * Otherwise, a suffix is appended to the filename to make it unique within the required context.
 * For current thread group, the thread group identityHashcode is used;
 * for individual threads, the thread hashcode is used as the suffix.
 * Or the user can provide their own suffix, in which case the file is shared between all
 * threads with the same suffix.
 *
 */
public class CSVDataSet extends ConfigTestElement implements TestBean, LoopIterationListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 232L;

    private static final String EOFVALUE = // value to return at EOF
        JMeterUtils.getPropDefault("csvdataset.eofstring", "<EOF>"); //$NON-NLS-1$ //$NON-NLS-2$

    private transient String filename;

    private transient String fileEncoding;

    private transient String variableNames;

    private transient String delimiter;

    private transient boolean quoted;

    private transient boolean recycle = true;

    private transient boolean stopThread;

    private transient String[] vars;

    private transient String alias;

    private transient String shareMode;
    
    private boolean firstLineIsNames = false;

    private Object readResolve(){
        recycle = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void iterationStart(LoopIterationEvent iterEvent) {
        FileServer server = FileServer.getFileServer();
        final JMeterContext context = getThreadContext();
        String delim = getDelimiter();
        if (delim.equals("\\t")) { // $NON-NLS-1$
            delim = "\t";// Make it easier to enter a Tab // $NON-NLS-1$
        } else if (delim.length()==0){
            log.warn("Empty delimiter converted to ','");
            delim=",";
        }
        if (vars == null) {
            String _fileName = getFilename();
            String mode = getShareMode();
            int modeInt = CSVDataSetBeanInfo.getShareModeAsInt(mode);
            switch(modeInt){
                case CSVDataSetBeanInfo.SHARE_ALL:
                    alias = _fileName;
                    break;
                case CSVDataSetBeanInfo.SHARE_GROUP:
                    alias = _fileName+"@"+System.identityHashCode(context.getThreadGroup());
                    break;
                case CSVDataSetBeanInfo.SHARE_THREAD:
                    alias = _fileName+"@"+System.identityHashCode(context.getThread());
                    break;
                default:
                    alias = _fileName+"@"+mode; // user-specified key
                    break;
            }
            final String names = getVariableNames();
            if (names == null || names.length()==0) {
                String header = server.reserveFile(_fileName, getFileEncoding(), alias, true);
                try {
                    vars = CSVSaveService.csvSplitString(header, delim.charAt(0));
                    firstLineIsNames = true;
                } catch (IOException e) {
                    log.warn("Could not split CSV header line",e);
                }
            } else {
                server.reserveFile(_fileName, getFileEncoding(), alias);
                vars = JOrphanUtils.split(names, ","); // $NON-NLS-1$
            }
        }
           
        // TODO: fetch this once as per vars above?
        JMeterVariables threadVars = context.getVariables();
        String line = null;
        try {
            line = server.readLine(alias, getRecycle(), firstLineIsNames);
        } catch (IOException e) { // treat the same as EOF
            log.error(e.toString());
        }
        if (line!=null) {// i.e. not EOF
            try {
                String[] lineValues = getQuotedData() ?
                        CSVSaveService.csvSplitString(line, delim.charAt(0))
                        : JOrphanUtils.split(line, delim, false);
                for (int a = 0; a < vars.length && a < lineValues.length; a++) {
                    threadVars.put(vars[a], lineValues[a]);
                }
            } catch (IOException e) { // Should only happen for quoting errors
               log.error("Unexpected error splitting '"+line+"' on '"+delim.charAt(0)+"'");
            }
            // TODO - report unused columns?
            // TODO - provide option to set unused variables ?
        } else {
            if (getStopThread()) {
                throw new JMeterStopThreadException("End of file detected");
            }
            for (int a = 0; a < vars.length ; a++) {
                threadVars.put(vars[a], EOFVALUE);
            }
        }
    }

    /**
     * @return Returns the filename.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename
     *            The filename to set.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return Returns the file encoding.
     */
    public String getFileEncoding() {
        return fileEncoding;
    }

    /**
     * @param fileEncoding
     *            The fileEncoding to set.
     */
    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    /**
     * @return Returns the variableNames.
     */
    public String getVariableNames() {
        return variableNames;
    }

    /**
     * @param variableNames
     *            The variableNames to set.
     */
    public void setVariableNames(String variableNames) {
        this.variableNames = variableNames;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean getQuotedData() {
        return quoted;
    }

    public void setQuotedData(boolean quoted) {
        this.quoted = quoted;
    }

    public boolean getRecycle() {
        return recycle;
    }

    public void setRecycle(boolean recycle) {
        this.recycle = recycle;
    }

    public boolean getStopThread() {
        return stopThread;
    }

    public void setStopThread(boolean value) {
        this.stopThread = value;
    }

    public String getShareMode() {
        return shareMode;
    }

    public void setShareMode(String value) {
        this.shareMode = value;
    }
    
    /** 
     * {@inheritDoc}}
     */
    @Override
    public List<String> getSearchableTokens() throws Exception {
        List<String> result = super.getSearchableTokens();
        result.add(getPropertyAsString("variableNames"));
        return result;
    }
}
