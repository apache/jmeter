/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.testelement;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.NewDriver;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * @author Michael Stover Created March 13, 2001
 * @version $Revision$ Last updated: $Date$
 */
public class TestPlan extends AbstractTestElement implements Serializable, TestListener {
	private static Logger log = LoggingManager.getLoggerForClass();

    // Does not appear to be needed
	public final static String THREAD_GROUPS = "TestPlan.thread_groups";

	public final static String FUNCTIONAL_MODE = "TestPlan.functional_mode";

	public final static String USER_DEFINED_VARIABLES = "TestPlan.user_defined_variables";

	public final static String SERIALIZE_THREADGROUPS = "TestPlan.serialize_threadgroups";

    public final static String CLASSPATHS = "TestPlan.user_define_classpath";
    private static final String CLASSPATH_SEPARATOR = ",";
    
	public final static String COMMENTS = "TestPlan.comments";

    // Does not appear to be needed
	public final static String BASEDIR = "basedir";

    // Does not appear to be needed
	private transient List threadGroups = new LinkedList();

    // Does not appear to be needed
	private transient List configs = new LinkedList();

    // Does not appear to be needed
	private static List itemsCanAdd = new LinkedList();

    // Does not appear to be needed
	private static TestPlan plan;

	// There's only 1 test plan, so can cache the mode here
	private static boolean functionalMode = false;

	static {
		// WARNING! This String value must be identical to the String value
		// returned in org.apache.jmeter.threads.ThreadGroup.getClassLabel()
		// method. If it's not you will not be able to add a Thread Group
		// element to a Test Plan.

        // Does not appear to be needed
		itemsCanAdd.add(JMeterUtils.getResString("threadgroup"));
	}

	public TestPlan() {
		// this("Test Plan");
		// setFunctionalMode(false);
		// setSerialized(false);
	}

	public TestPlan(String name) {
		setName(name);
		// setFunctionalMode(false);
		// setSerialized(false);

        // Does not appear to be needed
        setProperty(new CollectionProperty(THREAD_GROUPS, threadGroups));
	}
    
    public void prepareForPreCompile()
    {
        getVariables().setRunningVersion(true);
    }

	/**
	 * Fetches the functional mode property
	 * 
	 * @return functional mode
	 */
	public boolean isFunctionalMode() {
		return getPropertyAsBoolean(FUNCTIONAL_MODE);
	}

	public void setUserDefinedVariables(Arguments vars) {
		setProperty(new TestElementProperty(USER_DEFINED_VARIABLES, vars));
	}

    // Does not appear to be needed
	public String getBasedir() {
		return getPropertyAsString(BASEDIR);
	}

    // Does not appear to be needed
	public void setBasedir(String b) {
		setProperty(BASEDIR, b);
	}

	public Map getUserDefinedVariables() {
		Arguments args = getVariables();
		return args.getArgumentsAsMap();
	}

	private Arguments getVariables() {
		Arguments args = (Arguments) getProperty(USER_DEFINED_VARIABLES).getObjectValue();
		if (args == null) {
			args = new Arguments();
			setUserDefinedVariables(args);
		}
		return args;
	}

	public void setFunctionalMode(boolean funcMode) {
		setProperty(new BooleanProperty(FUNCTIONAL_MODE, funcMode));
		functionalMode = funcMode;
	}

	/**
	 * Gets the static copy of the functional mode
	 * 
	 * @return mode
	 */
	public static boolean getFunctionalMode() {
		return functionalMode;
	}

	public void setSerialized(boolean serializeTGs) {
		setProperty(new BooleanProperty(SERIALIZE_THREADGROUPS, serializeTGs));
	}
    
    /**
     * Set the classpath for the test plan
     * @param text
     */
    public void setTestPlanClasspath(String text) {
        setProperty(CLASSPATHS,text);
    }
    
    public void setTestPlanClasspathArray(String[] text) {
        StringBuffer cat = new StringBuffer();
        for (int idx=0; idx < text.length; idx++) {
            if (idx > 0) {
                cat.append(CLASSPATH_SEPARATOR);
            }
            cat.append(text[idx]);
        }
        this.setTestPlanClasspath(cat.toString());
    }
    
    public String[] getTestPlanClasspathArray() {
        return JOrphanUtils.split(this.getTestPlanClasspath(),CLASSPATH_SEPARATOR);
    }
    
    /**
     * Returns a string in CSV format
     * @return
     */
    public String getTestPlanClasspath() {
        return getPropertyAsString(CLASSPATHS);
    }

	/**
	 * Fetch the serialize threadgroups property
	 * 
	 * @return serialized setting
	 */
	public boolean isSerialized() {
		return getPropertyAsBoolean(SERIALIZE_THREADGROUPS);
	}

	public void addParameter(String name, String value) {
		getVariables().addArgument(name, value);
	}

    // Does not appear to be needed
	public static TestPlan createTestPlan(String name) {
		if (plan == null) {
			if (name == null) {
				plan = new TestPlan();
			} else {
				plan = new TestPlan(name);
			}
			plan.setProperty(new StringProperty(TestElement.GUI_CLASS, "org.apache.jmeter.control.gui.TestPlanGui"));
		}
		return plan;
	}

	public void addTestElement(TestElement tg) {
		super.addTestElement(tg);
		if (tg instanceof ThreadGroup && !isRunningVersion()) {
			addThreadGroup((ThreadGroup) tg);
		}
	}

    // Does not appear to be needed
	public void addJMeterComponent(TestElement child) {
		if (child instanceof ThreadGroup) {
			addThreadGroup((ThreadGroup) child);
		}
	}

	/**
	 * Gets the ThreadGroups attribute of the TestPlan object.
	 * 
	 * @return the ThreadGroups value
	 */
    // Does not appear to be needed
	public Collection getThreadGroups() {
		return threadGroups;
	}

	/**
	 * Adds a feature to the ConfigElement attribute of the TestPlan object.
	 * 
	 * @param c
	 *            the feature to be added to the ConfigElement attribute
	 */
    // Does not appear to be needed
	public void addConfigElement(ConfigElement c) {
		configs.add(c);
	}

	/**
	 * Adds a feature to the ThreadGroup attribute of the TestPlan object.
	 * 
	 * @param group
	 *            the feature to be added to the ThreadGroup attribute
	 */
    // Does not appear to be needed
	public void addThreadGroup(ThreadGroup group) {
		threadGroups.add(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded()
	 */
	public void testEnded() {
		try {
			FileServer.getFileServer().closeFiles();
		} catch (IOException e) {
			log.error("Problem closing files at end of test", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
	 */
	public void testEnded(String host) {
		testEnded();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
	 */
	public void testIterationStart(LoopIterationEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted()
	 */
	public void testStarted() {
		if (getBasedir() != null && getBasedir().length() > 0) {
			try {
				FileServer.getFileServer().setBasedir(FileServer.getFileServer().getBaseDir() + getBasedir());
			} catch (IOException e) {
				log.error("Failed to set file server base dir with " + getBasedir(), e);
			}
		}
        // we set the classpath
        String[] paths = this.getTestPlanClasspathArray();
        for (int idx=0; idx < paths.length; idx++) {
            NewDriver.addURL(paths[idx]);
            log.info("add " + paths[idx] + " to classpath");
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
	 */
	public void testStarted(String host) {
		testStarted();
	}
    
}
