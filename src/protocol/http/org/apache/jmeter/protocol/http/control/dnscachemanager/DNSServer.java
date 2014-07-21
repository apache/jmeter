package org.apache.jmeter.protocol.http.control.dnscachemanager;

import org.apache.jmeter.testelement.AbstractTestElement;

import java.io.Serializable;

/**
 * Created by dzmitrykashlach on 6/13/14.
 */
public class DNSServer extends AbstractTestElement implements Serializable {
    private static final long serialVersionUID = 240L;

    private static final String TAB = "\t";

    private static final String IP = "DNS.ip"; //$NON-NLS-1$

    private static final String PRIORITY = "DNS.priority"; //$NON-NLS-1$


    /**
     * create the DNS Server
     */
    public DNSServer() {
        this("", "", 0);
    }

    /**
     * create the DNS Server
     *
     * @param name     - name of server
     * @param ip       - ip of server
     * @param priority - preferable priority of server choosing via randomizer
     */
    public DNSServer(String name, String ip, int priority) {
        this.setName(name);
        this.setIP(ip);
        this.setPriority(priority);
    }

    /**
     * get the ip for the server.
     */
    public String getIP() {
        return getPropertyAsString(IP);
    }

    /**
     * set the value for the server.
     */
    public void setIP(String ip) {
        this.setProperty(IP, ip);
    }

    /**
     * get the priority for the server.
     */
    public int getPriority() {
        return getPropertyAsInt(PRIORITY);
    }

    /**
     * set the priority for the server.
     */
    public void setPriority(int priority) {
        setProperty(PRIORITY, priority);
    }


    /**
     * creates a string representation of this server
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(80);
        sb.append(getPriority());
        // flag - if all machines within a given domain can access the variable.
        //(from http://www.cookiecentral.com/faq/ 3.5)
        sb.append(TAB).append("TRUE");
        sb.append(TAB).append(getName());
        sb.append(TAB).append(getIP());
        return sb.toString();
    }
}
