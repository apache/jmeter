/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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

package org.apache.jmeter.functions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class wraps the FileRowColContainer for use across multiple
 * threads.
 * 
 * It does this by maintaining a list of open files, keyed by file name
 * (or alias, if used).
 * A list of open files is also maintained for each thread, together with
 * the current line number.
 * 
 * @author sebb AT apache DOT org
 * @version $Revision$ $Date$
 */
public class FileWrapper
{

	transient private static Logger log = LoggingManager.getLoggerForClass();
	
	private FileRowColContainer container;
    private int currentRow;
    private static final int NO_LINE = -1;
    
    private static String defaultFile = ""; // for omitted file names
    
	private static Map fileContainers = new HashMap(); // Map file names to containers
    
    /*
     * Only needed locally 
     */
    private FileWrapper(FileRowColContainer fdc)
    {
        super();
        container = fdc;
        currentRow = -1;
    }

	/* The cache of file packs */
	private static ThreadLocal filePacks = new ThreadLocal(){
		protected Object initialValue(){
			return new HashMap();
		}
	};

    private static String checkDefault(String file)
    {
		if (file.length() == 0)
		{
			if (fileContainers.size() == 1 && defaultFile.length() > 0)
			{
				log.warn("Using default: "+defaultFile);
				file = defaultFile;
			}
			else
			{
				log.error("Cannot determine default file name");
			}
		}
    	return file;
    }
    /*
     * called by CSVRead(file,alias)
     */
    public static synchronized void open(String file, String alias)
    {
    	log.info("Opening "+file+ " as " + alias);
    	file = checkDefault(file);
		if (alias.length() == 0)
		{
			log.error("Alias cannot be empty");
			return;
		} 
    	Map m = (Map) filePacks.get();
    	if (m.get(alias) == null)
    	{
    		FileRowColContainer frcc;
            try
            {
                frcc = getFile(file, alias);
				log.info("Stored "+file+" as "+alias);
				m.put(alias,new FileWrapper(frcc));
            }
            catch (FileNotFoundException e)
            {
            	//Already logged
            }
            catch (IOException e)
            {
				//Already logged
            }
    	}
    }
    
    private static FileRowColContainer getFile(String file, String alias)
    throws FileNotFoundException, IOException
    {
    	FileRowColContainer frcc;
    	if ((frcc = (FileRowColContainer) fileContainers.get(alias)) == null)
    	{
    		frcc = new FileRowColContainer(file);
    		fileContainers.put(alias,frcc);
			log.info("Saved "+file+" as "+alias);
			if (defaultFile.length() == 0){
				defaultFile = file;// Save in case needed later
			}
    	}
    	return frcc;
    }
    
    /*
     * Called by CSVRead(x,next) - sets the row to nil so the next
     * row will be picked up the next time round
     * 
     */
    public static void endRow(String file)
    {
    	file=checkDefault(file);
		Map my = (Map) filePacks.get();
		FileWrapper fw = (FileWrapper) (my).get(file);
		if (fw == null)
		{
			log.warn("endRow(): no entry for "+file);
		}
		else
		{
			fw.endRow();
		}
    }
    
    private void endRow()
    {
		if (currentRow == NO_LINE)
		{
			log.warn("endRow() called twice in succession");
		}
		currentRow = NO_LINE;
    }

    public static String getColumn(String file,int col)
    {
    	Map my = (Map) filePacks.get();
		FileWrapper fw = (FileWrapper) (my).get(file);
		if (fw == null) // First call
		{
			if (file.startsWith("*")) {
				log.warn("Cannot perform initial open using alias "+file);
			}
			else
			{
				file=checkDefault(file);
				log.info("Attaching "+file);
				open(file,file);
				fw = (FileWrapper) my.get(file);
			}
			//TODO improve the error handling
			if (fw == null) return "";
		}
    	return fw.getColumn(col);
    }
    
    private String getColumn(int col)
    {
		if (currentRow == NO_LINE)
		{
			currentRow = container.nextRow();
    		
		}
		return container.getColumn(currentRow,col);
    }

    /**
     * 
     */
    public static void clearAll()
    {
		log.debug("clearAll()");
		Map my = (Map) filePacks.get();
		for (Iterator i=my.entrySet().iterator();i.hasNext();)
		{
			Object fw = i.next();
			log.info("Removing "+fw.toString());
			i.remove();
		}
		fileContainers.clear();
		defaultFile = "";
	}
}
