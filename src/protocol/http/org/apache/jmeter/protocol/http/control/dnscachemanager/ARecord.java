package org.apache.jmeter.protocol.http.control.dnscachemanager;

import org.apache.jmeter.testelement.AbstractTestElement;

import java.io.Serializable;

/**
 * Created by dzmitrykashlach on 6/13/14.
 */
public class ARecord extends AbstractTestElement implements Serializable {
    private static final long serialVersionUID = 240L;

    private static final String TAB = "\t";

    private static final String IP = "ARecord.ip"; //$NON-NLS-1$

    private static final String EXPIRES = "ARecord.expires"; //$NON-NLS-1$


    /**
     * create the A Record
     */
    public ARecord() {
        this("", "", 0);
    }

    /**
     * create the A Record
     *
     * @param name - name of record
     * @param ip   - ip of record
     * @param expires  - time-to-live for aRecord
     */
    public ARecord(String name, String ip, long expires) {
        this.setName(name);
        this.setIP(ip);
        this.setExpires(expires);
    }

    /**
     * get the ip for the record.
     */
    public String getIP() {
        return getPropertyAsString(IP);
    }

    /**
     * set the ip for the record.
     */
    public void setIP(String ip) {
        this.setProperty(IP, ip);
    }

    /**
     * get the ValidTill for the record.
     */
    public long getExpires() {
        return getPropertyAsLong(EXPIRES);
    }

    /**
     * set the ValidTill for the record.
     */
    public void setExpires(long expires) {
        setProperty(EXPIRES, expires);
    }


    /**
     * creates a string representation of this record
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(80);
        sb.append(getExpires());
        // flag - if all machines within a given domain can access the variable.
        //(from http://www.cookiecentral.com/faq/ 3.5)
        sb.append(TAB).append("TRUE");
        sb.append(TAB).append(getName());
        sb.append(TAB).append(getIP());
        return sb.toString();
    }
}
