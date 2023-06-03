/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.testbeans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
     * This class extracts information from {@link CachedPropertyDescriptor} in such a way so it
     * does not need to synchronize.
     */
    static class CachedPropertyDescriptor {
        final PropertyDescriptor descriptor;
        /**
         * Cached value for {@link PropertyDescriptor#getWriteMethod()}.
         * {@code getWriteMethod} is {@code synchronized} in OpenJDK 17.
         *
         * @see PropertyDescriptor#getWriteMethod()
         */
        final Method writeMethod;
        /**
         * Cached value for {@link PropertyDescriptor#getPropertyType()}.
         * {@code getPropertyType} is {@code synchronized} in OpenJDK 17.
         *
         * @see PropertyDescriptor#getPropertyType()
         */
        final Class<?> propertyType;

        CachedPropertyDescriptor(PropertyDescriptor descriptor) {
            this.descriptor = descriptor;
            this.writeMethod = descriptor.getWriteMethod();
            this.propertyType = descriptor.getPropertyType();
        }
    }

    /**
     * Cache property information, so preparing test elements does not need to perform reflective and
     * synchronization again.
     */
    private static final ClassValue<List<CachedPropertyDescriptor>> GOOD_PROPS = new ClassValue<List<CachedPropertyDescriptor>>() {
        @Override
        protected List<CachedPropertyDescriptor> computeValue(Class type) {
            PropertyDescriptor[] descs;
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(type);
                descs = beanInfo.getPropertyDescriptors();
            } catch (IntrospectionException e) {
                log.error("Couldn't set properties for {}", type, e);
                throw new IllegalArgumentException("Couldn't set properties for " + type, e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Preparing {}", type);
            }

            List<CachedPropertyDescriptor> descriptors = new ArrayList<>(descs.length);
            for (PropertyDescriptor desc : descs) {
                if (isDescriptorIgnored(desc)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Ignoring property '{}' in {}", desc.getName(), type.getCanonicalName());
                    }
                    continue;
                }
                Method writeMethod = desc.getWriteMethod();
                if (writeMethod == null) {
                    continue;
                }
                descriptors.add(new CachedPropertyDescriptor(desc));
            }
            return descriptors;
        }
    };

    /**
     * Prepare the bean for work by populating the bean's properties from the
     * property value map.
     *
     * @param el the TestElement to be prepared
     */
    public static void prepare(TestElement el) {
        if (!(el instanceof TestBean)) {
            return;
        }
        // Avoid allocating array for every method call
        Object[] tmp = new Object[1];
        try {
            for (CachedPropertyDescriptor desc : GOOD_PROPS.get(el.getClass())) {
                // Obtain a value of the appropriate type for this property.
                JMeterProperty jprop = el.getProperty(desc.descriptor.getName());
                Class<?> type = desc.propertyType;
                Object value = unwrapProperty(desc.descriptor, jprop, type);

                if (log.isDebugEnabled()) {
                    log.debug("Setting {}={}", jprop.getName(), value);
                }

                // Set the bean's property to the value we just obtained:
                // We can't assign null to primitive types.
                if (value != null || !type.isPrimitive()) {
                    Method writeMethod = desc.writeMethod;
                    tmp[0] = value;
                    invokeOrBailOut(el, writeMethod, tmp);
                }
            }
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
            Collection<Object> values = new ArrayList<>();
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
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new Error(createMessage(invokee, method, params), e);
        } catch (InvocationTargetException e) {
            throw new Error(createMessage(invokee, method, params), e.getCause());
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
