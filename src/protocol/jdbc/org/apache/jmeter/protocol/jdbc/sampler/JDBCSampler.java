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
import org.apache.jmeter.protocol.jdbc.util.DBConnectionManager;
import org.apache.jmeter.protocol.jdbc.util.DBKey;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.PerSampleClonable;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jorphan.collections.Data;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
/************************************************************
 *  A sampler which understands JDBC database requests
 *
 *@author     $Author$
 *@created    $Date$
 *@version    $Revision$
 ***********************************************************/
public class JDBCSampler extends AbstractSampler
	implements TestListener, PerSampleClonable {
    transient private static Logger log =
            Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.jdbc");
    public final static String URL = "JDBCSampler.url";
    public final static String DRIVER = "JDBCSampler.driver";
    public static String CONNECTIONS = "JDBCSampler.connections";
    public static String MAXUSE = "JDBCSampler.maxuse";
    //database connection pool manager
    transient DBConnectionManager manager = DBConnectionManager.getManager();
    // end method
    public final static String QUERY = "JDBCSampler.query";
    private static Map keyMap = new HashMap();
    private static boolean running = false;

    /**
     * Creates a JDBCSampler.
     */
    public JDBCSampler()
    {
    }

    public void testStarted(String host)
    {
    }

    public void testEnded(String host)
    {
    }

    public synchronized void testStarted()
    {
        if (!running)
        {
            running = true;
        }
    }

    public synchronized void testEnded()
    {
        if (running)
        {
            manager.shutdown();
            keyMap.clear();
            running = false;
        }
    }

    public String getQuery()
    {
        return this.getPropertyAsString(QUERY);
    }

    public SampleResult sample(Entry e)
    {
        DBKey key = getKey();
        long start;
        long end;
        long time;
        time = start = end = 0;
        SampleResult res = new SampleResult();
        Connection con = null;
        ResultSet rs = null;
        Statement stmt = null;
        Data data = new Data();
        res.setSampleLabel(getName());
        start = System.currentTimeMillis();
        try
        {
            int count = 0;
            while (count < 20 && (con = manager.getConnection(key)) == null)
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (Exception err)
                {
                    count++;
                }
            }
            stmt = con.createStatement();
            // Execute database query
            boolean retVal = stmt.execute(getQuery());
            // Based on query return value, get results
            if (retVal)
            {
                rs = stmt.getResultSet();
                data = getDataFromResultSet(rs);
                rs.close();
            }
            else
            {
                int updateCount = stmt.getUpdateCount();
            }
            stmt.close();
            manager.releaseConnection(con);
            res.setResponseData(data.toString().getBytes());
            res.setDataType(SampleResult.TEXT);
            res.setSuccessful(true);
        }
        catch (Exception ex)
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (SQLException err)
                {
                    rs = null;
                }
            }
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
            manager.releaseConnection(con);
            log.error("Error in JDBC sampling", ex);
            res.setResponseData(new byte[0]);
            res.setSuccessful(false);
        }
        // Calculate response time
        end = System.currentTimeMillis();
        time += end - start;
        res.setTime(time);
        res.setSamplerData(this.toString());
        return res;
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

    public String getDriver()
    {
        return getPropertyAsString(DRIVER);
    }

    public int getMaxUse()
    {
        return getPropertyAsInt(MAXUSE);
    }

    public int getNumConnections()
    {
        return getPropertyAsInt(CONNECTIONS);
    }

    private DBKey getKey()
    {
        DBKey key = (DBKey) keyMap.get(getUrl());
        if (key == null)
        {
            key = manager.getKey(getUrl(),
                                 getUsername(),
                                 getPassword(),
                                 getDriver(),
                                 getMaxUse(),
                                 getNumConnections());
            keyMap.put(getUrl(), key);
        }
        return key;
    }

    /**
     * Gets a Data object from a ResultSet.
     *
     * @param  rs                      ResultSet passed in from a database query
     * @return                         A Data object (com.stover.utils)
     * @exception  SQLException        !ToDo (Exception description)
     * @throws  java.sql.SQLException
     */
    private Data getDataFromResultSet(ResultSet rs) throws SQLException
    {
        ResultSetMetaData meta;
        Data data = new Data();
        meta = rs.getMetaData();
        int numColumns = meta.getColumnCount();
        String[] dbCols = new String[numColumns];
        for (int count = 1; count <= numColumns; count++)
        {
            dbCols[count - 1] = meta.getColumnName(count);
            data.addHeader(dbCols[count - 1]);
        }
        while (rs.next())
        {
            data.next();
            for (int count = 0; count < numColumns; count++)
            {
                Object o = rs.getObject(count + 1);
                if (o == null)
                {
                }
                else if (o instanceof byte[])
                {
                    o = new String((byte[]) o);
                }
                data.addColumnValue(dbCols[count], o);
            }
        }
        return data;
    }

    public String toString()
    {
        return getUrl()+", user: "+getUsername()+"\n"+getQuery();
    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testIterationStart(LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {}

}
