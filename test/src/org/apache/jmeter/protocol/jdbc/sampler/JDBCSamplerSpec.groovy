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
 */

package org.apache.jmeter.protocol.jdbc.sampler

import org.apache.jmeter.config.ConfigTestElement
import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.testelement.property.JMeterProperty
import spock.lang.Unroll

import java.sql.Connection
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Statement

@Unroll
class JDBCSamplerSpec extends JMeterSpec {

    def sut = new JDBCSampler()

    def "applies matches SimpleConfigGui"() {
        given:
            def mockConfig = Mock(ConfigTestElement)
            def mockProperty = Mock(JMeterProperty)
        when:
            def applies = sut.applies(mockConfig)
        then:
            1 * mockConfig.getProperty(_ as String) >> mockProperty
            1 * mockProperty.getStringValue() >> propertyValue
            applies == expectedApplies
            // this check will catch any future additions
            sut.APPLIABLE_CONFIG_CLASSES.size() == 1
        where:
            propertyValue                                  | expectedApplies
            "org.apache.jmeter.config.gui.SimpleConfigGui" | true
            "org.apache.jmeter.config.gui.SomethingElse"   | false
    }

    /* AbstractJDBCTestElement tests */

    def "execute with SELECT query"() {
        given:
            def conn = Mock(Connection)
            def sample = Mock(SampleResult)
            def stmt = Mock(Statement)
            def rs = Mock(ResultSet)
            def meta = Mock(ResultSetMetaData)
            sut.setQuery("SELECT")
        when:
            def response = sut.execute(conn, sample)
        then:
            1 * conn.createStatement() >> stmt
            1 * stmt.setQueryTimeout(0)
            1 * stmt.executeQuery(_ as String) >> rs
            1 * sample.latencyEnd()
            // getStringFromResultSet
            1 * rs.getMetaData() >> meta
            1 * rs.next()
            1 * rs.close() >> { throw new SQLException() }
            1 * stmt.close()
            // 1 * conn.close() // closed by JDBCSampler
            1 * meta.getColumnCount() >> 0
            response == [] as byte[]
    }

    def "Catches SQLException during Connection closing"() {
        given:
            def mockConnection = Mock(Connection)
        when:
            sut.close(mockConnection)
        then:
            1 * mockConnection.close() >> { throw new SQLException() }
            noExceptionThrown()
    }

    def "Catches SQLException during Statement closing"() {
        given:
            def mockStatement = Mock(Statement)
        when:
            sut.close(mockStatement)
        then:
            1 * mockStatement.close() >> { throw new SQLException() }
            noExceptionThrown()
    }

    def "Catches SQLException during ResultSet closing"() {
        given:
            def mockResultSet = Mock(ResultSet)
        when:
            sut.close(mockResultSet)
        then:
            1 * mockResultSet.close() >> { throw new SQLException() }
            noExceptionThrown()
    }

    def "getIntegerQueryTimeout returns #expectedTimeout from #initialTimeout"() {
        given:
            sut.setQueryTimeout(initialTimeout)
        when:
            def timeout = sut.getIntegerQueryTimeout()
        then:
            timeout == expectedTimeout
        where:
            initialTimeout | expectedTimeout
            "0"            | 0
            "1"            | 1
            "2147483647"   | Integer.MAX_VALUE
            "-1"           | -1
            "-2147483648"  | Integer.MIN_VALUE
            "2147483648"   | 0 // max int + 1
            "-2147483649"  | 0 // min int - 1
            "nan"          | 0
            ""             | 0
    }
}
