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
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected static final Logger log = LoggerFactory.getLogger(TestBeanHelper.class);

    /**
     * Prepare the bean for work by populating the bean's properties from the
     * property value map.
     * <p>
     *
     * @param el the TestElement to be prepared
     */
    public static void prepare(TestElement el) {
        if (!(el instanceof TestBean)) {
            return;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(el.getClass());
            PropertyDescriptor[] descs = beanInfo.getPropertyDescriptors();

            if (log.isDebugEnabled()) {
                log.debug("Preparing {}", el.getClass());
            }

            for (PropertyDescriptor desc : descs) {
                if (isDescriptorIgnored(desc)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Ignoring property '{}' in {}", desc.getName(), el.getClass().getCanonicalName());
                    }
                    continue;
                }
                // Obtain a value of the appropriate type for this property.
                JMeterProperty jprop = el.getProperty(desc.getName());
                Class<?> type = desc.getPropertyType();
                Object value = unwrapProperty(desc, jprop, type);

                if (log.isDebugEnabled()) {
                    log.debug("Setting {}={}", jprop.getName(), value);
                }

                // Set the bean's property to the value we just obtained:
                if (value != null || !type.isPrimitive())
                // We can't assign null to primitive types.
                {
                    Method writeMethod = desc.getWriteMethod();
                    if (writeMethod!=null) {
                        invokeOrBailOut(el, writeMethod, new Object[] {value});
                    }
                }
            }
        } catch (IntrospectionException e) {
            log.error("Couldn't set properties for {}", el.getClass(), e);
        } catch (UnsatisfiedLinkError ule) { // Can occur running headless on Jenkins
            log.error("Couldn't set properties for {}", el.getClass());
            throw ule;
        }
    }

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
                ! Boolean.TRUE.equals(desc.getValue(GenericTestBeanCustomizer.NOT_UNDEFINED))) {
            value=null;
        } else {
            value = Converter.convert(jprop.getStringValue(), type);
        }
        return value;
    }

    private static Object unwrapCollection(MultiProperty prop, String type)
    {
        if(prop instanceof CollectionProperty)
        {
            Collection<Object> values = new LinkedList<>();
            for (JMeterProperty jMeterProperty : prop) {
                try {
                    values.add(unwrapProperty(null, jMeterProperty, Class.forName(type)));
                }
                catch(Exception e) {
                    log.error("Couldn't convert object: {} to {}", prop.getObjectValue(), type, e);
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
     * @param invokee
     *            the object on which the method should be invoked
     * @param method
     *            the method which should be invoked
     * @param params
     *            the parameters for the method
     * @return the result of the method invocation.
     */
    private static Object invokeOrBailOut(Object invokee, Method method, Object[] params) {
        try {
            return method.invoke(invokee, params);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new Error(createMessage(invokee, method, params), e);
        }
    }

    private static String createMessage(Object invokee, Method method, Object[] params){
        StringBuilder sb = new StringBuilder();
        sb.append("This should never happen. Tried to invoke:\n");
        sb.append(invokee.getClass().getName());
        sb.append("#");
        sb.append(method.getName());
        sb.append("(");
        for(Object o : params) {
            if (o != null) {
                sb.append(o.getClass().getSimpleName());
                sb.append(' ');
            }
            sb.append(o);
            sb.append(' ');
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Checks whether the descriptor should be ignored, i.e.
     * <ul>
     * <li>isHidden</li>
     * <li>isExpert and JMeter not using expert mode</li>
     * <li>no read method</li>
     * <li>no write method</li>
     * </ul>
     * @param descriptor the {@link PropertyDescriptor} to be checked
     * @return <code>true</code> if the descriptor should be ignored
     */
    public static boolean isDescriptorIgnored(PropertyDescriptor descriptor) {
        return descriptor.isHidden()
            || (descriptor.isExpert() && !JMeterUtils.isExpertMode())
            || descriptor.getReadMethod() == null
            || descriptor.getWriteMethod() == null;
    }
}
