//$Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.ldap.config;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.ldap.sampler.LDAPExtSampler;
import org.apache.jmeter.protocol.ldap.config.gui.LDAPArguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

/****************************************
 *
 *author    Dolf Smits(Dolf.Smits@Siemens.com)
 *created    Aug 09 2003 11:00 AM
 *company    Siemens Netherlands N.V..
 *
 * Based on the work of:
 *@author    T.Elanjchezhiyan(chezhiyan@siptech.co.in)
 *created    Apr 29 2003 11:00 AM
 *company    Sip Technologies and Exports Ltd.
 *
 ***************************************/

/****************************************
 * This is model class for the LdapConfigGui
 * this will hold all the LDAP config value
 ***************************************/
public class LdapExtConfig extends ConfigTestElement implements Serializable
{

     
   /****************************************
     * Constructor for the LdapConfig object
     ***************************************/

    public LdapExtConfig() {
    	 
    }

    /************************************************************
     *  Sets the Rootdn attribute of the LdapConfig object
     *
     *@param  rootdn  The new rootdn value
     ***********************************************************/
    public void setRootdn(String newRootdn)	{
        this.setProperty(LDAPExtSampler.ROOTDN,newRootdn);
    }
	
    /************************************************************
     *  Gets the Rootdn attribute of the LdapConfig object
     *
     *@return    The Rootdn value
     ***********************************************************/
    public String getRootdn() {
        return getPropertyAsString(LDAPExtSampler.ROOTDN);
    }

    /************************************************************
     *  Sets the Test attribute of the LdapConfig object
     *
     *@param  Test  The new test value(Add,Modify,Delete  and search)
     ***********************************************************/
    public void setTest(String newTest) {
        this.setProperty(LDAPExtSampler.TEST,newTest);
    }

    /************************************************************
     *  Gets the test attribute of the LdapConfig object
     *
     *@return    The test value (Add,Modify,Delete  and search)
     ***********************************************************/
    public String getTest()	{
        return getPropertyAsString(LDAPExtSampler.TEST);
    }


 
    /************************************************************
     *  Sets the Arguments attribute of the LdapConfig object
     *  This will collect values from the table for user defined test
     *  case 
     *@param  value  The  arguments 
     ***********************************************************/
    public void setArguments(Arguments value)
    {
        setProperty(new TestElementProperty(LDAPExtSampler.ARGUMENTS, value));
    }

    /************************************************************
     *  Gets the Arguments attribute of the LdapConfig object
     *
     *@return    The  arguments
     *           user defined test  case
     ***********************************************************/
    public Arguments getArguments()
    {
        return (Arguments) getProperty(LDAPExtSampler.ARGUMENTS).getObjectValue();
    }
	
    /************************************************************
     *  Sets the Arguments attribute of the LdapConfig object
     *  This will collect values from the table for user defined test
     *  case 
     *@param  value  The  arguments 
     ***********************************************************/
    public void setLDAPArguments(LDAPArguments value)
    {
        setProperty(new TestElementProperty(LDAPExtSampler.LDAPARGUMENTS, value));
    }

    /************************************************************
     *  Gets the Arguments attribute of the LdapConfig object
     *
     *@return    The  arguments
     *           user defined test  case
     ***********************************************************/
    public LDAPArguments getLDAPArguments()
    {
        return (LDAPArguments) getProperty(LDAPExtSampler.LDAPARGUMENTS).getObjectValue();
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
