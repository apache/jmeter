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
