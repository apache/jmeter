// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.jdbc.util;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.testelement.property.JMeterProperty;
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
     * @param properties configuration properties to be used by the connection
     *                  pool.
     * @return          DBKey object. Returns null if connection fails.
     */
    public DBKey getKey(
        String url,
        String username,
        String password,
        String driver,
        Map properties)
        throws ConnectionPoolException
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
                        createConnectionPool(key, properties));
                }
                else
                {
                    return null;
                }
            }
        }
        
        return key;
    }

    private ConnectionPool createConnectionPool(DBKey key, Map properties)
        throws ConnectionPoolException
    {
        String className =
            ((JMeterProperty) properties.get(JDBCSampler.CONNECTION_POOL_IMPL))
                .getStringValue();

        Class types[] = new Class[] {DBKey.class, Map.class};         
        Object params[] = new Object[] {key, properties};
        
        try
        {
            Class poolClass = Class.forName(className);
            Constructor constructor = poolClass.getConstructor(types);
            return (ConnectionPool)constructor.newInstance(params);
        }
        catch (Exception e)
        {
            log.error(
                "Error instantiating JDBC connection pool class '"
                    + className
                    + "'",
                e);
            throw new ConnectionPoolException(
                "Error instantiating JDBC connection pool class '"
                    + className
                    + "': "
                    + e);
        }
    }

    public void shutdown()
    {
    	log.debug("Running shutdown from "+Thread.currentThread().getName());
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
        catch (SQLException e)
        {
            log.error("Error registering database driver '" + driver + "'", e);
            return false;
        } catch (InstantiationException e) {
            log.error("Error registering database driver '" + driver + "'", e);
            return false;
		} catch (IllegalAccessException e) {
            log.error("Error registering database driver '" + driver + "'", e);
            return false;
		} catch (ClassNotFoundException e) {
            log.error("Error registering database driver '" + driver + "'", e);
            return false;
		}
        return true;
    }
}
