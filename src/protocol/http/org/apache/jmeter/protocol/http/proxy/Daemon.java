package org.apache.jmeter.protocol.http.proxy;

import java.io.*;
import org.apache.jmeter.protocol.http.control.CookieManager;

/************************************************************
 *  File Daemon.java
 ***********************************************************/
import java.net.*;
//
// Class:     Daemon
// Abstract:  Web daemon thread. creates main socket on port 8080
//            and listens on it forever. For each client request,
//            creates proxy thread to handle the request.
//
/************************************************************
 *  Description of the Class
 *
 *@author     default
 *@created    June 29, 2001
 ***********************************************************/
public class Daemon extends Thread
{
	//
	// Member variables
	//
	static ServerSocket MainSocket = null;
	static Cache cache = null;
	static Config config;
	static String adminPath;
	final static int defaultDaemonPort = 8080;
	final static int maxDaemonPort = 65536;
	private int daemonPort;
	private boolean running;
	ProxyControl target;
	private Socket ClientSocket;

	public Daemon()
	{
	}

	public Daemon(int port,ProxyControl target) throws UnknownHostException
	{
		this.target = target;
		configureProxy(port);
	}

	/************************************************************
	 *  Description of the Method
	 *
	 *@param  daemonPort  Description of Parameter
	 ***********************************************************/
	public void configureProxy(int daemonPort) throws UnknownHostException
	{
		this.daemonPort = daemonPort;
		// Create the Cache Manager and Configuration objects
		System.out.println("Initializing...");
		System.out.print("Creating Config Object...");
		config = new Config();
		config.setIsAppletContext(false);
		config.setLocalHost(InetAddress.getLocalHost().getHostName());
		String tmp = InetAddress.getLocalHost().toString();
		config.setLocalIP(tmp.substring(tmp.indexOf('/') + 1));
		config.setProxyMachineNameAndPort(InetAddress.getLocalHost().getHostName() + ":" + daemonPort);
		config.setJmxScriptDir("proxy_script");
		File adminDir = new File("Applet");
		config.setAdminPath(adminDir.getAbsolutePath());
		System.out.println("OK");
		System.out.print("Creating Cache Manager...");
		cache = new Cache(config);
		System.out.println("OK");
	}

	//
	// Member methods
	//

	// Application starts here
	/************************************************************
	 *  Description of the Method
	 *
	 *@param  args  Description of Parameter
	 ***********************************************************/
	public static void main(String args[])
	{
		int daemonPort;
		// Parse command line
		switch (args.length)
		{
			case 0:
				daemonPort = defaultDaemonPort;
				break;
			case 1:
				try
				{
					daemonPort = Integer.parseInt(args[0]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Error: Invalid daemon port");
					return;
				}
				if(daemonPort > maxDaemonPort)
				{
					System.out.println("Error: Invalid daemon port");
					return;
				}
				break;
			default:
				System.out.println("Usage: Proxy [daemon port]");
				return;
		}
		Daemon demon = new Daemon();
		try
		{
			demon.configureProxy(daemonPort);
		}
		catch(UnknownHostException e)
		{
			System.out.println("Unknown host");
			System.exit(-1);
		}
		demon.start();
	}

	/************************************************************
	 *  Main processing method for the Daemon object
	 ***********************************************************/
	public void run()
	{
		CookieManager cookieManager = new CookieManager();
		running = true;
		try
		{
			System.out.print("Creating Daemon Socket...");
			MainSocket = new ServerSocket(daemonPort);
			System.out.println(" port " + daemonPort + " OK");
			System.out.println("Proxy up and running!");
			while(running)
			{
				// Listen on main socket
				Socket ClientSocket = MainSocket.accept();
				// Pass request to new proxy thread
				Proxy thd = new Proxy(ClientSocket, cache, config,target,cookieManager);
				thd.start();
			}
			System.out.println("Proxy Server stopped");
		}
		catch(Exception e)
		{
			System.out.println("Proxy Server stopped");
		}
		finally
		{
			try
			{
				MainSocket.close();
			}
			catch(Exception exc)
			{
			}
		}
	}
	public void stopServer() {
		this.running = false;
		Socket endIt = null;
		try
		{
			endIt = new Socket(InetAddress.getLocalHost().getHostName(),daemonPort);
			endIt.getOutputStream().write(5);
		}
		catch(IOException e)
		{
		}
		finally
		{
			try
			{
				endIt.close();
			}
			catch(Exception e)
			{
			}
		}
	}
}

