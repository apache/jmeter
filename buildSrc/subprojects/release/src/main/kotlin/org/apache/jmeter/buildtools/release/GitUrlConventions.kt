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
package org.apache.jmeter.buildtools.release

import java.net.URI

interface GitUrlConventions {
    val pushUrl: String
    val pagesUri: URI
    fun tagUri(tag: String): URI
}

class GitHub(val organization: String, val repo: String): GitUrlConventions {
    override val pushUrl: String
        get() = "https://github.com/$organization/$repo.git"

    override val pagesUri: URI
        get() = URI("https://$organization.github.io/$repo")

    override fun tagUri(tag: String) = URI("https://github.com/$organization/$repo/tree/$tag")
}

class GitDaemon(val host: String, val repo: String): GitUrlConventions {
    override val pushUrl: String
        get() = "git://$host/$repo.git"

    override val pagesUri: URI
        get() = URI("http://$host:8888")

    override fun tagUri(tag: String) = URI("$pushUrl/tags/$tag")
}
