// $Header:
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
 */
package org.apache.jmeter.visualizers;

import java.io.Serializable;

public class MonitorStats implements Serializable
{

	public int health;
	public int load;
	public int cpuload;
	public int memload;
	public int threadload;
	public String host;
	public String port;
	public String protocol;
	public long timestamp;
	
    /**
     * 
     */
    public MonitorStats()
    {
        super();
    }
    
    /**
     * Default constructor
     * @param health
     * @param load
     * @param cpuload
     * @param memload
     * @param threadload
     * @param host
     * @param port
     * @param protocol
     * @param time
     */
    public MonitorStats(int health,
    	int load,
    	int cpuload,
    	int memload,
    	int threadload,
    	String host,
    	String port,
    	String protocol,
    	long time){
    		this.health = health;
    		this.load = load;
    		this.cpuload = cpuload;
    		this.memload = memload;
    		this.threadload = threadload;
    		this.host = host;
    		this.port = port;
    		this.protocol = protocol;
    		this.timestamp = time;
    	}

	/**
	 * For convienance, this method returns the protocol,
	 * host and port as a URL.
	 * @return
	 */
	public String getURL(){
		return protocol + "://" + host + ":" + port;
	}
}
