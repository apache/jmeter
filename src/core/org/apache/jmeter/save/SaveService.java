/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.save;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.NameUpdater;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.xml.sax.SAXException;

/**
 * This class provides a means for saving test results.  Test results are
 * typically saved in an XML file, but other storage mechanisms may also be
 * used, for instance, CSV files or databases.
 *
 * @author     Mike Stover
 * @author     <a href="mailto:kcassell&#X0040;apache.org">Keith Cassell</a>
 * @version    $Revision$ $Date$
 */
public final class SaveService implements SaveServiceConstants
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    protected static final int SAVE_NO_ASSERTIONS = 0;
    protected static final int SAVE_FIRST_ASSERTION = SAVE_NO_ASSERTIONS + 1;
    protected static final int SAVE_ALL_ASSERTIONS = SAVE_FIRST_ASSERTION + 1;

    /** A formatter for the time stamp. */
    protected static SimpleDateFormat formatter = null;

    /** A flag to indicate which output format to use for results. */
    protected static int outputFormat = SAVE_AS_XML;

    /** A flag to indicate whether to print the field names for delimited
        result files. */
    protected static boolean printFieldNames = false;

    /** A flag to indicate whether the data type should
        be saved to the test results. */
    protected static boolean saveDataType = true;

    /** A flag to indicate whether the assertion result's failure message
        should be saved to the test results. */
    protected static boolean saveAssertionResultsFailureMessage = false;

    /** A flag to indicate whether the label should be saved to the test
        results. */
    protected static boolean saveLabel = true;

    /** A flag to indicate whether the response code should be saved to the
        test results. */
    protected static boolean saveResponseCode = false;

    /** A flag to indicate whether the response data should be saved to the
        test results. */
    protected static boolean saveResponseData = false;

    /** A flag to indicate whether the response message should be saved to the
        test results. */
    protected static boolean saveResponseMessage = false;

    /** A flag to indicate whether the success indicator should be saved to the
        test results. */
    protected static boolean saveSuccessful = true;

    /** A flag to indicate whether the thread name should be saved to the test
        results. */
    protected static boolean saveThreadName = true;

    /** A flag to indicate whether the time should be saved to the test
        results. */
    protected static boolean saveTime = true;

    /** A flag to indicate the format of the time stamp within the test
        results. */
    protected static String timeStampFormat = MILLISECONDS;

    /** A flag to indicate whether the time stamp should be printed in
        milliseconds. */
    protected static boolean printMilliseconds = true;

    /** A flag to indicate which assertion results should be saved to the test
        results.  Legitimate values include none, first, all. */
    protected static String whichAssertionResults = FIRST;

    protected static int assertionsResultsToSave = SAVE_NO_ASSERTIONS;

    /** The string used to separate fields when stored to disk, for example,
        the comma for CSV files. */
    protected static String defaultDelimiter = ",";

    private static DefaultConfigurationBuilder builder =
        new DefaultConfigurationBuilder();

    // Initialize various variables based on properties.
    static {
        readProperties();
    } // static initialization

    /**
     * Private constructor to prevent instantiation.
     */
    private SaveService()
    {
    }

    /**
     * Read in the properties having to do with saving from a properties file.
     */
    protected static void readProperties()
    {
        Properties systemProps = System.getProperties();
        Properties props = new Properties(systemProps);

        try
        {
            props = JMeterUtils.getJMeterProperties();
        }
        catch (Exception e)
        {
            log.error(
                "SaveService.readProperties: Problem loading properties file: ",
                e);
        }

        printFieldNames =
            TRUE.equalsIgnoreCase(
                props.getProperty(PRINT_FIELD_NAMES_PROP, FALSE));

        saveDataType =
            TRUE.equalsIgnoreCase(props.getProperty(SAVE_DATA_TYPE_PROP, TRUE));

        saveLabel =
            TRUE.equalsIgnoreCase(props.getProperty(SAVE_LABEL_PROP, TRUE));

        saveResponseCode =
            TRUE.equalsIgnoreCase(
                props.getProperty(SAVE_RESPONSE_CODE_PROP, TRUE));

        saveResponseData =
            TRUE.equalsIgnoreCase(
                props.getProperty(SAVE_RESPONSE_DATA_PROP, FALSE));

        saveResponseMessage =
            TRUE.equalsIgnoreCase(
                props.getProperty(SAVE_RESPONSE_MESSAGE_PROP, TRUE));

        saveSuccessful =
            TRUE.equalsIgnoreCase(
                props.getProperty(SAVE_SUCCESSFUL_PROP, TRUE));

        saveThreadName =
            TRUE.equalsIgnoreCase(
                props.getProperty(SAVE_THREAD_NAME_PROP, TRUE));

        saveTime =
            TRUE.equalsIgnoreCase(props.getProperty(SAVE_TIME_PROP, TRUE));

        timeStampFormat =
            props.getProperty(TIME_STAMP_FORMAT_PROP, MILLISECONDS);

        printMilliseconds = MILLISECONDS.equalsIgnoreCase(timeStampFormat);

        // Prepare for a pretty date
        if (!printMilliseconds
            && !NONE.equalsIgnoreCase(timeStampFormat)
            && (timeStampFormat != null))
        {
            formatter = new SimpleDateFormat(timeStampFormat);
        }

        whichAssertionResults = props.getProperty(ASSERTION_RESULTS_PROP, NONE);
        saveAssertionResultsFailureMessage =
            TRUE.equalsIgnoreCase(
                props.getProperty(
                    ASSERTION_RESULTS_FAILURE_MESSAGE_PROP,
                    FALSE));

        if (NONE.equals(whichAssertionResults))
        {
            assertionsResultsToSave = SAVE_NO_ASSERTIONS;
        }
        else if (FIRST.equals(whichAssertionResults))
        {
            assertionsResultsToSave = SAVE_FIRST_ASSERTION;
        }
        else if (ALL.equals(whichAssertionResults))
        {
            assertionsResultsToSave = SAVE_ALL_ASSERTIONS;
        }

        String howToSave = props.getProperty(OUTPUT_FORMAT_PROP, XML);

        if (CSV.equals(howToSave))
        {
            outputFormat = SAVE_AS_CSV;
        }
        else
        {
            outputFormat = SAVE_AS_XML;
        }

        defaultDelimiter = props.getProperty(DEFAULT_DELIMITER_PROP, ",");
    }

    /**
     * Return the format for the saved results, e.g., csv or xml.
     * 
     * @return  the format for the saved results
     */
    public static int getOutputFormat()
    {
        return outputFormat;
    }

    /**
     * Return whether the field names should be printed to a delimited results
     * file.
     * @return  whether the field names should be printed
     */
    public static boolean getPrintFieldNames()
    {
        return printFieldNames;
    }
    
    /**
     * Make a SampleResult given a delimited string.
     * @param delim
     * @return
     * SampleResult
     */
    public static SampleResult makeResultFromDelimitedString(String delim)
    {
        SampleResult result = new SampleResult();
        StringTokenizer splitter = new StringTokenizer(delim,defaultDelimiter);
        String text = null;
        if (printMilliseconds)
        {
            text = splitter.nextToken();
            result.setTimeStamp(Long.parseLong(text));
        }
           else if (formatter != null)
           {
               text = splitter.nextToken();
               try
            {
                Date stamp = formatter.parse(text);
                   result.setTimeStamp(stamp.getTime());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
           }
        
           if (saveTime)
           {
               text = splitter.nextToken();
               result.setTime(Long.parseLong(text));
           }
        
           if (saveLabel)
           {
               text = splitter.nextToken();
               result.setSampleLabel(text);  
           }
           if (saveResponseCode)
           {
               text = splitter.nextToken();
               result.setResponseCode(text);
           }
        
           if (saveResponseMessage)
           {
               text = splitter.nextToken();
               result.setResponseMessage(text);
           }
        
           if (saveThreadName)
           {
               text = splitter.nextToken();
               result.setThreadName(text);
           }
        
           if (saveDataType)
           {
               text = splitter.nextToken();
               result.setDataType(text);
           }
        
           if (saveSuccessful)
           {
               text = splitter.nextToken();
               result.setSuccessful(new Boolean(text).booleanValue());
           }
        
           if (saveAssertionResultsFailureMessage)
           {
               text = splitter.nextToken();
           }
        return result;
    }

    /**
     * Return whether the field names should be printed to the output file.
     * @return whether the field names should be printed to the output file
     */
    public static String printableFieldNamesToString()
    {
        StringBuffer text = new StringBuffer();

        if (printMilliseconds || (formatter != null))
        {
            text.append(SaveServiceConstants.TIME_STAMP);
            text.append(defaultDelimiter);
        }

        if (saveTime)
        {
            text.append(SaveServiceConstants.TIME);
            text.append(defaultDelimiter);
        }

        if (saveLabel)
        {
            text.append(SaveServiceConstants.LABEL);
            text.append(defaultDelimiter);
        }

        if (saveResponseCode)
        {
            text.append(SaveServiceConstants.RESPONSE_CODE);
            text.append(defaultDelimiter);
        }

        if (saveResponseMessage)
        {
            text.append(SaveServiceConstants.RESPONSE_MESSAGE);
            text.append(defaultDelimiter);
        }

        if (saveThreadName)
        {
            text.append(SaveServiceConstants.THREAD_NAME);
            text.append(defaultDelimiter);
        }

        if (saveDataType)
        {
            text.append(SaveServiceConstants.DATA_TYPE);
            text.append(defaultDelimiter);
        }

        if (saveSuccessful)
        {
            text.append(SaveServiceConstants.SUCCESSFUL);
            text.append(defaultDelimiter);
        }

        if (saveAssertionResultsFailureMessage)
        {
            text.append(SaveServiceConstants.FAILURE_MESSAGE);
            text.append(defaultDelimiter);
        }

        String resultString = null;
        int size = text.length();
        int delSize = defaultDelimiter.length();

        // Strip off the trailing delimiter
        if (size >= delSize)
        {
            resultString = text.substring(0, size - delSize);
        }
        else
        {
            resultString = text.toString();
        }
        return resultString;
    }

    public static void saveSubTree(HashTree subTree, OutputStream writer)
        throws IOException
    {
        Configuration config =
            (Configuration) getConfigsFromTree(subTree).get(0);
        DefaultConfigurationSerializer saver =
            new DefaultConfigurationSerializer();

        saver.setIndent(true);
        try
        {
            saver.serialize(writer, config);
        }
        catch (SAXException e)
        {
            throw new IOException("SAX implementation problem");
        }
        catch (ConfigurationException e)
        {
            throw new IOException("Problem using Avalon Configuration tools");
        }
    }

    public static SampleResult getSampleResult(Configuration config)
    {
        SampleResult result = new SampleResult();

        result.setThreadName(config.getAttribute(THREAD_NAME, ""));
        result.setDataType(config.getAttribute(DATA_TYPE, ""));
        result.setResponseCode(config.getAttribute(RESPONSE_CODE, ""));
        result.setResponseMessage(config.getAttribute(RESPONSE_MESSAGE, ""));
        result.setTime(config.getAttributeAsLong(TIME, 0L));
        result.setTimeStamp(config.getAttributeAsLong(TIME_STAMP, 0L));
        result.setSuccessful(config.getAttributeAsBoolean(SUCCESSFUL, false));
        result.setSampleLabel(config.getAttribute(LABEL, ""));
        result.setResponseData(getBinaryData(config.getChild(BINARY)));
        Configuration[] subResults = config.getChildren(SAMPLE_RESULT_TAG_NAME);

        for (int i = 0; i < subResults.length; i++)
        {
            result.addSubResult(getSampleResult(subResults[i]));
        }
        Configuration[] assResults =
            config.getChildren(ASSERTION_RESULT_TAG_NAME);

        for (int i = 0; i < assResults.length; i++)
        {
            result.addAssertionResult(getAssertionResult(assResults[i]));
        }

        Configuration[] samplerData = config.getChildren("property");
        for (int i = 0; i < samplerData.length; i++)
        {
            result.setSamplerData(samplerData[i].getValue(""));
        }
        return result;
    }

    private static List getConfigsFromTree(HashTree subTree)
    {
        Iterator iter = subTree.list().iterator();
        List configs = new LinkedList();

        while (iter.hasNext())
        {
            TestElement item = (TestElement) iter.next();
            DefaultConfiguration config =
                new DefaultConfiguration("node", "node");

            config.addChild(getConfigForTestElement(null, item));
            List configList = getConfigsFromTree(subTree.getTree(item));
            Iterator iter2 = configList.iterator();

            while (iter2.hasNext())
            {
                config.addChild((Configuration) iter2.next());
            }
            configs.add(config);
        }
        return configs;
    }

    public static Configuration getConfiguration(byte[] bin)
    {
        DefaultConfiguration config =
            new DefaultConfiguration(BINARY, "JMeter Save Service");

        try
        {
            config.setValue(new String(bin, "utf-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            log.error("", e);
        }
        return config;
    }

    public static byte[] getBinaryData(Configuration config)
    {
        if (config == null)
        {
            return new byte[0];
        }
        try
        {
            return config.getValue("").getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return new byte[0];
        }
    }

    public static AssertionResult getAssertionResult(Configuration config)
    {
        AssertionResult result = new AssertionResult();
        result.setError(config.getAttributeAsBoolean(ERROR, false));
        result.setFailure(config.getAttributeAsBoolean(FAILURE, false));
        result.setFailureMessage(config.getAttribute(FAILURE_MESSAGE, ""));
        return result;
    }

    public static Configuration getConfiguration(AssertionResult assResult)
    {
        DefaultConfiguration config =
            new DefaultConfiguration(
                ASSERTION_RESULT_TAG_NAME,
                "JMeter Save Service");

        config.setAttribute(FAILURE_MESSAGE, assResult.getFailureMessage());
        config.setAttribute(ERROR, "" + assResult.isError());
        config.setAttribute(FAILURE, "" + assResult.isFailure());
        return config;
    }

    /**
     * This method determines the content of the result data that will be
     * stored.
     * @param result   the object containing all of the data that has been
     *                 collected.
     * @param funcTest an indicator of whether the user wants all data
     *                 recorded.
     */
    public static Configuration getConfiguration(
        SampleResult result,
        boolean funcTest)
    {
        DefaultConfiguration config =
            new DefaultConfiguration(
                SAMPLE_RESULT_TAG_NAME,
                "JMeter Save Service");

        if (saveTime)
        {
            config.setAttribute(TIME, "" + result.getTime());
        }
        if (saveLabel)
        {
            config.setAttribute(LABEL, result.getSampleLabel());
        }
        if (saveResponseCode)
        {
            config.setAttribute(RESPONSE_CODE, result.getResponseCode());
        }
        if (saveResponseMessage)
        {
            config.setAttribute(RESPONSE_MESSAGE, result.getResponseMessage());
        }
        if (saveThreadName)
        {
            config.setAttribute(THREAD_NAME, result.getThreadName());
        }
        if (saveDataType)
        {
            config.setAttribute(DATA_TYPE, result.getDataType());
        }

        if (printMilliseconds)
        {
            config.setAttribute(TIME_STAMP, "" + result.getTimeStamp());
        }
        else if (formatter != null)
        {
            String stamp = formatter.format(new Date(result.getTimeStamp()));

            config.setAttribute(TIME_STAMP, stamp);
        }

        if (saveSuccessful)
        {
            config.setAttribute(
                SUCCESSFUL,
                new Boolean(result.isSuccessful()).toString());
        }

        SampleResult[] subResults = result.getSubResults();

        if (subResults != null)
        {
            for (int i = 0; i < subResults.length; i++)
            {
                config.addChild(getConfiguration(subResults[i], funcTest));
            }
        }

        AssertionResult[] assResults = result.getAssertionResults();

        if (funcTest)
        {
            config.addChild(
                createConfigForString("samplerData", result.getSamplerData()));
            if (assResults != null)
            {
                for (int i = 0; i < assResults.length; i++)
                {
                    config.addChild(getConfiguration(assResults[i]));
                }
            }
            config.addChild(getConfiguration(result.getResponseData()));
        }
        // Determine which of the assertion results to save and
        // whether to save the response data
        else
        {
            if (assertionsResultsToSave == SAVE_ALL_ASSERTIONS)
            {
                config.addChild(
                    createConfigForString(
                        "samplerData",
                        result.getSamplerData()));
                if (assResults != null)
                {
                    for (int i = 0; i < assResults.length; i++)
                    {
                        config.addChild(getConfiguration(assResults[i]));
                    }
                }
            }
            else if (
                (assertionsResultsToSave == SAVE_FIRST_ASSERTION)
                    && assResults != null
                    && assResults.length > 0)
            {
                config.addChild(getConfiguration(assResults[0]));
            }

            if (saveResponseData)
            {
                config.addChild(getConfiguration(result.getResponseData()));
            }
        }
        return config;
    }

    /**
     * Convert a result into a string, where the fields of the result are
     * separated by the default delimiter.
     * 
     * @param sample the test result to be converted
     * @return       the separated value representation of the result
     */
    public static String resultToDelimitedString(SampleResult sample)
    {
        return resultToDelimitedString(sample, defaultDelimiter);
    }

    /**
     * Convert a result into a string, where the fields of the result are
     * separated by a specified String.
     * 
     * @param sample    the test result to be converted
     * @param delimiter the separation string
     * @return          the separated value representation of the result
     */
    public static String resultToDelimitedString(
        SampleResult sample,
        String delimiter)
    {
        StringBuffer text = new StringBuffer();

        if (printMilliseconds)
        {
            text.append("" + sample.getTimeStamp());
            text.append(delimiter);
        }
        else if (formatter != null)
        {
            String stamp = formatter.format(new Date(sample.getTimeStamp()));
            text.append(stamp);
            text.append(delimiter);
        }

        if (saveTime)
        {
            text.append("" + sample.getTime());
            text.append(delimiter);
        }

        if (saveLabel)
        {
            text.append(sample.getSampleLabel());
            text.append(delimiter);
        }

        if (saveResponseCode)
        {
            text.append(sample.getResponseCode());
            text.append(delimiter);
        }

        if (saveResponseMessage)
        {
            text.append(sample.getResponseMessage());
            text.append(delimiter);
        }

        if (saveThreadName)
        {
            text.append(sample.getThreadName());
            text.append(delimiter);
        }

        if (saveDataType)
        {
            text.append(sample.getDataType());
            text.append(delimiter);
        }

        if (saveSuccessful)
        {
            text.append("" + sample.isSuccessful());
            text.append(delimiter);
        }

        if (saveAssertionResultsFailureMessage)
        {
            String message = null;
            AssertionResult[] results = sample.getAssertionResults();

            if ((results != null) && (results.length > 0))
            {
                message = results[0].getFailureMessage();
            }

            if (message == null)
            {
                message = "";
            }
            text.append(message);
            text.append(delimiter);
        }
        // text.append(sample.getSamplerData().toString());
        // text.append(getAssertionResult(sample));

        String resultString = null;
        int size = text.length();
        int delSize = delimiter.length();

        // Strip off the trailing delimiter
        if (size >= delSize)
        {
            resultString = text.substring(0, size - delSize);
        }
        else
        {
            resultString = text.toString();
        }
        return resultString;
    }

    public static Configuration getConfigForTestElement(
        String named,
        TestElement item)
    {
        TestElementSaver saver = new TestElementSaver(named);
        item.traverse(saver);
        Configuration config = saver.getConfiguration();
        /* DefaultConfiguration config =
           new DefaultConfiguration("testelement", "testelement");
        
         if (named != null)
         {
             config.setAttribute("name", named);
         }
         if (item.getProperty(TestElement.TEST_CLASS) != null)
         {
             config.setAttribute("class",
                    (String) item.getProperty(TestElement.TEST_CLASS));
         }
         else
         {
             config.setAttribute("class", item.getClass().getName());
         }
         Iterator iter = item.getPropertyNames().iterator();
        
         while (iter.hasNext())
         {
             String name = (String) iter.next();
             Object value = item.getProperty(name);
        
             if (value instanceof TestElement)
             {
                 config.addChild(getConfigForTestElement(name,
                        (TestElement) value));
             }
             else if (value instanceof Collection)
             {
                 config.addChild(createConfigForCollection(name,
                        (Collection) value));
             }
             else if (value != null)
             {
                 config.addChild(createConfigForString(name, value.toString()));
             }
         }*/
        return config;
    }

    private static Configuration createConfigForCollection(
        String propertyName,
        Collection list)
    {
        DefaultConfiguration config =
            new DefaultConfiguration("collection", "collection");

        if (propertyName != null)
        {
            config.setAttribute("name", propertyName);
        }
        config.setAttribute("class", list.getClass().getName());
        Iterator iter = list.iterator();

        while (iter.hasNext())
        {
            Object item = iter.next();

            if (item instanceof TestElement)
            {
                config.addChild(
                    getConfigForTestElement(null, (TestElement) item));
            }
            else if (item instanceof Collection)
            {
                config.addChild(
                    createConfigForCollection(null, (Collection) item));
            }
            else
            {
                config.addChild(createConfigForString(item.toString()));
            }
        }
        return config;
    }

    private static Configuration createConfigForString(String value)
    {
        DefaultConfiguration config =
            new DefaultConfiguration("string", "string");

        config.setValue(value);
        config.setAttribute(XML_SPACE, PRESERVE);
        return config;
    }

    private static Configuration createConfigForString(
        String name,
        String value)
    {
        if (value == null)
        {
            value = "";
        }
        DefaultConfiguration config =
            new DefaultConfiguration("property", "property");

        config.setAttribute("name", name);
        config.setValue(value);
        config.setAttribute(XML_SPACE, PRESERVE);
        return config;
    }

    public synchronized static HashTree loadSubTree(InputStream in)
        throws IOException
    {
        try
        {
            Configuration config = builder.build(in);
            HashTree loadedTree = generateNode(config);

            return loadedTree;
        }
        catch (ConfigurationException e)
        {
            String message = "Problem loading using Avalon Configuration tools";
            log.error(message, e);
            throw new IOException(message);
        }
        catch (SAXException e)
        {
            String message = "Problem with SAX implementation";
            log.error(message, e);
            throw new IOException(message);
        }
    }

    public static TestElement createTestElement(Configuration config)
        throws
            ConfigurationException,
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException
    {
        TestElement element = null;

        element =
            (TestElement) Class
                .forName(
                    NameUpdater.getCurrentName(
                        (String) config.getAttribute("class")))
                .newInstance();
        Configuration[] children = config.getChildren();

        for (int i = 0; i < children.length; i++)
        {
            if (children[i].getName().equals("property"))
            {
                try
                {
                    element.setProperty(createProperty(children[i]));
                }
                catch (Exception ex)
                {
                    log.error("Problem loading property", ex);
                    element.setProperty(children[i].getAttribute("name"), "");
                }
            }
            else if (children[i].getName().equals("testelement"))
            {
                element.setProperty(
                    new TestElementProperty(
                        children[i].getAttribute("name",""),
                        createTestElement(children[i])));
            }
            else if (children[i].getName().equals("collection"))
            {
                element.setProperty(
                    new CollectionProperty(
                        children[i].getAttribute("name",""),
                        createCollection(children[i])));
            }
            else if (children[i].getName().equals("map"))
            {
                element.setProperty(
                    new MapProperty(
                        children[i].getAttribute("name",""),
                        createMap(children[i])));
            }
        }
        return element;
    }

    private static Collection createCollection(Configuration config)
        throws
            ConfigurationException,
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException
    {
        Collection coll =
            (Collection) Class
                .forName((String) config.getAttribute("class"))
                .newInstance();
        Configuration[] items = config.getChildren();

        for (int i = 0; i < items.length; i++)
        {
            if (items[i].getName().equals("property"))
            {
                coll.add(createProperty(items[i]));
            }
            else if (items[i].getName().equals("testelement"))
            {
                coll.add(
                    new TestElementProperty(
                        items[i].getAttribute("name", ""),
                        createTestElement(items[i])));
            }
            else if (items[i].getName().equals("collection"))
            {
                coll.add(
                    new CollectionProperty(
                        items[i].getAttribute("name", ""),
                        createCollection(items[i])));
            }
            else if (items[i].getName().equals("string"))
            {
                coll.add(createProperty(items[i]));
            }
            else if (items[i].getName().equals("map"))
            {
                coll.add(
                    new MapProperty(
                        items[i].getAttribute("name", ""),
                        createMap(items[i])));
            }
        }
        return coll;
    }

    private static JMeterProperty createProperty(Configuration config)
        throws
            ConfigurationException,
            IllegalAccessException,
            ClassNotFoundException,
            InstantiationException
    {
        String value = config.getValue("");
        JMeterProperty prop = (JMeterProperty) Class.forName(
                config.getAttribute(
                    "propType",
                    "org.apache.jmeter.testelement.property.StringProperty"))
                .newInstance();
        prop.setName(config.getAttribute("name", value));
        prop.setObjectValue(value);
        return prop;
    }

    private static Map createMap(Configuration config)
        throws
            ConfigurationException,
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException
    {
        Map map =
            (Map) Class
                .forName((String) config.getAttribute("class"))
                .newInstance();
        Configuration[] items = config.getChildren();

        for (int i = 0; i < items.length; i++)
        {
            if (items[i].getName().equals("property"))
            {
                JMeterProperty prop = createProperty(items[i]);
                map.put(prop.getName(), prop);
            }
            else if (items[i].getName().equals("testelement"))
            {
                map.put(
                    items[i].getAttribute("name",""),
                    new TestElementProperty(
                        items[i].getAttribute("name", ""),
                        createTestElement(items[i])));
            }
            else if (items[i].getName().equals("collection"))
            {
                map.put(
                    items[i].getAttribute("name"),
                    new CollectionProperty(
                        items[i].getAttribute("name", ""),
                        createCollection(items[i])));
            }
            else if (items[i].getName().equals("map"))
            {
                map.put(
                    items[i].getAttribute("name", ""),
                    new MapProperty(
                        items[i].getAttribute("name", ""),
                        createMap(items[i])));
            }
        }
        return map;
    }

    private static HashTree generateNode(Configuration config)
    {
        TestElement element = null;

        try
        {
            element = createTestElement(config.getChild("testelement"));
        }
        catch (Exception e)
        {
            log.error("Problem loading part of file", e);
            return null;
        }
        HashTree subTree = new ListedHashTree(element);
        Configuration[] subNodes = config.getChildren("node");

        for (int i = 0; i < subNodes.length; i++)
        {
            HashTree t = generateNode(subNodes[i]);

            if (t != null)
            {
                subTree.add(element, t);
            }
        }
        return subTree;
    }

    public static class Test extends TestCase
    {
        private static final String[] FILES =
            new String[] {
                "AssertionTestPlan.jmx",
                "AuthManagerTestPlan.jmx",
                "HeaderManagerTestPlan.jmx",
                "InterleaveTestPlan2.jmx",
                "InterleaveTestPlan.jmx",
                "LoopTestPlan.jmx",
                "Modification Manager.jmx",
                "OnceOnlyTestPlan.jmx",
                "proxy.jmx",
                "ProxyServerTestPlan.jmx",
                "SimpleTestPlan.jmx",
                };

        public Test(String name)
        {
            super(name);
        }

        public void setUp()
        {}

        public void testLoadAndSave() throws java.io.IOException
        {
            byte[] original = new byte[1000000];

            for (int i = 0; i < FILES.length; i++)
            {
                InputStream in =
                    new FileInputStream(new File("testfiles/" + FILES[i]));
                int len = in.read(original);

                in.close();

                in = new ByteArrayInputStream(original, 0, len);
                HashTree tree = loadSubTree(in);

                in.close();

                ByteArrayOutputStream out = new ByteArrayOutputStream(1000000);

                saveSubTree(tree, out);
                out.close();

                // We only check the length of the result. Comparing the
                // actual result (out.toByteArray==original) will usually
                // fail, because the order of the properties within each
                // test element may change. Comparing the lengths should be
                // enough to detect most problem cases...
                if (len != out.size())
                {
                    fail(
                        "Loading file bin/testfiles/"
                            + FILES[i]
                            + " and "
                            + "saving it back changes its contents.");
                }

                // Note this test will fail if a property is added or
                // removed to any of the components used in the test
                // files. The way to solve this is to appropriately change
                // the test file.
            }
        }
    }
}
