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

import org.apache.jmeter.samplers.Clearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MonitorModel implements Clearable, Serializable, Cloneable
{

	private String name;
	private List listeners;
	private MonitorStats current =
		new MonitorStats(0,0,0,0,0,"","","",System.currentTimeMillis());
	
    /**
     * 
     */
    public MonitorModel()
    {
        super();
        listeners = new LinkedList();
    }

	public MonitorModel(MonitorStats stat){
		this.current = stat;
	}
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.getURL();
	}
	
	public int getHealth(){
		return this.current.health;
	}
	
	public int getLoad(){
		return this.current.load;
	}

	public int getCpuload(){
		return this.current.cpuload;	
	}
	
	public int getMemload(){
		return this.current.memload;
	}
	
	public int getThreadload(){
		return this.current.threadload;
	}
	
	public String getHost(){
		return this.current.host;
	}
	
	public String getPort(){
		return this.current.port;
	}
	
	public String getProtocol(){
		return this.current.protocol;
	}
	
	public long getTimestamp(){
		return this.current.timestamp;
	}
	
	public String getURL(){
		return this.current.getURL();
	}
	
	public String getTimestampString(){
		Date date = new Date(this.current.timestamp);
		SimpleDateFormat ft = new SimpleDateFormat();
		return ft.format(date);
	}
	
	/**
	 * Method is used by DefaultMutableTreeNode to get
	 * the label for the node.
	 */
	public String toString(){
		return getURL();
	}
	
    /* (non-Javadoc)
     * @see org.apache.jmeter.samplers.Clearable#clear()
     */
    public void clear()
    {
		current = 
			new MonitorStats(0,0,0,0,0,"","","",System.currentTimeMillis());
    }

	/**
	 * notify the listeners with the MonitorModel object.
	 * @param model
	 */
	public void notifyListeners(MonitorModel model)
	{
		for (int idx=0; idx < listeners.size(); idx++){
			MonitorListener ml = (MonitorListener)listeners.get(idx);
			ml.addSample(model);
		}
	}
	
	public void addListener(MonitorListener listener){
		listeners.add(listener);
	}
	
	public Object clone(){
		MonitorStats newstats =
			new MonitorStats(current.health,
				current.load,
				current.cpuload,
				current.memload,
				current.threadload,
				current.host,
				current.port,
				current.protocol,
				current.timestamp);
		return new MonitorModel(newstats);
	}
}
