package org.apache.jmeter.monitor.model;

public interface Connector
{
	ThreadInfo getThreadInfo();

	void setThreadInfo(ThreadInfo value);

	RequestInfo getRequestInfo();

	void setRequestInfo(RequestInfo value);

	Workers getWorkers();

	void setWorkers(Workers value);

	String getName();

	void setName(String value);

}
