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
import java.sql.SQLException;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;


/*********************************************************
 A wrapper class for a database Connection object.  This class holds
 information about the state of the connection object in a pool managed by
 a DBConnectionManager object.  It keeps track of how many times the
 connection object has been used, the time of its last usage, and whether it
 is currently in use.
@author Michael Stover
@version 1.0 10/13/1998
 *****************************************************************/
public class ConnectionObject  implements Runnable
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.jdbc");
  Connection con;
  DBKey key;
  int useCount,maxUsage;
  long lastAccessed;
  boolean inUse,inMaintenance;
  DBConnectionManager manager;
  Thread reset;
  static long accessInterval=180000;

  /******************************************************
  Constructor - takes a connection object.
@param man DBConnectionManager object.
@param k DBKey object.
  ******************************************************/
  public ConnectionObject(DBConnectionManager man,DBKey k)
  {
	 key=k;
	 manager=man;
	 reset=new Thread(this);
	 useCount=0;
	 lastAccessed=System.currentTimeMillis();
	 inMaintenance=true;
	 inUse=false;
	 con=null;
	 maxUsage=key.getMaxUsage();
	 reset.start();
	/* try{
		reset();
	 }catch(SQLException e){log.error("",e);}*/
  } // End Method

/*************************************************************
Gets whether the Connection Object is being maintained.
@return true if the ConnectionObject is being maintained, false otherwise.
****************************************************************/
  public boolean getInMaintenance()
  {
	 return inMaintenance;
  }

/*************************************************************
Sets whether the Connection Object is being maintained.
@param b true if the ConnectionObject is being maintained, false otherwise.
****************************************************************/
  public void setInMaintenance(boolean b)
  {
	 inMaintenance=b;
  }

  /******************************************************
  Gets the last time this object was accessed.
@return Time (in milliseconds) the connection object was last used
  ******************************************************/
  public long getLastAccessed()
  {
	 return lastAccessed;
  } // End Method

/*************************************************************
Closes out this object and returns resources to the system.
*************************************************************/
  public void close()
  {
	 if(con!=null)
	 {
		try{
		  con.close();
		}catch(SQLException e){}
	 }
	 con=null;
  }

  /******************************************************
 Updates the last accessed time for the connection object.
  ******************************************************/
  public void update()
  {
	 lastAccessed=System.currentTimeMillis();
  } // End Method

  /******************************************************
  Gets whether the connection object is currently in use.
@return True if it is in use, false otherwise.
  ******************************************************/
  public boolean getInUse()
  {
	 return inUse;
  } // End Method

  /******************************************************
  Grabs the connection and sets the inUse value to true.
@return connection object
  ******************************************************/
  public synchronized Connection grab()
  {
	 Connection c=null;
	 if(!inUse && !inMaintenance)
	 {
		if(con!=null)
		{
		  try{
			 if(con.isClosed())
			 {
				inMaintenance=true;
		 //   reset=new Thread(this);
				reset.start();
			 }
			 else if(System.currentTimeMillis()-lastAccessed>accessInterval)
			 {
				inMaintenance=true;
		 //   reset=new Thread(this);
				reset.start();
			 }
			 else
			 {
				inUse=true;
				c=con;
			 }
		  }catch(SQLException e){}
		}
		else
		{
		  inMaintenance=true;
		 // reset=new Thread(this);
		  reset.start();
		}
	 }
	 return c;
  }

  /******************************************************
  Gets the number of times this connection object has been used.
@return Number of times the connection object has been used.
  ******************************************************/
  public int getUseCount()
  {
	 return useCount;
  } // End Method

/***************************************************
Method to run in separate thread that resets the connection object
*******************************************************/
  public void run()
  {
	// Functions.javaLog("ConnectionObject: Got to here - 1");
	 boolean set=true;
	 while(set)
	 {
		try{
		  reset();
		  set=false;
		 // Functions.javaLog("ConnectionObject: Got to here - 2");
		}catch(SQLException e){
			log.error("ConnectionObject: url = "+key.getUrl(),e);
		}			
	 }
	// Functions.javaLog("ConnectionObject: Got to here - 3");
	 reset=new Thread(this);
  }

  /******************************************************
  Resets the use count, the last accessed time, and the in Use values
	And replaces the old connection object with the new one.
@param c new connection object to set to
  ******************************************************/
  public void reset()  throws SQLException
  {
	 if(con!=null)
	 {
	  // try{
	  //   con.commit();
	  // }catch(SQLException e){}
		try{
		  con.close();
		}catch(Exception e){}
		con=null;
	 }
	 con=manager.newConnection(key);
	 useCount=0;
	 lastAccessed=System.currentTimeMillis();
	 inUse=false;
	 inMaintenance=false;
  } // End Method

  /******************************************************
  Releases the connection object.  Increments its usage count, updates
	the last accessed time, and returns it for use in the pool.
  ******************************************************/
  public void release()
  {
	 useCount++;
	 try{
		if(con!=null)
		{
		  //con.commit();
		  if(System.currentTimeMillis()-lastAccessed>accessInterval)
		  {
			 inMaintenance=true;
		 //   reset=new Thread(this);
			 reset.start();
		  }
		  else if(useCount>=maxUsage)
		  {
			 inMaintenance=true;
			// reset=new Thread(this);
			 reset.start();
		  }
		  else if(con.isClosed())
		  {
			 inMaintenance=true;
		 //   reset=new Thread(this);
			 reset.start();
		  }
		}
		else
		{
		  inMaintenance=true;
		 // reset=new Thread(this);
		  reset.start();
		}
	 }catch(SQLException e){
		inMaintenance=true;
		reset.start();
	 }
	 inUse=false;
	 lastAccessed=System.currentTimeMillis();
  } // End Method

  /******************************************************
  Returns the connection held by this connection object.
@return Connection object
  ******************************************************/
  public Connection getCon()
  {
	 return con;
  } // End Method
}
