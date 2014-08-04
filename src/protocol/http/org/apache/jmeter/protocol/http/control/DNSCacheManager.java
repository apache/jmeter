package org.apache.jmeter.protocol.http.control;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.*;

public class DNSCacheManager extends ConfigTestElement implements TestIterationListener, Serializable,
        DnsResolver {
    // Package protected for tests
    private static final long serialVersionUID = 233L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private SystemDefaultDnsResolver systemDefaultDnsResolver=null;
    private Map<String,InetAddress[]> cache = null;
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
        clone.systemDefaultDnsResolver=new SystemDefaultDnsResolver();
        clone.cache=new LinkedHashMap<String,InetAddress[]>();
        Security.setProperty("networkaddress.cache.ttl", "0");
        return clone;
    }


    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR, clear));
    }



    public InetAddress[] resolve(String host) throws UnknownHostException {
        log.debug("Preparing for resolving host...");
        InetAddress[] addresses;
        if(cache.containsKey(host)){
            log.debug(host+" found in cache");
           return cache.get(host);
        }else{
            log.debug("Preparing for resolving host through OS resolver...");
            addresses=systemDefaultDnsResolver.resolve(host);
            log.debug("Got "+addresses.length+" addresses...");
            cache.put(host,addresses);
            return addresses;
        }
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
    public void testIterationStart(LoopIterationEvent event) {
        if (getClearEachIteration()) {
            log.debug("Initialise servers ...");
            // No need to call clear
            this.cache.clear();
        }
    }
}