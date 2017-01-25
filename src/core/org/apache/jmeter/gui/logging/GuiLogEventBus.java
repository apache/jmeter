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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * GUI Log Event Bus.
 */
public class GuiLogEventBus {

    /**
     * Registered GUI log event listeners array.
     */
    private GuiLogEventListener [] listeners;

    /**
     * Default constructor.
     */
    public GuiLogEventBus() {
    }

    /**
     * Register a GUI log event listener ({@link GuiLogEventListener}).
     * @param listener a GUI log event listener ({@link GuiLogEventListener})
     */
    public void registerEventListener(GuiLogEventListener listener) {
        if (listeners == null) {
            listeners = new GuiLogEventListener[] { listener };
        } else {
            Set<GuiLogEventListener> set = new LinkedHashSet<>(Arrays.asList(listeners));
            set.add(listener);
            GuiLogEventListener [] arr = new GuiLogEventListener[set.size()];
            listeners = set.toArray(arr);
        }
    }

    /**
     * Unregister a GUI log event listener ({@link GuiLogEventListener}).
     * @param listener a GUI log event listener ({@link GuiLogEventListener})
     */
    public void unregisterEventListener(GuiLogEventListener listener) {
        if (listeners != null) {
            Set<GuiLogEventListener> set = new LinkedHashSet<>(Arrays.asList(listeners));
            set.remove(listener);
            GuiLogEventListener [] arr = new GuiLogEventListener[set.size()];
            listeners = set.toArray(arr);
        }
    }

    /**
     * Post a log event object.
     * @param logEvent log event object
     */
    public void postEvent(LogEventObject logEventObject) {
        if (listeners != null) {
            for (GuiLogEventListener listener : listeners) {
                listener.log(logEventObject);
            }
        }
    }
}
