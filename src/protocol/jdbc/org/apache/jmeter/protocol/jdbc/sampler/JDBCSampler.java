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

package org.apache.jmeter.protocol.jdbc.sampler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
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
public class JDBCSampler extends TestBean implements Sampler
{
    private static Logger log = LoggingManager.getLoggerForClass();
    public static final String QUERY = "query";
    
    public String query = "";
    public String dataSource = "";

    /**
     * Creates a JDBCSampler.
     */
    public JDBCSampler()
    {
    }

    public SampleResult sample(Entry e)
    {
       log.debug("sampling jdbc");
       SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(toString());


        res.sampleStart();
        DataSourceComponent pool = (DataSourceComponent)getThreadContext().getVariables().getObject(getDataSource());
        log.debug("DataSourceComponent: " + pool);
        Connection conn = null;
        Statement stmt = null;

        try
        {
            
            // TODO: Consider creating a sub-result with the time to get the
            //       connection.
            conn = pool.getConnection();
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
            res.setResponseData(ex.toString().getBytes());
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
                   log.error("Error in JDBC sampling", err);
                   res.setResponseData(err.toString().getBytes());
                   res.setSuccessful(false);
                }
            }

            if (conn != null)
            {
                try
               {
                  conn.close();
               }
               catch (SQLException e1)
               {
                  log.error("Error in JDBC sampling", e1);
                  res.setResponseData(e1.toString().getBytes());
                  res.setSuccessful(false);
               }
            }
        }

        res.sampleEnd();
        return res;
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

    public String getQuery()
    {
        return query;
    }


    public String toString()
    {
        return getQuery();
    }
   /**
    * @param query The query to set.
    */
   public void setQuery(String query)
   {
      this.query = query;
   }
   /**
    * @return Returns the dataSource.
    */
   public String getDataSource()
   {
      return dataSource;
   }
   /**
    * @param dataSource The dataSource to set.
    */
   public void setDataSource(String dataSource)
   {
      this.dataSource = dataSource;
   }
}
