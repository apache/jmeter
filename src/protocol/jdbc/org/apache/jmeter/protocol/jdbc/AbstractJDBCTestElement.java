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

package org.apache.jmeter.protocol.jdbc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A base class for all JDBC test elements handling the basics of a SQL request.
 * 
 */
public abstract class AbstractJDBCTestElement extends AbstractTestElement implements TestListener{
    private static final long serialVersionUID = 235L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String COMMA = ","; // $NON-NLS-1$
    private static final char COMMA_CHAR = ',';

    private static final String UNDERSCORE = "_"; // $NON-NLS-1$

    // This value is used for both the connection (perConnCache) and statement (preparedStatementMap) caches.
    // TODO - do they have to be the same size?
    private static final int MAX_ENTRIES =
        JMeterUtils.getPropDefault("jdbcsampler.cachesize",200); // $NON-NLS-1$

    // String used to indicate a null value
    private static final String NULL_MARKER =
        JMeterUtils.getPropDefault("jdbcsampler.nullmarker","]NULL["); // $NON-NLS-1$

    private static final String INOUT = "INOUT"; // $NON-NLS-1$

    private static final String OUT = "OUT"; // $NON-NLS-1$

    // TODO - should the encoding be configurable?
    protected static final String ENCODING = "UTF-8"; // $NON-NLS-1$

    // key: name (lowercase) from java.sql.Types; entry: corresponding int value
    private static final Map<String, Integer> mapJdbcNameToInt;
    // read-only after class init

    static {
        // based on e291. Getting the Name of a JDBC Type from javaalmanac.com
        // http://javaalmanac.com/egs/java.sql/JdbcInt2Str.html
        mapJdbcNameToInt = new HashMap<String, Integer>();

        //Get all fields in java.sql.Types and store the corresponding int values
        Field[] fields = java.sql.Types.class.getFields();
        for (int i=0; i<fields.length; i++) {
            try {
                String name = fields[i].getName();
                Integer value = (Integer)fields[i].get(null);
                mapJdbcNameToInt.put(name.toLowerCase(java.util.Locale.ENGLISH),value);
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
    private String variableNames = ""; // $NON-NLS-1$
    private String resultVariable = "";

    /**
     *  Cache of PreparedStatements stored in a per-connection basis. Each entry of this
     *  cache is another Map mapping the statement string to the actual PreparedStatement.
     *  The cache has a fixed size of MAX_ENTRIES and it will throw away all PreparedStatements
     *  from the least recently used connections.
     */
    private static final Map<Connection, Map<String, PreparedStatement>> perConnCache =
        Collections.synchronizedMap(new LinkedHashMap<Connection, Map<String, PreparedStatement>>(MAX_ENTRIES){
        private static final long serialVersionUID = 1L;
        @Override
        protected boolean removeEldestEntry(Map.Entry<Connection, Map<String, PreparedStatement>> arg0) {
            if (size() > MAX_ENTRIES) {
                final  Map<String, PreparedStatement> value = arg0.getValue();
                closeAllStatements(value.values());
                return true;
            }
            return false;
        }
    });

    /**
     * Creates a JDBCSampler.
     */
    protected AbstractJDBCTestElement() {
    }
    
    /**
     * Execute the test element.
     * 
     * @param res a {@link SampleResult} in case the test should sample; <code>null</code> if only execution is requested
     * @throws UnsupportedOperationException if the user provided incorrect query type 
     */
    protected byte[] execute(Connection conn) throws SQLException, UnsupportedEncodingException, IOException, UnsupportedOperationException {
        log.debug("executing jdbc");
        Statement stmt = null;
        
        try {
            // Based on query return value, get results
            String _queryType = getQueryType();
            if (SELECT.equals(_queryType)) {
                stmt = conn.createStatement();
                ResultSet rs = null;
                try {
                    rs = stmt.executeQuery(getQuery());
                    return getStringFromResultSet(rs).getBytes(ENCODING);
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
                return sb.getBytes(ENCODING);
            } else if (UPDATE.equals(_queryType)) {
                stmt = conn.createStatement();
                stmt.executeUpdate(getQuery());
                int updateCount = stmt.getUpdateCount();
                String results = updateCount + " updates";
                return results.getBytes(ENCODING);
            } else if (PREPARED_SELECT.equals(_queryType)) {
                PreparedStatement pstmt = getPreparedStatement(conn);
                setArguments(pstmt);
                boolean hasResultSet = pstmt.execute();
                String sb = resultSetsToString(pstmt,hasResultSet,null);
                return sb.getBytes(ENCODING);
            } else if (PREPARED_UPDATE.equals(_queryType)) {
                PreparedStatement pstmt = getPreparedStatement(conn);
                setArguments(pstmt);
                pstmt.executeUpdate();
                String sb = resultSetsToString(pstmt,false,null);
                return sb.getBytes(ENCODING);
            } else if (ROLLBACK.equals(_queryType)){
                conn.rollback();
                return ROLLBACK.getBytes(ENCODING);
            } else if (COMMIT.equals(_queryType)){
                conn.commit();
                return COMMIT.getBytes(ENCODING);
            } else if (AUTOCOMMIT_FALSE.equals(_queryType)){
                conn.setAutoCommit(false);
                return AUTOCOMMIT_FALSE.getBytes(ENCODING);
            } else if (AUTOCOMMIT_TRUE.equals(_queryType)){
                conn.setAutoCommit(true);
                return AUTOCOMMIT_TRUE.getBytes(ENCODING);
            } else { // User provided incorrect query type
                throw new UnsupportedOperationException("Unexpected query type: "+_queryType);
            }
        } finally {
            close(stmt);
        }
    }

    private String resultSetsToString(PreparedStatement pstmt, boolean result, int[] out) throws SQLException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        int updateCount = 0;
        if (!result) {
            updateCount = pstmt.getUpdateCount();
        }
        do {
            if (result) {
                ResultSet rs = null;
                try {
                    rs = pstmt.getResultSet();
                    sb.append(getStringFromResultSet(rs)).append("\n"); // $NON-NLS-1$
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


    private int[] setArguments(PreparedStatement pstmt) throws SQLException, IOException {
        if (getQueryArguments().trim().length()==0) {
            return new int[]{};
        }
        String[] arguments = CSVSaveService.csvSplitString(getQueryArguments(), COMMA_CHAR);
        String[] argumentsTypes = getQueryArgumentsTypes().split(COMMA);
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
        Integer entry = mapJdbcNameToInt.get(jdbcType.toLowerCase(java.util.Locale.ENGLISH));
        if (entry == null) {
            try {
                entry = Integer.decode(jdbcType);
            } catch (NumberFormatException e) {
                throw new SQLException("Invalid data type: "+jdbcType);
            }
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
        Map<String, PreparedStatement> preparedStatementMap = perConnCache.get(conn);
        if (null == preparedStatementMap ) {
            // MRU PreparedStatements cache.
            preparedStatementMap = Collections.synchronizedMap(new LinkedHashMap<String, PreparedStatement>(MAX_ENTRIES) {
                private static final long serialVersionUID = 240L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, PreparedStatement> arg0) {
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
            });
            perConnCache.put(conn, preparedStatementMap);
        }
        PreparedStatement pstmt = preparedStatementMap.get(getQuery());
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

    private static void closeAllStatements(Collection<PreparedStatement> collection) {
        for (PreparedStatement pstmt : collection) {
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
     * @throws UnsupportedEncodingException
     */
    private String getStringFromResultSet(ResultSet rs) throws SQLException, UnsupportedEncodingException {
        ResultSetMetaData meta = rs.getMetaData();

        StringBuilder sb = new StringBuilder();

        int numColumns = meta.getColumnCount();
        for (int i = 1; i <= numColumns; i++) {
            sb.append(meta.getColumnName(i));
            if (i==numColumns){
                sb.append('\n');
            } else {
                sb.append('\t');
            }
        }
        

        JMeterVariables jmvars = getThreadContext().getVariables();
        String varnames[] = getVariableNames().split(COMMA);
        String resultVariable = getResultVariable().trim();
        List<Map<String, Object> > results = null;
        if(resultVariable.length() > 0) {
            results = new ArrayList<Map<String,Object> >();
            jmvars.putObject(resultVariable, results);
        }
        int j = 0;
        while (rs.next()) {
            Map<String, Object> row = null;
            j++;
            for (int i = 1; i <= numColumns; i++) {
                Object o = rs.getObject(i);
                if(results != null) {
                    if(row == null) {
                        row = new HashMap<String, Object>(numColumns);
                        results.add(row);
                    }
                    row.put(meta.getColumnName(i), o);
                }
                if (o instanceof byte[]) {
                    o = new String((byte[]) o, ENCODING);
                }
                sb.append(o);
                if (i==numColumns){
                    sb.append('\n');
                } else {
                    sb.append('\t');
                }
                if (i <= varnames.length) { // i starts at 1
                    String name = varnames[i - 1].trim();
                    if (name.length()>0){ // Save the value in the variable if present
                        jmvars.put(name+UNDERSCORE+j, o == null ? null : o.toString());
                    }
                }
            }
        }
        // Remove any additional values from previous sample
        for(int i=0; i < varnames.length; i++){
            String name = varnames[i].trim();
            if (name.length()>0 && jmvars != null){
                final String varCount = name+"_#"; // $NON-NLS-1$
                // Get the previous count
                String prevCount = jmvars.get(varCount);
                if (prevCount != null){
                    int prev = Integer.parseInt(prevCount);
                    for (int n=j+1; n <= prev; n++ ){
                        jmvars.remove(name+UNDERSCORE+n);
                    }
                }
                jmvars.put(varCount, Integer.toString(j)); // save the current count
            }
        }

        return sb.toString();
    }

    public static void close(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            log.warn("Error closing Connection", e);
        }
    }

    public static void close(Statement s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (SQLException e) {
            log.warn("Error closing Statement " + s.toString(), e);
        }
    }

    public static void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.warn("Error closing ResultSet", e);
        }
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(80);
        sb.append("["); // $NON-NLS-1$
        sb.append(getQueryType());
        sb.append("] "); // $NON-NLS-1$
        sb.append(getQuery());
        sb.append("\n");
        sb.append(getQueryArguments());
        sb.append("\n");
        sb.append(getQueryArgumentsTypes());
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

    /**
     * @return the variableNames
     */
    public String getVariableNames() {
        return variableNames;
    }

    /**
     * @param variableNames the variableNames to set
     */
    public void setVariableNames(String variableNames) {
        this.variableNames = variableNames;
    }

    /**
     * @return the resultVariable
     */
    public String getResultVariable() {
        return resultVariable ;
    }

    /**
     * @param resultVariable the variable name in which results will be stored
     */
    public void setResultVariable(String resultVariable) {
        this.resultVariable = resultVariable;
    }    

    /** 
     * {@inheritDoc}}
	 */
	@Override
	public List<String> getSearchableTokens() throws Exception {
		List<String> result = super.getSearchableTokens();
		Set<String> properties = new HashSet<String>();
		properties.addAll(Arrays.asList(new String[]{
			"dataSource",
			"query",
			"queryArguments",
			"queryArgumentsTypes",
			"queryType",
			"resultVariable",
			"variableNames"
		}));
		addPropertiesValues(result, properties);
        return result;
	}

	/** 
	 * {@inheritDoc}
	 * @see org.apache.jmeter.testelement.TestListener#testStarted()
	 */
	public void testStarted() {
		testStarted("");
	}

	/**
	 * {@inheritDoc}
	 * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
	 */
	public void testStarted(String host) {
		cleanCache();
	}

	/**
	 * {@inheritDoc}
	 * @see org.apache.jmeter.testelement.TestListener#testEnded()
	 */
	public void testEnded() {
		testEnded("");
	}

	/**
	 * {@inheritDoc}
	 * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
	 */
	public void testEnded(String host) {
		cleanCache();		
	}
	
	/**
	 * Clean cache of PreparedStatements
	 */
	private static final void cleanCache() {
		for (Map.Entry<Connection, Map<String, PreparedStatement>> element : perConnCache.entrySet()) {
			closeAllStatements(element.getValue().values());
		}
		perConnCache.clear();
	}

	/**
	 * {@inheritDoc}
	 * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
	 */
	public void testIterationStart(LoopIterationEvent event) {
		// NOOP
	}  
}