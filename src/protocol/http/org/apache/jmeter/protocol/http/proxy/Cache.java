package org.apache.jmeter.protocol.http.proxy;
/******************************************************************
*** File Cache.java
***
***/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

//
// Class:     Cache
// Abstract:  manages all caching activities.
//

public class Cache
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");
	//
	// Members variables
	//

	String basePath = null;
	long MinFreeSpace;// in bytes
	Hashtable htable;
	Config config;


	//
	// Public methods
	//

	//
	// Constructor
	//
	public Cache(Config configObject)
	{
		//
		// Initialize variables
		//
		config = configObject;
		MinFreeSpace = 15000;
		htable = new Hashtable();

		//
		// Create directory for caching
		//

		File cacheDir = new File("Cache");
		cacheDir.mkdirs();
		basePath = cacheDir.getAbsolutePath();

		//
		// Delete all files in cache directory
		//
		int i;
		File file = new File(basePath);;
		String filename;

		// Get list of files in cache direcotry
		String files[] = file.list();

		// Delete each file found
		for (i=0; i<files.length; i++)
		{
			file = new File(basePath + File.separatorChar + files[i]);
			file.delete();
		}
		config.setFilesCached(0);
		config.setBytesCached(0);
		config.setHits(0);
		config.setMisses(0);
	}


	//
	// isCachable - check if URL reply should be cached
	//
	public boolean IsCachable(String rawUrl)
	{
		return (getFileName(rawUrl) != null);
	}


	//
	// IsCached - Check if we have in cache what the client wants.
	//
	public boolean IsCached(String rawUrl)
	{
		// Generate filename from URL
		String filename = getFileName(rawUrl);
		if (filename == null)
			return false;

		// Search in hash table
		if (htable.get(filename) != null)
			return true;

		return false;
	}


	//
	// getFileInputStream - When this method is called, it means a cache hit.
	//   We update the date field in the hash table entry and return a
	//   FileInputStream object corresponding to the file caching the info.
	//
	 public FileInputStream getFileInputStream(String rawUrl)
	{
		FileInputStream in = null;
		try
		{
			 String filename = getFileName(rawUrl);

			// Update the hash table entry with current date as value
			htable.put(filename,new Date());

			in = new FileInputStream(filename);
		}
		catch (FileNotFoundException fnf)
		{
			try

			{
				log.warn("File Not Found:"+getFileName(rawUrl),fnf);
			}
			catch (Exception e)
			{}
		}
		finally
		{
			return in;
		}
	}


	//
	// getFileoutputStream - When this method is called, it means we're about
	//   to cache a new object. We generate a file name, and return
	//   a corresponding FileOutputStream object.
	//
	 public FileOutputStream getFileOutputStream(String rawUrl)
	{
		FileOutputStream out = null;
		String filename;
		try
		{
			 filename = getFileName(rawUrl);

			out = new FileOutputStream(filename);
		}
		catch (IOException e)
		{}
		finally
		{
			return out;
		}
	}


	//
	// Decrement Cache Free Space (In Bytes)
	//
	public synchronized void DecrementFreeSpace(int nbytes, String rawUrl)
	{
		config.setBytesCached(config.getBytesCached() + nbytes);
		if (config.getBytesFree() <= MinFreeSpace)
			MakeFreeSpace(rawUrl);
	}


	//
	// Add new entry to hash table
	//
	public synchronized void AddToTable(String rawUrl)
	{
		String filename = getFileName(rawUrl);

		// Add filename to hash table with the current date as its value
		htable.put(filename,new Date());
		config.increaseFilesCached();
	}

	//
	// clean - delete the cached files
	//
	 public synchronized void clean()
	{
		log.info("Cleaning the cache...");

		// Enumerate the hash table
		for (Enumeration keys = htable.keys(); keys.hasMoreElements() ;)
		{
			String filename = (String)keys.nextElement();
			File file = new File(filename);
			long nbytes = file.length();
			boolean result = file.delete();
			if (result == true)
			{
				// Delete entry in hash table
				htable.remove(filename);
				config.decreaseFilesCached();

				// Increment free space
				config.setBytesCached(config.getBytesCached() - nbytes);
			}
			else
			{
				// Another thread holds this file open for writing
			}
		}
		config.setHits(0);
		config.setMisses(0);
		log.info("Cache is clean.");
	}


	//
	// Private methods
	//

	//
	// MakeFreeSpace - throw LRU file until free space is above min level
	//
	private synchronized void MakeFreeSpace(String rawUrl)
	{
		String filename,
				LRUfilename;
		Date   date,
				minDate;

		minDate = new Date();
		while (config.getBytesFree() < MinFreeSpace)
		{
			filename = LRUfilename = null;
			date = null;

			if (htable.isEmpty())
			{
				log.info("Could not make free space: Hash table empty...");
				return;
			}

			//
			// Enumerate the hash table entries to find the LRU file
			//
			for (Enumeration keys = htable.keys(); keys.hasMoreElements() ;)
			{
				filename = (String)keys.nextElement();
				date = (Date)htable.get(filename);
				if (date.before(minDate))
					LRUfilename = filename;
			}

			//
			// Delete the LRU file
			//
			File LRUfile = new File(LRUfilename);
			long nbytes = LRUfile.length();
			boolean result = LRUfile.delete();
			if (result == true)
			{
				// Delete entry in hash table
				htable.remove(LRUfilename);
				config.decreaseFilesCached();

				// Increment free space
				config.setBytesCached(config.getBytesCached() - nbytes);
			}
			else
			{
				// Another thread holds this file open for writing
				log.info("File "+LRUfilename+" could not be deleted...");
				return;
			}
		}
	}


	//
	// Convert the URL to filename - this method parses the URL and
	//   generate filename only if the URL is to be cached.
	//   We do not cache URLs containing '?', "cgi-bin" and
	//   a list of not-to-cached-URLs as instructed by the proxy administrator.
	//
	private String getFileName(String rawUrl)
	{
		String filename = basePath + File.separatorChar + rawUrl.substring(7).replace('/','@');

		if (filename.indexOf('?') != -1 || filename.indexOf("cgi-bin") != -1)
		{
			return null;
		}

		return filename;
	}
}

