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

package org.apache.jmeter.protocol.http.sampler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.protocol.http.util.accesslog.LogParser;
import org.apache.jmeter.protocol.http.util.accesslog.Generator;
//import org.apache.jmeter.protocol.http.util.accesslog.LogFilter;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Description:<br>
 * <br>
 * AccessLogSampler is responsible for a couple of things:<p>
 * <ul>
 * <li> creating instances of Generator
 * <li> creating instances of Parser
 * <li> triggering popup windows
 * <li> calling Generator.generateRequest()
 * <li> checking to make sure the classes are valid
 * <li> making sure a class can be instantiated
 * </ul>
 * The intent of this sampler is it uses the generator and
 * parser to create a HTTPSampler when it is needed. It
 * does not contain logic about how to parse the logs. It
 * also doesn't care how Generator is implemented, as long
 * as it implements the interface. This means a person
 * could simply implement a dummy parser to generate
 * random parameters and the generator consumes the results.
 * This wasn't the original intent of the sampler. I
 * originaly wanted to write this sampler, so that I can
 * take production logs to simulate production traffic in
 * a test environment. Doing so is desirable to study odd
 * or unusual behavior. It's also good to compare a new
 * system against an existing system to get near apples-
 * to-apples comparison. I've been asked if benchmarks
 * are really fair comparisons just about every single
 * time, so this helps me accomplish that task.<p>
 * Some bugs only appear under production traffic, so it
 * is useful to generate traffic using production logs.
 * This way, JMeter can record when problems occur and
 * provide a way to match the server logs.
 * <p>
 * Created on:  Jun 26, 2003
 * 
 * @author Peter Lin
 * @version $Revision$ last updated $Date$
 */
public class AccessLogSampler extends HTTPSampler
{
	public static final String DEFAULT_CLASS =
		"org.apache.jmeter.protocol.http.util.accesslog.TCLogParser";

    public static final String LOG_FILE =
        "AccessLogSampler.log_file";
    public static final String PARSER_CLASS_NAME = "AccessLogSampler.parser_class_name";
	public static final String GENERATOR_CLASS_NAME = "AccessLogSampler.generator_class_name";

	/** private members used by class **/
	transient private Generator GENERATOR = null;
	transient private LogParser PARSER = null;
	//transient private LogFilter FILTER = null; //TODO not used
	private Class GENERATORCLASS = null;
	private Class PARSERCLASS = null;

    /**
     * Set the path where XML messages are stored for random selection.
     */
    public void setLogFile(String path)
    {
        setProperty(LOG_FILE, path);
    }

    /**
     * Get the path where XML messages are stored. this is the directory where
     * JMeter will randomly select a file.
     */
    public String getLogFile()
    {
        return getPropertyAsString(LOG_FILE);
    }

    /**
     * it's kinda obvious, but we state it anyways.  Set the xml file with a
     * string path.
     * @param classname - parser class name
     */
    public void setParserClassName(String classname)
    {
        setProperty(PARSER_CLASS_NAME, classname);
    }

    /**
     * Get the file location of the xml file.
     * @return String file path.
     */
    public String getParserClassName()
    {
        return getPropertyAsString(PARSER_CLASS_NAME);
    }

	/**
	 * We give the user the choice of entering their own generator
	 * instead of the default generator. This also has the positive
	 * side effect that users can provide their own generators
	 * without providing a parser. Things like creating a test plan
	 * that randomly generates requests can be support in a simple
	 * way for programmers. Non programmers should use existing
	 * features to accomplish similar task.
	 * @param classname
	 */
	public void setGeneratorClassName(String classname){
		setProperty(GENERATOR_CLASS_NAME, classname);
	}

	/**
	 * Return the name of the generator class to use.
	 * @return generator class name
	 */	
	public String getGeneratorClassName(){
		return getPropertyAsString(GENERATOR_CLASS_NAME);
	}

	/**
	 * Set the generator for the Sampler to use
	 * @param gen
	 */	
	public void setGenerator(Generator gen){
		if (gen == null){
		} else {
			GENERATOR = gen;
		}
	}

	/**
	 * Return the generator
	 * @return generator
	 */
	public Generator getGenerator(){
		return GENERATOR;
	}
	
	/**
	 * Set the parser for the sampler to use
	 * @param parser
	 */	
	public void setParser(LogParser parser){
		PARSER = parser;
	}

	/**
	 * Return the parser
	 * @return parser
	 */
	public LogParser getParser(){
		return PARSER;	
	}
	
	/**
	 * sample gets a new HTTPSampler from the generator and
	 * calls it's sample() method.
	 */
	public SampleResult sampleWithGenerator()
	{
		checkParser();
		checkGenerator();
		this.instantiateParser();
		this.instantiateGenerator();
		HTTPSampler samp = null;
		SampleResult res = null;
        try
        {
            samp = (HTTPSampler) GENERATOR.generateRequest();
            
            /*samp.setDomain(this.getDomain());
            samp.setPort(this.getPort());
            samp.setImageParser(this.isImageParser());*/
            // we call parse with 1 to get only one.
            // this also means if we change the implementation
            // to use 2, it would use every other entry and
            // so on. Not that it is really useful, but a
            // person could use it that way if they have a
            // huge gigabyte log file and they only want to
            // use a quarter of the entries.
			PARSER.parse(1);
			this.addTestElement(samp);
            res = sample();
            res.setSampleLabel(toString());
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
		return res;
	}

	/**
	 * sample(Entry e) simply calls sample().
	 * @param e - ignored
	 * @return the new sample
	 */
	public SampleResult sample(Entry e)
	{
		return sampleWithGenerator();
	}

	/**
	 * Method will instantiate the generator. This is done to
	 * make it easier for users to extend and plugin their
	 * own generators.
	 */
	public void instantiateGenerator(){
		if (this.getGeneratorClassName() != null
			&& this.getGeneratorClassName().length() > 0)
		{
			try
			{
				GENERATOR = (Generator) GENERATORCLASS.newInstance();
				if (GENERATOR != null)
				{
					if (this.getDomain() != null)
					{
						GENERATOR.setHost(this.getDomain());
					}
					if (this.getPort() > 0)
					{
						GENERATOR.setPort(this.getPort());
					}
				}
				if (PARSER != null && GENERATOR != null)
				{
					PARSER.setGenerator(GENERATOR);
				}
			}
			catch (InstantiationException e)
			{
				// e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				// e.printStackTrace();
			}
		}
	}
	
	/**
	 * Method will instantiate the generator. This is done to
	 * make it easier for users to extend and plugin their
	 * own generators.
	 */
	public boolean checkGenerator(){
		if (this.getGeneratorClassName() != null
			&& this.getGeneratorClassName().length() > 0)
		{
			try
			{
				if (GENERATORCLASS == null)
				{
					GENERATORCLASS =
						Class.forName(this.getGeneratorClassName());
				}
				return true;
			}
			catch (ClassNotFoundException e)
			{
				// since samplers shouldn't deal with
				// gui stuff, bad class names will
				// fail silently.
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Method will instantiate the log parser based on
	 * the class in the text field. This was done to
	 * make it easier for people to plugin their own log parser
	 * and use different log parser.
	 */
	public void instantiateParser()
	{
		if (this.getParserClassName() != null && this.getParserClassName().length() > 0)
		{
			try
			{
				if (PARSER == null)
				{
					if (this.getLogFile() != null
						&& this.getLogFile().length() > 0)
					{
						PARSER = (LogParser) PARSERCLASS.newInstance();
						PARSER.setSourceFile(this.getLogFile());
					}
				}
			}
			catch (InstantiationException e)
			{
				// since samplers shouldn't deal with
				// gui stuff, bad class names will
				// fail silently.
			}
			catch (IllegalAccessException e)
			{
				// since samplers shouldn't deal with
				// gui stuff, bad class names will
				// fail silently.
			}
		}
	}

	/**
	 * Method will instantiate the log parser based on
	 * the class in the text field. This was done to
	 * make it easier for people to plugin their own log parser
	 * and use different log parser.
	 * @return true if parser exists or was created
	 */
	public boolean checkParser()
	{
		if (this.getParserClassName() != null && this.getParserClassName().length() > 0)
		{
			try
			{
				if (PARSERCLASS == null)
				{
					PARSERCLASS = Class.forName(this.getParserClassName());
				}
				return true;
			}
			catch (ClassNotFoundException e)
			{
				// since samplers shouldn't deal with
				// gui stuff, bad class names will
				// fail silently.
				return false;
			}
		} else {
			return false;
		}
	}

    /**
     * We override this to prevent the wrong encoding and provide no
     * implementation. We want to reuse the other parts of HTTPSampler, but not
     * the connection. The connection is handled by the Apache SOAP driver.
     */
    public void addEncodedArgument(String name, String value, String metaData)
    {
    }

    /**
     * We override this to prevent the wrong encoding and provide no
     * implementation. We want to reuse the other parts of HTTPSampler, but not
     * the connection. The connection is handled by the Apache SOAP driver.
     */
    protected HttpURLConnection setupConnection(URL u, String method)
        throws IOException
    {
        return null;
    }

    /**
     * We override this to prevent the wrong encoding and provide no
     * implementation. We want to reuse the other parts of HTTPSampler, but not
     * the connection. The connection is handled by the Apache SOAP driver.
     */
    protected long connect() throws IOException
    {
        return -1;
    }
}
