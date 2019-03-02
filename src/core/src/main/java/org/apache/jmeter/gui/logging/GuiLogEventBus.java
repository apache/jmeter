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

import java.util.ArrayList;
import java.util.List;

/**
 * GUI Log Event Bus.
 * @since 3.2
 */
public class GuiLogEventBus {

    /**
     * Registered GUI log event listeners array.
     */
    private List<GuiLogEventListener> listeners = new ArrayList<>();

    /**
     * Default constructor.
     */
    public GuiLogEventBus() {
        super();
    }

    /**
     * Register a GUI log event listener ({@link GuiLogEventListener}).
     * @param listener a GUI log event listener ({@link GuiLogEventListener})
     */
    public void registerEventListener(GuiLogEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister a GUI log event listener ({@link GuiLogEventListener}).
     * @param listener a GUI log event listener ({@link GuiLogEventListener})
     */
    public void unregisterEventListener(GuiLogEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Post a log event object.
     * @param logEventObject log event object
     */
    public void postEvent(LogEventObject logEventObject) {
        for (GuiLogEventListener listener : listeners) {
            listener.processLogEvent(logEventObject);
        }
    }
}
