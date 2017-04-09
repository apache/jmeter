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

import javax.swing.Box;
import javax.swing.JCheckBox;

/**
 * Utility class to wrap a JCheckBox in a horizontal box.
 * This limits the clickable area to the label only, and not the width of its container.
 * See <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=58810">Bug 58810</a><br>
 * Note: using a JPanel affects the alignment within the container
 */
public class CheckBoxPanel {

    private CheckBoxPanel() {
        // not instantiable    
    }
    
    public static Box wrap(JCheckBox cb) {
        Box b = Box.createHorizontalBox();
        b.add(cb);
        return b;
    }
}
