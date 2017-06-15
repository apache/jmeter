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

package org.apache.jmeter.protocol.http.util.accesslog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

// For JUnit tests, @see TestTCLogParser

/**
 * Description:<br>
 * <br>
 * Currently the parser only handles GET/POST requests. It's easy enough to add
 * support for other request methods by changing checkMethod. The is a complete
 * rewrite of a tool I wrote for myself earlier. The older algorithm was basic
 * and did not provide the same level of flexibility I want, so I wrote a new
 * one using a totally new algorithm. This implementation reads one line at a
 * time using BufferedReader. When it gets to the end of the file and the
 * sampler needs to get more requests, the parser will re-initialize the
 * BufferedReader. The implementation uses StringTokenizer to create tokens.
 * <p>
 * The parse algorithm is the following:
 * <ol>
 * <li> cleans the entry by looking for backslash "\"
 * <li> looks to see if GET or POST is in the line
 * <li> tokenizes using quotes "
 * <li> finds the token with the request method
 * <li> gets the string of the token and tokenizes it using space
 * <li> finds the first token beginning with slash character
 * <li> tokenizes the string using question mark "?"
 * <li> get the path from the first token
 * <li> returns the second token and checks it for parameters
 * <li> tokenizes the string using ampersand "&amp;"
 * <li> parses each token to name/value pairs
 * </ol>
 * <p>
 * Extending this class is fairly simple. Most access logs use the same format
 * starting from the request method. Therefore, changing the implementation of
 * cleanURL(string) method should be sufficient to support new log formats.
 * Tomcat uses common log format, so any webserver that uses the format should
 * work with this parser. Servers that are known to use non standard formats are
 * IIS and Netscape.
 */

public class TCLogParser implements LogParser {
    protected static final Logger log = LoggerFactory.getLogger(TCLogParser.class);

    /*
     * TODO should these fields be public?
     * They don't appear to be used externally.
     * 
     * Also, are they any different from HTTPConstants.GET etc. ?
     * In some cases they seem to be used as the method name from the Tomcat log.
     * However the RMETHOD field is used as the value for HTTPSamplerBase.METHOD,
     * for which HTTPConstants is most appropriate.
     */
    public static final String GET = "GET";

    public static final String POST = "POST";

    public static final String HEAD = "HEAD";

    /** protected members * */
    protected String RMETHOD = null;

    /**
     * The path to the access log file
     */
    protected String URL_PATH = null;

    protected boolean useFILE = true;

    protected File SOURCE = null;

    protected String FILENAME = null;

    protected BufferedReader READER = null;

    /**
     * Handles to supporting classes
     */
    protected Filter FILTER = null;

    /**
     * by default, we probably should decode the parameter values
     */
    protected boolean decode = true;

    // TODO downcase UPPER case non-final variables

    /**
     *
     */
    public TCLogParser() {
        super();
    }

    /**
     * @param source name of the source file
     */
    public TCLogParser(String source) {
        setSourceFile(source);
    }

    /**
     * by default decode is set to true. if the parameters shouldn't be
     * decoded, call the method with false
     * @param decodeparams flag whether parameters should be decoded
     */
    public void setDecodeParameterValues(boolean decodeparams) {
        this.decode = decodeparams;
    }

    /**
     * decode the parameter values is to true by default
     * @return <code>true</code> if parameter values should be decoded, <code>false</code> otherwise
     */
    public boolean decodeParameterValue() {
        return this.decode;
    }

    /**
     * Calls this method to set whether or not to use the path in the log. We
     * may want to provide the ability to filter the log file later on. By
     * default, the parser uses the file in the log.
     *
     * @param file
     *            flag whether to use the path from the log
     */
    public void setUseParsedFile(boolean file) {
        this.useFILE = file;
    }

    /**
     * Use the filter to include/exclude files in the access logs. This is
     * provided as a convenience and reduce the need to spend hours cleaning up
     * log files.
     *
     * @param filter {@link Filter} to be used while reading the log lines
     */
    @Override
    public void setFilter(Filter filter) {
        FILTER = filter;
    }

    /**
     * Sets the source file.
     *
     * @param source name of the source file
     */
    @Override
    public void setSourceFile(String source) {
        this.FILENAME = source;
    }

    /**
     * parse the entire file.
     *
     * @param el TestElement to read the lines into
     * @param parseCount number of max lines to read
     * @return number of read lines, or <code>-1</code> if an error occurred while reading
     */
    public int parse(TestElement el, int parseCount) {
        if (this.SOURCE == null) {
            this.SOURCE = new File(this.FILENAME);
        }
        try {
            if (this.READER == null) {
                this.READER = getReader(this.SOURCE);
            }
            return parse(this.READER, el, parseCount);
        } catch (Exception exception) {
            log.error("Problem creating samples", exception);
        }
        return -1;// indicate that an error occurred
    }

    private static BufferedReader getReader(File file) throws IOException {
        if (! isGZIP(file)) {
            return new BufferedReader(new FileReader(file));
        }
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(file));
        return new BufferedReader(new InputStreamReader(in));
    }

    private static boolean isGZIP(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return in.read() == (GZIPInputStream.GZIP_MAGIC & 0xFF)
                && in.read() == (GZIPInputStream.GZIP_MAGIC >> 8);
        }
    }

    /**
     * parse a set number of lines from the access log. Keep in mind the number
     * of lines parsed will depend on the filter and number of lines in the log.
     * The method returns the actual number of lines parsed.
     *
     * @param count number of lines to read
     * @param el {@link TestElement} to read lines into
     * @return lines parsed
     */
    @Override
    public int parseAndConfigure(int count, TestElement el) {
        return this.parse(el, count);
    }

    /**
     * The method is responsible for reading each line, and breaking out of the
     * while loop if a set number of lines is given.<br>
     * Note: empty lines will not be counted
     *
     * @param breader {@link BufferedReader} to read lines from
     * @param el {@link TestElement} to read lines into
     * @param parseCount number of lines to read
     * @return number of lines parsed
     */
    protected int parse(BufferedReader breader, TestElement el, int parseCount) {
        int actualCount = 0;
        String line = null;
        try {
            // read one line at a time using
            // BufferedReader
            line = breader.readLine();
            while (line != null) {
                if (line.length() > 0) {
                    actualCount += this.parseLine(line, el);
                }
                // we check the count to see if we have exceeded
                // the number of lines to parse. There's no way
                // to know where to stop in the file. Therefore
                // we use break to escape the while loop when
                // we've reached the count.
                if (parseCount != -1 && actualCount >= parseCount) {
                    break;
                }
                line = breader.readLine();
            }
            if (line == null) {
                breader.close();
                this.READER = null;
                // this.READER = new BufferedReader(new
                // FileReader(this.SOURCE));
                // parse(this.READER,el);
            }
        } catch (IOException ioe) {
            log.error("Error reading log file", ioe);
        }
        return actualCount;
    }

    /**
     * parseLine calls the other parse methods to parse the given text.
     *
     * @param line single line to be parsed
     * @param el {@link TestElement} in which the line will be added
     * @return number of lines parsed (zero or one, actually)
     */
    protected int parseLine(String line, TestElement el) {
        int count = 0;
        // we clean the line to get
        // rid of extra stuff
        String cleanedLine = this.cleanURL(line);
        log.debug("parsing line: " + line);
        // now we set request method
        el.setProperty(HTTPSamplerBase.METHOD, RMETHOD);
        if (FILTER != null) {
            log.debug("filter is not null");
            if (!FILTER.isFiltered(line,el)) {
                log.debug("line was not filtered");
                // increment the current count
                count++;
                // we filter the line first, before we try
                // to separate the URL into file and
                // parameters.
                line = FILTER.filter(cleanedLine);
                if (line != null) {
                    createUrl(line, el);
                }
            } else {
                log.debug("Line was filtered");
            }
        } else {
            log.debug("filter was null");
            // increment the current count
            count++;
            // in the case when the filter is not set, we
            // parse all the lines
            createUrl(cleanedLine, el);
        }
        return count;
    }

    /**
     * @param line single line of which the url should be extracted 
     * @param el {@link TestElement} into which the url will be added
     */
    private void createUrl(String line, TestElement el) {
        String paramString = null;
        // check the URL for "?" symbol
        paramString = this.stripFile(line, el);
        if (paramString != null) {
            this.checkParamFormat(line);
            // now that we have stripped the file, we can parse the parameters
            this.convertStringToJMRequest(paramString, el);
        }
    }

    /**
     * The method cleans the URL using the following algorithm.
     * <ol>
     * <li> check for double quotes
     * <li> check the request method
     * <li> tokenize using double quotes
     * <li> find first token containing request method
     * <li> tokenize string using space
     * <li> find first token that begins with "/"
     * </ol>
     * Example Tomcat log entry:
     * <p>
     * 127.0.0.1 - - [08/Jan/2003:07:03:54 -0500] "GET /addrbook/ HTTP/1.1" 200
     * 1981
     * <p>
     * would result in the extracted url <code>/addrbook/</code>
     *
     * @param entry line from which the url is to be extracted
     * @return cleaned url
     */
    public String cleanURL(String entry) {
        String url = entry;
        if (entry.contains("\"") && checkMethod(entry)) {
            // we tokenize using double quotes. this means
            // for tomcat we should have 3 tokens if there
            // isn't any additional information in the logs
            StringTokenizer tokens = this.tokenize(entry, "\"");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                if (checkMethod(token)) {
                    // we tokenzie it using space and escape
                    // the while loop. Only the first matching
                    // token will be used
                    StringTokenizer token2 = this.tokenize(token, " ");
                    while (token2.hasMoreTokens()) {
                        String t = (String) token2.nextElement();
                        if (t.equalsIgnoreCase(GET)) {
                            RMETHOD = GET;
                        } else if (t.equalsIgnoreCase(POST)) {
                            RMETHOD = POST;
                        } else if (t.equalsIgnoreCase(HEAD)) {
                            RMETHOD = HEAD;
                        }
                        // there should only be one token
                        // that starts with slash character
                        if (t.startsWith("/")) {
                            url = t;
                            break;
                        }
                    }
                    break;
                }
            }
            return url;
        }
        // we return the original string
        return url;
    }

    /**
     * The method checks for <code>POST</code>, <code>GET</code> and <code>HEAD</code> methods currently.
     * The other methods aren't supported yet.
     *
     * @param text text to be checked for HTTP method
     * @return <code>true</code> if method is supported, <code>false</code> otherwise
     */
    public boolean checkMethod(String text) {
        if (text.contains("GET")) {
            this.RMETHOD = GET;
            return true;
        } else if (text.contains("POST")) {
            this.RMETHOD = POST;
            return true;
        } else if (text.contains("HEAD")) {
            this.RMETHOD = HEAD;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tokenize the URL into two tokens. If the URL has more than one "?", the
     * parse may fail. Only the first two tokens are used. The first token is
     * automatically parsed and set at {@link TCLogParser#URL_PATH URL_PATH}.
     *
     * @param url url which should be stripped from parameters
     * @param el {@link TestElement} to parse url into
     * @return String presenting the parameters, or <code>null</code> when none where found
     */
    public String stripFile(String url, TestElement el) {
        if (url.contains("?")) {
            StringTokenizer tokens = this.tokenize(url, "?");
            this.URL_PATH = tokens.nextToken();
            el.setProperty(HTTPSamplerBase.PATH, URL_PATH);
            return tokens.hasMoreTokens() ? tokens.nextToken() : null;
        }
        el.setProperty(HTTPSamplerBase.PATH, url);
        return null;
    }

    /**
     * Checks the string to make sure it has <code>/path/file?name=value</code> format. If
     * the string doesn't contains a "?", it will return <code>false</code>.
     *
     * @param url url to check for parameters
     * @return <code>true</code> if url contains a <code>?</code>,
     *         <code>false</code> otherwise
     */
    public boolean checkURL(String url) {
        return url.contains("?");
    }

    /**
     * Checks the string to see if it contains "&amp;" and "=". If it does, return
     * <code>true</code>, so that it can be parsed.
     *
     * @param text text to be checked for <code>&amp;</code> and <code>=</code>
     * @return <code>true</code> if <code>text</code> contains both <code>&amp;</code>
     *         and <code>=</code>, <code>false</code> otherwise
     */
    public boolean checkParamFormat(String text) {
        return text.contains("&") && text.contains("=");
    }

    /**
     * Convert a single line into XML
     *
     * @param text to be converted
     * @param el {@link HTTPSamplerBase} which consumes the <code>text</code>
     */
    public void convertStringToJMRequest(String text, TestElement el) {
        ((HTTPSamplerBase) el).parseArguments(text);
    }

    /**
     * Parse the string parameters into NVPair[] array. Once they are parsed, it
     * is returned. The method uses parseOneParameter(string) to convert each
     * pair.
     *
     * @param stringparams String with parameters to be parsed
     * @return array of {@link NVPair}s
     */
    public NVPair[] convertStringtoNVPair(String stringparams) {
        List<String> vparams = this.parseParameters(stringparams);
        NVPair[] nvparams = new NVPair[vparams.size()];
        // convert the Parameters
        for (int idx = 0; idx < nvparams.length; idx++) {
            nvparams[idx] = this.parseOneParameter(vparams.get(idx));
        }
        return nvparams;
    }

    /**
     * Method expects name and value to be separated by an equal sign "=". The
     * method uses StringTokenizer to make a NVPair object. If there happens to
     * be more than one "=" sign, the others are ignored. The chance of a string
     * containing more than one is unlikely and would not conform to HTTP spec.
     * I should double check the protocol spec to make sure this is accurate.
     *
     * @param parameter
     *            to be parsed
     * @return {@link NVPair} with the parsed name and value of the parameter
     */
    protected NVPair parseOneParameter(String parameter) {
        String name = ""; // avoid possible NPE when trimming the name
        String value = null;
        try {
            StringTokenizer param = this.tokenize(parameter, "=");
            name = param.nextToken();
            value = param.nextToken();
        } catch (Exception e) {
            // do nothing. it's naive, but since
            // the utility is meant to parse access
            // logs the formatting should be correct
        }
        if (value == null) {
            value = "";
        } else {
            if (decode) {
                try {
                    value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    log.warn(e.getMessage());
                }
            }
        }
        return new NVPair(name.trim(), value.trim());
    }

    /**
     * Method uses StringTokenizer to convert the string into single pairs. The
     * string should conform to HTTP protocol spec, which means the name/value
     * pairs are separated by the ampersand symbol "&amp;". Someone could write the
     * querystrings by hand, but that would be round about and go against the
     * purpose of this utility.
     *
     * @param parameters string to be parsed
     * @return List of name/value pairs
     */
    protected List<String> parseParameters(String parameters) {
        List<String> parsedParams = new ArrayList<>();
        StringTokenizer paramtokens = this.tokenize(parameters, "&");
        while (paramtokens.hasMoreElements()) {
            parsedParams.add(paramtokens.nextToken());
        }
        return parsedParams;
    }

    /**
     * Parses the line using java.util.StringTokenizer.
     *
     * @param line
     *            line to be parsed
     * @param delim
     *            delimiter
     * @return StringTokenizer constructed with <code>line</code> and <code>delim</code>
     */
    public StringTokenizer tokenize(String line, String delim) {
        return new StringTokenizer(line, delim);
    }

    @Override
    public void close() {
        try {
            this.READER.close();
            this.READER = null;
            this.SOURCE = null;
        } catch (IOException e) {
            // do nothing
        }
    }
}
