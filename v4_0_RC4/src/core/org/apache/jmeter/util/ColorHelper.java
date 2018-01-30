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

package org.apache.jmeter.util;

import java.awt.Color;

/**
 * This class contains the static utility methods to manipulate colors.
 *
 */
public final class ColorHelper {
    /**
     * Private constructor to prevent instantiation.
     */
    private ColorHelper() {
    }

    /**
     * Given the <code>Color</code>, get the red, green and blue components.
     * Increment the lowest of the components by the indicated increment value.
     * If all the components are the same value increment in the order of red,
     * green and blue.
     *
     * @param col
     *            {@link Color} to start with
     * @param inc
     *            value to increment the color components
     * @return the color after change
     */
    public static Color changeColorCyclicIncrement(Color col, int inc) {
        int red = col.getRed();
        int green = col.getGreen();
        int blue = col.getBlue();
        int temp1 = Math.min(red, green);
        int temp2 = Math.min(temp1, blue);
        // now temp2 has the lowest of the three components
        if (red == temp2) {
            red += inc;
            red %= 256;
        } else if (green == temp2) {
            green += inc;
            green %= 256;
        } else if (blue == temp2) {
            blue += inc;
            blue %= 256;
        }
        return new Color(red, green, blue);
    }
}
