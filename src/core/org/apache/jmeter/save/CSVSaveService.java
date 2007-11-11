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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.samplers.StatisticalSampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.jorphan.util.JMeterError;
import org.apache.log.Logger;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * This class provides a means for saving/reading test results as CSV files.
 */
public final class CSVSaveService {
	private static final Logger log = LoggingManager.getLoggerForClass();

    // ---------------------------------------------------------------------
    // XML RESULT FILE CONSTANTS AND FIELD NAME CONSTANTS
    // ---------------------------------------------------------------------

    private static final String DATA_TYPE = "dataType"; // $NON-NLS-1$
    private static final String FAILURE_MESSAGE = "failureMessage"; // $NON-NLS-1$
    private static final String LABEL = "label"; // $NON-NLS-1$
    private static final String RESPONSE_CODE = "responseCode"; // $NON-NLS-1$
    private static final String RESPONSE_MESSAGE = "responseMessage"; // $NON-NLS-1$
    private static final String SUCCESSFUL = "success"; // $NON-NLS-1$
    private static final String THREAD_NAME = "threadName"; // $NON-NLS-1$
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
    private static final String CSV_HOSTNAME = "Hostname"; // $NON-NLS-1$
    
    // Initial config from properties
	static private final SampleSaveConfiguration _saveConfig = SampleSaveConfiguration.staticConfig();

	// Date format to try if the time format does not parse as milliseconds
	// (this is the suggested value in jmeter.properties)
	private static final String DEFAULT_DATE_FORMAT_STRING = "MM/dd/yy HH:mm:ss"; // $NON-NLS-1$
	private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STRING);

	/**
	 * Private constructor to prevent instantiation.
	 */
	private CSVSaveService() {
	}

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
    public static SampleEvent makeResultFromDelimitedString(
    		final String inputLine, 
    		final SampleSaveConfiguration saveConfig, // may be updated
    		final long lineNumber) {
 
    	SampleResult result = null;
        String hostname = "";// $NON-NLS-1$
		long timeStamp = 0;
		long elapsed = 0;
		/*
		 * Bug 40772: replaced StringTokenizer with String.split(), as the
		 * former does not return empty tokens.
		 */
		// The \Q prefix is needed to ensure that meta-characters (e.g. ".") work.
		String parts[]=inputLine.split("\\Q"+saveConfig.getDelimiter());// $NON-NLS-1$
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
            	field = CSV_THREAD_COUNT1;
                text = parts[i++];
                result.setGroupThreads(Integer.parseInt(text));
                
            	field = CSV_THREAD_COUNT2;
                text = parts[i++];
                result.setAllThreads(Integer.parseInt(text));
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

            if (saveConfig.saveHostname()) {
            	field = CSV_HOSTNAME;
                hostname = parts[i++];
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
		return new SampleEvent(result,"",hostname);
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

        if (saveConfig.saveHostname()) {
            text.append(CSV_HOSTNAME);
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
	private static final LinkedMap headerLabelMethods = new LinkedMap();
	
	// These entries must be in the same order as columns are saved/restored.
	
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
            // Both these are needed in the list even though they set the same variable
            headerLabelMethods.put(CSV_THREAD_COUNT1,new Functor("setThreadCounts"));
            headerLabelMethods.put(CSV_THREAD_COUNT2,new Functor("setThreadCounts"));
            headerLabelMethods.put(CSV_URL, new Functor("setUrl"));
            headerLabelMethods.put(CSV_FILENAME, new Functor("setFileName"));
            headerLabelMethods.put(CSV_LATENCY, new Functor("setLatency"));
            headerLabelMethods.put(CSV_ENCODING, new Functor("setEncoding"));
            // Both these are needed in the list even though they set the same variable
            headerLabelMethods.put(CSV_SAMPLE_COUNT, new Functor("setSampleCount"));
            headerLabelMethods.put(CSV_ERROR_COUNT, new Functor("setSampleCount"));
            headerLabelMethods.put(CSV_HOSTNAME, new Functor("setHostname"));
	}

	/**
	 * Parse a CSV header line
	 * @param headerLine from CSV file
	 * @param filename name of file (for log message only)
	 * @return config corresponding to the header items found or null if not a header line
	 */
	public static SampleSaveConfiguration getSampleSaveConfiguration(String headerLine, String filename){
		String[] parts = splitHeader(headerLine,_saveConfig.getDelimiter()); // Try default delimiter

		String delim = null;
		
		if (parts == null){
			Perl5Matcher matcher = JMeterUtils.getMatcher();
			PatternMatcherInput input = new PatternMatcherInput(headerLine);
			Pattern pattern = JMeterUtils.getPatternCache()
			// This assumes the header names are all single words with no spaces
			// word followed by 0 or more repeats of (non-word char + word)
			// where the non-word char (\2) is the same
			// e.g.  abc|def|ghi but not abd|def~ghi
			        .getPattern("\\w+((\\W)\\w+)?(\\2\\w+)*", // $NON-NLS-1$
					Perl5Compiler.READ_ONLY_MASK);
			if (matcher.matches(input, pattern)) {
				delim = matcher.getMatch().group(2);
				parts = splitHeader(headerLine,delim);// now validate the result
			}
		}
		
		if (parts == null) {
			return null; // failed to recognise the header
		}
		
		// We know the column names all exist, so create the config 
		SampleSaveConfiguration saveConfig=new SampleSaveConfiguration(false);
		
		for(int i=0;i<parts.length;i++){
			Functor set = (Functor) headerLabelMethods.get(parts[i]);
			set.invoke(saveConfig,new Boolean[]{Boolean.TRUE});
		}

		if (delim != null){
			log.warn("Default delimiter '"+_saveConfig.getDelimiter()+"' did not work; using alternate '"+delim+"' for reading "+filename);
			saveConfig.setDelimiter(delim);
		}
		return saveConfig;
	}

	private static String[] splitHeader(String headerLine, String delim) {
		String parts[]=headerLine.split("\\Q"+delim);// $NON-NLS-1$
		int previous = -1;
		// Check if the line is a header
		for(int i=0;i<parts.length;i++){
			final String label = parts[i];
			int current = headerLabelMethods.indexOf(label);
			if (current == -1){
				return null; // unknown column name
			}
			if (current <= previous){
				log.warn("Column header number "+(i+1)+" name "+ label + " is out of order.");
				return null; // out of order
			}
			previous = current;
		}
		return parts;
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
     * @param event
     *            the sample event to be converted
     * @return the separated value representation of the result
     */
    public static String resultToDelimitedString(SampleEvent event) {
    	return resultToDelimitedString(event, event.getResult().getSaveConfig().getDelimiter());
    }

    /**
     * Convert a result into a string, where the fields of the result are
     * separated by a specified String.
     * 
     * @param event
     *            the sample event to be converted
     * @param delimiter
     *            the separation string
     * @return the separated value representation of the result
     */
    public static String resultToDelimitedString(SampleEvent event, String delimiter) {
    	StringBuffer text = new StringBuffer();
    	SampleResult sample = event.getResult();
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
            text.append(sample.getGroupThreads());
            text.append(delimiter);
            text.append(sample.getAllThreads());
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
    
        if (saveConfig.saveHostname()) {
            text.append(event.getHostname());
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
}
