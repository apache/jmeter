/*
 * Created on Mar 12, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.monitor.model;

/**
 * @author pete<p>
 * @version 0.1<p>
 * 
 * Description:<p>
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface RequestInfo
{
	long getBytesReceived();

	void setBytesReceived(long value);

	long getBytesSent();

	void setBytesSent(long value);

	long getRequestCount();

	void setRequestCount(long value);

	long getErrorCount();

	void setErrorCount(long value);

	int getMaxTime();

	void setMaxTime(int value);

	int getProcessingTime();

	void setProcessingTime(int value);

}
