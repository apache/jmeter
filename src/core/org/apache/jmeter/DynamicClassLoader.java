/*
 * Created on Jul 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
