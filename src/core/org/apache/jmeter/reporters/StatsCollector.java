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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.OldSaveService;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.save.ListenerResultWrapper;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import org.xml.sax.SAXException;

/**
 * @author pete
 *
 * StatsCollector is used to save the calculated statistics generated
 * by visualizers. It makes sense to have a separete collector for
 * stats, since the runtime behavior is different than raw SampleResults.
 */
public class StatsCollector extends AbstractTestElement implements Clearable,
    Serializable, TestListener, Remoteable,
    NoThreadClone
{

	static final long serialVersionUID = 1;
	private static final String TESTRESULTS_START = "<calculatedResults>";
	private static final String TESTRESULTS_END = "</calculatedResults>";
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final int MIN_XML_FILE_LEN = XML_HEADER.length()
		  + TESTRESULTS_START.length() + TESTRESULTS_END.length();
	transient private static Logger log = LoggingManager.getLoggerForClass();
	public final static String FILENAME = "filename";
	public final static String SAVE_CONFIG = "saveConfig";
	private static boolean functionalMode = false;
	public static final String ERROR_LOGGING = "ResultCollector.error_logging";
	transient private DefaultConfigurationSerializer serializer;
	transient private volatile PrintWriter out;
	private boolean inTest = false;
	private static Map files = new HashMap();
	private Set hosts = new HashSet();

    /**
     * 
     */
    public StatsCollector() {
        super();
		serializer = new DefaultConfigurationSerializer();
		setErrorLogging(false);
		setProperty(new ObjectProperty(SAVE_CONFIG,new SampleSaveConfiguration()));
    }

	public void testStarted(){
		testStarted("local");
	}

	public void testEnded(){
		testEnded("local");
	}

	public void testStarted(String host){
		hosts.add(host);
		try
		{
		   initializeFileOutput();
		}
		catch (Exception e)
		{
		   log.error("", e);
		}
		inTest = true;
	}

	public void testEnded(String host){
		hosts.remove(host);
		if (hosts.size() == 0)
		{
		   finalizeFileOutput();
		   inTest = false;
		}
	}
    
	/**
	 * Each time through a Thread Group's test script, an iteration event is
	 * fired.
	 * @param event
	 */
	public void testIterationStart(LoopIterationEvent event){
	}

	//------- file related methods --------//
	private void setFilenameProperty(String f)
	{
	   setProperty(FILENAME, f);
	}

	public String getFilename()
	{
	   return getPropertyAsString(FILENAME);
	}

	public void setFilename(String f)
	{
	   if (inTest) { return; }
	   setFilenameProperty(f);
	}

	private void initializeFileOutput() throws IOException,
		  ConfigurationException, SAXException
	{

	   String filename = getFilename();
	   if (out == null && filename != null)
	   {
		  if (out == null)
		  {
			 try
			 {
				out = getFileWriter(filename,getSaveConfig());
			 }
			 catch (FileNotFoundException e)
			 {
				out = null;
			 }
		  }
	   }
	}

	private synchronized void finalizeFileOutput()
	{
	   if (out != null)
	   {
		  writeFileEnd(out,getSaveConfig());
		  out.close();
		  files.remove(getFilename());
		  out = null;
	   }
	}

	private static synchronized PrintWriter getFileWriter(String filename,SampleSaveConfiguration saveConfig)
		  throws IOException
	{
	   if (filename == null || filename.length() == 0) { return null; }
	   PrintWriter writer = (PrintWriter) files.get(filename);
	   boolean trimmed = true;

	   if (writer == null)
	   {
		  if (saveConfig.saveAsXml())
		  {
			 trimmed = trimLastLine(filename);
		  }
		  // Find the name of the directory containing the file
		  // and create it - if there is one
		  File pdir = new File(filename).getParentFile();
		  if (pdir != null) pdir.mkdirs();
		  writer = new PrintWriter(
				new OutputStreamWriter(new BufferedOutputStream(
					  new FileOutputStream(filename, trimmed)), "UTF-8"), true);
		  files.put(filename, writer);
	   }
	   if (!trimmed)
	   {
		  writeFileStart(writer,saveConfig);
	   }
	   return writer;
	}

	// returns false if the file did not contain the terminator
	private static boolean trimLastLine(String filename)
	{
	   RandomAccessFile raf = null;
	   try
	   {
		  raf = new RandomAccessFile(filename, "rw");
		  long len = raf.length();
		  if (len < MIN_XML_FILE_LEN) { return false; }
		  raf.seek(len - TESTRESULTS_END.length() - 10);//TODO: may not work on
														// all OSes?
		  String line;
		  long pos = raf.getFilePointer();
		  int end = 0;
		  while ((line = raf.readLine()) != null)// reads to end of line OR file
		  {
			 end = line.indexOf(TESTRESULTS_END);
			 if (end >= 0) // found the string
			 {
				break;
			 }
			 pos = raf.getFilePointer();
		  }
		  if (line == null)
		  {
			 log.warn("Unexpected EOF trying to find XML end marker in "
				   + filename);
			 raf.close();
			 return false;
		  }
		  raf.setLength(pos + end);// Truncate the file
		  raf.close();
	   }
	   catch (FileNotFoundException e)
	   {
		  return false;
	   }
	   catch (IOException e)
	   {
		  log.warn("Error trying to find XML terminator " + e.toString());
		  try
		  {
			 if (raf != null) raf.close();
		  }
		  catch (IOException e1)
		  {
		  }
		  return false;
	   }
	   return true;
	}

	private static void writeFileStart(PrintWriter writer,SampleSaveConfiguration saveConfig)
	{
	   if (saveConfig.saveAsXml())
	   {
		  writer.println(XML_HEADER);
		  writer.println(TESTRESULTS_START);
	   }
	   else if (saveConfig.saveFieldNames())
		  {
			 writer.println(OldSaveService.printableFieldNamesToString());
		  }
	}

	private static void writeFileEnd(PrintWriter pw,SampleSaveConfiguration saveConfig)
	{
	   if (saveConfig.saveAsXml())
	   {
		  pw.print("\n");
		  pw.print(TESTRESULTS_END);
	   }
	}

	private void recordResult(TestElement e) throws Exception
	{
	   if (out != null)
	   {
          SaveService.saveElement(e, out);
	   }
	}

	public void loadExistingFile() throws SAXException, IOException,
		  ConfigurationException
	{
	}

	private void readSamples(ListenerResultWrapper testResults) throws Exception
	{
	}

	//------- error related methods --------//
	public boolean isErrorLogging()
	{
	   return getPropertyAsBoolean(ERROR_LOGGING);
	}

	public void setErrorLogging(boolean errorLogging)
	{
	   setProperty(new BooleanProperty(ERROR_LOGGING, errorLogging));
	}

    //------- config related methods -------//
	/**
	 * @return Returns the saveConfig.
	 */
	public SampleSaveConfiguration getSaveConfig()
	{
	   return (SampleSaveConfiguration)getProperty(SAVE_CONFIG).getObjectValue();
	}
	/**
	 * @param saveConfig The saveConfig to set.
	 */
	public void setSaveConfig(SampleSaveConfiguration saveConfig)
	{
	   getProperty(SAVE_CONFIG).setObjectValue(saveConfig);
	}

	private Configuration getConfiguration(String filename) throws SAXException,
		  IOException, ConfigurationException
	{
	   DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

	   return builder.buildFromFile(filename);
	}

	public void sampleOccurred(TestElement e){
		
	}

	//------- functional mode related methods -------//
	public static void enableFunctionalMode(boolean mode)
	{
	   functionalMode = mode;
	}

	public boolean getFunctionalMode()
	{
	   return functionalMode || isErrorLogging();
	}

}
