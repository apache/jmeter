/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright  (c) 2001-2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache JMeter" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache JMeter", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.jmeter.reporters;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.jorphan.io.TextFile;
import org.xml.sax.SAXException;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author Michael Stover
 * @version $Id$
 */
public class ResultCollector
    extends AbstractListenerElement
    implements SampleListener, Clearable, Serializable, TestListener, Remoteable
{
    transient private static Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.elements");
    private final static String COLLECTED = "collected";
    public final static String FILENAME = "filename";
    private static boolean functionalMode = false;
    public static final String ERROR_LOGGING = "ResultCollector.error_logging";

    //protected List results = Collections.synchronizedList(new ArrayList());
    private int current;
    transient private DefaultConfigurationSerializer serializer;
    private boolean inLoading = false;
    transient private PrintWriter out;
    private boolean inTest = false;
    private static Map files = new HashMap();
    private Set hosts = new HashSet();

    /**
     * No-arg constructor.
     */
    public ResultCollector()
    {
        current = -1;
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
        setProperty(ERROR_LOGGING, new Boolean(errorLogging));
    }

    /**
     * Sets the filename attribute of the ResultCollector object
     *
     * @param f The new filename value.
     */
    public void setFilename(String f) throws IOException, IllegalUserActionException
    {
        if (inTest)
        {
            throw new IllegalUserActionException(JMeterUtils.getResString("busy_testing"));
        }
        try
        {
            setFilenameProperty(f);
            loadExistingFile();
        }
        catch (SAXException e)
        {
            log.error("", e);
            throw new IOException("File " + f + " was improperly formatted");
        }
        catch (ConfigurationException e)
        {
            throw new IOException("File " + f + " was improperly formatted");
        }
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
            initializeFileOutput();
        }
        catch (Exception e)
        {
            log.error("", e);
        }
        inTest = true;
    }

    public void testEnded()
    {
        testEnded("local");
    }

    public void testStarted()
    {
        testStarted("local");
    }

    public void loadExistingFile() throws SAXException, IOException, ConfigurationException
    {
        inLoading = true;
        if (new File(getFilename()).exists())
        {
            clear();
            try
            {
                Configuration savedSamples = getConfiguration(getFilename());
                readSamples(savedSamples);
            }
            catch (Exception e)
            {
                log.error("", e);
            }
        }
        inLoading = false;
    }

    private static void writeFileStart(PrintWriter writer)
    {
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<testResults>");
    }

    private void writeFileEnd()
    {
        out.print("</testResults>");
    }

    private static synchronized PrintWriter getFileWriter(String filename) throws IOException
    {
        if (filename == null || filename.length() == 0)
        {
            return null;
        }
        PrintWriter writer = (PrintWriter) files.get(filename);
        boolean trimmed = true;
        if (writer == null)
        {
            trimmed = trimLastLine(filename);
            writer =
                new PrintWriter(
                    new OutputStreamWriter(
                        new BufferedOutputStream(new FileOutputStream(filename, trimmed)),
                        "UTF-8"));
            files.put(filename, writer);
        }
        if (!trimmed)
        {
            writeFileStart(writer);
        }
        return writer;
    }

    private static boolean trimLastLine(String filename)
    {
        try
        {
            TextFile text = new TextFile(filename);
            String xml = text.getText();
            xml = xml.substring(0, xml.indexOf("</testResults>"));
            text.setText(xml);
        }
        catch (Exception e)
        {
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
     * Gets the serializedSampleResult attribute of the ResultCollector object
     *
     * @param result Description of the Parameter
     * @return The serializedSampleResult value
     */
    private String getSerializedSampleResult(SampleResult result)
        throws SAXException, IOException, ConfigurationException
    {
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        serializer.serialize(tempOut, SaveService.getConfiguration(result, getFunctionalMode()));
        String serVer = tempOut.toString();
        return serVer.substring(serVer.indexOf(System.getProperty("line.separator")));
    }

    /**
     * Description of the Method
     *
     * @param testResults Description of the Parameter
     */
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
     * Gets the configuration attribute of the ResultCollector object
     *
     * @param filename Description of the Parameter
     * @return The configuration value
     */
    private Configuration getConfiguration(String filename)
        throws SAXException, IOException, ConfigurationException
    {
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        return builder.buildFromFile(filename);
    }

    public void clear()
    {
        current = -1;
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

    public void sampleOccurred(SampleEvent e)
    {
        if (!isErrorLogging() || !e.getResult().isSuccessful())
        {
            sendToVisualizer(e.getResult());
            try
            {
                recordResult(e.getResult());
            }
            catch (Exception err)
            {
                log.error("", err); //should throw exception back to caller
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

    /**
     * Description of the Method
     *
     * @param result Description of the Parameter
     */
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
        boolean marked = res.isMarked(getFilename());
        if (!marked)
        {
            res.setMarked(getFilename());
        }
        return marked;
    }

    private void initializeFileOutput() throws IOException, ConfigurationException, SAXException
    {

        if (out == null && getFilename() != null)
        {
            if (out == null)
            {
                try
                {
                    out = getFileWriter(getFilename());
                }
                catch (FileNotFoundException e)
                {
                    out = null;
                }
            }
        }
    }

    /**
     * Description of the Method
     */
    private synchronized void finalizeFileOutput()
    {
        if (out != null)
        {
            writeFileEnd();
            out.close();
            files.remove(getFilename());
            out = null;
        }
    }
    
}
