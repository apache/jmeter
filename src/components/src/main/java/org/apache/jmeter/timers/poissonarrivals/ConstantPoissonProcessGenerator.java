/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.timers.poissonarrivals;

import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.Random;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates events for Poisson processes, ensuring throughput*duration events will be present in each "duration"
 * @since 4.0
 */
public class ConstantPoissonProcessGenerator implements EventProducer {
    private static final Logger log = LoggerFactory.getLogger(ConstantPoissonProcessGenerator.class);

    private static final double PRECISION = 0.00001;

    private final Random rnd = new Random();
    private final ThroughputProvider throughputProvider;
    private final int batchSize;
    // TODO: implement
    @SuppressWarnings("unused")
    private final int batchThreadDelay;
    private final DurationProvider durationProvider;
    private final boolean logFirstSamples;

    private int batchItemIndex;
    private double lastThroughput;
    private double lastThroughputDurationFinish;
    private DoubleBuffer events;

    public ConstantPoissonProcessGenerator(
            ThroughputProvider throughput, int batchSize, int batchThreadDelay,
            DurationProvider duration,
            Long seed, boolean logFirstSamples) {
        this.throughputProvider = throughput;
        this.batchSize = batchSize;
        this.batchThreadDelay = batchThreadDelay;
        this.durationProvider = duration;
        this.logFirstSamples = logFirstSamples;
        if (seed != null && seed.intValue() != 0) {
            rnd.setSeed(seed);
        }
        ensureCapacity(0);
    }

    private void ensureCapacity(int size) {
        if (events != null && events.capacity() >= size) {
            return;
        }
        events = DoubleBuffer.allocate(size);
    }

    public void generateNext() {
        double throughput = this.throughputProvider.getThroughput();
        lastThroughput = throughput;
        if (batchSize > 1) {
            throughput /= batchSize;
        }
        batchItemIndex = 0;
        long duration = this.durationProvider.getDuration();
        int samples = (int) Math.ceil(throughput * duration);
        ensureCapacity(samples);
        long t = System.currentTimeMillis();
        events.clear();
        for (int i = 0; i < samples; i++) {
            events.put(lastThroughputDurationFinish + rnd.nextDouble() * duration);
        }
        Arrays.sort(events.array(), events.arrayOffset(), events.position());
        t = System.currentTimeMillis() - t;
        if (t > 1000) {
            log.warn("Spent {} ms while generating sequence of delays for {} samples, {} throughput, {} duration",
                    t, samples, throughput, duration);
        }
        lastThroughputDurationFinish += duration;
        if (logFirstSamples) {
            if (log.isDebugEnabled()) {
                log.debug("Generated {} events ({} required, rate {}) in {} ms",
                        events.position(), samples, throughput, t);
            }
            if (log.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Generated ").append(events.position()).append(" timings (");
                if (this.durationProvider instanceof AbstractTestElement) {
                    sb.append(((AbstractTestElement) this.durationProvider).getName());
                }
                sb.append(" ").append(samples).append(" required, rate ").append(throughput)
                        .append(", duration ").append(duration)
                        .append(") in ").append(t).append(" ms");
                sb.append(". First 15 events will be fired at: ");
                double prev = 0;
                for (int i = 0; i < events.position() && i < 15; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    double ev = events.get(i);
                    sb.append(ev);
                    sb.append(" (+").append(ev - prev).append(")");
                    prev = ev;
                }
                log.info(sb.toString());
            }
        }
        events.flip();
    }

    @Override
    public double next() {
        if ((batchItemIndex == 0 && !events.hasRemaining())
                || !valuesAreEqualWithPrecision(throughputProvider.getThroughput(),lastThroughput)) {
            generateNext();
        }
        if (batchSize == 1) {
            return events.get();
        }
        batchItemIndex++;
        if (batchItemIndex == 1) {
            // The first item advances the position
            return events.get();
        }
        if (batchItemIndex == batchSize) {
            batchItemIndex = 0;
        }
        // All the other items in the batch refer to the previous position
        // since #position() points to the next item to be returned
        return events.get(events.position() - 1);
    }

    private boolean valuesAreEqualWithPrecision(double throughput, double lastThroughput) {
        return Math.abs(throughput - lastThroughput) < PRECISION;
    }
}
