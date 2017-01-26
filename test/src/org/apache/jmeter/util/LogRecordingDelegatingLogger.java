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
import org.slf4j.ext.LoggerWrapper;

/**
 * Logger wrapper to keep the log event for the record and delegate to the internal logger.
 */
public class LogRecordingDelegatingLogger extends LoggerWrapper {

    private static final String FQCN = LogRecordingDelegatingLogger.class.getName();

    private List<LogRecord> logRecords = Collections.synchronizedList(new LinkedList<>());

    public LogRecordingDelegatingLogger(Logger logger) {
        super(logger, FQCN);
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
        super.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.TRACE, format, arg));
        super.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.TRACE, format, arg1, arg2));
        super.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.TRACE, format, argArray));
        super.trace(format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.TRACE, msg, t));
        super.trace(msg, t);
    }

    @Override
    public void trace(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, msg));
        super.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, format, arg));
        super.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, format, arg1, arg2));
        super.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, format, argArray));
        super.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.TRACE, marker, msg, t));
        super.trace(marker, msg, t);
    }

    @Override
    public void debug(String msg) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, msg));
        super.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, format, arg));
        super.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, format, arg1, arg2));
        super.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, format, argArray));
        super.debug(format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, msg, t));
        super.debug(msg, t);
    }

    @Override
    public void debug(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, msg));
        super.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, format, arg));
        super.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, format, arg1, arg2));
        super.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, format, argArray));
        super.debug(marker, format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.DEBUG, marker, msg, t));
        super.debug(marker, msg, t);
    }

    @Override
    public void info(String msg) {
        logRecords.add(new LogRecord(LogRecord.INFO, msg));
        super.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.INFO, format, arg));
        super.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.INFO, format, arg1, arg2));
        super.info(format,  arg1, arg2);
    }

    @Override
    public void info(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.INFO, format, argArray));
        super.info(format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.INFO, msg, t));
        super.info(msg, t);
    }

    @Override
    public void info(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, msg));
        super.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, format, arg));
        super.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, format, arg1, arg2));
        super.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, format, argArray));
        super.info(marker, format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.INFO, marker, msg, t));
        super.info(marker, msg, t);
    }

    @Override
    public void warn(String msg) {
        logRecords.add(new LogRecord(LogRecord.WARN, msg));
        super.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.WARN, format, arg));
        super.warn(format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.WARN, format, arg1, arg2));
        super.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.WARN, format, argArray));
        super.warn(format, argArray);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.WARN, msg, t));
        super.warn(msg, t);
    }

    @Override
    public void warn(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, msg));
        super.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, format, arg));
        super.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, format, arg1, arg2));
        super.warn(marker, format, arg1, arg2);
    }

    public void warn(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, format, argArray));
        super.warn(marker, format, argArray);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.WARN, marker, msg, t));
        super.warn(marker, msg, t);
    }

    public void error(String msg) {
        logRecords.add(new LogRecord(LogRecord.ERROR, msg));
        super.error(msg);
    }

    public void error(String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.ERROR, format, arg));
        super.error(format, arg);
    }

    public void error(String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.ERROR, format, arg1, arg2));
        super.error(format, arg1, arg2);
    }

    public void error(String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.ERROR, format, argArray));
        super.error(format, argArray);
    }

    public void error(String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.ERROR, msg, t));
        super.error(msg, t);
    }

    public void error(Marker marker, String msg) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, msg));
        super.error(marker, msg);
    }

    public void error(Marker marker, String format, Object arg) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, format, arg));
        super.error(marker, format, arg);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, format, arg1, arg2));
        super.error(marker, format, arg1, arg2);
    }

    public void error(Marker marker, String format, Object... argArray) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, format, argArray));
        super.error(marker, format, argArray);
    }

    public void error(Marker marker, String msg, Throwable t) {
        logRecords.add(new LogRecord(LogRecord.ERROR, marker, msg, t));
        super.error(marker, msg, t);
    }
}
