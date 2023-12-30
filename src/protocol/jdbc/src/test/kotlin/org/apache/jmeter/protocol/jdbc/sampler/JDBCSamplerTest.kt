/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.jdbc.sampler

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verifyOrder
import org.apache.jmeter.config.ConfigTestElement
import org.apache.jmeter.config.gui.SimpleConfigGui
import org.apache.jmeter.protocol.jdbc.executeForTest
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.testelement.TestElementSchema
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.sql.Connection
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Statement

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JDBCSamplerTest {

    lateinit var sut: JDBCSampler

    @BeforeEach
    fun setup() {
        sut = JDBCSampler()
    }

    @Test
    fun `applies matches SimpleConfigGui`() {
        val configElement = SimpleConfigGui().createTestElement() as ConfigTestElement
        assertTrue(
            sut.applies(configElement),
            "JDBCSampler should apply to SimpleConfigGui"
        )
        configElement[TestElementSchema.guiClass] = "org.apache.jmeter.config.gui.SomethingElse"
        assertFalse(
            sut.applies(configElement),
            "JDBCSampler should not apply to gui.SomethingElse"
        )
    }

    /* AbstractJDBCTestElement tests */

    @Test
    fun `execute with SELECT query`() {
        val meta = mockk<ResultSetMetaData> {
            every { columnCount } returns 0
        }
        val rs = mockk<ResultSet> {
            every { metaData } returns meta
            every { close() } throws SQLException()
            every { next() } returns false
        }
        val stmt = mockk<Statement> {
            every { executeQuery(any()) } returns rs
            justRun { queryTimeout = any() }
            justRun { maxRows = any() }
            justRun { close() }
        }
        val conn = mockk<Connection> {
            every { createStatement() } returns stmt
        }
        val sample = mockk<SampleResult> {
            justRun { latencyEnd() }
        }

        sut.query = "SELECT"
        sut.resultSetMaxRows = "10"
        val response = sut.executeForTest(conn, sample)

        verifyOrder {
            conn.createStatement()
            stmt.queryTimeout = 0
            stmt.maxRows = 10
            stmt.executeQuery(any())
            sample.latencyEnd()
            // getStringFromResultSet
            rs.metaData
            meta.columnCount
            rs.next()
            rs.close()
            stmt.close()
            // conn.close() // closed by JDBCSampler
        }
        assertArrayEquals(byteArrayOf(), response, "response")
    }

    @Test
    fun `Catches SQLException during Connection closing`() {
        val mockConnection = mockk<Connection> {
            every { close() } throws SQLException()
        }
        JDBCSampler.close(mockConnection)
    }

    @Test
    fun `Catches SQLException during Statement closing`() {
        val mockStatement = mockk<Statement> {
            every { close() } throws SQLException()
        }
        JDBCSampler.close(mockStatement)
    }

    @Test
    fun `Catches SQLException during ResultSet closing`() {
        val mockStatement = mockk<ResultSet> {
            every { close() } throws SQLException()
        }
        JDBCSampler.close(mockStatement)
    }

    data class GetIntegerQueryTimeoutCase(val initialTimeout: String, val expectedTimeout: Int, val message: String? = null)

    @ParameterizedTest
    @MethodSource("getIntegerQueryTimeoutCases")
    fun `getIntegerQueryTimeout returns #expectedTimeout from #initialTimeout`(case: GetIntegerQueryTimeoutCase) {
        sut.queryTimeout = case.initialTimeout
        assertEquals(case.expectedTimeout, sut.getIntegerQueryTimeout(), case.message)
    }

    fun getIntegerQueryTimeoutCases() = listOf(
        GetIntegerQueryTimeoutCase("0", 0),
        GetIntegerQueryTimeoutCase("1", 1),
        GetIntegerQueryTimeoutCase("2147483647", Integer.MAX_VALUE),
        GetIntegerQueryTimeoutCase("-1", -1),
        GetIntegerQueryTimeoutCase("-2147483648", Integer.MIN_VALUE),
        GetIntegerQueryTimeoutCase("2147483648", 0, "max int + 1"),
        GetIntegerQueryTimeoutCase("-2147483649", 0, "min int - 1"),
        GetIntegerQueryTimeoutCase("nan", 0),
        GetIntegerQueryTimeoutCase("", 0),
    )
}
