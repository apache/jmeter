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

package org.apache.jmeter.protocol.jdbc.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


/**
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Revision$
 */
public class JMeter19ConnectionPool implements ConnectionPool
{
    private static Logger log = LoggingManager.getLoggerForClass();
    private static final int ABSOLUTE_MAX_CONNECTIONS = 100;

    public static final String CONNECTIONS =
        JDBCSampler.JDBCSAMPLER_PROPERTY_PREFIX + "connections";
    public static final String MAXUSE =
        JDBCSampler.JDBCSAMPLER_PROPERTY_PREFIX + "maxuse";

    private final ConnectionObject connectionArray[];
    private final Hashtable rentedConnections = new Hashtable();

    private final DBKey key;
    private int maxConnections;

    public JMeter19ConnectionPool(DBKey key, Map properties)
        throws ConnectionPoolException
    {
        this.key = key;

        this.maxConnections =
            ((JMeterProperty) properties.get(CONNECTIONS)).getIntValue();
        int maxUsage =
            ((JMeterProperty) properties.get(MAXUSE)).getIntValue();
        
        validateMaxConnections();

        connectionArray = new ConnectionObject[maxConnections];
        try
        {
            for (int i = 0; i < maxConnections; i++)
            {
                connectionArray[i] = new ConnectionObject(key, maxUsage);
            }
        }
        catch (SQLException e)
        {
            log.error("Error initializing JDBC connection pool", e);
            throw new ConnectionPoolException(
                "Error initializing JDBC connection pool: " + e);
        }
    }


    private void validateMaxConnections()
    {
        //Connection c = null;
        try
        {
            DatabaseMetaData md =
                DriverManager
                    .getConnection(
                        key.getUrl(),
                        key.getUsername(),
                        key.getPassword())
                    .getMetaData();

            int dbMax = md.getMaxConnections();
            if (dbMax > 0 && maxConnections > dbMax)
            {
                log.warn(
                    "Connection pool configured for "
                        + maxConnections
                        + " but database claims to allow only "
                        + dbMax
                        + ".  Reducing the pool size, but problems may occur "
                        + "if multiple connection pools are in use for the "
                        + "same database.");
                maxConnections = dbMax;
            }
            else if (maxConnections > ABSOLUTE_MAX_CONNECTIONS)
            {
                maxConnections = ABSOLUTE_MAX_CONNECTIONS;
            }
        }
        catch (Exception e)
        {
            log.error("Couldn't get connection to database", e);
            maxConnections = 0;
            return;
        }
        finally
        {
//            if (c != null)
//            {
//                try
//                {
//                    c.close();
//                }
//                catch (SQLException e)
//                {
//                    log.warn("Error closing metadata database connection", e);
//                }
//            }
        }
    }

    /**
     * Rents out a database connection object.
     * @return Connection object.
     */
    public Connection getConnection() throws ConnectionPoolException
    {
        if (connectionArray.length == 0)
        {
            throw new NoConnectionsAvailableException();
        }

        Connection c = null;
        int attempts = 0;
        while (attempts < 20 && (c = attemptGetConnection()) == null)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (Exception err)
            {
                attempts++;
            }
        }
        
        return c;
    }

    private Connection attemptGetConnection()
    {
        Connection c = null;
        int index = (int) (100 * Math.random());
        int count = -1;
        while (++count < maxConnections && c == null)
        {
            index++;
            c = connectionArray[index % maxConnections].grab();
        }
        
        if (c != null)
        {
            rentedConnections.put(c, connectionArray[index % maxConnections]);
        }
        
        return c;
    }
    /**
     * Releases a connection back to the pool.
     * @param c Connection object being returned
     */
    public void returnConnection(Connection c)
    {
        if (c == null)
        {
            return;
        }
        
        ConnectionObject connOb = (ConnectionObject) rentedConnections.get(c);
        if (connOb != null)
        {
            rentedConnections.remove(c);
            connOb.release();
        }
        else
        {
            log.warn("DBConnectionManager: Lost a connection connection='" + c);
            c = null;
        }
    }

    /**
     * Returns a new java.sql.Connection object.
     */
    public Connection newConnection(DBKey key) throws SQLException
    {
        return DriverManager.getConnection(
            key.getUrl(),
            key.getUsername(),
            key.getPassword());
    }

    /**
     * Closes out this object and returns resources to the system.
     */
    public void close()
    {
        for (int i = 0; i < connectionArray.length; i++)
        {
            connectionArray[i].close();
            connectionArray[i] = null;
        }
    }
}
