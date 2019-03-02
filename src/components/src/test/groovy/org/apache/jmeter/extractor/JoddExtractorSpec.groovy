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
package org.apache.jmeter.extractor

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class JoddExtractorSpec extends Specification {

    def "extract #expression and #attribute"() {
        given:
            def resultList = []
            def input = """
<html>
  <head><title>Test</title></head>
  <body>
    <h1 class="title">TestTitle</h1>
    <p>Some text</p>
    <h1>AnotherTitle</h1>
  </body>
</html>
"""
        when:
            def foundCount = new JoddExtractor().extract(expression, attribute, matchNumber, input, resultList, found, cacheKey)
        then:
            foundCount == expected
            resultList == expectedList
        where:
            expression        | attribute | matchNumber | expectedList                  | found | expected | cacheKey
            "p"               | ""        | 1           | ["Some text"]                 | -1    | 0        | "key"
            "h1[class=title]" | "class"   | 1           | ["title"]                     | -1    | 0        | "key"
            "h1"              | ""        | 0           | ["TestTitle", "AnotherTitle"] | -1    | 1        | "key"
            "notthere"        | ""        | 0           | []                            | -1    | -1       | "key"
    }
}
