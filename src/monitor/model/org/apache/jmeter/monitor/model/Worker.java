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
public interface Worker
{
	int getRequestProcessingTime();

	void setRequestProcessingTime(int value);

	long getRequestBytesSent();

	void setRequestBytesSent(long value);

	java.lang.String getCurrentQueryString();

	void setCurrentQueryString(java.lang.String value);

	java.lang.String getRemoteAddr();

	void setRemoteAddr(java.lang.String value);

	java.lang.String getCurrentUri();

	void setCurrentUri(java.lang.String value);

	java.lang.String getStage();

	void setStage(java.lang.String value);

	java.lang.String getVirtualHost();

	void setVirtualHost(java.lang.String value);

	java.lang.String getProtocol();

	void setProtocol(java.lang.String value);

	long getRequestBytesReceived();

	void setRequestBytesReceived(long value);

	java.lang.String getMethod();

	void setMethod(java.lang.String value);

}
