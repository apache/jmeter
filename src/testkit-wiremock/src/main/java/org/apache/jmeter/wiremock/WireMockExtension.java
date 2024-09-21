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

package org.apache.jmeter.wiremock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class WireMockExtension implements BeforeEachCallback, BeforeAllCallback, AfterAllCallback, ParameterResolver {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(WireMockExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        WireMockServer server = getServer(context);
        if (server != null) {
            server.resetAll();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == WireMockServer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return getServer(extensionContext);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    private static WireMockServer getServer(ExtensionContext context) {
        return getStore(context).get("server", WireMockServer.class);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        WireMockServer server = new WireMockServer(
                WireMockConfiguration.wireMockConfig()
                        .dynamicPort()
                        .extensions(new RequestCountDown())
        );
        server.start();
        getStore(context).put("server", server);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        WireMockServer server = getServer(context);
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }
}
