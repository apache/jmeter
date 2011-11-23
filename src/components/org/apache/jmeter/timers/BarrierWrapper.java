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

package org.apache.jmeter.timers;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wrapper to {@link CyclicBarrier} to allow lazy init of CyclicBarrier when SyncTimer is configured with 0
 */
class BarrierWrapper implements Cloneable {
	private CyclicBarrier barrier;

	/**
	 * 
	 */
	public BarrierWrapper() {
		this.barrier = null;
	}
	
	/**
	 * @param parties Number of parties
	 */
	public BarrierWrapper(int parties) {
		this.barrier = new CyclicBarrier(parties);
	}
	/**
	 * @param parties Number of parties
	 */
	public synchronized void setup(int parties) {
		if(this.barrier== null) {
			this.barrier = new CyclicBarrier(parties);
		}
	}
	
	/**
	 * @see CyclicBarrier#await()
	 * @return int 
	 * @throws InterruptedException
	 * @throws BrokenBarrierException
	 * @see java.util.concurrent.CyclicBarrier#await()
	 */
	public int await() throws InterruptedException, BrokenBarrierException {
		return barrier.await();
	}

	/**
	 * @param timeout Timeout
	 * @param unit {@link TimeUnit}
	 * @throws InterruptedException
	 * @throws BrokenBarrierException
	 * @throws TimeoutException
	 * @see java.util.concurrent.CyclicBarrier#await(long, java.util.concurrent.TimeUnit)
	 */
	public int await(long timeout, TimeUnit unit) throws InterruptedException,
			BrokenBarrierException, TimeoutException {
		return barrier.await(timeout, unit);
	}

	/**
	 * @see java.util.concurrent.CyclicBarrier#isBroken()
	 */
	public boolean isBroken() {
		return barrier.isBroken();
	}

	/**
	 * @see java.util.concurrent.CyclicBarrier#reset()
	 */
	public void reset() {
		barrier.reset();
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone()  {
		BarrierWrapper barrierWrapper=  null;
		try {
			barrierWrapper = (BarrierWrapper) super.clone();
			barrierWrapper.barrier = this.barrier;
		} catch (CloneNotSupportedException e) {
			//Cannot happen
		}
		return barrierWrapper;
	}
	
}