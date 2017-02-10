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
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consume samples using the JMeter timestamp property (defaulting to {@link SampleSaveConfiguration#MILLISECONDS}) and reproduce them as a long
 * value (for faster treatment later in the consuming chain).
 * 
 * @since 3.0
 */
public class NormalizerSampleConsumer extends AbstractSampleConsumer {

    private static final Logger log = LoggerFactory.getLogger(NormalizerSampleConsumer.class);

    private static final String TIMESTAMP_FORMAT = 
            JMeterUtils.getPropDefault(
                    "jmeter.save.saveservice.timestamp_format", // $NON-NLS-1$
                    SampleSaveConfiguration.MILLISECONDS);

    private static final String PARSE_TIMESTAMP_EXCEPTION_MESSAGE = 
            "Could not parse timeStamp <%s> using format defined by property jmeter.save.saveservice.timestamp_format=%s on sample %s ";

    /**
     * index of the timeStamp column
     */
    private int timestamp;

    /**
     * is format ms
     */
    private boolean isMillisFormat;
    
    /**
     * null if format is isMillisFormat is true
     */
    private final SimpleDateFormat dateFormat = createFormatter();

    private SampleMetadata sampleMetadata;

    @Override
    public void startConsuming() {
        sampleMetadata = getConsumedMetadata(0);
        timestamp = sampleMetadata.ensureIndexOf(CSVSaveService.TIME_STAMP);
        super.setProducedMetadata(sampleMetadata, 0);
        startProducing();
    }

    /**
     * @return null if format is ms or a SimpleDateFormat
     * @throws SampleException is format is none
     */
    private SimpleDateFormat createFormatter() {
        if(SampleSaveConfiguration.NONE.equalsIgnoreCase(TIMESTAMP_FORMAT)) {
            throw new SampleException("'none' format for 'jmeter.save.saveservice.timestamp_format' property is not accepted for report generation");
        }
        log.info("Using format, '{}', to parse timeStamp field", TIMESTAMP_FORMAT);
        
        isMillisFormat = SampleSaveConfiguration.MILLISECONDS.equalsIgnoreCase(TIMESTAMP_FORMAT);
        SimpleDateFormat formatter = null;
        // Prepare for a pretty date
        if (!isMillisFormat) {
            formatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
        } 
        return formatter;
    }

    @Override
    public void consume(Sample s, int channel) {
        Date date = null;
        try {
            String tStr = s.getData(timestamp);
            if(isMillisFormat) {
                date = new Date(Long.parseLong(tStr));
            } else {
                date = dateFormat.parse(tStr);                    
            }
        } catch (Exception e) {
            throw new SampleException(String.format(
                    PARSE_TIMESTAMP_EXCEPTION_MESSAGE, s.getData(timestamp),
                    TIMESTAMP_FORMAT, s.toString()), e);
        }
        long time = date.getTime();
        int cc = sampleMetadata.getColumnCount();
        String[] data = new String[cc];
        for (int i = 0; i < cc; i++) {
            if (i == timestamp) {
                data[i] = Long.toString(time);
            } else {
                data[i] = s.getData(i);
            }
        }
        Sample rewritten = new Sample(s.getSampleRow(), sampleMetadata, data);
        super.produce(rewritten, 0);
    }

    @Override
    public void stopConsuming() {
        super.stopProducing();
    }
}
