// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.tcp.config;

import java.io.Serializable;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;

/**
 * @version $Revision$ $Date$
 */
public class TCPConfig extends ConfigTestElement implements Serializable
{

    public TCPConfig()
    {
    }

    public void setServer(String newServer)
    {
        this.setProperty(TCPSampler.SERVER, newServer);
    }
    public String getServer()
    {
        return getPropertyAsString(TCPSampler.SERVER);
    }
    
    public void setPort(String newPort)
    {
        this.setProperty(TCPSampler.PORT, newPort);
    }
    public int getPort()
    {
        return getPropertyAsInt(TCPSampler.PORT);
    }

	public void setFilename(String newFilename)
	{
		this.setProperty(TCPSampler.FILENAME, newFilename);
	}
	public String getFilename()
	{
		return getPropertyAsString(TCPSampler.FILENAME);
	}


    /**
     * Returns a formatted string label describing this sampler
     * Example output:
     *      Tcp://Tcp.nowhere.com:port
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel()
    {
        return ("tcp://" + this.getServer() + ":" + this.getPort());
    }
}
