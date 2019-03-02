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
 * Renders a min or max value and hides the extrema as they
 * are used for initialization of the values and will likely be interpreted as
 * random values.
 * <p>
 * {@link Long#MIN_VALUE} and {@link Long#MAX_VALUE} will be displayed as
 * {@code #N/A}.
 *
 */
public class MinMaxLongRenderer extends NumberRenderer { // NOSONAR

    private static final long serialVersionUID = 1L;

    public MinMaxLongRenderer(String format) {
        super(format);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Long) {
            long longValue = ((Long) value).longValue();
            if (!(longValue == Long.MAX_VALUE || longValue == Long.MIN_VALUE)) {
                setText(formatter.format(longValue));
                return;
            }
        }
        setText("#N/A");
    }
}
