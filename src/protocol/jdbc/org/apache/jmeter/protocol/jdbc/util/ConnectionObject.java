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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A wrapper class for a database Connection object.  This class holds
 * information about the state of the connection object in a pool managed by
 * a DBConnectionManager object.  It keeps track of how many times the
 * connection object has been used, the time of its last usage, and whether it
 * is currently in use.
 * @author Michael Stover
 * @version $Revision$
 */
public class ConnectionObject implements Runnable
{
    static long accessInterval = 180000;

    private static Logger log = LoggingManager.getLoggerForClass();

    private final DBKey key;
    private Connection con;
    private int useCount;
    private int maxUsage;
    private long lastAccessed;
    private volatile boolean inUse;
    private volatile boolean inMaintenance;

    private Thread reset;
    
    /**
     * Constructor - takes a connection object.
     * 
     * @param k DBKey object.
     * @param maxUsage 
     */
    public ConnectionObject(DBKey k, int maxUsage) throws SQLException
    {
        key = k;
        reset = new Thread(this);
        useCount = 0;
        lastAccessed = System.currentTimeMillis();
        inMaintenance = true;
        inUse = false;
        con = null;
        this.maxUsage = maxUsage;
        reset();
    }

    /**
     * Gets whether the Connection Object is being maintained.
     * @return true if the ConnectionObject is being maintained, false
     *         otherwise.
     */
    public boolean getInMaintenance()
    {
        return inMaintenance;
    }

    /**
     * Sets whether the Connection Object is being maintained.
     * @param b true if the ConnectionObject is being maintained, false
     *          otherwise.
     */
    public void setInMaintenance(boolean b)
    {
        inMaintenance = b;
    }

    /**
     * Gets the last time this object was accessed.
     * @return Time (in milliseconds) the connection object was last used
     */
    public long getLastAccessed()
    {
        return lastAccessed;
    }

    /**
     * Closes out this object and returns resources to the system.
     */
    public void close()
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (SQLException e)
            {
            }
        }
        con = null;
    }

    /**
     * Updates the last accessed time for the connection object.
     */
    public void update()
    {
        lastAccessed = System.currentTimeMillis();
    }

    /**
     * Gets whether the connection object is currently in use.
     * @return True if it is in use, false otherwise.
     */
    public boolean getInUse()
    {
        return inUse;
    }

    /**
     * Grabs the connection and sets the inUse value to true.
     * @return connection object
     */
    public synchronized Connection grab()
    {
        Connection c = null;
        if (inUse || inMaintenance)
        {
            log.debug(
                "Connection not available because it is "
                    + (inUse ? "in use" : "in maintenance"));
        }
        else
        {
            if (con != null)
            {
                try
                {
                    if (con.isClosed())
                    {
                        log.debug(
                            "Connection is closed.  "
                                + "Putting it in maintenance.");
                        inMaintenance = true;
                        //   reset=new Thread(this);
                        reset.start();
                    }
                    else if (
                        System.currentTimeMillis() - lastAccessed
                            > accessInterval)
                    {
                        log.debug(
                            "Connection is timed out.  "
                                + "Putting it in maintenance.");
                        inMaintenance = true;
                        //   reset=new Thread(this);
                        reset.start();
                    }
                    else
                    {
                        log.debug(
                            "Connection is available."
                                + "Marking it as in use.");
                        inUse = true;
                        c = con;
                    }
                }
                catch (SQLException e)
                {
                    log.warn("Exception checking if connection is closed", e);
                }
            }
            else
            {
                log.debug("connection is null.  Putting it in maintenance.");
                inMaintenance = true;
                reset.start();
            }
        }
        return c;
    }

    /**
     * Gets the number of times this connection object has been used.
     * @return Number of times the connection object has been used.
     */
    public int getUseCount()
    {
        return useCount;
    }

    /**
     * Method to run in separate thread that resets the connection object
     */
    public void run()
    {
        boolean set = true;
        while (set)
        {
            try
            {
                reset();
                set = false;
            }
            catch (SQLException e)
            {
                log.error("ConnectionObject: url = " + key.getUrl(), e);
            }
        }
        reset = new Thread(this);
    }

    /**
     * Resets the use count, the last accessed time, and the in Use values
     * and replaces the old connection object with the new one.
     */
    public void reset() throws SQLException
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
            con = null;
        }
        con = DriverManager.getConnection(
            key.getUrl(),
            key.getUsername(),
            key.getPassword());

        useCount = 0;
        lastAccessed = System.currentTimeMillis();
        inUse = false;
        inMaintenance = false;
    }

    /**
     * Releases the connection object.  Increments its usage count, updates
     * the last accessed time, and returns it for use in the pool.
     */
    public void release()
    {
        useCount++;
        try
        {
            if (con != null)
            {
                if (System.currentTimeMillis() - lastAccessed > accessInterval)
                {
                    inMaintenance = true;
                    reset.start();
                }
                else if (useCount >= maxUsage)
                {
                    inMaintenance = true;
                    reset.start();
                }
                else if (con.isClosed())
                {
                    inMaintenance = true;
                    reset.start();
                }
            }
            else
            {
                inMaintenance = true;
                reset.start();
            }
        }
        catch (SQLException e)
        {
            inMaintenance = true;
            reset.start();
        }
        inUse = false;
        lastAccessed = System.currentTimeMillis();
    }

    /**
     * Returns the connection held by this connection object.
     * @return Connection object
     */
    public Connection getCon()
    {
        return con;
    }
}
