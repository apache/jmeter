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
 */
 
package org.apache.jmeter.protocol.jms.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author pete
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ClientPool {

	private static ArrayList clients = new ArrayList();
	private static HashMap client_map = new HashMap();
	
    /**
     * 
     */
	public static void addClient(ReceiveSubscriber client){
		clients.add(client);
	}

	public static void addClient(OnMessageSubscriber client){
		clients.add(client);
	}

	public static void addClient(Publisher client){
		clients.add(client);	
	}
	
	public static void clearClient(){
		Iterator itr = clients.iterator();
		while (itr.hasNext()){
			Object client = itr.next();
			if (client instanceof ReceiveSubscriber){
				ReceiveSubscriber sub = (ReceiveSubscriber)client;
				if (sub != null){
					sub.close();
					sub = null;
				}
			} else if (client instanceof Publisher){
				Publisher pub = (Publisher)client;
				if (pub != null){
					pub.close();
					pub = null;
				}
			} else if (client instanceof OnMessageSubscriber){
				OnMessageSubscriber sub = (OnMessageSubscriber)client;
				if (sub != null){
					sub.close();
					sub = null;
				}
			}
		}
		clients.clear();
		client_map.clear();
	}
	
	public static void put(Object key, OnMessageSubscriber client){
		client_map.put(key,client);
	}
	
	public static void put(Object key, Publisher client){
		client_map.put(key,client);
	}
	
	public static Object get(Object key){
		return (OnMessageSubscriber)client_map.get(key);
	}
}
