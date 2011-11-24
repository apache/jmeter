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
import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.testbeans.gui.GenericTestBeanCustomizer;
import org.apache.jmeter.testbeans.gui.TableEditor;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
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
    @Deprecated
    public static void prepare(TestElement el) {
        if (!(el instanceof TestBean)) {
            return;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(el.getClass());
            PropertyDescriptor[] desc = beanInfo.getPropertyDescriptors();
            Object[] param = new Object[1];

            if (log.isDebugEnabled()) {
                log.debug("Preparing " + el.getClass());
            }

            for (int x = 0; x < desc.length; x++) {
                // Obtain a value of the appropriate type for this property.
                JMeterProperty jprop = el.getProperty(desc[x].getName());
                Class<?> type = desc[x].getPropertyType();
                Object value = unwrapProperty(desc[x], jprop, type);

                if (log.isDebugEnabled()) {
                    log.debug("Setting " + jprop.getName() + "=" + value);
                }

                // Set the bean's property to the value we just obtained:
                if (value != null || !type.isPrimitive())
                // We can't assign null to primitive types.
                {
                    param[0] = value;
                    Method writeMethod = desc[x].getWriteMethod();
                    if (writeMethod!=null) {
                        invokeOrBailOut(el, writeMethod, param);
                    }
                }
            }
        } catch (IntrospectionException e) {
            log.error("Couldn't set properties for " + el.getClass().getName(), e);
        }
    }

    /**
     * @param desc
     * @param x
     * @param jprop
     * @param type
     * @return
     */
    private static Object unwrapProperty(PropertyDescriptor desc, JMeterProperty jprop, Class<?> type) {
        Object value;
        if(jprop instanceof TestElementProperty)
        {
            TestElement te = ((TestElementProperty)jprop).getElement();
            if(te instanceof TestBean)
            {
                prepare(te);
            }
            value = te;
        }
        else if(jprop instanceof MultiProperty)
        {
            value = unwrapCollection((MultiProperty)jprop,(String)desc.getValue(TableEditor.CLASSNAME));
        }
        // value was not provided, and this is allowed
        else if (jprop instanceof NullProperty &&
                // use negative condition so missing (null) value is treated as FALSE
                ! Boolean.TRUE.equals(desc.getValue(GenericTestBeanCustomizer.NOT_UNDEFINED)))
        {    
            value=null;
        }
        else value = Converter.convert(jprop.getStringValue(), type);
        return value;
    }

    private static Object unwrapCollection(MultiProperty prop,String type)
    {
        if(prop instanceof CollectionProperty)
        {
            Collection<Object> values = new LinkedList<Object>();
            PropertyIterator iter = prop.iterator();
            while(iter.hasNext())
            {
                try
                {
                    values.add(unwrapProperty(null,iter.next(),Class.forName(type)));
                }
                catch(Exception e)
                {
                    log.error("Couldn't convert object: " + prop.getObjectValue() + " to " + type,e);
                }
            }
            return values;
        }
        return null;
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
            log.error("This should never happen. "+invokee.getClass().getName()+" "+method.getName()+" "+params.length+" "+params[0].getClass().getName(), e);
            throw new Error(e.toString()); // Programming error: bail out.
        } catch (IllegalAccessException e) {
            log.error("This should never happen.", e);
            throw new Error(e.toString()); // Programming error: bail out.
        } catch (InvocationTargetException e) {
            log.error("This should never happen.", e);
            throw new Error(e.toString()); // Programming error: bail out.
        }
    }
}
