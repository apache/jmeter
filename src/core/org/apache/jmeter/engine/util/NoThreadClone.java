/*
 * Created on Apr 23, 2003
 */
package org.apache.jmeter.engine.util;

/**
 * Implement this method-less interface to indicate your test element should
 * not be cloned for each thread in a test run. Otherwise, the default behavior
 * is to clone every test element for each thread.
 * 
 * @version $Revision$
 */
public interface NoThreadClone
{
}
