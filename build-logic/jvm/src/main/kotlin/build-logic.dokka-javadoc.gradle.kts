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
    id("java-base")
    id("org.jetbrains.dokka")
}

java {
    // Workaround https://github.com/gradle/gradle/issues/21933, so it adds javadocElements configuration
    withJavadocJar()
}

tasks.named<Jar>("javadocJar") {
    // Set a custom classifier, so we can distinguish it from Dokka-generated javadoc
    archiveClassifier.set("javadoc_java")
}

tasks.dokkaJavadoc {
    moduleName.set("Apache JMeter ${project.name}")
    mustRunAfter("kaptKotlin")
}

val dokkaJar by tasks.registering(Jar::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Assembles a jar archive containing javadoc"
    from(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
}

configurations[JavaPlugin.JAVADOC_ELEMENTS_CONFIGURATION_NAME].outgoing {
    // Avoid publishing Java-generated javadoc
    artifacts.clear()
    // Publish Dokka-generated javadoc instead
    artifact(dokkaJar)
}
