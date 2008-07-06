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

package org.apache.jmeter.monitor.model;

/**
 * @version $Revision$
 */
public interface Worker {
    int getRequestProcessingTime();

    void setRequestProcessingTime(int value);

    long getRequestBytesSent();

    void setRequestBytesSent(long value);

    java.lang.String getCurrentQueryString();

    void setCurrentQueryString(java.lang.String value);

    java.lang.String getRemoteAddr();

    void setRemoteAddr(java.lang.String value);

    java.lang.String getCurrentUri();

    void setCurrentUri(java.lang.String value);

    java.lang.String getStage();

    void setStage(java.lang.String value);

    java.lang.String getVirtualHost();

    void setVirtualHost(java.lang.String value);

    java.lang.String getProtocol();

    void setProtocol(java.lang.String value);

    long getRequestBytesReceived();

    void setRequestBytesReceived(long value);

    java.lang.String getMethod();

    void setMethod(java.lang.String value);

}
