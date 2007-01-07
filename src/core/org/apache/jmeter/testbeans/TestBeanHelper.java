/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jmeter.testbeans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.Converter;
import org.apache.log.Logger;

/**
 * This is an experimental class. An attempt to address the complexity of
 * writing new JMeter components.
 * <p>
 * TestBean currently extends AbstractTestElement to support
 * backward-compatibility, but the property-value-map may later on be separated
 * from the test beans themselves. To ensure this will be doable with minimum
 * damage, all inherited methods are deprecated.
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart </a>
 * @version $Revision$ updated on $Date$
 */
public class TestBeanHelper {
	protected static final Logger log = LoggingManager.getLoggerForClass();

	/**
	 * Prepare the bean for work by populating the bean's properties from the
	 * property value map.
	 * <p>
	 * 
	 * @deprecated to limit it's usage in expectation of moving it elsewhere.
	 */
	public static void prepare(TestElement el) {
		if (!(el instanceof TestBean)) {
			return;
		}
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(el.getClass());
			PropertyDescriptor[] desc = beanInfo.getPropertyDescriptors();
			Object[] param = new Object[1];

			if (log.isDebugEnabled())
				log.debug("Preparing " + el.getClass());

			for (int x = 0; x < desc.length; x++) {
				// Obtain a value of the appropriate type for this property.
				JMeterProperty jprop = el.getProperty(desc[x].getName());
				Class type = desc[x].getPropertyType();
				Object value = Converter.convert(jprop.getStringValue(), type);

				if (log.isDebugEnabled())
					log.debug("Setting " + jprop.getName() + "=" + value);

				// Set the bean's property to the value we just obtained:
				if (value != null || !type.isPrimitive())
				// We can't assign null to primitive types.
				{
					param[0] = value;
					Method writeMethod = desc[x].getWriteMethod();
					if (writeMethod!=null) invokeOrBailOut(el, writeMethod, param);
				}
			}
		} catch (IntrospectionException e) {
			log.error("Couldn't set properties for " + el.getClass().getName(), e);
		}
	}

	/**
	 * Utility method that invokes a method and does the error handling around
	 * the invocation.
	 * 
	 * @param method
	 * @param params
	 * @return the result of the method invocation.
	 */
	private static Object invokeOrBailOut(Object invokee, Method method, Object[] params) {
		try {
			return method.invoke(invokee, params);
		} catch (IllegalArgumentException e) {
			log.error("This should never happen.", e);
			throw new Error(e.toString()); // Programming error: bail out.
		} catch (IllegalAccessException e) {
			log.error("This should never happen.", e);
			throw new Error(e.toString()); // Programming error: bail out.
		} catch (InvocationTargetException e) {
			log.error("This should never happen.", e);
			throw new Error(e.toString()); // Programming error: bail out.
		}
	}

	/**
	 * Utility method to obtain the value of a property in the given type.
	 * <p>
	 * I plan to get rid of this sooner than later, so please don't use it much.
	 * 
	 * @param property
	 *            Property to get the value of.
	 * @param type
	 *            Type of the result.
	 * @return an object of the given type if it is one of the known supported
	 *         types, or the value returned by property.getObjectValue
	 * @deprecated
	 */
	private static Object unwrapProperty(JMeterProperty property, Class type) {
		return Converter.convert(property.getObjectValue(), type);
	}
}
