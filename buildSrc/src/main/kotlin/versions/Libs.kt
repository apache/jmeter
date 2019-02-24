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

package versions

object Versions {
    const val jackson = "2.9.9"
    const val tika = "1.21"
    const val bouncycastle = "1.60"
    const val jodd = "5.0.6"
    const val slf4j = "1.7.25"
    const val xalan = "2.7.2"
    const val ktlint = "0.29.0"
    const val kotlin = "1.3.11"
    const val activemq = "5.15.8"
}

object BuildToolVersions {
    const val jacoco = "0.8.2"
    const val checkstyle = "8.8"
    const val spotbugs = "3.1.12"
}

object BuildTools {
    const val velocity = "org.apache.velocity:velocity:1.7"
}

object Libs {
    const val bsf = "bsf:bsf:2.4.0"
    const val cglib_nodep = "cglib:cglib-nodep:3.2.9"
    const val jackson_annotations =
        "com.fasterxml.jackson.core:jackson-annotations:${Versions.jackson}"
    const val jackson_core = "com.fasterxml.jackson.core:jackson-core:${Versions.jackson}"
    const val jackson_databind = "com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}"
    const val rsyntaxtextarea = "com.fifesoft:rsyntaxtextarea:3.0.2"
    const val caffeine = "com.github.ben-manes.caffeine:caffeine:2.6.2"
    const val ph_commons = "com.helger:ph-commons:9.2.1"
    const val ph_css = "com.helger:ph-css:6.1.1"
    const val json_path = "com.jayway.jsonpath:json-path:2.4.0"
    const val javax_activation = "com.sun.activation:javax.activation:1.2.0"
    const val xstream = "com.thoughtworks.xstream:xstream:1.4.11"
    const val commons_codec = "commons-codec:commons-codec:1.11"
    const val commons_collections = "commons-collections:commons-collections:3.2.2"
    const val commons_io = "commons-io:commons-io:2.6"
    const val commons_lang = "commons-lang:commons-lang:2.6"
    const val commons_net = "commons-net:commons-net:3.6"
    const val dnsjava = "dnsjava:dnsjava:2.1.8"
    const val darcula =
        "com.github.bulenkov.darcula:darcula:e208efb96f70e4be9dc362fbb46f6e181ef501dd"
    const val javax_activation_api = "javax.activation:javax.activation-api:1.2.0"
    const val mail = "javax.mail:mail:1.5.0-b01"
    const val jcharts = "jcharts:jcharts:0.7.5"
    const val junit = "junit:junit:4.12"
    const val accessors_smart = "net.minidev:accessors-smart:1.2"
    const val json_smart = "net.minidev:json-smart:2.3"
    const val jtidy = "net.sf.jtidy:jtidy:r938"
    const val Saxon_HE = "net.sf.saxon:Saxon-HE:9.9.1-1"
    const val bsh = "org.apache-extras.beanshell:bsh:2.0b6"
    const val commons_dbcp2 = "org.apache.commons:commons-dbcp2:2.5.0"
    const val commons_jexl3 = "org.apache.commons:commons-jexl3:3.1"
    const val commons_jexl = "org.apache.commons:commons-jexl:2.1.1"
    const val commons_lang3 = "org.apache.commons:commons-lang3:3.8.1"
    const val commons_math3 = "org.apache.commons:commons-math3:3.6.1"
    const val commons_pool2 = "org.apache.commons:commons-pool2:2.6.0"
    const val commons_text = "org.apache.commons:commons-text:1.6"
    const val ftplet_api = "org.apache.ftpserver:ftplet-api:1.1.1"
    const val ftpserver_core = "org.apache.ftpserver:ftpserver-core:1.1.1"
    const val geronimo_jms_spec = "org.apache.geronimo.specs:geronimo-jms_1.1_spec:1.1.1"
    const val httpasyncclient = "org.apache.httpcomponents:httpasyncclient:4.1.4"
    const val httpclient = "org.apache.httpcomponents:httpclient:4.5.8"
    const val httpcore_nio = "org.apache.httpcomponents:httpcore-nio:4.4.11"
    const val httpcore = "org.apache.httpcomponents:httpcore:4.4.11"
    const val httpmime = "org.apache.httpcomponents:httpmime:4.5.8"
    const val log4j_12_api = "org.apache.logging.log4j:log4j-1.2-api:2.11.1"
    const val log4j_api = "org.apache.logging.log4j:log4j-api:2.11.1"
    const val log4j_core = "org.apache.logging.log4j:log4j-core:2.11.1"
    const val log4j_slf4j_impl = "org.apache.logging.log4j:log4j-slf4j-impl:2.11.1"
    const val mina_core = "org.apache.mina:mina-core:2.0.19"
    const val apache_rat = "org.apache.rat:apache-rat:0.13"
    const val tika_core = "org.apache.tika:tika-core:${Versions.tika}"
    const val tika_parsers = "org.apache.tika:tika-parsers:${Versions.tika}"
    const val velocity = "org.apache.velocity:velocity:1.7"
    const val xmlgraphics_commons = "org.apache.xmlgraphics:xmlgraphics-commons:2.3"
    const val bcmail = "org.bouncycastle:bcmail-jdk15on:${Versions.bouncycastle}"
    const val bcpkix = "org.bouncycastle:bcpkix-jdk15on:${Versions.bouncycastle}"
    const val bcprov = "org.bouncycastle:bcprov-jdk15on:${Versions.bouncycastle}"
    const val dec = "org.brotli:dec:0.1.2"
    const val groovy_all = "org.codehaus.groovy:groovy-all:2.4.16"
    const val hamcrest_date = "org.exparity:hamcrest-date:2.0.4"
    const val freemarker = "org.freemarker:freemarker:2.3.28"
    const val hamcrest_core = "org.hamcrest:hamcrest-core:1.3"
    const val hsqldb = "org.hsqldb:hsqldb:2.4.1"
    const val jdom = "org.jdom:jdom:1.1.3"
    const val jodd_core = "org.jodd:jodd-core:${Versions.jodd}"
    const val jodd_lagarto = "org.jodd:jodd-lagarto:${Versions.jodd}"
    const val jodd_log = "org.jodd:jodd-log:${Versions.jodd}"
    const val jodd_props = "org.jodd:jodd-props:${Versions.jodd}"
    const val jsoup = "org.jsoup:jsoup:1.11.3"
    const val mongo_java_driver = "org.mongodb:mongo-java-driver:2.11.3"
    const val rhino = "org.mozilla:rhino:1.7.10"
    const val objenesis = "org.objenesis:objenesis:2.6"
    const val asm = "org.ow2.asm:asm:7.0"
    const val jcl_over_slf4j = "org.slf4j:jcl-over-slf4j:${Versions.slf4j}"
    const val slf4j_api = "org.slf4j:slf4j-api:${Versions.slf4j}"
    const val sonarqube_ant = "org.sonarsource.scanner.ant:sonarqube-ant-task:2.5"
    const val spock_core = "org.spockframework:spock-core:1.2-groovy-2.4"
    const val oro = "oro:oro:2.0.8"
    const val serializer = "xalan:serializer:${Versions.xalan}"
    const val xalan = "xalan:xalan:${Versions.xalan}"
    const val xercesImpl = "xerces:xercesImpl:2.12.0"
    const val xml_apis = "xml-apis:xml-apis:1.4.01"
    const val xmlpull = "xmlpull:xmlpull:1.1.3.1"
    const val xpp3_min = "xpp3:xpp3_min:1.1.4c"
    // activemq-all should not be used as it provides secondary slf4j binding
    val activemq = arrayOf(
        "org.apache.activemq:activemq-broker:${Versions.activemq}",
        "org.apache.activemq:activemq-client:${Versions.activemq}",
        "org.apache.activemq:activemq-spring:${Versions.activemq}",
        "org.springframework:spring-context:4.3.17.RELEASE",
        "org.springframework:spring-beans:4.3.17.RELEASE",
        jackson_databind, // ensure we have the same version
        jackson_annotations,
        commons_pool2
    )
}

