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
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/*******************************************************************************
 *
 * author Dolf Smits(Dolf.Smits@Siemens.com) created Aug 09 2003 11:00 AM
 * company Siemens Netherlands N.V..
 *
 * Based on the work of: author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
 * created Apr 29 2003 11:00 AM company Sip Technologies and Exports Ltd.
 *
 ******************************************************************************/

/*******************************************************************************
 * Ldap Client class is main class to create ,modify, search and delete all the
 * LDAP functionality available
 ******************************************************************************/
public class LdapExtClient {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String CONTEXT_IS_NULL = "Context is null";

    /**
     * Constructor for the LdapClient object
     */
    public LdapExtClient() {
    }

    /**
     * connect to server
     *
     * @param host
     *            Description of Parameter
     * @param username
     *            Description of Parameter
     * @param password
     *            Description of Parameter
     * @exception NamingException
     *                Description of Exception
     */
    public static DirContext connect(String host, String port, String rootdn, String username, String password, String connTimeOut, boolean secure)
            throws NamingException {
        DirContext dirContext;
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); // $NON-NLS-1$
        StringBuilder sb = new StringBuilder(80);
        if (secure) {
            sb.append("ldaps://"); // $NON-NLS-1$
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
        log.info("prov_url= " + env.get(Context.PROVIDER_URL)); // $NON-NLS-1$
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

    /***************************************************************************
     * Filter the data in the ldap directory for the given search base
     *
     * @param searchBase
     *            base where the search should start
     * @param searchFilter
     *            filter filter this value from the base
     **************************************************************************/
    public static NamingEnumeration<SearchResult> searchTest(DirContext dirContext, String searchBase, String searchFilter, int scope, long countlim,
            int timelim, String[] attrs, boolean retobj, boolean deref) throws NamingException {
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        if (log.isDebugEnabled()){
            log.debug(
                    "searchBase=" + searchBase +
                    " scope=" + scope +
                    " countlim=" + countlim +
                    " timelim=" + timelim +
                    " attrs=" + JMeterUtils.unsplit(attrs,",") +
                    " retobj=" + retobj +
                    " deref=" + deref +
                    " filter=" + searchFilter
                      );
        }
        SearchControls searchcontrols = null;
        searchcontrols = new SearchControls(scope, countlim, timelim, attrs, retobj, deref);
        return dirContext.search(searchBase, searchFilter, searchcontrols);
    }

    /***************************************************************************
     * Filter the data in the ldap directory
     *
     * @param filter
     *            filter this value from the base
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
     **************************************************************************/
    public static void moddnOp(DirContext dirContext, String ddn, String newdn) throws NamingException {
        log.debug("ddn and newDn= " + ddn + "@@@@" + newdn);
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        dirContext.rename(ddn, newdn);
    }

    /***************************************************************************
     * Modify the attribute in the ldap directory for the given string
     *
     * @param mods
     *            add all the entry in to the ModificationItem
     * @param string
     *            The string (dn) value
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
     * @param attributes
     *            add all the attributes and values from the attributes object
     * @param string
     *            The string (dn) value
     **************************************************************************/
    public static DirContext createTest(DirContext dirContext, Attributes attributes, String string)
            throws NamingException {
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        return dirContext.createSubcontext(string, attributes);
    }

    /***************************************************************************
     * Delete the attribute from the ldap directory
     *
     * @param string
     *            The string (dn) value
     **************************************************************************/
    public static void deleteTest(DirContext dirContext, String string) throws NamingException {
        if (dirContext == null) {
            throw new NamingException(CONTEXT_IS_NULL);
        }
        dirContext.destroySubcontext(string);
    }
}
