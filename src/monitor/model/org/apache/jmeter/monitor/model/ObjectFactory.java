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

/**
 * ObjectFactory is a simple factory class which creates
 * new instances of objects. It also provides convienant
 * method to parse XML status results.
 */
public class ObjectFactory
{

	private static ObjectFactory FACTORY = null;
	private static Parser PARSER = null;
	
    /**
     * 
     */
    protected ObjectFactory()
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
	
	public synchronized Status parseBytes(byte[] bytes){
		return PARSER.parseBytes(bytes);
	}
	
	public Status parseString(String content){
		return PARSER.parseString(content);
	}

	public Status parseSampleResult(SampleResult result){
		return PARSER.parseSampleResult(result);
	}
	
	public Status createStatus(){
		return new StatusImpl();
	}
	
	public Connector createConnector(){
		return new ConnectorImpl();
	}

	public Jvm createJvm(){
		return new JvmImpl();
	}

	public Memory createMemory(){
		return new MemoryImpl();	
	}
	
	public RequestInfo createRequestInfo(){
		return new RequestInfoImpl();
	}
	
	public ThreadInfo createThreadInfo(){
		return new ThreadInfoImpl();
	}
	
	public Worker createWorker(){
		return new WorkerImpl();
	}
	
	public Workers createWorkers(){
		return new WorkersImpl();
	}
	
	protected class MonitorParser extends ParserImpl {
		public MonitorParser(ObjectFactory factory){
			super(factory);
		}
	}

	/**
	 * Basic method for testing the class
	 * @param args
	 */	
	public static void main(String[] args){
		if (args != null && args.length == 2){
			String file = null;
			//int count = 1;
			if (args[0] != null){
				file = args[0];
			}
			if (args[1] != null){
				//count = Integer.parseInt(args[1]);
			}
			try {
				ObjectFactory of = ObjectFactory.getInstance();
				java.io.File infile = new java.io.File(file);
				java.io.FileInputStream fis =
					new java.io.FileInputStream(infile);
				java.io.InputStreamReader isr =
					new java.io.InputStreamReader(fis);
				StringBuffer buf = new StringBuffer();
				java.io.BufferedReader br = new java.io.BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null){
					buf.append(line);
				}
				System.out.println("contents: ");
				System.out.println(buf.toString());
				System.out.println("----------------------");
				Status st = of.parseBytes(buf.toString().getBytes());
				if (st == null){
					System.out.println("parse failed");
				} else {
					System.out.println("parse successful:");
					System.out.println(st.getJvm().getMemory().getFree());
					System.out.println(st.getJvm().getMemory().getTotal());
					System.out.println(st.getJvm().getMemory().getMax());
					System.out.println("connector size: " +
						st.getConnector().size());
					Connector conn = (Connector)st.getConnector().get(0);
					System.out.println("conn: " +
						conn.getThreadInfo().getMaxThreads());
				}
			} catch (java.io.FileNotFoundException e){
				e.printStackTrace();
			} catch (java.io.IOException e){
				e.printStackTrace();
			}
		} else {
		}
	}
}
