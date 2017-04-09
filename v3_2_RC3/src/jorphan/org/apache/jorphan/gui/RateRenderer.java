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

package org.apache.jorphan.gui;

/**
 * Renders a rate in a JTable.
 *
 * The output is in units appropriate to its dimension:
 * <p>
 * The number is represented in one of:
 * - requests/second
 * - requests/minute
 * - requests/hour.
 * <p>
 * Examples: "34.2/sec" "0.1/sec" "43.0/hour" "15.9/min"
 */
public class RateRenderer extends NumberRenderer{ // NOSONAR 7 parents is OK in this case

    private static final long serialVersionUID = 240L;

    public RateRenderer(String format) {
        super(format);
    }

    @Override
    public void setValue(Object value) {
        if (!(value instanceof Double)) {
            setText("#N/A");
            return;
        }
        double rate = ((Double) value).doubleValue();
        if (Double.compare(rate,Double.MAX_VALUE)==0){
            setText("#N/A");
            return;
        }

        String unit = "sec";

        if (rate < 1.0) {
            rate *= 60.0;
            unit = "min";
        }
        if (rate < 1.0) {
            rate *= 60.0;
            unit = "hour";
        }
        setText(formatter.format(rate) + "/" + unit);
    }
}
