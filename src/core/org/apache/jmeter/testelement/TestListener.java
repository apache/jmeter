package org.apache.jmeter.testelement;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface TestListener
{

	public void testStarted();

	public void testEnded();
	
	public void testStarted(String host);
	
	public void testEnded(String host);
}