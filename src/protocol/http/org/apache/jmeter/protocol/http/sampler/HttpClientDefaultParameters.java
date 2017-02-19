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
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.jmeter.NewDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to set up default HttpClient parameters from a file.
 * 
 * Supports Apache HttpClient.
 */
public class HttpClientDefaultParameters {

    private static final Logger log = LoggerFactory.getLogger(HttpClientDefaultParameters.class);

    // Non-instantiable
    private HttpClientDefaultParameters(){
    }

    // Helper class (callback) for applying parameter definitions
    private static abstract class GenericHttpParams {
        public abstract void setParameter(String name, Object value);
        public abstract void setVersion(String name, String value) throws Exception;
    }

    /**
     * Loads a property file and converts parameters as necessary.
     * 
     * @param file the file to load
     * @param params Apache HttpClient parameter instance
     */
    public static void load(String file, 
            final org.apache.http.params.HttpParams params){
        load(file, 
                new GenericHttpParams (){
                    @Override
                    public void setParameter(String name, Object value) {
                        params.setParameter(name, value);
                    }

                    @Override
                    public void setVersion(String name, String value) {
                        String[] parts = value.split("\\.");
                        if (parts.length != 2){
                            throw new IllegalArgumentException("Version must have form m.n");
                        }
                        params.setParameter(name,
                                new org.apache.http.HttpVersion(
                                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
                    }            
                }
            );
    }

    private static void load(String file, GenericHttpParams params){
        log.info("Trying httpclient parameters from "+file);
        File f = new File(file);        
        if(! (f.exists() && f.canRead())) {
            f = new File(NewDriver.getJMeterDir() + File.separator
                    + "bin" + File.separator + file); // $NON-NLS-1$
            log.info(file + " httpclient parameters does not exist, trying "+f.getAbsolutePath());
            if(! (f.exists() && f.canRead())) {
                log.error("Cannot read parameters file for HttpClient: "+ file);
                return;
            }
        }
        log.info("Reading httpclient parameters from "+f.getAbsolutePath());
        Properties props = new Properties();
        try ( InputStream is = new FileInputStream(f) ){
            props.load(is);
            for (Map.Entry<Object, Object> me : props.entrySet()){
                String key = (String) me.getKey();
                String value = (String)me.getValue();
                int typeSep = key.indexOf('$'); // $NON-NLS-1$
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
                        } else if (type.equals("HttpVersion")){ // Commons HttpClient only
                            params.setVersion(name, value);
                        } else {
                            log.warn("Unexpected type: "+type+" for name "+name);
                        }
                    } else {
                            log.info("Defining "+key+ " as "+value);
                            params.setParameter(key, value);
                    }
                } catch (Exception e) {
                    log.error("Error in property: "+key+"="+value+" "+e.toString());
                }
            }
        } catch (IOException e) {
            log.error("Problem loading properties "+e.toString());
        }
    }
}
