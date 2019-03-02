/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.jmeter.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Logger wrapper to keep the log event for the record and delegate to the internal logger.
 */
public class LogRecordingDelegatingLogger implements Logger {

    private List<LogRecord> logRecords = Collections.synchronizedList(new LinkedList<>());

    private Logger delegate;

    public LogRecordingDelegatingLogger(Logger logger) {
        this.delegate = logger;
    }

    public Collection<LogRecord> getLogRecords() {
        return Collections.unmodifiableCollection(logRecords);
    }

    public int getLogRecordCount() {
        return logRecords.size();
    }

    public void clearLogRecords() {
        logRecords.clear();
    }

    @Override
    public void trace(String msg) {
        logRecords.add(new LogRecord(LogRecord.TRACE, msg));
        delegate.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.TRACE, format, arg));
        delegate.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.TRACE, format, arg1, arg2));
        delegate.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.TRACE, format, argArray));
        delegate.trace(format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.TRACE, msg, t));
        delegate.trace(msg, t);
    }

    @Override
    public void trace(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, msg));
        delegate.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, format, arg));
        delegate.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, format, arg1, arg2));
        delegate.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, format, argArray));
        delegate.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, msg, t));
        delegate.trace(marker, msg, t);
    }

    @Override
    public void debug(String msg) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, msg));
        delegate.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, format, arg));
        delegate.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, format, arg1, arg2));
        delegate.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, format, argArray));
        delegate.debug(format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, msg, t));
        delegate.debug(msg, t);
    }

    @Override
    public void debug(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, msg));
        delegate.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, format, arg));
        delegate.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, format, arg1, arg2));
        delegate.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, format, argArray));
        delegate.debug(marker, format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, msg, t));
        delegate.debug(marker, msg, t);
    }

    @Override
    public void info(String msg) {
        logRecords.add(new LogRecord(LogRecord.INFO, msg));
        delegate.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.INFO, format, arg));
        delegate.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.INFO, format, arg1, arg2));
        delegate.info(format,  arg1, arg2);
    }

    @Override
    public void info(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.INFO, format, argArray));
        delegate.info(format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.INFO, msg, t));
        delegate.info(msg, t);
    }

    @Override
    public void info(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, msg));
        delegate.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, format, arg));
        delegate.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, format, arg1, arg2));
        delegate.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, format, argArray));
        delegate.info(marker, format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, msg, t));
        delegate.info(marker, msg, t);
    }

    @Override
    public void warn(String msg) {
        logRecords.add(new LogRecord(LogRecord.WARN, msg));
        delegate.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.WARN, format, arg));
        delegate.warn(format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.WARN, format, arg1, arg2));
        delegate.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.WARN, format, argArray));
        delegate.warn(format, argArray);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.WARN, msg, t));
        delegate.warn(msg, t);
    }

    @Override
    public void warn(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, msg));
        delegate.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, format, arg));
        delegate.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, format, arg1, arg2));
        delegate.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, format, argArray));
        delegate.warn(marker, format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, msg, t));
        delegate.warn(marker, msg, t);
    }

    @Override
    public void error(String msg) {
        logRecords.add(new LogRecord(LogRecord.ERROR, msg));
        delegate.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.ERROR, format, arg));
        delegate.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.ERROR, format, arg1, arg2));
        delegate.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.ERROR, format, argArray));
        delegate.error(format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.ERROR, msg, t));
        delegate.error(msg, t);
    }

    @Override
    public void error(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, msg));
        delegate.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, format, arg));
        delegate.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, format, arg1, arg2));
        delegate.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, format, argArray));
        delegate.error(marker, format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, msg, t));
        delegate.error(marker, msg, t);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate.isDebugEnabled(marker);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate.isErrorEnabled(marker);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate.isInfoEnabled(marker);
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate.isTraceEnabled(marker);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate.isWarnEnabled(marker);
    }
}
