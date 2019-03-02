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

package org.apache.jmeter.rmi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RMI Helper class
 * @since 4.0
 */
public final class RmiUtils {
    private static final Logger log = LoggerFactory.getLogger(RmiUtils.class);

    public static final int DEFAULT_RMI_REGISTRY_PORT = JMeterUtils
            .getPropDefault("server_port", 0); // $NON-NLS-1$

    public static final int DEFAULT_RMI_PORT = JMeterUtils
            .getPropDefault("server.rmi.port", 1099); // $NON-NLS-1$

    public static final int DEFAULT_LOCAL_PORT = JMeterUtils
            .getPropDefault("server.rmi.localport", 0); // $NON-NLS-1$

    private static final String KEYSTORE_TYPE = JMeterUtils
            .getPropDefault("server.rmi.ssl.keystore.type", "JKS");

    private static final String KEYSTORE_FILE = JMeterUtils
            .getPropDefault("server.rmi.ssl.keystore.file", "rmi_keystore.jks");

    private static final String KEYSTORE_PASSWORD = JMeterUtils
            .getPropDefault("server.rmi.ssl.keystore.password", "changeit");

    private static final String KEYSTORE_ALIAS = JMeterUtils
            .getPropDefault("server.rmi.ssl.keystore.alias", "rmi");

    private static final String TRUSTSTORE_TYPE = JMeterUtils
            .getPropDefault("server.rmi.ssl.truststore.type", KEYSTORE_TYPE);

    private static final String TRUSTSTORE_FILE = JMeterUtils
            .getPropDefault("server.rmi.ssl.truststore.file", KEYSTORE_FILE);

    private static final String TRUSTSTORE_PASSWORD = JMeterUtils
            .getPropDefault("server.rmi.ssl.truststore.password",
                    KEYSTORE_PASSWORD);

    private static final boolean SSL_DISABLED = JMeterUtils
            .getPropDefault("server.rmi.ssl.disable", false);

    static{
        if (DEFAULT_LOCAL_PORT != 0){
            System.out.println("Using local port: " + DEFAULT_LOCAL_PORT); // NOSONAR
        }
    }

    private RmiUtils() {
        super();
    }

    public static RMIClientSocketFactory createClientSocketFactory() {
        if (SSL_DISABLED) {
            log.info("Disabling SSL for RMI as server.rmi.ssl.disable is set to 'true'");
            return null;
        }
        if (StringUtils.isBlank(KEYSTORE_FILE)) {
            Validate.validState(SSL_DISABLED,
                    "No keystore for RMI over SSL specified. Set 'server.rmi.ssl.disable' to true, if this is intentional,"
                    + "if not run create-rmi-keystore.bat/create-rmi-keystore.sh to create a keystore and distribute it on client and servers"
                    + "used for distributed testing.");
            return null;
        }
        final SSLRMIClientSocketFactory factory = new SSLRMIClientSocketFactory();
        factory.setAlias(KEYSTORE_ALIAS);
        factory.setKeystore(KEYSTORE_FILE, KEYSTORE_TYPE, KEYSTORE_PASSWORD);
        factory.setTruststore(TRUSTSTORE_FILE, TRUSTSTORE_TYPE, TRUSTSTORE_PASSWORD);
        return factory;
    }

    public static RMIServerSocketFactory createServerSocketFactory() throws RemoteException {
        if (SSL_DISABLED) {
            log.info("Disabling SSL for RMI as server.rmi.ssl.disable is set to 'true'");
            return null;
        }
        if (StringUtils.isBlank(KEYSTORE_FILE)) {
            Validate.validState(SSL_DISABLED,
                    "No keystore for RMI over SSL specified. Set 'server.rmi.ssl.disable' to true, if this is intentional.");
            return new RMIServerSocketFactoryImpl(getRmiHost());
        }
        SSLRMIServerSocketFactory factory = new SSLRMIServerSocketFactory(getRmiHost());
        factory.setAlias(KEYSTORE_ALIAS);
        factory.setNeedClientAuth(true);
        factory.setKeystore(KEYSTORE_FILE, KEYSTORE_TYPE, KEYSTORE_PASSWORD);
        factory.setTruststore(TRUSTSTORE_FILE, TRUSTSTORE_TYPE, TRUSTSTORE_PASSWORD);
        return factory;
    }

    /**
     * @return the configure address for usage by RMI
     * @throws RemoteException when no valid address can be found
     */
    public static InetAddress getRmiHost() throws RemoteException {
        InetAddress localHost=null;
        // Bug 47980 - allow override of local hostname
        String host = System.getProperties().getProperty("java.rmi.server.hostname"); // $NON-NLS-1$
        try {
            if( host==null ) {
                log.info("System property 'java.rmi.server.hostname' is not defined, using localHost address");
                localHost = InetAddress.getLocalHost();
            } else {
                log.info("Resolving by name the value of System property 'java.rmi.server.hostname': {}", host);
                localHost = InetAddress.getByName(host);
            }
        } catch (UnknownHostException e) {
            throw new RemoteException("Cannot start. Unable to get local host IP address.", e);
        }
        if (log.isInfoEnabled()) {
            log.info("Local IP address={}", localHost.getHostAddress());
        }
        // BUG 52469 : Allow loopback address for SSH Tunneling of RMI traffic
        if (host == null && localHost.isLoopbackAddress()){
            String hostName = localHost.getHostName();
            throw new RemoteException("Cannot start. " + hostName + " is a loopback address.");
        }
        return localHost;
    }

    /**
     * 
     * @return port of RMI Registry
     */
    public static int getRmiRegistryPort() {
        return DEFAULT_RMI_REGISTRY_PORT == 0 ? 
                RmiUtils.DEFAULT_RMI_PORT : DEFAULT_RMI_REGISTRY_PORT;
    }
}
