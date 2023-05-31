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

import net.ltgt.gradle.errorprone.ErrorProneOptions
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("build-logic.jvm-library")
    id("me.champeau.jmh")
}

if (file("src/jmh/kotlin").isDirectory) {
    apply(plugin = "build-logic.kotlin")
}

dependencies {
    findProject(":src:bom-testing")?.let{
        jmhImplementation(platform(it))
    }
    jmhImplementation("org.openjdk.jmh:jmh-core")
    jmhImplementation("org.openjdk.jmh:jmh-generator-annprocess")
}

tasks.jmh {
    outputs.upToDateWhen {
        false
    }
}

tasks.jmhCompileGeneratedClasses {
    options.annotationProcessorPath = configurations.jmhAnnotationProcessor.get()
}

plugins.withId("net.ltgt.errorprone") {
    tasks.jmhCompileGeneratedClasses {
        options.errorprone {
            // JMH generates duplicate field names like byte p000, p001, p002
            disable("HidingField")
        }
    }
}

jmh {
    // Add memory allocation profiling by default
    profilers.add("gc")
}

// See https://github.com/melix/jmh-gradle-plugin
// Unfortunately, current jmh-gradle-plugin does not allow to customize jmh parameters from the
// command line, so the workarounds are:
// a) Build and execute the jar itself: ./gradlew jmhJar && java -jar build/libs/calcite-...jar JMH_OPTIONS
// b) Execute benchmarks via .main() methods from IDE (you might want to activate "power save mode"
//    in the IDE to minimize the impact of the IDE itself on the benchmark results)

tasks.withType<JavaExec>().configureEach {
    // Execution of .main methods from IDEA should re-generate benchmark classes if required
    dependsOn("jmhCompileGeneratedClasses")
    doFirst {
        // At best jmh plugin should add the generated directories to the Gradle model, however,
        // currently it builds the jar only :-/
        // IntelliJ IDEA "execute main method" adds a JavaExec task, so we configure it
        classpath(File(buildDir, "jmh-generated-classes"))
        classpath(File(buildDir, "jmh-generated-resources"))
    }
}
