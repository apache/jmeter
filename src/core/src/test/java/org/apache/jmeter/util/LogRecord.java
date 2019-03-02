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

import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

/**
 * Log record.
 */
public class LogRecord {

    public static final int TRACE = LocationAwareLogger.TRACE_INT;
    public static final int DEBUG = LocationAwareLogger.DEBUG_INT;
    public static final int INFO = LocationAwareLogger.INFO_INT;
    public static final int WARN = LocationAwareLogger.WARN_INT;
    public static final int ERROR = LocationAwareLogger.ERROR_INT;

    private int level;
    private Marker marker;
    private String format;
    private Object[] arguments;
    private Throwable throwable;

    public LogRecord(int level, String msg) {
        this(level, null, msg, (Object []) null, null);
    }

    public LogRecord(int level, String format, Object arg) {
        this(level, null, format, new Object [] { arg }, null);
    }

    public LogRecord(int level, String format, Object arg1, Object arg2) {
        this(level, null, format, new Object [] { arg1, arg2 }, null);
    }

    public LogRecord(int level, String format, Object[] argArray) {
        this(level, null, format, argArray, null);
    }

    public LogRecord(int level, String msg, Throwable t) {
        this(level, null, msg, null, t);
    }

    public LogRecord(int level, Marker marker, String msg) {
        this(level, marker, msg, null, null);
    }

    public LogRecord(int level, Marker marker, String format, Object arg) {
        this(level, marker, format, new Object [] { arg }, null);
    }

    public LogRecord(int level, Marker marker, String format, Object arg1, Object arg2) {
        this(level, marker, format, new Object [] { arg1, arg2 }, null);
    }

    public LogRecord(int level, Marker marker, String format, Object[] argArray) {
        this(level, marker, format, argArray, null);
    }

    public LogRecord(int level, Marker marker, String msg, Throwable t) {
        this(level, marker, msg, null, t);
    }

    public LogRecord(int level, Marker marker, String format, Object[] argArray, Throwable throwable) {
        this.level = level;
        this.marker = marker;
        this.format = format;

        if (argArray != null) {
            this.arguments = new Object[argArray.length];
            System.arraycopy(argArray, 0, this.arguments, 0, argArray.length);
        }

        this.throwable = throwable;
    }

    public int getLevel() {
        return level;
    }

    public Marker getMarker() {
        return marker;
    }

    public String getFormat() {
        return format;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
