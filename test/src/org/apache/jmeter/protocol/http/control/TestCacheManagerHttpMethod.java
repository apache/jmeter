package org.apache.jmeter.protocol.http.control;

import org.apache.commons.httpclient.Header;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;

import static org.junit.Assert.assertEquals;

public class TestCacheManagerHttpMethod extends TestCacheManagerUrlConnectionBase {

    @Override
    protected void setExpires(String expires) {
        ((HttpMethodStub) httpMethod).expires = expires;
    }

    @Override
    protected void setCacheControl(String cacheControl) {
        ((HttpMethodStub) httpMethod).cacheControl = cacheControl;
    }

    @Override
    protected void setLastModified(String lastModified) {
        ((HttpMethodStub) httpMethod).lastModifiedHeader =
                new org.apache.commons.httpclient.Header(HTTPConstants.LAST_MODIFIED,
                        lastModified);
    }

    @Override
    protected void cacheResult(HTTPSampleResult result) throws Exception {
        this.cacheManager.saveDetails(this.httpMethod, result);
    }

    @Override
    protected void addRequestHeader(String requestHeader, String value) {
        this.httpMethod.addRequestHeader(new Header(requestHeader, value, false));
    }

    @Override
    protected void setRequestHeaders() {
        this.cacheManager.setHeaders(this.url, this.httpMethod);
    }

    @Override
    protected void checkRequestHeader(String requestHeader, String expectedValue) {
        org.apache.commons.httpclient.Header header = this.httpMethod.getRequestHeader(requestHeader);
        assertEquals("Wrong name in header for " + requestHeader, requestHeader, header.getName());
        assertEquals("Wrong value for header " + header, expectedValue, header.getValue());
    }

}
