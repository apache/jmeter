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

package org.apache.jmeter.protocol.ldap.sampler;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.jmeter.util.TrustAllSSLSocketFactory;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ldap Client class is main class to create ,modify, search and delete all the
 * LDAP functionality available
 * Based on the work of: author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
 *
 */
public class LdapExtClient {
    private static final Logger log = LoggerFactory.getLogger(LdapExtClient.class);

    private static final String CONTEXT_IS_NULL = "Context is null";

    /**
     * Constructor for the LdapClient object
     */
    private LdapExtClient() {
        super();
    }

    /**
     * connect to server
     *
     * @param host
     *            name of the server to connect
     * @param port
     *            port of the server to connect
     * @param rootdn
     *            base of the tree to operate on
     * @param username
     *            name of the user to use for binding
     * @param password
     *            password to use for binding
     * @param connTimeOut
     *            connection timeout for connecting the server see
     *            "com.sun.jndi.ldap.connect.timeout"
     * @param secure
     *            flag whether ssl should be used
     * @param trustAll flag whether we should trust all certificates 
     * @return newly created {@link DirContext}
     * @exception NamingException
     *                when creating the {@link DirContext} fails
     */
    public static DirContext connect(String host,
            String port, 
            String rootdn, 
            String username,
            String password, 
            String connTimeOut, 
            boolean secure, 
            boolean trustAll)
            throws NamingException {
        DirContext dirContext;
        Hashtable<String, String> env = new Hashtable<>(); // NOSONAR : Env needs to be HashTable
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); // $NON-NLS-1$
        StringBuilder sb = new StringBuilder(80);
        if (secure) {
            sb.append("ldaps://"); // $NON-NLS-1$
            if (trustAll){
                log.debug("Using secure connection with trustAll");
                env.put("java.naming.ldap.factory.socket", TrustAllSSLSocketFactory.class.getName());
            }
        } else {
            sb.append("ldap://"); // $NON-NLS-1$
        }
        sb.append(host);
        if (port.length()>0){
            sb.append(":"); // $NON-NLS-1$
            sb.append(port);
        }
        sb.append("/"); // $NON-NLS-1$
        sb.append(rootdn);
        env.put(Context.PROVIDER_URL,sb.toString());
        if(log.isInfoEnabled()) {
            log.info("prov_url= {}", env.get(Context.PROVIDER_URL)); // $NON-NLS-1$
        }
        if (connTimeOut.length()> 0) {
            env.put("com.sun.jndi.ldap.connect.timeout", connTimeOut); // $NON-NLS-1$
        }
        env.put(Context.REFERRAL, "throw"); // $NON-NLS-1$
        env.put("java.naming.batchsize", "0"); // $NON-NLS-1$  // $NON-NLS-2$
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_PRINCIPAL, username);
        dirContext = new InitialDirContext(env);
        return dirContext;
    }

    /**
     * disconnect from the server
     *
     * @param dirContext
     *            context do disconnect (may be <code>null</code>)
     */
    public static void disconnect(DirContext dirContext) {
        if (dirContext == null) {
            log.info("Cannot disconnect null context");
            return;
        }

        try {
            dirContext.close();
        } catch (NamingException e) {
            log.warn("Ldap client disconnect - ", e);
        }
    }

    /**
     * Filter the data in the ldap directory for the given search base
     * 
     * @param dirContext
     *            context to perform the search on
     *
     * @param searchBase
     *            base where the search should start
     * @param searchFilter
     *            filter this value from the base
     * @param scope
     *            scope for search. May be one of
     *            {@link SearchControls#OBJECT_SCOPE},
     *            {@link SearchControls#ONELEVEL_SCOPE} or
     *            {@link SearchControls#SUBTREE_SCOPE}
     * @param countlim
     *            max number of results to get, <code>0</code> for all entries
     * @param timelim
     *            max time to wait for entries (in milliseconds), <code>0</code>
     *            for unlimited time
     * @param attrs
     *            list of attributes to return. If <code>null</code> all
     *            attributes will be returned. If empty, none will be returned
     * @param retobj
     *            flag whether the objects should be returned
     * @param deref
     *            flag whether objects should be dereferenced
     * @return result of the search
     * @throws NamingException
     *             when searching fails
     **/
    public static NamingEnumeration<SearchResult> searchTest(  
            DirContext dirContext, 
            String searchBase, 
            String searchFilter, 
            int scope, 
            long countlim,
            int timelim, 
            String[] attrs, 
            boolean retobj, 
            boolean deref) throws NamingException {
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        if (log.isDebugEnabled()){
            log.debug(
                    "searchBase={}, scope={}, countlim={}, timelim={}, attrs={}, retobj={}, deref={}, filter={}",
                    searchBase,scope, countlim,timelim, attrs != null ? JOrphanUtils.unsplit(attrs,","): null,retobj,deref,searchFilter);
        }
        SearchControls searchcontrols = new SearchControls(scope, countlim,
                timelim, attrs, retobj, deref);
        return dirContext.search(searchBase, searchFilter, searchcontrols);
    }

    /***************************************************************************
     * Filter the data in the ldap directory
     *
     * @param dirContext
     *            the context to operate on
     * @param filter
     *            filter this value from the base
     * @param entrydn
     *            distinguished name of entry to compare
     * @return result of the search
     * @throws NamingException
     *             when searching fails
     **************************************************************************/
    public static NamingEnumeration<SearchResult> compare(DirContext dirContext, String filter, String entrydn) throws NamingException {
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        SearchControls searchcontrols = new SearchControls(0, 1, 0, new String[0], false, false);
        return dirContext.search(entrydn, filter, searchcontrols);
    }

    /***************************************************************************
     * ModDN the data in the ldap directory for the given search base
     *
     * @param dirContext
     *            context to operate on
     * @param ddn
     *            distinguished name of object to rename
     * @param newdn
     *            new distinguished name of object
     * @throws NamingException
     *             when renaming fails
     *
     **************************************************************************/
    public static void moddnOp(DirContext dirContext, String ddn, String newdn) throws NamingException {
        log.debug("ddn and newDn= {}@@@@{}", ddn , newdn);
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        dirContext.rename(ddn, newdn);
    }

    /***************************************************************************
     * Modify the attribute in the ldap directory for the given string
     *
     * @param dirContext
     *            context to operate on
     * @param mods
     *            list of all the {@link ModificationItem}s to apply on
     *            <code>string</code>
     * @param string
     *            distinguished name of the object to modify
     * @throws NamingException
     *             when modification fails
     **************************************************************************/
    public static void modifyTest(DirContext dirContext, ModificationItem[] mods, String string) throws NamingException {
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        dirContext.modifyAttributes(string, mods);

    }

    /***************************************************************************
     * Create the entry in the ldap directory for the given string
     *
     * @param dirContext
     *            context to operate on
     * @param attributes
     *            add all the attributes and values from the attributes object
     * @param string
     *            distinguished name of the subcontext to create
     * @return newly created subcontext
     * @throws NamingException
     *             when creating subcontext fails
     **************************************************************************/
    public static DirContext createTest(DirContext dirContext, Attributes attributes, String string)
            throws NamingException {
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        return dirContext.createSubcontext(string, attributes);
    }

    /**
     * Delete the attribute from the ldap directory
     *
     * @param dirContext
     *            context to operate on
     * @param string
     *            distinguished name of the subcontext to destroy
     * @throws NamingException
     *             when destroying the subcontext fails
     */
    public static void deleteTest(DirContext dirContext, String string) throws NamingException {
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        dirContext.destroySubcontext(string);
    }
}
