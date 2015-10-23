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
package org.apache.jmeter.report.processor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleException;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.processor.AbstractSampleConsumer;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Consume samples whose timestamp format is HH:mm and reproduce them as a long
 * value (for faster treatment later in the consuming chain).
 * 
 * @since 2.14
 */
public class NormalizerSampleConsumer extends AbstractSampleConsumer {

    private static final String PARSE_TIMESTAMP_EXCEPTION_MESSAGE = "Could not parse timeStamp <%> on sample %s";

    // TODO Get the date format from jmeter properties
    private static String DEFAULT_DATE_FORMAT = "HH:mm:ss";

    private int timestamp;

    private SimpleDateFormat df = new SimpleDateFormat(
	    JMeterUtils.getPropDefault(
	            "jmeter.save.saveservice.timestamp_format",
	            DEFAULT_DATE_FORMAT));

    private SampleMetadata sampleMetadata;

    public void startConsuming() {
	sampleMetadata = getConsumedMetadata(0);
	timestamp = sampleMetadata.indexOf(SampleMetadata.JMETER_TIMESTAMP);
	super.setProducedMetadata(sampleMetadata, 0);
	startProducing();
    }

    public void consume(Sample s, int channel) {
	Date date = null;
	try {
	    String tStr = s.getString(timestamp);
	    try {
		// Try to parse the timestamp assuming is a long
		date = new Date(Long.parseLong(tStr));
	    } catch (NumberFormatException ex) {
		// Try to parse the timestamp assuming it has HH:mm:ss format
		date = df.parse(tStr);
	    }
	} catch (Exception e) {
	    throw new SampleException(String.format(
		    PARSE_TIMESTAMP_EXCEPTION_MESSAGE, s.getString(timestamp),
		    s.toString()), e);
	}
	long time = date.getTime();
	int cc = sampleMetadata.getColumnCount();
	String[] data = new String[cc];
	for (int i = 0; i < cc; i++) {
	    if (i == timestamp) {
		data[i] = Long.toString(time);
	    } else {
		data[i] = s.getString(i);
	    }
	}
	Sample rewrited = new Sample(s.getSampleRow(), sampleMetadata, data);
	super.produce(rewrited, 0);
    }

    public void stopConsuming() {
	super.stopProducing();
    }
}
