/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * @created	2001 Dec 17
 * @modified	2001 Dec 17
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
