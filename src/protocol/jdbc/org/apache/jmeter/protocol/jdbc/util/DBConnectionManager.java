/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class manages a pool of Connection objects (ConnectionObject).  This
 * pool is constantly checked for old, over-used, or dead connections in a
 * separated thread.  Connections are rented out and then given back by the
 * DBConnect object and its subclasses.  This class is not directly accessed
 * by the end-user objects.  It is accessed by the DBConnect object and its
 * subclasses.
 * 
 * @author Michael Stover
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Revision$
 */

public final class DBConnectionManager
{
    private static Logger log = LoggingManager.getLoggerForClass();

    private static DBConnectionManager manager = new DBConnectionManager();
    
    /** Map of DBKey to ConnectionPool. */
    private Map poolMap = new HashMap();
    
    /**
     * Private constructor to prevent instantiation from outside this class.
     */
    private DBConnectionManager()
    {
    }

    public static DBConnectionManager getManager()
    {
        return manager;
    }

    /**
     * Starts the connection manager going for a given database connection, and
     * returns the DBKey object required to get a Connection object for this
     * database.
     * 
     * @param url       URL of database to be connected to.
     * @param username  username to use to connect to database.
     * @param password  password to use to connect to database.
     * @param driver    driver to use for the database.
     * @param maxUsage  sets the maxUsage parameter for connections to this
     *                  database.
     * @param maxConnections tells the DBConnectionManager how many connections
     *                  to keep active.
     * @return          DBKey object. Returns null if connection fails.
     */
    public DBKey getKey(
        String url,
        String username,
        String password,
        String driver,
        int maxUsage,
        int maxConnections)
    {
        DBKey key =
            new DBKey(
                driver,
                url,
                username,
                password);

        synchronized (poolMap)
        {
            if (!poolMap.containsKey(key))
            {
                if (registerDriver(driver))
                {
                    poolMap.put(
                        key,
                        createConnectionPool(key, maxUsage, maxConnections));
                }
                else
                {
                    return null;
                }
            }
        }
        
        return key;
    }

    private ConnectionPool createConnectionPool(
        DBKey key,
        int maxUsage,
        int maxConnections)
    {
        return new JMeter19ConnectionPool(key, maxUsage, maxConnections);
    }

    public void shutdown()
    {
        synchronized (poolMap)
        {
            Iterator iter = poolMap.keySet().iterator();
            while (iter.hasNext())
            {
                DBKey key = (DBKey)iter.next();
                ConnectionPool pool = (ConnectionPool) poolMap.remove(key);
                pool.close();
            }
        }
    }

    private ConnectionPool getPool(DBKey key)
    {
        synchronized (poolMap)
        {
            return (ConnectionPool)poolMap.get(key);
        }
    }
    
    /**
     * Rents out a database connection object.
     * @return Connection object.
     */
    public Connection getConnection(DBKey key)
        throws NoConnectionsAvailableException
    {
        ConnectionPool pool = getPool(key);
        
        if (pool == null)
        {
            throw new NoConnectionsAvailableException();
        }
        else
        {
            try
            {
                return pool.getConnection();
            }
            catch (NoSuchElementException e)
            {
                log.warn(
                    "NoSuchElementException getting database connection",
                    e);
                return null;
            }
            catch (Exception e)
            {
                log.warn("Exception getting database connection from pool", e);
                throw new NoConnectionsAvailableException();
            }
        }
    }

    /**
     * Releases a connection back to the pool.
     * @param c Connection object being returned
     */
    public void releaseConnection(DBKey key, Connection c)
    {
        getPool(key).returnConnection(c);
    }

    /**
     * Registers a driver for a database.
     * @param driver full classname for the driver.
     * @return True if successful, false otherwise.
     */
    private boolean registerDriver(String driver)
    {
        try
        {
            DriverManager.registerDriver(
                (Driver) Class.forName(driver).newInstance());
        }
        catch (Exception e)
        {
            log.error("Error registering database driver '" + driver + "'", e);
            return false;
        }
        return true;
    }
}
