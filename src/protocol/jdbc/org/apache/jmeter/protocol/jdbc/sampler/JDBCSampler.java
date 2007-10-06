/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler which understands JDBC database requests.
 * 
 */
public class JDBCSampler extends AbstractSampler implements TestBean {
	private static final Logger log = LoggingManager.getLoggerForClass();

	// This value is used for both the connection (perConnCache) and statement (preparedStatementMap) caches.
	// TODO - do they have to be the same size?
	private static final int MAX_ENTRIES = 
		JMeterUtils.getPropDefault("jdbcsampler.cachesize",200); // $NON-NLS-1$

	// String used to indicate a null value
	private static final String NULL_MARKER = 
		JMeterUtils.getPropDefault("jdbcsampler.nullmarker","]NULL["); // $NON-NLS-1$

	private static final String INOUT = "INOUT"; // $NON-NLS-1$

	private static final String OUT = "OUT"; // $NON-NLS-1$

	private static final Map mapJdbcNameToInt;

    static {
        // based on e291. Getting the Name of a JDBC Type from javaalmanac.com
        // http://javaalmanac.com/egs/java.sql/JdbcInt2Str.html
		mapJdbcNameToInt = new HashMap();
		
		//Get all fields in java.sql.Types and store the corresponding int values
		Field[] fields = java.sql.Types.class.getFields();
        for (int i=0; i<fields.length; i++) {
            try {
                String name = fields[i].getName();                
                Integer value = (Integer)fields[i].get(null);
                mapJdbcNameToInt.put(name.toLowerCase(),value);
            } catch (IllegalAccessException e) {
            	throw new RuntimeException(e); // should not happen
            }
        }    		
    }

    // Query types (used to communicate with GUI)
    // N.B. These must not be changed, as they are used in the JMX files
	static final String SELECT   = "Select Statement"; // $NON-NLS-1$
	static final String UPDATE   = "Update Statement"; // $NON-NLS-1$
	static final String CALLABLE = "Callable Statement"; // $NON-NLS-1$
	static final String PREPARED_SELECT = "Prepared Select Statement"; // $NON-NLS-1$
	static final String PREPARED_UPDATE = "Prepared Update Statement"; // $NON-NLS-1$
	static final String COMMIT   = "Commit"; // $NON-NLS-1$
	static final String ROLLBACK = "Rollback"; // $NON-NLS-1$
	static final String AUTOCOMMIT_FALSE = "AutoCommit(false)"; // $NON-NLS-1$
	static final String AUTOCOMMIT_TRUE  = "AutoCommit(true)"; // $NON-NLS-1$

	private String query = ""; // $NON-NLS-1$

	private String dataSource = ""; // $NON-NLS-1$

	private String queryType = SELECT;
	private String queryArguments = ""; // $NON-NLS-1$
	private String queryArgumentsTypes = ""; // $NON-NLS-1$
	
	/**
	 *  Cache of PreparedStatements stored in a per-connection basis. Each entry of this 
	 *  cache is another Map mapping the statement string to the actual PreparedStatement.
	 *  The cache has a fixed size of MAX_ENTRIES and it will throw aways all PreparedStatements 
	 *  from the least recently used connections.  
	 */
	private static Map perConnCache = new LinkedHashMap(MAX_ENTRIES){
		private static final long serialVersionUID = 1L;
		protected boolean removeEldestEntry(java.util.Map.Entry arg0) {
			if (size() > MAX_ENTRIES) {
				final Object value = arg0.getValue();
				if (value instanceof Map) {
					closeAllStatements(((Map)value).values());
				}
				return true;
			}
			return false;
		}
	};

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
        res.setDataEncoding(System.getProperty("file.encoding")); // $NON-NLS-1$

        // Assume we will be successful
        res.setSuccessful(true);


		res.sampleStart();
		Connection conn = null;
		Statement stmt = null;

		try {

			try {
			    conn = DataSourceElement.getConnection(getDataSource());
			} finally {
				res.latencyEnd(); // use latency to measure connection time
			}
			res.setResponseHeaders(conn.toString());

            // Based on query return value, get results
            String _queryType = getQueryType();
            if (SELECT.equals(_queryType)) {
            	stmt = conn.createStatement();
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery(getQuery());
					Data data = getDataFromResultSet(rs);
					res.setResponseData(data.toString().getBytes());
				} finally {
					close(rs);
				}
            } else if (CALLABLE.equals(_queryType)) {
            	CallableStatement cstmt = getCallableStatement(conn);
            	int out[]=setArguments(cstmt);
            	// A CallableStatement can return more than 1 ResultSets
            	// plus a number of update counts. 
            	boolean hasResultSet = cstmt.execute();
            	String sb = resultSetsToString(cstmt,hasResultSet, out);
            	res.setResponseData(sb.toString().getBytes());       	
            } else if (UPDATE.equals(_queryType)) {
            	stmt = conn.createStatement();
				stmt.executeUpdate(getQuery());
				int updateCount = stmt.getUpdateCount();
				String results = updateCount + " updates";
				res.setResponseData(results.getBytes());
            } else if (PREPARED_SELECT.equals(_queryType)) {
            	PreparedStatement pstmt = getPreparedStatement(conn);
            	setArguments(pstmt);
            	pstmt.executeQuery();
            	String sb = resultSetsToString(pstmt,true,null);
            	res.setResponseData(sb.toString().getBytes());
            } else if (PREPARED_UPDATE.equals(_queryType)) {
            	PreparedStatement pstmt = getPreparedStatement(conn);
            	setArguments(pstmt);
            	pstmt.executeUpdate();
				String sb = resultSetsToString(pstmt,false,null);
            	res.setResponseData(sb.toString().getBytes());
            } else if (ROLLBACK.equals(_queryType)){
            	conn.rollback();
            	res.setResponseData(ROLLBACK.getBytes());
            } else if (COMMIT.equals(_queryType)){
            	conn.commit();
            	res.setResponseData(COMMIT.getBytes());
            } else if (AUTOCOMMIT_FALSE.equals(_queryType)){
            	conn.setAutoCommit(false);
            	res.setResponseData(AUTOCOMMIT_FALSE.getBytes());
            } else if (AUTOCOMMIT_TRUE.equals(_queryType)){
            	conn.setAutoCommit(true);
            	res.setResponseData(AUTOCOMMIT_TRUE.getBytes());
            } else { // User provided incorrect query type
                String results="Unexpected query type: "+_queryType;
                res.setResponseMessage(results);
                res.setSuccessful(false);
			}

		} catch (SQLException ex) {
			final String errCode = Integer.toString(ex.getErrorCode());
			res.setResponseMessage(ex.toString());
			res.setResponseCode(ex.getSQLState()+ " " +errCode);
			res.setSuccessful(false);
		} finally {
			close(stmt);
			close(conn);
		}

		// TODO: process warnings? Set Code and Message to success?
		res.sampleEnd();
		return res;
	}

	private String resultSetsToString(PreparedStatement pstmt, boolean result, int[] out) throws SQLException {
		StringBuffer sb = new StringBuffer();
		sb.append("\n"); // $NON-NLS-1$
		int updateCount = 0;
		if (!result) {
			updateCount = pstmt.getUpdateCount();
		}
		do {
			if (result) {
				ResultSet rs = null;
				try {
					rs = pstmt.getResultSet();
					Data data = getDataFromResultSet(rs);
					sb.append(data.toString()).append("\n"); // $NON-NLS-1$
				} finally {
					close(rs);
				}
			} else {
				sb.append(updateCount).append(" updates.\n");
			}
			result = pstmt.getMoreResults();
			if (!result) {
				updateCount = pstmt.getUpdateCount();
			}
		} while (result || (updateCount != -1));
		if (out!=null && pstmt instanceof CallableStatement){
			CallableStatement cs = (CallableStatement) pstmt;
			sb.append("Output variables by position:\n");
			for(int i=0; i < out.length; i++){
				if (out[i]!=java.sql.Types.NULL){
					sb.append("[");
					sb.append(i+1);
					sb.append("] ");
					sb.append(cs.getObject(i+1));
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}


	private int[] setArguments(PreparedStatement pstmt) throws SQLException {
		if (getQueryArguments().trim().length()==0) {
			return new int[]{};
		}
		String[] arguments = getQueryArguments().split(","); // $NON-NLS-1$
		String[] argumentsTypes = getQueryArgumentsTypes().split(","); // $NON-NLS-1$
		if (arguments.length != argumentsTypes.length) {
			throw new SQLException("number of arguments ("+arguments.length+") and number of types ("+argumentsTypes.length+") are not equal");
		}
		int[] outputs= new int[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			String argument = arguments[i];
			String argumentType = argumentsTypes[i];
			String[] arg = argumentType.split(" ");
			String inputOutput="";
			if (arg.length > 1) {
				argumentType = arg[1];
				inputOutput=arg[0];
			}
		    int targetSqlType = getJdbcType(argumentType);
		    try {
		    	if (!OUT.equalsIgnoreCase(inputOutput)){
			    	if (argument.equals(NULL_MARKER)){
			    		pstmt.setNull(i+1, targetSqlType);
			    	} else {
			    		pstmt.setObject(i+1, argument, targetSqlType);
			    	}
		    	}
		    	if (OUT.equalsIgnoreCase(inputOutput)||INOUT.equalsIgnoreCase(inputOutput)) {
		    		CallableStatement cs = (CallableStatement) pstmt;
		    		cs.registerOutParameter(i+1, targetSqlType);
		    		outputs[i]=targetSqlType;
		    	} else {
		    		outputs[i]=java.sql.Types.NULL; // can't have an output parameter type null
		    	}
			} catch (NullPointerException e) { // thrown by Derby JDBC (at least) if there are no "?" markers in statement
				throw new SQLException("Could not set argument no: "+(i+1)+" - missing parameter marker?");
			}
		}
		return outputs;
	}
    
    
    private static int getJdbcType(String jdbcType) throws SQLException {
    	Integer entry = (Integer)mapJdbcNameToInt.get(jdbcType.toLowerCase());
    	if (entry == null) {
    		throw new SQLException("Invalid data type: "+jdbcType);
    	}
		return (entry).intValue();
    }
	

	private CallableStatement getCallableStatement(Connection conn) throws SQLException {
		return (CallableStatement) getPreparedStatement(conn,true);
		
	}
	private PreparedStatement getPreparedStatement(Connection conn) throws SQLException {
		return getPreparedStatement(conn,false);
	}

	private PreparedStatement getPreparedStatement(Connection conn, boolean callable) throws SQLException {
		Map preparedStatementMap = (Map) perConnCache.get(conn); 
		if (null == preparedStatementMap ) {
		    // MRU PreparedStatements cache. 
			preparedStatementMap = new LinkedHashMap(MAX_ENTRIES) {
				protected boolean removeEldestEntry(java.util.Map.Entry arg0) {
					final int theSize = size();
					if (theSize > MAX_ENTRIES) {
						Object value = arg0.getValue();
						if (value instanceof PreparedStatement) {
							PreparedStatement pstmt = (PreparedStatement) value;
							close(pstmt);
						}
						return true;
					}
					return false;
				}
			};
			perConnCache.put(conn, preparedStatementMap);
		}
		PreparedStatement pstmt = (PreparedStatement) preparedStatementMap.get(getQuery());
		if (null == pstmt) {
			if (callable) {
				pstmt = conn.prepareCall(getQuery());
			} else {
				pstmt = conn.prepareStatement(getQuery());
			}
			preparedStatementMap.put(getQuery(), pstmt);
		}
		pstmt.clearParameters();
		return pstmt;
	}

	private static void closeAllStatements(Collection collection) {
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			PreparedStatement pstmt = (PreparedStatement) iterator.next();
			close(pstmt);
		}		
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
			log.warn("Error closing Statement " + s.toString(), e);
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
        sb.append("["); // $NON-NLS-1$
        sb.append(getQueryType());
        sb.append("] "); // $NON-NLS-1$
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

	public String getQueryArguments() {
		return queryArguments;
	}

	public void setQueryArguments(String queryArguments) {
		this.queryArguments = queryArguments;
	}

	public String getQueryArgumentsTypes() {
		return queryArgumentsTypes;
	}

	public void setQueryArgumentsTypes(String queryArgumentsType) {
		this.queryArgumentsTypes = queryArgumentsType;
	}
}
