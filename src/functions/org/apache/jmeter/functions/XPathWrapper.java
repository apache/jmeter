/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.xml.sax.SAXException;

/**
 * This class wraps the XPathFileContainer for use across multiple threads.
 * 
 * It maintains a list of nodelist containers, one for each file/xpath combination
 * 
 */
public class XPathWrapper {

	private static final Logger log = LoggingManager.getLoggerForClass();

    /*
     * This Map serves two purposes:
     * - maps names to  containers
     * - ensures only one container per file across all threads
     */
    private static Map fileContainers = new HashMap();
    
	/* The cache of file packs - for faster local access */
	private static ThreadLocal filePacks = new ThreadLocal() {
		protected Object initialValue() {
			return new HashMap();
		}
	};

    private XPathWrapper() {// Prevent separate instantiation
        super();
    }

	private static XPathFileContainer open(String file, String xpathString) {
        String tname = Thread.currentThread().getName();
		log.info(tname+": Opening " + file);
		XPathFileContainer frcc=null;
		try {
            frcc = new XPathFileContainer(file, xpathString);
		} catch (FileNotFoundException e) {
			log.warn(e.getLocalizedMessage());
		} catch (IOException e) {
			log.warn(e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			log.warn(e.getLocalizedMessage());
		} catch (SAXException e) {
			log.warn(e.getLocalizedMessage());
		} catch (TransformerException e) {
			log.warn(e.getLocalizedMessage());
		}
        return frcc;
	}

    /**
     * Not thread-safe - must be called from a synchronized method.
     * 
     * @param file
     * @param xpathString
     * @return
     */
    public static String getXPathString(String file, String xpathString) {
		Map my = (Map) filePacks.get();
        String key = file+xpathString;
        XPathFileContainer xpfc = (XPathFileContainer) my.get(key);
		if (xpfc == null) // We don't have a local copy
		{
            synchronized(fileContainers){
                xpfc = (XPathFileContainer) fileContainers.get(key);
                if (xpfc == null) { // There's no global copy either
                    xpfc=open(file, xpathString);                    
                }
                if (xpfc != null) {
                    fileContainers.put(key, xpfc);// save the global copy
                }
            }
			// TODO improve the error handling
			if (xpfc == null) {
				log.error("XPathWrapper is null!");
				return ""; //$NON-NLS-1$
			}
            my.put(key,xpfc); // save our local copy
		}
        int currentRow = xpfc.nextRow();
        log.debug("getting match number " + currentRow);
        return xpfc.getXPathString(currentRow);
	}

	public static void clearAll() {
		log.debug("clearAll()");
		Map my = (Map) filePacks.get();
        my.clear();
        String tname = Thread.currentThread().getName();
        log.info(tname+": clearing container");
        fileContainers.clear();
	}
}
