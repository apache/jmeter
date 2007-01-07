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

package org.apache.jmeter.ejb.jndi.sampler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
/**
 * Samples the JNDI performance and records them
 *
 * @author	Khor Soon Hin
 * Created	2001 Dec 18
 * @version $Revision$ Last Updated: $Date$
 */
public class JNDISampler implements Sampler
{
  transient private static Logger log = LoggingManager.getLoggerForClass();

  public static final String QUERY = "JNDISampler.query";

  protected static Map keyMap = new HashMap();

  public JNDISampler()
  {
  }

  /**
   * The main method which samples and records the JNDI performance
   *
   * @param e	the JNDI sampling configuration
   * @return	the measurements captured
   */
  public SampleResult sample(Entry e)
  {
    log.info("Start : sample1");
    boolean reflectionStatus = false;
    // There is a row of ifs condition which may be executed depending on the
    // state of the MethodConfig state.  During reflection only one of these
    // condition should be become true.  However, once one condition becomes
    // true it will set the MethodConfig state and this will cause the next 
    // condition to become true and so on.  Thus else-ifs should be used BUT
    // these same conditions need to be used such that more than one condition
    // can be true during sampling so ifs and NOT else-ifs must be used.  To 
    // resolve this conflict, ifs are used with stateJustChange is used to act 
    // like else-if 
    boolean stateJustChanged = false;
    long start = (long)0;
    long end = (long)0;
    long ctxTime = (long)0;
    long lookupTime = (long)0;
    long homeMethodTime = (long)0;
    long remoteMethodTime = (long)0;

    Object remoteInterface = null;
    Object results = null;
    Object ref = null;
    
    SampleResult res = new SampleResult();
    SampleResult contextLookupRes = new SampleResult();
    contextLookupRes.putValue(SampleResult.DISPLAY_NAME, "Context Lookup");
    SampleResult lookupRes = new SampleResult();
    SampleResult homeMethodRes = null;
    SampleResult remoteMethodRes = null;
    Hashtable ht = new Hashtable();
    JndiConfig jndiConfig = null;
    InitialContext ctx = null;
    try
    {
      jndiConfig = (JndiConfig)e.getConfigElement(JndiConfig.class);
      // check if InitialContext is already obtained previously
      ctx = jndiConfig.getInitialContext();
      if(ctx == null)
      {
        // setup the hashtable
        for(int i = 0 ; i < JndiConfig.JNDI_PROPS.length; i++)
        {
          String value = jndiConfig.getValue(i);
          if(value != null)
          {
            if(log.isDebugEnabled())
            {
              log.debug("sample1 : JNDI env - " + 
		JndiConfig.JNDI_PROPS[i] + " = " + value);
            }
            ht.put(JndiConfig.JNDI_PROPS[i], value);
          }
        }
        // initialize initial context
        start = System.currentTimeMillis();
        ctx = new InitialContext(ht);
        end = System.currentTimeMillis();
        log.info("sample1 : Got initial context");
        // store the initial context for reuse
        jndiConfig.setInitialContext(ctx);
      }
      // set the initial context lookup time
      ctxTime = end - start;
      contextLookupRes.setTime(ctxTime);

      // look up the name
      LookupConfig lookupConfig = 
		(LookupConfig)e.getConfigElement(LookupConfig.class);
      String lookupName = null;
      if(lookupConfig != null)
      {
        lookupName = lookupConfig.getLookupName();
        if(log.isDebugEnabled())
        {
          log.debug("sample1 : LookupName - " + lookupName);
        }
        start = System.currentTimeMillis();
        ref = ctx.lookup(lookupName);
        end = System.currentTimeMillis();
        lookupTime = end - start;
        log.info("Got remote interface");
        lookupRes.setTime(lookupTime);
        lookupRes.putValue(SampleResult.DISPLAY_NAME, 
		"Remote Interface Lookup - " + lookupName);
      }
      Class lookupNameClass = ref.getClass();
 
      // lookup method name
      MethodConfig methodConfig = 
		(MethodConfig)e.getConfigElement(MethodConfig.class);
      // store all reflections result in the model of the gui and not the
      // MethodConfig obtained from getConfigElement() 'cos that's the clone.
      // To get the model of the MethodConfigGui, get the MethodConfigGui
      // from the MethodConfig clone first.  All MethodConfig clones cloned
      // from the same MethodConfig shares the same MethodConfigGui.
      MethodConfigGui methodConfigGui =  methodConfig.getGui();
      MethodConfig model = methodConfigGui.getModel();
      // Make all changes on the model of the gui and not the MethodConfig
      // obtained from getConfigElement() because that is the clone.
      int state = model.getState();
      reflectionStatus = model.getReflectionStatus();
      String[] strings = null;
      if(log.isDebugEnabled())
      {
        log.debug("sample1 : state - " + state);
        log.debug("sample1 : reflectionStatus - " + reflectionStatus);
      }
      // Perform only if : 
      // 1. doing a reflection and in this state
      // 2. sampling does not perform this step
      if((state == MethodConfig.METHOD_GET_HOME_NAMES && reflectionStatus 
	&& !stateJustChanged)) 
//	|| (state >= MethodConfig.METHOD_GET_HOME_NAMES && !reflectionStatus))
      {
        // for this state, get the list of all methods in the home
        // interface
        Method[] methods = lookupNameClass.getMethods();
        strings = new String[methods.length];
        for(int i = 0; i < methods.length; i++)
        {
          // create method name which includes method signatures
          strings[i] = getMethodSignature(methods[i]);
        }
        model.setMethodHomeList(strings);
        model.setState(MethodConfig.METHOD_GET_HOME_PARMS);
        stateJustChanged = true;
      }
      // Perform only if : 
      // 1. doing a reflection and in this state
      // 2. sampling does not perform this step
      if((state == MethodConfig.METHOD_GET_HOME_PARMS && reflectionStatus
	&& !stateJustChanged)) 
//	|| (state >= MethodConfig.METHOD_GET_HOME_PARMS && !reflectionStatus))
      {
        // for this state, get all the required parms for the selected
        // method
        String methodHomeName = methodConfig.getMethodHomeName();
        if(log.isDebugEnabled())
        {
          log.debug("sample1 : selected methodHomeName - " +
		methodHomeName);
        }
        Vector returnValues = 
		getMethodParmsTypes(methodHomeName, lookupNameClass);
        // the first object of returnValues will be the Method while the
        // the second object will be the parm types of Method
        Method method = (Method)returnValues.get(0);
        Class[] methodParmTypes = (Class[])returnValues.get(1);
        // once the method is obtained store the parms
        model.setMethodHomeParms(methodParmTypes);
        model.setHomeMethod(method);
        model.setState(MethodConfig.METHOD_INVOKE_HOME);
        stateJustChanged = true;
      }
      // Perform only if : 
      // 1. doing a reflection and in this state
      // 2. sampling and reflection has been done at least this state
      //    if reflection has not been done till this state then user is not
      //    interested in sampling till this state
      if((state == MethodConfig.METHOD_INVOKE_HOME && reflectionStatus
	&& !stateJustChanged)
	|| (state >= MethodConfig.METHOD_INVOKE_HOME && !reflectionStatus))
      {
        log.debug("sample1 : METHOD_INVOKE_HOME");
        Method method = model.getHomeMethod();
        if(log.isDebugEnabled())
        {
          log.debug("sample1 : home method to be invoked - " + method);
        }
        // only initialize homeMethodRes if method execution is to be measured
        homeMethodRes = new SampleResult();
        // gather all parms from MethodConfigGui
        Object[] parmsArray = null;
        try
        {
          parmsArray = methodConfigGui.getMethodParmsValues(
		MethodConfig.METHOD_INVOKE_HOME);
          if(log.isDebugEnabled())
          {
            log.debug("sample1 : home method parms - " + parmsArray);
          }
          // invoke the method
          start = System.currentTimeMillis();
          remoteInterface = method.invoke(ref, parmsArray);
			log.info("return - " + remoteInterface);
        }
        catch(IllegalAccessException err)
        {
          log.error(err);
        }
        catch(InvocationTargetException err)
        {
          log.error(err);
        }
        catch(MethodConfigUserObjectException err)
        {
          log.error(err);
        }
        end = System.currentTimeMillis();
        if(!reflectionStatus)
        {
          // if sampling then get the time lapsed
          homeMethodTime = end - start;
          homeMethodRes.setTime(homeMethodTime);
          homeMethodRes.putValue(SampleResult.DISPLAY_NAME, "Home Method Execution - "
                + method.getName());
          homeMethodRes.putValue(SampleResult.SUCCESS, Boolean.TRUE);
        }
        else
        {
          // if reflection then get all the info required
          model.setState(MethodConfig.METHOD_GET_REMOTE_NAMES);
          stateJustChanged = true;
          // store list of remote interfaces returned
          model.setRemoteInterfaceList(remoteInterface);
        }
      }
      // Perform only if : 
      // 1. doing a reflection and in this state
      // 2. sampling does NOT perform this step
      if((state == MethodConfig.METHOD_GET_REMOTE_NAMES && reflectionStatus
	&& !stateJustChanged))
//	|| (state >= MethodConfig.METHOD_GET_REMOTE_NAMES && !reflectionStatus))
      {
        // for this state, get the list of all methods in the remote
        // interface
        remoteInterface = model.getRemoteInterfaceType();
        Class remoteInterfaceClass = remoteInterface.getClass();
        if(log.isDebugEnabled())
        {
          log.debug("updateGui1 : remoteInterfaceClass - " +
		remoteInterfaceClass);
        }
        Method[] methods = remoteInterfaceClass.getMethods();
        strings = new String[methods.length];
        for(int i = 0; i < methods.length; i++)
        {
          strings[i] = getMethodSignature(methods[i]);
        }
        model.setMethodRemoteList(strings);
        model.setState(MethodConfig.METHOD_GET_REMOTE_PARMS);
        stateJustChanged = true;
      }
      // Perform only if : 
      // 1. doing a reflection and in this state
      // 2. sampling does NOT perform this step
      if((state == MethodConfig.METHOD_GET_REMOTE_PARMS && reflectionStatus
	&& !stateJustChanged))
//	|| (state >= MethodConfig.METHOD_GET_REMOTE_PARMS && !reflectionStatus))
      {
        // for this state, get all the required parms for the selected
        // method
        String methodRemoteName = methodConfig.getMethodRemoteName();
        if(log.isDebugEnabled())
        {
          log.debug("sample1 : selected methodRemoteName - " +
		methodRemoteName);
        }
        Object selectedRemoteInterfaceType = model.getRemoteInterfaceType();
        Class selectedRemoteInterfaceTypeClass = 
		selectedRemoteInterfaceType.getClass();
        Vector returnValues = getMethodParmsTypes(methodRemoteName, 
		selectedRemoteInterfaceTypeClass);
        // the first object of returnValues contains the Method while the
        // the second object the parm types of the Method
        Method method = (Method)returnValues.get(0);
        Class[] methodParmTypes = (Class[])returnValues.get(1);
        // once the method is obtained store the parms
        model.setMethodRemoteParms(methodParmTypes);
        model.setRemoteMethod(method);
        model.setState(MethodConfig.METHOD_INVOKE_REMOTE);
        stateJustChanged = true;
      }
      // Perform only if : 
      // 1. doing a reflection and in this state
      // 2. sampling and reflection has been done at least this state
      //    if reflection has not been done till this state then user is not
      //    interested in sampling till this state
      if((state == MethodConfig.METHOD_INVOKE_REMOTE && reflectionStatus
	&& !stateJustChanged)
	|| (state >= MethodConfig.METHOD_INVOKE_REMOTE && !reflectionStatus))
      {
        log.debug("sample1 : METHOD_INVOKE_REMOTE");
        Method method = model.getRemoteMethod();
        if(log.isDebugEnabled())
        {
          log.debug("sample1 : remote method to be invoked - " + method);
        }
        Object selectedRemoteInterfaceType = model.getRemoteInterfaceType();
        // only initialize homeMethodRes if method execution is to be measured
        remoteMethodRes = new SampleResult();
        // gather all parms from MethodConfigGui
        Object[] parmsArray = null;
        try
        {
          parmsArray = methodConfigGui.getMethodParmsValues(
		MethodConfig.METHOD_INVOKE_REMOTE);
          // invoke the method
          start = System.currentTimeMillis();
          results = method.invoke(selectedRemoteInterfaceType, parmsArray);
			log.info("return - " + results);
        }
        catch(IllegalAccessException err)
        {
          log.error(err);
        }
        catch(InvocationTargetException err)
        {
          log.error(err);
        }
        catch(MethodConfigUserObjectException err)
        {
          log.error(err);
        }
        end = System.currentTimeMillis();
        if(!reflectionStatus)
        {
          // if sampling get the time lapse
          remoteMethodTime = end - start;
          remoteMethodRes.setTime(remoteMethodTime);
          remoteMethodRes.putValue(SampleResult.DISPLAY_NAME, "Remote Method Execution - "
                + method.getName());
          String resultsString = results.toString();
          byte[] resultBytes = null;
          if(resultsString != null)
          {
            resultBytes = resultsString.getBytes();
          }
          remoteMethodRes.putValue(SampleResult.TEXT_RESPONSE, resultBytes);
          remoteMethodRes.putValue(SampleResult.SUCCESS, new Boolean(true));
        }
        else
        {
          // if reflection the set state
          model.setState(MethodConfig.METHOD_COMPLETE);
          stateJustChanged = true;
        }
      }

      long totalTime = ctxTime + lookupTime + homeMethodTime + remoteMethodTime;
      res.setTime(0);
      res.putValue(SampleResult.DISPLAY_NAME, lookupName);
      ArrayList resultList = new ArrayList();
      // don't need to test for null in contextLookupRes and lookupRes
      // because both cannot be null otherwise error will be thrown
      resultList.add(contextLookupRes);
      resultList.add(lookupRes);
      // test for null in homeMethodRes 'cos a null means that user just want
      // to get a list of all home methods
      if(homeMethodRes != null)
      {
        resultList.add(homeMethodRes);
      }
      // test for null in remoteMethodRes 'cos a null means that user just want
      // to get a list of all methods of the remote interfaces
      if(remoteMethodRes != null)
      {
        resultList.add(remoteMethodRes);
      }
      res.putValue(SampleResult.RESULT_LIST, resultList);
      res.putValue(SampleResult.TOTAL_TIME, new Long(totalTime));

      log.info("!!!!! ctxTime : " + ctxTime);
      log.info("!!!!! lookupTime : " + lookupTime);
      log.info("!!!!! homeMethodTime : " + homeMethodTime);
    }
    catch(NamingException err)
    {
      log.error(err);
    }
    
    log.info("End : sample1");
    return res;
  }

  /**
   * From the <code>Method</code>, return the method signature i.e.
   * method name followed by all the parms separated by commas and within
   * parentheses
   *
   * @param method	the method which the method signature is required
   * @return	method signature of the method
   */
  protected String getMethodSignature(Method method)
  {
    log.debug("Start : getMethodSignature1");
    StringBuffer strbuff = new StringBuffer();
    Class[] parameterTypes = method.getParameterTypes();
    strbuff.append(method.getName());
    strbuff.append("(");
    if(parameterTypes.length > 0)
    {
      for(int j = 0; j < (parameterTypes.length - 1); j++)
      {
        strbuff.append(parameterTypes[j].toString());
        strbuff.append(", ");
      }
      strbuff.append(parameterTypes[parameterTypes.length - 1]);
    }
    strbuff.append(")");
    String returnVal = strbuff.toString();
    log.debug("getMethodSignature1 : method signature - " + returnVal);
    log.debug("End : getMethodSignature1");
    return returnVal;
  }

  /**
   * Given a method name and a class, compares the method name against all
   * the methods in the class to look for a match.  Once found, return an
   * array containing all the Class of the parms of the method.
   */
  protected Vector getMethodParmsTypes(String methodName, Class objectClass)
  {
    log.debug("Start : getMethodParms1");
    Method[] methods = objectClass.getMethods();
    Method method = null;
    Class[] methodParmTypes = null;
    Class[] parameterTypes = null;
    StringBuffer strbuff = new StringBuffer();
    for(int i = 0; i < methods.length; i++)
    {
      // create method name which includes method signatures
      parameterTypes = methods[i].getParameterTypes();
      strbuff.delete(0, strbuff.length());
      strbuff.append(methods[i].getName());
      strbuff.append("(");
      if(parameterTypes.length > 0)
      {
        for(int j = 0; j < (parameterTypes.length - 1); j++)
        {
          strbuff.append(parameterTypes[j].toString());
          strbuff.append(", ");
        }
        strbuff.append(parameterTypes[parameterTypes.length - 1]);
      }
      strbuff.append(")");
      String name = strbuff.toString();
      if(log.isDebugEnabled())
      {
        log.debug("getMethodParms1 : current method to be compared - " 
		+ name);
      }
      if(name.equals(methodName))
      {
        method = methods[i];
        methodParmTypes = parameterTypes;
        break;
      }
    }
    Vector returnValues = new Vector();
    returnValues.add(method);
    returnValues.add(methodParmTypes);
    log.debug("End : getMethodParms1");
    return returnValues;
  }
}
