/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 package org.apache.jmeter.engine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author  Michael Stover
 * @author  <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Revision$ updated on $Date$
 */
public class ValueReplacer
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    CompoundVariable masterFunction = new CompoundVariable();
    Map variables = new HashMap();

    public ValueReplacer()
    {
    }

    public ValueReplacer(TestPlan tp)
    {
        setUserDefinedVariables(tp.getUserDefinedVariables());
    }

    public void setUserDefinedVariables(Map variables)
    {
        this.variables = variables;
    }

    public void replaceValues(TestElement el) throws InvalidVariableException
    {
        Collection newProps =
            replaceValues(
                el.propertyIterator(),
                new ReplaceStringWithFunctions(masterFunction, variables));
        setProperties(el, newProps);
    }

    private void setProperties(TestElement el, Collection newProps)
    {
        Iterator iter = newProps.iterator();
        el.clear();
        while(iter.hasNext())
        {
            el.setProperty((JMeterProperty)iter.next());
        }
    }
    
    public void reverseReplace(TestElement el) throws InvalidVariableException
    {
        Collection newProps =
            replaceValues(
                el.propertyIterator(),
                new ReplaceFunctionsWithStrings(masterFunction, variables));
        setProperties(el, newProps);
    }
        
    public void undoReverseReplace(TestElement el)
        throws InvalidVariableException
    {
        Collection newProps =
            replaceValues(
                el.propertyIterator(),
                new UndoVariableReplacement(masterFunction, variables));
        setProperties(el, newProps);
    }

    public void addVariable(String name, String value)
    {
        variables.put(name, value);
    }

    /**
     * Add all the given variables to this replacer's variables map.
     * 
     * @param vars A map of variable name-value pairs (String-to-String).
     */
    public void addVariables(Map vars)
    {
        variables.putAll(vars);
    }

    private Collection replaceValues(
        PropertyIterator iter,
        ValueTransformer transform)
        throws InvalidVariableException
    {
        List props = new LinkedList();
        while(iter.hasNext())
        {
            JMeterProperty val = iter.next();
            if (log.isDebugEnabled())
            {
                log.debug("About to replace in property of tipe: "
                  +val.getClass()+": "+val);
            }
            if (val instanceof StringProperty)
            {
                val = transform.transformValue((StringProperty) val);
                if (log.isDebugEnabled())
                {
                    log.debug("Replacement result: " +val);
                }
            }
            else if (val instanceof MultiProperty)
            {
                MultiProperty multiVal = (MultiProperty)val;
                Collection newValues =
                    replaceValues(multiVal.iterator(), transform);
                multiVal.clear();
                Iterator propIter = newValues.iterator();
                while(propIter.hasNext())
                {
                    multiVal.addProperty((JMeterProperty) propIter.next());
                }
                if (log.isDebugEnabled())
                {
                    log.debug("Replacement result: " +multiVal);
                }
            }
            else {
                if (log.isDebugEnabled())
                {
                    log.debug("Won't replace.");
                }
            }
            props.add(val);
        }
        return props;
    }

    

    public static class Test extends TestCase
    {
        TestPlan variables;

        public Test(String name)
        {
            super(name);
        }

        public void setUp()
        {
            variables = new TestPlan();
            variables.addParameter("server", "jakarta.apache.org");
            variables.addParameter("username", "jack");
            variables.addParameter("password", "jacks_password");
            variables.addParameter("regex", ".*");
            JMeterVariables vars = new JMeterVariables();
            vars.put("server", "jakarta.apache.org");
            JMeterContextService.getContext().setVariables(vars);
            JMeterContextService.getContext().setSamplingStarted(true);
        }
        
        

        public void testReverseReplacement() throws Exception
        {
            ValueReplacer replacer = new ValueReplacer(variables);
            assertTrue(
                variables.getUserDefinedVariables().containsKey("server"));
            assertTrue(replacer.variables.containsKey("server"));
            TestElement element = new TestPlan();
            element.setProperty(
                new StringProperty("domain", "jakarta.apache.org"));
            List args = new ArrayList();
            args.add("username is jack");
            args.add("jacks_password");
            element.setProperty(new CollectionProperty("args", args));
            replacer.reverseReplace(element);
            assertEquals("${server}", element.getPropertyAsString("domain"));
            args = (List) element.getProperty("args").getObjectValue();
            assertEquals(
                "${password}",
                ((JMeterProperty) args.get(1)).getStringValue());
        }

        public void testReplace() throws Exception
        {
            ValueReplacer replacer = new ValueReplacer();
            replacer.setUserDefinedVariables(
                variables.getUserDefinedVariables());
            TestElement element = new ConfigTestElement();
            element.setProperty(new StringProperty("domain", "${server}"));
            replacer.replaceValues(element);
            log.debug("domain property = " + element.getProperty("domain"));
            element.setRunningVersion(true);
            assertEquals(
                "jakarta.apache.org",
                element.getPropertyAsString("domain"));
        }

        /* (non-Javadoc)
         * @see junit.framework.TestCase#tearDown()
         */
        protected void tearDown() throws Exception
        {
            JMeterContextService.getContext().setSamplingStarted(false);
        }
    }
}
