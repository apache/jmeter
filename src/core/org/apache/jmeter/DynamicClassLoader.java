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
package org.apache.jmeter;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * This is a basic URL classloader for loading new resources
 * dynamically.
 *
 * It allows public access to the addURL() method.
 *
 * It also adds a convenience method to update the current thread classloader
 *
 */
public class DynamicClassLoader extends URLClassLoader {

    public DynamicClassLoader(URL[] urls) {
        super(urls);
    }

    public DynamicClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public DynamicClassLoader(URL[] urls, ClassLoader parent,
            URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    // Make the addURL method visible
    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    /**
     *
     * @param urls - list of URLs to add to the thread's classloader
     */
    public static void updateLoader(URL [] urls) {
        DynamicClassLoader loader
            = (DynamicClassLoader) Thread.currentThread().getContextClassLoader();
        for(URL url : urls) {
            loader.addURL(url);
        }
    }
}
