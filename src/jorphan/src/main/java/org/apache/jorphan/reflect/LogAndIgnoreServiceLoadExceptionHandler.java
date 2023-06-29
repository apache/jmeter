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

package org.apache.jorphan.reflect;

import org.slf4j.Logger;

/**
 * Logs all the failures to agiven {@link Logger} and ignores them.
 */
public class LogAndIgnoreServiceLoadExceptionHandler implements ServiceLoadExceptionHandler<Object> {
    private final Logger log;

    public LogAndIgnoreServiceLoadExceptionHandler(Logger log) {
        this.log = log;
    }

    @Override
    public void handle(Class<?> service, String className, Throwable throwable) {
        if (throwable instanceof NoClassDefFoundError) {
            if (throwable.getMessage().contains("javafx")) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Unable to load class {} for interface {}. The class depends on JavaFX which is missing in the current JVM. " +
                                    "Use JVM distribution with JavaFX, or add it as a library for the class to work",
                            className, service, throwable
                    );
                } else {
                    log.info(
                            "Class {} depends on JavaFX which is missing in the current JVM. " +
                                    "Use JVM distribution with JavaFX, or add it as a library for the class to work",
                            className
                    );
                }
            } else {
                log.error(
                        "Exception registering implementation: [{}] of interface: [{}], a dependency used by the plugin class is missing",
                        className, service, throwable
                );
            }
        } else {
            log.error(
                    "Exception registering implementation: [{}] of interface: [{}], a jar is probably missing",
                    className, service, throwable
            );
        }
    }
}
