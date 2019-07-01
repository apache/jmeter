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

package org.apache.jmeter.protocol.http.control;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes in charge Kerberos auth mechanism
 * @since 2.10
 */
public class KerberosManager implements Serializable {

    private static final long serialVersionUID = 2L;

    private static final Logger log = LoggerFactory.getLogger(KerberosManager.class);

    private static final String JAAS_APPLICATION = JMeterUtils.getPropDefault("kerberos_jaas_application", "JMeter"); //$NON-NLS-1$ $NON-NLS-2$
    private final ConcurrentMap<String, Future<Subject>> subjects = new ConcurrentHashMap<>();

    public KerberosManager() {
        super();
    }

    void clearSubjects() {
        subjects.clear();
    }

    public Subject getSubjectForUser(final String username,
            final String password) {
        FutureTask<Subject> task = new FutureTask<>(() -> {
            LoginContext loginCtx;
            try {
                loginCtx = new LoginContext(JAAS_APPLICATION,
                        new LoginCallbackHandler(username, password));
                loginCtx.login();
                return loginCtx.getSubject();
            } catch (LoginException e) {
                log.warn("Could not log in user " + username, e);
            }
            return null;
        });
        if(log.isDebugEnabled()) {
            log.debug("Subject cached:"+subjects.keySet() +" before:"+username);
        }
        Future<Subject> subjectFuture = subjects.putIfAbsent(username, task);
        if (subjectFuture == null) {
            subjectFuture = task;
            task.run(); // NOSONAR we just execute method
        }
        try {
            return subjectFuture.get();
        } catch (InterruptedException e1) {
            log.warn("Interrupted while getting subject for " + username, e1);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e1) {
            log.warn("Execution of getting subject for " + username + " failed", e1);
        }
        return null;
    }

    // Needs to be package-protected to avoid problem with serialisation tests
    static class LoginCallbackHandler implements CallbackHandler {
        private final String password;
        private final String username;

        public LoginCallbackHandler(final String username, final String password) {
            super();
            this.username = username;
            this.password = password;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException,
                UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback && username != null) {
                    NameCallback nc = (NameCallback) callback;
                    nc.setName(username);
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callback;
                    pc.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException( callback,
                            "Unrecognized Callback"); //$NON-NLS-1$
                }
            }
        }
    }

    public String getKrb5Conf() {
        return System.getProperty("java.security.krb5.conf"); //$NON-NLS-1$
    }

    public boolean getKrb5Debug() {
        return Boolean.getBoolean("java.security.krb5.debug"); //$NON-NLS-1$
    }

    public String getJaasConf() {
        return System.getProperty("java.security.auth.login.config"); //$NON-NLS-1$
    }

    @Override
    public String toString() {
        return "KerberosManager[jaas: " + getJaasConf() + ", krb5: " + getKrb5Conf() + ", debug: " + getKrb5Debug() +"]";
    }
}
