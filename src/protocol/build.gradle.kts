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

import versions.*

subprojects {
    dependencies {
        compile(project(":src:core"))
        testCompile(project(":src:core", "testClasses"))
    }
}

project("ftp") {
    dependencies {
        compile(Libs.commons_net)
    }
}

project("http") {
    dependencies {
        // for SearchTextExtension
        compile(project(":src:components"))
        testCompile(project(":src:components", "testClasses"))
        compile(Libs.commons_net)
        compile(Libs.ph_commons) {
            // We don't really need to use/distribute jsr305
            exclude("com.google.code.findbugs", "jsr305")
        }
        compile(Libs.ph_css) {
            // We don't really need to use/distribute jsr305
            exclude("com.google.code.findbugs", "jsr305")
        }
        compile(Libs.dnsjava)
        compile(Libs.httpmime)
        compile(Libs.dec)
    }

}

project("jdbc") {
    dependencies {
        compile(Libs.commons_dbcp2)
    }
}

project("jms") {
    dependencies {
        testCompile(project(":src:core", "testClasses"))
        // TODO: technically speaking, jms_1.1_spec should be compileOnly
        // since we either include a JMS implementation or we can't use JMS at all
        implementation(Libs.geronimo_jms_spec)
    }
}

project("junit") {
    dependencies {
        compile(Libs.junit)
    }
}

project("junit-sample") {
    dependencies {
        compile(Libs.junit)
    }
}

project("mail") {
    dependencies {
        compile(Libs.mail)
    }
}

project("mongodb") {
    dependencies {
        compile(Libs.mongo_java_driver)
    }
}
