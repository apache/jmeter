/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.jdbc.sampler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.jdbc.util.ConnectionPoolException;
import org.apache.jmeter.protocol.jdbc.util.DBConnectionManager;
import org.apache.jmeter.protocol.jdbc.util.DBKey;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler which understands JDBC database requests.
 *
 * @author Original author unknown
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Revision$
 */
public class JDBCSampler extends AbstractSampler implements TestListener
{
    private static Logger log = LoggingManager.getLoggerForClass();
    
    public static final String URL = "JDBCSampler.url";
    public static final String DRIVER = "JDBCSampler.driver";
    public static final String QUERY = "JDBCSampler.query";

    public static final String JDBCSAMPLER_PROPERTY_PREFIX = "JDBCSampler.";
    public static final String CONNECTION_POOL_IMPL =
        JDBCSAMPLER_PROPERTY_PREFIX + "connPoolClass";

    
    /** Database connection pool manager. */
    private transient DBConnectionManager manager =
        DBConnectionManager.getManager();
    private transient DBKey dbkey = null;

    /**
     * Creates a JDBCSampler.
     */
    public JDBCSampler()
    {
    }

    public SampleResult sample(Entry e)
    {
        DBKey key = null;

        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(this.toString());


        res.sampleStart();
        Connection conn = null;
        Statement stmt = null;

        try
        {
            key = getKey();
            
            // TODO: Consider creating a sub-result with the time to get the
            //       connection.
            conn = manager.getConnection(key);
            stmt = conn.createStatement();
            
            // Based on query return value, get results
            if (stmt.execute(getQuery()))
            {
                ResultSet rs = null;
                try
                {
                    rs = stmt.getResultSet();
                    Data data = getDataFromResultSet(rs);
                    res.setResponseData(data.toString().getBytes());
                }
                finally
                {
                    if (rs != null)
                    {
                        try
                        {
                            rs.close();
                        }
                        catch (SQLException exc)
                        {
                            log.warn("Error closing ResultSet", exc);
                        }
                    }
                }
            }
            else
            {
                int updateCount = stmt.getUpdateCount();
                String results = updateCount + " updates";
                res.setResponseData(results.getBytes());
            }

            res.setDataType(SampleResult.TEXT);
            res.setSuccessful(true);
        }
        catch (Exception ex)
        {
            log.error("Error in JDBC sampling", ex);
            res.setResponseData(new byte[0]);
            res.setSuccessful(false);
        }
        finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (SQLException err)
                {
                    stmt = null;
                }
            }

            if (conn != null)
            {
                manager.releaseConnection(key, conn);
            }
        }

        res.sampleEnd();
        return res;
    }

    private DBKey getKey() throws ConnectionPoolException
    {
        if (dbkey == null)
        {
            // With multiple threads, it is possible that more than one thread
            // will enter this block at the same time, resulting in multiple
            // calls to manager.getKey().  But this is okay, since DBKey has
            // a proper implementation of equals and hashCode.  The original
            // implementation of this method always returned a new instance --
            // this cached dbkey is just a performance optimization.
            dbkey =
                manager.getKey(
                    getUrl(),
                    getUsername(),
                    getPassword(),
                    getDriver(),
                    getJDBCProperties());
        }
        
        return dbkey;
    }

    private Map getJDBCProperties()
    {
        Map props = new HashMap();
        
        PropertyIterator iter = propertyIterator();
        while (iter.hasNext())
        {
            JMeterProperty prop = iter.next();
            if (prop.getName().startsWith(JDBCSAMPLER_PROPERTY_PREFIX))
            {
                props.put(prop.getName(), prop);
            }
        }
        return props;
    }
    
    /**
     * Gets a Data object from a ResultSet.
     *
     * @param  rs ResultSet passed in from a database query
     * @return    a Data object
     * @throws    java.sql.SQLException
     */
    private Data getDataFromResultSet(ResultSet rs) throws SQLException
    {
        ResultSetMetaData meta = rs.getMetaData();
        Data data = new Data();

        int numColumns = meta.getColumnCount();
        String[] dbCols = new String[numColumns];
        for (int i = 0; i < numColumns; i++)
        {
            dbCols[i] = meta.getColumnName(i + 1);
            data.addHeader(dbCols[i]);
        }
        
        while (rs.next())
        {
            data.next();
            for (int i = 0; i < numColumns; i++)
            {
                Object o = rs.getObject(i + 1);
                if (o instanceof byte[])
                {
                    o = new String((byte[]) o);
                }
                data.addColumnValue(dbCols[i], o);
            }
        }
        return data;
    }


    public String getDriver()
    {
        return getPropertyAsString(DRIVER);
    }

    public String getUrl()
    {
        return getPropertyAsString(URL);
    }

    public String getUsername()
    {
        return getPropertyAsString(ConfigTestElement.USERNAME);
    }

    public String getPassword()
    {
        return getPropertyAsString(ConfigTestElement.PASSWORD);
    }

    public String getQuery()
    {
        return this.getPropertyAsString(QUERY);
    }


    public String toString()
    {
        return getUrl() + ", user: " + getUsername() + "\n" + getQuery();
    }


    public void testStarted(String host)
    {
        testStarted();
    }

    public synchronized void testStarted()
    {
        // The first call to getKey for a given key will set up the connection
        // pool.  This can take awhile, so do it while the test is starting
        // instead of waiting for the first sample.
        try
        {
            getKey();
        }
        catch (ConnectionPoolException e)
        {
            log.error("Error initializing database connection", e);
        }
    }

    public void testEnded(String host)
    {
        testEnded();
    }

    public synchronized void testEnded()
    {
        manager.shutdown();
        dbkey = null;
    }

    public void testIterationStart(LoopIterationEvent event)
    {
    }
}
