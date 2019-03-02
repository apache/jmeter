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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ldap Sampler class is main class for the LDAP test. This will control all the
 * test available in the LDAP Test.
 *
 */
public class LDAPSampler extends AbstractSampler {
    private static final Logger log = LoggerFactory.getLogger(LDAPSampler.class);

    private static final long serialVersionUID = 240L;

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList(
                    "org.apache.jmeter.config.gui.LoginConfigGui",
                    "org.apache.jmeter.protocol.ldap.config.gui.LdapConfigGui",
                    "org.apache.jmeter.config.gui.SimpleConfigGui"
            ));

    public static final String SERVERNAME = "servername"; //$NON-NLS-1$

    public static final String PORT = "port"; //$NON-NLS-1$

    public static final String ROOTDN = "rootdn"; //$NON-NLS-1$

    public static final String TEST = "test"; //$NON-NLS-1$

    public static final String ADD = "add"; //$NON-NLS-1$

    public static final String MODIFY = "modify"; //$NON-NLS-1$

    public static final String DELETE = "delete"; //$NON-NLS-1$

    public static final String SEARCHBASE = "search"; //$NON-NLS-1$

    public static final String SEARCHFILTER = "searchfilter"; //$NON-NLS-1$

    public static final String USER_DEFINED = "user_defined"; //$NON-NLS-1$

    public static final String ARGUMENTS = "arguments"; //$NON-NLS-1$

    public static final String BASE_ENTRY_DN = "base_entry_dn"; //$NON-NLS-1$

    // For In build test case using this counter
    // create the new entry in the server
    private static AtomicInteger COUNTER = new AtomicInteger(0);

    private boolean searchFoundEntries;// TODO turn into parameter?

    public LDAPSampler() {
    }

    /**
     * Gets the username attribute of the LDAP object.
     *
     * @return the username
     */
    public String getUsername() {
        return getPropertyAsString(ConfigTestElement.USERNAME);
    }

    /**
     * Gets the password attribute of the LDAP object.
     *
     * @return the password
     */
    public String getPassword() {
        return getPropertyAsString(ConfigTestElement.PASSWORD);
    }

    /**
     * Sets the Servername attribute of the ServerConfig object.
     *
     * @param servername
     *            the new servername value
     */
    public void setServername(String servername) {
        setProperty(new StringProperty(SERVERNAME, servername));
    }

    /**
     * Sets the Port attribute of the ServerConfig object.
     *
     * @param port
     *            the new Port value
     */
    public void setPort(String port) {
        setProperty(new StringProperty(PORT, port));
    }

    /**
     * Gets the servername attribute of the LDAPSampler object.
     *
     * @return the Servername value
     */
    public String getServername() {
        return getPropertyAsString(SERVERNAME);
    }

    /**
     * Gets the Port attribute of the LDAPSampler object.
     *
     * @return the Port value
     */
    public String getPort() {
        return getPropertyAsString(PORT);
    }

    /**
     * Sets the Rootdn attribute of the LDAPSampler object.
     *
     * @param newRootdn
     *            the new rootdn value
     */
    public void setRootdn(String newRootdn) {
        this.setProperty(ROOTDN, newRootdn);
    }

    /**
     * Gets the Rootdn attribute of the LDAPSampler object.
     *
     * @return the Rootdn value
     */
    public String getRootdn() {
        return getPropertyAsString(ROOTDN);
    }

    /**
     * Sets the Test attribute of the LdapConfig object.
     *
     * @param newTest
     *            the new test value(Add,Modify,Delete and search)
     */
    public void setTest(String newTest) {
        this.setProperty(TEST, newTest);
    }

    /**
     * Gets the test attribute of the LDAPSampler object.
     *
     * @return the test value (Add, Modify, Delete and search)
     */
    public String getTest() {
        return getPropertyAsString(TEST);
    }

    /**
     * Sets the UserDefinedTest attribute of the LDAPSampler object.
     *
     * @param value
     *            the new UserDefinedTest value
     */
    public void setUserDefinedTest(boolean value) {
        setProperty(new BooleanProperty(USER_DEFINED, value));
    }

    /**
     * Gets the UserDefinedTest attribute of the LDAPSampler object.
     *
     * @return the test value true or false. If true it will do the
     *         UserDefinedTest else our own inbuild test case.
     */
    public boolean getUserDefinedTest() {
        return getPropertyAsBoolean(USER_DEFINED);
    }

    /**
     * Sets the Base Entry DN attribute of the LDAPSampler object.
     *
     * @param newbaseentry
     *            the new Base entry DN value
     */
    public void setBaseEntryDN(String newbaseentry) {
        setProperty(new StringProperty(BASE_ENTRY_DN, newbaseentry));
    }

    /**
     * Gets the BaseEntryDN attribute of the LDAPSampler object.
     *
     * @return the Base entry DN value
     */
    public String getBaseEntryDN() {
        return getPropertyAsString(BASE_ENTRY_DN);
    }

    /**
     * Sets the Arguments attribute of the LdapConfig object. This will collect
     * values from the table for user defined test case.
     *
     * @param value
     *            the arguments
     */
    public void setArguments(Arguments value) {
        setProperty(new TestElementProperty(ARGUMENTS, value));
    }

    /**
     * Gets the Arguments attribute of the LdapConfig object.
     *
     * @return the arguments. User defined test case.
     */
    public Arguments getArguments() {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }

    /**
     * Collect all the value from the table (Arguments), using this create the
     * basicAttributes. This will create the Basic Attributes for the User
     * defined TestCase for Add Test.
     *
     * @return the BasicAttributes
     */
    private BasicAttributes getUserAttributes() {
        BasicAttribute basicattribute = new BasicAttribute("objectclass"); //$NON-NLS-1$
        basicattribute.add("top"); //$NON-NLS-1$
        basicattribute.add("person"); //$NON-NLS-1$
        basicattribute.add("organizationalPerson"); //$NON-NLS-1$
        basicattribute.add("inetOrgPerson"); //$NON-NLS-1$
        BasicAttributes attrs = new BasicAttributes(true);
        attrs.put(basicattribute);
        BasicAttribute attr;

        for (JMeterProperty jMeterProperty : getArguments()) {
            Argument item = (Argument) jMeterProperty.getObjectValue();
            attr = getBasicAttribute(item.getName(), item.getValue());
            attrs.put(attr);
        }
        return attrs;
    }

    /**
     * Collect all the value from the table (Arguments), using this create the
     * basicAttributes. This will create the Basic Attributes for the User
     * defined TestCase for Modify test.
     *
     * @return the BasicAttributes
     */
    private ModificationItem[] getUserModAttributes() {
        ModificationItem[] mods = new ModificationItem[getArguments().getArguments().size()];
        BasicAttribute attr;
        PropertyIterator iter = getArguments().iterator();
        int count = 0;
        while (iter.hasNext()) {
            Argument item = (Argument) iter.next().getObjectValue();
            attr = getBasicAttribute(item.getName(), item.getValue());
            mods[count] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
            count = +1;
        }
        return mods;
    }

    /**
     * This will create the Basic Attributes for the Inbuilt TestCase for Modify
     * test.
     *
     * @return the BasicAttributes
     */
    private ModificationItem[] getModificationItem() {
        ModificationItem[] mods = new ModificationItem[2];
        // replace (update) attribute
        Attribute mod0 = new BasicAttribute("userpassword", "secret"); //$NON-NLS-1$ //$NON-NLS-2$
        // add mobile phone number attribute
        Attribute mod1 = new BasicAttribute("mobile", "123-456-1234"); //$NON-NLS-1$ //$NON-NLS-2$

        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
        mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, mod1);

        return mods;
    }

    /**
     * This will create the Basic Attributes for the In build TestCase for Add
     * Test.
     *
     * @return the BasicAttributes
     */
    private BasicAttributes getBasicAttributes() {
        BasicAttributes basicattributes = new BasicAttributes();
        BasicAttribute basicattribute = new BasicAttribute("objectclass"); //$NON-NLS-1$
        basicattribute.add("top"); //$NON-NLS-1$
        basicattribute.add("person"); //$NON-NLS-1$
        basicattribute.add("organizationalPerson"); //$NON-NLS-1$
        basicattribute.add("inetOrgPerson"); //$NON-NLS-1$
        basicattributes.put(basicattribute);
        String s1 = "User"; //$NON-NLS-1$
        String s3 = "Test"; //$NON-NLS-1$
        String s5 = "user"; //$NON-NLS-1$
        String s6 = "test"; //$NON-NLS-1$
        COUNTER.incrementAndGet();
        basicattributes.put(new BasicAttribute("givenname", s1)); //$NON-NLS-1$
        basicattributes.put(new BasicAttribute("sn", s3)); //$NON-NLS-1$
        basicattributes.put(new BasicAttribute("cn", "TestUser" + COUNTER.get())); //$NON-NLS-1$ //$NON-NLS-2$
        basicattributes.put(new BasicAttribute("uid", s5)); //$NON-NLS-1$
        basicattributes.put(new BasicAttribute("userpassword", s6)); //$NON-NLS-1$
        setProperty(new StringProperty(ADD, "cn=TestUser" + COUNTER.get())); //$NON-NLS-1$
        return basicattributes;
    }

    /**
     * This will create the Basic Attribute for the given name value pair.
     *
     * @return the BasicAttribute
     */
    private BasicAttribute getBasicAttribute(String name, String value) {
        BasicAttribute attr = new BasicAttribute(name, value);
        return attr;
    }

    /**
     * Returns a formatted string label describing this sampler
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel() {
        return "ldap://" + this.getServername() + ":" + getPort() + "/" + this.getRootdn();
    }

    /**
     * This will do the add test for the User defined TestCase as well as
     * inbuilt test case.
     *
     */
    private void addTest(LdapClient ldap, SampleResult res) throws NamingException {
        if (getPropertyAsBoolean(USER_DEFINED)) {
            res.sampleStart();
            ldap.createTest(getUserAttributes(), getPropertyAsString(BASE_ENTRY_DN));
            res.sampleEnd();
        } else {
            res.sampleStart();
            ldap.createTest(getBasicAttributes(), getPropertyAsString(ADD));
            res.sampleEnd();
            ldap.deleteTest(getPropertyAsString(ADD));
        }
    }

    /**
     * This will do the delete test for the User defined TestCase as well as
     * inbuilt test case.
     *
     */
    private void deleteTest(LdapClient ldap, SampleResult res) throws NamingException {
        if (!getPropertyAsBoolean(USER_DEFINED)) {
            ldap.createTest(getBasicAttributes(), getPropertyAsString(ADD));
            setProperty(new StringProperty(DELETE, getPropertyAsString(ADD)));
        }
        res.sampleStart();
        ldap.deleteTest(getPropertyAsString(DELETE));
        res.sampleEnd();
    }

    /**
     * This will do the search test for the User defined TestCase as well as
     * inbuilt test case.
     *
     */
    private void searchTest(LdapClient ldap, SampleResult res) throws NamingException {
        if (!getPropertyAsBoolean(USER_DEFINED)) {
            ldap.createTest(getBasicAttributes(), getPropertyAsString(ADD));
            setProperty(new StringProperty(SEARCHBASE, getPropertyAsString(ADD)));
            setProperty(new StringProperty(SEARCHFILTER, getPropertyAsString(ADD)));
        }
        res.sampleStart();
        searchFoundEntries = ldap.searchTest(getPropertyAsString(SEARCHBASE), getPropertyAsString(SEARCHFILTER));
        res.sampleEnd();
        if (!getPropertyAsBoolean(USER_DEFINED)) {
            ldap.deleteTest(getPropertyAsString(ADD));
        }
    }

    /**
     * This will do the search test for the User defined TestCase as well as
     * inbuilt test case.
     *
     */
    private void modifyTest(LdapClient ldap, SampleResult res) throws NamingException {
        if (getPropertyAsBoolean(USER_DEFINED)) {
            res.sampleStart();
            ldap.modifyTest(getUserModAttributes(), getPropertyAsString(BASE_ENTRY_DN));
            res.sampleEnd();
        } else {
            ldap.createTest(getBasicAttributes(), getPropertyAsString(ADD));
            setProperty(new StringProperty(MODIFY, getPropertyAsString(ADD)));
            res.sampleStart();
            ldap.modifyTest(getModificationItem(), getPropertyAsString(MODIFY));
            res.sampleEnd();
            ldap.deleteTest(getPropertyAsString(ADD));
        }
    }

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        boolean isSuccessful = false;
        res.setSampleLabel(getName());
        res.setSamplerData(getPropertyAsString(TEST));// TODO improve this
        LdapClient ldap = new LdapClient();

        try {
            ldap.connect(getServername(), getPort(), getRootdn(), getUsername(), getPassword());

            if (getPropertyAsString(TEST).equals(ADD)) {
                addTest(ldap, res);
            } else if (getPropertyAsString(TEST).equals(DELETE)) {
                deleteTest(ldap, res);
            } else if (getPropertyAsString(TEST).equals(MODIFY)) {
                modifyTest(ldap, res);
            } else if (getPropertyAsString(TEST).equals(SEARCHBASE)) {
                searchTest(ldap, res);
            }

            // TODO - needs more work ...
            if (getPropertyAsString(TEST).equals(SEARCHBASE) && !searchFoundEntries) {
                res.setResponseCode("201");// TODO is this a sensible number? //$NON-NLS-1$
                res.setResponseMessage("OK - no results");
                res.setResponseData("successful - no results", null);
            } else {
                res.setResponseCodeOK();
                res.setResponseMessage("OK"); //$NON-NLS-1$
                res.setResponseData("successful", null);
            }
            res.setDataType(SampleResult.TEXT);
            isSuccessful = true;
        } catch (Exception ex) {
            log.error("Ldap client - ", ex);
            res.setResponseCode("500");// TODO distinguish errors better //$NON-NLS-1$
            res.setResponseMessage(ex.toString());
            isSuccessful = false;
        } finally {
            ldap.disconnect();
        }

        // Set if we were successful or not
        res.setSuccessful(isSuccessful);
        return res;
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }
}
