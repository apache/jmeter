// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.jmeter.monitor.model;

import org.apache.jmeter.monitor.parser.Parser;
import org.apache.jmeter.monitor.parser.ParserImpl;
import org.apache.jmeter.samplers.SampleResult;

public class ObjectFactory
{

	private static ObjectFactory FACTORY = null;
	private static Parser PARSER = null;
	
    /**
     * 
     */
    public ObjectFactory()
    {
        super();
		PARSER = new MonitorParser(this);
    }

	public static ObjectFactory getInstance(){
		if (FACTORY == null){
			FACTORY = new ObjectFactory();
		}
		return FACTORY;
	}
	
	public Status parseBytes(byte[] bytes){
		return null;
	}
	
	public Status parseString(String content){
		return null;
	}

	public Status parseSampleResult(SampleResult result){
		return null;
	}
	
	public static Status createStatus(){
		return null;
	}
	
	public static Connector createConnector(){
		return null;
	}

	public static Jvm createJvm(){
		return null;
	}

	public static Memory createMemory(){
		return null;	
	}
	
	public static RequestInfo createRequestInfo(){
		return null;
	}
	
	public static ThreadInfo createThreadInfo(){
		return null;
	}
	
	public static Worker createWorker(){
		return null;
	}
	
	public static Workers createWorkers(){
		return null;
	}
	
	protected class MonitorParser extends ParserImpl {
		public MonitorParser(ObjectFactory factory){
			super(factory);
		}
	}
}
