/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

package org.apache.jmeter.config;

import java.io.IOException;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * @author mstover
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CSVDataSet extends ConfigTestElement implements TestBean, LoopIterationListener
{
    private static Logger log = LoggingManager.getLoggerForClass();
    static final public long serialVersionUID = 1;
    
    transient String filename;
    transient String variableNames;
	transient String delimiter;

	transient private String[] vars;
    

    /* (non-Javadoc)
     * @see org.apache.jmeter.engine.event.LoopIterationListener#iterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
     */
    public void iterationStart(LoopIterationEvent iterEvent)
    {
        FileServer server = FileServer.getFileServer();
        if(vars == null)
        {
            server.reserveFile(getFilename());
            vars = JOrphanUtils.split(getVariableNames(),",");
        }
        try
        {
			String delim=getDelimiter();
			if (delim.equals("\\t")) delim="\t";// Make it easier to enter a Tab
            String[] lineValues = JOrphanUtils.split(server.readLine(getFilename()),delim);
	        for(int a = 0;a < vars.length && a < lineValues.length;a++)
	        {
	            this.getThreadContext().getVariables().put(vars[a],lineValues[a]);
	        }
        }
        catch(IOException e)
        {
            log.error("Failed to read file: " + getFilename());
        }
    }

    /**
     * @return Returns the filename.
     */
    public String getFilename()
    {
        return filename;
    }
    /**
     * @param filename The filename to set.
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }
    /**
     * @return Returns the variableNames.
     */
    public String getVariableNames()
    {
        return variableNames;
    }
    /**
     * @param variableNames The variableNames to set.
     */
    public void setVariableNames(String variableNames)
    {
        this.variableNames = variableNames;
    }

	public String getDelimiter() {
		return delimiter;
	}
	

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
}
