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

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.samplers.StatisticalSampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.reflect.Functor;
import org.apache.jorphan.util.JMeterError;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a means for saving/reading test results as CSV files.
 */
// For unit tests, @see TestCSVSaveService
public final class CSVSaveService {
    private static final Logger log = LoggerFactory.getLogger(CSVSaveService.class);

    // ---------------------------------------------------------------------
    // XML RESULT FILE CONSTANTS AND FIELD NAME CONSTANTS
    // ---------------------------------------------------------------------

    public static final String DATA_TYPE = "dataType"; // $NON-NLS-1$
    public static final String FAILURE_MESSAGE = "failureMessage"; // $NON-NLS-1$
    public static final String LABEL = "label"; // $NON-NLS-1$
    public static final String RESPONSE_CODE = "responseCode"; // $NON-NLS-1$
    public static final String RESPONSE_MESSAGE = "responseMessage"; // $NON-NLS-1$
    public static final String SUCCESSFUL = "success"; // $NON-NLS-1$
    public static final String THREAD_NAME = "threadName"; // $NON-NLS-1$
    public static final String TIME_STAMP = "timeStamp"; // $NON-NLS-1$

    // ---------------------------------------------------------------------
    // ADDITIONAL CSV RESULT FILE CONSTANTS AND FIELD NAME CONSTANTS
    // ---------------------------------------------------------------------

    public static final String CSV_ELAPSED = "elapsed"; // $NON-NLS-1$
    public static final String CSV_BYTES = "bytes"; // $NON-NLS-1$
    public static final String CSV_SENT_BYTES = "sentBytes"; // $NON-NLS-1$
    public static final String CSV_THREAD_COUNT1 = "grpThreads"; // $NON-NLS-1$
    public static final String CSV_THREAD_COUNT2 = "allThreads"; // $NON-NLS-1$
    public static final String CSV_SAMPLE_COUNT = "SampleCount"; // $NON-NLS-1$
    public static final String CSV_ERROR_COUNT = "ErrorCount"; // $NON-NLS-1$
    public static final String CSV_URL = "URL"; // $NON-NLS-1$
    public static final String CSV_FILENAME = "Filename"; // $NON-NLS-1$
    public static final String CSV_LATENCY = "Latency"; // $NON-NLS-1$
    public static final String CSV_CONNECT_TIME = "Connect"; // $NON-NLS-1$
    public static final String CSV_ENCODING = "Encoding"; // $NON-NLS-1$
    public static final String CSV_HOSTNAME = "Hostname"; // $NON-NLS-1$
    public static final String CSV_IDLETIME = "IdleTime"; // $NON-NLS-1$

    // Used to enclose variable name labels, to distinguish from any of the
    // above labels
    public static final String VARIABLE_NAME_QUOTE_CHAR = "\""; // $NON-NLS-1$

    // Initial config from properties
    private static final SampleSaveConfiguration _saveConfig = SampleSaveConfiguration
            .staticConfig();

    // Date formats to try if the time format does not parse as milliseconds
    private static final String[] DATE_FORMAT_STRINGS = {
        "yyyy/MM/dd HH:mm:ss.SSS",  // $NON-NLS-1$
        "yyyy/MM/dd HH:mm:ss",  // $NON-NLS-1$
        "yyyy-MM-dd HH:mm:ss.SSS",  // $NON-NLS-1$
        "yyyy-MM-dd HH:mm:ss",  // $NON-NLS-1$

        "MM/dd/yy HH:mm:ss"  // $NON-NLS-1$ (for compatibility, this is the original default)
        };

    private static final String LINE_SEP = System.getProperty("line.separator"); // $NON-NLS-1$

    /**
     * Private constructor to prevent instantiation.
     */
    private CSVSaveService() {
    }

    /**
     * Read Samples from a file; handles quoted strings.
     *
     * @param filename
     *            input file
     * @param visualizer
     *            where to send the results
     * @param resultCollector
     *            the parent collector
     * @throws IOException
     *             when the file referenced by <code>filename</code> can't be
     *             read correctly
     */
    public static void processSamples(String filename, Visualizer visualizer,
            ResultCollector resultCollector) throws IOException {
        final boolean errorsOnly = resultCollector.isErrorLogging();
        final boolean successOnly = resultCollector.isSuccessOnlyLogging();
        try (InputStream inStream = new FileInputStream(filename);
                Reader inReader = new InputStreamReader(inStream,
                        SaveService.getFileEncoding(StandardCharsets.UTF_8.name()));
                BufferedReader dataReader = new BufferedReader(inReader)) {
            dataReader.mark(400);// Enough to read the header column names
            // Get the first line, and see if it is the header
            String line = dataReader.readLine();
            if (line == null) {
                throw new IOException(filename + ": unable to read header line");
            }
            long lineNumber = 1;
            SampleSaveConfiguration saveConfig = CSVSaveService
                    .getSampleSaveConfiguration(line, filename);
            if (saveConfig == null) {// not a valid header
                log.info("{} does not appear to have a valid header. Using default configuration.", filename);
                saveConfig = (SampleSaveConfiguration) resultCollector
                        .getSaveConfig().clone(); // may change the format later
                dataReader.reset(); // restart from beginning
                lineNumber = 0;
            }
            String[] parts;
            final char delim = saveConfig.getDelimiter().charAt(0);
            // TODO: does it matter that an empty line will terminate the loop?
            // CSV output files should never contain empty lines, so probably
            // not
            // If so, then need to check whether the reader is at EOF
            while ((parts = csvReadFile(dataReader, delim)).length != 0) {
                lineNumber++;
                SampleEvent event = CSVSaveService.makeResultFromDelimitedString(parts, saveConfig, lineNumber);
                if (event != null) {
                    final SampleResult result = event.getResult();
                    if (ResultCollector.isSampleWanted(result.isSuccessful(),
                            errorsOnly, successOnly)) {
                        visualizer.add(result);
                    }
                }
            }
        }
    }

    /**
     * Make a SampleResult given a set of tokens
     *
     * @param parts
     *            tokens parsed from the input
     * @param saveConfig
     *            the save configuration (may be updated)
     * @param lineNumber the line number (for error reporting)
     * @return the sample result
     *
     * @throws JMeterError
     */
    private static SampleEvent makeResultFromDelimitedString(
            final String[] parts,
            final SampleSaveConfiguration saveConfig, // may be updated
            final long lineNumber) {

        SampleResult result = null;
        String hostname = "";// $NON-NLS-1$
        long timeStamp = 0;
        long elapsed = 0;
        String text = null;
        String field = null; // Save the name for error reporting
        int i = 0;
        try {
            if (saveConfig.saveTimestamp()) {
                field = TIME_STAMP;
                text = parts[i++];
                if (saveConfig.printMilliseconds()) {
                    try {
                        timeStamp = Long.parseLong(text); // see if this works
                    } catch (NumberFormatException e) { // it did not, let's try some other formats
                        log.warn("Cannot parse timestamp: '{}', will try following formats {}", text,
                                Arrays.asList(DATE_FORMAT_STRINGS));
                        boolean foundMatch = false;
                        for(String fmt : DATE_FORMAT_STRINGS) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat(fmt);
                            dateFormat.setLenient(false);
                            try {
                                Date stamp = dateFormat.parse(text);
                                timeStamp = stamp.getTime();
                                log.warn("Setting date format to: {}", fmt);
                                saveConfig.setDateFormat(fmt);
                                foundMatch = true;
                                break;
                            } catch (ParseException pe) {
                                log.info("{} did not match {}, trying next date format", text, fmt);
                            }
                        }
                        if (!foundMatch) {
                            throw new ParseException("No date-time format found matching "+text,-1);
                        }
                    }
                } else if (saveConfig.strictDateFormatter() != null) {
                    Date stamp = saveConfig.strictDateFormatter().parse(text);
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
                result.setBytes(Long.parseLong(text));
            }

            if (saveConfig.saveSentBytes()) {
                field = CSV_SENT_BYTES;
                text = parts[i++];
                result.setSentBytes(Long.parseLong(text));
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

            if (saveConfig.saveIdleTime()) {
                field = CSV_IDLETIME;
                text = parts[i++];
                result.setIdleTime(Long.parseLong(text));
            }
            if (saveConfig.saveConnectTime()) {
                field = CSV_CONNECT_TIME;
                text = parts[i++];
                result.setConnectTime(Long.parseLong(text));
            }

            if (i + saveConfig.getVarCount() < parts.length) {
                log.warn("Line: {}. Found {} fields, expected {}. Extra fields have been ignored.", lineNumber,
                        parts.length, i);
            }

        } catch (NumberFormatException | ParseException e) {
            if (log.isWarnEnabled()) {
                log.warn("Error parsing field '{}' at line {}. {}", field, lineNumber, e.toString());
            }
            throw new JMeterError(e);
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warn("Insufficient columns to parse field '{}' at line {}", field, lineNumber);
            throw new JMeterError(e);
        }
        return new SampleEvent(result, "", hostname);
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
     * @param saveConfig
     *            the configuration of what is to be saved
     * @return the field names as a string
     */
    public static String printableFieldNamesToString(
            SampleSaveConfiguration saveConfig) {
        StringBuilder text = new StringBuilder();
        String delim = saveConfig.getDelimiter();

        appendFields(saveConfig.saveTimestamp(), text, delim, TIME_STAMP);
        appendFields(saveConfig.saveTime(), text, delim, CSV_ELAPSED);
        appendFields(saveConfig.saveLabel(), text, delim, LABEL);
        appendFields(saveConfig.saveCode(), text, delim, RESPONSE_CODE);
        appendFields(saveConfig.saveMessage(), text, delim, RESPONSE_MESSAGE);
        appendFields(saveConfig.saveThreadName(), text, delim, THREAD_NAME);
        appendFields(saveConfig.saveDataType(), text, delim, DATA_TYPE);
        appendFields(saveConfig.saveSuccess(), text, delim, SUCCESSFUL);
        appendFields(saveConfig.saveAssertionResultsFailureMessage(), text, delim, FAILURE_MESSAGE);
        appendFields(saveConfig.saveBytes(), text, delim, CSV_BYTES);
        appendFields(saveConfig.saveSentBytes(), text, delim, CSV_SENT_BYTES);
        appendFields(saveConfig.saveThreadCounts(), text, delim, CSV_THREAD_COUNT1, CSV_THREAD_COUNT2);
        appendFields(saveConfig.saveUrl(), text, delim, CSV_URL);
        appendFields(saveConfig.saveFileName(), text, delim, CSV_FILENAME);
        appendFields(saveConfig.saveLatency(), text, delim, CSV_LATENCY);
        appendFields(saveConfig.saveEncoding(), text, delim, CSV_ENCODING);
        appendFields(saveConfig.saveSampleCount(), text, delim, CSV_SAMPLE_COUNT, CSV_ERROR_COUNT);
        appendFields(saveConfig.saveHostname(), text, delim, CSV_HOSTNAME);
        appendFields(saveConfig.saveIdleTime(), text, delim, CSV_IDLETIME);
        appendFields(saveConfig.saveConnectTime(), text, delim, CSV_CONNECT_TIME);

        for (int i = 0; i < SampleEvent.getVarCount(); i++) {
            text.append(VARIABLE_NAME_QUOTE_CHAR);
            text.append(SampleEvent.getVarName(i));
            text.append(VARIABLE_NAME_QUOTE_CHAR);
            text.append(delim);
        }

        String resultString;
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

    private static void appendFields(final boolean condition, StringBuilder textBuffer, String delim, String... fieldNames) {
        if (condition) {
            for (String name: fieldNames) {
                textBuffer.append(name);
                textBuffer.append(delim);
            }
        }
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
        headerLabelMethods.put(FAILURE_MESSAGE, new Functor(
                "setAssertionResultsFailureMessage"));
        headerLabelMethods.put(CSV_BYTES, new Functor("setBytes"));
        headerLabelMethods.put(CSV_SENT_BYTES, new Functor("setSentBytes"));
        // Both these are needed in the list even though they set the same
        // variable
        headerLabelMethods.put(CSV_THREAD_COUNT1,
                new Functor("setThreadCounts"));
        headerLabelMethods.put(CSV_THREAD_COUNT2,
                new Functor("setThreadCounts"));
        headerLabelMethods.put(CSV_URL, new Functor("setUrl"));
        headerLabelMethods.put(CSV_FILENAME, new Functor("setFileName"));
        headerLabelMethods.put(CSV_LATENCY, new Functor("setLatency"));
        headerLabelMethods.put(CSV_ENCODING, new Functor("setEncoding"));
        // Both these are needed in the list even though they set the same
        // variable
        headerLabelMethods.put(CSV_SAMPLE_COUNT, new Functor("setSampleCount"));
        headerLabelMethods.put(CSV_ERROR_COUNT, new Functor("setSampleCount"));
        headerLabelMethods.put(CSV_HOSTNAME, new Functor("setHostname"));
        headerLabelMethods.put(CSV_IDLETIME, new Functor("setIdleTime"));
        headerLabelMethods.put(CSV_CONNECT_TIME, new Functor("setConnectTime"));
    }

    /**
     * Parse a CSV header line
     *
     * @param headerLine
     *            from CSV file
     * @param filename
     *            name of file (for log message only)
     * @return config corresponding to the header items found or null if not a
     *         header line
     */
    public static SampleSaveConfiguration getSampleSaveConfiguration(
            String headerLine, String filename) {
        String[] parts = splitHeader(headerLine, _saveConfig.getDelimiter()); // Try
                                                                              // default
                                                                              // delimiter

        String delim = null;

        if (parts == null) {
            Perl5Matcher matcher = JMeterUtils.getMatcher();
            PatternMatcherInput input = new PatternMatcherInput(headerLine);
            Pattern pattern = JMeterUtils.getPatternCache()
            // This assumes the header names are all single words with no spaces
            // word followed by 0 or more repeats of (non-word char + word)
            // where the non-word char (\2) is the same
            // e.g. abc|def|ghi but not abd|def~ghi
                    .getPattern("\\w+((\\W)\\w+)?(\\2\\w+)*(\\2\"\\w+\")*", // $NON-NLS-1$
                            // last entries may be quoted strings
                            Perl5Compiler.READ_ONLY_MASK);
            if (matcher.matches(input, pattern)) {
                delim = matcher.getMatch().group(2);
                parts = splitHeader(headerLine, delim);// now validate the
                                                       // result
            }
        }

        if (parts == null) {
            return null; // failed to recognise the header
        }

        // We know the column names all exist, so create the config
        SampleSaveConfiguration saveConfig = new SampleSaveConfiguration(false);

        int varCount = 0;
        for (String label : parts) {
            if (isVariableName(label)) {
                varCount++;
            } else {
                Functor set = (Functor) headerLabelMethods.get(label);
                set.invoke(saveConfig, new Boolean[]{Boolean.TRUE});
            }
        }

        if (delim != null) {
            if (log.isWarnEnabled()) {
                log.warn("Default delimiter '{}' did not work; using alternate '{}' for reading {}",
                        _saveConfig.getDelimiter(), delim, filename);
            }
            saveConfig.setDelimiter(delim);
        }

        saveConfig.setVarCount(varCount);

        return saveConfig;
    }

    private static String[] splitHeader(String headerLine, String delim) {
        String[] parts = headerLine.split("\\Q" + delim);// $NON-NLS-1$
        int previous = -1;
        // Check if the line is a header
        for (int i = 0; i < parts.length; i++) {
            final String label = parts[i];
            // Check for Quoted variable names
            if (isVariableName(label)) {
                previous = Integer.MAX_VALUE; // they are always last
                continue;
            }
            int current = headerLabelMethods.indexOf(label);
            if (current == -1) {
                log.warn("Unknown column name {}", label);
                return null; // unknown column name
            }
            if (current <= previous) {
                log.warn("Column header number {} name {} is out of order.", i + 1, label);
                return null; // out of order
            }
            previous = current;
        }
        return parts;
    }

    /**
     * Check if the label is a variable name, i.e. is it enclosed in
     * double-quotes?
     *
     * @param label
     *            column name from CSV file
     * @return if the label is enclosed in double-quotes
     */
    private static boolean isVariableName(final String label) {
        return label.length() > 2 && label.startsWith(VARIABLE_NAME_QUOTE_CHAR)
                && label.endsWith(VARIABLE_NAME_QUOTE_CHAR);
    }

    /**
     * Method will save aggregate statistics as CSV. For now I put it here. Not
     * sure if it should go in the newer SaveService instead of here. if we ever
     * decide to get rid of this class, we'll need to move this method to the
     * new save service.
     *
     * @param data
     *            List of data rows
     * @param writer
     *            output writer
     * @throws IOException
     *             when writing to <code>writer</code> fails
     */
    public static void saveCSVStats(List<?> data, Writer writer)
            throws IOException {
        saveCSVStats(data, writer, null);
    }

    /**
     * Method will save aggregate statistics as CSV. For now I put it here. Not
     * sure if it should go in the newer SaveService instead of here. if we ever
     * decide to get rid of this class, we'll need to move this method to the
     * new save service.
     *
     * @param data
     *            List of data rows
     * @param writer
     *            output file
     * @param headers
     *            header names (if non-null)
     * @throws IOException
     *             when writing to <code>writer</code> fails
     */
    public static void saveCSVStats(List<?> data, Writer writer,
            String[] headers) throws IOException {
        final char DELIM = ',';
        final char[] SPECIALS = new char[] { DELIM, QUOTING_CHAR };
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) {
                    writer.write(DELIM);
                }
                writer.write(quoteDelimiters(headers[i], SPECIALS));
            }
            writer.write(LINE_SEP);
        }
        for (Object o : data) {
            List<?> row = (List<?>) o;
            for (int idy = 0; idy < row.size(); idy++) {
                if (idy > 0) {
                    writer.write(DELIM);
                }
                Object item = row.get(idy);
                writer.write(quoteDelimiters(String.valueOf(item), SPECIALS));
            }
            writer.write(LINE_SEP);
        }
    }

    /**
     * Method saves aggregate statistics (with header names) as CSV from a table
     * model. Same as {@link #saveCSVStats(List, Writer, String[])} except
     * that there is no need to create a List containing the data.
     *
     * @param model
     *            table model containing the data
     * @param writer
     *            output file
     * @throws IOException
     *             when writing to <code>writer</code> fails
     */
    public static void saveCSVStats(DefaultTableModel model, FileWriter writer)
            throws IOException {
        saveCSVStats(model, writer, true);
    }

    /**
     * Method saves aggregate statistics as CSV from a table model. Same as
     * {@link #saveCSVStats(List, Writer, String[])} except that there is no
     * need to create a List containing the data.
     *
     * @param model
     *            table model containing the data
     * @param writer
     *            output file
     * @param saveHeaders
     *            whether or not to save headers
     * @throws IOException
     *             when writing to <code>writer</code> fails
     */
    public static void saveCSVStats(DefaultTableModel model, FileWriter writer,
            boolean saveHeaders) throws IOException {
        final char DELIM = ',';
        final char[] SPECIALS = new char[] { DELIM, QUOTING_CHAR };
        final int columns = model.getColumnCount();
        final int rows = model.getRowCount();
        if (saveHeaders) {
            for (int i = 0; i < columns; i++) {
                if (i > 0) {
                    writer.write(DELIM);
                }
                writer.write(quoteDelimiters(model.getColumnName(i), SPECIALS));
            }
            writer.write(LINE_SEP);
        }
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (column > 0) {
                    writer.write(DELIM);
                }
                Object item = model.getValueAt(row, column);
                writer.write(quoteDelimiters(String.valueOf(item), SPECIALS));
            }
            writer.write(LINE_SEP);
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
        return resultToDelimitedString(event, event.getResult().getSaveConfig()
                .getDelimiter());
    }

    /*
     * Class to handle generating the delimited string. - adds the delimiter
     * if not the first call - quotes any strings that require it
     */
    static final class StringQuoter {
        private final StringBuilder sb;
        private final char[] specials;
        private boolean addDelim;

        public StringQuoter(char delim) {
            sb = new StringBuilder(150);
            specials = new char[] { delim, QUOTING_CHAR, CharUtils.CR,
                    CharUtils.LF };
            addDelim = false; // Don't add delimiter first time round
        }

        private void addDelim() {
            if (addDelim) {
                sb.append(specials[0]);
            } else {
                addDelim = true;
            }
        }

        // These methods handle parameters that could contain delimiters or
        // quotes:
        public void append(String s) {
            addDelim();
            sb.append(quoteDelimiters(s, specials));
        }

        public void append(Object obj) {
            append(String.valueOf(obj));
        }

        // These methods handle parameters that cannot contain delimiters or
        // quotes
        public void append(int i) {
            addDelim();
            sb.append(i);
        }

        public void append(long l) {
            addDelim();
            sb.append(l);
        }

        public void append(boolean b) {
            addDelim();
            sb.append(b);
        }

        @Override
        public String toString() {
            return sb.toString();
        }
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
    public static String resultToDelimitedString(SampleEvent event,
            final String delimiter) {
        return resultToDelimitedString(event, event.getResult(), event.getResult().getSaveConfig(), delimiter);
    }

    /**
     * Convert a result into a string, where the fields of the result are
     * separated by a specified String.
     *
     * @param event
     *            the sample event to be converted
     * @param sample {@link SampleResult} to log
     * @param saveConfig {@link SampleSaveConfiguration} to use for logging
     * @param delimiter
     *            the separation string
     * @return the separated value representation of the result
     */
    public static String resultToDelimitedString(SampleEvent event,
            SampleResult sample,
            SampleSaveConfiguration saveConfig,
            final String delimiter) {
        StringQuoter text = new StringQuoter(delimiter.charAt(0));
        if (saveConfig.saveTimestamp()) {
            if (saveConfig.printMilliseconds()) {
                text.append(sample.getTimeStamp());
            } else if (saveConfig.threadSafeLenientFormatter() != null) {
                String stamp = saveConfig.threadSafeLenientFormatter().format(
                        new Date(sample.getTimeStamp()));
                text.append(stamp);
            }
        }

        if (saveConfig.saveTime()) {
            text.append(sample.getTime());
        }

        if (saveConfig.saveLabel()) {
            text.append(sample.getSampleLabel());
        }

        if (saveConfig.saveCode()) {
            text.append(sample.getResponseCode());
        }

        if (saveConfig.saveMessage()) {
            text.append(sample.getResponseMessage());
        }

        if (saveConfig.saveThreadName()) {
            text.append(sample.getThreadName());
        }

        if (saveConfig.saveDataType()) {
            text.append(sample.getDataType());
        }

        if (saveConfig.saveSuccess()) {
            text.append(sample.isSuccessful());
        }

        if (saveConfig.saveAssertionResultsFailureMessage()) {
            String message = sample.getFirstAssertionFailureMessage();
            if (message != null) {
                text.append(message);
            } else {
                text.append(""); // Need to append something so delimiter is
                                 // added
            }
        }

        if (saveConfig.saveBytes()) {
            text.append(sample.getBytesAsLong());
        }

        if (saveConfig.saveSentBytes()) {
            text.append(sample.getSentBytes());
        }

        if (saveConfig.saveThreadCounts()) {
            text.append(sample.getGroupThreads());
            text.append(sample.getAllThreads());
        }
        if (saveConfig.saveUrl()) {
            text.append(sample.getURL());
        }

        if (saveConfig.saveFileName()) {
            text.append(sample.getResultFileName());
        }

        if (saveConfig.saveLatency()) {
            text.append(sample.getLatency());
        }

        if (saveConfig.saveEncoding()) {
            text.append(sample.getDataEncodingWithDefault());
        }

        if (saveConfig.saveSampleCount()) {
            // Need both sample and error count to be any use
            text.append(sample.getSampleCount());
            text.append(sample.getErrorCount());
        }

        if (saveConfig.saveHostname()) {
            text.append(event.getHostname());
        }

        if (saveConfig.saveIdleTime()) {
            text.append(sample.getIdleTime());
        }

        if (saveConfig.saveConnectTime()) {
            text.append(sample.getConnectTime());
        }

        for (int i = 0; i < SampleEvent.getVarCount(); i++) {
            text.append(event.getVarValue(i));
        }

        return text.toString();
    }

    // =================================== CSV quote/unquote handling
    // ==============================

    /*
     * Private versions of what might eventually be part of Commons-CSV or
     * Commons-Lang/Io...
     */

    /**
     * <p> Returns a <code>String</code> value for a character-delimited column
     * value enclosed in the quote character, if required. </p>
     *
     * <p> If the value contains a special character, then the String value is
     * returned enclosed in the quote character. </p>
     *
     * <p> Any quote characters in the value are doubled up. </p>
     *
     * <p> If the value does not contain any special characters, then the String
     * value is returned unchanged. </p>
     *
     * <p> N.B. The list of special characters includes the quote character.
     * </p>
     *
     * @param input the input column String, may be null (without enclosing
     * delimiters)
     *
     * @param specialChars special characters; second one must be the quote
     * character
     *
     * @return the input String, enclosed in quote characters if the value
     * contains a special character, <code>null</code> for null string input
     */
    public static String quoteDelimiters(String input, char[] specialChars) {
        if (StringUtils.containsNone(input, specialChars)) {
            return input;
        }
        StringBuilder buffer = new StringBuilder(input.length() + 10);
        final char quote = specialChars[1];
        buffer.append(quote);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == quote) {
                buffer.append(quote); // double the quote char
            }
            buffer.append(c);
        }
        buffer.append(quote);
        return buffer.toString();
    }

    // State of the parser
    private enum ParserState {INITIAL, PLAIN, QUOTED, EMBEDDEDQUOTE}

    public static final char QUOTING_CHAR = '"';

    /**
     * Reads from file and splits input into strings according to the delimiter,
     * taking note of quoted strings.
     * <p>
     * Handles DOS (CRLF), Unix (LF), and Mac (CR) line-endings equally.
     * <p>
     * A blank line - or a quoted blank line - both return an array containing
     * a single empty String.
     * @param infile
     *            input file - must support mark(1)
     * @param delim
     *            delimiter (e.g. comma)
     * @return array of strings, will be empty if there is no data, i.e. if the input is at EOF.
     * @throws IOException
     *             also for unexpected quote characters
     */
    public static String[] csvReadFile(BufferedReader infile, char delim)
            throws IOException {
        int ch;
        ParserState state = ParserState.INITIAL;
        List<String> list = new ArrayList<>();
        CharArrayWriter baos = new CharArrayWriter(200);
        boolean push = false;
        while (-1 != (ch = infile.read())) {
            push = false;
            switch (state) {
            case INITIAL:
                if (ch == QUOTING_CHAR) {
                    state = ParserState.QUOTED;
                } else if (isDelimOrEOL(delim, ch)) {
                    push = true;
                } else {
                    baos.write(ch);
                    state = ParserState.PLAIN;
                }
                break;
            case PLAIN:
                if (ch == QUOTING_CHAR) {
                    baos.write(ch);
                    throw new IOException(
                            "Cannot have quote-char in plain field:["
                                    + baos.toString() + "]");
                } else if (isDelimOrEOL(delim, ch)) {
                    push = true;
                    state = ParserState.INITIAL;
                } else {
                    baos.write(ch);
                }
                break;
            case QUOTED:
                if (ch == QUOTING_CHAR) {
                    state = ParserState.EMBEDDEDQUOTE;
                } else {
                    baos.write(ch);
                }
                break;
            case EMBEDDEDQUOTE:
                if (ch == QUOTING_CHAR) {
                    baos.write(QUOTING_CHAR); // doubled quote => quote
                    state = ParserState.QUOTED;
                } else if (isDelimOrEOL(delim, ch)) {
                    push = true;
                    state = ParserState.INITIAL;
                } else {
                    baos.write(QUOTING_CHAR);
                    throw new IOException(
                            "Cannot have single quote-char in quoted field:["
                                    + baos.toString() + "]");
                }
                break;
            default:
                throw new IllegalStateException("Unexpected state " + state);
            } // switch(state)
            if (push) {
                if (ch == '\r') {// Remove following \n if present
                    infile.mark(1);
                    if (infile.read() != '\n') {
                        infile.reset(); // did not find \n, put the character
                                        // back
                    }
                }
                String s = baos.toString();
                list.add(s);
                baos.reset();
            }
            if ((ch == '\n' || ch == '\r') && state != ParserState.QUOTED) {
                break;
            }
        } // while not EOF
        if (ch == -1) {// EOF (or end of string) so collect any remaining data
            if (state == ParserState.QUOTED) {
                throw new IOException("Missing trailing quote-char in quoted field:[\""
                        + baos.toString() + "]");
            }
            // Do we have some data, or a trailing empty field?
            if (baos.size() > 0 // we have some data
                    || push // we've started a field
                    || state == ParserState.EMBEDDEDQUOTE // Just seen ""
            ) {
                list.add(baos.toString());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private static boolean isDelimOrEOL(char delim, int ch) {
        return ch == delim || ch == '\n' || ch == '\r';
    }

    /**
     * Reads from String and splits into strings according to the delimiter,
     * taking note of quoted strings.
     *
     * Handles DOS (CRLF), Unix (LF), and Mac (CR) line-endings equally.
     *
     * @param line
     *            input line - not {@code null}
     * @param delim
     *            delimiter (e.g. comma)
     * @return array of strings
     * @throws IOException
     *             also for unexpected quote characters
     */
    public static String[] csvSplitString(String line, char delim)
            throws IOException {
        return csvReadFile(new BufferedReader(new StringReader(line)), delim);
    }

    /**
     * @param event {@link SampleEvent}
     * @param out {@link PrintWriter} to which samples will be written
     */
    public static void saveSampleResult(SampleEvent event, PrintWriter out) {
        SampleSaveConfiguration saveConfiguration = event.getResult().getSaveConfig();
        String delimiter = saveConfiguration.getDelimiter();
        String savee = resultToDelimitedString(event, event.getResult(), saveConfiguration, delimiter);
        out.println(savee);

        if(saveConfiguration.saveSubresults()) {
            SampleResult result = event.getResult();
            for (SampleResult subResult : result.getSubResults()) {
                savee = resultToDelimitedString(event, subResult, saveConfiguration, delimiter);
                out.println(savee);
            }
        }
    }
}
