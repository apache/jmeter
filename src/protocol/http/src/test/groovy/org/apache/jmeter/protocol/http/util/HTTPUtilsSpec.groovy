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

package org.apache.jmeter.protocol.http.util

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class HTTPUtilsSpec extends Specification {

    def "getEncoding returns #expectedEncoding from #contentType"() {
        expect:
            ConversionUtils.getEncodingFromContentType(contentType) == expectedEncoding
        where:
            contentType                           | expectedEncoding
            "charset=utf8"                        | "utf8"
            "charset=\"utf8\""                    | "utf8"
            "text/plain ;charset=utf8"            | "utf8"
            "text/html ;charset=utf8;charset=def" | "utf8"
            "xyx"                                 | null
            "charset="                            | null
            ";charset=;"                          | null
            ";charset=no-such-charset;"           | null
    }

    def "makeRelativeURL works with a trailing file"() {
        given:
            URL baseURL = new URL("http://192.168.0.1/a/b/c")
        when:
            def relativeURL = ConversionUtils.makeRelativeURL(baseURL, path)
        then:
            relativeURL == new URL(expectedURL)
        where:
            path            | expectedURL
            "d"             | "http://192.168.0.1/a/b/d"
            "../d"          | "http://192.168.0.1/a/d"
            "../../d"       | "http://192.168.0.1/d"
            "../../../d"    | "http://192.168.0.1/d"
            "../../../../d" | "http://192.168.0.1/d"
            "/../d"         | "http://192.168.0.1/../d"
            "./d"           | "http://192.168.0.1/a/b/d"
    }

    def "makeRelativeURL works with a trailing directory"() {
        given:
            URL baseURL = new URL("http://192.168.0.1/a/b/c/")
        when:
            def relativeURL = ConversionUtils.makeRelativeURL(baseURL, path)
        then:
            relativeURL == new URL(expectedURL)
        where:
            path            | expectedURL
            "d"             | "http://192.168.0.1/a/b/c/d"
            "../d"          | "http://192.168.0.1/a/b/d"
            "../../d"       | "http://192.168.0.1/a/d"
            "../../../d"    | "http://192.168.0.1/d"
            "../../../../d" | "http://192.168.0.1/d"
            "/../d"         | "http://192.168.0.1/../d"
            "./d"           | "http://192.168.0.1/a/b/c/d"
    }

    // Test that location urls with a protocol are passed unchanged
    def "makeRelativeURL given invalid path '#path' returns '#path'"() {
        given:
            URL baseURL = new URL("http://ahost.invalid/a/b/c")
        when:
            def relativeURL = ConversionUtils.makeRelativeURL(baseURL, path)
        then:
            relativeURL == new URL(path)
        where:
            path << ["http://host.invalid/e",
                     "https://host.invalid/e",
                     "http://host.invalid:8081/e",
                     "https://host.invalid:8081/e"]
    }

    def "removeSlashDotDot given a url or path #urlOrPath returns #expectedUrl"() {
        expect:
            ConversionUtils.removeSlashDotDot(urlOrPath) == expectedUrl
        where:
            urlOrPath                                   | expectedUrl
            "/path/"                                    | "/path/"
            "http://host/"                              | "http://host/"
            "http://host/one"                           | "http://host/one"
            "/one/../two"                               | "/two"
            "http://host:8080/one/../two"               | "http://host:8080/two"
            "http://host:8080/one/../two/"              | "http://host:8080/two/"
            "http://usr@host:8080/one/../two/"          | "http://usr@host:8080/two/"
            "http://host:8080/one/../two/?query#anchor" | "http://host:8080/two/?query#anchor"
            "one/two/.."                                | "one"
            "../../path"                                | "../../path"
            "/one/.."                                   | "/"
            "/one/../"                                  | "/"
            "/one/..?a"                                 | "/?a"
            "http://host/one/../one"                    | "http://host/one"
            "http://host/one/two/../../one/two"         | "http://host/one/two"
            "http://host/.."                            | "http://host/.."
            "http://host/../abc"                        | "http://host/../abc"
    }

    def testSanitizeUrl() {
        expect:
            ConversionUtils.sanitizeUrl(new URL(input)) == new URI(expected)
        where:
            input                                                        | expected
            'http://localhost/'                                          | 'http://localhost/'                                               // normal, no encoding needed
            'http://192.168.0.1/'                                        | 'http://192.168.0.1/'                                             // normal, no encoding needed
            'http://host/a/b/c|d'                                        | 'http://host/a/b/c%7Cd'                                           // pipe needs encoding
            'http://host:8080/%5B%5D'                                    | 'http://host:8080/%5B%5D'                                         // already encoded
            'http://host:8080/?%5B%5D'                                   | 'http://host:8080/?%5B%5D'                                        //already encoded
            'http://host:8080/?!£$*():@~;\'\"%^{}[]<>|\\#'               | 'http://host:8080/?!£$*():@~;\'%22%25%5E%7B%7D[]%3C%3E%7C%5C#'    // unencoded query
            'http://host/?!£$*():@~;\'%22%25%5E%7B%7D[]%3C%3E%7C%5C#'    | 'http://host/?!£$*():@~;\'%22%25%5E%7B%7D[]%3C%3E%7C%5C#'         // encoded
            'http://host:8080/!£$*():@~;\'"%^{}[]<>|\\#'                 | 'http://host:8080/!£$*():@~;\'%22%25%5E%7B%7D%5B%5D%3C%3E%7C%5C#' // unencoded path
            'http://host/!£$*():@~;\'%22%25%5E%7B%7D%5B%5D%3C%3E%7C%5C#' | 'http://host/!£$*():@~;\'%22%25%5E%7B%7D%5B%5D%3C%3E%7C%5C#'      // encoded
            'http://host/?%25%5B%5D!@$%^*()#'                            | 'http://host/?%2525%255B%255D!@$%25%5E*()#'
            'http://host/?%2525%255B%255D!@$%25%5E*()#'                  | 'http://host/?%2525%255B%255D!@$%25%5E*()#'
            'http://host/%5B%5D?[]!@$%^*()#'                             | 'http://host/%255B%255D?[]!@$%25%5E*()#'
            'http://host/%255B%255D?[]!@$%25%5E*()#'                     | 'http://host/%255B%255D?[]!@$%25%5E*()#'
            'http://host/IqGo6EM1JEVZ+MSRJqUSo@qhjVMSFBTs/37/0/1'        | 'http://host/IqGo6EM1JEVZ+MSRJqUSo@qhjVMSFBTs/37/0/1'
            'http://host/test?getItem={someID}'                          | 'http://host/test?getItem=%7BsomeID%7D'
    }
}
