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

package org.apache.jmeter.protocol.ftp.config;

import java.io.Serializable;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.ftp.sampler.FTPSampler;

/**
 * @author Michael Stover
 * @version $Revision$ last updated $Date$
 */
public class FtpConfig extends ConfigTestElement implements Serializable
{

    public FtpConfig()
    {
    }

    public boolean isComplete()
    {
        if ((getServer() != null)
            && (getFilename() != null)
            && (!getServer().equals(""))
            && (!getFilename().equals("")))
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    public void setServer(String newServer)
    {
        this.setProperty(FTPSampler.SERVER, newServer);
    }
    public String getServer()
    {
        return getPropertyAsString(FTPSampler.SERVER);
    }
    public void setFilename(String newFilename)
    {
        this.setProperty(FTPSampler.FILENAME, newFilename);
    }
    public String getFilename()
    {
        return getPropertyAsString(FTPSampler.FILENAME);
    }

    /**
     * Returns a formatted string label describing this sampler
     * Example output:
     *      ftp://ftp.nowhere.com/pub/README.txt
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel()
    {
        return ("ftp://" + this.getServer() + "/" + this.getFilename());
    }
}
