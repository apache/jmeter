// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.http.util.accesslog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Description:<br>
 * <br>
 * Currently the parser only handles GET/POST
 * requests. It's easy enough to add support
 * for other request methods by changing
 * checkMethod. The is a complete rewrite of
 * a tool I wrote for myself earlier. The older
 * algorithm was basic and did not provide the
 * same level of flexibility I want, so I
 * wrote a new one using a totally new algorithm.
 * This implementation reads one line at a time
 * using BufferedReader. When it gets to the end
 * of the file and the sampler needs to get more
 * requests, the parser will re-initialize the
 * BufferedReader. The implementation uses
 * StringTokenizer to create tokens.<p>
 * The parse algorithm is the following:<p>
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
 * <li> tokenizes the string using ampersand "&"
 * <li> parses each token to name/value pairs
 * </ol>
 * <p>
 * Extending this class is fairly simple. Most
 * access logs use the same format starting from
 * the request method. Therefore, changing the
 * implementation of cleanURL(string) method
 * should be sufficient to support new log
 * formats. Tomcat uses common log format, so
 * any webserver that uses the format should
 * work with this parser. Servers that are known
 * to use non standard formats are IIS and Netscape.
 * <p>
 * 
 * @version 	$Revision$ last updated $Date$
 * Created on:	June 23, 2003<br>
 */

public class TCLogParser implements LogParser
{
   static Logger log = LoggingManager.getLoggerForClass();

    public static final String GET = "GET";
    public static final String POST = "POST";

    /** protected members **/
    protected String RMETHOD = null;
    /**
     * The path to the access log file
     */
    protected String URL_PATH = null;
    /**
     * A counter used by the parser.
     * it is the real count of lines
     * parsed
     */
    protected int COUNT = 0;
    /**
     * the number of lines the user
     * wishes to parse
     */
    protected int PARSECOUNT = -1;
    protected boolean useFILE = true;

    protected File SOURCE = null;
    protected String FILENAME = null;
    protected BufferedReader READER = null;

    /**
     * Handles to supporting classes
     */
    protected Generator GEN = null;
    protected Filter FILTER = null;

//TODO downcase UPPER case variables
 
    /**
     * 
     */
    public TCLogParser()
    {
        super();
    }

    /**
     * @param source
     */
    public TCLogParser(String source)
    {
        setSourceFile(source);
    }

    /**
     * Set the Generator
     * @param generator 
     */
    public void setGenerator(Generator generator)
    {
        this.GEN = generator;
    }

    /**
     * Calls this method to set whether or not
     * to use the path in the log. We may want
     * to provide the ability to filter the
     * log file later on. By default, the parser
     * uses the file in the log.
     * @param file
     */
    public void setUseParsedFile(boolean file)
    {
        this.useFILE = file;
    }

    /**
     * Use the filter to include/exclude files
     * in the access logs. This is provided as
     * a convienance and reduce the need to
     * spend hours cleaning up log files.
     * @param filter
     */
    public void setFilter(Filter filter)
    {
        FILTER = filter;
    }

    /**
     * Sets the source file.
     * @param source
     */
    public void setSourceFile(String source)
    {
        this.FILENAME = source;
    }

    /**
     * Creates a new File object.
     * @param filename
     */
    public File openFile(String filename)
    {
        return new File(filename);
    }

    /**
     * parse the entire file.
     * @return boolean success/failure
     */
    public boolean parse()
    {
        if (this.SOURCE == null)
        {
            this.SOURCE = this.openFile(this.FILENAME);
        }
        try
        {
            if (this.READER == null)
            {
                this.READER = new BufferedReader(new FileReader(this.SOURCE));
            }
            parse(this.READER);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return true;
    }

    /**
     * parse a set number of lines from
     * the access log. Keep in mind the
     * number of lines parsed will depend
     * the filter and number of lines in
     * the log. The method returns the
     * actual lines parsed.
     * @param count
     * @return lines parsed
     */
    public int parse(int count)
    {
        if (count > 0)
        {
            this.PARSECOUNT = count;
        }
        this.parse();
        return COUNT;
    }

    /**
     * The method is responsible for reading each
     * line, and breaking out of the while loop
     * if a set number of lines is given.
     * @param breader
     */
    protected void parse(BufferedReader breader)
    {
        String line = null;
        try
        {
            // read one line at a time using
            // BufferedReader
            line = breader.readLine();
            if (line == null && COUNT >= this.PARSECOUNT)
            {
                this.READER.close();
                this.READER = null;
                this.READER = new BufferedReader(new FileReader(this.SOURCE));
                parse(this.READER);
            }
            while (line != null)
            {
				if (line.length() > 0)
				{
					this.parseLine(line);
				}
                // we check the count to see if we have exceeded
                // the number of lines to parse. There's no way
                // to know where to stop in the file. Therefore
                // we use break to escape the while loop when
                // we've reached the count.
                if (this.PARSECOUNT != -1 && COUNT >= this.PARSECOUNT)
                {
                    break;
                }
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * parseLine calls the other parse methods
     * to parse the given text.
     * @param line
     */
    protected void parseLine(String line)
    {
        // we clean the line to get
        // rid of extra stuff
        line = this.cleanURL(line);
        // now we set request method
        this.GEN.setMethod(this.RMETHOD);
        if (FILTER != null)
        {
            if (!FILTER.isFiltered(line))
            {
                // increment the current count
                COUNT++;
                // we filter the line first, before we try
                // to separate the URL into file and 
                // parameters.
                line = FILTER.filter(line);
                if (line != null)
                {
                    createUrl(line);
                }
            }
        }
        else
        {
            // increment the current count
            COUNT++;
            // in the case when the filter is not set, we
            // parse all the lines
            createUrl(line);
        }
    }

    /**
    * @param line
    */
   private void createUrl(String line)
   {
      String paramString = null;
        // check the URL for "?" symbol
        paramString = this.stripFile(line);
        if(paramString != null)
        {
           this.checkParamFormat(line);
           // now that we have stripped the file, we can parse the parameters
           this.convertStringToJMRequest(paramString);
        }
   }

   /**
     * The method cleans the URL using the following
     * algorithm.
     * <ol>
     * <li> check for double quotes
     * <li> check the request method
     * <li> tokenize using double quotes
     * <li> find first token containing request method
     * <li> tokenize string using space
     * <li> find first token that begins with "/"
     * </ol>
     * Example Tomcat log entry:<p>
     * 127.0.0.1 - - [08/Jan/2003:07:03:54 -0500]
     * 	"GET /addrbook/ HTTP/1.1" 200 1981
     * <p>
     * @param entry
     * @return cleaned url
     */
    public String cleanURL(String entry)
    {
        String url = entry;
        // if the string contains atleast one double
        // quote and checkMethod is true, go ahead
        // and tokenize the string.
        if (entry.indexOf("\"") > -1 && checkMethod(entry))
        {
            StringTokenizer tokens = null;
            // we tokenize using double quotes. this means
            // for tomcat we should have 3 tokens if there
            // isn't any additional information in the logs
            tokens = this.tokenize(entry, "\"");
            while (tokens.hasMoreTokens())
            {
                String toke = (String) tokens.nextToken();
                // if checkMethod on the token is true
                // we tokenzie it using space and escape
                // the while loop. Only the first matching
                // token will be used
                if (checkMethod(toke))
                {
                    StringTokenizer token2 = this.tokenize(toke, " ");
                    while (token2.hasMoreTokens())
                    {
                        String t = (String) token2.nextElement();
                        if(t.equalsIgnoreCase(GET))
                        {
                           RMETHOD = GET;
                        }
                        else if(t.equalsIgnoreCase(POST))
                        {
                           RMETHOD = POST;
                        }
                        // there should only be one token
                        // that starts with slash character
                        if (t.startsWith("/"))
                        {
                            url = t;
                            break;
                        }
                    }
                    break;
                }
            }
            return url;
        }
        else
        {
            // we return the original string
            return url;
        }
    }

    /**
     * The method checks for POST and GET
     * methods currently. The other methods
     * aren't supported yet.
     * @param text
     * @return if method is supported
     */
    public boolean checkMethod(String text)
    {
        if (text.indexOf("GET") > -1)
        {
            this.RMETHOD = GET;
            return true;
        }
        else if (text.indexOf("POST") > -1)
        {
            this.RMETHOD = POST;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Tokenize the URL into two tokens. If the URL has more than one "?", the
     * parse may fail. Only the first two tokens are used. The first token is
     * automatically parsed and set at URL_PATH.
     * @param url
     * @return String parameters
     */
    public String stripFile(String url)
    {
        if (url.indexOf("?") > -1)
        {
            StringTokenizer tokens = this.tokenize(url, "?");
            this.URL_PATH = tokens.nextToken();
            this.GEN.setPath(URL_PATH);
            return tokens.nextToken();
        }
        else
        {
            this.GEN.setPath(url);
            return null;
        }
    }

    /**
     * Checks the string to make sure it has /path/file?name=value format. If
     * the string doesn't have "?", it will return false.
     * @param url
     * @return boolean
     */
    public boolean checkURL(String url)
    {
        if (url.indexOf("?") > -1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks the string to see if it contains "&" and "=". If it does, return
     * true, so that it can be parsed.
     * @param  text
     * @return boolean
     */
    public boolean checkParamFormat(String text)
    {
        if (text.indexOf("&") > -1 && text.indexOf("=") > -1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Convert a single line into XML
     * @param text
     */
    public void convertStringToJMRequest(String text)
    {
        this.GEN.setParams(this.convertStringtoNVPair(text));
    }

    /**
     * Parse the string parameters into NVPair[]
     * array. Once they are parsed, it is returned.
     * The method uses parseOneParameter(string)
     * to convert each pair.
     * @param stringparams
     */
    public NVPair[] convertStringtoNVPair(String stringparams)
    {
        Vector vparams = this.parseParameters(stringparams);
        NVPair[] nvparams = new NVPair[vparams.size()];
        // convert the Parameters
        for (int idx = 0; idx < nvparams.length; idx++)
        {
            nvparams[idx] = this.parseOneParameter((String) vparams.get(idx));
        }
        return nvparams;
    }

    /**
     * Method expects name and value to be separated
     * by an equal sign "=". The method uses StringTokenizer
     * to make a NVPair object. If there happens to be more
     * than one "=" sign, the others are ignored. The chance
     * of a string containing more than one is unlikely
     * and would not conform to HTTP spec. I should double
     * check the protocol spec to make sure this is
     * accurate.
     * @param  parameter to be parsed
     * @return NVPair
     */
    protected NVPair parseOneParameter(String parameter)
    {
        String name = null;
        String value = null;
        try
        {
            StringTokenizer param = this.tokenize(parameter, "=");
            name = param.nextToken();
            value = param.nextToken();
        }
        catch (Exception e)
        {
            // do nothing. it's naive, but since 
            // the utility is meant to parse access
            // logs the formatting should be correct
        }
        if (value == null)
        {
            value = "";
        }
        return new NVPair(name.trim(), value.trim());
    }

    /**
     * Method uses StringTokenizer to convert the string
     * into single pairs. The string should conform to
     * HTTP protocol spec, which means the name/value
     * pairs are separated by the ampersand symbol "&".
     * Some one could write the querystrings by hand,
     * but that would be round about and go against the
     * purpose of this utility.
     * @param  parameters
     * @return Vector
     */
    protected Vector parseParameters(String parameters)
    {
        Vector parsedParams = new Vector();
        StringTokenizer paramtokens = this.tokenize(parameters, "&");
        while (paramtokens.hasMoreElements())
        {
            parsedParams.add(paramtokens.nextElement());
        }
        return parsedParams;
    }

    /**
     * Parses the line using java.util.StringTokenizer.
     * @param line line to be parsed
     * @param delim delimiter
     * @return StringTokenizer
     */
    public StringTokenizer tokenize(String line, String delim)
    {
        return new StringTokenizer(line, delim);
    }

    public void close()
    {
        try
        {
            this.READER.close();
            this.READER = null;
            this.SOURCE = null;
        }
        catch (IOException e)
        {
            // do nothing
        }
    }
    //TODO write some more tests
    
    ///////////////////////////// Start of Test Code //////////////////////////
	
	public static class Test extends JMeterTestCase
	{
		private static final TCLogParser tclp = new TCLogParser();

		private static final String URL1 =
		"127.0.0.1 - - [08/Jan/2003:07:03:54 -0500] \"GET /addrbook/ HTTP/1.1\" 200 1981";

		private static final String URL2 =
		"127.0.0.1 - - [08/Jan/2003:07:03:54 -0500] \"GET /addrbook?x=y HTTP/1.1\" 200 1981";

		public void testConstruct() throws Exception
		{
			TCLogParser tcp;
			tcp = new TCLogParser();
			assertNull("Should not have set the filename",tcp.FILENAME);

			String file = "testfiles/access.log";
			tcp = new TCLogParser(file);
			assertEquals("Filename should have been saved",file,tcp.FILENAME);
		}
		
		public void testcleanURL() throws Exception
		{
		   tclp.GEN = new StandardGenerator();
		   tclp.GEN.generateRequest();
			String res = tclp.cleanURL(URL1);
			assertEquals("/addrbook/",res);
			assertNull(tclp.stripFile(res));
		}
		public void testcheckURL() throws Exception
		{
			assertFalse("URL is not have a query",tclp.checkURL(URL1));
			assertTrue("URL is a query",tclp.checkURL(URL2));
		}
	}

}