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

package org.apache.jmeter.protocol.mail.sampler;

import java.io.File;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class MailFileStore extends Store {

    public MailFileStore(Session s, URLName u){
        super(s,u);
    }

    @Override
    protected boolean protocolConnect(String host, int port, String user, String password)
        throws MessagingException {
        File base = new File(host);
        if (base.isDirectory() || base.isFile()) {
            return true;
        }
        throw new MessagingException("Host must be a valid directory or file");
    }

    @Override
    public Folder getDefaultFolder() throws MessagingException {
        return new MailFileFolder(this,"");
    }

    @Override
    public Folder getFolder(String path) throws MessagingException {
        return new MailFileFolder(this, path);
    }

    @Override
    public Folder getFolder(URLName path) throws MessagingException {
        return new MailFileFolder(this, path);
    }
}
