/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.jmeter.ejb.jndi.config;

import java.io.Serializable;

import org.apache.jmeter.config.AbstractConfigElement;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.ejb.jndi.config.gui.LookupConfigGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log4j.Category;

/**
 * Stores the configuration for JNDI lookup
 *
 * @author	Khor Soon Hin
 * Created	2001 Dec 19
 * @version $Revision$ Last Updated: $Date$
 */
public class LookupConfig extends AbstractConfigElement implements Serializable
{
  private static Category catClass = Category.getInstance(
	LookupConfig.class.getName());

  protected static final String LOOKUP_NAME = "lookup_name";

  public LookupConfig()
  {
  }

  public Class getGuiClass()
  {
    return org.apache.jmeter.ejb.jndi.config.gui.LookupConfigGui.class;
  }

  public Object clone()
  {
    LookupConfig newConfig = new LookupConfig();
    configureClone(newConfig);
    return newConfig;
  }

  public String getLookupName()
  {
    String string = (String)this.getProperty(LOOKUP_NAME);
    if(catClass.isDebugEnabled())
    {
      catClass.debug("getLookupName1 : lookup name - " + string);
    }
    return string;
  }

  public void setLookupName(String string)
  {
    if(catClass.isDebugEnabled())
    {
      catClass.debug("setLookupName1 : lookup name  - " + string);
    }
    this.putProperty(LOOKUP_NAME, string);
  }

  public String getClassLabel()
  {
    return JMeterUtils.getResString("jndi_lookup_title");
  }

  public void addConfigElement(ConfigElement config)
  {
  }
}
