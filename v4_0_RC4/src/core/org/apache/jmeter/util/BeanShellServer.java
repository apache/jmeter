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

package org.apache.jmeter.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a BeanShell server to allow access to JMeter variables and
 * methods.
 *
 * To enable, define the JMeter property: beanshell.server.port (see
 * JMeter.java) beanshell.server.file (optional, startup file)
 *
 */
public class BeanShellServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BeanShellServer.class);

    private final int serverport;

    private final String serverfile;

    /**
     * Constructor which sets the port for this server and the path to an
     * optional init file
     * 
     * @param port
     *            the port for the server to use
     * @param file
     *            the path to an init file, or an empty string, if no init file
     *            should be used
     */
    public BeanShellServer(int port, String file) {
        super();
        serverfile = file;// can be the empty string
        serverport = port;
    }

    // For use by the server script
    static String getprop(String s) {
        return JMeterUtils.getPropDefault(s, s);
    }

    // For use by the server script
    static void setprop(String s, String v) {
        JMeterUtils.getJMeterProperties().setProperty(s, v);
    }

    @Override
    public void run() {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            Class<?> interpreter = loader.loadClass("bsh.Interpreter");//$NON-NLS-1$
            Object instance = interpreter.newInstance();
            Class<String> string = String.class;
            Class<Object> object = Object.class;

            Method eval = interpreter.getMethod("eval", new Class[] { string });//$NON-NLS-1$
            Method setObj = interpreter.getMethod("set", new Class[] { string, object });//$NON-NLS-1$
            Method setInt = interpreter.getMethod("set", new Class[] { string, int.class });//$NON-NLS-1$
            Method source = interpreter.getMethod("source", new Class[] { string });//$NON-NLS-1$

            setObj.invoke(instance, new Object[] { "t", this });//$NON-NLS-1$
            setInt.invoke(instance, new Object[] { "portnum", Integer.valueOf(serverport) });//$NON-NLS-1$

            if (serverfile.length() > 0) {
                try {
                    source.invoke(instance, new Object[] { serverfile });
                } catch (InvocationTargetException ite) {
                    Throwable cause = ite.getCause();
                    if (log.isWarnEnabled()) {
                        log.warn("Could not source, {}. {}", serverfile,
                                (cause != null) ? cause.toString() : ite.toString());
                    }
                    if (cause instanceof Error) {
                        throw (Error) cause;
                    }
                }
            }
            eval.invoke(instance, new Object[] { "setAccessibility(true);" });//$NON-NLS-1$
            eval.invoke(instance, new Object[] { "server(portnum);" });//$NON-NLS-1$

        } catch (ClassNotFoundException e) {
            log.error("Beanshell Interpreter not found");
        } catch (Exception e) {
            log.error("Problem starting BeanShell server", e);
        }
    }
}
