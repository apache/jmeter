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

package org.apache.jmeter.logging;

import java.io.Serializable;

import org.apache.log.Logger;
import org.apache.log.Priority;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * Logkit adapter for slf4j 
 * @since 3.0
 */
public class LogkitLoggerAdapter extends MarkerIgnoringBase implements Serializable {

    final transient Logger logger;

    private static final long serialVersionUID = -122848886791823355L;

    /**
     * @deprecated Only for use by JUnit
     */
    @Deprecated // only for Unit test usage
    public LogkitLoggerAdapter() {
        super();
        this.logger = null;
    }
    
    LogkitLoggerAdapter(org.apache.log.Logger logkitLogger) {
        this.logger = logkitLogger;
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String)
     */
    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object)
     */
    @Override
    public void debug(String format, Object arg) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.log(Priority.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object[])
     */
    @Override
    public void debug(String format, Object... args) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, args);
            logger.log(Priority.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void debug(String msg, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.log(Priority.DEBUG, msg, throwable);
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            logger.log(Priority.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String)
     */
    @Override
    public void error(String message) {
        if (logger.isErrorEnabled()) {
            logger.log(Priority.ERROR, message);
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object)
     */
    @Override
    public void error(String format, Object arg) {
        if (logger.isErrorEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.log(Priority.ERROR, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object[])
     */
    @Override
    public void error(String format, Object... args) {
        if (logger.isErrorEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, args);
            logger.log(Priority.ERROR, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void error(String message, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.log(Priority.ERROR, message, throwable);
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (logger.isErrorEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            logger.log(Priority.ERROR, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String)
     */
    @Override
    public void info(String message) {
        if (logger.isInfoEnabled()) {
            logger.log(Priority.INFO, message);
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object)
     */
    @Override
    public void info(String format, Object arg1) {
        if (logger.isInfoEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg1);
            logger.log(Priority.INFO, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object[])
     */
    @Override
    public void info(String format, Object... args) {
        if (logger.isInfoEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, args);
            logger.log(Priority.INFO, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void info(String message, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.log(Priority.INFO, message, throwable);
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (logger.isInfoEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            logger.log(Priority.INFO, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isErrorEnabled()
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isInfoEnabled()
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isTraceEnabled()
     */
    @Override
    public boolean isTraceEnabled() {
        return logger.isDebugEnabled();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isWarnEnabled()
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String)
     */
    @Override
    public void warn(String message) {
        if (logger.isWarnEnabled()) {
            logger.log(Priority.WARN, message);
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object)
     */
    @Override
    public void warn(String format, Object arg) {
        if (logger.isWarnEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.log(Priority.WARN, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object[])
     */
    @Override
    public void warn(String format, Object... args) {
        if (logger.isWarnEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, args);
            logger.log(Priority.WARN, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void warn(String message, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.log(Priority.WARN, message, throwable);
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (logger.isWarnEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            logger.log(Priority.WARN, ft.getMessage(), ft.getThrowable());
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String)
     */
    @Override
    public void trace(String message) {
        debug(message);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object)
     */
    @Override
    public void trace(String format, Object arg) {
        debug(format, arg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object[])
     */
    @Override
    public void trace(String format, Object... args) {
        debug(format, args);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void trace(String message, Throwable throwable) {
        debug(message, throwable);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void trace(String format, Object arg1, Object arg2) {
        debug(format, arg1, arg2);
    }
}
