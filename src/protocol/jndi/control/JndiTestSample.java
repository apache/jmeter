// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * 
*/

package org.apache.jmeter.ejb.jndi.control;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.jmeter.control.AbstractGenerativeController;
import org.apache.jmeter.ejb.jndi.config.JndiConfig;
import org.apache.jmeter.ejb.jndi.config.LookupConfig;
import org.apache.jmeter.ejb.jndi.config.MethodConfig;
import org.apache.jmeter.ejb.jndi.control.gui.JndiTestSampleGui;
import org.apache.jmeter.ejb.jndi.sampler.JNDISampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log4j.Category;
/**
 * Controls how the sampling is done
 *
 * @author	Khor Soon Hin
 * Created	20 Dec 2001
 * @version $Revision$ Last Updated: $Date$
 */
public class JndiTestSample extends AbstractGenerativeController 
	implements Serializable
{
  private static Category catClass = Category.getInstance(
	JndiTestSample.class.getName());

  protected JndiConfig defaultJndiConfig;

  protected static Set addableList;

  public JndiTestSample()
  {
    defaultJndiConfig = new JndiConfig();
  }

  public JndiConfig getDefaultJndiConfig()
  {
    return defaultJndiConfig;
  }

  public void setDefaultJndiConfig(JndiConfig config)
  {
    defaultJndiConfig = config;
  }

  public void uncompile()
  {
    super.uncompile();
  }

  public String getClassLabel()
  {
    return JMeterUtils.getResString("jndi_testing_title");
  }

  public Class getGuiClass()
  {
    return org.apache.jmeter.ejb.jndi.control.gui.JndiTestSampleGui.class;
  }

  public Class getTagHandlerClass()
  {
    return org.apache.jmeter.ejb.jndi.save.JndiTestSampleHandler.class;
  }

  /**
   * Returns a <code>Collaction</code> containing a list of all 
   * elements which can be added to this element
   *
   * @return	a collection of elements
   */
  public Collection getAddList()
  {
    if(addableList == null)
    {
      addableList = new HashSet();
      addableList.add(new LookupConfig().getClassLabel());
      addableList.add(new MethodConfig().getClassLabel());
    }
    return addableList;
  }

  public Object clone()
  {
    catClass.info("Start : clone1");
    JndiTestSample control = new JndiTestSample();
    control.setDefaultJndiConfig(defaultJndiConfig);
    this.standardCloneProc(control);
    catClass.info("End : clone1");
    return control;
  }

  protected Entry createEntry()
  {
    catClass.info("Start : createEntry1");
    Entry entry = new Entry();
    entry.setSamplerClass(JNDISampler.class);
    entry.addConfigElement(defaultJndiConfig);
    catClass.info("End : createEntry1");
    return entry;
  }
}
