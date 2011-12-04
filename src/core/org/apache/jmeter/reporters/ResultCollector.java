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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterError;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * This class handles all saving of samples.
 * The class must be thread-safe because it is shared between threads (NoThreadClone).
 */
public class ResultCollector extends AbstractListenerElement implements SampleListener, Clearable, Serializable,
        TestListener, Remoteable, NoThreadClone {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 233L;

    // This string is used to identify local test runs, so must not be a valid host name
    private static final String TEST_IS_LOCAL = "*local*"; // $NON-NLS-1$

    private static final String TESTRESULTS_START = "<testResults>"; // $NON-NLS-1$

    private static final String TESTRESULTS_START_V1_1_PREVER = "<testResults version=\"";  // $NON-NLS-1$

    private static final String TESTRESULTS_START_V1_1_POSTVER="\">"; // $NON-NLS-1$

    private static final String TESTRESULTS_END = "</testResults>"; // $NON-NLS-1$

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; // $NON-NLS-1$

    private static final int MIN_XML_FILE_LEN = XML_HEADER.length() + TESTRESULTS_START.length()
            + TESTRESULTS_END.length();

    public static final String FILENAME = "filename"; // $NON-NLS-1$

    private static final String SAVE_CONFIG = "saveConfig"; // $NON-NLS-1$

    private static final String ERROR_LOGGING = "ResultCollector.error_logging"; // $NON-NLS-1$

    private static final String SUCCESS_ONLY_LOGGING = "ResultCollector.success_only_logging"; // $NON-NLS-1$

    // Static variables

    // Lock used to guard static mutable variables
    private static final Object LOCK = new Object();

    //@GuardedBy("LOCK")
    private static final Map<String, FileEntry> files = new HashMap<String, FileEntry>();

    /*
     * Keep track of the file writer and the configuration,
     * as the instance used to close them is not the same as the instance that creates
     * them. This means one cannot use the saved PrintWriter or use getSaveConfig()
     */
    private static class FileEntry{
        final PrintWriter pw;
        final SampleSaveConfiguration config;
        FileEntry(PrintWriter _pw, SampleSaveConfiguration _config){
            pw =_pw;
            config = _config;
        }
    }

    /**
     * The instance count is used to keep track of whether any tests are currently running.
     * It's not possible to use the constructor or threadStarted etc as tests may overlap
     * e.g. a remote test may be started,
     * and then a local test started whilst the remote test is still running.
     */
    //@GuardedBy("LOCK")
    private static int instanceCount; // Keep track of how many instances are active

    // Instance variables (guarded by volatile)

    private transient volatile PrintWriter out;

    private volatile boolean inTest = false;

    private volatile boolean isStats = false;

    /** the summarizer to which this result collector will forward the samples */
    private volatile Summariser summariser;

    /**
     * No-arg constructor.
     */
    public ResultCollector() {
        this(null);
    }

    public ResultCollector(Summariser summer) {
        setErrorLogging(false);
        setSuccessOnlyLogging(false);
        setProperty(new ObjectProperty(SAVE_CONFIG, new SampleSaveConfiguration()));
        summariser = summer;
    }

    // Ensure that the sample save config is not shared between copied nodes
    // N.B. clone only seems to be used for client-server tests
    @Override
    public Object clone(){
        ResultCollector clone = (ResultCollector) super.clone();
        clone.setSaveConfig((SampleSaveConfiguration)clone.getSaveConfig().clone());
        // Unfortunately AbstractTestElement does not call super.clone()
        clone.summariser = this.summariser;
        return clone;
    }

    private void setFilenameProperty(String f) {
        setProperty(FILENAME, f);
    }

    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    public boolean isErrorLogging() {
        return getPropertyAsBoolean(ERROR_LOGGING);
    }

    public final void setErrorLogging(boolean errorLogging) {
        setProperty(new BooleanProperty(ERROR_LOGGING, errorLogging));
    }

    public final void setSuccessOnlyLogging(boolean value) {
        if (value) {
            setProperty(new BooleanProperty(SUCCESS_ONLY_LOGGING, true));
        } else {
            removeProperty(SUCCESS_ONLY_LOGGING);
        }
    }

    public boolean isSuccessOnlyLogging() {
        return getPropertyAsBoolean(SUCCESS_ONLY_LOGGING,false);
    }

    /**
     * Decides whether or not to a sample is wanted based on:<br/>
     * - errorOnly<br/>
     * - successOnly<br/>
     * - sample success<br/>
     * Should only be called for single samples.
     *
     * @param success is sample successful
     * @return whether to log/display the sample
     */
    public boolean isSampleWanted(boolean success){
        boolean errorOnly = isErrorLogging();
        boolean successOnly = isSuccessOnlyLogging();
        return isSampleWanted(success, errorOnly, successOnly);
    }

    /**
     * Decides whether or not to a sample is wanted based on: <br/>
     * - errorOnly <br/>
     * - successOnly <br/>
     * - sample success <br/>
     * This version is intended to be called by code that loops over many samples;
     * it is cheaper than fetching the settings each time.
     * @param success status of sample
     * @param errorOnly if errors only wanted
     * @param successOnly if success only wanted
     * @return whether to log/display the sample
     */
    public static boolean isSampleWanted(boolean success, boolean errorOnly,
            boolean successOnly) {
        return (!errorOnly && !successOnly) ||
               (success && successOnly) ||
               (!success && errorOnly);
        // successOnly and errorOnly cannot both be set
    }
    /**
     * Sets the filename attribute of the ResultCollector object.
     *
     * @param f
     *            the new filename value
     */
    public void setFilename(String f) {
        if (inTest) {
            return;
        }
        setFilenameProperty(f);
    }

    public void testEnded(String host) {
        synchronized(LOCK){
            instanceCount--;
            if (instanceCount <= 0) {
                finalizeFileOutput();
                inTest = false;
            }
        }

        if(summariser != null) {
            summariser.testEnded(host);
        }
    }

    public void testStarted(String host) {
        synchronized(LOCK){
            instanceCount++;
            try {
                initializeFileOutput();
                if (getVisualizer() != null) {
                    this.isStats = getVisualizer().isStats();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        inTest = true;

        if(summariser != null) {
            summariser.testStarted(host);
        }
    }

    public void testEnded() {
        testEnded(TEST_IS_LOCAL);
    }

    public void testStarted() {
        testStarted(TEST_IS_LOCAL);
    }

    /**
     * Loads an existing sample data (JTL) file.
     * This can be one of:
     * - XStream format
     * - Avalon format
     * - CSV format
     *
     */
    public void loadExistingFile() {
        final Visualizer visualizer = getVisualizer();
        if (visualizer == null) {
            return; // No point reading the file if there's no visualiser
        }
        boolean parsedOK = false;
        String filename = getFilename();
        File file = new File(filename);
        if (file.exists()) {
            BufferedReader dataReader = null;
            BufferedInputStream bufferedInputStream = null;
            try {
                dataReader = new BufferedReader(new FileReader(file));
                // Get the first line, and see if it is XML
                String line = dataReader.readLine();
                dataReader.close();
                dataReader = null;
                if (line == null) {
                    log.warn(filename+" is empty");
                } else {
                    if (!line.startsWith("<?xml ")){// No, must be CSV //$NON-NLS-1$
                        CSVSaveService.processSamples(filename, visualizer, this);
                        parsedOK = true;
                    } else { // We are processing XML
                        try { // Assume XStream
                            bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                            SaveService.loadTestResults(bufferedInputStream,
                                    new ResultCollectorHelper(this, visualizer));
                            parsedOK = true;
                        } catch (Exception e) {
                            log.warn("Failed to load "+filename+" using XStream. Error was: "+e);
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("Problem reading JTL file: "+file);
            } catch (JMeterError e){
                log.warn("Problem reading JTL file: "+file);
            } catch (RuntimeException e){ // e.g. NullPointerException
                log.warn("Problem reading JTL file: "+file,e);
            } catch (OutOfMemoryError e) {
                log.warn("Problem reading JTL file: "+file,e);
            } finally {
                JOrphanUtils.closeQuietly(dataReader);
                JOrphanUtils.closeQuietly(bufferedInputStream);
                if (!parsedOK) {
                    GuiPackage.showErrorMessage(
                                "Error loading results file - see log file",
                                "Result file loader");
                }
            }
        } else {
            GuiPackage.showErrorMessage(
                    "Error loading results file - could not open file",
                    "Result file loader");
        }
    }

    private static void writeFileStart(PrintWriter writer, SampleSaveConfiguration saveConfig) {
        if (saveConfig.saveAsXml()) {
            writer.print(XML_HEADER);
            // Write the EOL separately so we generate LF line ends on Unix and Windows
            writer.print("\n"); // $NON-NLS-1$
            String pi=saveConfig.getXmlPi();
            if (pi.length() > 0) {
                writer.println(pi);
            }
            // Can't do it as a static initialisation, because SaveService
            // is being constructed when this is called
            writer.print(TESTRESULTS_START_V1_1_PREVER);
            writer.print(SaveService.getVERSION());
            writer.print(TESTRESULTS_START_V1_1_POSTVER);
            // Write the EOL separately so we generate LF line ends on Unix and Windows
            writer.print("\n"); // $NON-NLS-1$
        } else if (saveConfig.saveFieldNames()) {
            writer.println(CSVSaveService.printableFieldNamesToString(saveConfig));
        }
    }

    private static void writeFileEnd(PrintWriter pw, SampleSaveConfiguration saveConfig) {
        if (saveConfig.saveAsXml()) {
            pw.print("\n"); // $NON-NLS-1$
            pw.print(TESTRESULTS_END);
            pw.print("\n");// Added in version 1.1 // $NON-NLS-1$
        }
    }

    private static PrintWriter getFileWriter(String filename, SampleSaveConfiguration saveConfig)
            throws IOException {
        if (filename == null || filename.length() == 0) {
            return null;
        }
        filename = FileServer.resolveBaseRelativeName(filename);
        FileEntry fe = files.get(filename);
        PrintWriter writer = null;
        boolean trimmed = true;

        if (fe == null) {
            if (saveConfig.saveAsXml()) {
                trimmed = trimLastLine(filename);
            } else {
                trimmed = new File(filename).exists();
            }
            // Find the name of the directory containing the file
            // and create it - if there is one
            File pdir = new File(filename).getParentFile();
            if (pdir != null) {
            	// returns false if directory already exists, so need to check again
                if(pdir.mkdirs()){
                	log.info("Folder "+pdir.getAbsolutePath()+" was created");
                } // else if might have been created by another process so not a problem
                if (!pdir.exists()){
                    log.warn("Error creating directories for "+pdir.toString());
                }
            }
            writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(filename,
                    trimmed)), SaveService.getFileEncoding("UTF-8")), true); // $NON-NLS-1$
            log.debug("Opened file: "+filename);
            files.put(filename, new FileEntry(writer, saveConfig));
        } else {
            writer = fe.pw;
        }
        if (!trimmed) {
            writeFileStart(writer, saveConfig);
        }
        return writer;
    }

    // returns false if the file did not contain the terminator
    private static boolean trimLastLine(String filename) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(filename, "rw"); // $NON-NLS-1$
            long len = raf.length();
            if (len < MIN_XML_FILE_LEN) {
                return false;
            }
            raf.seek(len - TESTRESULTS_END.length() - 10);// TODO: may not work on all OSes?
            String line;
            long pos = raf.getFilePointer();
            int end = 0;
            while ((line = raf.readLine()) != null)// reads to end of line OR end of file
            {
                end = line.indexOf(TESTRESULTS_END);
                if (end >= 0) // found the string
                {
                    break;
                }
                pos = raf.getFilePointer();
            }
            if (line == null) {
                log.warn("Unexpected EOF trying to find XML end marker in " + filename);
                raf.close();
                return false;
            }
            raf.setLength(pos + end);// Truncate the file
            raf.close();
            raf = null;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            log.warn("Error trying to find XML terminator " + e.toString());
            return false;
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e1) {
                log.info("Could not close " + filename + " " + e1.getLocalizedMessage());
            }
        }
        return true;
    }

    public void sampleStarted(SampleEvent e) {
    }

    public void sampleStopped(SampleEvent e) {
    }

    /**
     * When a test result is received, display it and save it.
     *
     * @param event
     *            the sample event that was received
     */
    public void sampleOccurred(SampleEvent event) {
        SampleResult result = event.getResult();

        if (isSampleWanted(result.isSuccessful())) {
            sendToVisualizer(result);
            if (out != null && !isResultMarked(result) && !this.isStats) {
                SampleSaveConfiguration config = getSaveConfig();
                result.setSaveConfig(config);
                try {
                    if (config.saveAsXml()) {
                        SaveService.saveSampleResult(event, out);
                    } else { // !saveAsXml
                        String savee = CSVSaveService.resultToDelimitedString(event);
                        out.println(savee);
                    }
                } catch (Exception err) {
                    log.error("Error trying to record a sample", err); // should throw exception back to caller
                }
            }
        }

        if(summariser != null) {
            summariser.sampleOccurred(event);
        }
    }

    protected final void sendToVisualizer(SampleResult r) {
        if (getVisualizer() != null) {
            getVisualizer().add(r);
        }
    }

    /**
     * recordStats is used to save statistics generated by visualizers
     *
     * @param e
     * @throws Exception
     */
    // Used by: MonitorHealthVisualizer.add(SampleResult res)
    public void recordStats(TestElement e) throws Exception {
        if (out != null) {
            SaveService.saveTestElement(e, out);
        }
    }

    /**
     * Checks if the sample result is marked or not, and marks it
     * @param res - the sample result to check
     * @return <code>true</code> if the result was marked
     */
    private boolean isResultMarked(SampleResult res) {
        String filename = getFilename();
        return res.markFile(filename);
    }

    private void initializeFileOutput() throws IOException {

        String filename = getFilename();
        if (filename != null) {
            if (out == null) {
                try {
                    out = getFileWriter(filename, getSaveConfig());
                } catch (FileNotFoundException e) {
                    out = null;
                }
            }
        }
    }

    private void finalizeFileOutput() {
        for(Map.Entry<String,ResultCollector.FileEntry> me : files.entrySet()){
            log.debug("Closing: "+me.getKey());
            FileEntry fe = me.getValue();
            writeFileEnd(fe.pw, fe.config);
            fe.pw.close();
            if (fe.pw.checkError()){
                log.warn("Problem detected during use of "+me.getKey());
            }
        }
        files.clear();
    }

    /**
     * {@inheritDoc}
     */
    public void testIterationStart(LoopIterationEvent event) {
    }

    /**
     * @return Returns the saveConfig.
     */
    public SampleSaveConfiguration getSaveConfig() {
        try {
            return (SampleSaveConfiguration) getProperty(SAVE_CONFIG).getObjectValue();
        } catch (ClassCastException e) {
            setSaveConfig(new SampleSaveConfiguration());
            return getSaveConfig();
        }
    }

    /**
     * @param saveConfig
     *            The saveConfig to set.
     */
    public void setSaveConfig(SampleSaveConfiguration saveConfig) {
        getProperty(SAVE_CONFIG).setObjectValue(saveConfig);
    }

    // This is required so that
    // @see org.apache.jmeter.gui.tree.JMeterTreeModel.getNodesOfType()
    // can find the Clearable nodes - the userObject has to implement the interface.
    public void clearData() {
    }
}