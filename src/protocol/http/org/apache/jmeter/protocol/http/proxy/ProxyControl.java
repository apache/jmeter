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
package org.apache.jmeter.protocol.http.proxy;

import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * @author     Michael Stover
 * @version    $Revision$
 */
public class ProxyControl extends ConfigTestElement implements Serializable
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    Daemon server;
    private final int DEFAULT_PORT = 8080;
    private static PatternCacheLRU patternCache =
        new PatternCacheLRU(1000, new Perl5Compiler());
    transient Perl5Matcher matcher;
    public final static String PORT = "ProxyControlGui.port";
    public final static String EXCLUDE_LIST = "ProxyControlGui.exclude_list";
    public final static String INCLUDE_LIST = "ProxyControlGui.include_list";

    public ProxyControl()
    {
        matcher = new Perl5Matcher();
        setPort(DEFAULT_PORT);
        setExcludeList(new HashSet());
        setIncludeList(new HashSet());
    }

    public void setPort(int port)
    {
        this.setProperty(new IntegerProperty(PORT, port));
    }
    
    public void setPort(String port)
    {
        setProperty(PORT,port);
    }
    
    public void setIncludeList(Collection list)
    {
        setProperty(new CollectionProperty(INCLUDE_LIST, new HashSet(list)));
    }
    public void setExcludeList(Collection list)
    {
        setProperty(new CollectionProperty(EXCLUDE_LIST, new HashSet(list)));
    }

    public String getClassLabel()
    {
        return JMeterUtils.getResString("proxy_title");
    }

    public int getPort()
    {
        return getPropertyAsInt(PORT);
    }

    public int getDefaultPort()
    {
        return DEFAULT_PORT;
    }

    public Class getGuiClass()
    {
        return org.apache.jmeter.protocol.http.proxy.gui.ProxyControlGui.class;
    }

    public void addConfigElement(ConfigElement config)
    {}

    public void startProxy()
    {
        server = new Daemon(getPort(), this);
        server.start();
    }
    
    public void addExcludedPattern(String pattern)
    {
        getExcludePatterns().addItem(pattern);
    }

    public CollectionProperty getExcludePatterns()
    {
        return (CollectionProperty) getProperty(EXCLUDE_LIST);
    }

    public void addIncludedPattern(String pattern)
    {
        getIncludePatterns().addItem(pattern);
    }

    public CollectionProperty getIncludePatterns()
    {
        return (CollectionProperty) getProperty(INCLUDE_LIST);
    }

    public void clearExcludedPatterns()
    {
        getExcludePatterns().clear();
    }

    public void clearIncludedPatterns()
    {
        getIncludePatterns().clear();
    }

    /**
     * Receives the recorded sampler from the proxy server for placing in the
     * test tree.
     * @param serverResponse added to allow saving of the server's response
     *                       while recording.  A future consideration.
     */
    public void deliverSampler(
        HTTPSampler sampler,
        TestElement[] subConfigs,
        byte[] serverResponse)
    {
        if (filterUrl(sampler))
        {
            placeConfigElement(sampler, subConfigs);
        }
    }

    public void stopProxy()
    {
        if (server != null)
        {
            server.stopServer();
        }
    }

    protected boolean filterUrl(HTTPSampler sampler)
    {
        boolean ok = false;
        if (sampler.getDomain() == null || sampler.getDomain().equals(""))
        {
            return false;
        }
        if (getIncludePatterns().size() == 0)
        {
            ok = true;
        }
        else
        {
            ok = checkIncludes(sampler);
        }
        if (!ok)
        {
            return ok;
        }
        else
        {
            if (getExcludePatterns().size() == 0)
            {
                return ok;
            }
            else
            {
                ok = checkExcludes(sampler);
            }
        }
        return ok;
    }

    private void placeConfigElement(
        HTTPSampler sampler,
        TestElement[] subConfigs)
    {
        //NOTUSED ValueReplacer replacer = GuiPackage.getInstance().getReplacer();
        TestElement urlConfig = null;
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        List nodes = treeModel.getNodesOfType(RecordingController.class);
        if (nodes.size() == 0)
        {
            nodes = treeModel.getNodesOfType(ThreadGroup.class);
        }
        Iterator iter = nodes.iterator();
        while (iter.hasNext())
        {
            JMeterTreeNode node = (JMeterTreeNode) iter.next();

            if (!node.isEnabled())
            {
                continue;
            }
            else
            {
                Enumeration enum = node.children();
                String guiClassName = null;
                while (enum.hasMoreElements())
                {
                    JMeterTreeNode subNode =
                        (JMeterTreeNode) enum.nextElement();
                    TestElement sample =
                        (TestElement) subNode.createTestElement();
                    guiClassName =
                        sample.getPropertyAsString(TestElement.GUI_CLASS);
                    if (guiClassName.equals(UrlConfigGui.class.getName())
                        || guiClassName.equals(HttpDefaultsGui.class.getName()))
                    {
                        urlConfig = sample;
                        break;
                    }
                }
                if (areMatched(sampler, urlConfig))
                {
                    removeValuesFromSampler(sampler, urlConfig);
                    replaceValues(sampler,subConfigs);
                    sampler.setProperty(
                        TestElement.GUI_CLASS,
                        "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui");
                    try
                    {
                        JMeterTreeNode newNode =
                            treeModel.addComponent(sampler, node);
                        for (int i = 0;
                            subConfigs != null && i < subConfigs.length;
                            i++)
                        {
                            if (subConfigs[i] instanceof HeaderManager)
                            {
                                subConfigs[i].setProperty(
                                    TestElement.GUI_CLASS,
                                    "org.apache.jmeter.protocol.http.gui.HeaderPanel");
                                treeModel.addComponent(subConfigs[i], newNode);
                            }
                        }
                    }
                    catch (IllegalUserActionException e)
                    {
                        JMeterUtils.reportErrorToUser(e.getMessage());
                    }
                }
            }
        }
    }

    private void removeValuesFromSampler(
        HTTPSampler sampler,
        TestElement urlConfig)
    {
        if (urlConfig != null)
        {
            if (sampler
                .getDomain()
                .equals(urlConfig.getPropertyAsString(HTTPSampler.DOMAIN)))
            {
                sampler.setDomain("");
            }
            
            // Need to add some kind of "ignore-me" value
            if (sampler.getPort()
                == urlConfig.getPropertyAsInt(HTTPSampler.PORT))
            {
                sampler.setPort(HTTPSampler.UNSPECIFIED_PORT);
            }
            
            if (sampler
                .getPath()
                .equals(urlConfig.getPropertyAsString(HTTPSampler.PATH)))
            {
                sampler.setPath("");
            }
        }
    }

    private boolean areMatched(HTTPSampler sampler, TestElement urlConfig)
    {
        return urlConfig == null
            || (urlConfig.getPropertyAsString(HTTPSampler.DOMAIN).equals("")
                || urlConfig.getPropertyAsString(HTTPSampler.DOMAIN).equals(
                    sampler.getDomain()))
            && (urlConfig.getPropertyAsString(HTTPSampler.PATH).equals("")
                || urlConfig.getPropertyAsString(HTTPSampler.PATH).equals(
                    sampler.getPath()));
    }

    private boolean checkIncludes(HTTPSampler sampler)
    {
        boolean ok = false;
		StringBuffer url = new StringBuffer(sampler.getDomain());
		url.append(":");
		url.append(sampler.getPort());
		url.append(sampler.getPath());
		if (sampler.getQueryString().length() > 0)
		{
			url.append("?");
			url.append(sampler.getQueryString());
		}
        PropertyIterator iter = getIncludePatterns().iterator();
        while (iter.hasNext())
        {
            String item = iter.next().getStringValue();
			Pattern pattern;
			try {
				pattern =
				    patternCache.getPattern(
				        item,
				        Perl5Compiler.READ_ONLY_MASK
				            & Perl5Compiler.SINGLELINE_MASK);
				ok = matcher.matches(url.toString(), pattern);
			} catch (MalformedCachePatternException e) {
				log.warn("Skipped invalid Include pattern "+item,e);
			}
            if (ok)
            {
                break;
            }
        }
        return ok;
    }

    private boolean checkExcludes(HTTPSampler sampler)
    {
        boolean ok = true;
		StringBuffer url = new StringBuffer(sampler.getDomain());
		url.append(":");
		url.append(sampler.getPort());
		url.append(sampler.getPath());
		if (sampler.getQueryString().length() > 0)
		{
			url.append("?");
			url.append(sampler.getQueryString());
		}
        PropertyIterator iter = getExcludePatterns().iterator();
        while (iter.hasNext())
        {
            String item = iter.next().getStringValue();
			Pattern pattern=null;
            try {
                pattern =
                patternCache.getPattern(
                    item,
                    Perl5Compiler.READ_ONLY_MASK
                        & Perl5Compiler.SINGLELINE_MASK);
				ok = ok && !matcher.matches(url.toString(), pattern);
			} catch (MalformedCachePatternException e){
				log.warn("Skipped invalid Exclude pattern: "+item,e);
			}
            if (!ok)
            {
                return ok;
            }
        }
        return ok;
    }

    protected void replaceValues(TestElement sampler, TestElement[] configs)
    {
        try
        {
            GuiPackage.getInstance().getReplacer().reverseReplace(sampler);
            for (int i = 0; i < configs.length; i++)
            {
                GuiPackage.getInstance().getReplacer().reverseReplace(
                    configs[i]);
            }
        }
        catch (InvalidVariableException e)
        {
            log.warn(
                "Invalid variables included for replacement into recorded "
                    + "sample",
                e);
        }
    }

    public static class Test extends TestCase
    {
        public Test(String name)
        {
            super(name);
        }
        public void testFiltering() throws Exception
        {
            ProxyControl control = new ProxyControl();
            control.addIncludedPattern(".*\\.jsp");
            control.addExcludedPattern(".*apache.org.*");
            HTTPSampler sampler = new HTTPSampler();
            sampler.setDomain("jakarta.org");
            sampler.setPath("index.jsp");
            assertTrue(control.filterUrl(sampler));
            sampler.setDomain("www.apache.org");
            assertTrue(!control.filterUrl(sampler));
            sampler.setPath("header.gif");
            sampler.setDomain("jakarta.org");
            assertTrue(!control.filterUrl(sampler));
        }
    }
}
