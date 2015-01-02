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
// import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
// import javax.naming.directory.SearchResult;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Ldap Client class is main class to create, modify, search and delete all the
 * LDAP functionality available.
 *
 */
public class LdapClient {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private DirContext dirContext = null;

    /**
     * Constructor for the LdapClient object.
     */
    public LdapClient() {
    }

    /**
     * Connect to server.
     * 
     * @param host
     *            name of the ldap server
     * @param port
     *            port of the ldap server
     * @param rootdn
     *            base dn to start ldap operations from
     * @param username
     *            user name to use for binding
     * @param password
     *            password to use for binding
     * @throws NamingException
     *             if {@link InitialDirContext} can not be build using the above
     *             parameters
     */
    public void connect(String host, String port, String rootdn, String username, String password)
            throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); //$NON-NLS-1$
        env.put(Context.PROVIDER_URL, "ldap://" + host + ":" + port + "/" + rootdn); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
        env.put(Context.REFERRAL, "throw"); //$NON-NLS-1$
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_PRINCIPAL, username);
        dirContext = new InitialDirContext(env);
    }

    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        try {
            if (dirContext != null) {
                dirContext.close();
                dirContext = null;
            }
        } catch (NamingException e) {
            log.error("Ldap client - ", e);
        }
    }

    /**
     * Filter the data in the ldap directory for the given search base.
     *
     * @param searchBase
     *            where the search should start
     * @param searchFilter
     *            filter this value from the base
     * @return <code>true</code> when the search yields results,
     *         <code>false</code> otherwise
     * @throws NamingException
     *             when searching fails
     */
    public boolean searchTest(String searchBase, String searchFilter) throws NamingException {
        // System.out.println("Base="+searchBase+" Filter="+searchFilter);
        SearchControls searchcontrols = new SearchControls(SearchControls.SUBTREE_SCOPE,
                1L, // count limit
                0, // time limit
                null,// attributes (null = all)
                false,// return object ?
                false);// dereference links?
        NamingEnumeration<?> ne = dirContext.search(searchBase, searchFilter, searchcontrols);
        // System.out.println("Loop "+ne.toString()+" "+ne.hasMore());
        // while (ne.hasMore()){
        // Object tmp = ne.next();
        // System.out.println(tmp.getClass().getName());
        // SearchResult sr = (SearchResult) tmp;
        // Attributes at = sr.getAttributes();
        // System.out.println(at.get("cn"));
        // }
        // System.out.println("Done "+ne.hasMore());
        return ne.hasMore();
    }

    /**
     * Modify the attribute in the ldap directory for the given string.
     *
     * @param mods
     *            list of all {@link ModificationItem}s to apply
     * @param string
     *            dn of the object to modify
     * @throws NamingException when modification fails
     */
    public void modifyTest(ModificationItem[] mods, String string) throws NamingException {
        dirContext.modifyAttributes(string, mods);
    }

    /**
     * Create the attribute in the ldap directory for the given string.
     *
     * @param basicattributes
     *            add all the entry in to the basicattribute
     * @param string
     *            the string (dn) value
     * @throws NamingException when creating subcontext fails
     */
    public void createTest(BasicAttributes basicattributes, String string) throws NamingException {
        // DirContext dc = //TODO perhaps return this?
        dirContext.createSubcontext(string, basicattributes);
    }

    /**
     * Delete the attribute from the ldap directory.
     *
     * @param string
     *            the string (dn) value
     * @throws NamingException when destroying sub context fails
     */
    public void deleteTest(String string) throws NamingException {
        dirContext.destroySubcontext(string);
    }
}
