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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.URLName;

public class MailFileFolder extends Folder {

    private static final String FILENAME_FORMAT = "%d.msg";
    private static final String FILENAME_REGEX = "\\d+\\.msg";
    private boolean isOpen;
    private final File folderPath;// Parent folder (or single message file)
    private final boolean isFile;
    private static final FilenameFilter FILENAME_FILTER = (dir, name) -> name.matches(FILENAME_REGEX);

    public MailFileFolder(Store store, String path) {
        super(store);
        String base = store.getURLName().getHost(); // == ServerName from mail sampler
        File parentFolder = new File(base);
        isFile = parentFolder.isFile();
        if (isFile){
            folderPath = new File(base);
        } else {
            folderPath = new File(base,path);
        }
    }

    public MailFileFolder(Store store, URLName path) {
        this(store, path.getFile());
    }

    @Override
    public void appendMessages(Message[] messages) throws MessagingException {
        throw new MessagingException("Not supported");
    }

    @Override
    public void close(boolean expunge) throws MessagingException {
        this.store.close();
        isOpen = false;
    }

    @Override
    public boolean create(int type) throws MessagingException {
        return false;
    }

    @Override
    public boolean delete(boolean recurse) throws MessagingException {
        return false;
    }

    @Override
    public boolean exists() throws MessagingException {
        return true;
    }

    @Override
    public Message[] expunge() throws MessagingException {
        return new Message[0];
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
        return this;
    }

    @Override
    public String getFullName() {
        return this.toString();
    }

    @Override
    public Message getMessage(int index) throws MessagingException {
        File f;
        if (isFile) {
            f = folderPath;
        } else {
            f = new File(folderPath,String.format(FILENAME_FORMAT, Integer.valueOf(index)));
        }
        try (InputStream fis = new FileInputStream(f);
                InputStream bis = new BufferedInputStream(fis)) {
            return new MailFileMessage(this, bis, index);
        } catch (IOException e) {
            throw new MessagingException(
                    "Cannot open folder: " + e.getMessage(), e);
        }
    }

    @Override
    public int getMessageCount() throws MessagingException {
        if (!isOpen) {
            return -1;
        }
        if (isFile) {
            return 1;
        }
        File[] listFiles = folderPath.listFiles(FILENAME_FILTER);
        return listFiles != null ? listFiles.length : 0;
    }

    @Override
    public String getName() {
        return this.toString();
    }

    @Override
    public Folder getParent() throws MessagingException {
        return null;
    }

    @Override
    public Flags getPermanentFlags() {
        return null;
    }

    @Override
    public char getSeparator() throws MessagingException {
        return '/';
    }

    @Override
    public int getType() throws MessagingException {
        return HOLDS_MESSAGES;
    }

    @Override
    public boolean hasNewMessages() throws MessagingException {
        return false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public Folder[] list(String pattern) throws MessagingException {
        return new Folder[]{this};
    }

    @Override
    public void open(int mode) throws MessagingException {
        if (mode != READ_ONLY) {
            throw new MessagingException("Implementation only supports read-only access");
        }
        this.mode = mode;
        isOpen = true;
    }

    @Override
    public boolean renameTo(Folder newName) throws MessagingException {
        return false;
    }

}
