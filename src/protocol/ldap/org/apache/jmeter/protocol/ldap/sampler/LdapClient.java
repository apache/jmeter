/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.protocol.ldap.sampler;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
//import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
//import javax.naming.directory.SearchResult;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


/**
 * Ldap Client class is main class to create, modify, search and delete all the
 * LDAP functionality available.
 * 
 * @author    T.Elanjchezhiyan(chezhiyan@siptech.co.in) - Sip Technologies and
 *            Exports Ltd.
 * Created     Apr 29 2003 11:00 AM
 * @version   $Revision$ Last updated: $Date$
 */
public class LdapClient
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    private DirContext dirContext = null;

    /**
     *  Constructor for the LdapClient object.
     */
    public LdapClient()
    {
    }

    /**
     * Connect to server.
     */
    public void connect(
        String host,
        String port,
        String rootdn,
        String username,
        String password)
        throws NamingException
    {
        Hashtable env = new Hashtable();
        env.put(
            Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL,"ldap://"+host +":"+port+"/"+rootdn);
        env.put(Context.REFERRAL,"throw");
        env.put(Context.SECURITY_CREDENTIALS,password);
        env.put(Context.SECURITY_PRINCIPAL,username);
        dirContext = new InitialDirContext(env);
    }

    /**
     * Disconnect from the server.
     */
    public void disconnect()
    {
        try
        {
        	if (dirContext != null){
				dirContext.close();
				dirContext=null;
        	}
        }
        catch (NamingException e)
        {
            log.error("Ldap client - ",e);
        }
    }

    /**
     * Filter  the data in the ldap directory for the given search base.
     *  
     * @param  searchBase   where the search should start
     * @param  searchFilter filter this value from the base  
     */
    public boolean searchTest(String searchBase, String searchFilter)
        throws NamingException
    {
    	//System.out.println("Base="+searchBase+" Filter="+searchFilter);
        SearchControls searchcontrols =
            new SearchControls(SearchControls.SUBTREE_SCOPE,
					            1L, //count limit
					            0,  //time limit
					            null,//attributes (null = all)
					            false,// return object ?
					            false);// dereference links?
        NamingEnumeration ne = 
        dirContext.search(searchBase, searchFilter, searchcontrols);
		//System.out.println("Loop "+ne.toString()+" "+ne.hasMore());
//		while (ne.hasMore()){
//			Object tmp = ne.next();
//			System.out.println(tmp.getClass().getName());
//			SearchResult sr = (SearchResult) tmp;
//			Attributes at = sr.getAttributes();
//			System.out.println(at.get("cn"));
//		}
		//System.out.println("Done "+ne.hasMore());
        return ne.hasMore();
    }

    /**
     * Modify the attribute in the ldap directory for the given string.
     * 
     * @param mods    add all the entry in to the ModificationItem
     * @param string  the  string (dn) value 
     */
    public void modifyTest(ModificationItem[] mods, String string)
        throws NamingException
    {
        dirContext.modifyAttributes(string, mods);
    }

    /**
     * Create the attribute in the ldap directory for the given string.
     * 
     * @param  basicattributes  add all the entry in to the basicattribute
     * @param  string           the  string (dn) value 
     */
    public void createTest(BasicAttributes basicattributes, String string)
        throws NamingException
    {
    	//DirContext dc = //TODO perhaps return this?
        dirContext.createSubcontext(string, basicattributes);
    }
        
    /**
     * Delete the attribute from the ldap directory.
     * 
     * @param  string  the string (dn) value 
     */
    public void deleteTest(String string)
        throws NamingException
    {
        dirContext.destroySubcontext(string);
    }
}