/*
 * Created on Jul 25, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.engine.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PackageTest extends TestCase
{
    Map variables;
    SampleResult result;
    ReplaceStringWithFunctions transformer;
    /**
    * @param arg0
    */
    public PackageTest(String arg0)
    {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public void setUp()
    {
        variables = new HashMap();
        variables.put("my_regex", ".*");
        variables.put("server", "jakarta.apache.org");
        result = new SampleResult();
        result.setResponseData("<html>hello world</html> costs: $3.47,$5.67".getBytes());
        transformer = new ReplaceStringWithFunctions(new CompoundVariable(), variables);
        JMeterContextService.getContext().setVariables(new JMeterVariables());
        JMeterContextService.getContext().setSamplingStarted(true);
        JMeterContextService.getContext().setPreviousResult(result);
        JMeterContextService.getContext().getVariables().put("server", "jakarta.apache.org");
        JMeterContextService.getContext().getVariables().put("my_regex", ".*");
    }

    public void testFunctionParse1() throws Exception
    {
        StringProperty prop =
            new StringProperty("date", "${__javaScript((new Date().getDate() / 100).toString().substr(${__javaScript(1+1,d\\,ay)}\\,2),heute)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        newProp.recoverRunningVersion(null);
        assertTrue(Integer.parseInt(newProp.getStringValue()) > -1);
        assertEquals("2", JMeterContextService.getContext().getVariables().getObject("d,ay"));
    }

    public void testParseExample1() throws Exception
    {
        StringProperty prop = new StringProperty("html", "${__regexFunction(<html>(.*)</html>,$1$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("hello world", newProp.getStringValue());
    }

    public void testParseExample2() throws Exception
    {
        StringProperty prop = new StringProperty("html", "It should say:\\${${__regexFunction(<html>(.*)</html>,$1$)}}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("It should say:${hello world}", newProp.getStringValue());
    }

    public void testParseExample3() throws Exception
    {
        StringProperty prop =
            new StringProperty("html", "${__regexFunction(<html>(.*)</html>,$1$)}" + "${__regexFunction(<html>(.*o)(.*o)(.*)</html>," + "$1$$3$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("hello worldhellorld", newProp.getStringValue());
    }

    public void testParseExample4() throws Exception
    {
        StringProperty prop = new StringProperty("html", "${non-existing function}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("${non-existing function}", newProp.getStringValue());
    }

    public void testParseExample6() throws Exception
    {
        StringProperty prop = new StringProperty("html", "${server}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("jakarta.apache.org", newProp.getStringValue());
    }

    public void testParseExample5() throws Exception
    {
        StringProperty prop = new StringProperty("html", "");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.StringProperty", newProp.getClass().getName());
        assertEquals("", newProp.getStringValue());
    }

    public void testParseExample7() throws Exception
    {
        StringProperty prop = new StringProperty("html", "${__regexFunction(\\<([a-z]*)\\>,$1$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("html", newProp.getStringValue());
    }
    
    public void testParseExample8() throws Exception
        {
            StringProperty prop = new StringProperty("html", "${__regexFunction((\\\\$\\d+\\.\\d+),$1$)}");
            JMeterProperty newProp = transformer.transformValue(prop);
            newProp.setRunningVersion(true);
            assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
            assertEquals("$3.47", newProp.getStringValue());
        }
        
    public void testParseExample9() throws Exception
            {
                StringProperty prop = new StringProperty("html", "${__regexFunction(([$]\\d+\\.\\d+),$1$)}");
                JMeterProperty newProp = transformer.transformValue(prop);
                newProp.setRunningVersion(true);
                assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
                assertEquals("$3.47", newProp.getStringValue());
            }
            
    public void testParseExample10() throws Exception
                {
                    StringProperty prop = new StringProperty("html", "${__regexFunction(\\ (\\\\\\$\\d+\\.\\d+\\,\\\\$\\d+\\.\\d+),$1$)}");
                    JMeterProperty newProp = transformer.transformValue(prop);
                    newProp.setRunningVersion(true);
                    assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
                    assertEquals("$3.47,$5.67", newProp.getStringValue());
                }

    public void testNestedExample1() throws Exception
    {
        StringProperty prop =
            new StringProperty("html", "${__regexFunction(<html>(${my_regex})</html>," + "$1$)}${__regexFunction(<html>(.*o)(.*o)(.*)" + "</html>,$1$$3$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("hello worldhellorld", newProp.getStringValue());
    }

    public void testNestedExample2() throws Exception
    {
        StringProperty prop = new StringProperty("html", "${__regexFunction(<html>(${my_regex})</html>,$1$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("hello world", newProp.getStringValue());
    }

}
