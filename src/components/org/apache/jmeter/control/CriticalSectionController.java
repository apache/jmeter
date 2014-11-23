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

package org.apache.jmeter.control;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This is a Critical Section Controller; it will execute the set of statements
 * (samplers/controllers, etc) under named lock.
 * <p>
 * In a programming world - this is equivalent of :
 * 
 * <pre>
 * try {
 *          named_lock.lock();
 *          statements ....
 * } finally {
 *          named_lock.unlock();
 * }
 * </pre>
 * 
 * In JMeter you may have :
 * 
 * <pre>
 * Thread-Group (set to loop a number of times or indefinitely,
 *    ... Samplers ... (e.g. Counter )
 *    ... Other Controllers ....
 *    ... CriticalSectionController ( lock name like "foobar" )
 *       ... statements to perform when lock acquired
 *       ...
 *    ... Other Controllers /Samplers }
 * </pre>
 * 
 * @since 2.12
 */
public class CriticalSectionController extends GenericController implements
        ThreadListener, TestStateListener {

    /**
     * 
     */
    private static final long serialVersionUID = 4362876132435968088L;

    private static final Logger logger = LoggingManager.getLoggerForClass();

    private static final String LOCK_NAME = "CriticalSectionController.lockName"; //$NON-NLS-1$

    private static final ConcurrentHashMap<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<String, ReentrantLock>();

    private transient volatile ReentrantLock currentLock;

    /**
     * constructor
     */
    public CriticalSectionController() {
        super();
    }

    /**
     * constructor
     * @param name The name of this controller
     */
    public CriticalSectionController(String name) {
        super();
        this.setName(name);
    }

    /**
     * Condition Accessor - this is gonna be any string value
     * @param name The name of the lock for this controller
     */
    public void setLockName(String name) {
        setProperty(new StringProperty(LOCK_NAME, name));
    }

    /**
     * If lock exists returns it, otherwise creates one, puts it in LOCK_MAP 
     * then returns it
     * 
     * @return {@link ReentrantLock}
     */
    private ReentrantLock getOrCreateLock() {
        String lockName = getLockName();
        ReentrantLock lock = LOCK_MAP.get(lockName);
        ReentrantLock prev = null;
        if (lock != null) {
            return lock;
        }
        lock = new ReentrantLock();
        prev = LOCK_MAP.putIfAbsent(lockName, lock);
        return prev == null ? lock : prev;
    }

    /**
     * @return String lock name
     */
    public String getLockName() {
        return getPropertyAsString(LOCK_NAME);
    }

    /**
     * @see org.apache.jmeter.control.Controller#next()
     */
    @Override
    public Sampler next() {
        if (StringUtils.isEmpty(getLockName())) {
            logger.warn("Empty lock name in Critical Section Controller:"
                    + getName());
            return super.next();
        }
        if (isFirst()) {
            // Take the lock for first child element
            long startTime = System.currentTimeMillis();
            if (this.currentLock == null) {
                this.currentLock = getOrCreateLock();
            }
            this.currentLock.lock();
            long endTime = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug(Thread.currentThread().getName()
                        + " acquired lock:'" + getLockName()
                        + "' in Critical Section Controller " + getName()
                        + " in:" + (endTime - startTime) + " ms");
            }
        }
        return super.next();
    }

    /**
     * Called after execution of last child of the controller We release lock
     * 
     * @see org.apache.jmeter.control.GenericController#reInitialize()
     */
    @Override
    protected void reInitialize() {
        if (this.currentLock != null) {
            if (currentLock.isHeldByCurrentThread()) {
                this.currentLock.unlock();
            }
            this.currentLock = null;
        }
        super.reInitialize();
    }

    @Override
    public void threadStarted() {
        this.currentLock = null;
    }

    @Override
    public void threadFinished() {
        if (this.currentLock != null
                && this.currentLock.isHeldByCurrentThread()) {
            logger.warn("Lock " + getLockName() + " not released in:"
                    + getName() + ", releasing in threadFinished");
            this.currentLock.unlock();
        }
        this.currentLock = null;
    }

    @Override
    public void testStarted() {
        // NOOP
    }

    @Override
    public void testStarted(String host) {
        // NOOP
    }

    @Override
    public void testEnded() {
        LOCK_MAP.clear();
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }
}
