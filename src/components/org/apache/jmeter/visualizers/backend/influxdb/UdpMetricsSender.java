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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Influxdb sender base on The Line Protocol. The Line Protocol is a text based
 * format for writing points to InfluxDB. Syntax : <measurement>[,<tag_key>=
 * <tag_value>[,<tag_key>=<tag_value>]] <field_key>=<field_value>[,<field_key>=
 * <field_value>] [<timestamp>] Each line, separated by the newline character,
 * represents a single point in InfluxDB. Line Protocol is whitespace sensitive.
 * 
 */
class UdpMetricsSender extends AbstractInfluxdbMetricsSender {
	private static final Logger log = LoggerFactory.getLogger(UdpMetricsSender.class);

	int SOCKET_CONNECT_TIMEOUT_MS = 1000;
	int SOCKET_TIMEOUT = 1000;

	private final Object lock = new Object();

	private String udpUrl;
	private int udpPort;

	private List<MetricTuple> metrics = new ArrayList<>();

	UdpMetricsSender() {
		super();
	}

	@Override
	public void setup(String influxdbUrl) throws Exception {
		try {
			udpUrl = influxdbUrl.split(":")[0];
			udpPort = Integer.parseInt(influxdbUrl.split(":")[1]);
		} catch (Exception e) {
			log.error("Influxdb url is wrong. The format shoule be <host/ip>:<port>");
		}
	}

	@Override
	public void addMetric(String mesurement, String tag, String field) {
		synchronized (lock) {
			metrics.add(new MetricTuple(mesurement, tag, field, System.currentTimeMillis()));
		}
	}

	@Override
	public void writeAndSendMetrics() {
		List<MetricTuple> tempMetrics;
		synchronized (lock) {
			if (metrics.isEmpty()) {
				return;
			}
			tempMetrics = metrics;
			metrics = new ArrayList<>(tempMetrics.size());
		}
		final List<MetricTuple> copyMetrics = tempMetrics;

		if (!copyMetrics.isEmpty()) {
			StringBuilder sb = new StringBuilder(copyMetrics.size() * 35);
			for (MetricTuple metric : copyMetrics) {
				// Add TimeStamp in nanosecond from epoch ( default in InfluxDB )
				sb.append(metric.measurement).append(metric.tag).append(" ") //$NON-NLS-1$
						.append(metric.field).append(" ").append(metric.timestamp + "000000").append("\n"); //$NON-NLS-3$
			}

			DatagramSocket ds = null;
			try {
				ds = new DatagramSocket();
			} catch (SocketException e) {
				log.error("Cannot open udp port!");
				return;
			}

			byte[] buf = sb.toString().getBytes();
			InetAddress destination = null;
			try {
				destination = InetAddress.getByName(udpUrl);
			} catch (UnknownHostException e) {
				log.error("Unknown host for udp");
			}
			DatagramPacket dp = new DatagramPacket(buf, buf.length, destination, udpPort);

			try {
				ds.send(dp);
			} catch (IOException e) {
				log.error("Error in transferring udp package");
			}
			ds.close();

			// We drop metrics in all cases
			copyMetrics.clear();
		}

	}

	@Override
	public void destroy() {

	}

}
