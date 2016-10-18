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

package org.apache.jmeter.control.gui.wdc;

import java.awt.Color;
import java.text.DecimalFormat;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * This class renders the Probability column with a grey background with a
 * nicely formatted percentage string
 * 
 * NOTE:  This class was intended to be a internal class, but the requirement by
 * JMeterTest to instantiate all Serializable classes breaks unless this is public
 */
@SuppressWarnings("serial")
public class WeightedDistributionIneditableProbabilityRenderer
        extends DefaultTableCellRenderer {
    private static final String FORMAT = "###.####%";
    private final DecimalFormat formatter = new DecimalFormat(FORMAT);

    public WeightedDistributionIneditableProbabilityRenderer() {
        super();
        setBackground(Color.LIGHT_GRAY);
        setHorizontalAlignment(RIGHT);
    }

    public void setValue(Object value) {
        setText((value == null) ? "" : formatter.format(value));
    }
}
