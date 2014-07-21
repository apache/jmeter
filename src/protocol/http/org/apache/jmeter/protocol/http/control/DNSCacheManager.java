package org.apache.jmeter.protocol.http.control;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.control.dnscachemanager.ARecord;
import org.apache.jmeter.protocol.http.control.dnscachemanager.DNSServer;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
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

    public CollectionProperty getARecords() {
        return (CollectionProperty) getProperty(A_RECORDS);
    }

    public int getDNSServersCount() {// Used by GUI
        return getDNSServers().size();
    }

    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR, clear));
    }


    public String getPredefinedARecord(String host) {
        List<TestElementProperty> aRecords = (List<TestElementProperty>) getARecords().getObjectValue();
        Iterator<TestElementProperty> iterator = aRecords.iterator();
        long curTime = System.currentTimeMillis();
        while (iterator.hasNext()) {
            ARecord aRecord = (ARecord) iterator.next().getObjectValue();
            if (aRecord.getName().equals(host) && curTime <= aRecord.getExpires()) {
                return aRecord.getIP();
            }
        }
        return "";
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
        // if A record is in table, then use it.
        /*
        1. Get list of names from A Records table
        2. if host is in list -> take IP and return it. Otherwise, continue with dnsjava
         */
        String aRecord = getPredefinedARecord(host);
        if (!aRecord.isEmpty()) {
            return aRecord;
        }
        List<TestElementProperty> dnsServers = (List<TestElementProperty>) getDNSServers().getObjectValue();

        Comparator<TestElementProperty> dnsServerComparator = new Comparator<TestElementProperty>() {
            @Override
            public int compare(TestElementProperty o1, TestElementProperty o2) {
                int prior1 = ((DNSServer) o1.getObjectValue()).getPriority();
                int prior2 = ((DNSServer) o2.getObjectValue()).getPriority();
                return prior2 - prior1;
            }
        };


        Collections.sort(dnsServers, dnsServerComparator);
        Iterator<TestElementProperty> iterator = dnsServers.iterator();
        StringBuilder ip = new StringBuilder();
        while (iterator.hasNext()) {
            TestElementProperty dnsServer = iterator.next();
            ip.append(((DNSServer) dnsServer.getObjectValue()).getIP());
            InetSocketAddress dns = new InetSocketAddress(ip.toString(), 53);
            ((SimpleResolver) resolver).setAddress(dns);
            Lookup.setDefaultResolver(resolver);
            Lookup.setDefaultCache(cache, DClass.IN);
            Lookup lookup = new Lookup(host, Type.A, DClass.IN);
            String recordStr = doRequest(lookup);
            if (!recordStr.isEmpty()) {
                return recordStr;
            }

        }
        Lookup.refreshDefault();
        Lookup lookup = new Lookup(host, Type.A, DClass.IN);
        return doRequest(lookup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRunningVersion(boolean running) {
        // do nothing, the DNS cache manager has to accept changes.
    }

    /**
     * Add a server.
     */
    public void addDNSServer(DNSServer dnsServer) {
        String dnsIP = dnsServer.getIP();
        removeMatchingServers(dnsServer); // Can't have two matching servers

        if (DELETE_NULL_SERVERS && (null == dnsIP || dnsIP.length() == 0)) {
            if (log.isDebugEnabled()) {
                log.debug("Dropping dnsServer with null IP " + dnsServer.toString());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Add dnsServer to store " + dnsServer.toString());
            }
            getDNSServers().addItem(dnsServer);
        }
    }

    /**
     * Add an A Record.
     */
    public void addARecord(ARecord aRecord) {
        String aRIP = aRecord.getIP();
        removeMatchingARecords(aRecord); // Can't have two matching servers

        if (DELETE_NULL_SERVERS && (null == aRIP || aRIP.length() == 0)) {
            if (log.isDebugEnabled()) {
                log.debug("Dropping dnsServer with null IP " + aRecord.toString());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Add dnsServer to store " + aRecord.toString());
            }
            getARecords().addItem(aRecord);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        super.clear();
        clearDNSServers(); // ensure data is set up OK initially
        clearARecords();
    }

    /*
     * Remove all the servers.
     */
    private void clearDNSServers() {
        log.debug("Clear all servers from store");
        setProperty(new CollectionProperty(DNS_SERVERS, new ArrayList<Object>()));
    }

    /*
     * Remove all the aRecords
     */
    private void clearARecords() {
        log.debug("Clear all aRecords from store");
        setProperty(new CollectionProperty(A_RECORDS, new ArrayList<Object>()));
    }

    /**
     * Remove a server.
     */
    public void remove(int index) {// TODO not used by GUI
        getDNSServers().remove(index);
    }

    /**
     * Return the server at index i.
     */
    public DNSServer get(int i) {// Only used by GUI
        return (DNSServer) getDNSServers().get(i).getObjectValue();
    }

    /**
     * Check if servers match, i.e. name, path and domain are equal.
     * <br/>
     * TODO - should we compare secure too?
     *
     * @param a
     * @param b
     * @return true if servers match
     */
    private boolean matchServer(DNSServer a, DNSServer b) {
        return
                a.getName().equals(b.getName())
                        &&
                        a.getIP().equals(b.getIP())
                        &&
                        a.getPriority() == b.getPriority();
    }


    /**
     * Check if servers match, i.e. name, path and domain are equal.
     * <br/>
     * TODO - should we compare secure too?
     *
     * @param a
     * @param b
     * @return true if servers match
     */
    private boolean matchRecord(ARecord a, ARecord b) {
        return
                a.getName().equals(b.getName())
                        &&
                        a.getIP().equals(b.getIP())
                        &&
                        a.getExpires() == b.getExpires();
    }

    void removeMatchingServers(DNSServer dnsServer) {
        // Scan for any matching dnsServers
        PropertyIterator iter = getDNSServers().iterator();
        while (iter.hasNext()) {
            DNSServer dnsServIter = (DNSServer) iter.next().getObjectValue();
            if (dnsServer == null) {// TODO is this possible?
                continue;
            }
            if (matchServer(dnsServIter, dnsServer)) {
                if (log.isDebugEnabled()) {
                    log.debug("New DNSServer = " + dnsServer.toString()
                            + " removing matching DNSServer " + dnsServer.toString());
                }
                iter.remove();
            }
        }
    }

    void removeMatchingARecords(ARecord aRecord) {
        // Scan for any matching dnsServers
        PropertyIterator iter = getARecords().iterator();
        while (iter.hasNext()) {
            ARecord aRecordIter = (ARecord) iter.next().getObjectValue();
            if (aRecord == null) {// TODO is this possible?
                continue;
            }
            if (matchRecord(aRecordIter, aRecord)) {
                if (log.isDebugEnabled()) {
                    log.debug("New DNSServer = " + aRecord.toString()
                            + " removing matching DNSServer " + aRecord.toString());
                }
                iter.remove();
            }
        }
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
