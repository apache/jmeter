/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.protocol.jini.config;

/**
 * Class representing the basic connection parameters for a Jini connection
 *
 */
public class JiniConfiguration {

    private final String rmiRegistryUrl;
    private final String serviceName;
    private final String serviceInterface;

    public JiniConfiguration(String rmiRegistryUrl, String serviceName, String serviceInterface) {
        this.rmiRegistryUrl = rmiRegistryUrl;
        this.serviceName = serviceName;
        this.serviceInterface = serviceInterface;
    }

    public String getRmiRegistryUrl() {
        return rmiRegistryUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rmiRegistryUrl == null) ? 0 : rmiRegistryUrl.hashCode());
        result = prime * result + ((serviceInterface == null) ? 0 : serviceInterface.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JiniConfiguration other = (JiniConfiguration) obj;
        if (rmiRegistryUrl == null) {
            if (other.rmiRegistryUrl != null)
                return false;
        } else if (!rmiRegistryUrl.equals(other.rmiRegistryUrl))
            return false;
        if (serviceInterface == null) {
            if (other.serviceInterface != null)
                return false;
        } else if (!serviceInterface.equals(other.serviceInterface))
            return false;
        if (serviceName == null) {
            if (other.serviceName != null)
                return false;
        } else if (!serviceName.equals(other.serviceName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "JiniConfiguration [rmiRegistryUrl=" + rmiRegistryUrl + ", serviceName=" + serviceName + ", serviceInterface=" + serviceInterface + "]";
    }

}
