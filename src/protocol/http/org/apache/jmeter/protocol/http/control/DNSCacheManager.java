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
import org.xbill.DNS.*;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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
    private static final String DNS_SERVERS = "DNSCacheManager.servers";// $NON-NLS-1$
    private static final String A_RECORDS = "DNSCacheManager.aRecords";// $NON-NLS-1$
    // See bug 33796
    private static final boolean DELETE_NULL_SERVERS =
            JMeterUtils.getPropDefault("DNSCacheManager.delete_null_servers", true);// $NON-NLS-1$

    static {
        log.info("Settings:"
                        + " Delete null: " + DELETE_NULL_SERVERS
        );
    }

    private Resolver resolver = null;
    private Cache cache = null;


    // ensure that the initial DNSServers are copied to the per-thread instances

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        DNSCacheManager clone = (DNSCacheManager) super.clone();
        try {
            clone.resolver = new SimpleResolver();
            clone.cache = new Cache(DClass.IN);
        } catch (UnknownHostException uhe) {
            log.error("Failed to clone DNS CacheManager: " + uhe);
        }
        return clone;
    }


    public CollectionProperty getDNSServers() {
        return (CollectionProperty) getProperty(DNS_SERVERS);
    }

    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR, clear));
    }



    public String doRequest(Lookup lookup) {
        Record[] records = lookup.run();
        String recordStr = "";
        if (records != null && records.length > 0) {
            recordStr = records[0].toString();
            return recordStr.substring(recordStr.lastIndexOf("\t") + 1);
        }

        return "";
    }
    public String resolve(String host) throws TextParseException, UnknownHostException {

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
            setProperty(getDNSServers().clone());
            this.cache.clearCache();
        }
    }
}
