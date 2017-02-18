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
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for all JDBC test elements handling the basics of a SQL request.
 * 
 */
public abstract class AbstractJDBCTestElement extends AbstractTestElement implements TestStateListener{
    private static final long serialVersionUID = 235L;

    private static final Logger log = LoggerFactory.getLogger(AbstractJDBCTestElement.class);

    private static final String COMMA = ","; // $NON-NLS-1$
    private static final char COMMA_CHAR = ',';

    private static final String UNDERSCORE = "_"; // $NON-NLS-1$

    // String used to indicate a null value
    private static final String NULL_MARKER =
        JMeterUtils.getPropDefault("jdbcsampler.nullmarker","]NULL["); // $NON-NLS-1$

    private static final String INOUT = "INOUT"; // $NON-NLS-1$

    private static final String OUT = "OUT"; // $NON-NLS-1$

    // TODO - should the encoding be configurable?
    protected static final String ENCODING = StandardCharsets.UTF_8.name();

    // key: name (lowercase) from java.sql.Types; entry: corresponding int value
    private static final Map<String, Integer> mapJdbcNameToInt;
    // read-only after class init

    static {
        // based on e291. Getting the Name of a JDBC Type from javaalmanac.com
        // http://javaalmanac.com/egs/java.sql/JdbcInt2Str.html
        mapJdbcNameToInt = new HashMap<>();

        //Get all fields in java.sql.Types and store the corresponding int values
        Field[] fields = java.sql.Types.class.getFields();
        for (Field field : fields) {
            try {
                String name = field.getName();
                Integer value = (Integer) field.get(null);
                mapJdbcNameToInt.put(name.toLowerCase(java.util.Locale.ENGLISH), value);
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

    static final String RS_STORE_AS_STRING = "Store as String"; // $NON-NLS-1$
    static final String RS_STORE_AS_OBJECT = "Store as Object"; // $NON-NLS-1$
    static final String RS_COUNT_RECORDS = "Count Records"; // $NON-NLS-1$

    private String query = ""; // $NON-NLS-1$

    private String dataSource = ""; // $NON-NLS-1$

    private String queryType = SELECT;
    private String queryArguments = ""; // $NON-NLS-1$
    private String queryArgumentsTypes = ""; // $NON-NLS-1$
    private String variableNames = ""; // $NON-NLS-1$
    private String resultSetHandler = RS_STORE_AS_STRING; 
    private String resultVariable = ""; // $NON-NLS-1$
    private String queryTimeout = ""; // $NON-NLS-1$

    private static final int MAX_RETAIN_SIZE = JMeterUtils.getPropDefault("jdbcsampler.max_retain_result_size", 64 * 1024);

    /**
     * Creates a JDBCSampler.
     */
    protected AbstractJDBCTestElement() {
    }

    /**
     * Execute the test element.
     *
     * @param conn a {@link Connection}
     * @return the result of the execute command
     * @throws SQLException if a database error occurs
     * @throws IOException when I/O error occurs
     * @throws UnsupportedOperationException if the user provided incorrect query type 
     */
    protected byte[] execute(Connection conn) throws SQLException, IOException, UnsupportedOperationException {
        return execute(conn,  new SampleResult());
    }

    /**
     * Execute the test element.
     * Use the sample given as argument to set time to first byte in the "latency" field of the SampleResult.
     *
     * @param conn a {@link Connection}
     * @param sample a {@link SampleResult} to save the latency
     * @return the result of the execute command
     * @throws SQLException if a database error occurs
     * @throws IOException when I/O error occurs
     * @throws UnsupportedOperationException if the user provided incorrect query type
     */
    protected byte[] execute(Connection conn, SampleResult sample) throws SQLException, IOException, UnsupportedOperationException {
        log.debug("executing jdbc:{}", getQuery());
        // Based on query return value, get results
        String _queryType = getQueryType();
        if (SELECT.equals(_queryType)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(getIntegerQueryTimeout());
                ResultSet rs = null;
                try {
                    rs = stmt.executeQuery(getQuery());
                    sample.latencyEnd();
                    return getStringFromResultSet(rs).getBytes(ENCODING);
                } finally {
                    close(rs);
                }
            }
        } else if (CALLABLE.equals(_queryType)) {
            try (CallableStatement cstmt = getCallableStatement(conn)) {
                int[] out = setArguments(cstmt);
                // A CallableStatement can return more than 1 ResultSets
                // plus a number of update counts.
                boolean hasResultSet = cstmt.execute();
                sample.latencyEnd();
                String sb = resultSetsToString(cstmt,hasResultSet, out);
                return sb.getBytes(ENCODING);
            }
        } else if (UPDATE.equals(_queryType)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(getIntegerQueryTimeout());
                stmt.executeUpdate(getQuery());
                sample.latencyEnd();
                int updateCount = stmt.getUpdateCount();
                String results = updateCount + " updates";
                return results.getBytes(ENCODING);
            }
        } else if (PREPARED_SELECT.equals(_queryType)) {
            try (PreparedStatement pstmt = getPreparedStatement(conn)) {
                setArguments(pstmt);
                ResultSet rs = null;
                try {
                    rs = pstmt.executeQuery();
                    sample.latencyEnd();
                    return getStringFromResultSet(rs).getBytes(ENCODING);
                } finally {
                    close(rs);
                }
            }
        } else if (PREPARED_UPDATE.equals(_queryType)) {
            try (PreparedStatement pstmt = getPreparedStatement(conn)) {
                setArguments(pstmt);
                pstmt.executeUpdate();
                sample.latencyEnd();
                String sb = resultSetsToString(pstmt,false,null);
                return sb.getBytes(ENCODING);
            }
        } else if (ROLLBACK.equals(_queryType)){
            conn.rollback();
            sample.latencyEnd();
            return ROLLBACK.getBytes(ENCODING);
        } else if (COMMIT.equals(_queryType)){
            conn.commit();
            sample.latencyEnd();
            return COMMIT.getBytes(ENCODING);
        } else if (AUTOCOMMIT_FALSE.equals(_queryType)){
            conn.setAutoCommit(false);
            sample.latencyEnd();
            return AUTOCOMMIT_FALSE.getBytes(ENCODING);
        } else if (AUTOCOMMIT_TRUE.equals(_queryType)){
            conn.setAutoCommit(true);
            sample.latencyEnd();
            return AUTOCOMMIT_TRUE.getBytes(ENCODING);
        } else { // User provided incorrect query type
            throw new UnsupportedOperationException("Unexpected query type: "+_queryType);
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
            List<Object> outputValues = new ArrayList<>();
            CallableStatement cs = (CallableStatement) pstmt;
            sb.append("Output variables by position:\n");
            for(int i=0; i < out.length; i++){
                if (out[i]!=java.sql.Types.NULL){
                    Object o = cs.getObject(i+1);
                    outputValues.add(o);
                    sb.append("[");
                    sb.append(i+1);
                    sb.append("] ");
                    sb.append(o);
                    if( o instanceof java.sql.ResultSet && RS_COUNT_RECORDS.equals(resultSetHandler)) {
                        sb.append(" ").append(countRows((ResultSet) o)).append(" rows");
                    }
                    sb.append("\n");
                }
            }
            String[] varnames = getVariableNames().split(COMMA);
            if(varnames.length > 0) {
            JMeterVariables jmvars = getThreadContext().getVariables();
                for(int i = 0; i < varnames.length && i < outputValues.size(); i++) {
                    String name = varnames[i].trim();
                    if (name.length()>0){ // Save the value in the variable if present
                        Object o = outputValues.get(i);
                        if( o instanceof java.sql.ResultSet ) { 
                            putIntoVar(jmvars, name, (java.sql.ResultSet) o);
                        } else if (o instanceof java.sql.Clob) {
                            putIntoVar(jmvars, name, (java.sql.Clob) o);
                        } else if (o instanceof java.sql.Blob) {
                            putIntoVar(jmvars, name, (java.sql.Blob) o);
                        }
                        else {
                            jmvars.put(name, o == null ? null : o.toString());
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    private void putIntoVar(final JMeterVariables jmvars, final String name,
            final ResultSet resultSet) throws SQLException {
        if (RS_STORE_AS_OBJECT.equals(resultSetHandler)) {
            jmvars.putObject(name, resultSet);
        } else if (RS_COUNT_RECORDS.equals(resultSetHandler)) {
            jmvars.put(name, resultSet.toString() + " " + countRows(resultSet)
                    + " rows");
        } else {
            jmvars.put(name, resultSet.toString());
        }
    }

    private void putIntoVar(final JMeterVariables jmvars, final String name,
            final Clob clob) throws SQLException {
        try {
            if (clob.length() > MAX_RETAIN_SIZE) {
                try (Reader reader = clob.getCharacterStream(0,MAX_RETAIN_SIZE)) {
                    jmvars.put(
                            name,
                            IOUtils.toString(reader)
                            + "<result cut off, it is too big>");
                }
            } else {
                try (Reader reader = clob.getCharacterStream()) {
                    jmvars.put(name, IOUtils.toString(reader));
                }
            }
        } catch (IOException e) {
            log.warn("Could not read CLOB into {}", name, e);
        }
    }

    private void putIntoVar(final JMeterVariables jmvars, final String name,
            final Blob blob) throws SQLException {
        if (RS_STORE_AS_OBJECT.equals(resultSetHandler)) {
            try {
                long length = Math.max(blob.length(), MAX_RETAIN_SIZE);
                jmvars.putObject(name,
                        IOUtils.toByteArray(blob.getBinaryStream(0, length)));
            } catch (IOException e) {
                log.warn("Could not read BLOB into {} as object.", name, e);
            }
        } else if (RS_COUNT_RECORDS.equals(resultSetHandler)) {
            jmvars.put(name, blob.length() + " bytes");
        } else {
            try {
                long length = Math.max(blob.length(), MAX_RETAIN_SIZE);
                try (InputStream is = blob.getBinaryStream(0, length)) {
                    jmvars.put(name, IOUtils.toString(is, ENCODING));
                }
            } catch (IOException e) {
                log.warn("Can't convert BLOB to String using {}", ENCODING, e);
            }
        }
    }

    /**
     * Count rows in result set
     * @param resultSet {@link ResultSet}
     * @return number of rows in resultSet
     * @throws SQLException
     */
    private static int countRows(ResultSet resultSet) throws SQLException {
        return resultSet.last() ? resultSet.getRow() : 0;
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
                        setArgument(pstmt, argument, targetSqlType, i+1);
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
                throw new SQLException("Could not set argument no: "+(i+1)+" - missing parameter marker?", e);
            }
        }
        return outputs;
    }

    private void setArgument(PreparedStatement pstmt, String argument, int targetSqlType, int index) throws SQLException {
        switch (targetSqlType) {
        case Types.INTEGER:
            pstmt.setInt(index, Integer.parseInt(argument));
            break;
        case Types.DECIMAL:
        case Types.NUMERIC:
            pstmt.setBigDecimal(index, new BigDecimal(argument));
            break;
        case Types.DOUBLE:
        case Types.FLOAT:
            pstmt.setDouble(index, Double.parseDouble(argument));
            break;
        case Types.CHAR:
        case Types.LONGVARCHAR:
        case Types.VARCHAR:
            pstmt.setString(index, argument);
            break;
        case Types.BIT:
        case Types.BOOLEAN:
            pstmt.setBoolean(index, Boolean.parseBoolean(argument));
            break;
        case Types.BIGINT:
            pstmt.setLong(index, Long.parseLong(argument));
            break;
        case Types.DATE:
            pstmt.setDate(index, Date.valueOf(argument));
            break;
        case Types.REAL:
            pstmt.setFloat(index, Float.parseFloat(argument));
            break;
        case Types.TINYINT:
            pstmt.setByte(index, Byte.parseByte(argument));
            break;
        case Types.SMALLINT:
            pstmt.setShort(index, Short.parseShort(argument));
            break;
        case Types.TIMESTAMP:
            pstmt.setTimestamp(index, Timestamp.valueOf(argument));
            break;
        case Types.TIME:
            pstmt.setTime(index, Time.valueOf(argument));
            break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            pstmt.setBytes(index, argument.getBytes());
            break;
        case Types.NULL:
            pstmt.setNull(index, targetSqlType);
            break;
        default:
            pstmt.setObject(index, argument, targetSqlType);
        }
    }


    private static int getJdbcType(String jdbcType) throws SQLException {
        Integer entry = mapJdbcNameToInt.get(jdbcType.toLowerCase(java.util.Locale.ENGLISH));
        if (entry == null) {
            try {
                entry = Integer.decode(jdbcType);
            } catch (NumberFormatException e) {
                throw new SQLException("Invalid data type: "+jdbcType, e);
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
        PreparedStatement pstmt;
        if (callable) {
            pstmt = conn.prepareCall(getQuery()); // NOSONAR closed by caller
        } else {
            pstmt = conn.prepareStatement(getQuery()); // NOSONAR closed by caller
        }
        pstmt.setQueryTimeout(getIntegerQueryTimeout());
        return pstmt;
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
            sb.append(meta.getColumnLabel(i));
            if (i==numColumns){
                sb.append('\n');
            } else {
                sb.append('\t');
            }
        }
        

        JMeterVariables jmvars = getThreadContext().getVariables();
        String[] varNames = getVariableNames().split(COMMA);
        String resultVariable = getResultVariable().trim();
        List<Map<String, Object> > results = null;
        if(resultVariable.length() > 0) {
            results = new ArrayList<>();
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
                        row = new HashMap<>(numColumns);
                        results.add(row);
                    }
                    row.put(meta.getColumnLabel(i), o);
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
                if (i <= varNames.length) { // i starts at 1
                    String name = varNames[i - 1].trim();
                    if (name.length()>0){ // Save the value in the variable if present
                        jmvars.put(name+UNDERSCORE+j, o == null ? null : o.toString());
                    }
                }
            }
        }
        // Remove any additional values from previous sample
        for (String varName : varNames) {
            String name = varName.trim();
            if (name.length() > 0 && jmvars != null) {
                final String varCount = name + "_#"; // $NON-NLS-1$
                // Get the previous count
                String prevCount = jmvars.get(varCount);
                if (prevCount != null) {
                    int prev = Integer.parseInt(prevCount);
                    for (int n = j + 1; n <= prev; n++) {
                        jmvars.remove(name + UNDERSCORE + n);
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
            log.warn("Error closing Statement {}", s.toString(), e);
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
    
    /**
     * @return the integer representation queryTimeout
     */
    public int getIntegerQueryTimeout() {
        int timeout = 0;
        try {
            timeout = Integer.parseInt(queryTimeout);
        } catch (NumberFormatException nfe) {
            timeout = 0;
        }
        return timeout;
    }

    /**
     * @return the queryTimeout
     */
    public String getQueryTimeout() {
        return queryTimeout ;
    }

    /**
     * @param queryTimeout query timeout in seconds
     */
    public void setQueryTimeout(String queryTimeout) {
        this.queryTimeout = queryTimeout;
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
     * @return the resultSetHandler
     */
    public String getResultSetHandler() {
        return resultSetHandler;
    }

    /**
     * @param resultSetHandler the resultSetHandler to set
     */
    public void setResultSetHandler(String resultSetHandler) {
        this.resultSetHandler = resultSetHandler;
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
     * {@inheritDoc}
     * @see org.apache.jmeter.testelement.TestStateListener#testStarted()
     */
    @Override
    public void testStarted() {
        testStarted("");
    }

    /**
     * {@inheritDoc}
     * @see org.apache.jmeter.testelement.TestStateListener#testStarted(java.lang.String)
     */
    @Override
    public void testStarted(String host) {
    }

    /**
     * {@inheritDoc}
     * @see org.apache.jmeter.testelement.TestStateListener#testEnded()
     */
    @Override
    public void testEnded() {
        testEnded("");
    }

    /**
     * {@inheritDoc}
     * @see org.apache.jmeter.testelement.TestStateListener#testEnded(java.lang.String)
     */
    @Override
    public void testEnded(String host) {
    }

}
