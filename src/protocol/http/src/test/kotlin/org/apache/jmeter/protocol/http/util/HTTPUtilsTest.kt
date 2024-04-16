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

package org.apache.jmeter.protocol.http.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.net.URL

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HTTPUtilsTest {
    data class GetEncodingFromContentTypeCase(val contentType: String, val expectedEncoding: String?)

    @ParameterizedTest
    @MethodSource("getEncodingFromContentTypeCases")
    fun getEncodingFromContentType(case: GetEncodingFromContentTypeCase) {
        assertEquals(case.expectedEncoding, ConversionUtils.getEncodingFromContentType(case.contentType))
    }

    private fun getEncodingFromContentTypeCases() = listOf(
        GetEncodingFromContentTypeCase("charset=utf8", "utf8"),
        GetEncodingFromContentTypeCase("charset=\"utf8\"", "utf8"),
        GetEncodingFromContentTypeCase("text/plain ;charset=utf8", "utf8"),
        GetEncodingFromContentTypeCase("text/html ;charset=utf8;charset=def", "utf8"),
        GetEncodingFromContentTypeCase("xyx", null),
        GetEncodingFromContentTypeCase("charset=", null),
        GetEncodingFromContentTypeCase(";charset=;", null),
        GetEncodingFromContentTypeCase(";charset=no-such-charset;", null),
    )

    data class MakeRelativeUrlCase(val baseURL: String, val path: String, val expectedURL: String)

    @ParameterizedTest
    @MethodSource("makeRelativeUrlFileCases")
    fun `makeRelativeURL works with a trailing file`(case: MakeRelativeUrlCase) {
        assertMakeRelativeUrl(case.expectedURL, case.baseURL, case.path)
    }

    private fun makeRelativeUrlFileCases() = listOf(
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c", "d", "http://192.168.0.1/a/b/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c", "../d", "http://192.168.0.1/a/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c", "../../d", "http://192.168.0.1/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c", "../../../d", "http://192.168.0.1/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c", "../../../../d", "http://192.168.0.1/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c", "/../d", "http://192.168.0.1/../d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c", "./d", "http://192.168.0.1/a/b/d"),
    )

    @ParameterizedTest
    @MethodSource("makeRelativeUrlDirectoryCases")
    fun `makeRelativeURL works with a trailing directory`(case: MakeRelativeUrlCase) {
        assertMakeRelativeUrl(case.expectedURL, case.baseURL, case.path)
    }

    private fun makeRelativeUrlDirectoryCases() = listOf(
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c/", "d", "http://192.168.0.1/a/b/c/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c/", "../d", "http://192.168.0.1/a/b/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c/", "../../d", "http://192.168.0.1/a/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c/", "../../../d", "http://192.168.0.1/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c/", "../../../../d", "http://192.168.0.1/d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c/", "/../d", "http://192.168.0.1/../d"),
        MakeRelativeUrlCase("http://192.168.0.1/a/b/c/", "./d", "http://192.168.0.1/a/b/c/d"),
    )

    private fun assertMakeRelativeUrl(expectedUrl: String, baseUrl: String, path: String) {
        val baseURL = URL(baseUrl)
        val relativeURL = ConversionUtils.makeRelativeURL(baseURL, path)
        assertEquals(URL(expectedUrl), relativeURL)
    }

    // Test that location urls with a protocol are passed unchanged
    @ParameterizedTest
    @ValueSource(
        strings = [
            "http://host.invalid/e",
            "https://host.invalid/e",
            "http://host.invalid:8081/e",
            "https://host.invalid:8081/e",
        ]
    )
    fun `makeRelativeURL given invalid path '#path' returns '#path'`(path: String) {
        assertMakeRelativeUrl(path, "http://ahost.invalid/a/b/c", path)
    }

    data class RemoveSlashDotCase(val urlOrPath: String, val expectedUrl: String)

    @ParameterizedTest
    @MethodSource("removeSlashDotDot")
    fun removeSlashDotDot(case: RemoveSlashDotCase) {
        assertEquals(case.expectedUrl, ConversionUtils.removeSlashDotDot(case.urlOrPath))
    }

    private fun removeSlashDotDot() = listOf(
        RemoveSlashDotCase("/path/", "/path/"),
        RemoveSlashDotCase("http://host/", "http://host/"),
        RemoveSlashDotCase("http://host/one", "http://host/one"),
        RemoveSlashDotCase("/one/../two", "/two"),
        RemoveSlashDotCase("http://host:8080/one/../two", "http://host:8080/two"),
        RemoveSlashDotCase("http://host:8080/one/../two/", "http://host:8080/two/"),
        RemoveSlashDotCase("http://usr@host:8080/one/../two/", "http://usr@host:8080/two/"),
        RemoveSlashDotCase("http://host:8080/one/../two/?query#anchor", "http://host:8080/two/?query#anchor"),
        RemoveSlashDotCase("one/two/..", "one"),
        RemoveSlashDotCase("../../path", "../../path"),
        RemoveSlashDotCase("/one/..", "/"),
        RemoveSlashDotCase("/one/../", "/"),
        RemoveSlashDotCase("/one/..?a", "/?a"),
        RemoveSlashDotCase("http://host/one/../one", "http://host/one"),
        RemoveSlashDotCase("http://host/one/two/../../one/two", "http://host/one/two"),
        RemoveSlashDotCase("http://host/..", "http://host/.."),
        RemoveSlashDotCase("http://host/../abc", "http://host/../abc"),
    )

    @ParameterizedTest
    @MethodSource("sanitizeUrlCases")
    fun testSanitizeUrl(case: SanitizeUrlCase) {
        assertEquals(URI(case.expectedUrl), ConversionUtils.sanitizeUrl(URL(case.input)), case.message)
    }

    data class SanitizeUrlCase(val input: String, val expectedUrl: String, val message: String? = null)

    private fun sanitizeUrlCases() = listOf(
        SanitizeUrlCase("http://localhost/", "http://localhost/", "normal, no encoding needed"),
        SanitizeUrlCase("http://192.168.0.1/", "http://192.168.0.1/", "normal, no encoding needed"),
        SanitizeUrlCase("http://host/a/b/c|d", "http://host/a/b/c%7Cd", "pipe needs encoding"),
        SanitizeUrlCase("http://host:8080/%5B%5D", "http://host:8080/%5B%5D", "already encoded"),
        SanitizeUrlCase("http://host:8080/?%5B%5D", "http://host:8080/?%5B%5D", "already encoded"),
        SanitizeUrlCase(
            "http://host:8080/?!£$*():@~;'\"%^{}[]<>|\\#",
            "http://host:8080/?!£$*():@~;'%22%25%5E%7B%7D[]%3C%3E%7C%5C#",
            "unencoded query"
        ),
        SanitizeUrlCase(
            "http://host/?!£$*():@~;'%22%25%5E%7B%7D[]%3C%3E%7C%5C#",
            "http://host/?!£$*():@~;'%22%25%5E%7B%7D[]%3C%3E%7C%5C#",
            "encoded"
        ),
        SanitizeUrlCase(
            "http://host:8080/!£$*():@~;'\"%^{}[]<>|\\#",
            "http://host:8080/!£$*():@~;'%22%25%5E%7B%7D%5B%5D%3C%3E%7C%5C#",
            "unencoded path"
        ),
        SanitizeUrlCase(
            "http://host/!£$*():@~;'%22%25%5E%7B%7D%5B%5D%3C%3E%7C%5C#",
            "http://host/!£$*():@~;'%22%25%5E%7B%7D%5B%5D%3C%3E%7C%5C#",
            "encoded"
        ),
        SanitizeUrlCase("http://host/?%25%5B%5D!@$%^*()#", "http://host/?%2525%255B%255D!@$%25%5E*()#"),
        SanitizeUrlCase("http://host/?%2525%255B%255D!@$%25%5E*()#", "http://host/?%2525%255B%255D!@$%25%5E*()#"),
        SanitizeUrlCase("http://host/%5B%5D?[]!@$%^*()#", "http://host/%255B%255D?[]!@$%25%5E*()#"),
        SanitizeUrlCase("http://host/%255B%255D?[]!@$%25%5E*()#", "http://host/%255B%255D?[]!@$%25%5E*()#"),
        SanitizeUrlCase(
            "http://host/IqGo6EM1JEVZ+MSRJqUSo@qhjVMSFBTs/37/0/1",
            "http://host/IqGo6EM1JEVZ+MSRJqUSo@qhjVMSFBTs/37/0/1"
        ),
        SanitizeUrlCase("http://host/test?getItem={someID}", "http://host/test?getItem=%7BsomeID%7D"),
    )
}
