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
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.jmeter.config.AbstractConfigElement;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.ejb.jndi.config.gui.MethodConfigGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log4j.Category;

/**
 * Stores the configuration for remote method execution
 *
 * @author	Khor Soon Hin
 * @version	1.0
 * @created	2001 Dec 24
 * @modified	2001 Dec 24
 */
public class MethodConfig extends AbstractConfigElement implements Serializable
{
  private static Category catClass = Category.getInstance(
	MethodConfig.class.getName());

  protected static final String METHOD_HOME_NAME = 
	"MethodConfig.method_home_name";
  protected static final String METHOD_HOME_LIST = 
	"MethodConfig.method_home_list";
  protected static final String METHOD_HOME_PARMS = 
	"MethodConfig.method_home_parms";
  protected static final String METHOD_REMOTE_INTERFACE_LIST = 
	"MethodConfig.method_remote_name_list";
  protected static final String METHOD_REMOTE_NAME =
	"MethodConfig.method_remote_name";
  protected static final String METHOD_REMOTE_LIST =
	"MethodConfig.method_remote_list";
  protected static final String METHOD_REMOTE_PARMS =
	"MethodConfig.method_remote_parms";
  protected static final String METHOD_CONFIG_GUI =
	"MethodConfig.method_config_gui";
	// Attach the gui to the model.  This enables us to get the gui
	// instance given the model.  This is important for MethodConfig
	// class since sampling this class generates Method names
	// and parameters (through reflection) which will be used in the
	// MethodConfigGui
  protected static final String METHOD_REMOTE_INTERFACE_TYPE = 
	"MethodConfig.method_remote_interface_type";

  // Below are the states the MethodConfig can be in. Depending on the state
  // which MethodConfig is in, when 'Run' is executed, the sampler
  // will perform different things.  See explanation of each state below.
  public static final int METHOD_GET_HOME_NAMES = 0;
	// reflect on all the method names of the Home interface
  public static final int METHOD_GET_HOME_PARMS = 1;
	// with a Home method selected reflect on all parms of the method
  public static final int METHOD_INVOKE_HOME = 2;
	// with all parms of the selected method filled, invoke the method
  public static final int METHOD_SELECT_REMOTE_INTERFACE = 3;
  public static final int METHOD_GET_REMOTE_NAMES = 4;
	// reflect on all the method names of the Remote interface
  public static final int METHOD_GET_REMOTE_PARMS = 5;
	// with a Remote method selected reflect on all parms of the method
  public static final int METHOD_INVOKE_REMOTE = 6;
  public static final int METHOD_COMPLETE = 7;

  protected Method homeMethod;
  protected Method remoteMethod;
  protected int state;
  // This variable is always false until the 'Reflect' button on the
  // ejb method config panel is clicked.  This allows the JNDISampler
  // to differentiate between sampling and relfection since it does both.
  // When it does reflection, it will only run one reflection step at each time
  // e.g. the first reflection will expose all methods of the home object
  // returned by the lookup, the second the parms of the selected home object
  // method etc.  For sampling all the steps will be run.
  protected boolean reflectionStatus = false;
	
  public MethodConfig()
  {
  }

  public Class getGuiClass()
  {
    return org.apache.jmeter.ejb.jndi.config.gui.MethodConfigGui.class;
  }

  public Object clone()
  {
    MethodConfig newConfig = new MethodConfig();
    configureClone(newConfig);
    return newConfig;
  }

  public String getMethodHomeName()
  {
    String string = (String)this.getProperty(METHOD_HOME_NAME);
    if(catClass.isDebugEnabled())
    {
      catClass.debug("getMethodHomeName1 : method home name - " + string);
    }
    return string;
  }

  public void setMethodHomeName(String string)
  {
    if(catClass.isDebugEnabled())
    {
      catClass.debug("setMethodHomeName1 : method home name  - " + string);
    }
    this.putProperty(METHOD_HOME_NAME, string);
  }

  public String getMethodRemoteName()
  {
    String string = (String)this.getProperty(METHOD_REMOTE_NAME);
    if(catClass.isDebugEnabled())
    {
      catClass.debug("getMethodRemoteName1 : method remote name - " + string);
    }
    return string;
  }

  public void setMethodRemoteName(Object ref)
  {
    if(catClass.isDebugEnabled())
    {
      catClass.debug("setMethodRemoteName1 : method remote name  - " + ref);
    }
    this.putProperty(METHOD_REMOTE_NAME, ref);
  }

  public Object getRemoteInterfaceType()
  {
    Object ref  = this.getProperty(METHOD_REMOTE_INTERFACE_TYPE);
    if(catClass.isDebugEnabled())
    {
      catClass.debug("getRemoteInterfaceType1 : remote interface - " + 
	ref);
    }
    return ref;
  }

  public void setRemoteInterfaceType(Object ref)
  {
    if(catClass.isDebugEnabled())
    {
      catClass.debug("setRemoteInterfaceType1 : remote interface - " +
	ref);
    }
    this.putProperty(METHOD_REMOTE_INTERFACE_TYPE, ref);
  }

  public Object getRemoteInterfaceList()
  {
    Object ref = this.getProperty(METHOD_REMOTE_INTERFACE_LIST);
    if(catClass.isDebugEnabled())
    {
      catClass.debug("getRemoteInterfaceList1 : remote interface list - " + 
	ref);
    }
    return ref;
  }

  public void setRemoteInterfaceList(Object ref)
  {
    if(catClass.isDebugEnabled())
    {
      catClass.debug("setRemoteInterfaceList1 : remote interface list - " +
	ref);
    }
    this.putProperty(METHOD_REMOTE_INTERFACE_LIST, ref);
  }

  public String[] getMethodHomeList()
  {
    String[] strings = (String[])this.getProperty(METHOD_HOME_LIST);
    return strings;
  }

  public void setMethodHomeList(String[] list)
  {
    this.putProperty(METHOD_HOME_LIST, list);
  }

  public String[] getMethodRemoteList()
  {
    String[] strings = (String[])this.getProperty(METHOD_REMOTE_LIST);
    return strings;
  }

  public void setMethodRemoteList(String[] list)
  {
    this.putProperty(METHOD_REMOTE_LIST, list);
  }

  public Class[] getMethodHomeParms()
  {
    Class[] classes= (Class[])this.getProperty(METHOD_HOME_PARMS);
    return classes;
  }

  public void setMethodHomeParms(Class[] list)
  {
    this.putProperty(METHOD_HOME_PARMS, list);
  }

  public  Class[] getMethodRemoteParms()
  {
    Class[] classes= (Class[])this.getProperty(METHOD_REMOTE_PARMS);
    return classes;
  }

  public void setMethodRemoteParms(Class[] list)
  {
    this.putProperty(METHOD_REMOTE_PARMS, list);
  }

  public int getState()
  {
    return state;
  }

  public void setState(int current)
  {
    state = current;
  }

  public String getClassLabel()
  {
    return JMeterUtils.getResString("jndi_method_title");
  }

  public void addConfigElement(ConfigElement config)
  {
  }

  public void setGui(MethodConfigGui gui)
  {
    this.putProperty(METHOD_CONFIG_GUI, gui);
  }

  public MethodConfigGui getGui()
  {
    return (MethodConfigGui)this.getProperty(METHOD_CONFIG_GUI);
  }

  public void setHomeMethod(Method aMethod)
  {
    homeMethod = aMethod;
  }

  public Method getHomeMethod()
  {
    return homeMethod;
  }

  public void setRemoteMethod(Method aMethod)
  {
    remoteMethod = aMethod;
  }

  public Method getRemoteMethod()
  {
    return remoteMethod;
  }

  public void setReflectionStatus(boolean status)
  { 
    reflectionStatus = status;
  }

  public boolean getReflectionStatus()
  {
    return reflectionStatus;
  }
}
