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

plugins {
    id("build-logic.java-published-platform")
}

description = "A collection of versions of third-party libraries used for testing purposes by Apache JMeter"

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.junit:junit-bom:5.14.0"))
    api(platform("org.springframework:spring-framework-bom:6.2.12"))
    api(platform("org.eclipse.jetty:jetty-bom:11.0.26"))

    constraints {
        // api means "the dependency is for both compilation and runtime"
        // runtime means "the dependency is only for runtime, not for compilation"
        // In other words, marking dependency as "runtime" would avoid accidental
        // dependency on it during compilation
        // Note: if there's at least single chance for the dependency to be needed on the
        // compilation classpath (e.g. it is used as a transitive by a third-party library)
        // then it should be declared as "api" here since we use useCompileClasspathVersions
        // to make runtime classpath consistent with the compile one.
        api("org.wiremock:wiremock:3.13.1")
        api("io.mockk:mockk:1.14.6")
        api("net.bytebuddy:byte-buddy:1.14.11")
        api("nl.jqno.equalsverifier:equalsverifier:4.2.1")
        // activemq-all should not be used as it provides secondary slf4j binding
        api("org.apache.activemq:activemq-broker:5.16.8")
        api("org.apache.activemq:activemq-client:5.16.8")
        api("org.apache.activemq:activemq-spring:5.16.8")
        api("org.apache.ftpserver:ftplet-api:1.2.0")
        api("org.apache.ftpserver:ftpserver-core:1.2.0")
        api("org.apache.mina:mina-core:2.2.4")
        api("org.hamcrest:hamcrest-core:2.2")
        api("org.hamcrest:hamcrest-library:2.2")
        api("org.hamcrest:hamcrest:2.2")
        api("org.hsqldb:hsqldb:2.7.2")
        api("org.objenesis:objenesis:3.3")
        api("org.openjdk.jmh:jmh-core:1.37")
        api("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    }
}
