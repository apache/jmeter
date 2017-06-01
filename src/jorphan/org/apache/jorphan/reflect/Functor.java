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
 *
 */

package org.apache.jorphan.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.jorphan.util.JMeterError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements function call-backs.
 * <p>
 * Functors may be defined for instance objects or classes.
 * <p>
 * The method is created on first use, which allows the invokee (class or instance)
 * to be omitted from the constructor.
 * <p>
 * The class name takes precedence over the instance.
 * <p>
 * If a functor is created with a particular instance, then that is used for all future calls;
 * if an object is provided, it is ignored.
 * This allows easy override of the table model behaviour.
 * <p>
 * If an argument list is provided in the constructor, then that is ignored in subsequent invoke() calls.
 * <p>
 * Usage:
 *
 * <pre>
 * f = new Functor("methodName");
 * o = f.invoke(object); // - OR -
 * o = f.invoke(object, params);
 * </pre>
 * 
 * <pre>
 * f2 = new Functor(object, "methodName");
 * o = f2.invoke(); // - OR -
 * o = f2.invoke(params);
 * </pre>
 *
 * <pre>
 * f3 = new Functor(class, "methodName");
 * o = f3.invoke(object); // - will be ignored
 * o = f3.invoke(); // - OR -
 * o = f3.invoke(params);
 * o = f3.invoke(object, params); // - object will be ignored
 * </pre>
 */
public class Functor {
    private static final Logger log = LoggerFactory.getLogger(Functor.class);

    /*
     * If non-null, then any object provided to invoke() is ignored.
     */
    private final Object invokee;

    /*
     * Class to be used to create the Method.
     * Will be non-null if either Class or Object was provided during construction.
     *
     *  Can be used instead of invokee, e.g. when using interfaces.
    */
    private final Class<?> clazz;

    // Methondname must always be provided.
    private final String methodName;

    /*
     * If non-null, then any argument list passed to invoke() will be ignored.
     */
    private Object[] args;

    /*
     * Argument types used to create the method.
     * May be provided explicitly, or derived from the constructor argument list.
     */
    private final Class<?>[] types;

    /*
     * This depends on the class or invokee and either args or types;
     * it is set once by doCreateMethod(), which must be the only method to access it.
    */
    private Method methodToInvoke;

    Functor(){
        throw new IllegalArgumentException("Must provide at least one argument");
    }

    /**
     * Create a functor with the invokee and a method name.
     *
     * The invokee will be used in all future invoke calls.
     *
     * @param _invokee object on which to invoke the method
     * @param _methodName method name
     */
    public Functor(Object _invokee, String _methodName) {
        this(null, _invokee, _methodName, null, null);
    }

    /**
     * Create a functor from class and method name.
     * This is useful for methods defined in interfaces.
     *
     * The actual invokee must be provided in all invoke() calls,
     * and must be an instance of the class.
     *
     * @param _clazz class to be used
     * @param _methodName method name
     */
    public Functor(Class<?> _clazz, String _methodName) {
        this(_clazz, null, _methodName, null, null);
    }

    /**
     * Create a functor with the invokee, method name, and argument class types.
     *
     * The invokee will be ignored in any invoke() calls.
     *
     * @param _invokee object on which to invoke the method
     * @param _methodName method name
     * @param _types types of arguments to be used
     */
    public Functor(Object _invokee, String _methodName, Class<?>[] _types) {
        this(null, _invokee, _methodName, null, _types);
    }

    /**
     * Create a functor with the class, method name, and argument class types.
     *
     * Subsequent invoke() calls must provide the appropriate invokee object.
     *
     * @param _clazz the class in which to find the method
     * @param _methodName method name
     * @param _types types of arguments to be used
     */
    public Functor(Class<?> _clazz, String _methodName, Class<?>[] _types) {
        this(_clazz, null, _methodName, null, _types);
    }

    /**
     * Create a functor with just the method name.
     *
     * The invokee and any parameters must be provided in all invoke() calls.
     *
     * @param _methodName method name
     */
    public Functor(String _methodName) {
        this(null, null, _methodName, null, null);
    }

    /**
     * Create a functor with the method name and argument class types.
     *
     * The invokee must be provided in all invoke() calls
     *
     * @param _methodName method name
     * @param _types parameter types
     */
    public Functor(String _methodName, Class<?>[] _types) {
        this(null, null, _methodName, null, _types);
    }

    /**
     * Create a functor with an invokee, method name, and argument values.
     *
     * The invokee will be ignored in any invoke() calls.
     *
     * @param _invokee object on which to invoke the method
     * @param _methodName method name
     * @param _args arguments to be passed to the method
     */
    public Functor(Object _invokee, String _methodName, Object[] _args) {
        this(null, _invokee, _methodName, _args, null);
    }

    /**
     * Create a functor from method name and arguments.
     *
     * The class will be determined from the first invoke call.
     * All invoke calls must include a target object;
     * which must be of the same type as the initial invokee.
     *
     * @param _methodName method name
     * @param _args arguments to be used
     */
    public Functor(String _methodName, Object[] _args) {
        this(null, null, _methodName, _args, null);
    }

    /**
     * Create a functor from various different combinations of parameters.
     *
     * @param _clazz class containing the method
     * @param _invokee invokee to use for the method call
     * @param _methodName the method name (required)
     * @param _args arguments to be used
     * @param _types types of arguments to be used
     *
     * @throws IllegalArgumentException if:
     * - methodName is null
     * - both class and invokee are specified
     * - both arguments and types are specified
     */
    private Functor(Class<?> _clazz, Object _invokee, String _methodName, Object[] _args, Class<?>[] _types) {
        if (_methodName == null){
            throw new IllegalArgumentException("Methodname must not be null");
        }
        if (_clazz != null && _invokee != null){
            throw new IllegalArgumentException("Cannot provide both Class and Object");
        }
        if (_args != null && _types != null){
            throw new IllegalArgumentException("Cannot provide both arguments and argument types");
        }
        // If class not provided, default to invokee class, else null
        this.clazz = _clazz != null ? _clazz : (_invokee != null ? _invokee.getClass() : null);
        this.invokee = _invokee;
        this.methodName = _methodName;
        this.args = _args;
        // If types not provided, default to argument types, else null
        this.types = _types != null ? _types : (_args != null ? _getTypes(_args) : null);
    }

    //////////////////////////////////////////

    /*
     * Low level invocation routine.
     *
     * Should only be called after any defaults have been applied.
     *
     */
    private Object doInvoke(Class<?> _class, Object _invokee, Object[] _args) {
        Class<?>[] argTypes = getTypes(_args);
        try {
            Method method = doCreateMethod(_class , argTypes);
            if (method == null){
                final String message = "Can't find method "
                    +_class.getName()+"#"+methodName+typesToString(argTypes);
                log.error(message, new Throwable());
                throw new JMeterError(message);
            }
            return method.invoke(_invokee, _args);
        } catch (Exception e) {
            final String message = "Trouble functing: "
                +_class.getName()
                +"."+methodName+"(...) : "
                +" invokee: "+_invokee
                +" "+e.getMessage();
            log.warn(message, e);
            throw new JMeterError(message,e);
        }
    }

    /**
     * Invoke a Functor, which must have been created with either a class name or object.
     *
     * @return the object if any
     */
    public Object invoke() {
        if (invokee == null) {
            throw new IllegalStateException("Cannot call invoke() - invokee not known");
        }
        // If invokee was provided, then clazz has been set up
        return doInvoke(clazz, invokee, getArgs());
    }

    /**
     * Invoke the method on a given object.
     *
     * @param p_invokee - provides the object to call; ignored if the class or object were provided to the constructor
     * @return the value
     */
    public Object invoke(Object p_invokee) {
        return invoke(p_invokee, getArgs());
    }

    /**
     * Invoke the method with the provided parameters.
     *
     * The invokee must have been provided in the constructor.
     *
     * @param p_args parameters for the method
     * @return the value
     */
    public Object invoke(Object[] p_args) {
        if (invokee == null){
            throw new IllegalStateException("Invokee was not provided in constructor");
        }
        // If invokee was provided, then clazz has been set up
        return doInvoke(clazz, invokee, args != null? args : p_args);
    }

    /**
     * Invoke the method on the invokee with the provided parameters.
     *
     * The invokee must agree with the class (if any) provided at construction time.
     *
     * If the invokee was provided at construction time, then this invokee will be ignored.
     * If actual arguments were provided at construction time, then arguments will be ignored.
     * @param p_invokee invokee to use, if no class or invokee was provided at construction time
     * @param p_args arguments to use
     * @return result of invocation
     *
     */
    public Object invoke(Object p_invokee, Object[] p_args) {
        return doInvoke(clazz != null ? clazz : p_invokee.getClass(), // Use constructor class if present
                       invokee != null ? invokee : p_invokee, // use invokee if provided
                        args != null? args : p_args);// use arguments if provided
    }

    /*
     * Low-level (recursive) routine to define the method - if not already defined.
     * Synchronized to protect access to methodToInvoke.
     */
    private synchronized Method doCreateMethod(Class<?> p_class, Class<?>[] p_types) {
        if (log.isDebugEnabled()){
            log.debug("doCreateMethod() using "+this.toString()
                +"class="
                + p_class.getName()
                + " types: " + Arrays.asList(p_types));
        }
        if (methodToInvoke == null) {
            try {
                methodToInvoke = p_class.getMethod(methodName, p_types);
            } catch (Exception e) {
                for (int i = 0; i < p_types.length; i++) {
                    Class<?> primitive = getPrimitive(p_types[i]);
                    if (primitive != null) {
                        methodToInvoke = doCreateMethod(p_class, getNewArray(i, primitive, p_types));
                        if (methodToInvoke != null) {
                            return methodToInvoke;
                        }
                    }
                    Class<?>[] interfaces = p_types[i].getInterfaces();
                    for (Class<?> anInterface : interfaces) {
                        methodToInvoke = doCreateMethod(p_class, getNewArray(i, anInterface, p_types));
                        if (methodToInvoke != null) {
                            return methodToInvoke;
                        }
                    }
                    Class<?> parent = p_types[i].getSuperclass();
                    if (parent != null) {
                        methodToInvoke = doCreateMethod(p_class,getNewArray(i, parent, p_types));
                        if (methodToInvoke != null) {
                            return methodToInvoke;
                        }
                    }
                }
            }
        }
        return methodToInvoke;
    }

    /**
     * Check if a read Functor method is valid.
     * 
     * @param _invokee
     *            instance on which the method should be tested
     *
     * @deprecated ** for use by Unit test code only **
     *
     * @return <code>true</code> if method exists
     */
    @Deprecated
    public boolean checkMethod(Object _invokee){
        Method m = null;
        try {
            m = doCreateMethod(_invokee.getClass(), getTypes(args));
        } catch (Exception e){
            // ignored
        }
        return null != m;
    }

    /**
     * Check if a write Functor method is valid.
     * 
     * @param _invokee
     *            instance on which the method should be tested
     * @param c
     *            type of parameter
     *
     * @deprecated ** for use by Unit test code only **
     *
     * @return <code>true</code> if method exists
     */
    @Deprecated
    public boolean checkMethod(Object _invokee, Class<?> c){
        Method m = null;
        try {
            m = doCreateMethod(_invokee.getClass(), new Class[]{c});
        } catch (Exception e){
            // ignored
        }
        return null != m;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(100);
        if (clazz != null){
            sb.append(clazz.getName());
        }
        if (invokee != null){
            sb.append("@");
            sb.append(System.identityHashCode(invokee));
        }
        sb.append(".");
        sb.append(methodName);
        typesToString(sb,types);
        return sb.toString();
    }

    private void typesToString(StringBuilder sb,Class<?>[] _types) {
        sb.append("(");
        if (_types != null){
            for(int i=0; i < _types.length; i++){
                if (i>0) {
                    sb.append(",");
                }
                sb.append(_types[i].getName());
            }
        }
        sb.append(")");
    }

    private String typesToString(Class<?>[] argTypes) {
        StringBuilder sb = new StringBuilder();
        typesToString(sb,argTypes);
        return sb.toString();
    }

    private Class<?> getPrimitive(Class<?> t) {
        if (t==null) {
            return null;
        }
        if (t.equals(Integer.class)) {
            return int.class;
        } else if (t.equals(Long.class)) {
            return long.class;
        } else if (t.equals(Double.class)) {
            return double.class;
        } else if (t.equals(Float.class)) {
            return float.class;
        } else if (t.equals(Byte.class)) {
            return byte.class;
        } else if (t.equals(Boolean.class)) {
            return boolean.class;
        } else if (t.equals(Short.class)) {
            return short.class;
        } else if (t.equals(Character.class)) {
            return char.class;
        }
        return null;
    }

    private Class<?>[] getNewArray(int i, Class<?> replacement, Class<?>[] orig) {
        Class<?>[] newArray = new Class[orig.length];
        for (int j = 0; j < newArray.length; j++) {
            if (j == i) {
                newArray[j] = replacement;
            } else {
                newArray[j] = orig[j];
            }
        }
        return newArray;
    }

    private Class<?>[] getTypes(Object[] _args) {
        if (types == null)
        {
            return _getTypes(_args);
        }
        return types;
    }

    private static Class<?>[] _getTypes(Object[] _args) {
        Class<?>[] _types;
        if (_args != null) {
            _types = new Class[_args.length];
            for (int i = 0; i < _args.length; i++) {
                _types[i] = _args[i].getClass();
            }
        } else {
            _types = new Class[0];
        }
        return _types;
    }

    private Object[] getArgs() {
        if (args == null) {
            args = new Object[0];
        }
        return args;
    }
}
