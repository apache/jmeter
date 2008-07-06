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

package org.apache.jmeter.gui.util;

import java.awt.Component;

import javax.swing.SwingUtilities;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/*
 * Note: This helper class appeared in JavaWorld in June 2001
 * (http://www.javaworld.com) and was written by Michael Daconta.
 *
 */
public class FocusRequester implements Runnable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private final Component comp;

    public FocusRequester(Component comp) {
        this.comp = comp;
        try {
            SwingUtilities.invokeLater(this);
        } catch (Exception e) {
            log.error("", e); // $NON-NLS-1$
        }
    }

    public void run() {
        comp.requestFocus();
    }
}
