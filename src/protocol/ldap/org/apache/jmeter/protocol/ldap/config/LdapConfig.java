// $Header$
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
import org.apache.jmeter.protocol.ldap.sampler.LDAPSampler;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * This is model class for the LdapConfigGui.  This will hold all the LDAP
 * config value.
 * 
 * @author    T.Elanjchezhiyan(chezhiyan@siptech.co.in) - Sip Technologies and
 *            Exports Ltd.
 * Created     Apr 29 2003 11:45 AM
 * @version   $Revision$ Last Updated: $Date$
 */
public class LdapConfig extends ConfigTestElement implements Serializable
{
    /**
     * Constructor for the LdapConfig object.
     */
    public LdapConfig()
    {
    }

    /**
     * Sets the Rootdn attribute of the LdapConfig object.
     *
     * @param  newRootdn  the new rootdn value
     */
    public void setRootdn(String newRootdn)
    {
        this.setProperty(LDAPSampler.ROOTDN,newRootdn);
    }

    /**
     * Gets the Rootdn attribute of the LdapConfig object.
     *
     * @return    the Rootdn value
     */
    public String getRootdn()
    {
        return getPropertyAsString(LDAPSampler.ROOTDN);
    }

    /**
     * Sets the Test attribute of the LdapConfig object.
     *
     * @param  newTest  the new test value(Add,Modify,Delete  and search)
     */
    public void setTest(String newTest)
    {
        this.setProperty(LDAPSampler.TEST,newTest);
    }

    /**
     * Gets the test attribute of the LdapConfig object.
     *
     * @return    the test value (Add,Modify,Delete  and search)
     */
    public String getTest()
    {
        return getPropertyAsString(LDAPSampler.TEST);
    }

    /**
     * Sets the UserDefinedTest attribute of the LdapConfig object.
     *
     * @param  value  the new UserDefinedTest value 
     */
    public void setUserDefinedTest(boolean value)
    {
        setProperty(new BooleanProperty(LDAPSampler.USER_DEFINED, value));
    }

    /**
     * Gets the UserDefinedTest attribute of the LdapConfig object.
     *
     * @return    the test value true or false.  If true it will do the
     *            UserDefinedTest else our own inbuilt test case.
     */
    public boolean getUserDefinedTest()
    {
        return getPropertyAsBoolean(LDAPSampler.USER_DEFINED);
    }


    /**
     *  Sets the Arguments attribute of the LdapConfig object.
     *  This will collect values from the table for user defined test
     *  case.
     * 
     * @param  value  the arguments 
     */
    public void setArguments(Arguments value)
    {
        setProperty(new TestElementProperty(LDAPSampler.ARGUMENTS, value));
    }

    /**
     * Gets the Arguments attribute of the LdapConfig object.
     *
     * @return    the arguments.  User defined test case.
     */
    public Arguments getArguments()
    {
        return (Arguments) getProperty(LDAPSampler.ARGUMENTS).getObjectValue();
    }

    /**
     * Returns a formatted string label describing this sampler.
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel()
    {
        return ("ldap://" + "this.getServername()" + "/" + this.getRootdn());
    }
}
