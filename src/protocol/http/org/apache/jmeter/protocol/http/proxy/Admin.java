package org.apache.jmeter.protocol.http.proxy;
/******************************************************************
*** File Admin.java
***
***/

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

//
// Class:     Admin
// Abstract:  The admin thread listens on admin socket and handle all
//            communications with the remote administrator.
//

public class Admin extends Thread
{
	
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");
    //
    // Member variables
    //

    ServerSocket adminSocket = null;
    Socket appletSocket = null;
    String passwordCandidate = null;
    BufferedReader in = null;
    DataOutputStream out = null;
    Config config = null;
    Cache cache;


    //
    // Public methods
    //

    //
    // Constructor
    //
    Admin(Config configObject, Cache cacheManager)
    {
        try
        {
            config = configObject;
            cache = cacheManager;
            adminSocket = new ServerSocket(0);
            config.setAdminPort(adminSocket.getLocalPort());
        }
        catch (IOException e)
        {
            log.error("Error opening admin socket",e);
        }
    }


    //
    // Handle communications with remote administrator
    //
    public void run()
    {
        while(true)
        {
            try
            {
                appletSocket = adminSocket.accept();
                in = new BufferedReader(new InputStreamReader(new DataInputStream(appletSocket.getInputStream())));
                out = new DataOutputStream(appletSocket.getOutputStream());

                do
                {
                    // Read password candidate sent by applet
                    String passwordCandidate = in.readLine();

                    // Send applet ack/nack on password
                    if (config.getPassword().equals(passwordCandidate))
                    {
                        out.writeBytes("ACCEPT\n");
                        break;
                    }
                    else
                    {
                        out.writeBytes("REJECT\n");
                    }
                    out.flush();
                }
                while (true);

                //
                // Password is OK, so let's send the administrator the
                // parameters values and read his new values
                //
                while(true)
                {
                    out.writeBytes(config.toString());
                    out.flush();

                    config.parse(in.readLine());
                    log.info("Configuration changed by administrator.");

                    // Administrator wants to clean the cache
                    if (config.getCleanCache())
                    {
                        cache.clean();
                        config.setCleanCache(false); //no need to clean again
                    }
                }
            }
            catch (Exception e)
            {
                //
                // This line was reached because the administrator closed
                // the connection with the proxy. That's fine, we are now
                // available for another administrator to log in.
                //
                log.error("Connection with administrator closed.",e);
            }
            finally
            {
                try
                {
                    out.close();
                    in.close();
                }
                catch(Exception exc)
                {}
            }
        }//while
    }
}

