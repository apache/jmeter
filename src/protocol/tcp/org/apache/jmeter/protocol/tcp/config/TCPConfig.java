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
