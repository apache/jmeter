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

package org.apache.jmeter.protocol.http.util

import org.slf4j.LoggerFactory
import org.xbill.DNS.AAAARecord
import org.xbill.DNS.ARecord
import org.xbill.DNS.Message
import org.xbill.DNS.Name
import org.xbill.DNS.Rcode
import org.xbill.DNS.Record
import org.xbill.DNS.Section
import org.xbill.DNS.Type
import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * A lightweight mock DNS server for testing purposes.
 *
 * Supports A and AAAA record queries with configurable responses. Useful for testing
 * DNS resolution behavior without requiring external DNS infrastructure.
 *
 * Example usage:
 * ```
 * val server = MockDnsServer(
 *     answers = mapOf("example.com" to listOf("192.0.2.1", "2001:db8::1"))
 * )
 * server.start()
 * // ... perform tests using server.boundPort() ...
 * server.close()
 * ```
 *
 * @param port The port to bind to (0 = auto-assign)
 * @param answers Static hostname-to-IP mappings (hostname without trailing dot)
 * @param ttl Time-to-live for DNS records
 * @param soTimeout Socket timeout for graceful shutdown checks
 * @param answerProvider Custom record provider for dynamic responses (overrides [answers])
 */
class MockDnsServer(
    private val port: Int = 0,
    private val answers: Map<String, List<String>> = emptyMap(),
    private val ttl: Duration = Duration.ofSeconds(60),
    private val soTimeout: Duration = Duration.ofMillis(250),
    private val answerProvider: ((Name, Int, Int) -> List<Record>)? = null
) : Closeable {
    companion object {
        private val logger = LoggerFactory.getLogger(MockDnsServer::class.java)
        private const val UDP_SIZE = 512
    }

    private val running = AtomicBoolean(false)
    private val requests = AtomicInteger(0)
    private val receivedQueries = ConcurrentLinkedQueue<QueryInfo>()

    /**
     * Returns the number of DNS queries received by this server.
     */
    val requestCount: Int get() = requests.get()

    /**
     * Returns a list of all DNS queries received by this server.
     * Useful for test assertions.
     */
    fun getReceivedQueries(): List<QueryInfo> = receivedQueries.toList()

    /**
     * Clears the history of received queries.
     */
    fun clearQueryHistory() = receivedQueries.clear()

    /**
     * Information about a received DNS query.
     */
    data class QueryInfo(
        val name: String,
        val type: Int,
        val typeName: String,
        val dclass: Int
    )

    // single-thread executor for serving; daemon thread to not hang JVM
    private val executor: ExecutorService = Executors.newSingleThreadExecutor { r ->
        Thread(r, "mock-dns-server-$port").apply { isDaemon = true }
    }

    @Volatile private var socket: DatagramSocket? = null

    var boundPort: Int = -1
        private set

    val localAddress: InetAddress get() = socket!!.localAddress

    /**
     * Starts the DNS server on the configured port.
     *
     * @return The port the server is bound to
     * @throws IllegalStateException if port is invalid
     * @throws java.net.BindException if port is already in use
     */
    fun start(): Int {
        if (!running.compareAndSet(false, true)) return boundPort // idempotent

        require(port in 0..65535) { "Port must be in range 0-65535, got: $port" }

        try {
            val ds = DatagramSocket(port)
            ds.soTimeout = soTimeout.toMillis().toInt()
            socket = ds
            boundPort = (ds.localSocketAddress as InetSocketAddress).port
            logger.debug("MockDnsServer started on port $boundPort")
            executor.execute { serve(ds) }
            return boundPort
        } catch (e: Exception) {
            running.set(false)
            throw e
        }
    }

    private fun serve(ds: DatagramSocket) {
        val buf = ByteArray(UDP_SIZE)
        while (running.get()) {
            try {
                val packet = DatagramPacket(buf, buf.size)
                ds.receive(packet) // times out periodically due to soTimeout
                requests.incrementAndGet()
                val request = Message(packet.data)
                val response = buildResponse(request)
                val wire = response.toWire()
                val out = DatagramPacket(wire, wire.size, packet.address, packet.port)
                ds.send(out)
            } catch (e: java.net.SocketTimeoutException) {
                // loop again, check running flag
            } catch (e: Exception) {
                if (running.get()) {
                    logger.debug("MockDnsServer error while serving", e)
                } else {
                    break
                }
            }
        }
    }

    private fun buildResponse(req: Message): Message {
        val q = req.getQuestion()

        // Record query for test inspection
        receivedQueries.offer(
            QueryInfo(
                name = q.name.toString(true).trimEnd('.'),
                type = q.type,
                typeName = Type.string(q.type),
                dclass = q.dClass
            )
        )

        val resp = Message(req.header.id)
        resp.addRecord(q, Section.QUESTION)
        resp.header.rcode = Rcode.NOERROR
        val records = provideRecords(q.name, q.type, q.dClass)
        if (records.isEmpty()) {
            resp.header.rcode = Rcode.NXDOMAIN
        } else {
            records.forEach { resp.addRecord(it, Section.ANSWER) }
        }
        return resp
    }

    private fun provideRecords(name: Name, type: Int, dclass: Int): List<Record> {
        answerProvider?.let { return it(name, type, dclass) }
        val key = name.toString(true).trimEnd('.') // canonical hostname key
        val ips = answers[key] ?: return emptyList()
        val ttl = this.ttl.toSeconds()
        return ips.mapNotNull { ip ->
            try {
                val addr = InetAddress.getByName(ip)
                val recordType = if (addr.address.size == 4) Type.A else Type.AAAA

                // Return record only if query type matches or is ANY
                when {
                    type == Type.ANY -> when (recordType) {
                        Type.A -> ARecord(name, dclass, ttl, addr)
                        Type.AAAA -> AAAARecord(name, dclass, ttl, addr)
                        else -> null
                    }
                    type == recordType -> when (recordType) {
                        Type.A -> ARecord(name, dclass, ttl, addr)
                        Type.AAAA -> AAAARecord(name, dclass, ttl, addr)
                        else -> null
                    }
                    else -> null // query type doesn't match this address type
                }
            } catch (e: Exception) {
                logger.debug("Failed to parse IP address: $ip", e)
                null
            }
        }
    }

    override fun close() {
        stop()
    }

    /**
     * Stops the DNS server gracefully.
     * This method is idempotent and can be called multiple times safely.
     */
    fun stop() {
        if (!running.compareAndSet(true, false)) return // idempotent
        logger.debug("Stopping MockDnsServer on port $boundPort")
        try {
            socket?.close()
        } catch (e: Exception) {
            logger.debug("Error closing socket", e)
        }
        socket = null
        executor.shutdownNow()
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                logger.warn("MockDnsServer executor did not terminate in time")
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
