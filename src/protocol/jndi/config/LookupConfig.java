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

import org.apache.jmeter.config.AbstractConfigElement;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.ejb.jndi.config.gui.LookupConfigGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log4j.Category;

/**
 * Stores the configuration for JNDI lookup
 *
 * @author	Khor Soon Hin
 * @created	2001 Dec 19
 * @modified	2001 Dec 19
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
