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

package org.apache.jmeter.protocol.jdbc.util;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/*****************************************************************
 This class manages a pool of Connection objects (ConnectionObject).  This
 pool is constantly checked for old, over-used, or dead connections in a
 separated thread.  Connections are rented out and then given back by the
 DBConnect object and its subclasses.  This class is not directly accessed
 by the end-user objects.  It is accessed by the DBConnect object and its
 subclasses.
@author Michael Stover
@version 1.0 10/13/1998
 *****************************************************************/
public class DBConnectionManager
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.jdbc");
  int absoluteMaxConnections=100;
  long accessInterval=1800000;
  Hashtable connections;
  Hashtable rentedConnections;

  static DBConnectionManager manager;

/******************************************************************
Constructor.
*****************************************************************/
  private DBConnectionManager()
  {
	 if(connections==null)
		connections=new Hashtable();
	 if(rentedConnections == null)
		rentedConnections=new Hashtable();
  }

  public static DBConnectionManager getManager()
  {
		if(manager == null)
		{
			manager = new DBConnectionManager();
		}
		return manager;
  }

/*********************************************************************
  Starts the connection manager going for a given database connection, and
  returns the DBKey object required to get a Connection object for this
  database.
@param url URL of database to be connected to.
@param username Username to use to connect to database.
@param password Password to use to connect to database.
@param driver Driver to use for the database.
@param maxUsage Sets the maxUsage parameter for connections to this database.
@param maxConnections Tells the DBConnectionManager how many connections to keep active.
@return DBKey object. Returns null if connection fails.
**********************************************************************/
  public DBKey getKey(String url,String username,String password,
			 String driver,int maxUsage,int maxConnections)
  {
	 DBKey key=new DBKey();
	 if(registerDriver(driver))
	 {
		key.setDriver(driver);
		key.setMaxConnections(maxConnections);
		key.setMaxUsage(maxUsage);
		key.setPassword(password);
		key.setUrl(url);
		key.setUsername(username);
		if(!connections.containsKey(key))
		  setup(key);
	 }
	 else
		key=null;
	 return key;
  }

  /******************************************************
 Constructor.
@param key DBKey that holds all information needed to set up a set of
		  connections.
  ******************************************************/
  public void setup(DBKey key)
  {
	 /* Code used to create a JDBC log for debuggin purposes
	 try{
	 java.io.PrintWriter jdbcLog=new java.io.PrintWriter(new java.io.FileWriter(jdbcLogFile));
	 DriverManager.setLogWriter(jdbcLog);
	 }catch(Exception e){}*/
	 String url=key.getUrl();
	 String username=key.getUsername();
	 String password=key.getPassword();
	 int maxConnections=key.getMaxConnections();
	 int maxUsage=key.getMaxUsage();
	 ConnectionObject[] connectionArray;
	 int dbMax;
	 try{
		DriverManager.registerDriver((java.sql.Driver)Class.forName(key.getDriver()).newInstance());
		DatabaseMetaData md=DriverManager.getConnection(url,username,password).getMetaData();
		dbMax=md.getMaxConnections();
		if(dbMax>0 && maxConnections>dbMax)
		{
		  maxConnections=dbMax;
		  key.setMaxConnections(maxConnections);
		}
		else if(maxConnections>absoluteMaxConnections)
		{
		  maxConnections=absoluteMaxConnections;
		  key.setMaxConnections(maxConnections);
		}
	 }catch(Exception e){log.error("",e);
		maxConnections=1;}
	 connectionArray=new ConnectionObject[maxConnections];
	 int count=-1;
	 while(++count<maxConnections)
		connectionArray[count]=new ConnectionObject(this,key);
	 connections.put(key,connectionArray);
	 System.gc();
  } // End Method

	public void shutdown()
	{
		Iterator iter = connections.keySet().iterator();
		while (iter.hasNext()) {
			close((DBKey)iter.next());
		}
	}



  /******************************************************
  Rents out a database connection object.
@return Connection object.
  ******************************************************/
  public Connection getConnection(DBKey key)      //deleted synchronized
  {
	 ConnectionObject[] connectionArray=(ConnectionObject[])connections.get(key);
	 int maxConnections=key.getMaxConnections();
	 Connection c=null;
	 int index=(int)(100*Math.random());
	 int count=-1;
	 while(++count<maxConnections && c==null)
	 {
		index++;
		c=connectionArray[index%maxConnections].grab();
	 }
	 if(c!=null)
		rentedConnections.put(c,connectionArray[index%maxConnections]);
	 return c;
  } // End Method

  /******************************************************
  Releases a connection back to the pool
@param c Connection object being returned
  ******************************************************/
  public void releaseConnection(Connection c)         // deleted synchronized
  {
  	if(c == null)
  	{
  		return;
  	}
	 ConnectionObject connOb=(ConnectionObject)rentedConnections.get(c);
	 if(connOb!=null)
	 {
		rentedConnections.remove(c);
		connOb.release();
	 }
	 else
	 {
		log.warn("DBConnectionManager: Lost a connection connection='"+c);
		c=null;
	 }
  } // End Method


 /*********************************************************
  Returns a new java.sql.Connection object.
@throws java.sql.SQLException
 *********************************************************/
  public Connection newConnection(DBKey key) throws SQLException       //deleted synchronized
  {
	 Connection c;
	 c=DriverManager.getConnection(key.getUrl(),key.getUsername(),key.getPassword());
	 return c;
  }

/*************************************************************
Closes out this object and returns resources to the system.
*************************************************************/
  public void close(DBKey key)
  {
	 ConnectionObject[] connectionArray=(ConnectionObject[])connections.get(key);
	 int count=-1;
	 while(++count<connectionArray.length)
	 {
		connectionArray[count].close();
		connectionArray[count]=null;
	 }
	 connections.remove(key);
  }



/*********************************************************************
Registers a driver for a database.
@param driver full classname for the driver.
@return True if successful, false otherwise.
***********************************************************************/
  public boolean registerDriver(String driver)
  {
	 try{
		DriverManager.registerDriver((Driver)Class.forName(driver).newInstance());
	 }catch(Exception e){log.error("",e); return false;}
	 return true;
  }


/*******************************************************************
 Private method to check if database exists.
@return True if database exists, false otherwise
 ******************************************************************
  private synchronized boolean checkForDatabase()
  {
	 boolean connected=true;
	 try
	 {
		DatabaseMetaData dmd=connection[counter].getCon().getMetaData();
		int cons=dmd.getMaxConnections();
	 }catch(SQLException e){connected=false;log.error("",e);}

	 return connected;
  }  //end of method      */
}





