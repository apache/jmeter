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
 * 
 * @author  Michael Stover
 * @author  <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.protocol.http.proxy;

import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.control.GenericController;
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
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
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

public class ProxyControl extends GenericController implements Serializable
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    private Daemon server;
    public static final int DEFAULT_PORT = 8080;
	public static final String DEFAULT_PORT_S =
	         Integer.toString(DEFAULT_PORT);// Used by GUI
    private static PatternCacheLRU patternCache =
        new PatternCacheLRU(1000, new Perl5Compiler());
    transient Perl5Matcher matcher;
    public static final String PORT = "ProxyControlGui.port";
    public static final String EXCLUDE_LIST = "ProxyControlGui.exclude_list";
    public static final String INCLUDE_LIST = "ProxyControlGui.include_list";
    public static final String CAPTURE_HTTP_HEADERS = "ProxyControlGui.capture_http_headers";
	public static final String ADD_ASSERTIONS = "ProxyControlGui.add_assertion";
	public static final String GROUPING_MODE = "ProxyControlGui.grouping_mode";
	public static final String USE_KEEPALIVE  = "ProxyControlGui.use_keepalive";

    public static final int GROUPING_NO_GROUPS = 0;
    public static final int GROUPING_ADD_SEPARATORS = 1;
    public static final int GROUPING_IN_CONTROLLERS = 2;
    public static final int GROUPING_STORE_FIRST_ONLY = 3;

	private long lastTime = 0;//When was the last sample seen?
	private static final long sampleGap = 
	    JMeterUtils.getPropDefault("proxy.pause",1000);//Detect if user has pressed a new link
	private boolean addAssertions;
	private int groupingMode;
	private boolean useKeepAlive;
    
    /**
     * Tree node where the samples should be stored.
     * <p>
     * This property is not persistent.
     */
    private JMeterTreeNode target;
    
    public ProxyControl()
    {
        matcher = new Perl5Matcher();
        setPort(DEFAULT_PORT);
        setExcludeList(new HashSet());
        setIncludeList(new HashSet());
        setCaptureHttpHeaders(true); // maintain original behaviour
    }

    public void setPort(int port)
    {
        this.setProperty(new IntegerProperty(PORT, port));
    }
    
    public void setPort(String port)
    {
        setProperty(PORT,port);
    }
  
    public void setCaptureHttpHeaders(boolean capture)
    {
        setProperty(new BooleanProperty(CAPTURE_HTTP_HEADERS,capture));
    }

	public void setGroupingMode(int grouping)
	{
		this.groupingMode= grouping;
		setProperty(new IntegerProperty(GROUPING_MODE,grouping));
	}

	public void setAssertions(boolean b)
	{
		addAssertions=b;
		setProperty(new BooleanProperty(ADD_ASSERTIONS,b));
	}

	/**
	 * @param b
	 */
	public void setUseKeepAlive(boolean b)
	{
		useKeepAlive=b;
		setProperty(new BooleanProperty(USE_KEEPALIVE,b));
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

    public boolean getCaptureHttpHeaders()
    {
        return getPropertyAsBoolean(CAPTURE_HTTP_HEADERS);
    }

    public Class getGuiClass()
    {
        return org.apache.jmeter.protocol.http.proxy.gui.ProxyControlGui.class;
    }

    public void addConfigElement(ConfigElement config)
    {}

    public void startProxy()
    {
        notifyTestListenersOfStart();
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
     * @return the target controller node
     */
    public JMeterTreeNode getTarget()
    {
        return target;
    }
    
    /**
     * Sets the target node where the samples generated by the proxy have
     * to be stored.
     */
    public void setTarget(JMeterTreeNode target)
    {
        this.target= target;
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
        SampleResult result)
    {
        if (filterUrl(sampler))
        {
            placeConfigElement(sampler, subConfigs);
            notifySampleListeners(new SampleEvent(result,sampler.getName()));
        }
    }

    public void stopProxy()
    {
        if (server != null)
        {
            server.stopServer();
            try
            {
                server.join(1000); // wait for server to stop
            }
            catch (InterruptedException e)
            {
            }
            notifyTestListenersOfEnd();
        }
    }

    protected boolean filterUrl(HTTPSampler sampler)
    {
		String domain = sampler.getDomain();
		if (domain == null || domain.length() == 0)
		{
			return false;
		}
    	
		String url = generateMatchUrl(sampler);
		CollectionProperty includePatterns = getIncludePatterns();
		if (includePatterns.size() > 0)
		{
			if (!matchesPatterns(url, includePatterns))
			{
				return false;
			}
		}
    	
		CollectionProperty excludePatterns = getExcludePatterns();
		if (excludePatterns.size() > 0)
		{
			if (matchesPatterns(url, excludePatterns))
			{
				return false;
			}
		}
    	
		return true;
    }

    /*
     * Helper method to add a Response Assertion 
     */
    private void addAssertion(JMeterTreeModel model,JMeterTreeNode node)
        throws IllegalUserActionException
    {
		if (addAssertions){
			ResponseAssertion ra = new ResponseAssertion();
			ra.setProperty(TestElement.GUI_CLASS,
				"org.apache.jmeter.assertions.gui.AssertionGui");
			ra.setName("Check response");
			ra.setTestField(ResponseAssertion.RESPONSE_DATA);
			model.addComponent(ra,node);
		}
    }
    
	/*
	 * Helper method to add a Divider 
	 */
	private void addDivider(JMeterTreeModel model,JMeterTreeNode node)
	    throws IllegalUserActionException
	{
        GenericController sc = new GenericController();
        sc.setProperty(TestElement.GUI_CLASS,
            "org.apache.jmeter.control.gui.LogicControllerGui");
        sc.setName("-------------------");
        model.addComponent(sc,node);
	}
    
    /**
     * Helper method to add a Simple Controller to contain the samplers.
     * 
     * @param model Test component tree model
     * @param node  Node in the tree where we will add the Controller
     * @param name  A name for the Controller
     */
    private void addSimpleController(
            JMeterTreeModel model,
            JMeterTreeNode node,
            String name)
        throws IllegalUserActionException
    {
        GenericController sc = new GenericController();
        sc.setProperty(TestElement.GUI_CLASS,
            "org.apache.jmeter.control.gui.LogicControllerGui");
        sc.setName(name);
        model.addComponent(sc,node);
    }
    
    private void placeConfigElement(
        HTTPSampler sampler,
        TestElement[] subConfigs)
    {
        JMeterTreeNode myTarget= target;
        TestElement urlConfig = null;
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        if (myTarget == null)
        {
            List nodes = treeModel.getNodesOfType(RecordingController.class);
            Iterator iter= nodes.iterator();
            while (iter.hasNext()) {
                JMeterTreeNode node= (JMeterTreeNode) iter.next();
                if (node.isEnabled()) {
                    myTarget= node;
                    break;
                }
            }
        }
        if (myTarget == null)
        {
            List nodes = treeModel.getNodesOfType(ThreadGroup.class);
            Iterator iter = nodes.iterator();
            while (iter.hasNext()) {
                JMeterTreeNode node= (JMeterTreeNode) iter.next();
                if (node.isEnabled()) {
                    myTarget= node;
                    break;
                }
            }
        }

        Enumeration enum = myTarget.children();
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
            sampler.setUseKeepAlive(useKeepAlive);
            sampler.setProperty(
                TestElement.GUI_CLASS,
                "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui");
            try
            {
                boolean firstInBatch=false;
                long now = System.currentTimeMillis();
                if (now - lastTime > sampleGap){
                    if (!myTarget.isLeaf() 
                            && groupingMode == GROUPING_ADD_SEPARATORS)
                    {
                        addDivider(treeModel, myTarget);
                    }
                    if (groupingMode == GROUPING_IN_CONTROLLERS)
                    {
                        addSimpleController(treeModel, myTarget, sampler.getName());
                    }
                    firstInBatch=true;//Remember this was first in its batch
                }
                lastTime = now;

                if (groupingMode == GROUPING_STORE_FIRST_ONLY)
                {
                    if (!firstInBatch) return; // Huh! don't store this one!
                    
                    // If we're not storing subsequent samplers, we'll need the
                    // first sampler to do all the work...:
                    sampler.setFollowRedirects(true);
                    sampler.setImageParser(true);
                }
                
                if (groupingMode == GROUPING_IN_CONTROLLERS)
                {
                    // Find the last controller in the target to store the
                    // sampler there:
                    for (int i= myTarget.getChildCount()-1; i>=0; i--)
                    {
                        JMeterTreeNode c= (JMeterTreeNode)myTarget.getChildAt(i);
                        if (c.createTestElement() instanceof GenericController)
                        {
                            myTarget= c;
                            break;
                        }
                    }
                }

                JMeterTreeNode newNode =
                    treeModel.addComponent(sampler, myTarget);
                            
                if(firstInBatch){
                    addAssertion(treeModel,newNode);
                    firstInBatch=false;
                }

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
            
            if (sampler
                .getProtocol()
                .equalsIgnoreCase(urlConfig.getPropertyAsString(HTTPSampler.PROTOCOL))
                )
            {
            	sampler.setProtocol("");
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

	private String generateMatchUrl(HTTPSampler sampler)
	{
		StringBuffer buf = new StringBuffer(sampler.getDomain());
		buf.append(':');
		buf.append(sampler.getPort());
		buf.append(sampler.getPath());
		if (sampler.getQueryString().length() > 0)
		{
			buf.append('?');
			buf.append(sampler.getQueryString());
		}
		return buf.toString();
	}

    private boolean matchesPatterns(String url, CollectionProperty patterns)
    {
        PropertyIterator iter = getExcludePatterns().iterator();
        while (iter.hasNext())
        {
            String item = iter.next().getStringValue();
            Pattern pattern = null;
            try
            {
                pattern =
                    patternCache.getPattern(
                        item,
                        Perl5Compiler.READ_ONLY_MASK
                            | Perl5Compiler.SINGLELINE_MASK);
                if (matcher.matches(url, pattern))
                {
                    return true;
                }
            }
            catch (MalformedCachePatternException e)
            {
                log.warn("Skipped invalid pattern: " + item, e);
            }
        }
        return false;
    }

    protected void replaceValues(TestElement sampler, TestElement[] configs)
    {
        try
        {
            GuiPackage.getInstance().getReplacer().reverseReplace(sampler);
            for (int i = 0; i < configs.length; i++)
            {
                if (configs[i] != null)
                {
                    GuiPackage.getInstance().getReplacer().reverseReplace(
                        configs[i]);
                }

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
    
    /**
     * This will notify sample listeners directly within the Proxy
     * of the sampling that just occured -- so that we have a
     * means to record the server's responses as we go.
     * 
     * @param event sampling event to be delivered
     */
    private void notifySampleListeners(SampleEvent event)
    {
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        JMeterTreeNode myNode = treeModel.getNodeOf(this);
        Enumeration enum = myNode.children();
        while (enum.hasMoreElements())
        { 
            JMeterTreeNode subNode =
                (JMeterTreeNode) enum.nextElement();
            if (subNode.isEnabled()) {
                TestElement testElement =
                    (TestElement) subNode.createTestElement();
                if (testElement instanceof SampleListener) {
                    ((SampleListener)testElement).sampleOccurred(event);
                }
            }
        }
    }

    /**
     * This will notify test listeners directly within the Proxy that the 'test'
     * (here meaning the proxy recording) has started.
     */
    private void notifyTestListenersOfStart()
    {
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        JMeterTreeNode myNode = treeModel.getNodeOf(this);
        Enumeration enum = myNode.children();
        while (enum.hasMoreElements())
        {
            JMeterTreeNode subNode =
                (JMeterTreeNode) enum.nextElement();
            if (subNode.isEnabled()) {
                TestElement testElement =
                    (TestElement) subNode.createTestElement();
                if (testElement instanceof TestListener) {
                    ((TestListener)testElement).testStarted();
                }
            }
        }
    }
    
    /**
     * This will notify test listeners directly within the Proxy that the 'test'
     * (here meaning the proxy recording) has ended.
     */
    private void notifyTestListenersOfEnd()
    {
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        JMeterTreeNode myNode = treeModel.getNodeOf(this);
        Enumeration enum = myNode.children();
        while (enum.hasMoreElements())
        {
            JMeterTreeNode subNode =
                (JMeterTreeNode) enum.nextElement();
            if (subNode.isEnabled()) {
                TestElement testElement =
                    (TestElement) subNode.createTestElement();
                if (testElement instanceof TestListener) {
                    ((TestListener)testElement).testEnded();
                }
            }
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
