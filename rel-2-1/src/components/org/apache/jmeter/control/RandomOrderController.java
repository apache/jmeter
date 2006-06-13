// $Header$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jmeter.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A controller that runs its children each at most once, but in a random order.
 * 
 * @author Mike Verdone
 * @version $Revision$ updated on $Date$
 */
public class RandomOrderController extends GenericController implements Serializable {
	/**
	 * Create a new RandomOrderController.
	 */
	public RandomOrderController() {
	}

	/**
	 * @see GenericController#initialize()
	 */
	public void initialize() {
		super.initialize();
		this.reorder();
	}

	/**
	 * @see GenericController#reInitialize()
	 */
	public void reInitialize() {
		super.reInitialize();
		this.reorder();
	}

	/**
	 * Replace the subControllersAndSamplers list with a reordered ArrayList.
	 */
	private void reorder() {
		int numElements = this.subControllersAndSamplers.size();

		// Create a new list containing numElements null elements.
		List reordered = new ArrayList(this.subControllersAndSamplers.size());
		for (int i = 0; i < numElements; i++) {
			reordered.add(null);
		}

		// Insert the subControllersAndSamplers into random list positions.
		for (Iterator i = this.subControllersAndSamplers.iterator(); i.hasNext();) {
			int idx = (int) Math.floor(Math.random() * reordered.size());
			while (true) {
				if (idx == numElements) {
					idx = 0;
				}
				if (reordered.get(idx) == null) {
					reordered.set(idx, i.next());
					break;
				}
				idx++;
			}
		}

		// Replace subControllersAndSamplers with reordered copy.
		this.subControllersAndSamplers = reordered;
	}
}
