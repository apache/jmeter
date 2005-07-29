/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.jmeter;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * @author pete
 *
 * This is a basic URL classloader for loading new resources
 * dynamically
 */
public class DynamicClassLoader extends URLClassLoader {

	/**
	 * @param arg0
	 */
	public DynamicClassLoader(URL[] arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DynamicClassLoader(URL[] arg0, ClassLoader arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public DynamicClassLoader(URL[] arg0, ClassLoader arg1,
			URLStreamHandlerFactory arg2) {
		super(arg0, arg1, arg2);
	}

    public void addURL(URL url) {
        this.addURL(url);
    }
}
