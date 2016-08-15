package org.apache.jmeter.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLSocket;

/**
 * Uses the underlying implementation to support Server Name Indication (SNI).
 * @author Michael Locher <cmbntr@gmail.com>
 * @see <a href="https://issues.apache.org/jira/browse/HTTPCLIENT-1119">HTTPCLIENT-1119</a>
 * @since 3.1 (extracted from JMeterClientConnectionOperator.java)
 */
public class HostNameSetter {

    private static final AtomicReference<HostNameSetter> CURRENT = new AtomicReference<>();

    private final WeakReference<Class<?>> cls;
    private final WeakReference<Method> setter;

    private HostNameSetter(Class<?> clazz, Method setter) {
        this.cls = new WeakReference<Class<?>>(clazz);
        this.setter = setter == null ? null : new WeakReference<>(setter);
    }

    private static Method init(Class<?> cls) {
        Method s = null;
        try {
            s = cls.getMethod("setHost", String.class);
        } catch (Exception e) {
            initFail(e);
        }
        CURRENT.set(new HostNameSetter(cls, s));
        return s;
    }
    


    private static void initFail(Exception e) {
        // ignore
    }

    private Method reuse(Class<?> cls) {
        final boolean wrongClass = this.cls.get() != cls;
        if (wrongClass) {
            return init(cls);
        }

        final boolean setterNotSupported = this.setter == null;
        if (setterNotSupported) {
            return null;
        }

        final Method s = setter.get();
        final boolean setterLost = s == null;
        return setterLost ? init(cls) : s;
    }

    /**
     * Invokes the {@code #setName(String)} method if one is present.
     *
     * @param hostname the name to set
     * @param sslsock the socket
     */
    public static void setServerNameIndication(String hostname, SSLSocket sslsock) {
        final Class<?> cls = sslsock.getClass();
        final HostNameSetter current = CURRENT.get();
        final Method setter = (current == null) ? init(cls) : current.reuse(cls);
        if (setter != null) {
            try {
                setter.invoke(sslsock, hostname);
            } catch (IllegalArgumentException
                    | IllegalAccessException
                    | InvocationTargetException e) {
                setServerNameIndicationFail(e);
            }
        }
    }

    private static void setServerNameIndicationFail(Exception e) {
        // ignore
    }
}
