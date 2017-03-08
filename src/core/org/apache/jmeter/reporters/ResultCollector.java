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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.util.JMeterError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all saving of samples.
 * The class must be thread-safe because it is shared between threads (NoThreadClone).
 */
public class ResultCollector extends AbstractListenerElement implements SampleListener, Clearable, Serializable,
        TestStateListener, Remoteable, NoThreadClone {
    /**
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
    
    private static final class ShutdownHook implements Runnable {

        @Override
        public void run() {
            log.info("Shutdown hook started");
            synchronized (LOCK) {
                flushFileOutput();                    
            }
            log.info("Shutdown hook ended");
        }     
    }
    private static final Logger log = LoggerFactory.getLogger(ResultCollector.class);

    private static final long serialVersionUID = 234L;

    // This string is used to identify local test runs, so must not be a valid host name
    private static final String TEST_IS_LOCAL = "*local*"; // $NON-NLS-1$

    private static final String TESTRESULTS_START = "<testResults>"; // $NON-NLS-1$

    private static final String TESTRESULTS_START_V1_1_PREVER = "<testResults version=\"";  // $NON-NLS-1$

    private static final String TESTRESULTS_START_V1_1_POSTVER="\">"; // $NON-NLS-1$

    private static final String TESTRESULTS_END = "</testResults>"; // $NON-NLS-1$

    // we have to use version 1.0, see bug 59973
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; // $NON-NLS-1$

    private static final int MIN_XML_FILE_LEN = XML_HEADER.length() + TESTRESULTS_START.length()
            + TESTRESULTS_END.length();

    public static final String FILENAME = "filename"; // $NON-NLS-1$

    private static final String SAVE_CONFIG = "saveConfig"; // $NON-NLS-1$

    private static final String ERROR_LOGGING = "ResultCollector.error_logging"; // $NON-NLS-1$

    private static final String SUCCESS_ONLY_LOGGING = "ResultCollector.success_only_logging"; // $NON-NLS-1$

    /** AutoFlush on each line */
    private static final boolean SAVING_AUTOFLUSH = JMeterUtils.getPropDefault("jmeter.save.saveservice.autoflush", false); //$NON-NLS-1$

    // Static variables

    // Lock used to guard static mutable variables
    private static final Object LOCK = new Object();

    //@GuardedBy("LOCK")
    private static final Map<String, FileEntry> files = new HashMap<>();

    /**
     * Shutdown Hook that ensures PrintWriter is flushed is CTRL+C or kill is called during a test
     */
    //@GuardedBy("LOCK")
    private static Thread shutdownHook;


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

    /**
     * Is a test running ?
     */
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

    /**
     * Constructor which sets the used {@link Summariser}
     * @param summer The {@link Summariser} to use
     */
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

    /**
     * Get the filename of the file this collector uses
     * 
     * @return The name of the file
     */
    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    /**
     * Get the state of error logging
     * 
     * @return Flag whether errors should be logged
     */
    public boolean isErrorLogging() {
        return getPropertyAsBoolean(ERROR_LOGGING);
    }

    /**
     * Sets error logging flag
     * 
     * @param errorLogging
     *            The flag whether errors should be logged
     */
    public final void setErrorLogging(boolean errorLogging) {
        setProperty(new BooleanProperty(ERROR_LOGGING, errorLogging));
    }

    /**
     * Sets the flag whether only successful samples should be logged
     * 
     * @param value
     *            The flag whether only successful samples should be logged
     */
    public final void setSuccessOnlyLogging(boolean value) {
        if (value) {
            setProperty(new BooleanProperty(SUCCESS_ONLY_LOGGING, true));
        } else {
            removeProperty(SUCCESS_ONLY_LOGGING);
        }
    }

    /**
     * Get the state of successful only logging
     * 
     * @return Flag whether only successful samples should be logged
     */
    public boolean isSuccessOnlyLogging() {
        return getPropertyAsBoolean(SUCCESS_ONLY_LOGGING,false);
    }

    /**
     * Decides whether or not to a sample is wanted based on:
     * <ul>
     * <li>errorOnly</li>
     * <li>successOnly</li>
     * <li>sample success</li>
     * </ul>
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
     * Decides whether or not to a sample is wanted based on:
     * <ul>
     * <li>errorOnly</li>
     * <li>successOnly</li>
     * <li>sample success</li>
     * </ul>
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

    @Override
    public void testEnded(String host) {
        synchronized(LOCK){
            instanceCount--;
            if (instanceCount <= 0) {
                // No need for the hook now
                // Bug 57088 - prevent (im?)possible NPE
                if (shutdownHook != null) {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                } else {
                    log.warn("Should not happen: shutdownHook==null, instanceCount={}", instanceCount);
                }
                finalizeFileOutput();
                inTest = false;
            }
        }

        if(summariser != null) {
            summariser.testEnded(host);
        }
    }

    @Override
    public void testStarted(String host) {
        synchronized(LOCK){
            if (instanceCount == 0) { // Only add the hook once
                shutdownHook = new Thread(new ShutdownHook());
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }
            instanceCount++;
            try {
                if (out == null) {
                    try {
                        // Note: getFileWriter ignores a null filename
                        out = getFileWriter(getFilename(), getSaveConfig());
                    } catch (FileNotFoundException e) {
                        out = null;
                    }
                }
                if (getVisualizer() != null) {
                    this.isStats = getVisualizer().isStats();
                }
            } catch (Exception e) {
                log.error("Exception occurred while initializing file output.", e);
            }
        }
        inTest = true;

        if(summariser != null) {
            summariser.testStarted(host);
        }
    }

    @Override
    public void testEnded() {
        testEnded(TEST_IS_LOCAL);
    }

    @Override
    public void testStarted() {
        testStarted(TEST_IS_LOCAL);
    }

    /**
     * Loads an existing sample data (JTL) file.
     * This can be one of:
     * <ul>
     *   <li>XStream format</li>
     *   <li>CSV format</li>
     * </ul>
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
            try ( FileReader fr = new FileReader(file); 
                    BufferedReader dataReader = new BufferedReader(fr, 300)){
                // Get the first line, and see if it is XML
                String line = dataReader.readLine();
                dataReader.close();
                if (line == null) {
                    log.warn("{} is empty", filename);
                } else {
                    if (!line.startsWith("<?xml ")){// No, must be CSV //$NON-NLS-1$
                        CSVSaveService.processSamples(filename, visualizer, this);
                        parsedOK = true;
                    } else { // We are processing XML
                        try ( FileInputStream fis = new FileInputStream(file);
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(fis); ){ // Assume XStream
                            SaveService.loadTestResults(bufferedInputStream,
                                    new ResultCollectorHelper(this, visualizer));
                            parsedOK = true;
                        } catch (Exception e) {
                            if (log.isWarnEnabled()) {
                                log.warn("Failed to load {} using XStream. Error was: {}", filename, e.toString());
                            }
                        }
                    }
                }
            } catch (IOException | JMeterError | RuntimeException | OutOfMemoryError e) {
                // FIXME Why do we catch OOM ?
                log.warn("Problem reading JTL file: {}", file);
            } finally {
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

    private static PrintWriter getFileWriter(final String pFilename, SampleSaveConfiguration saveConfig)
            throws IOException {
        if (pFilename == null || pFilename.length() == 0) {
            return null;
        }
        if(log.isDebugEnabled()) {
            log.debug("Getting file: {} in thread {}", pFilename, Thread.currentThread().getName());
        }
        String filename = FileServer.resolveBaseRelativeName(pFilename);
        filename = new File(filename).getCanonicalPath(); // try to ensure uniqueness (Bug 60822)
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
                    if (log.isInfoEnabled()) {
                        log.info("Folder at {} was created", pdir.getAbsolutePath());
                    }
                } // else if might have been created by another process so not a problem
                if (!pdir.exists()){
                    log.warn("Error creating directories for {}", pdir);
                }
            }
            writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(filename,
                    trimmed)), SaveService.getFileEncoding(StandardCharsets.UTF_8.name())), SAVING_AUTOFLUSH);
            if(log.isDebugEnabled()) {
                log.debug("Opened file: {} in thread {}", filename, Thread.currentThread().getName());
            }
            files.put(filename, new FileEntry(writer, saveConfig));
        } else {
            writer = fe.pw;
        }
        if (!trimmed) {
            log.debug("Writing header to file: {}", filename);
            writeFileStart(writer, saveConfig);
        }
        return writer;
    }

    // returns false if the file did not contain the terminator
    private static boolean trimLastLine(String filename) {
        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")){ // $NON-NLS-1$
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
                log.warn("Unexpected EOF trying to find XML end marker in {}", filename);
                return false;
            }
            raf.setLength(pos + end);// Truncate the file
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Error trying to find XML terminator. {}", e.toString());
            }
            return false;
        }
        return true;
    }

    @Override
    public void sampleStarted(SampleEvent e) {
    }

    @Override
    public void sampleStopped(SampleEvent e) {
    }

    /**
     * When a test result is received, display it and save it.
     *
     * @param event
     *            the sample event that was received
     */
    @Override
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
     * Checks if the sample result is marked or not, and marks it
     * @param res - the sample result to check
     * @return <code>true</code> if the result was marked
     */
    private boolean isResultMarked(SampleResult res) {
        String filename = getFilename();
        return res.markFile(filename);
    }

    /**
     * Flush PrintWriter to synchronize file contents
     */
    public void flushFile() {
        if (out != null) {
            log.info("forced flush through ResultCollector#flushFile");
            out.flush();
        }
    }

    /**
     * Flush PrintWriter, called by Shutdown Hook to ensure no data is lost
     */
    private static void flushFileOutput() {
        String key;
        ResultCollector.FileEntry value;
        for(Map.Entry<String, ResultCollector.FileEntry> me : files.entrySet()) {
            key = me.getKey();
            value = me.getValue();
            log.debug("Flushing: {}", key);
            value.pw.flush();
            if (value.pw.checkError()){
                log.warn("Problem detected during use of {}", key);
            }
        }
    }
    
    private void finalizeFileOutput() {
        String key;
        ResultCollector.FileEntry value;
        for(Map.Entry<String, ResultCollector.FileEntry> me : files.entrySet()) {
            key = me.getKey();
            value = me.getValue();
            log.debug("Closing: {}", key);
            writeFileEnd(value.pw, value.config);
            value.pw.close();
            if (value.pw.checkError()){
                log.warn("Problem detected during use of {}", key);
            }
        }
        files.clear();
        out = null;
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
    @Override
    public void clearData() {
    }
}
