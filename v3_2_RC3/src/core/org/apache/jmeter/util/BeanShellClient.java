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

package org.apache.jmeter.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

// N.B. Do not call any JMeter methods; the jar is standalone


/**
 * Implements a client that can talk to the JMeter BeanShell server.
 */
public class BeanShellClient {

    private static final int MINARGS = 3;

    public static void main(String [] args) throws Exception{
        if (args.length < MINARGS){
            System.out.println("Please provide "+MINARGS+" or more arguments:");
            System.out.println("serverhost serverport filename [arg1 arg2 ...]");
            System.out.println("e.g. ");
            System.out.println("localhost 9000 extras/remote.bsh apple blake 7");
            return;
        }
        String host=args[0];
        String portString = args[1];
        String file=args[2];

        int port=Integer.parseInt(portString)+1;// convert to telnet port

        System.out.println("Connecting to BSH server on "+host+":"+portString);

        try (Socket sock = new Socket(host,port);
                InputStream is = sock.getInputStream();
                OutputStream os = sock.getOutputStream()) {
            SockRead sockRead = new SockRead(is);
            sockRead.start();

            sendLine("bsh.prompt=\"\";", os);// Prompt is unnecessary

            sendLine("String [] args={", os);
            for (int i = MINARGS; i < args.length; i++) {
                sendLine("\"" + args[i] + "\",\n", os);
            }
            sendLine("};", os);

            int b;
            try (InputStreamReader fis = new FileReader(file)) {
                while ((b = fis.read()) != -1) {
                    os.write(b);
                }
            }
            sendLine("bsh.prompt=\"bsh % \";", os);// Reset for other users
            os.flush();
            sock.shutdownOutput(); // Tell server that we are done
            sockRead.join(); // wait for script to finish
        }
    }

    private static void sendLine( String line, OutputStream outPipe )
    throws IOException
    {
        outPipe.write( line.getBytes() ); // TODO - charset?
        outPipe.flush();
    }

    private static class SockRead extends Thread {

        private final InputStream is;

        public SockRead(InputStream _is) {
            this.is=_is;
            //this.setDaemon(true);
        }

        @Override
        public void run(){
            System.out.println("Reading responses from server ...");
            int x = 0;
            try {
                while ((x = is.read()) > -1) {
                    char c = (char) x;
                    System.out.print(c);
                }
            } catch (IOException e) {
                e.printStackTrace(); // NOSONAR No way to log here
            } finally {
                System.out.println("... disconnected from server.");
            }
        }
    }
}
