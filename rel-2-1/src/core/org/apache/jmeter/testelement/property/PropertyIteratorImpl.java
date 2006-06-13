// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.testelement.property;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Administrator
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class PropertyIteratorImpl implements PropertyIterator {

	Iterator iter;

	public PropertyIteratorImpl(Collection value) {
		iter = value.iterator();
	}

	public PropertyIteratorImpl() {
	}

	public void setCollection(Collection value) {
		iter = value.iterator();
	}

	public boolean hasNext() {
		return iter.hasNext();
	}

	public JMeterProperty next() {
		return (JMeterProperty) iter.next();
	}

	/**
	 * @see org.apache.jmeter.testelement.property.PropertyIterator#remove()
	 */
	public void remove() {
		iter.remove();
	}

}
