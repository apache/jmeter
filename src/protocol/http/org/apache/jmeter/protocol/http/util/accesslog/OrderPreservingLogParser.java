package org.apache.jmeter.protocol.http.util.accesslog;

import org.apache.jmeter.testelement.TestElement;

public class OrderPreservingLogParser extends SharedTCLogParser {

    public OrderPreservingLogParser() {
        super();
    }

    public OrderPreservingLogParser(String source) {
        super(source);
    }

    /**
     * parse a set number of lines from the access log. Keep in mind the number
     * of lines parsed will depend the filter and number of lines in the log.
     * The method returns the actual lines parsed.
     * 
     * @param count
     * @return lines parsed
     */
    public synchronized int parseAndConfigure(int count, TestElement el) {
        return this.parse(el, count);
    }

}
