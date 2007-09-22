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

package org.apache.jmeter.save;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.samplers.StatisticalSampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.NameUpdater;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.jorphan.util.JMeterError;
import org.apache.log.Logger;
import org.xml.sax.SAXException;

/**
 * This class provides a means for saving test results. Test results are
 * typically saved in an XML file, but other storage mechanisms may also be
 * used, for instance, CSV files or databases.
 * 
 * @version $Revision$ $Date$
 */
public final class OldSaveService {
	private static final Logger log = LoggingManager.getLoggerForClass();

    // ---------------------------------------------------------------------
    // XML RESULT FILE CONSTANTS AND FIELD NAME CONSTANTS
    // ---------------------------------------------------------------------

    // Shared with TestElementSaver
    final static String PRESERVE = "preserve"; // $NON-NLS-1$
    final static String XML_SPACE = "xml:space"; // $NON-NLS-1$

    private static final String ASSERTION_RESULT_TAG_NAME = "assertionResult"; // $NON-NLS-1$
    private static final String BINARY = "binary"; // $NON-NLS-1$
    private static final String DATA_TYPE = "dataType"; // $NON-NLS-1$
    private static final String ERROR = "error"; // $NON-NLS-1$
    private static final String FAILURE = "failure"; // $NON-NLS-1$
    private static final String FAILURE_MESSAGE = "failureMessage"; // $NON-NLS-1$
    private static final String LABEL = "label"; // $NON-NLS-1$
    private static final String RESPONSE_CODE = "responseCode"; // $NON-NLS-1$
    private static final String RESPONSE_MESSAGE = "responseMessage"; // $NON-NLS-1$
    private static final String SAMPLE_RESULT_TAG_NAME = "sampleResult"; // $NON-NLS-1$
    private static final String SUCCESSFUL = "success"; // $NON-NLS-1$
    private static final String THREAD_NAME = "threadName"; // $NON-NLS-1$
    private static final String TIME = "time"; // $NON-NLS-1$
    private static final String TIME_STAMP = "timeStamp"; // $NON-NLS-1$

    // ---------------------------------------------------------------------
    // ADDITIONAL CSV RESULT FILE CONSTANTS AND FIELD NAME CONSTANTS
    // ---------------------------------------------------------------------

    private static final String CSV_ELAPSED = "elapsed"; // $NON-NLS-1$
    private static final String CSV_BYTES= "bytes"; // $NON-NLS-1$
    private static final String CSV_THREAD_COUNT1 = "grpThreads"; // $NON-NLS-1$
    private static final String CSV_THREAD_COUNT2 = "allThreads"; // $NON-NLS-1$
    private static final String CSV_SAMPLE_COUNT = "SampleCount"; // $NON-NLS-1$
    private static final String CSV_ERROR_COUNT = "ErrorCount"; // $NON-NLS-1$
    private static final String CSV_URL = "URL"; // $NON-NLS-1$
    private static final String CSV_FILENAME = "Filename"; // $NON-NLS-1$
    private static final String CSV_LATENCY = "Latency"; // $NON-NLS-1$
    private static final String CSV_ENCODING = "Encoding"; // $NON-NLS-1$
    
    // Initial config from properties
	static private final SampleSaveConfiguration _saveConfig = SampleSaveConfiguration.staticConfig();

	// Date format to try if the time format does not parse as milliseconds
	// (this is the suggested value in jmeter.properties)
	private static final String DEFAULT_DATE_FORMAT_STRING = "MM/dd/yy HH:mm:ss"; // $NON-NLS-1$
	private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STRING);

	private static DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

	/**
	 * Private constructor to prevent instantiation.
	 */
	private OldSaveService() {
	}

    //////////////////////////////////////////////////////////////////////////////
    //                  Start of CSV methods
    
    // TODO - move to separate file? If so, remember that some of the
      
    /**
     * Make a SampleResult given a delimited string.
     * 
     * @param inputLine - line from CSV file
     * @param saveConfig - configuration
     * @param lineNumber - line number for error reporting
     * @return SampleResult or null if header line detected
     * 
     * @throws JMeterError
     */
    public static SampleResult makeResultFromDelimitedString(
    		final String inputLine, 
    		final SampleSaveConfiguration saveConfig, // may be updated
    		final long lineNumber) {
 
    	SampleResult result = null;
		long timeStamp = 0;
		long elapsed = 0;
		/*
		 * Bug 40772: replaced StringTokenizer with String.split(), as the
		 * former does not return empty tokens.
		 */
		// The \Q prefix is needed to ensure that meta-characters (e.g. ".") work.
		String parts[]=inputLine.split("\\Q"+_saveConfig.getDelimiter());// $NON-NLS-1$
		String text = null;
		String field = null; // Save the name for error reporting
		int i=0;

		try {
			if (saveConfig.saveTimestamp()){
				field = TIME_STAMP;
				text = parts[i++];
				if (saveConfig.printMilliseconds()) {
					try {
						timeStamp = Long.parseLong(text);
					} catch (NumberFormatException e) {// see if this works
						log.warn(e.toString());
						Date stamp = DEFAULT_DATE_FORMAT.parse(text);
						timeStamp = stamp.getTime();
						log.warn("Setting date format to: "+DEFAULT_DATE_FORMAT_STRING);
						saveConfig.setFormatter(DEFAULT_DATE_FORMAT);
					}
				} else if (saveConfig.formatter() != null) {
					Date stamp = saveConfig.formatter().parse(text);
					timeStamp = stamp.getTime();
				} else { // can this happen?
					final String msg = "Unknown timestamp format";
					log.warn(msg);
					throw new JMeterError(msg);
				}
			}

			if (saveConfig.saveTime()) {
				field = CSV_ELAPSED;
				text = parts[i++];
				elapsed = Long.parseLong(text);
			}

			if (saveConfig.saveSampleCount()) {
				result = new StatisticalSampleResult(timeStamp, elapsed);
			} else {
				result = new SampleResult(timeStamp, elapsed);
			}

			if (saveConfig.saveLabel()) {
				field = LABEL;
				text = parts[i++];
				result.setSampleLabel(text);
			}
			if (saveConfig.saveCode()) {
				field = RESPONSE_CODE;
				text = parts[i++];
				result.setResponseCode(text);
			}

			if (saveConfig.saveMessage()) {
				field = RESPONSE_MESSAGE;
				text = parts[i++];
				result.setResponseMessage(text);
			}

			if (saveConfig.saveThreadName()) {
				field = THREAD_NAME;
				text = parts[i++];
				result.setThreadName(text);
			}

			if (saveConfig.saveDataType()) {
				field = DATA_TYPE;
				text = parts[i++];
				result.setDataType(text);
			}

			if (saveConfig.saveSuccess()) {
				field = SUCCESSFUL;
				text = parts[i++];
				result.setSuccessful(Boolean.valueOf(text).booleanValue());
			}

			if (saveConfig.saveAssertionResultsFailureMessage()) {
				i++;
                // TODO - should this be restored?
			}
            
            if (saveConfig.saveBytes()) {
            	field = CSV_BYTES;
                text = parts[i++];
                result.setBytes(Integer.parseInt(text));
            }
        
            if (saveConfig.saveThreadCounts()) {
                i+=2;// two counts
                // Not saved, as not part of a result
            }

            if (saveConfig.saveUrl()) {
                i++;
                // TODO: should this be restored?
            }
        
            if (saveConfig.saveFileName()) {
            	field = CSV_FILENAME;
                text = parts[i++];
                result.setResultFileName(text);
            }            
            if (saveConfig.saveLatency()) {
            	field = CSV_LATENCY;
                text = parts[i++];
                result.setLatency(Long.parseLong(text));
            }

            if (saveConfig.saveEncoding()) {
            	field = CSV_ENCODING;
                text = parts[i++];
                result.setEncodingAndType(text);
            }

            if (saveConfig.saveSampleCount()) {
            	field = CSV_SAMPLE_COUNT;
                text = parts[i++];
                result.setSampleCount(Integer.parseInt(text));
            	field = CSV_ERROR_COUNT;
                text = parts[i++];
                result.setErrorCount(Integer.parseInt(text));
            }

            
		} catch (NumberFormatException e) {
			log.warn("Error parsing field '" + field + "' at line " + lineNumber + " " + e);
			throw new JMeterError(e);
		} catch (ParseException e) {
			log.warn("Error parsing field '" + field + "' at line " + lineNumber + " " + e);
			throw new JMeterError(e);
		} catch (ArrayIndexOutOfBoundsException e){
			log.warn("Insufficient columns to parse field '" + field + "' at line " + lineNumber);
			throw new JMeterError(e);
		}
		return result;
	}

    /**
     * Generates the field names for the output file
     * 
     * @return the field names as a string
     */
    public static String printableFieldNamesToString() {
        return printableFieldNamesToString(_saveConfig);
    }
    
	/**
	 * Generates the field names for the output file
	 * 
	 * @return the field names as a string
	 */
	public static String printableFieldNamesToString(SampleSaveConfiguration saveConfig) {
		StringBuffer text = new StringBuffer();
		String delim = saveConfig.getDelimiter();

		if (saveConfig.saveTimestamp()) {
			text.append(TIME_STAMP);
			text.append(delim);
		}

		if (saveConfig.saveTime()) {
			text.append(CSV_ELAPSED);
			text.append(delim);
		}

		if (saveConfig.saveLabel()) {
			text.append(LABEL);
			text.append(delim);
		}

		if (saveConfig.saveCode()) {
			text.append(RESPONSE_CODE);
			text.append(delim);
		}

		if (saveConfig.saveMessage()) {
			text.append(RESPONSE_MESSAGE);
			text.append(delim);
		}

		if (saveConfig.saveThreadName()) {
			text.append(THREAD_NAME);
			text.append(delim);
		}

		if (saveConfig.saveDataType()) {
			text.append(DATA_TYPE);
			text.append(delim);
		}

		if (saveConfig.saveSuccess()) {
			text.append(SUCCESSFUL);
			text.append(delim);
		}

		if (saveConfig.saveAssertionResultsFailureMessage()) {
			text.append(FAILURE_MESSAGE);
			text.append(delim);
		}

        if (saveConfig.saveBytes()) {
            text.append(CSV_BYTES);
            text.append(delim);
        }

        if (saveConfig.saveThreadCounts()) {
            text.append(CSV_THREAD_COUNT1);
            text.append(delim);
            text.append(CSV_THREAD_COUNT2);
            text.append(delim);
        }

        if (saveConfig.saveUrl()) {
            text.append(CSV_URL);
            text.append(delim);
        }

        if (saveConfig.saveFileName()) {
            text.append(CSV_FILENAME);
            text.append(delim);
        }

        if (saveConfig.saveLatency()) {
            text.append(CSV_LATENCY);
            text.append(delim);
        }

        if (saveConfig.saveEncoding()) {
            text.append(CSV_ENCODING);
            text.append(delim);
        }

		if (saveConfig.saveSampleCount()) {
			text.append(CSV_SAMPLE_COUNT);
			text.append(delim);
			text.append(CSV_ERROR_COUNT);
			text.append(delim);
		}

		String resultString = null;
		int size = text.length();
		int delSize = delim.length();

		// Strip off the trailing delimiter
		if (size >= delSize) {
			resultString = text.substring(0, size - delSize);
		} else {
			resultString = text.toString();
		}
		return resultString;
	}
	
	// Map header names to set() methods
	private static final Map headerLabelMethods = new HashMap();
	
	static {
		    headerLabelMethods.put(TIME_STAMP, new Functor("setTimestamp"));
			headerLabelMethods.put(CSV_ELAPSED, new Functor("setTime"));
			headerLabelMethods.put(LABEL, new Functor("setLabel"));
			headerLabelMethods.put(RESPONSE_CODE, new Functor("setCode"));
			headerLabelMethods.put(RESPONSE_MESSAGE, new Functor("setMessage"));
			headerLabelMethods.put(THREAD_NAME, new Functor("setThreadName"));
			headerLabelMethods.put(DATA_TYPE, new Functor("setDataType"));
			headerLabelMethods.put(SUCCESSFUL, new Functor("setSuccess"));
			headerLabelMethods.put(FAILURE_MESSAGE, new Functor("setAssertionResultsFailureMessage"));
            headerLabelMethods.put(CSV_BYTES, new Functor("setBytes"));
            headerLabelMethods.put(CSV_URL, new Functor("setUrl"));
            headerLabelMethods.put(CSV_FILENAME, new Functor("setFileName"));
            headerLabelMethods.put(CSV_LATENCY, new Functor("setLatency"));
            headerLabelMethods.put(CSV_ENCODING, new Functor("setEncoding"));
            // Both these are needed in the list even though they set the same variable
            headerLabelMethods.put(CSV_THREAD_COUNT1,new Functor("setThreadCounts"));
            headerLabelMethods.put(CSV_THREAD_COUNT2,new Functor("setThreadCounts"));
            // Both these are needed in the list even though they set the same variable
            headerLabelMethods.put(CSV_SAMPLE_COUNT, new Functor("setSampleCount"));
            headerLabelMethods.put(CSV_ERROR_COUNT, new Functor("setSampleCount"));
	}

	/**
	 * Parse a CSV header line
	 * @param headerLine from CSV file
	 * @return config corresponding to the header items found or null if not a header line
	 */
	public static SampleSaveConfiguration getSampleSaveConfiguration(String headerLine){
		String parts[]=headerLine.split("\\Q"+_saveConfig.getDelimiter());// $NON-NLS-1$

		// Check if the line is a header
		for(int i=0;i<parts.length;i++){
			if (!headerLabelMethods.containsKey(parts[i])){
				return null; // unknown column name
			}
		}

		// We know the column names all exist, so create the config 
		SampleSaveConfiguration saveConfig=new SampleSaveConfiguration(false);
		
		for(int i=0;i<parts.length;i++){
			Functor set = (Functor) headerLabelMethods.get(parts[i]);
			set.invoke(saveConfig,new Boolean[]{Boolean.TRUE});
		}
		return saveConfig;
	}

	/**
     * Method will save aggregate statistics as CSV. For now I put it here.
     * Not sure if it should go in the newer SaveService instead of here.
     * if we ever decide to get rid of this class, we'll need to move this
     * method to the new save service.
     * @param data
     * @param writer
     * @throws IOException
     */
    public static void saveCSVStats(Vector data, FileWriter writer) throws IOException {
        for (int idx=0; idx < data.size(); idx++) {
            Vector row = (Vector)data.elementAt(idx);
            for (int idy=0; idy < row.size(); idy++) {
                if (idy > 0) {
                    writer.write(","); // $NON-NLS-1$
                }
                Object item = row.elementAt(idy);
                writer.write( String.valueOf(item) );
            }
            writer.write(System.getProperty("line.separator")); // $NON-NLS-1$
        }
    }

    /**
     * Convert a result into a string, where the fields of the result are
     * separated by the default delimiter.
     * 
     * @param sample
     *            the test result to be converted
     * @return the separated value representation of the result
     */
    public static String resultToDelimitedString(SampleResult sample) {
    	return resultToDelimitedString(sample, sample.getSaveConfig().getDelimiter());
    }

    /**
     * Convert a result into a string, where the fields of the result are
     * separated by a specified String.
     * 
     * @param sample
     *            the test result to be converted
     * @param delimiter
     *            the separation string
     * @return the separated value representation of the result
     */
    public static String resultToDelimitedString(SampleResult sample, String delimiter) {
    	StringBuffer text = new StringBuffer();
    	SampleSaveConfiguration saveConfig = sample.getSaveConfig();
    
    	if (saveConfig.saveTimestamp()) {
    		if (saveConfig.printMilliseconds()){
    			text.append(sample.getTimeStamp());
    			text.append(delimiter);
    		} else if (saveConfig.formatter() != null) {
    			String stamp = saveConfig.formatter().format(new Date(sample.getTimeStamp()));
    			text.append(stamp);
    			text.append(delimiter);
    		}
    	}
    
    	if (saveConfig.saveTime()) {
    		text.append(sample.getTime());
    		text.append(delimiter);
    	}
    
    	if (saveConfig.saveLabel()) {
    		text.append(sample.getSampleLabel());
    		text.append(delimiter);
    	}
    
    	if (saveConfig.saveCode()) {
    		text.append(sample.getResponseCode());
    		text.append(delimiter);
    	}
    
    	if (saveConfig.saveMessage()) {
    		text.append(sample.getResponseMessage());
    		text.append(delimiter);
    	}
    
    	if (saveConfig.saveThreadName()) {
    		text.append(sample.getThreadName());
    		text.append(delimiter);
    	}
    
    	if (saveConfig.saveDataType()) {
    		text.append(sample.getDataType());
    		text.append(delimiter);
    	}
    
    	if (saveConfig.saveSuccess()) {
    		text.append(sample.isSuccessful());
    		text.append(delimiter);
    	}
    
    	if (saveConfig.saveAssertionResultsFailureMessage()) {
    		String message = null;
    		AssertionResult[] results = sample.getAssertionResults();
    
    		if (results != null) {
    			// Find the first non-null message
    			for (int i = 0; i < results.length; i++){
        			message = results[i].getFailureMessage();
    				if (message != null) break;
    			}
    		}
    
    		if (message != null) {
    			text.append(message);
    		}
    		text.append(delimiter);
    	}
    
        if (saveConfig.saveBytes()) {
            text.append(sample.getBytes());
            text.append(delimiter);
        }
    
        if (saveConfig.saveThreadCounts()) {
        	org.apache.jmeter.threads.ThreadGroup 
        	threadGroup=JMeterContextService.getContext().getThreadGroup();
        	int numThreads =0;
        	if (threadGroup != null) { // can be null for remote testing
        	    numThreads = threadGroup.getNumberOfThreads();
        	}
            text.append(numThreads);
            text.append(delimiter);
            text.append(JMeterContextService.getNumberOfThreads());
            text.append(delimiter);
        }
        if (saveConfig.saveUrl()) {
            text.append(sample.getURL());
            text.append(delimiter);
        }
    
        if (saveConfig.saveFileName()) {
            text.append(sample.getResultFileName());
            text.append(delimiter);
        }
    
        if (saveConfig.saveLatency()) {
            text.append(sample.getLatency());
            text.append(delimiter);
        }

        if (saveConfig.saveEncoding()) {
            text.append(sample.getDataEncoding());
            text.append(delimiter);
        }

    	if (saveConfig.saveSampleCount()) {// Need both sample and error count to be any use
    		text.append(sample.getSampleCount());
    		text.append(delimiter);
    		text.append(sample.getErrorCount());
    		text.append(delimiter);
    	}
    
    	String resultString = null;
    	int size = text.length();
    	int delSize = delimiter.length();
    
    	// Strip off the trailing delimiter
    	if (size >= delSize) {
    		resultString = text.substring(0, size - delSize);
    	} else {
    		resultString = text.toString();
    	}
    	return resultString;
    }

    //              End of CSV methods
    //////////////////////////////////////////////////////////////////////////////////////////
    //              Start of Avalon methods
    
    public static void saveSubTree(HashTree subTree, OutputStream writer) throws IOException {
		Configuration config = (Configuration) getConfigsFromTree(subTree).get(0);
		DefaultConfigurationSerializer saver = new DefaultConfigurationSerializer();

		saver.setIndent(true);
		try {
			saver.serialize(writer, config);
		} catch (SAXException e) {
			throw new IOException("SAX implementation problem");
		} catch (ConfigurationException e) {
			throw new IOException("Problem using Avalon Configuration tools");
		}
	}
    
    public static SampleResult getSampleResult(Configuration config) {
		SampleResult result = new SampleResult(config.getAttributeAsLong(TIME_STAMP, 0L), config.getAttributeAsLong(
				TIME, 0L));

		result.setThreadName(config.getAttribute(THREAD_NAME, "")); // $NON-NLS-1$
		result.setDataType(config.getAttribute(DATA_TYPE, ""));
		result.setResponseCode(config.getAttribute(RESPONSE_CODE, "")); // $NON-NLS-1$
		result.setResponseMessage(config.getAttribute(RESPONSE_MESSAGE, "")); // $NON-NLS-1$
		result.setSuccessful(config.getAttributeAsBoolean(SUCCESSFUL, false));
		result.setSampleLabel(config.getAttribute(LABEL, "")); // $NON-NLS-1$
		result.setResponseData(getBinaryData(config.getChild(BINARY)));
		Configuration[] subResults = config.getChildren(SAMPLE_RESULT_TAG_NAME);

		for (int i = 0; i < subResults.length; i++) {
			result.storeSubResult(getSampleResult(subResults[i]));
		}
		Configuration[] assResults = config.getChildren(ASSERTION_RESULT_TAG_NAME);

		for (int i = 0; i < assResults.length; i++) {
			result.addAssertionResult(getAssertionResult(assResults[i]));
		}

		Configuration[] samplerData = config.getChildren("property"); // $NON-NLS-1$
		for (int i = 0; i < samplerData.length; i++) {
			result.setSamplerData(samplerData[i].getValue("")); // $NON-NLS-1$
		}
		return result;
	}

	private static List getConfigsFromTree(HashTree subTree) {
		Iterator iter = subTree.list().iterator();
		List configs = new LinkedList();

		while (iter.hasNext()) {
			TestElement item = (TestElement) iter.next();
			DefaultConfiguration config = new DefaultConfiguration("node", "node"); // $NON-NLS-1$ // $NON-NLS-2$

			config.addChild(getConfigForTestElement(null, item));
			List configList = getConfigsFromTree(subTree.getTree(item));
			Iterator iter2 = configList.iterator();

			while (iter2.hasNext()) {
				config.addChild((Configuration) iter2.next());
			}
			configs.add(config);
		}
		return configs;
	}

	public static Configuration getConfiguration(byte[] bin) {
		DefaultConfiguration config = new DefaultConfiguration(BINARY, "JMeter Save Service"); // $NON-NLS-1$

		try {
			config.setValue(new String(bin, "UTF-8")); // $NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			log.error("", e); // $NON-NLS-1$
		}
		return config;
	}

	public static byte[] getBinaryData(Configuration config) {
		if (config == null) {
			return new byte[0];
		}
		try {
			return config.getValue("").getBytes("UTF-8"); // $NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			return new byte[0];
		}
	}

	public static AssertionResult getAssertionResult(Configuration config) {
		AssertionResult result = new AssertionResult(""); //TODO provide proper name?
		result.setError(config.getAttributeAsBoolean(ERROR, false));
		result.setFailure(config.getAttributeAsBoolean(FAILURE, false));
		result.setFailureMessage(config.getAttribute(FAILURE_MESSAGE, ""));
		return result;
	}

	public static Configuration getConfiguration(AssertionResult assResult) {
		DefaultConfiguration config = new DefaultConfiguration(ASSERTION_RESULT_TAG_NAME, "JMeter Save Service");

		config.setAttribute(FAILURE_MESSAGE, assResult.getFailureMessage());
		config.setAttribute(ERROR, "" + assResult.isError());
		config.setAttribute(FAILURE, "" + assResult.isFailure());
		return config;
	}

	/*
	 * TODO - I think this is used for the original test plan format
	 * It seems to be rather out of date, as many attributes are missing?
	*/
	/**
	 * This method determines the content of the result data that will be
	 * stored.
	 * 
	 * @param result
	 *            the object containing all of the data that has been collected.
	 * @param saveConfig
	 *            the configuration giving the data items to be saved.
	 */
	public static Configuration getConfiguration(SampleResult result, SampleSaveConfiguration saveConfig) {
		DefaultConfiguration config = new DefaultConfiguration(SAMPLE_RESULT_TAG_NAME, "JMeter Save Service"); // $NON-NLS-1$

		if (saveConfig.saveTime()) {
			config.setAttribute(TIME, String.valueOf(result.getTime()));
		}
		if (saveConfig.saveLabel()) {
			config.setAttribute(LABEL, result.getSampleLabel());
		}
		if (saveConfig.saveCode()) {
			config.setAttribute(RESPONSE_CODE, result.getResponseCode());
		}
		if (saveConfig.saveMessage()) {
			config.setAttribute(RESPONSE_MESSAGE, result.getResponseMessage());
		}
		if (saveConfig.saveThreadName()) {
			config.setAttribute(THREAD_NAME, result.getThreadName());
		}
		if (saveConfig.saveDataType()) {
			config.setAttribute(DATA_TYPE, result.getDataType());
		}

		if (saveConfig.printMilliseconds()) {
			config.setAttribute(TIME_STAMP, String.valueOf(result.getTimeStamp()));
		} else if (saveConfig.formatter() != null) {
			String stamp = saveConfig.formatter().format(new Date(result.getTimeStamp()));

			config.setAttribute(TIME_STAMP, stamp);
		}

		if (saveConfig.saveSuccess()) {
			config.setAttribute(SUCCESSFUL, Boolean.toString(result.isSuccessful()));
		}

		SampleResult[] subResults = result.getSubResults();

		if (subResults != null) {
			for (int i = 0; i < subResults.length; i++) {
				config.addChild(getConfiguration(subResults[i], saveConfig));
			}
		}

		AssertionResult[] assResults = result.getAssertionResults();

		if (saveConfig.saveSamplerData(result)) {
			config.addChild(createConfigForString("samplerData", result.getSamplerData())); // $NON-NLS-1$
		}
		if (saveConfig.saveAssertions() && assResults != null) {
			for (int i = 0; i < assResults.length; i++) {
				config.addChild(getConfiguration(assResults[i]));
			}
		}
		if (saveConfig.saveResponseData(result)) {
			config.addChild(getConfiguration(result.getResponseData()));
		}
		return config;
	}

	public static Configuration getConfigForTestElement(String named, TestElement item) {
		TestElementSaver saver = new TestElementSaver(named);
		item.traverse(saver);
		Configuration config = saver.getConfiguration();
		/*
		 * DefaultConfiguration config = new DefaultConfiguration("testelement",
		 * "testelement");
		 * 
		 * if (named != null) { config.setAttribute("name", named); } if
		 * (item.getProperty(TestElement.TEST_CLASS) != null) {
		 * config.setAttribute("class", (String)
		 * item.getProperty(TestElement.TEST_CLASS)); } else {
		 * config.setAttribute("class", item.getClass().getName()); } Iterator
		 * iter = item.getPropertyNames().iterator();
		 * 
		 * while (iter.hasNext()) { String name = (String) iter.next(); Object
		 * value = item.getProperty(name);
		 * 
		 * if (value instanceof TestElement) {
		 * config.addChild(getConfigForTestElement(name, (TestElement) value)); }
		 * else if (value instanceof Collection) {
		 * config.addChild(createConfigForCollection(name, (Collection) value)); }
		 * else if (value != null) { config.addChild(createConfigForString(name,
		 * value.toString())); } }
		 */
		return config;
	}


	private static Configuration createConfigForString(String name, String value) {
		if (value == null) {
			value = "";
		}
		DefaultConfiguration config = new DefaultConfiguration("property", "property");

		config.setAttribute("name", name);
		config.setValue(value);
		config.setAttribute(XML_SPACE, PRESERVE);
		return config;
	}

	public synchronized static HashTree loadSubTree(InputStream in) throws IOException {
		try {
			Configuration config = builder.build(in);
			HashTree loadedTree = generateNode(config);

			return loadedTree;
		} catch (ConfigurationException e) {
			String message = "Problem loading using Avalon Configuration tools";
			log.error(message, e);
			throw new IOException(message);
		} catch (SAXException e) {
			String message = "Problem with SAX implementation";
			log.error(message, e);
			throw new IOException(message);
		}
	}

	public static TestElement createTestElement(Configuration config) throws ConfigurationException,
			ClassNotFoundException, IllegalAccessException, InstantiationException {
		TestElement element = null;

		String testClass = config.getAttribute("class"); // $NON-NLS-1$
		
        String gui_class=""; // $NON-NLS-1$
		Configuration[] children = config.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i].getName().equals("property")) { // $NON-NLS-1$
                if (children[i].getAttribute("name").equals(TestElement.GUI_CLASS)){ // $NON-NLS-1$
                    gui_class=children[i].getValue();
                }
            }  
        }
        
        String newClass = NameUpdater.getCurrentTestName(testClass,gui_class);

        element = (TestElement) Class.forName(newClass).newInstance();

        for (int i = 0; i < children.length; i++) {
			if (children[i].getName().equals("property")) { // $NON-NLS-1$
				try {
                    JMeterProperty prop = createProperty(children[i], newClass);
					if (prop!=null) element.setProperty(prop);
				} catch (Exception ex) {
					log.error("Problem loading property", ex);
					element.setProperty(children[i].getAttribute("name"), ""); // $NON-NLS-1$ // $NON-NLS-2$
				}
			} else if (children[i].getName().equals("testelement")) { // $NON-NLS-1$
				element.setProperty(new TestElementProperty(children[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
						createTestElement(children[i])));
			} else if (children[i].getName().equals("collection")) { // $NON-NLS-1$
				element.setProperty(new CollectionProperty(children[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
                        createCollection(children[i], newClass)));
			} else if (children[i].getName().equals("map")) { // $NON-NLS-1$
				element.setProperty(new MapProperty(children[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
                        createMap(children[i],newClass)));
			}
		}
		return element;
	}

	private static Collection createCollection(Configuration config, String testClass) throws ConfigurationException,
			ClassNotFoundException, IllegalAccessException, InstantiationException {
		Collection coll = (Collection) Class.forName(config.getAttribute("class")).newInstance(); // $NON-NLS-1$ 
		Configuration[] items = config.getChildren();

		for (int i = 0; i < items.length; i++) {
			if (items[i].getName().equals("property")) { // $NON-NLS-1$ 
                JMeterProperty prop = createProperty(items[i], testClass);
				if (prop!=null) coll.add(prop);
			} else if (items[i].getName().equals("testelement")) { // $NON-NLS-1$ 
				coll.add(new TestElementProperty(items[i].getAttribute("name", ""), createTestElement(items[i]))); // $NON-NLS-1$ // $NON-NLS-2$
			} else if (items[i].getName().equals("collection")) { // $NON-NLS-1$ 
				coll.add(new CollectionProperty(items[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
						createCollection(items[i], testClass)));
			} else if (items[i].getName().equals("string")) { // $NON-NLS-1$ 
                JMeterProperty prop = createProperty(items[i], testClass);
				if (prop!=null) coll.add(prop);
			} else if (items[i].getName().equals("map")) { // $NON-NLS-1$ 
				coll.add(new MapProperty(items[i].getAttribute("name", ""), createMap(items[i], testClass))); // $NON-NLS-1$ // $NON-NLS-2$
			}
		}
		return coll;
	}

	private static JMeterProperty createProperty(Configuration config, String testClass) throws IllegalAccessException,
			ClassNotFoundException, InstantiationException {
		String value = config.getValue(""); // $NON-NLS-1$ 
		String name = config.getAttribute("name", value); // $NON-NLS-1$ 
        String oname = name;
		String type = config.getAttribute("propType", StringProperty.class.getName()); // $NON-NLS-1$ 

		// Do upgrade translation:
		name = NameUpdater.getCurrentName(name, testClass);
		if (TestElement.GUI_CLASS.equals(name)) {
			value = NameUpdater.getCurrentName(value);
        } else if (TestElement.TEST_CLASS.equals(name)) {
            value=testClass; // must always agree
		} else {
			value = NameUpdater.getCurrentName(value, name, testClass);
		}

        // Delete any properties whose name converts to the empty string
        if (oname.length() != 0 && name.length()==0) {
            return null;
        }

        // Create the property:
		JMeterProperty prop = (JMeterProperty) Class.forName(type).newInstance();
		prop.setName(name);
		prop.setObjectValue(value);

		return prop;
	}

	private static Map createMap(Configuration config, String testClass) throws ConfigurationException,
			ClassNotFoundException, IllegalAccessException, InstantiationException {
		Map map = (Map) Class.forName(config.getAttribute("class")).newInstance();
		Configuration[] items = config.getChildren();

		for (int i = 0; i < items.length; i++) {
			if (items[i].getName().equals("property")) { // $NON-NLS-1$ 
				JMeterProperty prop = createProperty(items[i], testClass);
				if (prop!=null) map.put(prop.getName(), prop);
			} else if (items[i].getName().equals("testelement")) { // $NON-NLS-1$ 
				map.put(items[i].getAttribute("name", ""), new TestElementProperty(items[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
						createTestElement(items[i])));
			} else if (items[i].getName().equals("collection")) { // $NON-NLS-1$ 
				map.put(items[i].getAttribute("name"),  // $NON-NLS-1$ 
						new CollectionProperty(items[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
						createCollection(items[i], testClass)));
			} else if (items[i].getName().equals("map")) { // $NON-NLS-1$ 
				map.put(items[i].getAttribute("name", ""),  // $NON-NLS-1$ // $NON-NLS-2$
						new MapProperty(items[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
						createMap(items[i], testClass)));
			}
		}
		return map;
	}

	private static HashTree generateNode(Configuration config) {
		TestElement element = null;

		try {
			element = createTestElement(config.getChild("testelement")); // $NON-NLS-1$ 
		} catch (Exception e) {
			log.error("Problem loading part of file", e);
			return null;
		}
		HashTree subTree = new ListedHashTree(element);
		Configuration[] subNodes = config.getChildren("node"); // $NON-NLS-1$ 

		for (int i = 0; i < subNodes.length; i++) {
			HashTree t = generateNode(subNodes[i]);

			if (t != null) {
				subTree.add(element, t);
			}
		}
		return subTree;
	}
}
