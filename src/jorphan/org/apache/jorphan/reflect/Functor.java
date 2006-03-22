/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
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

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 */
public class Functor {
	private static Logger log = LoggingManager.getLoggerForClass();

	Object invokee;

	String methodName;

	Object[] args;

	Class[] types;

	Method methodToInvoke;

	/**
	 * Create a functor with the invokee and a method name.
	 * 
	 * @param invokee
	 * @param methodName
	 */
	public Functor(Object invokee, String methodName) {
		this(methodName);
		this.invokee = invokee;
	}

	/**
	 * Create a functor with the invokee, method name, and argument class types.
	 * 
	 * @param invokee
	 * @param methodName
	 * @param types
	 */
	public Functor(Object invokee, String methodName, Class[] types) {
		this(invokee, methodName);
		this.types = types;
	}

	/**
	 * Create a functor with just the method name.
	 * 
	 * @param methodName
	 */
	public Functor(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Create a functor with the method name and argument class types.
	 * 
	 * @param methodName
	 * @param types
	 */
	public Functor(String methodName, Class[] types) {
		this(methodName);
		this.types = types;
	}

	/**
	 * Create a functor with an invokee, method name, and argument values.
	 * 
	 * @param invokee
	 * @param methodName
	 * @param args
	 */
	public Functor(Object invokee, String methodName, Object[] args) {
		this(invokee, methodName);
		this.args = args;
	}

	public Functor(String methodName, Object[] args) {
		this(methodName);
		this.args = args;
	}

	/**
	 * Create a functor with an invokee, method name, argument values, and
	 * argument class types.
	 * 
	 * @param invokee
	 * @param methodName
	 * @param args
	 * @param types
	 */
	public Functor(Object invokee, String methodName, Object[] args, Class[] types) {
		this(invokee, methodName, args);
		this.types = types;
	}

	public Object invoke() {
		try {
			return createMethod(getTypes()).invoke(invokee, getArgs());
		} catch (Exception e) {
			log.warn("Trouble functing method: ", e);
			throw new org.apache.jorphan.util.JMeterError(e); // JDK1.4
		}
	}

	public Object invoke(Object p_invokee) {
		this.invokee = p_invokee;
		return invoke();
	}

	public Object invoke(Object[] p_args) {
		this.args = p_args;
		return invoke();
	}

	public Object invoke(Object p_invokee, Object[] p_args) {
		this.args = p_args;
		this.invokee = p_invokee;
		return invoke();
	}

	private Method createMethod(Class[] p_types) {
		log.debug("Trying to functorize invokee: " + invokee.getClass().getName() + " method: " + methodName
				+ " types: " + Arrays.asList(p_types));
		if (methodToInvoke == null) {
			try {
				methodToInvoke = invokee.getClass().getMethod(methodName, p_types);
			} catch (Exception e) {
				for (int i = 0; i < p_types.length; i++) {
					Class primitive = getPrimitive(p_types[i]);
					if (primitive != null) {
						methodToInvoke = createMethod(getNewArray(i, primitive, p_types));
						if (methodToInvoke != null)
							return methodToInvoke;
					}
					Class[] interfaces = p_types[i].getInterfaces();
					for (int j = 0; j < interfaces.length; j++) {
						methodToInvoke = createMethod(getNewArray(i, interfaces[j], p_types));
						if (methodToInvoke != null) {
							return methodToInvoke;
						}
					}
					Class parent = p_types[i].getSuperclass();
					methodToInvoke = createMethod(getNewArray(i, parent, p_types));
					if (methodToInvoke != null) {
						return methodToInvoke;
					}
				}
			}
		}
		return methodToInvoke;
	}

	protected Class getPrimitive(Class t) {
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

	protected Class[] getNewArray(int i, Class replacement, Class[] orig) {
		Class[] newArray = new Class[orig.length];
		for (int j = 0; j < newArray.length; j++) {
			if (j == i) {
				newArray[j] = replacement;
			} else {
				newArray[j] = orig[j];				
			}
		}
		return newArray;
	}

	private Class[] getTypes() {
		if (types == null) // do only once per functor instance. Could
		// cause errors if functor used for multiple
		// same-named-different-parametered methods.
		{
			if (args != null) {
				types = new Class[args.length];
				for (int i = 0; i < args.length; i++) {
					types[i] = args[i].getClass();
				}
			} else {
				types = new Class[0];
			}
		}
		return types;
	}

	private Object[] getArgs() {
		if (args == null) {
			args = new Object[0];
		}
		return args;
	}
}
