package org.apache.jmeter.protocol.http.control;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by dzmitrykashlach on 8/2/14.
 */
public class DNSResolver implements DnsResolver{
    private SystemDefaultDnsResolver systemDefaultDnsResolver=null;
  public DNSResolver(){
      this.systemDefaultDnsResolver=new SystemDefaultDnsResolver();
  }
    public InetAddress[] resolve(String host) throws UnknownHostException {
        return systemDefaultDnsResolver.resolve(host);
    }
}
