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

package org.apache.jmeter.protocol.ldap.sampler;

import java.util.*;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.NamingEnumeration;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.LoginConfig;
import org.apache.jmeter.protocol.ldap.config.LdapExtConfig;
import org.apache.jmeter.protocol.ldap.config.gui.LDAPArgument;
import org.apache.jmeter.protocol.ldap.config.gui.LDAPArguments;
import org.apache.jmeter.protocol.ldap.sampler.LdapExtClient;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Dolf Smits(Dolf.Smits@Siemens.com)
 *@created    Aug 09 2003 11:00 AM
 *@company    Siemens Netherlands N.V..
 *@version   1.0
 * Based on the work of:
 *@author    T.Elanjchezhiyan(chezhiyan@siptech.co.in)
 *@created    Apr 29 2003 11:00 AM
 *@company    Sip Technologies and Exports Ltd.
 *@version   1.0
 ***************************************/

/*****************************************************
 * Ldap Sampler class is main class for the LDAP test.
 * This will control all the test available in the LDAP Test.
 *****************************************************/

public class LDAPExtSampler extends AbstractSampler  {

    transient private static Logger log = LoggingManager.getLoggerForClass();

    public final static String SERVERNAME = "servername";
    public final static String PORT = "port";
    public final static String ROOTDN = "rootdn";
    public final static String TEST = "test";
    public final static String ADD = "add";
    public final static String MODIFY = "modify";
    public final static String BIND = "bind";
    public final static String UNBIND = "unbind";
    public final static String DELETE = "delete";             
    public final static String SEARCHBASE = "search";
    public final static String SEARCHFILTER = "searchfilter";
    public final static String ARGUMENTS = "arguments";
    public final static String LDAPARGUMENTS = "ldaparguments";
    public final static String BASE_ENTRY_DN = "base_entry_dn";
    public final static String SCOPE = "scope";
    public final static String COUNTLIM = "countlimit";
    public final static String TIMELIM = "timelimit";
    public final static String ATTRIBS = "attributes";
    public final static String RETOBJ = "return_object";
    public final static String DEREF = "deref_aliases";
    public final static String USERDN = "user_dn";
    public final static String USERPW = "user_pw";
    public final static String SBIND = "sbind";
    public final static String COMPARE = "compare";
    
    public final static String SUSERDN = "suser_dn";
    public final static String SUSERPW = "suser_pw";
    public final static String COMPAREDN = "comparedn";
    public final static String COMPAREFILT = "comparefilt";

    public final static String RENAME = "rename";
    public final static String MODDDN = "modddn";
    public final static String NEWDN = "newdn";
    //For In build test case using this counter 
    //create the new entry in the server
    public static int counter=0;
    
    private DirContext dirContext;
    private  LdapExtClient temp_client ;
    public static Hashtable ldapConnections=new Hashtable();
    public static Hashtable ldapContexts=new Hashtable();
   
 
    /************************************************************
     *  !ToDo (Constructor description)
     ***********************************************************/
    public LDAPExtSampler()
    {
    }


    /************************************************************
     *  
     ***********************************************************/

    public void addCustomTestElement(TestElement element)
    {
        if(element instanceof LdapExtConfig || element instanceof LoginConfig)
        {
            mergeIn(element);
        }
    }

    /************************************************************
     *  Gets the username attribute of the LDAP object
     *
     *@return    The username
     *************************************************************/

    public String getUserDN()
    {
        return getPropertyAsString(USERDN);
    }
	
    /************************************************************
     *  Sets the username attribute of the LDAP object
     *
     *@return    The username
     *************************************************************/

    public void SetUserDN(String newUserDN)
    {
        setProperty(new StringProperty(USERDN, newUserDN));
    }
	
    /************************************************************
     *  Gets the password attribute of the LDAP object
     *
     *@return    The password
     *************************************************************/

    public String getUserPw()
    {
        return getPropertyAsString(USERPW);
    }

    /************************************************************
     *  Gets the password attribute of the LDAP object
     *
     *@return    The password
     *************************************************************/

    public void setUserPw(String newUserPw)
    {
        setProperty(new StringProperty(USERPW, newUserPw));
    }

    /************************************************************
     *  Gets the username attribute of the LDAP object
     *
     *@return    The username
     *************************************************************/

    public String getSuserDN()
    {
        return getPropertyAsString(SUSERDN);
    }
	
    /************************************************************
     *  Sets the username attribute of the LDAP object
     *
     *@return    The username
     *************************************************************/

    public void SetSuserDN(String newUserDN)
    {
        setProperty(new StringProperty(SUSERDN, newUserDN));
    }
	
    /************************************************************
     *  Gets the password attribute of the LDAP object
     *
     *@return    The password
     *************************************************************/

    public String getSuserPw()
    {
        return getPropertyAsString(SUSERPW);
    }

    /************************************************************
     *  Gets the password attribute of the LDAP object
     *
     *@return    The password
     *************************************************************/

    public void setSuserPw(String newUserPw)
    {
        setProperty(new StringProperty(SUSERPW, newUserPw));
    }

    /************************************************************
     *  Sets the Servername attribute of the ServerConfig object
     *
     *@param  servername  The new servername value
     ***********************************************************/
    public void setServername(String servername) {
        setProperty(new StringProperty(SERVERNAME, servername));
    }

    /************************************************************
     *  Sets the Port attribute of the ServerConfig object
     *
     *@param  port  The new Port value
     ***********************************************************/
    public void setPort(String port) {
        setProperty(new StringProperty(PORT, port));
    }


    /************************************************************
     *  Gets the servername attribute of the LDAPSampler object
     *
     *@return    The Servername value
     ***********************************************************/

    public String getServername()
    {
        return getPropertyAsString(SERVERNAME);
    }
	
    /************************************************************
     *  Gets the Port attribute of the LDAPSampler object
     *
     *@return    The Port value
     *************************************************************/

    public String getPort()
    {
        return getPropertyAsString(PORT);
    }
	
    /************************************************************
     *  Sets the Rootdn attribute of the LDAPSampler object
     *
     *@param  rootdn  The new rootdn value
     ***********************************************************/
    public void setRootdn(String newRootdn)
    {
        this.setProperty(ROOTDN,newRootdn);
    }
    /************************************************************
     *  Gets the Rootdn attribute of the LDAPSampler object
     *
     *@return    The Rootdn value
     ***********************************************************/
    public String getRootdn() {
        return getPropertyAsString(ROOTDN);
    }

    /************************************************************
     *  Gets the search scope attribute of the LDAPSampler object
     *
     *@return    The scope value
     ***********************************************************/
    public String getScope() {
        return getPropertyAsString(SCOPE);
    }

    /************************************************************
     *  Sets the search scope attribute of the LDAPSampler object
     *
     *@param  rootdn  The new scope value
     ***********************************************************/
    public void setScope(String newScope)
    {
        this.setProperty(SCOPE,newScope);
    }
     /************************************************************
     *  Gets the size limit attribute of the LDAPSampler object
     *
     *@return    The scope value
     ***********************************************************/
    public String getCountlim() {
        return getPropertyAsString(COUNTLIM);
    }

    /************************************************************
     *  Sets the size limit attribute of the LDAPSampler object
     *
     *@param  rootdn  The new scope value
     ***********************************************************/
    public void setCountlim(String newClim)
    {
        this.setProperty(COUNTLIM,newClim);
    }
    
     /************************************************************
     *  Gets the time limit attribute of the LDAPSampler object
     *
     *@return    The scope value
     ***********************************************************/
    public String getTimelim() {
        return getPropertyAsString(TIMELIM);
    }

    /************************************************************
     *  Sets the time limit attribute of the LDAPSampler object
     *
     *@param  rootdn  The new scope value
     ***********************************************************/
    public void setTimelim(String newTlim)
    {
        this.setProperty(TIMELIM,newTlim);
    }
    
     /************************************************************
     *  Gets the return objects attribute of the LDAPSampler object
     *
     *@return    The scope value
     ***********************************************************/
    public boolean getRetobj() {
        return getPropertyAsBoolean(RETOBJ);
    }

    /************************************************************
     *  Sets the return objects attribute of the LDAPSampler object
     *
     *@param  rootdn  The new scope value
     ***********************************************************/
    public void setRetobj(String newRobj)
    {
        this.setProperty(RETOBJ,newRobj);
    }
    
    /************************************************************
     *  Gets the search scope attribute of the LDAPSampler object
     *
     *@return    The scope value
     ***********************************************************/
    public boolean getDeref() {
        return getPropertyAsBoolean(DEREF);
    }

    /************************************************************
     *  Sets the search scope attribute of the LDAPSampler object
     *
     *@param  rootdn  The new scope value
     ***********************************************************/
    public void setDeref(String newDref)
    {
        this.setProperty(DEREF,newDref);
    }
    
  /************************************************************
     *  Sets the Test attribute of the LdapConfig object
     *
     *@param  Test  The new test value(Add,Modify,Delete  and search)
     ***********************************************************/
    public void setTest(String newTest) {
        this.setProperty(TEST,newTest);
    }
    
    /************************************************************
     *  Gets the test attribute of the LDAPSampler object
     *
     *@return    The test value (Add,Modify,Delete  and search)
     ***********************************************************/
    public String getTest()
    {
        return getPropertyAsString(TEST);
    }

   /************************************************************
     *  Sets the Test attribute of the LdapConfig object
     *
     *@param  Test  The new test value(Add,Modify,Delete  and search)
     ***********************************************************/
    public void setAttrs(String newAttrs) {
        this.setProperty(ATTRIBS,newAttrs);
    }
    
    /************************************************************
     *  Gets the test attribute of the LDAPSampler object
     *
     *@return    The test value (Add,Modify,Delete  and search)
     ***********************************************************/
    public String getAttrs()
    {
        return getPropertyAsString(ATTRIBS);
    }


    /************************************************************
     *  Sets the Base Entry DN attribute of the LDAPSampler object
     *
     *@param  value  The  new Base entry DN value 
     ***********************************************************/
    public void setBaseEntryDN(String  newbaseentry) {
        setProperty(new StringProperty(BASE_ENTRY_DN, newbaseentry));
    }

    /************************************************************
     *  Gets the BaseEntryDN attribute of the LDAPSampler object
     *
     *@return    The  Base entry DN value
     ***********************************************************/
    public String getBaseEntryDN() {
        return getPropertyAsString(BASE_ENTRY_DN);
    }

    /************************************************************
     *  Sets the Arguments attribute of the LdapConfig object
     *  This will collect values from the table for user defined test
     *  case 
     *@param  value  The  arguments 
     ***********************************************************/
    public void setArguments(Arguments value)
    {
        setProperty(new TestElementProperty(ARGUMENTS, value));
    }

    /************************************************************
     *  Gets the Arguments attribute of the LdapConfig object
     *
     *@return    The  arguments
     *           user defined test  case
     ***********************************************************/
    public Arguments getArguments()
    {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }
	
    /************************************************************
     *  Sets the Arguments attribute of the LdapConfig object
     *  This will collect values from the table for user defined test
     *  case 
     *@param  value  The  arguments 
     ***********************************************************/
    public void setLDAPArguments(LDAPArguments value)
    {
        setProperty(new TestElementProperty(LDAPARGUMENTS, value));
    }

   /************************************************************
     *  Gets the LDAPArguments attribute of the LdapConfig object
     *
     *@return    The  LDAParguments
     *           user defined modify test  case
     ***********************************************************/
    public LDAPArguments getLDAPArguments()
    {
        return (LDAPArguments) getProperty(LDAPARGUMENTS).getObjectValue();
    }
	

    /************************************************************
     * Collect all the value from the table (Arguments), using  this
     * create the basicAttributes 
     * This will create the Basic Attributes for the User defined 
     * TestCase  for Add Test
     *@return    The  BasicAttributes
     ***********************************************************/
    public BasicAttributes getUserAttributes() {
        BasicAttributes attrs = new BasicAttributes(true);
        Attribute attr;
        PropertyIterator iter = getArguments().iterator();
         
        while (iter.hasNext()) {
            Argument item = (Argument) iter.next().getObjectValue();
            attr=attrs.get(item.getName());
            if (attr == null ) {
                 attr = getBasicAttribute( item.getName(),item.getValue());
            } else {
            	attr.add(item.getValue());
            }
            attrs.put(attr);
        }
        return attrs;
    }


    /************************************************************
     * Collect all the value from the table (Arguments), using  this
     * create the basicAttributes 
     * This will create the Basic Attributes for the User defined 
     * TestCase for Modify test
     *@return    The  BasicAttributes
     ***********************************************************/
    public ModificationItem[] getUserModAttributes() {
        ModificationItem[] mods =new  ModificationItem[getLDAPArguments().getArguments().size()];
        boolean add =true;
        BasicAttributes attrs = new BasicAttributes(true);
        BasicAttribute attr;
        PropertyIterator iter = getLDAPArguments().iterator();
        int count =0;
        while (iter.hasNext()) {
            LDAPArgument item = (LDAPArgument) iter.next().getObjectValue();
            if ((item.getValue()).equals("")) {
                 attr = new BasicAttribute(item.getName());
            } else {
               attr = getBasicAttribute( item.getName(),item.getValue());
            }
            if ("add".equals(item.getOpcode())) {
            	mods[count ] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);
            } else {
               if ("delete".equals(item.getOpcode())) {
            	mods[count ] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attr);
               } else {
            	  mods[count ] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
               }
            }
            count+=1;
        }
        return mods;
    }

     /************************************************************
     * Collect all the value from the table (Arguments), using  this
     * create the Attributes 
     * This will create the Basic Attributes for the User defined 
     * TestCase for search test
     *@return    The  BasicAttributes
     ***********************************************************/
    public String[] getRequestAttributes(String reqAttr) {
    	int index;
    	String[] mods;
        int count =0;
        if (reqAttr.length()==0) {
        	return null;
        }
        if (!reqAttr.endsWith(";")) {
        	reqAttr=reqAttr+";";
        }
    	String attr=reqAttr;
    	
    	while (attr.length() >0) {
    		index=attr.indexOf(";");
    		count+=1;
    		attr=attr.substring(index+1);
    	}
    	if (count > 0) {
           mods =new  String[count];
           attr=reqAttr;
           count=0;
           while (attr.length() >0) {
    		   index=attr.indexOf(";");
     		   mods[count]=attr.substring(0,index);
   		       count+=1;
    		   attr=attr.substring(index+1);
    	   }
    	} else {
    		mods=null;
    	}
        return mods;
    }


 
    /************************************************************
     * This will create the Basic Attribute for the give name 
     * value pair
     *@return    The  BasicAttribute
     ***********************************************************/
    public BasicAttribute getBasicAttribute(String name,String value)
    {
        BasicAttribute attr = new BasicAttribute(name,value);
        return attr;
    }

    /**
     * Returns a formatted string label describing this sampler
     * Example output:
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel() {
        return ("ldap://" + this.getServername() +":"+getPort()+ "/" + this.getRootdn());
    }


    /************************************************************
     * This will do the add test  for the User defined 
     * TestCase
     *@return    executed time for the give test case
     ***********************************************************/
    public void addTest(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws Exception{
		res.sampleStart();
		ldap.createTest(dirContext, getUserAttributes(), getPropertyAsString(BASE_ENTRY_DN));
		res.sampleEnd();
    }
         

    /************************************************************
     * This will do the delete test  for the User defined 
     * TestCase
     *@return    executed time for the give test case
     ***********************************************************/
    public void deleteTest(LdapExtClient ldap, DirContext dirContext, SampleResult res)throws Exception {
        res.sampleStart();
        ldap.deleteTest(dirContext, getPropertyAsString(DELETE));
        res.sampleEnd();
    }

    /************************************************************
     * This will do the search test  for the User defined 
     * TestCase
     *@return    executed time for the give test case
     ***********************************************************/
    public void searchTest(LdapExtClient ldap, DirContext dirContext, SampleResult res)throws Exception {
        res.sampleStart();
        ldap.searchTest(dirContext, getPropertyAsString(SEARCHBASE),getPropertyAsString(SEARCHFILTER)
        	,getPropertyAsInt(SCOPE),getPropertyAsLong(COUNTLIM),getPropertyAsInt(TIMELIM)
        		,getRequestAttributes(getPropertyAsString(ATTRIBS)),getPropertyAsBoolean(RETOBJ),getPropertyAsBoolean(DEREF));
        res.sampleEnd();		
    }

    /************************************************************
     * This will do the modify test  for the User defined 
     * TestCase
     *@return    executed time for the give test case
     ***********************************************************/
    public void modifyTest(LdapExtClient ldap, DirContext dirContext, SampleResult res)throws Exception{
            res.sampleStart();
            ldap.modifyTest(dirContext, getUserModAttributes(), getPropertyAsString(BASE_ENTRY_DN));
            res.sampleEnd();
    }
         
    /************************************************************
     * This will do the bind  for the User defined 
     * Thread, this bind is used for the whole context
     *@return    executed time for the bind op
     ***********************************************************/
    public void bindOp(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws Exception{
        res.sampleStart();
        dirContext=ldap.connect(getServername(),getPort(),getRootdn(),getUserDN(),getUserPw());
		res.sampleEnd();
        ldapContexts.put(Thread.currentThread().getName(), dirContext);
    }
         
    /************************************************************
     * This will do the bind  for the User defined 
     * TestCase  
     *@return    executed time for the bind op
     ***********************************************************/
    public void singleBindOp(SampleResult res) throws Exception{
        LdapExtClient ldap_temp;
        ldap_temp=new LdapExtClient();
		res.sampleStart();
        ldap_temp.sbind(getServername(),getPort(),getRootdn(),getSuserDN(),getSuserPw());
		res.sampleEnd();
    }
         
    /************************************************************
     * This will do a compare Opp for the User and attribute/value pair defined 
     *@return    executed time for the compare op
     ***********************************************************/
    public void compareOp(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws Exception{
		res.sampleStart();
        ldap.compare(dirContext, getPropertyAsString(COMPAREFILT), getPropertyAsString(COMPAREDN));
		res.sampleEnd();
    }
         
    /************************************************************
     * This will do a moddn Opp for the User new DN defined 
     *@return    executed time for the moddn op
     ***********************************************************/
    public void renameTest(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws Exception{
		res.sampleStart();
        ldap.moddnOp(dirContext, getPropertyAsString(MODDDN), getPropertyAsString(NEWDN));
		res.sampleEnd();
    }
         
    /************************************************************
     * This will do the unbind  for the User defined 
     * TestCase  as well as inbuilt test case
     *@return    executed time for the bind op
     ***********************************************************/
    public void unbindOp(LdapExtClient ldap, DirContext dirContext, SampleResult res) throws Exception{
		res.sampleStart();
        ldap.disconnect(dirContext);
		res.sampleEnd();
        log.info("context and LdapExtClients removed");
    }
         
    /************************************************************
     *  !ToDo (Method description)
     *
     *@param  e  !ToDo (Parameter description)
     *@return    !ToDo (Return description)
     ***********************************************************/
    public SampleResult sample(Entry e)
    {
        String responseData ="<ldapanswer>";
        SampleResult res = new SampleResult();
        res.setResponseData("successfull".getBytes());
        res.setResponseMessage("Success");
        res.setResponseCode("0");
        boolean isSuccessful = true;
        SearchResult sr;
        String iets;
        NamingEnumeration attrlist;
        res.setSampleLabel(getLabel()+":"+getPropertyAsString(TEST)+":"+Thread.currentThread().getName()+";"+System.currentTimeMillis());
        temp_client =(LdapExtClient) ldapConnections.get(Thread.currentThread().getName());
        dirContext =(DirContext) ldapContexts.get(Thread.currentThread().getName());
        if (temp_client == null) {
          	 temp_client =new LdapExtClient();
             try {
          	    dirContext=new InitialDirContext();
             } catch (Exception err) {
                log.error("Ldap client context creation - ",err);
             }
        	 ldapConnections.put(Thread.currentThread().getName(), temp_client);
        }

         try
        {
//        	log.error("performing test: "+getPropertyAsString(TEST));
            if (getPropertyAsString(TEST).equals("unbind")) {
                res.setSamplerData("Unbind");
                responseData=responseData + "<operation><opertype>unbind</opertype>";
                responseData=responseData + "<baseobj>" +getRootdn() +"</baseobj>";
                responseData=responseData + "<binddn>" +getUserDN() +"</binddn></operation>";
                unbindOp(temp_client, dirContext, res);
           }else if (getPropertyAsString(TEST).equals("bind")) {
                res.setSamplerData("Bind as "+getUserDN());
                responseData=responseData + "<operation><opertype>bind</opertype>";
                responseData=responseData + "<baseobj>" +getRootdn() +"</baseobj>";
                responseData=responseData + "<binddn>" +getUserDN() +"</binddn></operation>";
                bindOp(temp_client, dirContext, res);
            }else if (getPropertyAsString(TEST).equals("sbind")) {
                res.setSamplerData("SingleBind as "+getSuserDN());
                responseData=responseData + "<operation><opertype>bind</opertype>";
                responseData=responseData + "<binddn>" +getSuserDN() +"</binddn></operation>";
                singleBindOp(res);
            }else if (getPropertyAsString(TEST).equals("compare")) {
                res.setSamplerData("Compare "+getPropertyAsString(COMPAREFILT)+" "+getPropertyAsString(COMPAREDN));
                responseData=responseData + "<operation><opertype>compare</opertype>";
                responseData=responseData + "<comparedn>" +getPropertyAsString(COMPAREDN) +"</comparedn>";
                responseData=responseData + "<comparefilter>" +getPropertyAsString(COMPAREFILT) +"</comparefilter></operation>";
                compareOp(temp_client, dirContext, res);
            }else if (getPropertyAsString(TEST).equals("add")) {
                res.setSamplerData("Add object "+getPropertyAsString(BASE_ENTRY_DN));
                responseData=responseData + "<operation><opertype>add</opertype>";
                responseData=responseData + "<attributes>" +getArguments().toString() +"</attributes>";
                responseData=responseData + "<dn>" +getPropertyAsString(BASE_ENTRY_DN) +"</dn></operation>";
                addTest(temp_client, dirContext, res);
            }else if (getPropertyAsString(TEST).equals("delete")) {
                res.setSamplerData("Delete object "+getPropertyAsString(DELETE));
                responseData=responseData + "<operation><opertype>delete</opertype>";
                responseData=responseData + "<dn>" +getPropertyAsString(DELETE) +"</dn></operation>";
                deleteTest(temp_client, dirContext, res);
            }else if (getPropertyAsString(TEST).equals("modify")) {
                res.setSamplerData("Modify object "+getPropertyAsString(BASE_ENTRY_DN));
                responseData=responseData + "<operation><opertype>modify</opertype>";
                responseData=responseData + "<dn>" +getPropertyAsString(BASE_ENTRY_DN) +"</dn>";
                responseData=responseData + "<attributes>" +getLDAPArguments().toString() +"</attributes></operation>";
                modifyTest(temp_client, dirContext, res);
            }else if (getPropertyAsString(TEST).equals("rename")) {
                res.setSamplerData("ModDN object "+getPropertyAsString(MODDDN)+" to "+getPropertyAsString(NEWDN));
                responseData=responseData + "<operation><opertype>moddn</opertype>";
                responseData=responseData + "<dn>" +getPropertyAsString(MODDDN) +"</dn>";
                responseData=responseData + "<newdn>" +getPropertyAsString(NEWDN) +"</newdn></operation>";
                renameTest(temp_client, dirContext, res);
            }else if (getPropertyAsString(TEST).equals("search")) {
                res.setSamplerData("Search with filter "+getPropertyAsString(SEARCHFILTER));
                responseData=responseData + "<operation><opertype>search</opertype>";
                responseData=responseData + "<searchfilter>"+ getPropertyAsString(SEARCHFILTER) +"</searchfilter>";
                responseData=responseData + "<searchbase>"+ getPropertyAsString(SEARCHBASE)+","+getPropertyAsString(ROOTDN)+"</searchbase>";
                responseData=responseData + "<scope>"+ getPropertyAsString(SCOPE)+"</scope>";
                responseData=responseData + "<countlimit>"+ getPropertyAsString(COUNTLIM)+"</countlimit>";
                responseData=responseData + "<timelimit>"+ getPropertyAsString(TIMELIM)+"</timelimit>";
                responseData=responseData + "</operation><searchresult>";
                searchTest(temp_client, dirContext, res);
				    while (temp_client.answer.hasMore()) {
						sr = (SearchResult)temp_client.answer.next();
                        responseData=responseData + "<dn>"+sr.getName()+","+getPropertyAsString(SEARCHBASE)+","+getRootdn()+"</dn>";
                        responseData=responseData + "<returnedattr>" +sr.getAttributes().size() +"</returnedattr>";
                        attrlist=sr.getAttributes().getIDs();
 	   					while (attrlist.hasMore()) { 	
	   						iets=(String)attrlist.next();
	   						responseData=responseData + "<attribute><attributename>" +iets.toString()+"</attributename>";
	   						responseData=responseData + "<attributevalue>"+sr.getAttributes().get(iets.toString()).toString().substring(iets.toString().length()+2)+"</attributevalue></attribute>";
	   					}
	   				}
                responseData=responseData + "</searchresult></operation>";
            }

        }
        catch (NamingException ex) {
            String returnData=ex.toString();
             if (returnData.indexOf("LDAP: error code") >=0) {
            	res.setResponseMessage(returnData.substring(returnData.indexOf("LDAP: error code")+22,returnData.indexOf("]")));
            	res.setResponseCode(returnData.substring(returnData.indexOf("LDAP: error code")+17,returnData.indexOf("LDAP: error code")+19));
             }
            isSuccessful = false;
        }
        catch (Exception ex) {
            res.setResponseData(ex.toString().getBytes());
            log.error("Ldap client - ",ex);
            isSuccessful = false;
            res.setResponseMessage("internal error");
            res.setResponseCode("800");
        }
        finally {
           	responseData=responseData + "<responsecode>" + res.getResponseCode() + "</responsecode>";
           	responseData=responseData + "<responsemessage>" + res.getResponseMessage() + "</responsemessage>";
			responseData=responseData + "</ldapanswer>";
			res.setResponseData(responseData.getBytes());
        	res.setThreadName(Thread.currentThread().getName());
        	res.setDataType("text");
            res.setSuccessful(isSuccessful);
        }
        return res;
    }
}