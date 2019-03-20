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
package org.apache.jmeter.report.processor

import org.apache.jmeter.report.core.Sample
import org.apache.jmeter.report.core.SampleMetadata
import spock.lang.Specification

class FieldSampleComparatorSpec extends Specification {

    static char separator = ',' as char
    def multiColSampleMeta = new SampleMetadata(separator, "col1", "col2")


    def testCompare() {
        given:
            def sampleMetadata = new SampleMetadata(separator, "col1")
            def firstRow = new Sample(0, sampleMetadata, "1")
            def secondRow = new Sample(1, sampleMetadata, "2")
            def sut = new FieldSampleComparator("col1")
            sut.initialize(sampleMetadata)
        expect:
            sut.compare(firstRow, secondRow) < 0
            sut.compare(secondRow, firstRow) > 0
            sut.compare(firstRow, firstRow) == 0
            sut.compare(secondRow, secondRow) == 0
    }

    def "initialize ensures correct column is compared"() {
        given:
            def sut = new FieldSampleComparator("col2")
            def firstRow = new Sample(0, multiColSampleMeta, "1", "3")
            def secondRow = new Sample(1, multiColSampleMeta, "2", "3")
            sut.initialize(multiColSampleMeta)
        expect:
            sut.compare(firstRow, secondRow) == 0
    }

    def "Incorrectly uses first column if initialize isn't called"() {
        given:
            def sut = new FieldSampleComparator("col2")
            def firstRow = new Sample(0, multiColSampleMeta, "1", "3")
            def secondRow = new Sample(1, multiColSampleMeta, "2", "3")
        expect:
            sut.compare(firstRow, secondRow) != 0
    }

}
