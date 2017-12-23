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

package org.apache.jmeter.report.core

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class SampleMetadataParserSpec extends Specification {

    def "Parse headers (#headers) with separator (#separator) and get (#expectedColumns)"() {
        given:
            def sut = new SampleMetaDataParser(separator as char)
        when:
            def columns = sut.parse(headers).columns
        then:
            columns == expectedColumns
        where:
            separator | headers           | expectedColumns
            ';'       | "a;b;c;d;e"       | ["a", "b", "c", "d", "e"]
            '|'       | "a|b|c|d|e"       | ["a", "b", "c", "d", "e"]
            '|'       | "aa|bb|cc|dd|eef" | ["aa", "bb", "cc", "dd", "eef"]
            '&'       | "a&b&c&d&e"       | ["a", "b", "c", "d", "e"]
            '\t'      | "a\tb c\td\te"    | ["a", "b c", "d", "e"]
            ','       | "abcdef"          | ["abcdef"]
    }

}
