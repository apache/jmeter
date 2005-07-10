// $Header$
/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.samplers.AbstractSampler;
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
public class JDBCSampler extends AbstractSampler implements TestBean
{
    private static Logger log = LoggingManager.getLoggerForClass();
    public static final String QUERY = "query";
    
    public String query = "";
    public String dataSource = "";
    public boolean queryOnly = true;

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
            
        	if (pool == null) throw new SQLException("No pool created");
            
            // TODO: Consider creating a sub-result with the time to get the
            //       connection.
            conn = pool.getConnection();
            stmt = conn.createStatement();
            
            // Based on query return value, get results
            if (isQueryOnly())
            {
                ResultSet rs = null;
                try
                {
                    rs = stmt.executeQuery(getQuery());
                    Data data = getDataFromResultSet(rs);
                    res.setResponseData(data.toString().getBytes());
                }
                finally
                {
                    if (rs != null)
                    {
                        try { rs.close(); }
                        catch (SQLException exc) {log.warn("Error closing ResultSet", exc);}
                    }
                }
            }
            else
            {
                stmt.execute(getQuery());
                int updateCount = stmt.getUpdateCount();
                String results = updateCount + " updates";
                res.setResponseData(results.getBytes());
            }

            res.setDataType(SampleResult.TEXT);
            // Bug 31184 - make sure encoding is specified
            res.setDataEncoding(System.getProperty("file.encoding"));
            res.setSuccessful(true);
        }
        catch (SQLException ex)
        {
            log.error("Error in JDBC sampling", ex);
            res.setResponseMessage(ex.toString());
            res.setSuccessful(false);
        }
        finally
        {
            if (stmt != null) {
                try { stmt.close(); } 
                catch (SQLException ex) { log.warn("Error closing statement", ex); }
            }
            if (conn != null) {
               try { conn.close(); }
               catch (SQLException ex) { log.warn("Error closing connection", ex); }
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

/**
 * @return Returns the queryOnly.
 */
public boolean isQueryOnly() {
    return queryOnly;
}

/**
 * @param queryOnly The queryOnly to set.
 */
public void setQueryOnly(boolean queryOnly) {
    this.queryOnly = queryOnly;
}
}
