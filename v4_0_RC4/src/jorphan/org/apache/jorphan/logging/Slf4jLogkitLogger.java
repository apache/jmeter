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

import org.apache.log.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper, implementing <code>org.apache.log.Logger</code> and delegating to the internal SLF4J logger.
 */
@Deprecated // Logger & Priority will be dropped in 3.3; so will this class be
class Slf4jLogkitLogger extends org.apache.log.Logger {

    private final Logger slf4jLogger;

    Slf4jLogkitLogger(final Logger slf4jLogger) {
        this.slf4jLogger = slf4jLogger;
    }

    @Override
    public boolean isDebugEnabled() {
        return slf4jLogger.isDebugEnabled();
    }

    @Override
    public void debug(String message, Throwable throwable) {
        slf4jLogger.debug(message, throwable);
    }

    @Override
    public void debug(String message) {
        slf4jLogger.debug(message);
    }

    @Override
    public boolean isInfoEnabled() {
        return slf4jLogger.isInfoEnabled();
    }

    @Override
    public void info(String message, Throwable throwable) {
        slf4jLogger.info(message, throwable);
    }

    @Override
    public void info(String message) {
        slf4jLogger.info(message);
    }

    @Override
    public boolean isWarnEnabled() {
        return slf4jLogger.isWarnEnabled();
    }

    @Override
    public void warn(String message, Throwable throwable) {
        slf4jLogger.warn(message, throwable);
    }

    @Override
    public void warn(String message) {
        slf4jLogger.warn(message);
    }

    @Override
    public boolean isErrorEnabled() {
        return slf4jLogger.isErrorEnabled();
    }

    @Override
    public void error(String message, Throwable throwable) {
        slf4jLogger.error(message, throwable);
    }

    @Override
    public void error(String message) {
        slf4jLogger.error(message);
    }

    @Override
    public boolean isFatalErrorEnabled() {
        return slf4jLogger.isErrorEnabled();
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        slf4jLogger.error(message, throwable);
    }

    @Override
    public void fatalError(String message) {
        slf4jLogger.error(message);
    }

    @Override
    public boolean isPriorityEnabled(Priority priority) {
        if (priority == Priority.FATAL_ERROR) {
            return slf4jLogger.isErrorEnabled();
        } else if (priority == Priority.ERROR) {
            return slf4jLogger.isErrorEnabled();
        } else if (priority == Priority.WARN) {
            return slf4jLogger.isWarnEnabled();
        } else if (priority == Priority.INFO) {
            return slf4jLogger.isInfoEnabled();
        } else if (priority == Priority.DEBUG) {
            return slf4jLogger.isDebugEnabled();
        }

        return false;
    }

    @Override
    public void log(Priority priority, String message, Throwable throwable) {
        if (priority == Priority.FATAL_ERROR) {
            slf4jLogger.error(message, throwable);
        } else if (priority == Priority.ERROR) {
            slf4jLogger.error(message, throwable);
        } else if (priority == Priority.WARN) {
            slf4jLogger.warn(message, throwable);
        } else if (priority == Priority.INFO) {
            slf4jLogger.info(message, throwable);
        } else if (priority == Priority.DEBUG) {
            slf4jLogger.debug(message, throwable);
        }
    }

    @Override
    public void log(Priority priority, String message) {
        if (priority == Priority.FATAL_ERROR) {
            slf4jLogger.error(message);
        } else if (priority == Priority.ERROR) {
            slf4jLogger.error(message);
        } else if (priority == Priority.WARN) {
            slf4jLogger.warn(message);
        } else if (priority == Priority.INFO) {
            slf4jLogger.info(message);
        } else if (priority == Priority.DEBUG) {
            slf4jLogger.debug(message);
        }
    }

    @Override
    public org.apache.log.Logger getChildLogger(String subCategory) {
        return new Slf4jLogkitLogger(LoggerFactory
                .getLogger(slf4jLogger.getName() + org.apache.log.Logger.CATEGORY_SEPARATOR + subCategory));
    }
}
