/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.xml.sax.SAXException;

/************************************************************
 *  This class provides a means for saving test results.  Test
 *  results are typically saved in an XML file, but other
 *  storage mechanisms may also be used, for instance, CSV
 *  files or databases.
 *
 *@author     <a href="mailto:kcassell&#X0040;apache.org">Keith Cassell</a>
 *@created    $Date$
 *@version    $Revision$ $Date$
 ***********************************************************/

public class SaveService implements SaveServiceConstants
{
        transient private static Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.util");

        protected static final int SAVE_NO_ASSERTIONS = 0;
        protected static final int SAVE_FIRST_ASSERTION = SAVE_NO_ASSERTIONS + 1;
        protected static final int SAVE_ALL_ASSERTIONS = SAVE_FIRST_ASSERTION + 1;;

        /** A formatter for the time stamp.  **/
        protected static SimpleDateFormat formatter = null;

        /** A flag to indicate whether the data type should
            be saved to the test results.  **/
        protected static boolean saveDataType = true;

        /** A flag to indicate whether the label should
            be saved to the test results.  **/
        protected static boolean saveLabel = true;

        /** A flag to indicate whether the response code should
            be saved to the test results.  **/
        protected static boolean saveResponseCode = false;

        /** A flag to indicate whether the response data should
            be saved to the test results.  **/
        protected static boolean saveResponseData = false;

        /** A flag to indicate whether the response message should
            be saved to the test results.  **/
        protected static boolean saveResponseMessage = false;

        /** A flag to indicate whether the success indicator should
            be saved to the test results.  **/
        protected static boolean saveSuccessful = true;

        /** A flag to indicate whether the thread name should
            be saved to the test results.  **/
        protected static boolean saveThreadName = true;

        /** A flag to indicate whether the time should
            be saved to the test results.  **/
        protected static boolean saveTime = true;

        /** A flag to indicate the format of the time stamp within
            the test results.  **/
        protected static String timeStampFormat = MILLISECONDS;

        /** A flag to indicate whether the time stamp should be printed
            in milliseconds.  **/
        protected static boolean printMilliseconds = true;

        /** A flag to indicate which assertion results should
            be saved to the test results.  Legitimate values include
            none, first, all.  **/
        protected static String whichAssertionResults = FIRST;

        protected static int assertionsResultsToSave = SAVE_NO_ASSERTIONS;

        
        private static DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();


        // Initialize various variables based on properties.
        static
        {
                readAndSetSaveProperties();
        }       // static initialization


        public SaveService()
        {
        }

        protected static void readAndSetSaveProperties()
        {
                Properties systemProps = System.getProperties();
                Properties props = new Properties(systemProps);

                try
                {
                        props = JMeterUtils.getProperties(PROPS_FILE);
                }
                catch (Exception e)
                {
                        log.error("SaveService.readAndSetSaveProperties: Problem loading properties file " + PROPS_FILE, e);
                }

                saveDataType =
                        TRUE.equalsIgnoreCase(props.getProperty(SAVE_DATA_TYPE_PROP,
                                                                TRUE));
                saveLabel =
                        TRUE.equalsIgnoreCase(props.getProperty(SAVE_LABEL_PROP,
                                                                TRUE));
                saveResponseCode =
                        TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_CODE_PROP,
                                                                TRUE));
                saveResponseData =
                        TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_DATA_PROP,
                                                                FALSE));
                saveResponseMessage =
                        TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_MESSAGE_PROP,
                                                                TRUE));
                saveSuccessful =
                        TRUE.equalsIgnoreCase(props.getProperty(SAVE_SUCCESSFUL_PROP,
                                                                TRUE));
                saveThreadName =
                        TRUE.equalsIgnoreCase(props.getProperty(SAVE_THREAD_NAME_PROP,
                                                                TRUE));
                saveTime =
                        TRUE.equalsIgnoreCase(props.getProperty(SAVE_TIME_PROP,
                                                                TRUE));
                timeStampFormat =
                        props.getProperty(TIME_STAMP_FORMAT_PROP, MILLISECONDS);
                printMilliseconds = MILLISECONDS.equalsIgnoreCase(timeStampFormat);
                // 
                if (!printMilliseconds
                    && !NONE.equalsIgnoreCase(timeStampFormat)
                    && (timeStampFormat != null))
                {
                        formatter = new SimpleDateFormat(timeStampFormat);
                }

                whichAssertionResults = props.getProperty(ASSERTION_RESULTS_PROP,
                                                          NONE);

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
        }


        public static void saveSubTree(HashTree subTree,OutputStream writer) throws
                        IOException
        {
                Configuration config = (Configuration)getConfigsFromTree(subTree).get(0);
                DefaultConfigurationSerializer saver = new DefaultConfigurationSerializer();
                saver.setIndent(true);
                try
                {
                        saver.serialize(writer,config);
                }
                catch(SAXException e)
                {
                        throw new IOException("SAX implementation problem");
                }
                catch(ConfigurationException e)
                {
                        throw new IOException("Problem using Avalon Configuration tools");
                }
        }
        
        public static SampleResult getSampleResult(Configuration config)
        {
                SampleResult result = new SampleResult();
                result.setThreadName(config.getAttribute(THREAD_NAME,""));
                result.setDataType(config.getAttribute(DATA_TYPE,""));
                result.setResponseCode(config.getAttribute(RESPONSE_CODE,""));
                result.setResponseMessage(config.getAttribute(RESPONSE_MESSAGE,""));
                result.setTime(config.getAttributeAsLong(TIME,0L));
                result.setTimeStamp(config.getAttributeAsLong(TIME_STAMP,0L));
                result.setSuccessful(config.getAttributeAsBoolean(SUCCESSFUL,false));
                result.setSampleLabel(config.getAttribute(LABEL,""));
                result.setResponseData(getBinaryData(config.getChild(BINARY)));
                Configuration[] subResults = config.getChildren(SAMPLE_RESULT_TAG_NAME);
                for(int i = 0;i < subResults.length;i++)
                {
                        result.addSubResult(getSampleResult(subResults[i]));
                }
                Configuration[] assResults = config.getChildren(ASSERTION_RESULT_TAG_NAME);
                for(int i = 0;i < assResults.length;i++)
                {
                        result.addAssertionResult(getAssertionResult(assResults[i]));
                }
                return result;
        }

        private static List getConfigsFromTree(HashTree subTree)
        {
                Iterator iter = subTree.list().iterator();
                List configs = new LinkedList();
                while (iter.hasNext())
                {
                        TestElement item = (TestElement)iter.next();
                        DefaultConfiguration config = new DefaultConfiguration("node","node");
                        config.addChild(getConfigForTestElement(null,item));
                        List configList = getConfigsFromTree(subTree.getTree(item));
                        Iterator iter2 = configList.iterator();
                        while(iter2.hasNext())
                        {
                                config.addChild((Configuration)iter2.next());
                        }
                        configs.add(config);
                }
                return configs;
        }
        
        public static Configuration getConfiguration(byte[] bin)
        {
                DefaultConfiguration config = new DefaultConfiguration(BINARY,"JMeter Save Service");
                try {
                        config.setValue(new String(bin,"utf-8"));
                } catch(UnsupportedEncodingException e) {
                        log.error("",e);
                }
                return config;
        }
        
        public static byte[] getBinaryData(Configuration config)
        {
                if(config == null)
                {
                        return new byte[0];
                }
                try {
                        return config.getValue("").getBytes("utf-8");
                } catch(UnsupportedEncodingException e) {
                        return new byte[0];
                }
        }
        
        public static AssertionResult getAssertionResult(Configuration config)
        {
                AssertionResult result = new AssertionResult();
                result.setError(config.getAttributeAsBoolean(ERROR,false));
                result.setFailure(config.getAttributeAsBoolean(FAILURE,false));
                result.setFailureMessage(config.getAttribute(FAILURE_MESSAGE,""));
                return result;          
        }
        
        public static Configuration getConfiguration(AssertionResult assResult)
        {
                DefaultConfiguration config = new DefaultConfiguration(ASSERTION_RESULT_TAG_NAME,
                                "JMeter Save Service");
                config.setAttribute(FAILURE_MESSAGE,assResult.getFailureMessage());
                config.setAttribute(ERROR,""+assResult.isError());
                config.setAttribute(FAILURE,""+assResult.isFailure());
                return config;          
        }
        

        /**
           This method determines the content of the result data that
               will be stored.
           @param result the object containing all of the data that has
               been collected.
           @param funcTest an indicator of whether the user wants all
               data recorded.
        **/

        public static Configuration getConfiguration(SampleResult result,boolean funcTest)
        {
                DefaultConfiguration config = new DefaultConfiguration(SAMPLE_RESULT_TAG_NAME,"JMeter Save Service");

                if (saveTime)
                {
                        config.setAttribute(TIME,""+result.getTime());
                }
                if (saveLabel)
                {
                        config.setAttribute(LABEL,result.getSampleLabel());
                }
                if (saveResponseCode)
                {
                        config.setAttribute(RESPONSE_CODE,result.getResponseCode());
                }
                if (saveResponseMessage)
                {
                        config.setAttribute(RESPONSE_MESSAGE,result.getResponseMessage());
                }
                if (saveThreadName)
                {
                        config.setAttribute(THREAD_NAME,result.getThreadName());
                }
                if (saveDataType)
                {
                        config.setAttribute(DATA_TYPE,result.getDataType());
                }

                if (printMilliseconds)
                {
                        config.setAttribute(TIME_STAMP,""+result.getTimeStamp());
                }
                else if (formatter != null)
                {
                        String stamp = formatter.format(new Date(result.getTimeStamp()));
                        config.setAttribute(TIME_STAMP, stamp);
                }

                if (saveSuccessful)
                {
                        config.setAttribute(SUCCESSFUL,new Boolean(result.isSuccessful()).toString());
                }

                SampleResult[] subResults = result.getSubResults();
                for(int i = 0;i < subResults.length;i++)
                {
                        config.addChild(getConfiguration(subResults[i],funcTest));
                }

                AssertionResult[] assResults = result.getAssertionResults();

                if(funcTest)
                {
                        config.addChild(getConfigForTestElement(null,result.getSamplerData()));
                        for(int i = 0;i < assResults.length;i++)
                        {
                                config.addChild(getConfiguration(assResults[i]));
                        }
                        config.addChild(getConfiguration(result.getResponseData()));
                }
                // Determine which of the assertion results to save and
                // whether to save the response data
                else
                {
                        if (assertionsResultsToSave == SAVE_ALL_ASSERTIONS)
                        {
                                config.addChild(getConfigForTestElement(null,result.getSamplerData()));
                                for(int i = 0;i < assResults.length;i++)
                                {
                                        config.addChild(getConfiguration(assResults[i]));
                                }
                        }
                        else if ((assertionsResultsToSave == SAVE_FIRST_ASSERTION)
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

        public static Configuration getConfigForTestElement(String named,TestElement item)
        {
                DefaultConfiguration config = new DefaultConfiguration("testelement","testelement");
                if(named != null)
                {
                        config.setAttribute("name",named);
                }
                if(item.getProperty(TestElement.TEST_CLASS) != null)
                {
                        config.setAttribute("class",(String)item.getProperty(TestElement.TEST_CLASS));
                }
                else
                {
                        config.setAttribute("class",item.getClass().getName());
                }
                Iterator iter = item.getPropertyNames().iterator();
                while (iter.hasNext())
                {
                        String name = (String)iter.next();
                        Object value = item.getProperty(name);
                        if(value instanceof TestElement)
                        {
                                config.addChild(getConfigForTestElement(name,(TestElement)value));
                        }
                        else if(value instanceof Collection)
                        {
                                config.addChild(createConfigForCollection(name,(Collection)value));
                        }
                        else if(value != null)
                        {
                                config.addChild(createConfigForString(name,value.toString()));
                        }
                }
                return config;
        }

        private static Configuration createConfigForCollection(String propertyName,Collection list)
        {
                DefaultConfiguration config = new DefaultConfiguration("collection","collection");
                if(propertyName != null)
                {
                        config.setAttribute("name",propertyName);
                }
                config.setAttribute("class",list.getClass().getName());
                Iterator iter = list.iterator();
                while (iter.hasNext())
                {
                        Object item = iter.next();
                        if(item instanceof TestElement)
                        {
                                config.addChild(getConfigForTestElement(null,(TestElement)item));
                        }
                        else if(item instanceof Collection)
                        {
                                config.addChild(createConfigForCollection(null,(Collection)item));
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
                DefaultConfiguration config = new DefaultConfiguration("string","string");
                config.setValue(value);
                config.setAttribute(XML_SPACE,PRESERVE);
                return config;
        }

        private static Configuration createConfigForString(String name,String value)
        {
                if(value == null)
                {
                        value = "";
                }
                DefaultConfiguration config = new DefaultConfiguration("property","property");
                config.setAttribute("name",name);
                config.setValue(value);
                config.setAttribute(XML_SPACE,PRESERVE);
                return config;
        }

        public synchronized static HashTree loadSubTree(InputStream in) throws IOException
        {
                try
                {
                        Configuration config = builder.build(in);
                        HashTree loadedTree = generateNode(config);
                        return loadedTree;
                }
                catch(ConfigurationException e)
                {
                        throw new IOException("Problem loading using Avalon Configuration tools");
                }
                catch(SAXException e)
                {
                        throw new IOException("Problem with SAX implementation");
                }
        }

        public static TestElement createTestElement(Configuration config) throws ConfigurationException,
                        ClassNotFoundException, IllegalAccessException,InstantiationException
        {
                TestElement element = null;
                element = (TestElement)Class.forName((String)config.getAttribute("class")).newInstance();
                Configuration[] children = config.getChildren();
                for (int i = 0; i < children.length; i++)
                {
                        if(children[i].getName().equals("property"))
                        {
                                try
                                {
                                        element.setProperty(children[i].getAttribute("name"),
                                                children[i].getValue());
                                }
                                catch (Exception ex)
                                {
                                        log.error("Problem loading property",ex);
                                        element.setProperty(children[i].getAttribute("name"),"");
                                }
                        }
                        else if(children[i].getName().equals("testelement"))
                        {
                                element.setProperty(children[i].getAttribute("name"),
                                                createTestElement(children[i]));
                        }
                        else if(children[i].getName().equals("collection"))
                        {
                                element.setProperty(children[i].getAttribute("name"),
                                                createCollection(children[i]));
                        }
                }
                return element;
        }

        private static Collection createCollection(Configuration config) throws ConfigurationException,
                        ClassNotFoundException,IllegalAccessException,InstantiationException
        {
                Collection coll = (Collection)Class.forName((String)config.getAttribute("class")).newInstance();
                Configuration[] items = config.getChildren();
                for (int i = 0; i < items.length; i++)
                {
                        if(items[i].getName().equals("property"))
                        {
                                coll.add(items[i].getValue(""));
                        }
                        else if(items[i].getName().equals("testelement"))
                        {
                                coll.add(createTestElement(items[i]));
                        }
                        else if(items[i].getName().equals("collection"))
                        {
                                coll.add(createCollection(items[i]));
                        }
                        else if(items[i].getName().equals("string"))
                        {
                                coll.add(items[i].getValue(""));
                        }
                }
                return coll;
        }

        private static HashTree generateNode(Configuration config)
        {
                TestElement element = null;
                try
                {
                        element = createTestElement(config.getChild("testelement"));
                }
                catch(Exception e)
                {
                        log.error("Problem loading part of file",e);
                        return null;
                }
                HashTree subTree = new ListedHashTree(element);
                Configuration[] subNodes = config.getChildren("node");
                for (int i = 0; i < subNodes.length; i++)
                {
                        HashTree t = generateNode(subNodes[i]);
                        if(t != null)
                        {
                                subTree.add(element,t);
                        }
                }
                return subTree;
        }
        
        public static class Test extends TestCase
        {
                private static final String[] FILES= new String[]
                {
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
                
                public void setUp() {
                }

                public void testLoadAndSave() throws java.io.IOException {
                  byte[] original= new byte[1000000];

                  for (int i=0; i<FILES.length; i++) {
                    InputStream in= new FileInputStream(new File("testfiles/"+FILES[i]));
                    int len= in.read(original);
                    in.close();

                    in= new ByteArrayInputStream(original, 0, len);
                    HashTree tree= loadSubTree(in);
                    in.close();

                    ByteArrayOutputStream out= new ByteArrayOutputStream(1000000);
                    saveSubTree(tree, out);
                    out.close();

                    // We only check the length of the result. Comparing the
                    // actual result (out.toByteArray==original) will usually
                    // fail, because the order of the properties within each
                    // test element may change. Comparing the lengths should be
                    // enough to detect most problem cases...
                    if (len!=out.size()) {
                      fail("Loading file bin/testfiles/"+FILES[i]+" and "+
                          "saving it back changes its contents.");
                    }

                    // Note this test will fail if a property is added or
                    // removed to any of the components used in the test
                    // files. The way to solve this is to appropriately change
                    // the test file.
                  }
                }
        }
}
