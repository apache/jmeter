/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.jmeter.functions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.xml.sax.SAXException;

/**
 * This class wraps the XPathFileContainer for use across multiple
 * threads.
 * 
 * It does this by maintaining a list of open files, keyed by file name
 * (or alias, if used).
 * A list of open files is also maintained for each thread, together with
 * the current line number.
 * 
 * @version $Revision$ $Date$
 */
public class XPathWrapper
{

	transient private static Logger log = LoggingManager.getLoggerForClass();
	
	private XPathFileContainer container;
    private int currentRow;
    private static final int NO_LINE = -1;
    
    private static String defaultFile = ""; // for omitted file names
    private String xpathString;
    
	private static Map fileContainers = new HashMap(); // Map file names to containers
    
    /*
     * Only needed locally 
     */
    private XPathWrapper(XPathFileContainer fdc)
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
     * called by getXPathString(file,alias)
     */
    public static synchronized void open(String file,String xpathString,  String alias)
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
    		XPathFileContainer frcc;
            try
            {
                frcc = getFile(file,  xpathString, alias);
				log.info("Stored "+file+" as "+alias);
				m.put(alias,new XPathWrapper(frcc));
            }
            catch (FileNotFoundException e)
            {
            	log.warn(e.getLocalizedMessage());
            }
            catch (IOException e)
            {
            	log.warn(e.getLocalizedMessage());
            } catch (ParserConfigurationException e) {
            	log.warn(e.getLocalizedMessage());
            } catch (SAXException e) {
            	log.warn(e.getLocalizedMessage());
			} catch (TransformerException e) {
            	log.warn(e.getLocalizedMessage());
			}
    	}
    }
    
    private static XPathFileContainer getFile(String file, String xpathString, String alias)
    throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, TransformerException
    {
    	XPathFileContainer frcc;
    	if ((frcc = (XPathFileContainer) fileContainers.get(alias)) == null)
    	{
    		frcc = new XPathFileContainer(file, xpathString);
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
		XPathWrapper fw = (XPathWrapper) (my).get(file);
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

    public static String getXPathString(String file,String xpathString)
    {
    	Map my = (Map) filePacks.get();
		XPathWrapper fw = (XPathWrapper) (my).get(file);
		if (fw == null) // First call
		{
			if (file.startsWith("*")) {
				log.warn("Cannot perform initial open using alias "+file);
			}
			else
			{
				file=checkDefault(file);
				log.info("Attaching "+file);
				open(file,xpathString,file);
				fw = (XPathWrapper) my.get(file);
			}
			//TODO improve the error handling
			if (fw == null) {
				log.error("XPathWrapper is null!");
				return "";
			}
		}
    	return fw.getXPathString();
    }
    
    private String getXPathString()
    {
    	if (currentRow == NO_LINE) {
    		currentRow = container.nextRow();
    	}
    	log.debug("getting match number "+currentRow);
    	
		return container.getXPathString(currentRow);
    }

    /**
     * Gets the current row number (mainly for error reporting)
     * 
     * @param file
     * @return the current row number for this thread
     */
    public static int getCurrentRow(String file)
    {
    	
		Map my = (Map) filePacks.get();
		XPathWrapper fw = (XPathWrapper) (my).get(file);
		if (fw == null) // Not yet open
		{
			return -1;
		}
		else
		{
			return fw.currentRow;
		}
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
