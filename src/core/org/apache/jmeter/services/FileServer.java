/*
 * Created on Oct 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.jmeter.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.jmeter.gui.JMeterFileFilter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FileServer
{
    static Logger log = LoggingManager.getLoggerForClass();
    File base;
    Map files = new HashMap();
    private static FileServer server = new FileServer();
	private Random random = new Random();
    
    private FileServer()
    {
        base = new File(JMeterUtils.getProperty("user.dir"));
    }
    
    public static FileServer getFileServer()
    {
        return server;
    }
    
    public void setBasedir(String basedir) throws IOException
    {
        log.info("Setting basedir to: " + basedir);
        if(filesOpen())
        {
            throw new IOException("Files are still open, cannot change base directory");
        }
        files.clear();
        if(basedir != null)
        {
            base = new File(basedir);
            if(!base.isDirectory())
            {
                base = base.getParentFile();
            }
        }
    }
    
    public String getBaseDir()
    {
        return base.getAbsolutePath();
    }
    
    public synchronized void reserveFile(String filename)
    {
        log.info("filename = "+ filename+ " base = "+ base);
        if(!files.containsKey(filename))
        {
            Object[] file = new Object[]{new File(base,filename),null};
            files.put(filename,file);
        }
    }
    
    /**
     * Get the next line of the named file.
     * @param filename
     * @return
     * @throws IOException
     */
    public synchronized String readLine(String filename) throws IOException
    {
        Object[] file = (Object[])files.get(filename);
        if(file != null)
        {
            if(file[1] == null)
            {
                BufferedReader r = new BufferedReader(new FileReader((File)file[0]));
                file[1] = r;
            }
            BufferedReader reader = (BufferedReader)file[1];
            String line = reader.readLine();
            if(line == null)
            {
                reader.close();
                reader = new BufferedReader(new FileReader((File)file[0]));
                file[1] = reader;
                line = reader.readLine();
            }
            return line;
        }
        throw new IOException("File never reserved");
    }
    
    public synchronized void write(String filename,String value) throws IOException
    {
        Object[] file = (Object[])files.get(filename);
        if(file != null)
        {
            if(file[1] == null)
            {
                file[1] = new BufferedWriter(new FileWriter((File)file[0]));                
            }
            else if(!(file[1] instanceof Writer))
            {
                throw new IOException("File " + filename + " already in use");
            }
            BufferedWriter writer = (BufferedWriter)file[1];
            writer.write(value);
        }
    }
    
    public void closeFiles() throws IOException
    {
        Iterator iter = files.keySet().iterator();
        while(iter.hasNext())
        {
            closeFile((String)iter.next());
        }  
        files.clear();
    }

    /**
     * @param name
     * @throws IOException
     */
    public synchronized void closeFile(String name) throws IOException
    {
        Object[] file = (Object[])files.get(name);
        if(file[1] != null)
        {
            ((Reader)file[1]).close();
            file[1] = null;
        }
    }
    
    protected boolean filesOpen()
    {
        Iterator iter = files.keySet().iterator();
        while(iter.hasNext())
        {
            String name = (String)iter.next();
            Object[] file = (Object[])files.get(name);
            if(file[1] != null)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Method will get a random file in a base directory
     * @param basedir
     * @return
     */
    public File getRandomFile(String basedir, String[] extensions){
    	File input = null;
		if (basedir != null)
		{
			File src = new File(basedir);
			if (src.isDirectory() && src.list() != null)
			{
				File[] files =
					src.listFiles(
						new JMeterFileFilter(extensions));
				int count = files.length;
				input = files[random.nextInt(count)];
			}
		}
		return input;
    }
}
