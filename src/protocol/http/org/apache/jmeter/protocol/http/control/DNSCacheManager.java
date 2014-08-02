package org.apache.jmeter.protocol.http.control;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.*;


/**
 * Created by dzmitrykashlach on 6/13/14.
 */
public class DNSCacheManager extends ConfigTestElement implements TestStateListener, TestIterationListener, Serializable {
    // Package protected for tests
    private static final long serialVersionUID = 233L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    //++ JMX tag values
    private static final String CLEAR = "DNSCacheManager.clearEachIteration";// $NON-NLS-1$
    //-- JMX tag values
    // See bug 33796
    private static final boolean DELETE_NULL_SERVERS =
            JMeterUtils.getPropDefault("DNSCacheManager.delete_null_servers", true);// $NON-NLS-1$

    static {
        log.info("Settings:"
                        + " Delete null: " + DELETE_NULL_SERVERS
        );
    }



    // ensure that the initial DNSServers are copied to the per-thread instances

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        DNSCacheManager clone = (DNSCacheManager) super.clone();
        Security.setProperty("networkaddress.cache.ttl", "0");
        return clone;
    }


    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR, clear));
    }



    public String resolve(String host) throws UnknownHostException {

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRunningVersion(boolean running) {
        // do nothing, the DNS cache manager has to accept changes.
    }




    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        super.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String host) {
        testStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded(String host) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testIterationStart(LoopIterationEvent event) {
        if (getClearEachIteration()) {
            log.debug("Initialise servers ...");
            // No need to call clear
//            this.cache.clearCache();
        }
    }
}
