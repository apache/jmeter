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
 * @created	20 Dec 2001
 * @modified	20 Dec 2001
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
