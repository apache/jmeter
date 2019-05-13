package org.apache.jmeter.assertions;

import static org.junit.Assert.*;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Test;

public class XPath2AssertionTest {

    final String xmlDoc = JMeterUtils.getResourceFileAsText("XPathUtilTestXml.xml");
    @Test
    public void testComputeAssertionResultUsingSaxon() throws FactoryConfigurationError {
       XPath2Assertion assertion = new XPath2Assertion();
        SampleResult response = new SampleResult();
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84_pos#";
        String xPathQuery = "//Employees/Employee[1]/age:ag";
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertFalse("When xpath2 conforms to xml, the result of assertion should be true ",res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
        namespaces = "age=http://www.w3.org/2003/01/geo/wgs84#";
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        res = assertion.getResult(response);
        assertTrue("When xpath2 does,'t conforms to xml, the result of assertion should be false ", res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
    }
}
