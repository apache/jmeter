// $Header$
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

package org.apache.jmeter.protocol.http.modifier;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This module controls the Sequence in which user details are returned. This
 * module uses round robin allocation of users.
 * 
 * @author Mark Walsh
 * @version $Revision$
 */
public class UserSequence implements Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	// -------------------------------------------
	// Constants and Data Members
	// -------------------------------------------
	private List allUsers;

	private transient Iterator indexOfUsers;

	// -------------------------------------------
	// Constructors
	// -------------------------------------------

	public UserSequence() {
	}

	/**
	 * Load all user and parameter data into the sequence module.
	 * <P>
	 * ie a Set of Mapped "parameter names and parameter values" for each user
	 * to be loaded into the sequencer.
	 */
	public UserSequence(List allUsers) {
		this.allUsers = allUsers;

		// initalise pointer to first user
		indexOfUsers = allUsers.iterator();
	}

	// -------------------------------------------
	// Methods
	// -------------------------------------------

	/**
	 * Returns the parameter data for the next user in the sequence
	 * 
	 * @return a Map object of parameter names and matching parameter values for
	 *         the next user
	 */
	public synchronized Map getNextUserMods() {
		// Use round robin allocation of user details
		if (!indexOfUsers.hasNext()) {
			indexOfUsers = allUsers.iterator();
		}

		Map user;
		if (indexOfUsers.hasNext()) {
			user = (Map) indexOfUsers.next();
			log.debug("UserSequence.getNextuserMods(): current parameters will be " + "changed to: " + user);
		} else {
			// no entries in all users, therefore create an empty Map object
			user = new HashMap();
		}

		return user;
	}
}
