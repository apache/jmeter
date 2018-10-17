/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jmeter.services

import org.apache.jmeter.junit.spock.JMeterSpec
import spock.lang.Unroll

@Unroll
class FileServerSpec extends JMeterSpec {

    def sut = new FileServer()

    def testFile = findTestPath("testfiles/unit/FileServerSpec.csv")
    def emptyFile = findTestPath("testfiles/empty.csv")


    def setup() {
        sut.resetBase()
    }

    def tearDown() {
        sut.closeFiles()
    }

    def "reading a non-existent file throws an exception"() {
        when:
            sut.readLine("test")
        then:
            thrown(IOException)
    }

    def "writing to a non-exisent file throws an exception"() {
        when:
            sut.write("test", "")
        then:
            thrown(IOException)
    }

    def "no files should be open following resetBase"() {
        expect:
            !sut.filesOpen()
    }

    def "closing unrecognised files are ignored"() {
        when:
            sut.closeFile("xxx")
        then:
            !sut.filesOpen()
            noExceptionThrown()
    }

    def "file is not opened until read from"() {
        when:
            sut.reserveFile(testFile) // Does not open file
        then:
            !sut.filesOpen()
        when:
            def line = sut.readLine(testFile)
        then:
            line == "a1,b1,c1,d1"
            sut.filesOpen()
    }

    def "reading lines loops to start once last line is read"() {
        given:
            sut.reserveFile(testFile)
        when:
            def firstPass = [sut.readLine(testFile), sut.readLine(testFile), sut.readLine(testFile), sut.readLine(testFile)]
            def secondPass = [sut.readLine(testFile), sut.readLine(testFile), sut.readLine(testFile), sut.readLine(testFile)]
        then:
            firstPass == secondPass
    }

    def "cannot write to reserved file after reading"() {
        given:
            sut.reserveFile(testFile)
            sut.readLine(testFile)
        when:
            sut.write(testFile, "")
        then:
            thrown(IOException)
    }

    def "closing reserved file after reading resets"() {
        given:
            sut.reserveFile(testFile)
            sut.readLine(testFile)
        when:
            sut.closeFile(testFile) // does not remove the entry
        then:
            !sut.filesOpen()
            sut.readLine(testFile) == "a1,b1,c1,d1" // Re-read first line
            sut.filesOpen()
    }

    def "closeFiles() prevents reading of reserved file"() {
        given:
            sut.reserveFile(testFile)
            sut.readLine(testFile)
        when:
            sut.closeFiles() // removes all entries
            sut.readLine(testFile)
        then:
            !sut.filesOpen()
            thrown(IOException)
    }

    def "baseDir is the defaultBasedir"() {
        expect:
            sut.getBaseDir() == FileServer.getDefaultBase()
    }

    def "setBaseDir doesn't error when no files are open"() {
        when:
            sut.setBasedir("testfiles/unit/FileServerSpec.csv")
        then:
            sut.getBaseDir().replaceAll("\\\\", "/").endsWith("testfiles/unit")
    }

    // TODO: what about throwing an exception in setBaseDir?
    def "setBaseDir doesn't set base when passed a directory"() {
        def dir = "does-not-exist"
        given:
            sut.setBasedir(dir)
        when:
            sut.getBaseDir().endsWith(dir)
        then:
            thrown(NullPointerException)
    }

    def "cannot set baseDir when files are open"() {
        given:
            sut.reserveFile(testFile)
            sut.readLine(testFile) == "a1,b1,c1,d1"
        when:
            sut.setBasedir("testfiles")
        then:
            thrown(IllegalStateException)
    }

    static def baseFile = new File(FileServer.getDefaultBase())

    def "setting base to #file gives getBaseDirRelative == #expectedBaseDirRelative"() {
        when:
            sut.setBaseForScript(file)
        then:
            sut.getBaseDirRelative().toString() == expectedBaseDirRelative
        where:
            file                                                | expectedBaseDirRelative
            baseFile                                            | "."
            baseFile.getParentFile()                            | "."
            new File(baseFile.getParentFile(), "abcd/defg.jmx") | "."
            new File(baseFile, "abcd/defg.jmx")                 | "abcd"
    }

    def "non-existent filename to reserveFile will throw exception"() {
        given:
            def missing = "no-such-file"
            def alias = "missing"
            def charsetName = "UTF-8"
            def hasHeader = true
        when:
            sut.reserveFile(missing, charsetName, alias, hasHeader)
        then:
            def ex = thrown(IllegalArgumentException)
            ex.getMessage() == "Could not read file header line for file $missing"
            ex.getCause().getMessage() == "File $missing must exist and be readable"
    }

    def "reserving a file with no header will throw an exception if the header is expected"() {
        given:
            def alias = "empty"
            def charsetName = "UTF-8"
        when:
            sut.reserveFile(emptyFile, charsetName, alias, true)
        then:
            def e = thrown(IllegalArgumentException)
            e.getCause() instanceof EOFException
    }

    def "resolvedFile returns absolute and relative files"() {
        given:
            def testFile = new File(emptyFile)
        expect:
            // absolute
            sut.getResolvedFile(testFile.getAbsolutePath())
                    .getCanonicalFile() == testFile.getCanonicalFile()
            // relative
            sut.getResolvedFile(testFile.getParentFile().getPath() + "/../testfiles/empty.csv")
                    .getCanonicalFile() == testFile.getCanonicalFile()
    }

    def "resolvedFile returns relative files with BaseForScript set"() {
        given:
            def testFile = new File(emptyFile)
        when:
            sut.setBaseForScript(testFile)
        then:
            sut.getResolvedFile(testFile.getName())
                    .getCanonicalFile() == testFile.getCanonicalFile()
    }

}
