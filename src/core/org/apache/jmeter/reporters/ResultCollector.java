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

package org.apache.jmeter.reporters;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.OldSaveService;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.save.TestResultWrapper;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.xml.sax.SAXException;

/**
 */
public class ResultCollector extends AbstractListenerElement implements SampleListener, Clearable, Serializable,
		TestListener, Remoteable, NoThreadClone {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private static final long serialVersionUID = 2;

	private static final String TESTRESULTS_START = "<testResults>"; // $NON-NLS-1$

	private static final String TESTRESULTS_START_V1_1_PREVER = "<testResults version=\"";  // $NON-NLS-1$
        private static final String TESTRESULTS_START_V1_1_POSTVER="\">"; // $NON-NLS-1$

	private static final String TESTRESULTS_END = "</testResults>"; // $NON-NLS-1$

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; // $NON-NLS-1$

	private static final int MIN_XML_FILE_LEN = XML_HEADER.length() + TESTRESULTS_START.length()
			+ TESTRESULTS_END.length();

	public final static String FILENAME = "filename"; // $NON-NLS-1$

	private final static String SAVE_CONFIG = "saveConfig"; // $NON-NLS-1$

	private static final String ERROR_LOGGING = "ResultCollector.error_logging"; // $NON-NLS-1$

	// protected List results = Collections.synchronizedList(new ArrayList());
	// private int current;
	transient private DefaultConfigurationSerializer serializer;

	// private boolean inLoading = false;
	transient private volatile PrintWriter out;

	private boolean inTest = false;

	private static Map files = new HashMap();

	private Set hosts = new HashSet();

	protected boolean isStats = false;

	/**
	 * No-arg constructor.
	 */
	public ResultCollector() {
		// current = -1;
		// serializer = new DefaultConfigurationSerializer();
		setErrorLogging(false);
		setProperty(new ObjectProperty(SAVE_CONFIG, new SampleSaveConfiguration()));
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

	public void setErrorLogging(boolean errorLogging) {
		setProperty(new BooleanProperty(ERROR_LOGGING, errorLogging));
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
		hosts.remove(host);
		if (hosts.size() == 0) {
			finalizeFileOutput();
			inTest = false;
		}
	}

	public void testStarted(String host) {
		hosts.add(host);
		try {
			initializeFileOutput();
			if (getVisualizer() != null) {
				this.isStats = getVisualizer().isStats();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		inTest = true;
	}

	public void testEnded() {
		testEnded("local"); // $NON-NLS-1$
	}

	public void testStarted() {
		testStarted("local"); // $NON-NLS-1$
	}

	public void loadExistingFile() throws IOException {
		// inLoading = true;
		boolean parsedOK = false;
		if (new File(getFilename()).exists()) {
			clearVisualizer();
			BufferedReader dataReader = null;
            BufferedInputStream bufferedInputStream = null;
			try {
                bufferedInputStream = new BufferedInputStream(new FileInputStream(getFilename()));
                readSamples(SaveService.loadTestResults(bufferedInputStream));
				parsedOK = true;
			} catch (Exception e) {
				log.warn("File load failure, trying old data format.");
				try {
					Configuration savedSamples = getConfiguration(getFilename());
					Configuration[] samples = savedSamples.getChildren();
					for (int i = 0; i < samples.length; i++) {
						SampleResult result = OldSaveService.getSampleResult(samples[i]);
						sendToVisualizer(result);
					}
					parsedOK = true;
				} catch (Exception e1) {
					log.warn("Error parsing XML results " + e);
					log.info("Assuming CSV format instead");
					dataReader = new BufferedReader(new FileReader(getFilename()));
					String line;
					while ((line = dataReader.readLine()) != null) {
						sendToVisualizer(OldSaveService.makeResultFromDelimitedString(line));
					}
					parsedOK = true;
				}
			} finally {
				if (dataReader != null)
					dataReader.close();
                if (bufferedInputStream != null)
                    bufferedInputStream.close();
				if (!parsedOK) {
					SampleResult sr = new SampleResult();
					sr.setSampleLabel("Error loading results file - see log file");
					sendToVisualizer(sr);
				}
			}
		}
		// inLoading = false;
	}

	private static void writeFileStart(PrintWriter writer, SampleSaveConfiguration saveConfig) {
		if (saveConfig.saveAsXml()) {
			writer.println(XML_HEADER);
            String pi=saveConfig.getXmlPi();
            if (pi.length() > 0) {
                writer.println(pi);
            }
            // Can't do it as a static initialisation, because SaveService 
            // is being constructed when this is called
			writer.print(TESTRESULTS_START_V1_1_PREVER);
            writer.print(SaveService.getVERSION());
            writer.println(TESTRESULTS_START_V1_1_POSTVER);
		} else if (saveConfig.saveFieldNames()) {
			writer.println(OldSaveService.printableFieldNamesToString(saveConfig));
		}
	}

	private static void writeFileEnd(PrintWriter pw, SampleSaveConfiguration saveConfig) {
		if (saveConfig.saveAsXml()) {
			pw.print("\n"); // $NON-NLS-1$
			pw.print(TESTRESULTS_END);
			pw.print("\n");// Added in version 1.1 // $NON-NLS-1$
		}
	}

	private static synchronized PrintWriter getFileWriter(String filename, SampleSaveConfiguration saveConfig)
			throws IOException {
		if (filename == null || filename.length() == 0) {
			return null;
		}
		PrintWriter writer = (PrintWriter) files.get(filename);
		boolean trimmed = true;

		if (writer == null) {
			if (saveConfig.saveAsXml()) {
				trimmed = trimLastLine(filename);
			} else {
				trimmed = new File(filename).exists();
			}
			// Find the name of the directory containing the file
			// and create it - if there is one
			File pdir = new File(filename).getParentFile();
			if (pdir != null)
				pdir.mkdirs();
			writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(filename,
					trimmed)), "UTF-8"), true); // $NON-NLS-1$
			files.put(filename, writer);
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
			raf.seek(len - TESTRESULTS_END.length() - 10);// TODO: may not
															// work on all OSes?
			String line;
			long pos = raf.getFilePointer();
			int end = 0;
			while ((line = raf.readLine()) != null)// reads to end of line OR
													// file
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
				if (raf != null)
					raf.close();
			} catch (IOException e1) {
				log.info("Could not close " + filename + " " + e1.getLocalizedMessage());
			}
		}
		return true;
	}

	private void readSamples(TestResultWrapper testResults) throws Exception {
		Collection samples = testResults.getSampleResults();
		Iterator iter = samples.iterator();
		while (iter.hasNext()) {
			SampleResult result = (SampleResult) iter.next();
			sendToVisualizer(result);
		}
	}

	/**
	 * Gets the configuration attribute of the ResultCollector object.
	 * 
	 * @return the configuration value
	 */
	private Configuration getConfiguration(String filename) throws SAXException, IOException, ConfigurationException {
		DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

		return builder.buildFromFile(filename);
	}

	public void clearVisualizer() {
		// current = -1;
		if (getVisualizer() != null && getVisualizer() instanceof Clearable) {
			((Clearable) getVisualizer()).clear();
		}
		finalizeFileOutput();
	}

	public void sampleStarted(SampleEvent e) {
	}

	public void sampleStopped(SampleEvent e) {
	}

	/**
	 * When a test result is received, display it and save it.
	 * 
	 * @param e
	 *            the sample event that was received
	 */
	public void sampleOccurred(SampleEvent e) {
		SampleResult result = e.getResult();

		if (!isErrorLogging() || !result.isSuccessful()) {
			sendToVisualizer(result);

			SampleSaveConfiguration config = getSaveConfig();
			result.setSaveConfig(config);

			try {
				if (!config.saveAsXml()) {
					if (out != null) {
						String savee = OldSaveService.resultToDelimitedString(result);
						out.println(savee);
					}
				}
				// Save results as XML
				else {
					recordResult(result);
				}
			} catch (Exception err) {
				log.error("", err); // should throw exception back to caller
			}
		}
	}

	protected void sendToVisualizer(SampleResult r) {
		if (getVisualizer() != null) {
			getVisualizer().add(r);
		}
	}

	private void recordResult(SampleResult result) throws Exception {
		if (out != null) {
			if (!isResultMarked(result) && !this.isStats) {
				if (SaveService.isSaveTestLogFormat20()) {
					if (serializer == null)
						serializer = new DefaultConfigurationSerializer();
					out.write(getSerializedSampleResult(result));
				} else {
					SaveService.saveSampleResult(result, out);
				}
			}
		}
	}

	private String getSerializedSampleResult(SampleResult result) throws SAXException, IOException,
			ConfigurationException {
		ByteArrayOutputStream tempOut = new ByteArrayOutputStream();

		serializer.serialize(tempOut, OldSaveService.getConfiguration(result, getSaveConfig()));
		String serVer = tempOut.toString();
        String lineSep=System.getProperty("line.separator"); // $NON-NLS-1$
        /*
         * Remove the <?xml ... ?> prefix.
         * When using the x-jars (xakan etc) or Java 1.4, the serialised output has a 
         * newline after the prefix. However, when using Java 1.5 without the x-jars, the output
         * has no newline at all.
         */
		int index = serVer.indexOf(lineSep); // Is there a new-line?
		if (index > -1) {// Yes, assume it follows the prefix
			return serVer.substring(index);
		}
		if (serVer.startsWith("<?xml")){ // $NON-NLS-1$
		    index=serVer.indexOf("?>");// must exist // $NON-NLS-1$
		    return lineSep + serVer.substring(index+2);// +2 for ?>
		}
		return serVer;
	}

	/**
	 * recordStats is used to save statistics generated by visualizers
	 * 
	 * @param e
	 * @throws Exception
	 */
	public void recordStats(TestElement e) throws Exception {
		if (out == null) {
			initializeFileOutput();
		}
		if (out != null) {
			SaveService.saveTestElement(e, out);
		}
	}

	private synchronized boolean isResultMarked(SampleResult res) {
		String filename = getFilename();
		boolean marked = res.isMarked(filename);

		if (!marked) {
			res.setMarked(filename);
		}
		return marked;
	}

	private void initializeFileOutput() throws IOException {

		String filename = getFilename();
		if (out == null && filename != null) {
			if (out == null) {
				try {
					out = getFileWriter(filename, getSaveConfig());
				} catch (FileNotFoundException e) {
					out = null;
				}
			}
		}
	}

	private synchronized void finalizeFileOutput() {
		if (out != null) {
			writeFileEnd(out, getSaveConfig());
			out.close();
			files.remove(getFilename());
			out = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TestListener#testIterationStart(LoopIterationEvent)
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
}