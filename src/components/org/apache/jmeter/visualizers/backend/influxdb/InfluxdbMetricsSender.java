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

package org.apache.jmeter.visualizers.backend.influxdb;

interface InfluxdbMetricsSender {

    /**
	 * One data point in InfluxDB is represented by a measurement name, a tag
	 * set and a field set ( optionally a timestamp )
	 */
	final class MetricTuple {
		String measurment;
		String tag;
		String field;

		MetricTuple(String measurment, String tag, String field) {
			this.measurment = measurment;
			this.tag = tag;
			this.field = field;
		}
	}

	public abstract void addMetric(String measurment, String tag, String field);


	public void setup(String influxdbUrl);

    /**
	 * Write metrics to Influxdb with HTTP API with InfluxDB’s Line Protocol
	 */
	public abstract void writeAndSendMetrics();

	public abstract void destroy();

}
