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

package org.apache.jmeter.protocol.jms.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * InitialContextFactory is responsible for getting an instance of the initial context.
 */
public class InitialContextFactory {

    //GuardedBy("this")
    private static final HashMap<String, Context> MAP = new HashMap<String, Context>();

    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Look up the context from the local cache, creating it if necessary.
     * 
     * @param initialContextFactory used to set the property {@link Context#INITIAL_CONTEXT_FACTORY}
     * @param providerUrl used to set the property {@link Context#PROVIDER_URL}
     * @param useAuth set true if security is to be used.
     * @param securityPrincipal used to set the property {@link Context#SECURITY_PRINCIPAL}
     * @param securityCredentials used to set the property {@link Context#SECURITY_CREDENTIALS}
     * @return the context, never null
     * @throws NamingException 
     */
    public static synchronized Context lookupContext(String initialContextFactory, 
            String providerUrl, boolean useAuth, String securityPrincipal, String securityCredentials) throws NamingException {
        Context ctx = MAP.get(initialContextFactory + providerUrl);
        if (ctx == null) {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            props.setProperty(Context.PROVIDER_URL, providerUrl);
            if (useAuth && securityPrincipal != null && securityCredentials != null
                    && securityPrincipal.length() > 0 && securityCredentials.length() > 0) {
                props.setProperty(Context.SECURITY_PRINCIPAL, securityPrincipal);
                props.setProperty(Context.SECURITY_CREDENTIALS, securityCredentials);
                log.info("authentication properties set");
            }
            ctx = new InitialContext(props);
            MAP.put(initialContextFactory + providerUrl, ctx);
        }
        return ctx;
    }

    /**
     * Initialize the JNDI initial context
     *
     * @param useProps if true, create a new InitialContext; otherwise use the other parameters to call
     * {@link #lookupContext(String, String, boolean, String, String)} 
     * @param initialContextFactory
     * @param providerUrl
     * @param useAuth
     * @param securityPrincipal
     * @param securityCredentials
     * @return  the context, never null
     * @throws NamingException 
     */
    public static Context getContext(boolean useProps, 
            String initialContextFactory, String providerUrl, 
            boolean useAuth, String securityPrincipal, String securityCredentials) throws NamingException {
        if (useProps) {
            return new InitialContext();
        } else {
            return lookupContext(initialContextFactory, providerUrl, useAuth, securityPrincipal, securityCredentials);
        }
    }
    /**
     * clear all the InitialContext objects.
     */
    public synchronized static void close() { // TODO - why is this not used?
        Iterator<?> itr = MAP.keySet().iterator();
        while (itr.hasNext()) {
            Context ctx = MAP.get(itr.next());
            try {
                ctx.close();
            } catch (NamingException e) {
                log.error(e.getMessage());
            }
        }
        MAP.clear();
        log.info("InitialContextFactory.close() called and Context instances cleaned up");
    }
}
