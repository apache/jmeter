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
package org.apache.jmeter.gui.logging;

import java.util.EventObject;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;

/**
 * Log event object.
 */
public class LogEventObject extends EventObject {

    private static final long serialVersionUID = 1L;

    private Level level;
    private String seralizedString;

    public LogEventObject(Object source, String seralizedString) {
        super(source);
        level = ((LogEvent) source).getLevel();
        this.seralizedString = seralizedString;
    }

    public boolean isMoreSpecificThanError() {
        return level.isMoreSpecificThan(Level.ERROR);
    }

    public boolean isMoreSpecificThanWarn() {
        return level.isMoreSpecificThan(Level.WARN);
    }

    public boolean isMoreSpecificThanInfo() {
        return level.isMoreSpecificThan(Level.INFO);
    }

    @Override
    public String toString() {
        if (seralizedString != null) {
            return seralizedString;
        }

        return super.toString();
    }
}
