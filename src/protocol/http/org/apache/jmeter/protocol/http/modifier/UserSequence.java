/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.http.modifier;
 
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/************************************************************
 *  Title: Jakarta-JMeter Description: Copyright: Copyright (c) 2001 Company:
 *  Apache
 * <P> This module controls the Sequence in which user details are returned.
 * <BR>
 * <P> This module uses round robin allocation of users.
 *@author     Mark Walsh
 *@created    $Date$
 *@version    1.0
 ***********************************************************/

public class UserSequence implements Serializable {
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");

    //-------------------------------------------
    // Constants and Data Members
    //-------------------------------------------
    private List allUsers; 
    private Iterator indexOfUsers;

    //-------------------------------------------
    // Constructors
    //-------------------------------------------
    
    public UserSequence()
    {
    }

    /**
     * Load all user and parameter data into the sequence module
     * <BR>
     * ie a Set of Mapped "parameter names and parameter values" for each user to be loader into the sequencer
     */

    public UserSequence(List allUsers) {
	this.allUsers = allUsers;

	// initalise pointer to first user
	indexOfUsers = allUsers.iterator();
    }

    //-------------------------------------------
    // Methods
    //-------------------------------------------
    /**
     * Returns the parameter data for the next user in the sequence
     * @param Returns a Map object of parameter names and matching parameter values for the next user
     */
    public synchronized Map getNextUserMods() {
	
	// Use round robin allocation of user details
	  if (!indexOfUsers.hasNext()) {
	      indexOfUsers = allUsers.iterator();
	  }


	Map user;
	if (indexOfUsers.hasNext() ) {
	    user = (Map)indexOfUsers.next();
	    log.debug("UserSequence.getNextuserMods(): current parameters will be changed to: " + user);
	} else {
	    // no entries in all users, therefore create an empty Map object
	    user = new HashMap();
	}

	return user;

    } // end method getNextUserMods

} // end class
