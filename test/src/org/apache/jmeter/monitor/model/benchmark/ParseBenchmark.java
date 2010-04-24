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
 */
package org.apache.jmeter.monitor.model.benchmark;

public class ParseBenchmark {

    /**
     * 
     */
    public ParseBenchmark() {
        super();
    }

    public static void main(String[] args) {
        if (args.length == 3) {
            int parser = 0;
            String file = null;
            int loops = 1000;
            if (args[0] != null) {
                if (!args[0].equals("jaxb")) {
                    parser = 1;
                }
            }
            if (args[1] != null) {
                file = args[1];
            }
            if (args[2] != null) {
                loops = Integer.parseInt(args[2]);
            }

            java.io.File infile = new java.io.File(file);
            java.io.FileInputStream fis = null;
            java.io.InputStreamReader isr = null;
            StringBuilder buf = new StringBuilder();
            try {
                fis = new java.io.FileInputStream(infile);
                isr = new java.io.InputStreamReader(fis);
                java.io.BufferedReader br = new java.io.BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    buf.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            long start = 0;
            long end = 0;
            String contents = buf.toString().trim();
            System.out.println("start test: " + loops + " iterations");
            System.out.println("content:");
            System.out.println(contents);

            if (parser == 0) {
                /**
                 * try { JAXBContext jxbc = new
                 * org.apache.jorphan.tomcat.manager.ObjectFactory();
                 * Unmarshaller mar = jxbc.createUnmarshaller();
                 * 
                 * start = System.currentTimeMillis(); for (int idx=0; idx <
                 * loops; idx++){ StreamSource ss = new StreamSource( new
                 * ByteArrayInputStream(contents.getBytes())); Object ld =
                 * mar.unmarshal(ss); } end = System.currentTimeMillis();
                 * System.out.println("elapsed Time: " + (end - start)); } catch
                 * (JAXBException e){ }
                 */
            } else {
                org.apache.jmeter.monitor.model.ObjectFactory of = org.apache.jmeter.monitor.model.ObjectFactory
                        .getInstance();
                start = System.currentTimeMillis();
                for (int idx = 0; idx < loops; idx++) {
                    // NOTUSED org.apache.jmeter.monitor.model.Status st =
                    of.parseBytes(contents.getBytes()); // TODO - charset?
                }
                end = System.currentTimeMillis();
                System.out.println("elapsed Time: " + (end - start));
            }

        } else {
            System.out.println("missing paramters:");
            System.out.println("parser file iterations");
            System.out.println("example: jaxb status.xml 1000");
        }
    }
}
