package org.apache.jmeter.protocol.http.proxy;

import java.io.File;
import java.util.StringTokenizer;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;



//

// Class:     Config

// Abstract:  Configurable parameters of the proxy. This class is

//            used by both the applet and the proxy.

//


/************************************************************
 *  Description of the Class
 *
 *@author     default
 *@created    June 29, 2001
 ***********************************************************/
class Config
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");

	//

	// Member variables

	//

	String jmxScriptDir;

	private boolean isFatherProxy;

	private String fatherProxyHost;

	private int fatherProxyPort;

	private String[] deniedHosts;

	private String password;

	private boolean isCaching;
	// enable/disable caching

	private long cacheSize;
	// cache size in bytes

	private boolean cleanCache;

	private String[] cacheMasks;


	private long filesCached;

	private long bytesCached;

	private long bytesFree;

	private long hits;

	private long misses;


	private final int defaultProxyPort = 8080;

	private final String defaultPassword = "admin";

	private final long defaultCacheSize = 1000000;


	private String adminPath;

	private int adminPort;


	private String localHost;

	private String localIP;


	private boolean isAppletContext;

	private String separator = " ";

	private String proxyMachineNameAndPort;



	//

	// Member methods

	//


	//

	// Constructor

	//

	Config()
	{

		filesCached = 0;

		bytesCached = 0;

		bytesFree = cacheSize;

		hits = 0;

		misses = 0;


		reset();

	}


	/************************************************************
	 *  Sets the JmxScriptDir attribute of the Config object
	 *
	 *@param  filename  The new JmxScriptDir value
	 ***********************************************************/
	public void setJmxScriptDir(String filename)
	{

		jmxScriptDir = filename;

		File file = new File(System.getProperty("user.dir") + File.separator + jmxScriptDir);

		file.mkdirs();

	}



	//

	// Set/get methods

	//


	// Set if we are in the applet or in the proxy

	/************************************************************
	 *  Sets the IsAppletContext attribute of the Config object
	 *
	 *@param  b  The new IsAppletContext value
	 ***********************************************************/
	public void setIsAppletContext(boolean b)
	{

		isAppletContext = b;

	}


	/************************************************************
	 *  Sets the ProxyMachineNameAndPort attribute of the Config object
	 *
	 *@param  s  The new ProxyMachineNameAndPort value
	 ***********************************************************/
	public void setProxyMachineNameAndPort(String s)
	{

		proxyMachineNameAndPort = s;

	}


	/************************************************************
	 *  Sets the AdminPort attribute of the Config object
	 *
	 *@param  port  The new AdminPort value
	 ***********************************************************/
	public void setAdminPort(int port)
	{

		adminPort = port;

	}


	/************************************************************
	 *  Sets the AdminPath attribute of the Config object
	 *
	 *@param  path  The new AdminPath value
	 ***********************************************************/
	public void setAdminPath(String path)
	{

		adminPath = path;

	}


	/************************************************************
	 *  Sets the LocalHost attribute of the Config object
	 *
	 *@param  host  The new LocalHost value
	 ***********************************************************/
	public void setLocalHost(String host)
	{

		localHost = host;

	}


	/************************************************************
	 *  Sets the LocalIP attribute of the Config object
	 *
	 *@param  ip  The new LocalIP value
	 ***********************************************************/
	public void setLocalIP(String ip)
	{

		localIP = ip;

	}


	/************************************************************
	 *  Sets the IsCaching attribute of the Config object
	 *
	 *@param  caching  The new IsCaching value
	 ***********************************************************/
	public synchronized void setIsCaching(boolean caching)
	{

		isCaching = caching;

	}

	/************************************************************
	 *  Sets the CacheSize attribute of the Config object
	 *
	 *@param  size  The new CacheSize value
	 ***********************************************************/
	public synchronized void setCacheSize(long size)
	{

		cacheSize = size;

	}


	/************************************************************
	 *  Sets the IsFatherProxy attribute of the Config object
	 *
	 *@param  fatherProxy  The new IsFatherProxy value
	 ***********************************************************/
	public synchronized void setIsFatherProxy(boolean fatherProxy)
	{

		isFatherProxy = fatherProxy;

	}


	/************************************************************
	 *  Sets the FatherProxyHost attribute of the Config object
	 *
	 *@param  host  The new FatherProxyHost value
	 ***********************************************************/
	public synchronized void setFatherProxyHost(String host)
	{

		fatherProxyHost = host;

	}


	/************************************************************
	 *  Sets the FatherProxyPort attribute of the Config object
	 *
	 *@param  port  The new FatherProxyPort value
	 ***********************************************************/
	public synchronized void setFatherProxyPort(int port)
	{

		fatherProxyPort = port;

	}


	/************************************************************
	 *  Sets the DeniedHosts attribute of the Config object
	 *
	 *@param  hosts  The new DeniedHosts value
	 ***********************************************************/
	public synchronized void setDeniedHosts(String[] hosts)
	{

		deniedHosts = hosts;

	}


	/************************************************************
	 *  Sets the Password attribute of the Config object
	 *
	 *@param  newPassword  The new Password value
	 ***********************************************************/
	public synchronized void setPassword(String newPassword)
	{

		password = newPassword;

	}


	/************************************************************
	 *  Sets the CleanCache attribute of the Config object
	 *
	 *@param  clean  The new CleanCache value
	 ***********************************************************/
	public synchronized void setCleanCache(boolean clean)
	{

		cleanCache = clean;

	}


	/************************************************************
	 *  Sets the CacheMasks attribute of the Config object
	 *
	 *@param  masks  The new CacheMasks value
	 ***********************************************************/
	public synchronized void setCacheMasks(String[] masks)
	{

		cacheMasks = masks;

	}


	/************************************************************
	 *  Sets the FilesCached attribute of the Config object
	 *
	 *@param  number  The new FilesCached value
	 ***********************************************************/
	public synchronized void setFilesCached(long number)
	{

		filesCached = number;

	}


	/************************************************************
	 *  Sets the BytesCached attribute of the Config object
	 *
	 *@param  number  The new BytesCached value
	 ***********************************************************/
	public synchronized void setBytesCached(long number)
	{

		bytesCached = number;

	}


	/************************************************************
	 *  Sets the Hits attribute of the Config object
	 *
	 *@param  number  The new Hits value
	 ***********************************************************/
	public synchronized void setHits(long number)
	{

		hits = number;

	}


	/************************************************************
	 *  Sets the Misses attribute of the Config object
	 *
	 *@param  number  The new Misses value
	 ***********************************************************/
	public synchronized void setMisses(long number)
	{

		misses = number;

	}


	/************************************************************
	 *  Gets the JmxScriptDir attribute of the Config object
	 *
	 *@return    The JmxScriptDir value
	 ***********************************************************/
	public String getJmxScriptDir()
	{

		return jmxScriptDir;
	}


	/************************************************************
	 *  Gets the ProxyMachineNameAndPort attribute of the Config object
	 *
	 *@return    The ProxyMachineNameAndPort value
	 ***********************************************************/
	public String getProxyMachineNameAndPort()
	{

		return proxyMachineNameAndPort;
	}


	/************************************************************
	 *  Gets the AdminPort attribute of the Config object
	 *
	 *@return    The AdminPort value
	 ***********************************************************/
	public int getAdminPort()
	{

		return adminPort;
	}


	/************************************************************
	 *  Gets the AdminPath attribute of the Config object
	 *
	 *@return    The AdminPath value
	 ***********************************************************/
	public String getAdminPath()
	{

		return adminPath;
	}


	/************************************************************
	 *  Gets the LocalHost attribute of the Config object
	 *
	 *@return    The LocalHost value
	 ***********************************************************/
	public String getLocalHost()
	{

		return localHost;
	}


	/************************************************************
	 *  Gets the LocalIP attribute of the Config object
	 *
	 *@return    The LocalIP value
	 ***********************************************************/
	public String getLocalIP()
	{

		return localIP;
	}


	/************************************************************
	 *  Gets the CacheSize attribute of the Config object
	 *
	 *@return    The CacheSize value
	 ***********************************************************/
	public synchronized long getCacheSize()
	{

		return cacheSize;
	}


	/************************************************************
	 *  Gets the IsFatherProxy attribute of the Config object
	 *
	 *@return    The IsFatherProxy value
	 ***********************************************************/
	public boolean getIsFatherProxy()
	{

		return isFatherProxy;
	}


	/************************************************************
	 *  Gets the FatherProxyHost attribute of the Config object
	 *
	 *@return    The FatherProxyHost value
	 ***********************************************************/
	public String getFatherProxyHost()
	{

		return fatherProxyHost;
	}


	/************************************************************
	 *  Gets the FatherProxyPort attribute of the Config object
	 *
	 *@return    The FatherProxyPort value
	 ***********************************************************/
	public int getFatherProxyPort()
	{

		return fatherProxyPort;
	}


	/************************************************************
	 *  Gets the DeniedHosts attribute of the Config object
	 *
	 *@return    The DeniedHosts value
	 ***********************************************************/
	public String[] getDeniedHosts()
	{

		return deniedHosts;
	}


	/************************************************************
	 *  Gets the Password attribute of the Config object
	 *
	 *@return    The Password value
	 ***********************************************************/
	public String getPassword()
	{

		return password;
	}


	/************************************************************
	 *  Gets the CleanCache attribute of the Config object
	 *
	 *@return    The CleanCache value
	 ***********************************************************/
	public boolean getCleanCache()
	{

		return cleanCache;
	}


	/************************************************************
	 *  Gets the CacheMasks attribute of the Config object
	 *
	 *@return    The CacheMasks value
	 ***********************************************************/
	public String[] getCacheMasks()
	{

		return cacheMasks;
	}


	/************************************************************
	 *  Gets the FilesCached attribute of the Config object
	 *
	 *@return    The FilesCached value
	 ***********************************************************/
	public long getFilesCached()
	{

		return filesCached;
	}


	/************************************************************
	 *  Gets the BytesCached attribute of the Config object
	 *
	 *@return    The BytesCached value
	 ***********************************************************/
	public long getBytesCached()
	{

		return bytesCached;
	}


	/************************************************************
	 *  Gets the BytesFree attribute of the Config object
	 *
	 *@return    The BytesFree value
	 ***********************************************************/
	public long getBytesFree()
	{

		return cacheSize - bytesCached;
	}


	/************************************************************
	 *  Gets the Hits attribute of the Config object
	 *
	 *@return    The Hits value
	 ***********************************************************/
	public long getHits()
	{

		return hits;
	}


	/************************************************************
	 *  Gets the Misses attribute of the Config object
	 *
	 *@return    The Misses value
	 ***********************************************************/
	public long getMisses()
	{

		return misses;
	}


	/************************************************************
	 *  Gets the HitRatio attribute of the Config object
	 *
	 *@return    The HitRatio value
	 ***********************************************************/
	public double getHitRatio()
	{

		if((hits + misses) == 0)
		{

			return 0;
		}

		else
		{

			return 100 * hits / (hits + misses);
		}

	}


	//

	// Re-initialize

	//

	/************************************************************
	 *  Description of the Method
	 ***********************************************************/
	public void reset()
	{

		isFatherProxy = false;

		fatherProxyHost = "wwwproxy.ac.il";

		fatherProxyPort = defaultProxyPort;

		password = defaultPassword;

		isCaching = true;

		cacheSize = defaultCacheSize;

		cleanCache = false;


		deniedHosts = new String[0];

		cacheMasks = new String[0];

	}


	/************************************************************
	 *  Description of the Method
	 ***********************************************************/
	public synchronized void increaseFilesCached()
	{

		filesCached++;

	}


	/************************************************************
	 *  Description of the Method
	 ***********************************************************/
	public synchronized void decreaseFilesCached()
	{

		filesCached--;

	}


	/************************************************************
	 *  Description of the Method
	 ***********************************************************/
	public synchronized void increaseHits()
	{

		hits++;

	}


	/************************************************************
	 *  Description of the Method
	 ***********************************************************/
	public synchronized void increaseMisses()
	{

		misses++;

	}



	//

	// Construct a string with all parameters

	//

	/************************************************************
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 ***********************************************************/
	public synchronized String toString()
	{

		int i;

		String s = "";

		s += isFatherProxy + separator;

		s += fatherProxyHost.equals("") ? "NULL" : fatherProxyHost;

		s += separator +
				fatherProxyPort + separator;


		s += deniedHosts.length + separator;

		for(i = 0; i < deniedHosts.length; i++)
		{

			s += deniedHosts[i] + separator;
		}


		s +=
				password + separator +
				isCaching + separator +
				cacheSize + separator +
				cleanCache + separator;


		s += cacheMasks.length + separator;

		for(i = 0; i < cacheMasks.length; i++)
		{

			s += cacheMasks[i] + separator;
		}


		s += proxyMachineNameAndPort + separator;


		s +=
				filesCached + separator +
				bytesCached + separator +
				bytesFree + separator +
				hits + separator +
				misses + separator +
				"\n";

		return s;
	}


	//

	// Set parameters according to a string (that was sent by applet)

	//

	/************************************************************
	 *  Description of the Method
	 *
	 *@param  config  Description of Parameter
	 ***********************************************************/
	public synchronized void parse(String config)
	{

		log.info("Parsing administrator request...");

		int size;

		int i;

		StringTokenizer s = new StringTokenizer(config, separator);


		isFatherProxy = s.nextToken().equals("true");

		log.info("Use father proxy = " + isFatherProxy);

		fatherProxyHost = s.nextToken();

		if(fatherProxyHost.equals("NULL"))
		{

			fatherProxyHost = "";
		}

		log.info("Father proxy name = " + fatherProxyHost);


		fatherProxyPort = Integer.parseInt(s.nextToken());

		log.info("Father proxy port = " + fatherProxyPort);


		size = Integer.parseInt(s.nextToken());

		deniedHosts = new String[size];

		for(i = 0; i < size; i++)
		{

			deniedHosts[i] = s.nextToken();

			log.info("Deny access to " + deniedHosts[i]);

		}


		password = s.nextToken();

		log.info("password = " + password);

		isCaching = s.nextToken().equals("true");

		log.info("Caching = " + isCaching);

		cacheSize = Long.parseLong(s.nextToken());

		log.info("Cache size = " + cacheSize);

		cleanCache = s.nextToken().equals("true");

		log.info("Do cache clean up = " + cleanCache);


		size = Integer.parseInt(s.nextToken());

		cacheMasks = new String[size];

		for(i = 0; i < size; i++)
		{

			cacheMasks[i] = s.nextToken();

			log.info("Don't cache " + cacheMasks[i]);

		}


		proxyMachineNameAndPort = s.nextToken();


		if(isAppletContext)
		{

			filesCached = Long.parseLong(s.nextToken());

			bytesCached = Long.parseLong(s.nextToken());

			bytesFree = Long.parseLong(s.nextToken());

			hits = Long.parseLong(s.nextToken());

			misses = Long.parseLong(s.nextToken());

		}


		//

		// Update bytesFree to reflect the change in cache size.

		// Note that free bytes can be below min free level now.

		//

		bytesFree = cacheSize - bytesCached;

	}


	boolean getIsCaching()
	{

		return isCaching;
	}

}


