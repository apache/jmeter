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

package org.apache.jmeter.services

import org.apache.jmeter.junit.JMeterTestCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.EOFException
import java.io.File
import java.io.IOException

class FileServerTest : JMeterTestCase() {
    val sut = FileServer()

    val testFile = getResourceFilePath("testfiles/unit/FileServerSpec.csv")
    val emptyFile = getResourceFilePath("testfiles/empty.csv")
    val bomFile = getResourceFilePath("testfiles/bomData.csv")

    data class SetBaseForScriptCase(val file: File, val expectedBaseDirRelative: String)

    companion object {
        @JvmStatic
        fun setBaseForScript(): List<SetBaseForScriptCase> {
            val baseFile = File(FileServer.getDefaultBase())
            return listOf(
                SetBaseForScriptCase(baseFile, "."),
                SetBaseForScriptCase(baseFile.getParentFile(), "."),
                SetBaseForScriptCase(File(baseFile.getParentFile(), "abcd/defg.jmx"), "."),
                SetBaseForScriptCase(File(baseFile, "abcd/defg.jmx"), "abcd"),
            )
        }
    }

    @BeforeEach
    fun setup() {
        sut.resetBase()
    }

    @AfterEach
    fun tearDown() {
        sut.closeFiles()
    }

    @Test
    fun `reading a non-existent file throws an exception`() {
        assertThrows<IOException> {
            sut.readLine("test")
        }
    }

    @Test
    fun `writing to a non-exisent file throws an exception`() {
        assertThrows<IOException> {
            sut.write("test", "")
        }
    }

    @Test
    fun `no files should be open following resetBase`() {
        assertNoFilesOpen()
    }

    @Test
    fun `closing unrecognised files are ignored`() {
        sut.closeFile("xxx")
        assertNoFilesOpen()
    }

    @Test
    fun `file is not opened until read from`() {
        sut.reserveFile(testFile) // Does not open file
        assertNoFilesOpen()
        assertEquals("a1,b1,c1,d1", sut.readLine(testFile)) {
            "readLine($testFile)"
        }
        assertFilesOpen()
    }

    private fun assertNoFilesOpen() {
        assertFalse(sut.filesOpen(), "filesOpen")
    }

    private fun assertFilesOpen() {
        assertTrue(sut.filesOpen(), "filesOpen")
    }

    @Test
    fun `reading lines loops to start once last line is read`() {
        sut.reserveFile(testFile)
        val firstPass = Array(4) { sut.readLine(testFile) }
        val secondPass = Array(4) { sut.readLine(testFile) }
        assertArrayEquals(firstPass, secondPass)
    }

    @Test
    fun `cannot write to reserved file after reading`() {
        sut.reserveFile(testFile)
        sut.readLine(testFile)
        assertThrows<IOException> {
            sut.write(testFile, "")
        }
    }

    @Test
    fun `closing reserved file after reading resets`() {
        sut.reserveFile(testFile)
        sut.readLine(testFile)
        sut.closeFile(testFile) // does not remove the entry
        assertNoFilesOpen()
        assertEquals("a1,b1,c1,d1", sut.readLine(testFile), "re-read first line")
        assertFilesOpen()
    }

    @Test
    fun `closeFiles() prevents reading of reserved file`() {
        sut.reserveFile(testFile)
        sut.readLine(testFile)
        sut.closeFiles() // removes all entries
        assertThrows<IOException> {
            sut.readLine(testFile)
        }
        assertNoFilesOpen()
    }

    @Test
    fun `baseDir is the defaultBasedir`() {
        assertEquals(FileServer.getDefaultBase(), sut.getBaseDir()) {
            "getBaseDir should be FileServer.getDefaultBase()"
        }
    }

    @Test
    fun `setBaseDir doesn't error when no files are open`() {
        sut.setBasedir("testfiles/unit/FileServerSpec.csv")

        val result = sut.baseDir.replace("\\", "/")
        if (!result.endsWith("testfiles/unit")) {
            fail("baseDir should start with testfiles/unit, but was $result")
        }
    }

    // TODO: what about throwing an exception in setBaseDir?
    @Test
    fun `setBaseDir doesn't set base when passed a directory`() {
        val dir = "does-not-exist"
        sut.setBasedir(dir)
        assertThrows<NullPointerException> {
            sut.baseDir.endsWith(dir)
        }
    }

    @Test
    fun `cannot set baseDir when files are open`() {
        sut.reserveFile(testFile)
        assertEquals("a1,b1,c1,d1", sut.readLine(testFile), "sut.readLine($testFile)")
        assertThrows<IllegalStateException> {
            sut.setBasedir("testfiles")
        }
    }

    @ParameterizedTest
    @MethodSource("setBaseForScript")
    fun `setting base to #file gives getBaseDirRelative == #expectedBaseDirRelative`(case: SetBaseForScriptCase) {
        sut.setBaseForScript(case.file)
        assertEquals(case.expectedBaseDirRelative, sut.getBaseDirRelative().toString())
    }

    @Test
    fun `non-existent filename to reserveFile will throw exception`() {
        val missing = "no-such-file"
        val alias = "missing"
        val charsetName = "UTF-8"
        val hasHeader = true
        val ex = assertThrows<IllegalArgumentException> {
            sut.reserveFile(missing, charsetName, alias, hasHeader)
        }
        assertEquals("Could not read file header line for file $missing", ex.message) {
            "ex.message"
        }
        assertEquals("File $missing must exist and be readable", ex.cause?.message) {
            "ex.cause?.message"
        }
    }

    @Test
    fun `reserving a file with no header will throw an exception if the header is expected`() {
        val alias = "empty"
        val charsetName = "UTF-8"
        val e = assertThrows<IllegalArgumentException> {
            sut.reserveFile(emptyFile, charsetName, alias, true)
        }
        if (e.cause !is EOFException) {
            fail("reserveFile(emptyFile) should throw IllegalArgumentException(cause=EOFException), got cause=${e.cause}")
        }
    }

    @Test
    fun `resolvedFile returns absolute and relative files`() {
        val testFile = File(emptyFile)
        assertEquals(
            testFile.getCanonicalFile(),
            sut.getResolvedFile(testFile.absolutePath).getCanonicalFile(),
            "sut.getResolvedFile(testFile.absolutePath)"
        )
        // relative
        assertEquals(
            testFile.getCanonicalFile(),
            sut.getResolvedFile(testFile.getParentFile().path + "/../testfiles/empty.csv")
                .getCanonicalFile(),
            "sut.getResolvedFile(testFile.getParentFile().path + \"/../testfiles/empty.csv\")"
        )
    }

    @Test
    fun `resolvedFile returns relative files with BaseForScript set`() {
        val testFile = File(emptyFile)
        sut.setBaseForScript(testFile)
        assertEquals(
            testFile.getCanonicalFile(),
            sut.getResolvedFile(testFile.getName()).getCanonicalFile()
        ) {
            ".getResolvedFile(${testFile.name}).getCanonicalFile()"
        }
    }

    @Test
    fun `skip bom at start of file and set correct encoding`() {
        sut.reserveFile(bomFile)
        val header = sut.readLine(bomFile)
        assertEquals("\"äöü\"", header)
    }

    @Test
    fun `fail to read a line from a directory`() {
        val directory = File(bomFile).parent
        sut.reserveFile(directory)
        assertThrows<IllegalArgumentException> {
            sut.readLine(directory)
        }
    }
}
