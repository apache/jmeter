package org.apache.jmeter.protocol.http.control;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    // ensure that the initial DNSServers are copied to the per-thread instances

    static{

        String cacheTTL=System.getProperty("networkaddress.cache.ttl");

        if(cacheTTL==null||!cacheTTL.equals("0")){
            log.warn("JVM DNS cache is not disabled, DNS Resolver won't work correctly.");
            log.warn("Restart JMeter with the following parameter: -Dnetworkaddress.cache.ttl=0");
        if(!JMeter.isNonGUI()){
            GuiPackage.showErrorMessage("JVM DNS cache is not disabled, DNS Resolver won't work correctly.\n" +
                    "Restart JMeter with the following parameter: -Dnetworkaddress.cache.ttl=0","DNS Resolver won't work correctly");
        }
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        DNSCacheManager clone = (DNSCacheManager) super.clone();
        clone.systemDefaultDnsResolver=new SystemDefaultDnsResolver();
        clone.cache=new LinkedHashMap<String,InetAddress[]>();
        return clone;
    }


    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR, clear));
    }



    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] addresses;
        if(cache.containsKey(host)){
            if(log.isDebugEnabled()){
                log.debug("Cache hit: " + host + "  found in cache Thread #" + JMeterContextService.getContext().getThreadNum());
            }
           return cache.get(host);
        }else{
            addresses=systemDefaultDnsResolver.resolve(host);
            if(log.isDebugEnabled()){
                log.debug("Cache miss: " + host + "Thread #" + JMeterContextService.getContext().getThreadNum()
                        + ", resolved with system resolver into " + addresses.length + " addresses...");
            }
            cache.put(host,addresses);
            return addresses;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void testIterationStart(LoopIterationEvent event) {
        if (getClearEachIteration()) {
            // No need to call clear
            this.cache.clear();
        }
    }
}