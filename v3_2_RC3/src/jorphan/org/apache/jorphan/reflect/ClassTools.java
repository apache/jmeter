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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.ClassUtils;
import org.apache.jorphan.util.JMeterException;

/**
 * Utility methods for handling dynamic access to classes.
 */
public class ClassTools {


    /**
     * Call no-args constructor for a class.
     *
     * @param className name of the class to be constructed
     * @return an instance of the class
     * @throws JMeterException if class cannot be created
     */
    public static Object construct(String className) throws JMeterException {
        Object instance = null;
        try {
            instance = ClassUtils.getClass(className).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException
                | InstantiationException e) {
            throw new JMeterException(e);
        }
        return instance;
    }

    /**
     * Call a class constructor with an integer parameter
     * @param className name of the class to be constructed
     * @param parameter the value to be used in the constructor
     * @return an instance of the class
     * @throws JMeterException if class cannot be created
     */
    public static Object construct(String className, int parameter) throws JMeterException
    {
        Object instance = null;
        try {
            Class<?> clazz = ClassUtils.getClass(className);
            Constructor<?> constructor = clazz.getConstructor(Integer.TYPE);
            instance = constructor.newInstance(Integer.valueOf(parameter));
        } catch (ClassNotFoundException | InvocationTargetException
                | IllegalArgumentException | NoSuchMethodException
                | SecurityException | IllegalAccessException
                | InstantiationException e) {
            throw new JMeterException(e);
        }
        return instance;
    }

    /**
     * Call a class constructor with an String parameter
     * @param className the name of the class to construct
     * @param parameter to be used for the construction of the class instance
     * @return an instance of the class
     * @throws JMeterException if class cannot be created
     */
    public static Object construct(String className, String parameter)
            throws JMeterException {
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor(String.class);
            instance = constructor.newInstance(parameter);
        } catch (ClassNotFoundException | InvocationTargetException
                | IllegalArgumentException | NoSuchMethodException
                | IllegalAccessException | InstantiationException e) {
            throw new JMeterException(e);
        }
        return instance;
    }

    /**
     * Invoke a public method on a class instance
     *
     * @param instance
     *            object on which the method should be called
     * @param methodName
     *            name of the method to be called
     * @throws SecurityException
     *             if a security violation occurred while looking for the method
     * @throws IllegalArgumentException
     *             if the method parameters (none given) do not match the
     *             signature of the method
     * @throws JMeterException
     *             if something went wrong in the invoked method
     */
    public static void invoke(Object instance, String methodName)
    throws SecurityException, IllegalArgumentException, JMeterException
    {
        Method m;
        try {
            m = ClassUtils.getPublicMethod(instance.getClass(), methodName, new Class [] {});
            m.invoke(instance, (Object [])null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new JMeterException(e);
        }
    }
}
