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

package org.apache.jmeter.timers;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.nio.DoubleBuffer;
import java.util.Random;

/**
 * Generates events for poisson processes, ensuring throughput*duration events will be present in each "duration"
 */
public class ConstantPoissonProcessGenerator implements EventProducer {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private Random rnd = new Random();
    public ThroughputProvider throughput;
    private int batchSize;
    private int batchThreadDelay;
    public DurationProvider duration;
    private double lastThroughput;
    private int exactLimit;
    private double allowedThroughputSurplus;
    private DoubleBuffer events;
    private double lastEvent;
    private Long seed;
    private final boolean logFirstSamples;

    public ConstantPoissonProcessGenerator(ThroughputProvider throughput, int batchSize, int batchThreadDelay,
                                           DurationProvider duration, int exactLimit, double allowedThroughputSurplus, Long seed,
                                           boolean logFirstSamples) {
        this.throughput = throughput;
        this.batchSize = batchSize;
        this.batchThreadDelay = batchThreadDelay;
        this.duration = duration;
        this.exactLimit = exactLimit;
        this.allowedThroughputSurplus = allowedThroughputSurplus;
        this.seed = seed;
        this.logFirstSamples = logFirstSamples;
        if (seed != null && seed.intValue() != 0) {
            rnd.setSeed(seed);
        }
        ensureCapacity();
    }

    private void ensureCapacity() {
        int size = (int) Math.round((throughput.getThroughput() * duration.getDuration() + 1) * 3);
        if (events != null && events.capacity() >= size) {
            return;
        }
        events = DoubleBuffer.allocate(size);
    }

    public void generateNext() {
        double throughput = this.throughput.getThroughput();
        lastThroughput = throughput;
        if (batchSize > 1) {
            throughput /= batchSize;
        }
        long duration = this.duration.getDuration();
        ensureCapacity();
        int samples = (int) Math.ceil(throughput * duration);
        double time;
        int i = 0;
        long t = System.currentTimeMillis();
        int loops = 0;
        double allowedThroughputSurplus = samples < exactLimit ? 0.0d : this.allowedThroughputSurplus / 100;
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
            for (i = 0; time < duration; i++) {
                double u = rnd.nextDouble();
                // https://en.wikipedia.org/wiki/Exponential_distribution#Generating_exponential_variates
                double delay = -Math.log(1 - u) / throughput;
                time += delay;
                events.put(time + lastEvent);
            }
            loops++;
        } while (System.currentTimeMillis() - t < 5000 &&
                (i < samples + 1 // not enough samples
                        || (i - 1 - samples) * 1.0f / samples > allowedThroughputSurplus));
        t = System.currentTimeMillis() - t;
        if (t > 1000) {
            log.warn("Spent " + t + " ms while generating sequence of delays for " + samples + " samples, " + throughput + " throughput, " + duration + " duration");
        }
        if (logFirstSamples) {
            if (log.isDebugEnabled()) {
                log.debug("Generated " + events.position() + " events (" + samples + " required, rate " + throughput + ") in " + t + " ms, restart was issued " + loops + " times");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Generated ").append(events.position()).append(" timings (");
            if (this.duration instanceof AbstractTestElement) {
                sb.append(((AbstractTestElement) this.duration).getName());
            }
            sb.append(" ").append(samples).append(" required, rate ").append(throughput).append(", duration ").append(duration);
            sb.append(", exact lim ").append(exactLimit).append(", i").append(i);
            sb.append(") in ").append(t).append(" ms, restart was issued ").append(loops).append(" times. ");
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
        if (!events.hasRemaining() || throughput.getThroughput() != lastThroughput) {
            generateNext();
        }
        lastEvent = events.get();
        return lastEvent;
    }
}
