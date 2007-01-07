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

import javax.naming.InitialContext;

import org.apache.jmeter.config.AbstractConfigElement;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.ejb.jndi.config.LookupConfig;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log4j.Category;

/**
 * Stores the configuration for JNDI sampling
 *
 * @author	Khor Soon Hin
 * Created	2001 Dec 17
 * @version $Revision$ Last Updated: $Date$
 */
public class JndiConfig extends AbstractConfigElement implements Serializable
{
  private static Category catClass = Category.getInstance(
	JndiConfig.class.getName());

  public static final int JNDI_INITIAL_CONTEXT_FACTORY = 0;
  public static final int JNDI_OBJECT_FACTORIES = 1;
  public static final int JNDI_STATE_FACTORIES = 2;
  public static final int JNDI_URL_PKG_PREFIXES = 3;
  public static final int JNDI_PROVIDER_URL = 4;
  public static final int JNDI_DNS_URL = 5;
  public static final int JNDI_AUTHORITATIVE = 6;
  public static final int JNDI_BATCHSIZE = 7;
  public static final int JNDI_REFERRAL = 8;
  public static final int JNDI_SECURITY_PROTOCOL = 9;
  public static final int JNDI_SECURITY_AUTHENTICATION = 10;
  public static final int JNDI_SECURITY_PRINCIPAL = 11;
  public static final int JNDI_SECURITY_CREDENTIALS = 12;
  public static final int JNDI_LANGUAGE = 13;
  public static final int JNDI_APPLET = 14;

  public static final String[] JNDI_PROPS = {
	InitialContext.INITIAL_CONTEXT_FACTORY,
	InitialContext.OBJECT_FACTORIES,
	InitialContext.STATE_FACTORIES,
	InitialContext.URL_PKG_PREFIXES,
	InitialContext.PROVIDER_URL,
	InitialContext.DNS_URL,
	InitialContext.AUTHORITATIVE,
	InitialContext.BATCHSIZE,
	InitialContext.REFERRAL,
	InitialContext.SECURITY_PROTOCOL,
	InitialContext.SECURITY_AUTHENTICATION,
	InitialContext.SECURITY_PRINCIPAL,
	InitialContext.SECURITY_CREDENTIALS,
	InitialContext.LANGUAGE,
	InitialContext.APPLET
	};

  protected InitialContext initCtx = null;

  public JndiConfig()
  {
  }

  public Class getGuiClass()
  {
    return org.apache.jmeter.ejb.jndi.config.gui.JndiConfigGui.class;
  }

  public Object clone()
  {
    JndiConfig newConfig = new JndiConfig();
    configureClone(newConfig);
    return newConfig;
  }

  public String getValue(int i)
  {
    String string = (String)this.getProperty(JNDI_PROPS[i]);
    if(catClass.isDebugEnabled())
    {
      catClass.debug("getValue1 : int - " + i);
      catClass.debug("getValue1 : name - " + JNDI_PROPS[i]);
      catClass.debug("getValue1 : value - " + string);
    }
    return string;
  }

  public void setValue(int i, String string)
  {
    if(catClass.isDebugEnabled())
    {
      catClass.debug("setValue1 : int - " + i);
      catClass.debug("setValue1 : name - " + JNDI_PROPS[i]);
      catClass.debug("setValue1 : value - " + string);
    }
    this.putProperty(JNDI_PROPS[i], string);
  }

  public String getClassLabel()
  {
    return JMeterUtils.getResString("jndi_config_title");
  }

  public void addConfigElement(ConfigElement config)
  {
    if(config instanceof JndiConfig)
    {
      updatePropertyIfAbsent((JndiConfig)config);
    }
  }

  public void setInitialContext(InitialContext initCtx)
  {
    this.initCtx = initCtx;
  }

  public InitialContext getInitialContext()
  { 
    return initCtx;
  }
}
