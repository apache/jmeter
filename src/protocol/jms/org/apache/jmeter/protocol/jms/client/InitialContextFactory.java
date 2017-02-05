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

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InitialContextFactory is responsible for getting an instance of the initial context.
 */
public class InitialContextFactory {

    private static final ConcurrentHashMap<String, Context> MAP = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(InitialContextFactory.class);

    /**
     * Look up the context from the local cache, creating it if necessary.
     * 
     * @param initialContextFactory used to set the property {@link Context#INITIAL_CONTEXT_FACTORY}
     * @param providerUrl used to set the property {@link Context#PROVIDER_URL}
     * @param useAuth set <code>true</code> if security is to be used.
     * @param securityPrincipal used to set the property {@link Context#SECURITY_PRINCIPAL}
     * @param securityCredentials used to set the property {@link Context#SECURITY_CREDENTIALS}
     * @return the context, never <code>null</code>
     * @throws NamingException when creation of the context fails
     */
    public static Context lookupContext(String initialContextFactory, 
            String providerUrl, boolean useAuth, String securityPrincipal, String securityCredentials) throws NamingException {
        String cacheKey = createKey(Thread.currentThread().getId(),initialContextFactory ,providerUrl, securityPrincipal, securityCredentials);
        Context ctx = MAP.get(cacheKey);
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
            try {
                ctx = new InitialContext(props);
            } catch (NoClassDefFoundError | Exception e){
                throw new NamingException(e.toString());
            }
            // we want to return the context that is actually in the map
            // if it's the first put we will have a null result
            Context oldCtx = MAP.putIfAbsent(cacheKey, ctx);
            if(oldCtx != null) {
                // There was an object in map, destroy the temporary and return one in map (oldCtx)
                try {
                    ctx.close();
                } catch (Exception e) {
                    // NOOP
                }
                ctx = oldCtx;
            }
            // else No object in Map, ctx is the one
        }
        return ctx;
    }

    /**
     * Create cache key
     * @param threadId Thread Id
     * @param initialContextFactory
     * @param providerUrl
     * @param securityPrincipal
     * @param securityCredentials
     * @return the cache key
     */
    private static String createKey(
            long threadId,
            String initialContextFactory,
            String providerUrl, String securityPrincipal,
            String securityCredentials) {
       StringBuilder builder = new StringBuilder();
       builder.append(threadId);
       builder.append("#");
       builder.append(initialContextFactory);
       builder.append("#");
       builder.append(providerUrl);
       builder.append("#");
       if(!StringUtils.isEmpty(securityPrincipal)) {
           builder.append(securityPrincipal);
           builder.append("#");
       }
       if(!StringUtils.isEmpty(securityCredentials)) {
           builder.append(securityCredentials);
       }
       return builder.toString();
    }

    /**
     * Initialize the JNDI initial context
     *
     * @param useProps
     *            if true, create a new InitialContext; otherwise use the other
     *            parameters to call
     *            {@link #lookupContext(String, String, boolean, String, String)}
     * @param initialContextFactory
     *            name of the initial context factory (ignored if
     *            <code>useProps</code> is <code>true</code>)
     * @param providerUrl
     *            url of the provider to use (ignored if <code>useProps</code>
     *            is <code>true</code>)
     * @param useAuth
     *            <code>true</code> if auth should be used, <code>false</code>
     *            otherwise (ignored if <code>useProps</code> is
     *            <code>true</code>)
     * @param securityPrincipal
     *            name of the principal to (ignored if <code>useProps</code> is
     *            <code>true</code>)
     * @param securityCredentials
     *            credentials for the principal (ignored if
     *            <code>useProps</code> is <code>true</code>)
     * @return the context, never <code>null</code>
     * @throws NamingException
     *             when creation of the context fails
     */
    public static Context getContext(boolean useProps, 
            String initialContextFactory, String providerUrl, 
            boolean useAuth, String securityPrincipal, String securityCredentials) throws NamingException {
        if (useProps) {
            try {
                return new InitialContext();
            } catch (NoClassDefFoundError | Exception e){
                throw new NamingException(e.toString());
            }
        } else {
            return lookupContext(initialContextFactory, providerUrl, useAuth, securityPrincipal, securityCredentials);
        }
    }
    
    /**
     * clear all the InitialContext objects.
     */
    public static void close() {
        for (Context ctx : MAP.values()) {
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
