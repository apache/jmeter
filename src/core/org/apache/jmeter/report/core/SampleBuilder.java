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
package org.apache.jmeter.report.core;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;

/**
 * This class is meant to simplify the building of samples.
 * <p>
 * It provides default behaviour for number formatting.<br>
 * The default numberformat used is a DecimalFormat whose pattern is the
 * following :<br>
 * <code>"#########0.00#"</code>
 * </p>
 * 
 * @since 2.14
 */
public class SampleBuilder {

    private static final DecimalFormat DEFAULT_FLOAT_FORMATER = new DecimalFormat(
	    "#########0.00#");

    static {
	DEFAULT_FLOAT_FORMATER.setRoundingMode(RoundingMode.HALF_DOWN);
	DEFAULT_FLOAT_FORMATER.setGroupingUsed(false);
	DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(
	        Locale.getDefault());
	newSymbols.setDecimalSeparator('.');
	DEFAULT_FLOAT_FORMATER.setDecimalFormatSymbols(newSymbols);
    }

    private final SampleMetadata metadata;

    private String[] data;

    private NumberFormat floatFormater;

    private int k = 0;

    private long row = 0;

    public SampleBuilder(SampleMetadata metadata, NumberFormat floatFormater) {
	if (metadata == null) {
        throw new ArgumentNullException("metadata");
    }

	if (floatFormater == null) {
        throw new ArgumentNullException("floatFormater");
    }

	this.floatFormater = floatFormater;
	this.metadata = metadata;
	this.data = new String[metadata.getColumnCount()];
	k = 0;
	row = 0;
    }

    public SampleBuilder(SampleMetadata metadata) {
	this(metadata, DEFAULT_FLOAT_FORMATER);
    }

    /**
     * @return the metadata
     */
    public final SampleMetadata getMetadata() {
	return metadata;
    }

    public SampleBuilder add(String e) {
	if (k < data.length) {
	    data[k++] = e;
	}
	return this;
    }

    public SampleBuilder add(long e) {
	add(Long.toString(e));
	return this;
    }

    public SampleBuilder add(double e) {
	add(floatFormater.format(e));
	return this;
    }

    public Sample build() {
	while (k < data.length) {
	    data[k++] = "";
	}
	String[] sampleData = new String[data.length];
	System.arraycopy(data, 0, sampleData, 0, data.length);
	Sample out = new Sample(row++, metadata, sampleData);
	k = 0;
	return out;
    }
}
