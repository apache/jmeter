/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.ldap.config;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.ldap.sampler.LDAPSampler;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    T.Elanjchezhiyan(chezhiyan@siptech.co.in)
 *@created   Apr 29 2003 11:45 AM
 *@company   Sip Technologies and Exports Ltd.
 *@version   1.0
 ***************************************/

/****************************************
 * This is model class for the LdapConfigGui
 * this will hold all the LDAP config value
 ***************************************/
public class LdapConfig extends ConfigTestElement implements Serializable
{

    /****************************************
     * Constructor for the LdapConfig object
     ***************************************/

    public LdapConfig() {
    }

    /************************************************************
     *  Sets the Rootdn attribute of the LdapConfig object
     *
     *@param  rootdn  The new rootdn value
     ***********************************************************/
    public void setRootdn(String newRootdn)	{
        this.setProperty(LDAPSampler.ROOTDN,newRootdn);
    }
	
    /************************************************************
     *  Gets the Rootdn attribute of the LdapConfig object
     *
     *@return    The Rootdn value
     ***********************************************************/
    public String getRootdn() {
        return getPropertyAsString(LDAPSampler.ROOTDN);
    }

    /************************************************************
     *  Sets the Test attribute of the LdapConfig object
     *
     *@param  Test  The new test value(Add,Modify,Delete  and search)
     ***********************************************************/
    public void setTest(String newTest) {
        this.setProperty(LDAPSampler.TEST,newTest);
    }

    /************************************************************
     *  Gets the test attribute of the LdapConfig object
     *
     *@return    The test value (Add,Modify,Delete  and search)
     ***********************************************************/
    public String getTest()	{
        return getPropertyAsString(LDAPSampler.TEST);
    }

    /************************************************************
     *  Sets the UserDefinedTest attribute of the LdapConfig object
     *
     *@param  value  The  new UserDefinedTest value 
     ***********************************************************/
    public void setUserDefinedTest(boolean value) {
        setProperty(new BooleanProperty(LDAPSampler.USER_DEFINED, value));
    }

    /************************************************************
     *  Gets the UserDefinedTest attribute of the LdapConfig object
     *
     *@return    The test value true or false
     *           if true it will do the UserDefinedTest else our own  
     *           inbuilt test case
     ***********************************************************/
    public boolean getUserDefinedTest() {
        return getPropertyAsBoolean(LDAPSampler.USER_DEFINED);
    }


    /************************************************************
     *  Sets the Arguments attribute of the LdapConfig object
     *  This will collect values from the table for user defined test
     *  case 
     *@param  value  The  arguments 
     ***********************************************************/
    public void setArguments(Arguments value)
    {
        setProperty(new TestElementProperty(LDAPSampler.ARGUMENTS, value));
    }

    /************************************************************
     *  Gets the Arguments attribute of the LdapConfig object
     *
     *@return    The  arguments
     *           user defined test  case
     ***********************************************************/
    public Arguments getArguments()
    {
        return (Arguments) getProperty(LDAPSampler.ARGUMENTS).getObjectValue();
    }
	
    /**
     * Returns a formatted string label describing this sampler
     * Example output:
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel() {
        return ("ldap://" + "this.getServername()" + "/" + this.getRootdn());
    }
}
