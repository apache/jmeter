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

package org.apache.jmeter.timers.poissonarrivals;

import java.nio.DoubleBuffer;
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

    private Random rnd = new Random();
    private ThroughputProvider throughputProvider;
    private int batchSize;
    private int batchThreadDelay;
    private DurationProvider durationProvider;
    private double lastThroughput;
    private int exactLimit;
    private double allowedThroughputSurplus;
    private DoubleBuffer events;
    private double lastEvent;
    private final boolean logFirstSamples;

    public ConstantPoissonProcessGenerator(
            ThroughputProvider throughput, int batchSize, int batchThreadDelay,
            DurationProvider duration, int exactLimit, double allowedThroughputSurplus, 
            Long seed, boolean logFirstSamples) {
        this.throughputProvider = throughput;
        this.batchSize = batchSize;
        this.batchThreadDelay = batchThreadDelay;
        this.durationProvider = duration;
        this.exactLimit = exactLimit;
        this.allowedThroughputSurplus = allowedThroughputSurplus;
        this.logFirstSamples = logFirstSamples;
        if (seed != null && seed.intValue() != 0) {
            rnd.setSeed(seed);
        }
        ensureCapacity();
    }

    private void ensureCapacity() {
        int size = (int) Math.round((throughputProvider.getThroughput() * durationProvider.getDuration() + 1) * 3);
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
        long duration = this.durationProvider.getDuration();
        ensureCapacity();
        int samples = (int) Math.ceil(throughput * duration);
        double time;
        int i = 0;
        long t = System.currentTimeMillis();
        int loops = 0;
        double currentAllowedThroughputSurplus = samples < exactLimit ? 0.0d : this.allowedThroughputSurplus / 100;
        do {
            time = 0;
            events.clear();
            if (throughput < 1e-5) {
                log.info("Throughput should exceed zero");
                break;
            }
            if (duration < 5) {
                log.info("Duration should exceed 5 seconds");
                break;
            }
            i = 0;
            while (time < duration) {
                double u = rnd.nextDouble();
                // https://en.wikipedia.org/wiki/Exponential_distribution#Generating_exponential_variates
                double delay = -Math.log(1 - u) / throughput;
                time += delay;
                events.put(time + lastEvent);
                i++;
            }
            loops++;
        } while (System.currentTimeMillis() - t < 5000 &&
                (i < samples + 1 // not enough samples
                        || (i - 1 - samples) * 1.0f / samples > currentAllowedThroughputSurplus));
        t = System.currentTimeMillis() - t;
        if (t > 1000) {
            log.warn("Spent {} ms while generating sequence of delays for {} samples, {} throughput, {} duration",
                    t, samples, throughput, duration);
        }
        if (logFirstSamples) {
            if (log.isDebugEnabled()) {
                log.debug("Generated {} events ({} required, rate {}) in {} ms, restart was issued {} times",
                        events.position(), samples, throughput, t, loops);
            }
            if (log.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Generated ").append(events.position()).append(" timings (");
                if (this.durationProvider instanceof AbstractTestElement) {
                    sb.append(((AbstractTestElement) this.durationProvider).getName());
                }
                sb.append(" ").append(samples).append(" required, rate ").append(throughput)
                        .append(", duration ").append(duration)
                        .append(", exact lim ").append(exactLimit)
                        .append(", i").append(i)
                        .append(") in ").append(t)
                        .append(" ms, restart was issued ").append(loops).append(" times. ");
                sb.append("First 15 events will be fired at: ");
                double prev = 0;
                for (i = 0; i < events.position() && i < 15; i++) {
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
        if (batchSize > 1) {
            // If required to generate "pairs" of events, then just duplicate events in the buffer
            // TODO: for large batchSizes it makes sense to use counting instead
            DoubleBuffer tmpBuffer = DoubleBuffer.allocate(batchSize * events.remaining());
            while (events.hasRemaining()) {
                double curTime = events.get();
                for (int j = 0; j < batchSize; j++) {
                    tmpBuffer.put(curTime + j * batchThreadDelay);
                }
            }
            tmpBuffer.flip();
            events = tmpBuffer;
        }
    }

    @Override
    public double next() {
        if (!events.hasRemaining()
                || !valuesAreEqualWithPrecision(throughputProvider.getThroughput(),lastThroughput)) {
            generateNext();
        }
        lastEvent = events.get();
        return lastEvent;
    }

    private boolean valuesAreEqualWithPrecision(double throughput, double lastThroughput) {
        return Math.abs(throughput - lastThroughput) < PRECISION;
    }
}
