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

package org.apache.jorphan.logging;

import org.apache.log.Logger;
import org.apache.commons.logging.Log;

/**
 * Implementation of Commons Logging Log interface that delegates all
 * logging calls to Avalon Logkit 
 */
public class LogkitLogger implements Log {

    private final Logger logger;

    public LogkitLogger(String category) {
        logger = LoggingManager.getLoggerFor(category);
    }

    @Override
    public void debug(Object message) {
        logger.debug(String.valueOf(message));
    }

    @Override
    public void debug(Object message, Throwable throwable) {
        logger.debug(String.valueOf(message), throwable);
    }

    @Override
    public void error(Object message) {
        logger.error(String.valueOf(message));
    }

    @Override
    public void error(Object message, Throwable throwable) {
        logger.error(String.valueOf(message), throwable);
    }

    @Override
    public void fatal(Object message) {
        logger.fatalError(String.valueOf(message));
    }

    @Override
    public void fatal(Object message, Throwable throwable) {
        logger.fatalError(String.valueOf(message), throwable);
    }

    @Override
    public void info(Object message) {
        logger.info(String.valueOf(message));
    }

    @Override
    public void info(Object message, Throwable throwable) {
        logger.info(String.valueOf(message), throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isFatalErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return isDebugEnabled(); // Trace level is not supported, so we use debug
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void trace(Object message) {
        debug(message); // Trace level is not supported, so we use debug
    }

    @Override
    public void trace(Object message, Throwable throwable) {
        debug(message, throwable); // Trace level is not supported, so we use debug
    }

    @Override
    public void warn(Object message) {
        logger.warn(String.valueOf(message));
    }

    @Override
    public void warn(Object message, Throwable throwable) {
        logger.warn(String.valueOf(message), throwable);
    }

}
