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

package org.apache.jmeter.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;

/**
 * This TestBean is just an example about how to write testbeans. The intent is
 * to demonstrate usage of the TestBean features to podential TestBean
 * developers. Note that only the class's introspector view matters: the methods
 * do nothing -- nothing useful, in any case.
 */
public class DebugSampler extends AbstractSampler implements TestBean {
    
	private boolean displayJMeterVariables;
	
	private boolean displayJMeterProperties;
	
	private boolean displaySystemProperties;
	
	public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.sampleStart();
        StringBuffer sb = new StringBuffer(100);
        StringBuffer rd = new StringBuffer(20); // for request Data
        if (isDisplayJMeterVariables()){
        	rd.append("JMeterVariables\n");
        	sb.append("JMeterVariables:\n");
	        JMeterVariables vars = JMeterContextService.getContext().getVariables();
	        Iterator i = vars.getIterator();
	        while(i.hasNext())
	        {
	          Map.Entry me = (Map.Entry) i.next();
	           if(String.class.equals(me.getValue().getClass())){
	                 sb.append(me.toString()).append("\n");
	           }
	        }
	        sb.append("\n");
        }
        
        if (isDisplayJMeterProperties()){
        	rd.append("JMeterProperties\n");
        	sb.append("JMeterProperties:\n");
   		    formatProperties(sb, JMeterUtils.getJMeterProperties());        	
	        sb.append("\n");
        }
        
        if (isDisplaySystemProperties()){
        	rd.append("SystemProperties\n");
        	sb.append("SystemProperties:\n");
   		    formatProperties(sb, System.getProperties());        	        	
	        sb.append("\n");
        }
        
        res.setResponseData(sb.toString().getBytes());
        res.setDataType(SampleResult.TEXT);
        res.setSamplerData(rd.toString());
        res.setSuccessful(true);
        res.sampleEnd();
        return res;
	}

	private void formatProperties(StringBuffer sb, Properties p) {
		Set s = p.entrySet();
		ArrayList al = new ArrayList(s);
		Collections.sort(al, new Comparator(){
			public int compare(Object o1, Object o2) {
				String m1,m2;
				m1=(String)((Map.Entry)o1).getKey();
				m2=(String)((Map.Entry)o2).getKey();
				return m1.compareTo(m2);
			}
		});
		Iterator i = al.iterator();
		while(i.hasNext()){
			Map.Entry me = (Map.Entry) i.next();
			sb.append(me.getKey());
			sb.append("=");
			sb.append(me.getValue());
			sb.append("\n");
		}
	}

	public boolean isDisplayJMeterVariables() {
		return displayJMeterVariables;
	}

	public void setDisplayJMeterVariables(boolean displayJMeterVariables) {
		this.displayJMeterVariables = displayJMeterVariables;
	}

	public boolean isDisplayJMeterProperties() {
		return displayJMeterProperties;
	}

	public void setDisplayJMeterProperties(boolean displayJMeterPropterties) {
		this.displayJMeterProperties = displayJMeterPropterties;
	}

	public boolean isDisplaySystemProperties() {
		return displaySystemProperties;
	}

	public void setDisplaySystemProperties(boolean displaySystemProperties) {
		this.displaySystemProperties = displaySystemProperties;
	}
}
