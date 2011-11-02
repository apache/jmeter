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

package org.apache.jorphan.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.ClassUtils;
import org.apache.jorphan.util.JMeterException;

/**
 * Utility methods for handling dynamic access to classes.
 */
public class ClassTools {


    /**
     * Call no-args constructor for a class.
     *
     * @param className
     * @return an instance of the class
     * @throws JMeterException if class cannot be created
     */
    public static Object construct(String className) throws JMeterException {
        Object instance = null;
        try {
            instance = ClassUtils.getClass(className).newInstance();
        } catch (ClassNotFoundException e) {
            throw new JMeterException(e);
        } catch (InstantiationException e) {
            throw new JMeterException(e);
        } catch (IllegalAccessException e) {
            throw new JMeterException(e);
        }
        return instance;
    }

    /**
     * Call a class constructor with an integer parameter
     * @param className
     * @param parameter (integer)
     * @return an instance of the class
     * @throws JMeterException if class cannot be created
     */
    public static Object construct(String className, int parameter) throws JMeterException
    {
        Object instance = null;
        try {
            Class<?> clazz = ClassUtils.getClass(className);
            clazz.getConstructor(new Class [] {Integer.TYPE});
            instance = ClassUtils.getClass(className).newInstance();
        } catch (ClassNotFoundException e) {
            throw new JMeterException(e);
        } catch (InstantiationException e) {
            throw new JMeterException(e);
        } catch (IllegalAccessException e) {
            throw new JMeterException(e);
        } catch (SecurityException e) {
            throw new JMeterException(e);
        } catch (NoSuchMethodException e) {
            throw new JMeterException(e);
        }
        return instance;
    }

    /**
     * Invoke a public method on a class instance
     *
     * @param instance
     * @param methodName
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws JMeterException
     */
    public static void invoke(Object instance, String methodName)
    throws SecurityException, IllegalArgumentException, JMeterException
    {
        Method m;
        try {
            m = ClassUtils.getPublicMethod(instance.getClass(), methodName, new Class [] {});
            m.invoke(instance, (Object [])null);
        } catch (NoSuchMethodException e) {
            throw new JMeterException(e);
        } catch (IllegalAccessException e) {
            throw new JMeterException(e);
        } catch (InvocationTargetException e) {
            throw new JMeterException(e);
        }
    }
}
