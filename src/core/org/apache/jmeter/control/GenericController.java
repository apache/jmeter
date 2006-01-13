/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Michael Stover
 * @author Thad Smith
 * @version $Revision$
 */
public class GenericController extends AbstractTestElement implements Controller, Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	protected transient LinkedList iterationListeners = new LinkedList();

	protected transient List subControllersAndSamplers = new ArrayList();

	protected transient int current;

	private transient int iterCount;

	private transient boolean done, first;

	/**
	 * Creates a Generic Controller
	 */
	public GenericController() {
	}

	public void initialize() {
		resetCurrent();
		resetIterCount();
		done = false;
		first = true;
		TestElement elem;
		for (int i = 0; i < subControllersAndSamplers.size(); i++) {
			elem = (TestElement) subControllersAndSamplers.get(i);
			if (elem instanceof Controller) {
				((Controller) elem).initialize();
			}
		}
	}

	protected void reInitialize() {
		resetCurrent();
		incrementIterCount();
		setFirst(true);
		recoverRunningVersion();
	}

	/**
	 * @see org.apache.jmeter.control.Controller#next()
	 */
	public Sampler next() {
		fireIterEvents();
		log.debug("Calling next on: " + this.getClass().getName());
		if (isDone())
			return null;
		Sampler returnValue = null;
		TestElement currentElement = null;
		try {
			currentElement = getCurrentElement();
			setCurrentElement(currentElement);
			if (currentElement == null) {
				// incrementCurrent();
				returnValue = nextIsNull();
			} else {
				if (currentElement instanceof Sampler) {
					returnValue = nextIsASampler((Sampler) currentElement);
				} else {
					returnValue = nextIsAController((Controller) currentElement);
				}
			}
		} catch (NextIsNullException e) {
			returnValue = null;
		}
		return returnValue;
	}

	/**
	 * @see org.apache.jmeter.control.Controller#isDone()
	 */
	public boolean isDone() {
		return done;
	}

	protected void setDone(boolean done) {
		this.done = done;
	}

	protected boolean isFirst() {
		return first;
	}

	public void setFirst(boolean b) {
		first = b;
	}

	protected Sampler nextIsAController(Controller controller) throws NextIsNullException {
		Sampler returnValue;
		Sampler sampler = controller.next();
		if (sampler == null) {
			currentReturnedNull(controller);
			returnValue = next();
		} else {
			returnValue = sampler;
		}
		return returnValue;
	}

	protected Sampler nextIsASampler(Sampler element) throws NextIsNullException {
		incrementCurrent();
		return element;
	}

	protected Sampler nextIsNull() throws NextIsNullException {
		reInitialize();
		return null;
	}

	protected void currentReturnedNull(Controller c) {
		if (c.isDone()) {
			removeCurrentElement();
		} else {
			incrementCurrent();
		}
	}

	/**
	 * Gets the SubControllers attribute of the GenericController object
	 * 
	 * @return the SubControllers value
	 */
	protected List getSubControllers() {
		return subControllersAndSamplers;
	}

	private void addElement(TestElement child) {
		subControllersAndSamplers.add(child);
	}

	protected void setCurrentElement(TestElement currentElement) throws NextIsNullException {
	}

	protected TestElement getCurrentElement() throws NextIsNullException {
		if (current < subControllersAndSamplers.size()) {
			return (TestElement) subControllersAndSamplers.get(current);
		} else {
			if (subControllersAndSamplers.size() == 0) {
				setDone(true);
				throw new NextIsNullException();
			}
			return null;
		}
	}

	protected void removeCurrentElement() {
		subControllersAndSamplers.remove(current);
	}

	protected void incrementCurrent() {
		current++;
	}

	protected void resetCurrent() {
		current = 0;
	}

	public void addTestElement(TestElement child) {
		if (child instanceof Controller || child instanceof Sampler) {
			addElement(child);
		}
	}

	public void addIterationListener(LoopIterationListener lis) {
		/*
		 * A little hack - add each listener to the start of the list - this
		 * ensures that the thread running the show is the first listener and
		 * can modify certain values before other listeners are called.
		 */
		iterationListeners.addFirst(lis);
	}

	protected void fireIterEvents() {
		if (isFirst()) {
			fireIterationStart();
			first = false;
		}
	}

	protected void fireIterationStart() {
		Iterator iter = iterationListeners.iterator();
		LoopIterationEvent event = new LoopIterationEvent(this, getIterCount());
		while (iter.hasNext()) {
			LoopIterationListener item = (LoopIterationListener) iter.next();
			item.iterationStart(event);
		}
	}

	protected int getIterCount() {
		return iterCount;
	}

	protected void incrementIterCount() {
		iterCount++;
	}

	protected void resetIterCount() {
		iterCount = 0;
	}
}
