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

package org.apache.jmeter.protocol.http.util.accesslog;

/**
 * Description:<br>
 * <br>
 * Generator is a base interface that defines the minimum methods needed to
 * implement a concrete generator. The reason for creating this interface is
 * eventually JMeter could use the logs directly rather than pre- process the
 * logs into a JMeter .jmx file. In situations where a test plan simulates load
 * from production logs, it is more efficient for JMeter to use the logs
 * directly.
 * <p>
 * From first hand experience, loading a test plan with 10K or more Requests
 * requires a lot of memory. It's important to keep in mind this type of testing
 * is closer to functional and regression testing than the typical stress tests.
 * Typically, this kind of testing is most useful for search sites that get a
 * large number of requests per day, but the request parameters vary
 * dramatically. E-commerce sites typically have limited inventory, therefore it
 * is better to design test plans that use data from the database.
 * </p>
 *
 */

public interface Generator {

    /**
     * close the generator
     */
    void close();

    /**
     * The host is the name of the server.
     *
     * @param host name of the server
     */
    void setHost(String host);

    /**
     * This is the label for the request, which is used in the logs and results.
     *
     * @param label label of the request
     */
    void setLabel(String label);

    /**
     * The method is the HTTP request method. It's normally POST or GET.
     *
     * @param post_get method of the HTTP request
     */
    void setMethod(String post_get);

    /**
     * Set the request parameters
     *
     * @param params request parameter
     */
    void setParams(NVPair[] params);

    /**
     * The path is the web page you want to test.
     *
     * @param path path of the web page
     */
    void setPath(String path);

    /**
     * The default port for HTTP is 80, but not all servers run on that port.
     *
     * @param port -
     *            port number
     */
    void setPort(int port);

    /**
     * Set the querystring for the request if the method is GET.
     *
     * @param querystring query string of the request
     */
    void setQueryString(String querystring);

    /**
     * The source logs is the location where the access log resides.
     *
     * @param sourcefile path to the access log file
     */
    void setSourceLogs(String sourcefile);

    /**
     * The target can be either a java.io.File or a Sampler. We make it generic,
     * so that later on we can use these classes directly from a HTTPSampler.
     *
     * @param target target to generate into
     */
    void setTarget(Object target);

    /**
     * The method is responsible for calling the necessary methods to generate a
     * valid request. If the generator is used to pre-process access logs, the
     * method wouldn't return anything. If the generator is used by a control
     * element, it should return the correct Sampler class with the required
     * fields set.
     *
     * @return prefilled sampler
     */
    Object generateRequest();

    /**
     * If the generator is converting the logs to a .jmx file, save should be
     * called.
     */
    void save();

    /**
     * The purpose of the reset is so Samplers can explicitly call reset to
     * create a new instance of HTTPSampler.
     *
     */
    void reset();
}
