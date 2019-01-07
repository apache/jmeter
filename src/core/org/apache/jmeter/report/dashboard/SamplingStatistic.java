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

package org.apache.jmeter.report.dashboard;

/**
 * Statistics about a Transaction (Sampler or TransactionController)
 * @since 5.1
 *
 */
public class SamplingStatistic {
    private String transaction;
    private long sampleCount;
    private long errorCount;
    private float errorPct;
    private double meanResTime;
    private double minResTime;
    private double maxResTime;
    private double pct1ResTime;
    private double pct2ResTime;
    private double pct3ResTime;
    private double throughput;
    private double receivedKBytesPerSec;
    private double sentKBytesPerSec;

    public SamplingStatistic() {
        super();
    }

    /**
     * @return the transaction
     */
    public String getTransaction() {
        return transaction;
    }

    /**
     * @param transaction the transaction to set
     */
    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    /**
     * @return the sampleCount
     */
    public long getSampleCount() {
        return sampleCount;
    }

    /**
     * @param sampleCount the sampleCount to set
     */
    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    /**
     * @return the errorCount
     */
    public long getErrorCount() {
        return errorCount;
    }

    /**
     * @param errorCount the errorCount to set
     */
    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    /**
     * @return the errorPct
     */
    public float getErrorPct() {
        return errorPct;
    }

    /**
     * @param errorPct the errorPct to set
     */
    public void setErrorPct(float errorPct) {
        this.errorPct = errorPct;
    }

    /**
     * @return the meanResTime
     */
    public double getMeanResTime() {
        return meanResTime;
    }

    /**
     * @param meanResTime the meanResTime to set
     */
    public void setMeanResTime(double meanResTime) {
        this.meanResTime = meanResTime;
    }

    /**
     * @return the minResTime
     */
    public double getMinResTime() {
        return minResTime;
    }

    /**
     * @param minResTime the minResTime to set
     */
    public void setMinResTime(double minResTime) {
        this.minResTime = minResTime;
    }

    /**
     * @return the maxResTime
     */
    public double getMaxResTime() {
        return maxResTime;
    }

    /**
     * @param maxResTime the maxResTime to set
     */
    public void setMaxResTime(double maxResTime) {
        this.maxResTime = maxResTime;
    }

    /**
     * @return the pct1ResTime
     */
    public double getPct1ResTime() {
        return pct1ResTime;
    }

    /**
     * @param pct1ResTime the pct1ResTime to set
     */
    public void setPct1ResTime(double pct1ResTime) {
        this.pct1ResTime = pct1ResTime;
    }

    /**
     * @return the pct2ResTime
     */
    public double getPct2ResTime() {
        return pct2ResTime;
    }

    /**
     * @param pct2ResTime the pct2ResTime to set
     */
    public void setPct2ResTime(double pct2ResTime) {
        this.pct2ResTime = pct2ResTime;
    }

    /**
     * @return the pct3ResTime
     */
    public double getPct3ResTime() {
        return pct3ResTime;
    }

    /**
     * @param pct3ResTime the pct3ResTime to set
     */
    public void setPct3ResTime(double pct3ResTime) {
        this.pct3ResTime = pct3ResTime;
    }

    /**
     * @return the throughput
     */
    public double getThroughput() {
        return throughput;
    }

    /**
     * @param throughput the throughput to set
     */
    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    /**
     * @return the receivedKBytesPerSec
     */
    public double getReceivedKBytesPerSec() {
        return receivedKBytesPerSec;
    }

    /**
     * @param receivedKBytesPerSec the receivedKBytesPerSec to set
     */
    public void setReceivedKBytesPerSec(double receivedKBytesPerSec) {
        this.receivedKBytesPerSec = receivedKBytesPerSec;
    }

    /**
     * @return the sentKBytesPerSec
     */
    public double getSentKBytesPerSec() {
        return sentKBytesPerSec;
    }

    /**
     * @param sentKBytesPerSec the sentKBytesPerSec to set
     */
    public void setSentKBytesPerSec(double sentKBytesPerSec) {
        this.sentKBytesPerSec = sentKBytesPerSec;
    }
}
