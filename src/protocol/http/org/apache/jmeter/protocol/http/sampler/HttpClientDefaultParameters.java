/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.jmeter.protocol.http.sampler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/*
 * Utility class to set up default HttpClient parameters from a file
 */
public class HttpClientDefaultParameters {

    private static final Logger log = LoggingManager.getLoggerForClass();

    // Non-instantiable
    private HttpClientDefaultParameters(){        
    }
    
    public static void load(String file, HttpParams params){
        log.info("Reading httpclient parameters from "+file);
        File f = new File(file);
        InputStream is = null;
        Properties props = new Properties();
        try {
            is = new FileInputStream(f);
            props.load(is);
            Iterator pi = props.entrySet().iterator();
            while(pi.hasNext()){
                Map.Entry me = (Map.Entry) pi.next();
                String key = (String) me.getKey();
                String value = (String)me.getValue();
                int typeSep = key.indexOf("$"); // $NON-NLS-1$
                try {
                    if (typeSep > 0){
                        String type = key.substring(typeSep+1);// get past separator
                        String name=key.substring(0,typeSep);
                        log.info("Defining "+name+ " as "+value+" ("+type+")");
                        if (type.equals("Integer")){
                            params.setParameter(name, Integer.valueOf(value));
                        } else if (type.equals("Long")){
                            params.setParameter(name, Long.valueOf(value));
                        } else if (type.equals("Boolean")){
                            params.setParameter(name, Boolean.valueOf(value));
                        } else if (type.equals("HttpVersion")){
                            params.setParameter(name, HttpVersion.parse("HTTP/"+value));
                        } else {
                            log.warn("Unexpected type: "+type);
                        }
                    } else {
                            log.info("Defining "+key+ " as "+value);
                            params.setParameter(key, value);
                    }
                } catch (NumberFormatException e) {
                    log.error("Error in property: "+key+"="+value+" "+e.toString());
                } catch (ProtocolException e) {
                    log.error("Error in property: "+key+"="+value+" "+e.toString());
                }
            }        
        } catch (FileNotFoundException e) {
            log.error("Problem loading properties "+e.toString());
        } catch (IOException e) {
            log.error("Problem loading properties "+e.toString());
        } finally {
            JOrphanUtils.closeQuietly(is);
        }
    }
//    public static void main(String args[]){
//        load("bin/httpclient.parameters",
//            org.apache.commons.httpclient.params.DefaultHttpParams.getDefaultParams());
//    }
}
