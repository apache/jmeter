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

plugins {
    jmeterbuild.ide
}

dependencies {
    compile(project(":src:launcher"))
    compile(project(":src:jorphan"))
    testCompile(project(":src:jorphan", "testClasses"))

    // Specify asm version
    compile(Libs.asm)

    compile(Libs.bsf)
    compile(Libs.caffeine)
    compile(Libs.oro)
    compile(Libs.jackson_annotations)
    compile(Libs.jackson_core)
    compile(Libs.jackson_databind)
    compile(Libs.freemarker)
    compile(Libs.groovy_all) {
        because("Groovy is a default JSR232 engine")
    }
    compileOnly(Libs.tika_parsers)
    compile(Libs.rsyntaxtextarea)
    compile(Libs.rhino)
    compile(Libs.xmlgraphics_commons)
    compile(Libs.commons_text)
    compile(Libs.jodd_core)
    compile(Libs.jodd_props)
    compile(Libs.xalan)
    // TODO: JMeter bundles Xerces, however the reason is unknown
    compile(Libs.xercesImpl)
    compile(Libs.xml_apis)
    // Note: Saxon should go AFTER xalan so xalan XSLT is used
    // org.apache.jmeter.util.XPathUtilTest.testFormatXmlSimple assumes xalan transformer
    compile(Libs.Saxon_HE)
    compile(Libs.jtidy)
    compile(Libs.xstream)
    compile(Libs.log4j_12_api)
    compile(Libs.log4j_api)
    compile(Libs.log4j_core) {
        because("GuiLogEventAppender is using log4j-core to implement GUI-based log appender")
    }
    compile(Libs.log4j_slf4j_impl) {
        because("Both log4j and slf4j are included, so it makes sense to just add log4j->slf4j bridge as well")
    }
    compile(Libs.jcl_over_slf4j)

    testCompile(Libs.commons_net)
    testImplementation(Libs.spock_core)
}

val generatedVersionDir = File(buildDir, "generated/sources/version")

val versionClass by tasks.registering(Sync::class) {
    val lastEditYear: String by rootProject.extra
    val version = rootProject.version.toString()
    inputs.property("@VERSION@", version)
    inputs.property("@YEAR@", lastEditYear)
    outputs.dir(generatedVersionDir)

    from("$projectDir/src/main/version") {
        include("**/*.java")
        filter({ x: String ->
            x.replace("@VERSION@", version)
                .replace("@YEAR@", lastEditYear)
        })
    }
    into(generatedVersionDir)
}

ide {
    generatedJavaSources(versionClass.get(), generatedVersionDir)
}
