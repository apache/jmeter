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
public interface ThreadInfo
{
	int getMaxSpareThreads();

	void setMaxSpareThreads(int value);

	int getMinSpareThreads();

	void setMinSpareThreads(int value);

	int getMaxThreads();

	void setMaxThreads(int value);

	int getCurrentThreadsBusy();

	void setCurrentThreadsBusy(int value);

	int getCurrentThreadCount();

	void setCurrentThreadCount(int value);

}
