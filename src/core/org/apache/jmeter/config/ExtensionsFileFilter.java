/*
 * Copyright(c) 2000 Soltima, Inc.
 * Soltima Wireless Publishing Platform (WPP)
 *
 * @author S.Coleman
 */
package org.apache.jmeter.config;

import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

/**
 * This FileFilter allows for a list of file extensions
 * to be set that it will filter on. This design was taken
 * from the ExampleFileFilter used in
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/filechooser.html">How
 * to Use File Choosers</a>, a section in <em>The Java Tutorial</em>.
 *
 * Here is an example of how to use the file filter for JMX files,
 * that have the .jmx extension.
 * <pre>
 *      ExtensionsFileFilter filter = new ExtensionsFileFilter();
 *      filter.addExtension("jmx");
 *      filter.setDescription("JMeter (*.jmx)");
 *      FileChooser fileChooser = new FileChooser();
 *      fileChooser.setFileFilter(fileFilter);
 * </pre>
 */
public class ExtensionsFileFilter extends FileFilter
{
    /**
     * A description of the extensions being filtered on.
     */
    private String mDescription = "";

    /**
     * The list of extensions being filtered on.
     */
    private ArrayList acceptableExtensions = new ArrayList(3);

    /**
     * Default Constructor.
     */
    public ExtensionsFileFilter()
    {
    }

    /**
     * Add an extension to the list of extensions to filter on.
     *
     * @param pExtension The extension to add.
     */
    public void addExtension(String pExtension)
    {
        acceptableExtensions.add(pExtension);
    }

    /**
     * Removes an extension from the list of extensions to filter on.
     *
     * @param pExtension The extension to remove.
     */
    public void removeExtension(String pExtension)
    {
        acceptableExtensions.remove(pExtension);
    }

    /**
     * Set the description of the extensions being filtered on.
     *
     * @param pDescription the detailed description of the extensions being
     *                     filtered on.
     */
    public void setDescription(String pDescription)
    {
        mDescription = pDescription;
    }

    /**
     * Returns a descriptive string detailing the file extensions being
     * filtered.
     *
     * @return The description of the extensions being filtered.
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * Determines whether to accept the passed File or not.
     *
     * @param pFile The file to check whether we should accept or not.
     * @return true if the file is accepted, false if not.
     */
    public boolean accept(File pFile)
    {
        // Always accept directories or the user will not be able to navigate
        // around the file system.
        if (pFile.isDirectory())
        {
            return true;
        }
        if (acceptableExtensions.contains(getExtension(pFile)) == true)
        {
            return true;
        }

        return false;
    }

    /**
     * Finds the file extension for the passed File. If there
     * is no extension then an empty string is returned.
     *
     * @param pFile The file to find the extension of.
     */
    private String getExtension(File pFile)
    {
        String name = pFile.getName();
        int index = name.lastIndexOf('.');
        if (index == -1 || index == name.length())
        {
            return "";
        }
        else
        {
            return name.substring(index + 1);
        }
    }
}

