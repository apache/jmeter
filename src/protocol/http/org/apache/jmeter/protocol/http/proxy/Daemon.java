package org.apache.jmeter.protocol.http.proxy;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * Web daemon thread.  Creates main socket on port 8080 and listens on it
 * forever.  For each client request, creates a proxy thread to handle the
 * request.
 *
 * @author     default
 * @created    June 29, 2001
 */
public class Daemon extends Thread
{
    /** Logging */
    private static transient Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.http");

    /** The default port to listen on. */
    private static final int DEFAULT_DAEMON_PORT = 8080;

    /** The maximum allowed port to listen on. */
    private static final int MAX_DAEMON_PORT = 65535;

    /**
     * The time (in milliseconds) to wait when accepting a client connection.
     * The accept will be retried until the Daemon is told to stop.  So this
     * interval is the longest time that the Daemon will have to wait after
     * being told to stop.
     */
    private static final int ACCEPT_TIMEOUT = 1000;

    /** The port to listen on. */
    private int daemonPort;

    /** True if the Daemon is currently running. */
    private boolean running;

    /** The target which will receive the generated JMeter test components. */
    private ProxyControl target;

    /**
     * The proxy class which will be used to handle individual requests.  This
     * class must be the {@link Proxy} class or a subclass.
     */
    private Class proxyClass = Proxy.class;


    /**
     * Default constructor.
     */
    public Daemon()
    {
        super("HTTP Proxy Daemon");
    }

    /**
     * Create a new Daemon with the specified port and target.
     *
     * @param port   the port to listen on.
     * @param target the target which will receive the generated JMeter test
     *               components.
     */
    public Daemon(int port, ProxyControl target)
    {
        this();
        this.target = target;
        configureProxy(port);
    }

    /**
     * Create a new Daemon with the specified port and target, using the
     * specified class to handle individual requests.
     *
     * @param port   the port to listen on.
     * @param target the target which will receive the generated JMeter test
     *               components.
     * @param proxyClass the proxy class to use to handle individual requests.
     *                   This class must be the {@link Proxy} class or a
     *                   subclass.
     */
    public Daemon(int port, ProxyControl target, Class proxyClass)
    {
        this(port, target);
        this.proxyClass = proxyClass;
    }

    /**
     * Configure the Daemon to listen on the specified port.
     * @param daemonPort the port to listen on
     */
    public void configureProxy(int daemonPort)
    {
        this.daemonPort = daemonPort;
        log.info("Proxy: OK");
    }

    /**
     * Main method which will start the Proxy daemon on the specified port (or
     * the default port if no port is specified).
     *
     * @param args the command-line arguments
     */
    public static void main(String args[])
    {
        if (args.length > 1)
        {
            System.err.println ("Usage: Daemon [daemon port]");
            log.info("Usage: Daemon [daemon port]");
            return;
        }

        int daemonPort = DEFAULT_DAEMON_PORT;
        if (args.length > 0)
        {
            try
            {
                daemonPort = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                System.err.println ("Invalid daemon port: " + e);
                log.error("Invalid daemon port", e);
                return;
            }
            if (daemonPort <= 0 || daemonPort > MAX_DAEMON_PORT)
            {
                System.err.println ("Invalid daemon port");
                log.error("Invalid daemon port");
                return;
            }
        }

        Daemon demon = new Daemon();
        demon.configureProxy(daemonPort);
        demon.start();
    }

    /**
     * Listen on the daemon port and handle incoming requests.  This method will
     * not exit until {@link #stopServer()} is called or an error occurs.
     */
    public void run()
    {
        running = true;
        ServerSocket mainSocket = null;

        try
        {
            log.info("Creating Daemon Socket...");
            mainSocket = new ServerSocket(daemonPort);
            mainSocket.setSoTimeout(ACCEPT_TIMEOUT);
            log.info(" port " + daemonPort + " OK");
            log.info("Proxy up and running!");

            while (running)
            {
                try
                {
                    // Listen on main socket
                    Socket clientSocket = mainSocket.accept();
                    if (running)
                    {
                        // Pass request to new proxy thread
                        Proxy thd = (Proxy) proxyClass.newInstance();
                        thd.configure(clientSocket, target);
                        thd.start();
                    }
                    else
                    {
                        // The socket was accepted after we were told to stop.
                        try
                        {
                            clientSocket.close();
                        }
                        catch (IOException e)
                        {
                            // Ignore
                        }
                    }
                }
                catch (InterruptedIOException e)
                {
                    // Timeout occurred.  Ignore, and keep looping until we're
                    // told to stop running.
                }
            }
            log.info("Proxy Server stopped");
        }
        catch (Exception e)
        {
            log.warn("Proxy Server stopped", e);
        }
        finally
        {
            try
            {
                mainSocket.close();
            }
            catch (Exception exc)
            {
            }
        }
    }

    /**
     * Stop the proxy daemon.  The daemon may not stop immediately.
     *
     * @see #ACCEPT_TIMEOUT
     */
    public void stopServer()
    {
        running = false;
    }
}
