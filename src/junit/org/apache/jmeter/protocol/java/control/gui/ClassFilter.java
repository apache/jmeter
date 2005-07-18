/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
package org.apache.jmeter.protocol.java.control.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author pete
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClassFilter {

    protected String[] pkgs = new String[0];
	/**
	 * 
	 */
	public ClassFilter() {
		super();
	}
    
    public void setPackges(String[] pk) {
        this.pkgs = pk;
    }
    
    public void addPackage(String pkg) {
        String[] newpkg = new String[pkgs.length + 1];
        System.arraycopy(pkgs,0,newpkg,0,pkgs.length);
        newpkg[pkgs.length] = pkg;
        pkgs = newpkg;
    }
    
    public boolean include(String text) {
        boolean inc = false;
        for (int idx=0; idx < pkgs.length; idx++) {
            if (text.startsWith(pkgs[idx])){
                inc = true;
                break;
            }
        }
        return inc;
    }
    
    public Object[] filterArray(List items) {
        Iterator itr = items.iterator();
        ArrayList newlist = new ArrayList();
        while (itr.hasNext()) {
            Object item = itr.next();
            if (include((String)item)) {
                newlist.add(item);
            }
        }
        if (newlist.size() > 0) {
            return newlist.toArray();
        } else {
            return new Object[0];
        }
    }

    public int size(){
        return pkgs.length;
    }
}
