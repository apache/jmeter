/*
 * Created on Apr 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.engine.util;

/**
 * Implement this method-less interface to indicate your test element should not be cloned for each thread in a test run.
 * Otherwise, the default behavior is to clone every test element for each thread.
 */
public interface NoThreadClone
{}
