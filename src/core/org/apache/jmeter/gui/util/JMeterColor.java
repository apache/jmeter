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

import java.awt.Color;

public class JMeterColor extends Color {
    private static final long serialVersionUID = 240L;

    public static final Color dark_green = new JMeterColor(0F, .5F, 0F);

    public static final Color LAVENDER = new JMeterColor(206F / 255F, 207F / 255F, 1F);

    public static final Color purple = new JMeterColor(150 / 255F, 0, 150 / 255F);

    public JMeterColor(float r, float g, float b) {
        super(r, g, b);
    }

    public JMeterColor() {
        super(0, 0, 0);
    }
}
