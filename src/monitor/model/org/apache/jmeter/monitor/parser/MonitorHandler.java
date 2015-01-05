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
 */
package org.apache.jmeter.monitor.parser;

// import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.jmeter.monitor.model.ObjectFactory;
import org.apache.jmeter.monitor.model.Connector;
import org.apache.jmeter.monitor.model.Jvm;
import org.apache.jmeter.monitor.model.Memory;
import org.apache.jmeter.monitor.model.RequestInfo;
import org.apache.jmeter.monitor.model.Status;
import org.apache.jmeter.monitor.model.ThreadInfo;
import org.apache.jmeter.monitor.model.Worker;
import org.apache.jmeter.monitor.model.Workers;
import org.apache.jmeter.monitor.model.WorkersImpl;

public class MonitorHandler extends DefaultHandler {
    // private boolean startDoc = false;
    // private boolean endDoc = false;
    private final ObjectFactory factory;

    private Stack<Object> stacktree;

    private Status status;

    private Jvm jvm;

    private Memory memory;

    private Connector connector;

    private ThreadInfo threadinfo;

    private RequestInfo requestinfo;

    private Worker worker;

    private Workers workers;

    // private List workerslist;

    /**
     * @param factory {@link ObjectFactory} to use
     */
    public MonitorHandler(ObjectFactory factory) {
        super();
        this.factory = factory;
    }

    @Override
    public void startDocument() throws SAXException {
        // this.startDoc = true;
        // Reset all work variables so reusing the instance starts afresh.
        this.stacktree = new Stack<Object>();
        this.status = null;
        this.jvm = null;
        this.memory = null;
        this.connector = null;
        this.threadinfo = null;
        this.requestinfo = null;
        this.worker = null;
        this.workers = null;   
    }

    /** {@inheritDoc} */
    @Override
    public void endDocument() throws SAXException {
        // this.startDoc = false;
        // this.endDoc = true;
    }

    /**
     * Receive notification of the start of an element.
     *
     * <p>
     * By default, do nothing. Application writers may override this method in a
     * subclass to take specific actions at the start of each element (such as
     * allocating a new tree node or writing output to a file).
     * </p>
     *
     * @param uri
     *            The namespace uri, or the empty string, if no namespace is available
     * @param localName
     *            The element type name.
     * @param qName
     *            The qualified name, or the empty string (must not be <code>null</code>)
     * @param attributes
     *            The specified or defaulted attributes.
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals(Constants.STATUS)) {
            status = factory.createStatus();
            stacktree.push(status);
        } else if (qName.equals(Constants.JVM)) {
            jvm = factory.createJvm();
            if (stacktree.peek() instanceof Status) {
                status.setJvm(jvm);
                stacktree.push(jvm);
            }
        } else if (qName.equals(Constants.MEMORY)) {
            memory = factory.createMemory();
            if (stacktree.peek() instanceof Jvm) {
                stacktree.push(memory);
                if (attributes != null) {
                    for (int idx = 0; idx < attributes.getLength(); idx++) {
                        String attr = attributes.getQName(idx);
                        if (attr.equals(Constants.MEMORY_FREE)) {
                            memory.setFree(parseLong(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.MEMORY_TOTAL)) {
                            memory.setTotal(parseLong(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.MEMORY_MAX)) {
                            memory.setMax(parseLong(attributes.getValue(idx)));
                        }
                    }
                }
                jvm.setMemory(memory);
            }
        } else if (qName.equals(Constants.CONNECTOR)) {
            connector = factory.createConnector();
            if (stacktree.peek() instanceof Status || stacktree.peek() instanceof Connector) {
                status.addConnector(connector);
                stacktree.push(connector);
                if (attributes != null) {
                    for (int idx = 0; idx < attributes.getLength(); idx++) {
                        String attr = attributes.getQName(idx);
                        if (attr.equals(Constants.ATTRIBUTE_NAME)) {
                            connector.setName(attributes.getValue(idx));
                        }
                    }
                }
            }
        } else if (qName.equals(Constants.THREADINFO)) {
            threadinfo = factory.createThreadInfo();
            if (stacktree.peek() instanceof Connector) {
                stacktree.push(threadinfo);
                connector.setThreadInfo(threadinfo);
                if (attributes != null) {
                    for (int idx = 0; idx < attributes.getLength(); idx++) {
                        String attr = attributes.getQName(idx);
                        if (attr.equals(Constants.MAXTHREADS)) {
                            threadinfo.setMaxThreads(parseInt(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.MINSPARETHREADS)) {
                            threadinfo.setMinSpareThreads(parseInt(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.MAXSPARETHREADS)) {
                            threadinfo.setMaxSpareThreads(parseInt(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.CURRENTTHREADCOUNT)) {
                            threadinfo.setCurrentThreadCount(parseInt(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.CURRENTBUSYTHREADS)) {
                            threadinfo.setCurrentThreadsBusy(parseInt(attributes.getValue(idx)));
                        }
                    }
                }
            }
        } else if (qName.equals(Constants.REQUESTINFO)) {
            requestinfo = factory.createRequestInfo();
            if (stacktree.peek() instanceof Connector) {
                stacktree.push(requestinfo);
                connector.setRequestInfo(requestinfo);
                if (attributes != null) {
                    for (int idx = 0; idx < attributes.getLength(); idx++) {
                        String attr = attributes.getQName(idx);
                        if (attr.equals(Constants.MAXTIME)) {
                            requestinfo.setMaxTime(parseInt(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.PROCESSINGTIME)) {
                            requestinfo.setProcessingTime(parseInt(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.REQUESTCOUNT)) {
                            requestinfo.setRequestCount(parseInt(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.ERRORCOUNT)) {
                            requestinfo.setErrorCount(parseInt(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.BYTESRECEIVED)) {
                            requestinfo.setBytesReceived(parseLong(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.BYTESSENT)) {
                            requestinfo.setBytesSent(parseLong(attributes.getValue(idx)));
                        }
                    }
                }
            }
        } else if (qName.equals(Constants.WORKERS)) {
            workers = factory.createWorkers();
            if (stacktree.peek() instanceof Connector) {
                connector.setWorkers(workers);
                stacktree.push(workers);
            }
        } else if (qName.equals(Constants.WORKER)) {
            worker = factory.createWorker();
            if (stacktree.peek() instanceof Workers || stacktree.peek() instanceof Worker) {
                stacktree.push(worker);
                ((WorkersImpl) workers).addWorker(worker);
                if (attributes != null) {
                    for (int idx = 0; idx < attributes.getLength(); idx++) {
                        String attr = attributes.getQName(idx);
                        if (attr.equals(Constants.STAGE)) {
                            worker.setStage(attributes.getValue(idx));
                        } else if (attr.equals(Constants.REQUESTPROCESSINGTIME)) {
                            worker.setRequestProcessingTime(parseInt(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.REQUESTBYTESSENT)) {
                            worker.setRequestBytesSent(parseLong(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.REQUESTBYTESRECEIVED)) {
                            worker.setRequestBytesReceived(parseLong(attributes.getValue(idx)));
                        } else if (attr.equals(Constants.REMOTEADDR)) {
                            worker.setRemoteAddr(attributes.getValue(idx));
                        } else if (attr.equals(Constants.VIRTUALHOST)) {
                            worker.setVirtualHost(attributes.getValue(idx));
                        } else if (attr.equals(Constants.METHOD)) {
                            worker.setMethod(attributes.getValue(idx));
                        } else if (attr.equals(Constants.CURRENTURI)) {
                            worker.setCurrentUri(attributes.getValue(idx));
                        } else if (attr.equals(Constants.CURRENTQUERYSTRING)) {
                            worker.setCurrentQueryString(attributes.getValue(idx));
                        } else if (attr.equals(Constants.PROTOCOL)) {
                            worker.setProtocol(attributes.getValue(idx));
                        }
                    }
                }
            }
        }
    }

    /**
     * Receive notification of the end of an element.
     *
     * <p>
     * By default, do nothing. Application writers may override this method in a
     * subclass to take specific actions at the end of each element (such as
     * finalising a tree node or writing output to a file).
     * </p>
     *
     * @param uri
     *            the namespace uri, or the empty string, if no namespace is available
     * @param localName
     *            The element type name.
     * @param qName
     *            The specified or defaulted attributes.
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(Constants.STATUS)) {
            if (stacktree.peek() instanceof Status) {
                stacktree.pop();
            }
        } else if (qName.equals(Constants.JVM)) {
            if (stacktree.peek() instanceof Jvm) {
                stacktree.pop();
            }
        } else if (qName.equals(Constants.MEMORY)) {
            if (stacktree.peek() instanceof Memory) {
                stacktree.pop();
            }
        } else if (qName.equals(Constants.CONNECTOR)) {
            if (stacktree.peek() instanceof Connector || stacktree.peek() instanceof Connector) {
                stacktree.pop();
            }
        } else if (qName.equals(Constants.THREADINFO)) {
            if (stacktree.peek() instanceof ThreadInfo) {
                stacktree.pop();
            }
        } else if (qName.equals(Constants.REQUESTINFO)) {
            if (stacktree.peek() instanceof RequestInfo) {
                stacktree.pop();
            }
        } else if (qName.equals(Constants.WORKERS)) {
            if (stacktree.peek() instanceof Workers) {
                stacktree.pop();
            }
        } else if (qName.equals(Constants.WORKER)) {
            if (stacktree.peek() instanceof Worker || stacktree.peek() instanceof Worker) {
                stacktree.pop();
            }
        }
    }

    /**
     * Receive notification of character data inside an element.
     *
     * <p>
     * By default, do nothing. Application writers may override this method to
     * take specific actions for each chunk of character data (such as adding
     * the data to a node or buffer, or printing it to a file).
     * </p>
     *
     * @param ch
     *            The characters.
     * @param start
     *            The start position in the character array.
     * @param length
     *            The number of characters to use from the character array.
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
    }

    /**
     * Convienance method for parsing long. If the string was not a number, the
     * method returns zero.
     *
     * @param data string representation of a {@link Long}
     * @return the value as a long
     */
    public long parseLong(String data) {
        long val = 0;
        if (data.length() > 0) {
            try {
                val = Long.parseLong(data);
            } catch (NumberFormatException e) {
                val = 0;
            }
        }
        return val;
    }

    /**
     * Convienance method for parsing integers.
     *
     * @param data string representation of an {@link Integer}
     * @return the value as an integer
     */
    public int parseInt(String data) {
        int val = 0;
        if (data.length() > 0) {
            try {
                val = Integer.parseInt(data);
            } catch (NumberFormatException e) {
                val = 0;
            }
        }
        return val;
    }

    /**
     * method returns the status object.
     *
     * @return the status
     */
    public Status getContents() {
        return this.status;
    }
}
