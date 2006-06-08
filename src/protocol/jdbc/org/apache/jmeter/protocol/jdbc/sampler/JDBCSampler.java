/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler which understands JDBC database requests.
 * 
 * @author Original author unknown
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 */
public class JDBCSampler extends AbstractSampler implements TestBean {
	private static final Logger log = LoggingManager.getLoggerForClass();

    // Query types (used to communicate with GUI)
	static final String SELECT   = "Select Statement";
	static final String UPDATE   = "Update Statement";
	static final String CALLABLE = "Callable Statement";

	private String query = "";

	private String dataSource = "";

	private String queryType = SELECT;

	/**
	 * Creates a JDBCSampler.
	 */
	public JDBCSampler() {
	}

	public SampleResult sample(Entry e) {
		log.debug("sampling jdbc");
		
        SampleResult res = new SampleResult();
		res.setSampleLabel(getName());
		res.setSamplerData(toString());
        res.setDataType(SampleResult.TEXT);
        // Bug 31184 - make sure encoding is specified
        res.setDataEncoding(System.getProperty("file.encoding"));

        // Assume we will be successful
        res.setSuccessful(true);


		res.sampleStart();
		DataSourceComponent pool = (DataSourceComponent) getThreadContext().getVariables().getObject(getDataSource());
		log.debug("DataSourceComponent: " + pool);
		Connection conn = null;
		Statement stmt = null;
		CallableStatement cs = null;

		try {

			if (pool == null)
				throw new SQLException("No pool created");

			// TODO: Consider creating a sub-result with the time to get the
			// connection.
			conn = pool.getConnection();
			stmt = conn.createStatement();

            // Based on query return value, get results
            String _queryType = getQueryType();
            if (SELECT.equals(_queryType)) {
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery(getQuery());
					Data data = getDataFromResultSet(rs);
					res.setResponseData(data.toString().getBytes());
				} finally {
					close(rs);
				}
			} else if (CALLABLE.equals(_queryType)) {
					cs = conn.prepareCall(getQuery());
					boolean hasResultSet = cs.execute();
					if (hasResultSet){
						ResultSet rs=cs.getResultSet();
						Data data = getDataFromResultSet(rs);
						res.setResponseData(data.toString().getBytes());
					} else {
						int updateCount = cs.getUpdateCount();
						String results = updateCount + " updates";
						res.setResponseData(results.getBytes());
					}
					//TODO process additional results (if any) using getMoreResults()
            } else if (UPDATE.equals(_queryType)) {
				stmt.execute(getQuery());
				int updateCount = stmt.getUpdateCount();
				String results = updateCount + " updates";
				res.setResponseData(results.getBytes());
            // TODO add support for PreparedStatments
            } else { // User provided incorrect query type
                String results="Unexpected query type: "+_queryType;
                res.setResponseMessage(results);
                res.setSuccessful(false);
			}

		} catch (SQLException ex) {
			log.error("Error in JDBC sampling", ex);
			res.setResponseMessage(ex.toString());
			res.setSuccessful(false);
		} finally {
			close(cs);
			close(stmt);
			close(conn);
		}

		res.sampleEnd();
		return res;
	}

	/**
	 * Gets a Data object from a ResultSet.
	 * 
	 * @param rs
	 *            ResultSet passed in from a database query
	 * @return a Data object
	 * @throws java.sql.SQLException
	 */
	private Data getDataFromResultSet(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		Data data = new Data();

		int numColumns = meta.getColumnCount();
		String[] dbCols = new String[numColumns];
		for (int i = 0; i < numColumns; i++) {
			dbCols[i] = meta.getColumnName(i + 1);
			data.addHeader(dbCols[i]);
		}

		while (rs.next()) {
			data.next();
			for (int i = 0; i < numColumns; i++) {
				Object o = rs.getObject(i + 1);
				if (o instanceof byte[]) {
					o = new String((byte[]) o);
				}
				data.addColumnValue(dbCols[i], o);
			}
		}
		return data;
	}

	public static void close(Connection c) {
		try {
			if (c != null) c.close();
		} catch (SQLException e) {
			log.warn("Error closing Connection", e);
		}
	}
	
	public static void close(Statement s) {
		try {
			if (s != null) s.close();
		} catch (SQLException e) {
			log.warn("Error closing Statement", e);
		}
	}

	public static void close(CallableStatement cs) {
		try {
			if (cs != null) cs.close();
		} catch (SQLException e) {
			log.warn("Error closing CallableStatement", e);
		}
	}

	public static void close(ResultSet rs) {
		try {
			if (rs != null) rs.close();
		} catch (SQLException e) {
			log.warn("Error closing ResultSet", e);
		}
	}

	public String getQuery() {
		return query;
	}

	public String toString() {
        StringBuffer sb = new StringBuffer(80);
        sb.append("[");
        sb.append(getQueryType());
        sb.append("] ");
        sb.append(getQuery());
		return sb.toString();
	}

	/**
	 * @param query
	 *            The query to set.
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @return Returns the dataSource.
	 */
	public String getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource
	 *            The dataSource to set.
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return Returns the queryType.
	 */
	public String getQueryType() {
		return queryType;
	}

	/**
	 * @param queryType The queryType to set.
	 */
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
}
