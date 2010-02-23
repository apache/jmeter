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

package org.apache.jmeter.threads;

import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.LongProperty;

/**
 * ThreadGroup holds the settings for a JMeter thread group.
 * 
 * This class is intended to be ThreadSafe.
 */
public class ThreadGroup extends AbstractThreadGroup {
    private static final long serialVersionUID = 240L;

    /** Ramp-up time */
    public final static String RAMP_TIME = "ThreadGroup.ramp_time";

    /** Whether scheduler is being used */
    public final static String SCHEDULER = "ThreadGroup.scheduler";

    /** Scheduler absolute start time */
    public final static String START_TIME = "ThreadGroup.start_time";

    /** Scheduler absolute end time */
    public final static String END_TIME = "ThreadGroup.end_time";

    /** Scheduler duration, overrides end time */
    public final static String DURATION = "ThreadGroup.duration";

    /** Scheduler start delay, overrides start time */
    public final static String DELAY = "ThreadGroup.delay";

    /**
     * No-arg constructor.
     */
    public ThreadGroup() {
    }


    /**
     * Set whether scheduler is being used
     *
     * @param Scheduler true is scheduler is to be used
     */
    public void setScheduler(boolean Scheduler) {
        setProperty(new BooleanProperty(SCHEDULER, Scheduler));
    }

    /**
     * Get whether scheduler is being used
     *
     * @return true if scheduler is being used
     */
    public boolean getScheduler() {
        return getPropertyAsBoolean(SCHEDULER);
    }

    /**
     * Set the absolute StartTime value.
     *
     * @param stime -
     *            the StartTime value.
     */
    public void setStartTime(long stime) {
        setProperty(new LongProperty(START_TIME, stime));
    }

    /**
     * Get the absolute start time value.
     *
     * @return the start time value.
     */
    public long getStartTime() {
        return getPropertyAsLong(START_TIME);
    }

    /**
     * Get the desired duration of the thread group test run
     *
     * @return the duration (in secs)
     */
    public long getDuration() {
        return getPropertyAsLong(DURATION);
    }

    /**
     * Set the desired duration of the thread group test run
     *
     * @param duration
     *            in seconds
     */
    public void setDuration(long duration) {
        setProperty(new LongProperty(DURATION, duration));
    }

    /**
     * Get the startup delay
     *
     * @return the delay (in secs)
     */
    public long getDelay() {
        return getPropertyAsLong(DELAY);
    }

    /**
     * Set the startup delay
     *
     * @param delay
     *            in seconds
     */
    public void setDelay(long delay) {
        setProperty(new LongProperty(DELAY, delay));
    }

    /**
     * Set the EndTime value.
     *
     * @param etime -
     *            the EndTime value.
     */
    public void setEndTime(long etime) {
        setProperty(new LongProperty(END_TIME, etime));
    }

    /**
     * Get the end time value.
     *
     * @return the end time value.
     */
    public long getEndTime() {
        return getPropertyAsLong(END_TIME);
    }

    /**
     * Set the ramp-up value.
     *
     * @param rampUp
     *            the ramp-up value.
     */
    public void setRampUp(int rampUp) {
        setProperty(new IntegerProperty(RAMP_TIME, rampUp));
    }

    /**
     * Get the ramp-up value.
     *
     * @return the ramp-up value.
     */
    public int getRampUp() {
        return getPropertyAsInt(ThreadGroup.RAMP_TIME);
    }

   @Override
   public void scheduleThread(JMeterThread thread)
   {
       int rampUp = getRampUp();
       float perThreadDelay = ((float) (rampUp * 1000) / (float) getNumThreads());
       thread.setInitialDelay((int) (perThreadDelay * thread.getThreadNum()));

       scheduleThread(thread, this);
   }

    /**
     * This will schedule the time for the JMeterThread.
     *
     * @param thread
     * @param group
     */
    private void scheduleThread(JMeterThread thread, ThreadGroup group) {
        // if true the Scheduler is enabled
        if (group.getScheduler()) {
            long now = System.currentTimeMillis();
            // set the start time for the Thread
            if (group.getDelay() > 0) {// Duration is in seconds
                thread.setStartTime(group.getDelay() * 1000 + now);
            } else {
                long start = group.getStartTime();
                if (start < now) {
                    start = now; // Force a sensible start time
                }
                thread.setStartTime(start);
            }

            // set the endtime for the Thread
            if (group.getDuration() > 0) {// Duration is in seconds
                thread.setEndTime(group.getDuration() * 1000 + (thread.getStartTime()));
            } else {
                thread.setEndTime(group.getEndTime());
            }

            // Enables the scheduler
            thread.setScheduled(true);
        }
    }
}
