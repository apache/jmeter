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

package org.apache.jmeter.protocol.ftp.sampler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Simple FTP client (non-passive transfers don't work yet).
 * Kind of a hack, lots of room for optimizations.
 *
 * @author     mike
 * Created    August 31, 2001
 * @version $Revision$ Last updated: $Date$
 */
public class FtpClient
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    //File f = new File("e:\\");
    BufferedWriter out;
    BufferedReader in;
    Socket s;
    boolean passive = false;
    static int port = 21;
    static int dataPort = 4096;

    /**
     *  Constructor for the FtpClient object.
     */
    public FtpClient()
    {
    }

    /**
     * Set passive mode.
     *
     *@param  flag  the new Passive value
     */
    public void setPassive(boolean flag)
    {
        passive = flag;
    }

    /**
     * Get a file from the server.
     *
     * @return                  the Response value
     */
    public String getResponse() throws IOException
    {
        StringBuffer response = new StringBuffer();
        String line = in.readLine();
        response.append(line);
        log.info("FtpClient.getResponse(): #" + line + "#");
        while (line.charAt(3) == '-')
        {
            line = in.readLine();
            response.append("\n");
            response.append(line);
            log.info("FtpClient.getResponse(): #" + line + "#");
        }
        log.info("return response");
        return response.toString();
    }

    /**
     * Get a file from the server.
     */
    public String get(String file) throws Exception
    {
        send("SYST");
        getResponse();
        send("PWD");
        getResponse();
        send("TYPE I");
        getResponse();
        String data = "";
        if (!passive)
        {
            dataPort++;
            int upper = getUpper(dataPort);
            int lower = getLower(dataPort);
            String ip =
                InetAddress.getLocalHost().getHostAddress().replace('.', ',');
            String port = ip + "," + upper + "," + lower;
            log.info("port:" + port);
            send("PORT " + port);
            getResponse();
            dataGrabber grab = new dataGrabber(ip, dataPort);
            while (!grab.isPortCreated())
            {
            }
            send("RETR " + file);
            String response = in.readLine();
            log.info(response);
            log.info("" + dataPort);
            data = "FTP client - File Not Found";
            if (!response.startsWith("5"))
            {
                while (!grab.isDone())
                {
                }
                data = grab.getData();
            }
        }
        else
        {
            send("PASV");
            String port = getResponse();
            while (!port.startsWith("227"))
            {
                port = getResponse();
            }
            int start = port.indexOf('(');
            int end = port.indexOf(')');
            port = port.substring(start + 1, end);
            int a = port.indexOf(',');
            int b = port.indexOf(',', a + 1);
            int c = port.indexOf(',', b + 1);
            int d = port.indexOf(',', c + 1);
            int e = port.indexOf(',', d + 1);
            String ip =
                port.substring(0, a)
                    + "."
                    + port.substring(a + 1, b)
                    + "."
                    + port.substring(b + 1, c)
                    + "."
                    + port.substring(c + 1, d);
            int upper = Integer.parseInt(port.substring(d + 1, e));
            int lower = Integer.parseInt(port.substring(e + 1));
            int dataPort = getPort(upper, lower);
            send("RETR " + file);
            dataGrabber grab = new dataGrabber(ip, dataPort);
            getResponse();
            while (!grab.isDone())
            {
            }
            data = grab.getData();
        }
        return data;
    }

    /**
     * Connect to server.
     */
    public void connect(String host, String username, String password)
        throws Exception
    {
        InetAddress addr = InetAddress.getByName(host);
        s = new Socket(addr, port);
        out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

        InputStreamReader isr = new InputStreamReader(s.getInputStream());
        in = new BufferedReader(isr);
        send("USER " + username);
        send("PASS " + password);
    }

    /**
     * Disconnect from the server
     */
    public void disconnect()
    {
        try
        {
            send("QUIT");
            getResponse();
        }
        catch (Exception e)
        {
            log.error("FTP client - ", e);
        }
        try
        {
            in.close();
            out.close();
            s.close();
        }
        catch (Exception e)
        {
            log.error("FTP client - ", e);
        }
    }

    /**
     * Send a command to the server.
     */
    public void send(String command) throws IOException
    {
        for (int i = 0; i < command.length(); i++)
        {
            out.write(command.charAt(i));
        }
        out.write('\r');
        out.write('\n');
        out.flush();
    }

    /**
     * Gets the Port attribute of the FtpClient class.
     * @return        the Port value
     */
    public static int getPort(int upper, int lower)
    {
        return upper * 256 + lower;
    }

    /**
     * Gets the Upper attribute of the FtpClient class.
     * @return       the Upper value
     */
    public static int getUpper(int port)
    {
        return port / 256;
    }

    /**
     * Gets the Lower attribute of the FtpClient class.
     *
     * @return       the Lower value
     */
    public static int getLower(int port)
    {
        return port % 256;
    }

    /**
     * Grabs the data from the dataport.
     *
     * @author     mike
     * Created    August 31, 2001
     * @version $Revision$ Last updated: $Date$
     */
    public class dataGrabber implements Runnable
    {
        StringBuffer buffer = new StringBuffer();
        Socket s;
        boolean done = false;
        boolean portCreated = false;
        String host = "";
        int port = 22;

        /**
         * Constructor for the dataGrabber object.
         */
        public dataGrabber(String host, int port) throws Exception
        {
            this.host = host;
            this.port = port;
            new Thread((Runnable) this).start();
        }

        /**
         * Gets the Done attribute of the dataGrabber object.
         *
         * @return    the Done value
         */
        public boolean isDone()
        {
            return done;
        }

        /**
         * Gets the Data attribute of the dataGrabber object.
         *
         * @return    the Data value
         */
        public String getData()
        {
            return buffer.toString();
        }

        /**
         * Gets the PortCreated attribute of the dataGrabber object.
         *
         * @return    the PortCreated value
         */
        public boolean isPortCreated()
        {
            return portCreated;
        }

        /**
         * Main processing method for the dataGrabber object.
         */
        public void run()
        {
            try
            {
                if (passive)
                {
                    s = new Socket(host, port);
                }
                else
                {
                    log.info("creating socket on " + port);
                    ServerSocket server = new ServerSocket(port);
                    log.info("accepting...");
                    portCreated = true;
                    s = server.accept();
                    log.info("accepted");
                }
            }
            catch (Exception e)
            {
            }
            try
            {
                InputStream in = s.getInputStream();
                BufferedInputStream dataIn = new BufferedInputStream(in);
                int bufferSize = 4096;
                byte[] inputBuffer = new byte[bufferSize];
                int i = 0;
                while ((i = dataIn.read(inputBuffer, 0, bufferSize)) != -1)
                {
                    buffer.append((char) i);
                }
                dataIn.close();
                s.close();
            }
            catch (Exception e)
            {
                log.error("FTP client: dataGrabber", e);
            }
            done = true;
        }
    }
}
