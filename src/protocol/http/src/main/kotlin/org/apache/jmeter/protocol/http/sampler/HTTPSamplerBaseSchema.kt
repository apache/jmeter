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

package org.apache.jmeter.protocol.http.sampler

import org.apache.jmeter.config.Arguments
import org.apache.jmeter.config.KeystoreConfig
import org.apache.jmeter.protocol.http.control.AuthManager
import org.apache.jmeter.protocol.http.control.CacheManager
import org.apache.jmeter.protocol.http.control.CookieManager
import org.apache.jmeter.protocol.http.control.DNSCacheManager
import org.apache.jmeter.protocol.http.control.HeaderManager
import org.apache.jmeter.protocol.http.util.HTTPConstants
import org.apache.jmeter.protocol.http.util.HTTPFileArgs
import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor
import org.apache.jmeter.testelement.schema.IntegerPropertyDescriptor
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apache.jmeter.testelement.schema.TestElementPropertyDescriptor
import org.apiguardian.api.API

/**
 * Lists properties of a [HTTPSamplerBase].
 * @see HTTPSamplerBase
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public abstract class HTTPSamplerBaseSchema : TestElementSchema() {
    public companion object INSTANCE : HTTPSamplerBaseSchema()

    public val arguments: TestElementPropertyDescriptor<HTTPSamplerBaseSchema, Arguments>
        by testElement("HTTPsampler.Arguments")

    public val authManager: TestElementPropertyDescriptor<HTTPSamplerBaseSchema, AuthManager>
        by testElement("HTTPSampler.auth_manager")

    public val cookieManager: TestElementPropertyDescriptor<HTTPSamplerBaseSchema, CookieManager>
        by testElement("HTTPSampler.cookie_manager")

    public val keystoreConfig: TestElementPropertyDescriptor<HTTPSamplerBaseSchema, KeystoreConfig>
        by testElement("HTTPSampler.keystore_configuration")

    public val cacheManager: TestElementPropertyDescriptor<HTTPSamplerBaseSchema, CacheManager>
        by testElement("HTTPSampler.cache_manager")

    public val headerManager: TestElementPropertyDescriptor<HTTPSamplerBaseSchema, HeaderManager>
        by testElement("HTTPSampler.header_manager")

    public val dnsCacheManager: TestElementPropertyDescriptor<HTTPSamplerBaseSchema, DNSCacheManager>
        by testElement("HTTPSampler.dns_cache_manager")

    public val method: StringPropertyDescriptor<HTTPSamplerBaseSchema>
        by string("HTTPSampler.method")

    public val protocol: StringPropertyDescriptor<HTTPSamplerBaseSchema>
        by string("HTTPSampler.protocol", default = HTTPConstants.PROTOCOL_HTTP)

    public val domain: StringPropertyDescriptor<HTTPSamplerBaseSchema>
        by string("HTTPSampler.domain")

    public val port: IntegerPropertyDescriptor<HTTPSamplerBaseSchema>
        by int("HTTPSampler.port")

    public val path: StringPropertyDescriptor<HTTPSamplerBaseSchema>
        by string("HTTPSampler.path")

    public val proxy: HTTPSamplerProxyParamsSchema<HTTPSamplerBaseSchema> by HTTPSamplerProxyParamsSchema()

    public val contentEncoding: StringPropertyDescriptor<HTTPSamplerBaseSchema>
        by string("HTTPSampler.contentEncoding")

    public val implementation: StringPropertyDescriptor<HTTPSamplerBaseSchema>
        by string("HTTPSampler.implementation")

    public val connectTimeout: IntegerPropertyDescriptor<HTTPSamplerBaseSchema>
        by int("HTTPSampler.connect_timeout")

    public val responseTimeout: IntegerPropertyDescriptor<HTTPSamplerBaseSchema>
        by int("HTTPSampler.response_timeout")

    public val followRedirects: BooleanPropertyDescriptor<HTTPSamplerBaseSchema>
        by boolean("HTTPSampler.follow_redirects")

    public val autoRedirects: BooleanPropertyDescriptor<HTTPSamplerBaseSchema>
        by boolean("HTTPSampler.auto_redirects")

    public val useKeepalive: BooleanPropertyDescriptor<HTTPSamplerBaseSchema>
        by boolean("HTTPSampler.use_keepalive")

    public val useMultipartPost: BooleanPropertyDescriptor<HTTPSamplerBaseSchema>
        by boolean("HTTPSampler.DO_MULTIPART_POST")

    public val useBrowserCompatibleMultipart: BooleanPropertyDescriptor<HTTPSamplerBaseSchema>
        by boolean("HTTPSampler.BROWSER_COMPATIBLE_MULTIPART", default = false)

    public val concurrentDownload: BooleanPropertyDescriptor<HTTPSamplerBaseSchema>
        by boolean("HTTPSampler.concurrentDwn", default = false)

    public val concurrentDownloadPoolSize: IntegerPropertyDescriptor<HTTPSamplerBaseSchema>
        by int("HTTPSampler.concurrentPool", default = 6)

    public val retrieveEmbeddedResources: BooleanPropertyDescriptor<HTTPSamplerBaseSchema>
        by boolean("HTTPSampler.image_parser", default = false)

    public val embeddedUrlAllowRegex: StringPropertyDescriptor<HTTPSamplerBaseSchema>
        by string("HTTPSampler.embedded_url_re")

    public val embeddedUrlExcludeRegex: StringPropertyDescriptor<HTTPSamplerBaseSchema>
        by string("HTTPSampler.embedded_url_exclude_re")

    public val storeAsMD5: BooleanPropertyDescriptor<HTTPSamplerBaseSchema>
        by boolean("HTTPSampler.md5", default = false)

    public val postBodyRaw: BooleanPropertyDescriptor<HTTPSamplerBaseSchema>
        by boolean("HTTPSampler.postBodyRaw", default = false)

    public val ipSource: StringPropertyDescriptor<HTTPSamplerBaseSchema>
        by string("HTTPSampler.ipSource")

    public val ipSourceType: IntegerPropertyDescriptor<HTTPSamplerBaseSchema>
        by int("HTTPSampler.ipSourceType", default = HTTPSamplerBase.SourceType.HOSTNAME.ordinal)

    public val fileArguments: TestElementPropertyDescriptor<HTTPSamplerBaseSchema, HTTPFileArgs>
        by testElement("HTTPsampler.Files")
}
