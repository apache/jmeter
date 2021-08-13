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

subprojects {
    dependencies {
        api(project(":src:core"))
        testImplementation(project(":src:core", "testClasses"))
    }
}

project("bolt") {
    dependencies {
        implementation("org.neo4j.driver:neo4j-java-driver")
        implementation("org.apache.commons:commons-lang3")
        implementation("com.fasterxml.jackson.core:jackson-core")
        implementation("com.fasterxml.jackson.core:jackson-databind")
    }
}

project("ftp") {
    dependencies {
        implementation("commons-net:commons-net")
        implementation("commons-io:commons-io") {
            because("IOUtils")
        }
        implementation("org.apache.commons:commons-lang3") {
            because("StringUtils")
        }
    }
}

project("http") {
    dependencies {
        // for SearchTextExtension
        api(project(":src:components"))
        testImplementation(project(":src:components", "testClasses"))

        api("com.thoughtworks.xstream:xstream") {
            because("HTTPResultConverter uses XStream in public API")
        }

        compileOnly("javax.activation:javax.activation-api") {
            because("ParseCurlCommandAction uses new MimetypesFileTypeMap()")
        }

        implementation("com.github.ben-manes.caffeine:caffeine")
        implementation("commons-io:commons-io") {
            because("IOUtils")
        }
        implementation("org.apache.commons:commons-lang3") {
            because("StringUtils")
        }
        implementation("org.apache.commons:commons-text") {
            because("StringEscapeUtils")
        }
        implementation("org.jodd:jodd-lagarto")
        implementation("org.jsoup:jsoup")
        implementation("oro:oro")
        implementation("org.apache.commons:commons-collections4")
        implementation("commons-net:commons-net")
        implementation("com.helger.commons:ph-commons") {
            // We don't really need to use/distribute jsr305
            exclude("com.google.code.findbugs", "jsr305")
        }
        implementation("com.helger:ph-css") {
            // We don't really need to use/distribute jsr305
            exclude("com.google.code.findbugs", "jsr305")
        }
        implementation("dnsjava:dnsjava")
        implementation("org.apache.httpcomponents:httpmime")
        implementation("org.apache.httpcomponents:httpcore")
        implementation("org.brotli:dec")
        implementation("com.miglayout:miglayout-swing")
        implementation("com.fasterxml.jackson.core:jackson-core")
        implementation("com.fasterxml.jackson.core:jackson-databind")
        testImplementation(testFixtures(project(":src:testkit-wiremock")))
        testImplementation("com.github.tomakehurst:wiremock-jre8")
        // For some reason JMeter bundles just tika-core and tika-parsers without transitive
        // dependencies. So we exclude those
        implementation("org.apache.tika:tika-core") {
            isTransitive = false
        }
        runtimeOnly("org.apache.tika:tika-parsers") {
            isTransitive = false
        }
    }
}

project("java") {
    dependencies {
        implementation("org.apache.commons:commons-lang3") {
            because("ArrayUtils")
        }
        implementation("commons-io:commons-io") {
            because("IOUtils")
        }
    }
}

project("jdbc") {
    dependencies {
        implementation("org.apache.commons:commons-dbcp2")
        implementation("org.apache.commons:commons-lang3") {
            because("StringUtils, ObjectUtils")
        }
        implementation("commons-io:commons-io") {
            because("IOUtils")
        }
    }
}

project("jms") {
    dependencies {
        testImplementation(project(":src:core", "testClasses"))
        api("com.github.ben-manes.caffeine:caffeine") {
            because("MessageRenderer#getValueFromFile(..., caffeine.cache.Cache)")
        }
        // TODO: technically speaking, jms_1.1_spec should be compileOnly
        // since we either include a JMS implementation or we can't use JMS at all
        implementation("org.apache.geronimo.specs:geronimo-jms_1.1_spec")
        implementation("org.apache.commons:commons-lang3") {
            because("StringUtils")
        }
        implementation("commons-io:commons-io") {
            because("IOUtils")
        }
        implementation("com.miglayout:miglayout-swing")
    }
}

project("junit") {
    dependencies {
        api("junit:junit")
        implementation("org.apache.commons:commons-lang3") {
            because("ArrayUtils")
        }
        implementation("org.exparity:hamcrest-date") {
            because("hamcrest-date.jar was historically shipped with JMeter")
        }
        implementation("com.miglayout:miglayout-swing")
    }
}

project("junit-sample") {
    dependencies {
        api("junit:junit")
    }
}

project("ldap") {
    dependencies {
        implementation("org.apache.commons:commons-text") {
            because("StringEscapeUtils")
        }
        implementation("org.apache.commons:commons-lang3") {
            because("StringUtils")
        }
    }
}

project("mail") {
    dependencies {
        api("javax.mail:mail") {
            exclude("javax.activation", "activation")
        }
        // There's no javax.activation:activation:1.2.0, so we use com.sun...
        runtimeOnly("com.sun.activation:javax.activation")
        // This is an API-only jar. javax.activation is present in Java 8,
        // however it is not there in Java 9
        compileOnly("javax.activation:javax.activation-api")
        implementation("org.apache.commons:commons-lang3") {
            because("StringUtils")
        }
        implementation("commons-io:commons-io") {
            because("IOUtils")
        }
    }
}

project("mongodb") {
    dependencies {
        api("org.mongodb:mongo-java-driver")
        implementation("org.apache.commons:commons-lang3") {
            because("StringUtils")
        }
    }
}

project("native") {
    dependencies {
        implementation("org.apache.commons:commons-lang3") {
            because("StringUtils")
        }
    }
}

project("tcp") {
    dependencies {
        implementation("org.apache.commons:commons-lang3") {
            because("ArrayUtils")
        }
        implementation("commons-io:commons-io") {
            because("IOUtils")
        }
    }
}
