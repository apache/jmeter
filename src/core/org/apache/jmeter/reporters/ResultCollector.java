// $Header$
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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.xml.sax.SAXException;


/**
 * @version $Revision$ on $Date$
 */
public class ResultCollector
    extends AbstractListenerElement
    implements
        SampleListener,
        Clearable,
        Serializable,
        TestListener,
        Remoteable,
        NoThreadClone
{
    private static final String TESTRESULTS_START = "<testResults>";//$NON-NLS-1$
    private static final String TESTRESULTS_END = "</testResults>";//$NON-NLS-1$
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";//$NON-NLS-1$
	private static final int MIN_XML_FILE_LEN = 
			XML_HEADER.length() + TESTRESULTS_START.length() + TESTRESULTS_END.length();
	transient private static Logger log = LoggingManager.getLoggerForClass();
    public final static String FILENAME = "filename"; //$NON-NLS-1$
    transient private static boolean functionalMode = false;
    public static final String ERROR_LOGGING = "ResultCollector.error_logging";//$NON-NLS-1$
    // protected List results = Collections.synchronizedList(new ArrayList());
    //private int current;
    transient private DefaultConfigurationSerializer serializer;
    //private boolean inLoading = false;
    transient private volatile PrintWriter out;
    transient private boolean inTest = false;
    transient private static Map files = new HashMap();
    transient private Set hosts = new HashSet();

    /**
     * No-arg constructor.
     */
    public ResultCollector()
    {
        //current = -1;
        serializer = new DefaultConfigurationSerializer();
        setErrorLogging(false);
    }

    private void setFilenameProperty(String f)
    {
        setProperty(FILENAME, f);
    }

    public String getFilename()
    {
        return getPropertyAsString(FILENAME);
    }

    public boolean isErrorLogging()
    {
        return getPropertyAsBoolean(ERROR_LOGGING);
    }

    public void setErrorLogging(boolean errorLogging)
    {
        setProperty(new BooleanProperty(ERROR_LOGGING, errorLogging));
    }

    /**
     * Sets the filename attribute of the ResultCollector object.
     *
     * @param f the new filename value
     */
    public void setFilename(String f)
    {
        if (inTest)
        {
            return;
        }
        setFilenameProperty(f);
    }

    public void testEnded(String host)
    {
        hosts.remove(host);
        if (hosts.size() == 0)
        {
            finalizeFileOutput();
            inTest = false;
        }
    }

    public void testStarted(String host)
    {
        hosts.add(host);
        try
        {
            initializeFileOutput();//TODO - move to first sample??
        }
        catch (Exception e)
        {
            log.error("", e);
        }
        inTest = true;
    }

    public void testEnded()
    {
        testEnded("local");//$NON-NLS-1$
    }

    public void testStarted()
    {
        testStarted("local");//$NON-NLS-1$
    }

    public void loadExistingFile()
        throws SAXException, IOException, ConfigurationException
    {
        //inLoading = true;
        if (new File(getFilename()).exists())
        {
            clearVisualizer();
            BufferedReader dataReader = null;
            try
            {
                Configuration savedSamples = getConfiguration(getFilename());
                readSamples(savedSamples);
            }
            catch(SAXException e)
            {
                dataReader = new BufferedReader(new FileReader(getFilename()));
                String line;
                while((line = dataReader.readLine()) != null)
                {
                    sendToVisualizer(SaveService.makeResultFromDelimitedString(line));
                }
            }
            catch (Exception e)
            {
                log.error("", e);
            }
            finally
			{
            	if (dataReader != null) dataReader.close();
            }
        }
        //inLoading = false;
    }

    private static void writeFileStart(PrintWriter writer)
    {
        if (SaveService.getOutputFormat() == SaveService.SAVE_AS_XML)
        {
            writer.println(XML_HEADER);
            writer.println(TESTRESULTS_START);
        }
        else if (SaveService.getOutputFormat() == SaveService.SAVE_AS_CSV)
        {
            if (SaveService.getPrintFieldNames())
            {
                writer.println(SaveService.printableFieldNamesToString());
            }
        }
    }


    private static void writeFileEnd(PrintWriter pw)
    {
        if (SaveService.getOutputFormat() == SaveService.SAVE_AS_XML)
        {
            pw.print("\n");//$NON-NLS-1$
            pw.print(TESTRESULTS_END);
        }
    }

    private static synchronized PrintWriter getFileWriter(String filename)
        throws IOException
    {
        if (filename == null || filename.length() == 0)
        {
            return null;
        }
        PrintWriter writer = (PrintWriter) files.get(filename);
        boolean trimmed = true;

        if (writer == null)
        {
        	 if (SaveService.getOutputFormat() == SaveService.SAVE_AS_XML)
        	 {
        	 	trimmed = trimLastLine(filename);	
        	 }
            writer =
                new PrintWriter(
                    new OutputStreamWriter(
                        new BufferedOutputStream(
                            new FileOutputStream(filename, trimmed)),
                        "UTF-8"),//$NON-NLS-1$
                    true);
            files.put(filename, writer);
        }
        if (!trimmed)
        {
            writeFileStart(writer);
        }
        return writer;
    }

    // returns false if the file did not contain the terminator
    private static boolean trimLastLine(String filename)
    {
    	RandomAccessFile raf = null;
        try
        {
        	raf = new RandomAccessFile(filename,"rw");//$NON-NLS-1$
        	long len = raf.length();
        	if (len < MIN_XML_FILE_LEN)
        	{
        		return false;
        	}
        	raf.seek(len-TESTRESULTS_END.length()-10);//TODO: may not work on all OSes?
        	String line;
        	long pos = raf.getFilePointer();
        	int end=0;
        	while((line = raf.readLine()) != null)// reads to end of line OR file
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
        		log.warn("Unexpected EOF trying to find XML end marker in "+filename);
        		raf.close();
        		return false;
        	}
        	raf.setLength(pos+end);// Truncate the file
    		raf.close();
        }
        catch (FileNotFoundException e)
        {
            return false;
        } catch (IOException e) {
        	log.warn("Error trying to find XML terminator "+e.toString());
    		try {
				if (raf != null) raf.close();
			} catch (IOException e1) {}
			return false;
		}
        return true;
    }

    public static void enableFunctionalMode(boolean mode)
    {
        functionalMode = mode;
    }

    public boolean getFunctionalMode()
    {
        return functionalMode || isErrorLogging();
    }

    /**
     * Gets the serializedSampleResult attribute of the ResultCollector object.
     *
     * @param result description of the Parameter
     * @return the serializedSampleResult value
     */
    private String getSerializedSampleResult(SampleResult result)
            throws SAXException, IOException, ConfigurationException
    {
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();

        serializer.serialize(
            tempOut,
            SaveService.getConfiguration(result, getFunctionalMode()));
        String serVer = tempOut.toString();
        int index = serVer.indexOf(System.getProperty("line.separator"));
        if(index > -1)
        {
            return serVer.substring(index);
        }
        else
        {
            return serVer;
        }
    }

    private void readSamples(Configuration testResults)
            throws IOException, SAXException, ConfigurationException
    {
        Configuration[] samples = testResults.getChildren();

        for (int i = 0; i < samples.length; i++)
        {
            SampleResult result = SaveService.getSampleResult(samples[i]);

            sendToVisualizer(result);
            recordResult(result);
        }
    }

    /**
     * Gets the configuration attribute of the ResultCollector object.
     *
     * @return the configuration value
     */
    private Configuration getConfiguration(String filename)
            throws SAXException, IOException, ConfigurationException
    {
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

        return builder.buildFromFile(filename);
    }

    public void clearVisualizer()
    {
        //current = -1;
        if (getVisualizer() != null && getVisualizer() instanceof Clearable)
        {
            ((Clearable) getVisualizer()).clear();
        }
        finalizeFileOutput();
    }

    public void setListener(Object l)
    {
    }

    public void sampleStarted(SampleEvent e)
    {
    }

    public void sampleStopped(SampleEvent e)
    {
    }


    /**
     * When a test result is received, display it and save it.
     * @param  e the sample event that was received
     */
    public void sampleOccurred(SampleEvent e)
    {
        SampleResult result = e.getResult();

        if (!isErrorLogging() || !result.isSuccessful())
        {
            sendToVisualizer(result);

            try
            {
                if (SaveService.getOutputFormat() == SaveService.SAVE_AS_CSV)
                {
                    if (out != null)
                    {
                        String savee =
                                SaveService.resultToDelimitedString(result);
                        out.println(savee);
                    }
                }
                // Save results as XML
                else
                {
                    recordResult(result);
                }
            }
            catch (Exception err)
            {
                log.error("", err); // should throw exception back to caller
            }
        }
    }

    protected void sendToVisualizer(SampleResult r)
    {
        if (getVisualizer() != null)
        {
            getVisualizer().add(r);
        }
    }

    private void recordResult(SampleResult result)
            throws SAXException, IOException, ConfigurationException
    {
        if (out != null)
        {
            if (!isResultMarked(result))
            {
                out.print(getSerializedSampleResult(result));
            }
        }
    }

    private synchronized boolean isResultMarked(SampleResult res)
    {
        String filename = getFilename();
		boolean marked = res.isMarked(filename);

        if (!marked)
        {
            res.setMarked(filename);
        }
        return marked;
    }

    private synchronized void initializeFileOutput()
        throws IOException, ConfigurationException, SAXException
    {

        String filename = getFilename();
		if (out == null && filename != null)
        {
            if (out == null)
            {
                try
                {
                    out = getFileWriter(filename);
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
            writeFileEnd(out);
            out.close();
            files.remove(getFilename());
            out = null;
        }
    }

    /* (non-Javadoc)
     * @see TestListener#testIterationStart(LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {
    }
}
