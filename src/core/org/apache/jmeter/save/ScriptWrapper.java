/*
 * Created on Jun 13, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;


/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
class ScriptWrapper
{
   public String version = JMeterUtils.getJMeterVersion();
   public HashTree testPlan;
}
