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
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.ldap.config.gui.LDAPArgument;
import org.apache.jmeter.protocol.ldap.config.gui.LDAPArguments;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.XMLBuffer;
import org.apache.log.Logger;

/*******************************************************************************
 * Ldap Sampler class is main class for the LDAP test. This will control all the
 * test available in the LDAP Test.
 ******************************************************************************/

public class LDAPExtSampler extends AbstractSampler implements TestListener {

	private static final Logger log = LoggingManager.getLoggerForClass();

	/*
	 * The following strings are used in the test plan, and the values must not be changed
	 * if test plans are to be upwardly compatible. 
	 */
	public final static String SERVERNAME = "servername"; // $NON-NLS-1$

	public final static String PORT = "port"; // $NON-NLS-1$
	
	public final static String SECURE = "secure"; // $NON-NLS-1$

	public final static String ROOTDN = "rootdn"; // $NON-NLS-1$

	public final static String TEST = "test"; // $NON-NLS-1$

	// These are values for the TEST attribute above
	public final static String ADD = "add"; // $NON-NLS-1$

	public final static String MODIFY = "modify"; // $NON-NLS-1$

	public final static String BIND = "bind"; // $NON-NLS-1$

	public final static String UNBIND = "unbind"; // $NON-NLS-1$

	public final static String DELETE = "delete"; // $NON-NLS-1$

	public final static String SEARCH = "search"; // $NON-NLS-1$
    // end of TEST values
	
	public final static String SEARCHBASE = "search"; // $NON-NLS-1$

	public final static String SEARCHFILTER = "searchfilter"; // $NON-NLS-1$

	public final static String ARGUMENTS = "arguments"; // $NON-NLS-1$

	public final static String LDAPARGUMENTS = "ldaparguments"; // $NON-NLS-1$

	public final static String BASE_ENTRY_DN = "base_entry_dn"; // $NON-NLS-1$

	public final static String SCOPE = "scope"; // $NON-NLS-1$

	public final static String COUNTLIM = "countlimit"; // $NON-NLS-1$

	public final static String TIMELIM = "timelimit"; // $NON-NLS-1$

	public final static String ATTRIBS = "attributes"; // $NON-NLS-1$

	public final static String RETOBJ = "return_object"; // $NON-NLS-1$

	public final static String DEREF = "deref_aliases"; // $NON-NLS-1$

	public final static String USERDN = "user_dn"; // $NON-NLS-1$

	public final static String USERPW = "user_pw"; // $NON-NLS-1$

	public final static String SBIND = "sbind"; // $NON-NLS-1$

	public final static String COMPARE = "compare"; // $NON-NLS-1$
	
	public final static String CONNTO = "connection_timeout"; // $NON-NLS-1$

	public final static String COMPAREDN = "comparedn"; // $NON-NLS-1$

	public final static String COMPAREFILT = "comparefilt"; // $NON-NLS-1$

	public final static String PARSEFLAG = "parseflag"; // $NON-NLS-1$

	public final static String RENAME = "rename"; // $NON-NLS-1$

	public final static String MODDDN = "modddn"; // $NON-NLS-1$

	public final static String NEWDN = "newdn"; // $NON-NLS-1$

    private static final String SEMI_COLON = ";"; // $NON-NLS-1$


    private static Hashtable ldapConnections = new Hashtable();

	private static Hashtable ldapContexts = new Hashtable();

	/***************************************************************************
	 * !ToDo (Constructor description)
	 **************************************************************************/
	public LDAPExtSampler() {
	}

    public void setConnTimeOut(String connto) {
        setProperty(new StringProperty(CONNTO, connto));
    }

    public String getConnTimeOut() {
        return getPropertyAsString(CONNTO);
    }
    
    public void setSecure(String sec) {
        setProperty(new StringProperty(SECURE, sec));
    }

    public boolean isSecure() {
        return getPropertyAsBoolean(SECURE);
    }


    public boolean isParseFlag() {
        return getPropertyAsBoolean(PARSEFLAG);
    }

    public void setParseFlag(String parseFlag) {
        setProperty(new StringProperty(PARSEFLAG, parseFlag));
    }

	/***************************************************************************
	 * Gets the username attribute of the LDAP object
	 * 
	 * @return The username
	 **************************************************************************/

	public String getUserDN() {
		return getPropertyAsString(USERDN);
	}

	/***************************************************************************
	 * Sets the username attribute of the LDAP object
	 * 
	 * @return The username
	 **************************************************************************/

	public void setUserDN(String newUserDN) {
		setProperty(new StringProperty(USERDN, newUserDN));
	}

	/***************************************************************************
	 * Gets the password attribute of the LDAP object
	 * 
	 * @return The password
	 **************************************************************************/

	public String getUserPw() {
		return getPropertyAsString(USERPW);
	}

	/***************************************************************************
	 * Sets the password attribute of the LDAP object
	 * 
	 **************************************************************************/

	public void setUserPw(String newUserPw) {
		setProperty(new StringProperty(USERPW, newUserPw));
	}

	/***************************************************************************
	 * Sets the Servername attribute of the ServerConfig object
	 * 
	 * @param servername
	 *            The new servername value
	 **************************************************************************/
	public void setServername(String servername) {
		setProperty(new StringProperty(SERVERNAME, servername));
	}

	/***************************************************************************
	 * Sets the Port attribute of the ServerConfig object
	 * 
	 * @param port
	 *            The new Port value
	 **************************************************************************/
	public void setPort(String port) {
		setProperty(new StringProperty(PORT, port));
	}

	/***************************************************************************
	 * Gets the servername attribute of the LDAPSampler object
	 * 
	 * @return The Servername value
	 **************************************************************************/

	public String getServername() {
		return getPropertyAsString(SERVERNAME);
	}

	/***************************************************************************
	 * Gets the Port attribute of the LDAPSampler object
	 * 
	 * @return The Port value
	 **************************************************************************/

	public String getPort() {
		return getPropertyAsString(PORT);
	}

	/***************************************************************************
	 * Sets the Rootdn attribute of the LDAPSampler object
	 * 
	 * @param rootdn
	 *            The new rootdn value
	 **************************************************************************/
	public void setRootdn(String newRootdn) {
		this.setProperty(ROOTDN, newRootdn);
	}

	/***************************************************************************
	 * Gets the Rootdn attribute of the LDAPSampler object
	 * 
	 * @return The Rootdn value
	 **************************************************************************/
	public String getRootdn() {
		return getPropertyAsString(ROOTDN);
	}

	/***************************************************************************
	 * Gets the search scope attribute of the LDAPSampler object
	 * 
	 * @return The scope value
	 **************************************************************************/
	public String getScope() {
		return getPropertyAsString(SCOPE);
	}

	public int getScopeAsInt() {
		return getPropertyAsInt(SCOPE);
	}

	/***************************************************************************
	 * Sets the search scope attribute of the LDAPSampler object
	 * 
	 * @param rootdn
	 *            The new scope value
	 **************************************************************************/
	public void setScope(String newScope) {
		this.setProperty(SCOPE, newScope);
	}

	/***************************************************************************
	 * Gets the size limit attribute of the LDAPSampler object
	 * 
	 * @return The size limit
	 **************************************************************************/
	public String getCountlim() {
		return getPropertyAsString(COUNTLIM);
	}

	public long getCountlimAsLong() {
		return getPropertyAsLong(COUNTLIM);
	}

	/***************************************************************************
	 * Sets the size limit attribute of the LDAPSampler object
	 * 
	 * @param rootdn
	 *            The new scope value
	 **************************************************************************/
	public void setCountlim(String newClim) {
		this.setProperty(COUNTLIM, newClim);
	}

	/***************************************************************************
	 * Gets the time limit attribute of the LDAPSampler object
	 * 
	 * @return The time limit
	 **************************************************************************/
	public String getTimelim() {
		return getPropertyAsString(TIMELIM);
	}

	public int getTimelimAsInt() {
		return getPropertyAsInt(TIMELIM);
	}

	/***************************************************************************
	 * Sets the time limit attribute of the LDAPSampler object
	 * 
	 * @param rootdn
	 *            The new scope value
	 **************************************************************************/
	public void setTimelim(String newTlim) {
		this.setProperty(TIMELIM, newTlim);
	}

	/***************************************************************************
	 * Gets the return objects attribute of the LDAPSampler object
	 * 
	 * @return if the object(s) are to be returned
	 **************************************************************************/
	public boolean isRetobj() {
		return getPropertyAsBoolean(RETOBJ);
	}

	/***************************************************************************
	 * Sets the return objects attribute of the LDAPSampler object
	 * 
	 **************************************************************************/
	public void setRetobj(String newRobj) {
		this.setProperty(RETOBJ, newRobj);
	}

	/***************************************************************************
	 * Gets the deref attribute of the LDAPSampler object
	 * 
	 * @return if dereferencing is required
	 **************************************************************************/
	public boolean isDeref() {
		return getPropertyAsBoolean(DEREF);
	}

	/***************************************************************************
	 * Sets the search scope attribute of the LDAPSampler object
	 * 
	 * @param rootdn
	 *            The new scope value
	 **************************************************************************/
	public void setDeref(String newDref) {
		this.setProperty(DEREF, newDref);
	}

	/***************************************************************************
	 * Sets the Test attribute of the LdapConfig object
	 * 
	 * @param Test
	 *            The new test value(Add,Modify,Delete and search)
	 **************************************************************************/
	public void setTest(String newTest) {
		this.setProperty(TEST, newTest);
	}

	/***************************************************************************
	 * Gets the test attribute of the LDAPSampler object
	 * 
	 * @return The test value (Add,Modify,Delete and search)
	 **************************************************************************/
	public String getTest() {
		return getPropertyAsString(TEST);
	}

	/***************************************************************************
	 * Sets the Test attribute of the LdapConfig object
	 * 
	 * @param Test
	 *            The new test value(Add,Modify,Delete and search)
	 **************************************************************************/
	public void setAttrs(String newAttrs) {
		this.setProperty(ATTRIBS, newAttrs);
	}

	/***************************************************************************
	 * Gets the attributes of the LDAPSampler object
	 * 
	 * @return The attributes
	 **************************************************************************/
	public String getAttrs() {
		return getPropertyAsString(ATTRIBS);
	}

	/***************************************************************************
	 * Sets the Base Entry DN attribute of the LDAPSampler object
	 * 
	 * @param value
	 *            The new Base entry DN value
	 **************************************************************************/
	public void setBaseEntryDN(String newbaseentry) {
		setProperty(new StringProperty(BASE_ENTRY_DN, newbaseentry));
	}

	/***************************************************************************
	 * Gets the BaseEntryDN attribute of the LDAPSampler object
	 * 
	 * @return The Base entry DN value
	 **************************************************************************/
	public String getBaseEntryDN() {
		return getPropertyAsString(BASE_ENTRY_DN);
	}

	/***************************************************************************
	 * Sets the Arguments attribute of the LdapConfig object This will collect
	 * values from the table for user defined test case
	 * 
	 * @param value
	 *            The arguments
	 **************************************************************************/
	public void setArguments(Arguments value) {
		setProperty(new TestElementProperty(ARGUMENTS, value));
	}

	/***************************************************************************
	 * Gets the Arguments attribute of the LdapConfig object
	 * 
	 * @return The arguments user defined test case
	 **************************************************************************/
	public Arguments getArguments() {
		return (Arguments) getProperty(ARGUMENTS).getObjectValue();
	}

	/***************************************************************************
	 * Sets the Arguments attribute of the LdapConfig object This will collect
	 * values from the table for user defined test case
	 * 
	 * @param value
	 *            The arguments
	 **************************************************************************/
	public void setLDAPArguments(LDAPArguments value) {
		setProperty(new TestElementProperty(LDAPARGUMENTS, value));
	}

	/***************************************************************************
	 * Gets the LDAPArguments attribute of the LdapConfig object
	 * 
	 * @return The LDAParguments user defined modify test case
	 **************************************************************************/
	public LDAPArguments getLDAPArguments() {
		return (LDAPArguments) getProperty(LDAPARGUMENTS).getObjectValue();
	}

	/***************************************************************************
	 * Collect all the values from the table (Arguments), using this create the
	 * Attributes, this will create the Attributes for the User
	 * defined TestCase for Add Test
	 * 
	 * @return The Attributes
	 **************************************************************************/
	private Attributes getUserAttributes() {
        Attributes attrs = new BasicAttributes(true);
		Attribute attr;
		PropertyIterator iter = getArguments().iterator();

		while (iter.hasNext()) {
			Argument item = (Argument) iter.next().getObjectValue();
			attr = attrs.get(item.getName());
			if (attr == null) {
				attr = getBasicAttribute(item.getName(), item.getValue());
			} else {
				attr.add(item.getValue());
			}
			attrs.put(attr);
		}
		return attrs;
	}

	/***************************************************************************
	 * Collect all the value from the table (Arguments), using this create the
	 * basicAttributes This will create the Basic Attributes for the User
	 * defined TestCase for Modify test
	 * 
	 * @return The BasicAttributes
	 **************************************************************************/
	private ModificationItem[] getUserModAttributes() {
		ModificationItem[] mods = new ModificationItem[getLDAPArguments().getArguments().size()];
		BasicAttribute attr;
		PropertyIterator iter = getLDAPArguments().iterator();
		int count = 0;
		while (iter.hasNext()) {
			LDAPArgument item = (LDAPArgument) iter.next().getObjectValue();
			if ((item.getValue()).length()==0) {
				attr = new BasicAttribute(item.getName());
			} else {
				attr = getBasicAttribute(item.getName(), item.getValue());
			}
			
			final String opcode = item.getOpcode();
			if ("add".equals(opcode)) { // $NON-NLS-1$
				mods[count++] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);
			} else if ("delete".equals(opcode) // $NON-NLS-1$
				   ||  "remove".equals(opcode)) { // $NON-NLS-1$
					mods[count++] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attr);
			} else if("replace".equals(opcode)) { // $NON-NLS-1$
					mods[count++] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
			} else {
					log.warn("Invalid opCode: "+opcode);
			}
		}
		return mods;
	}

	/***************************************************************************
	 * Collect all the value from the table (Arguments), using this create the
	 * Attributes This will create the Basic Attributes for the User defined
	 * TestCase for search test
	 * 
	 * @return The BasicAttributes
	 **************************************************************************/
	private String[] getRequestAttributes(String reqAttr) {
		int index;
		String[] mods;
		int count = 0;
		if (reqAttr.length() == 0) {
			return null;
		}
		if (!reqAttr.endsWith(SEMI_COLON)) {
			reqAttr = reqAttr + SEMI_COLON; 
		}
		String attr = reqAttr;

		while (attr.length() > 0) {
			index = attr.indexOf(SEMI_COLON);
			count += 1;
			attr = attr.substring(index + 1);
		}
		if (count > 0) {
			mods = new String[count];
			attr = reqAttr;
			count = 0;
			while (attr.length() > 0) {
				index = attr.indexOf(SEMI_COLON);
				mods[count] = attr.substring(0, index);
				count += 1;
				attr = attr.substring(index + 1);
			}
		} else {
			mods = null;
		}
		return mods;
	}

	/***************************************************************************
	 * This will create the Basic Attribute for the give name value pair
	 * 
	 * @return The BasicAttribute
	 **************************************************************************/
	private BasicAttribute getBasicAttribute(String name, String value) {
		BasicAttribute attr = new BasicAttribute(name, value);
		return attr;
	}

	/**
	 * Returns a formatted string label describing this sampler Example output:
	 * 
	 * @return a formatted string label describing this sampler
	 */
	public String getLabel() {
		return ("ldap://" + this.getServername() + ":" + getPort() + "/" + this.getRootdn());
	}

	/***************************************************************************
	 * This will do the add test for the User defined TestCase
	 * 
	 **************************************************************************/
	private void addTest(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws NamingException {
		try {
			res.sampleStart();
			ldap.createTest(dirContext, getUserAttributes(), getBaseEntryDN());
		} finally {
			res.sampleEnd();
		}		
	}

	/***************************************************************************
	 * This will do the delete test for the User defined TestCase
	 * 
	 **************************************************************************/
	private void deleteTest(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws NamingException {
		try {
			res.sampleStart();
			ldap.deleteTest(dirContext, getPropertyAsString(DELETE));
		} finally {
			res.sampleEnd();
		}		
	}

	/***************************************************************************
	 * This will do the modify test for the User defined TestCase
	 * 
	 **************************************************************************/
	private void modifyTest(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws NamingException {
		try {
			res.sampleStart();
			ldap.modifyTest(dirContext, getUserModAttributes(), getBaseEntryDN());
		} finally {
			res.sampleEnd();
		}		
	}

	/***************************************************************************
	 * This will do the bind for the User defined Thread, this bind is used for
	 * the whole context
	 * 
	 **************************************************************************/
	private void bindOp(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws NamingException {
		DirContext ctx = (DirContext) ldapContexts.remove(getThreadName());
		if (ctx != null) {
			log.warn("Closing previous context for thread: " + getThreadName());
			ctx.close();
		}
		try {
			res.sampleStart();
			ctx = ldap.connect(getServername(), getPort(), getRootdn(), getUserDN(), getUserPw(),getConnTimeOut(),isSecure());
		} finally {
			res.sampleEnd();
		}		
		ldapContexts.put(getThreadName(), ctx);
	}

	/***************************************************************************
	 * This will do the bind and unbind for the User defined TestCase
	 * 
	 **************************************************************************/
	private void singleBindOp(SampleResult res) throws NamingException {
		LdapExtClient ldap_temp;
		ldap_temp = new LdapExtClient();
		try {
			res.sampleStart();
			DirContext ctx = ldap_temp.connect(getServername(), getPort(), getRootdn(), getUserDN(), getUserPw(),getConnTimeOut(),isSecure());
			ldap_temp.disconnect(ctx);
		} finally {
			res.sampleEnd();
		}		
	}

	/***************************************************************************
	 * This will do a moddn Opp for the User new DN defined
	 * 
	 **************************************************************************/
	private void renameTest(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws NamingException {
		try {
			res.sampleStart();
			ldap.moddnOp(dirContext, getPropertyAsString(MODDDN), getPropertyAsString(NEWDN));
		} finally {
			res.sampleEnd();
		}		
	}

	/***************************************************************************
	 * This will do the unbind for the User defined TestCase as well as inbuilt
	 * test case
	 * 
	 **************************************************************************/
	private void unbindOp(LdapExtClient ldap, DirContext dirContext, SampleResult res) {
		try {
			res.sampleStart();
			ldap.disconnect(dirContext);
		} finally {
			res.sampleEnd();
		}		
		ldapConnections.remove(getThreadName());
		ldapContexts.remove(getThreadName());
		log.info("context and LdapExtClients removed");
	}

	/***************************************************************************
	 * !ToDo (Method description)
	 * 
	 * @param e
	 *            !ToDo (Parameter description)
	 * @return !ToDo (Return description)
	 **************************************************************************/
	public SampleResult sample(Entry e) {
		XMLBuffer xmlBuffer = new XMLBuffer();
		xmlBuffer.openTag("ldapanswer"); // $NON-NLS-1$
		SampleResult res = new SampleResult();
		res.setResponseData("successfull".getBytes());
		res.setResponseMessage("Success"); // $NON-NLS-1$
		res.setResponseCode("0"); // $NON-NLS-1$
		boolean isSuccessful = true;
		res.setSampleLabel(getName());
		LdapExtClient temp_client = (LdapExtClient) ldapConnections.get(getThreadName());
		DirContext dirContext = (DirContext) ldapContexts.get(getThreadName());
		if (temp_client == null) {
			temp_client = new LdapExtClient();
			try {
				dirContext = new InitialDirContext();
			} catch (NamingException err) {
				log.error("Ldap client context creation - ", err);
			}
			ldapConnections.put(getThreadName(), temp_client);
		}

		try {
			xmlBuffer.openTag("operation"); // $NON-NLS-1$
			final String testType = getTest();
			xmlBuffer.tag("opertype", testType); // $NON-NLS-1$
			log.debug("performing test: " + testType);
			if (testType.equals(UNBIND)) {
				res.setSamplerData("Unbind");
				xmlBuffer.tag("baseobj",getRootdn()); // $NON-NLS-1$
				xmlBuffer.tag("binddn",getUserDN()); // $NON-NLS-1$
				unbindOp(temp_client, dirContext, res);
			} else if (testType.equals(BIND)) {
				res.setSamplerData("Bind as "+getUserDN());
				xmlBuffer.tag("baseobj",getRootdn()); // $NON-NLS-1$
				xmlBuffer.tag("binddn",getUserDN()); // $NON-NLS-1$
				xmlBuffer.tag("connectionTO",getConnTimeOut()); // $NON-NLS-1$
				bindOp(temp_client, dirContext, res);
			} else if (testType.equals(SBIND)) {
				res.setSamplerData("SingleBind as "+getUserDN());
				xmlBuffer.tag("baseobj",getRootdn()); // $NON-NLS-1$
				xmlBuffer.tag("binddn",getUserDN()); // $NON-NLS-1$
				xmlBuffer.tag("connectionTO",getConnTimeOut()); // $NON-NLS-1$
				singleBindOp(res);
			} else if (testType.equals(COMPARE)) {
				res.setSamplerData("Compare "+getPropertyAsString(COMPAREFILT) + " "
								+ getPropertyAsString(COMPAREDN));
				xmlBuffer.tag("comparedn",getPropertyAsString(COMPAREDN)); // $NON-NLS-1$
				xmlBuffer.tag("comparefilter",getPropertyAsString(COMPAREFILT)); // $NON-NLS-1$
                NamingEnumeration cmp;
				try {
					res.sampleStart();
					cmp = temp_client.compare(dirContext, getPropertyAsString(COMPAREFILT),
							getPropertyAsString(COMPAREDN));
				} finally {
					res.sampleEnd();
				}				
				if (cmp.hasMore()) {
				} else {
					res.setResponseCode("5"); // $NON-NLS-1$
					res.setResponseMessage("compareFalse");
					isSuccessful = false;
				}
			} else if (testType.equals(ADD)) {
				res.setSamplerData("Add object " + getBaseEntryDN());
				xmlBuffer.tag("attributes",getArguments().toString()); // $NON-NLS-1$
				xmlBuffer.tag("dn",getBaseEntryDN()); // $NON-NLS-1$
				addTest(temp_client, dirContext, res);
			} else if (testType.equals(DELETE)) {
				res.setSamplerData("Delete object " + getBaseEntryDN());
				xmlBuffer.tag("dn",getBaseEntryDN()); // $NON-NLS-1$
				deleteTest(temp_client, dirContext, res);
			} else if (testType.equals(MODIFY)) {
				res.setSamplerData("Modify object " + getBaseEntryDN());
				xmlBuffer.tag("dn",getBaseEntryDN()); // $NON-NLS-1$
				xmlBuffer.tag("attributes",getLDAPArguments().toString()); // $NON-NLS-1$
				modifyTest(temp_client, dirContext, res);
			} else if (testType.equals(RENAME)) {
				res.setSamplerData("ModDN object " + getPropertyAsString(MODDDN) + " to " + getPropertyAsString(NEWDN));
				xmlBuffer.tag("dn",getPropertyAsString(MODDDN)); // $NON-NLS-1$
				xmlBuffer.tag("newdn",getPropertyAsString(NEWDN)); // $NON-NLS-1$
				renameTest(temp_client, dirContext, res);
			} else if (testType.equals(SEARCH)) {
                final String            scopeStr = getScope();
                final int               scope = getScopeAsInt();
                final String searchFilter = getPropertyAsString(SEARCHFILTER);
				final String searchBase = getPropertyAsString(SEARCHBASE);
				final String timeLimit = getTimelim();
				final String countLimit = getCountlim();

				res.setSamplerData("Search with filter " + searchFilter);
				xmlBuffer.tag("searchfilter",searchFilter); // $NON-NLS-1$
				xmlBuffer.tag("baseobj",getRootdn()); // $NON-NLS-1$
				xmlBuffer.tag("searchbase",searchBase);// $NON-NLS-1$
				xmlBuffer.tag("scope" , scopeStr); // $NON-NLS-1$
				xmlBuffer.tag("countlimit",countLimit); // $NON-NLS-1$
				xmlBuffer.tag("timelimit",timeLimit); // $NON-NLS-1$

                NamingEnumeration srch;
				try {
					res.sampleStart();
					srch = temp_client.searchTest(
							dirContext, searchBase, searchFilter,
							scope, getCountlimAsLong(),
							getTimelimAsInt(),
							getRequestAttributes(getAttrs()),
							isRetobj(),
							isDeref());
				} finally {
					res.sampleEnd();
				}				

                if (isParseFlag()) {
					try {
						xmlBuffer.openTag("searchresults"); // $NON-NLS-1$
						while (srch.hasMore()) {
							try {
								xmlBuffer.openTag("searchresult"); // $NON-NLS-1$
								SearchResult sr = (SearchResult) srch.next();
								xmlBuffer.tag("dn",sr.getName());// $NON-NLS-1$
								xmlBuffer.tag("returnedattr",String.valueOf(sr.getAttributes().size())); // $NON-NLS-1$
								NamingEnumeration attrlist = sr.getAttributes().getIDs();
								while (attrlist.hasMore()) {
									String iets = (String) attrlist.next();
									xmlBuffer.openTag("attribute"); // $NON-NLS-1$
									xmlBuffer.tag("attributename", iets); // $NON-NLS-1$
									xmlBuffer.tag("attributevalue", // $NON-NLS-1$
											sr.getAttributes().get(iets).toString().substring(iets.length() + 2));
									xmlBuffer.closeTag("attribute"); // $NON-NLS-1$
								}
							} finally {
								xmlBuffer.closeTag("searchresult"); // $NON-NLS-1$
							}							
						}
					} finally {
						xmlBuffer.closeTag("searchresults"); // $NON-NLS-1$
					}					
                }
			}

		} catch (NamingException ex) {
			// TODO: tidy this up
			String returnData = ex.toString();
			final int indexOfLDAPErrCode = returnData.indexOf("LDAP: error code");
			if (indexOfLDAPErrCode >= 0) {
				res.setResponseMessage(returnData.substring(indexOfLDAPErrCode + 21, returnData
						.indexOf("]"))); // $NON-NLS-1$
				res.setResponseCode(returnData.substring(indexOfLDAPErrCode + 17, indexOfLDAPErrCode + 19));
			} else {
				res.setResponseMessage(returnData);
				res.setResponseCode("800"); // $NON-NLS-1$
			}
			isSuccessful = false;
		} finally {
			xmlBuffer.closeTag("operation"); // $NON-NLS-1$
			xmlBuffer.tag("responsecode",res.getResponseCode()); // $NON-NLS-1$
			xmlBuffer.tag("responsemessage",res.getResponseMessage()); // $NON-NLS-1$
			res.setResponseData(xmlBuffer.toString().getBytes());
			res.setDataType(SampleResult.TEXT);
			res.setSuccessful(isSuccessful);
		}
		return res;
	}

	public void testStarted() {
		testStarted(""); // $NON-NLS-1$
	}

	public void testEnded() {
		testEnded(""); // $NON-NLS-1$
	}

	public void testStarted(String host) {
		// ignored
	}

	// Ensure any remaining contexts are closed
	public void testEnded(String host) {
		Iterator it = ldapContexts.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			DirContext dc = (DirContext) entry.getValue();
			try {
				log.warn("Tidying old Context for thread: " + key);
				dc.close();
			} catch (NamingException ignored) {
				// ignored
			}
			it.remove();// Make sure the entry is not left around for the next
						// run
		}

	}

	public void testIterationStart(LoopIterationEvent event) {
		// ignored
	}
}
