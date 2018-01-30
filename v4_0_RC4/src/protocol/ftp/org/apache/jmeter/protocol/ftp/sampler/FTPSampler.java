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

package org.apache.jmeter.protocol.ftp.sampler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler which understands FTP file requests.
 *
 */
public class FTPSampler extends AbstractSampler implements Interruptible {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggerFactory.getLogger(FTPSampler.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList(
                    "org.apache.jmeter.config.gui.LoginConfigGui",
                    "org.apache.jmeter.protocol.ftp.config.gui.FtpConfigGui",
                    "org.apache.jmeter.config.gui.SimpleConfigGui"
            ));
    
    public static final String SERVER = "FTPSampler.server"; // $NON-NLS-1$

    public static final String PORT = "FTPSampler.port"; // $NON-NLS-1$

    // N.B. Originally there was only one filename, and only get(RETR) was supported
    // To maintain backwards compatibility, the property name needs to remain the same
    public static final String REMOTE_FILENAME = "FTPSampler.filename"; // $NON-NLS-1$

    public static final String LOCAL_FILENAME = "FTPSampler.localfilename"; // $NON-NLS-1$

    public static final String INPUT_DATA = "FTPSampler.inputdata"; // $NON-NLS-1$

    // Use binary mode file transfer?
    public static final String BINARY_MODE = "FTPSampler.binarymode"; // $NON-NLS-1$

    // Are we uploading?
    public static final String UPLOAD_FILE = "FTPSampler.upload"; // $NON-NLS-1$

    // Should the file data be saved in the response?
    public static final String SAVE_RESPONSE = "FTPSampler.saveresponse"; // $NON-NLS-1$

    private transient volatile FTPClient savedClient; // used for interrupting the sampler

    public FTPSampler() {
    }

    public String getUsername() {
        return getPropertyAsString(ConfigTestElement.USERNAME);
    }

    public String getPassword() {
        return getPropertyAsString(ConfigTestElement.PASSWORD);
    }

    public void setServer(String newServer) {
        this.setProperty(SERVER, newServer);
    }

    public String getServer() {
        return getPropertyAsString(SERVER);
    }

    public void setPort(String newPort) {
        this.setProperty(PORT, newPort, ""); // $NON-NLS-1$
    }

    public String getPort() {
        return getPropertyAsString(PORT, ""); // $NON-NLS-1$
    }

    public int getPortAsInt() {
        return getPropertyAsInt(PORT, 0);
    }

    public String getRemoteFilename() {
        return getPropertyAsString(REMOTE_FILENAME);
    }

    public String getLocalFilename() {
        return getPropertyAsString(LOCAL_FILENAME);
    }

    private String getLocalFileContents() {
        return getPropertyAsString(INPUT_DATA);
    }

    public boolean isBinaryMode(){
        return getPropertyAsBoolean(BINARY_MODE,false);
    }

    public boolean isSaveResponse(){
        return getPropertyAsBoolean(SAVE_RESPONSE,false);
    }

    public boolean isUpload(){
        return getPropertyAsBoolean(UPLOAD_FILE,false);
    }


    /**
     * Returns a formatted string label describing this sampler Example output:
     * ftp://ftp.nowhere.com/pub/README.txt
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel() {
        StringBuilder sb = new StringBuilder();
        sb.append("ftp://");// $NON-NLS-1$
        sb.append(getServer());
        String port = getPort();
        if (port.length() > 0){
            sb.append(':');
            sb.append(port);
        }
        sb.append("/");// $NON-NLS-1$
        sb.append(getRemoteFilename());
        sb.append(isBinaryMode() ? " (Binary) " : " (Ascii) ");// $NON-NLS-1$ $NON-NLS-2$
        sb.append(isUpload() ? " <- " : " -> "); // $NON-NLS-1$ $NON-NLS-2$
        sb.append(getLocalFilename());
        return sb.toString();
    }

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSuccessful(false); // Assume failure
        String remote = getRemoteFilename();
        String local = getLocalFilename();
        boolean binaryTransfer = isBinaryMode();
        res.setSampleLabel(getName());
        final String label = getLabel();
        res.setSamplerData(label);
        try {
            res.setURL(new URL(label));
        } catch (MalformedURLException e1) {
            log.warn("Cannot set URL: "+e1.getLocalizedMessage());
        }
        InputStream input = null;
        FileInputStream fileIS = null;
        res.sampleStart();
        FTPClient ftp = new FTPClient();
        try {
            savedClient = ftp;
            final int port = getPortAsInt();
            if (port > 0){
                ftp.connect(getServer(),port);
            } else {
                ftp.connect(getServer());
            }
            res.latencyEnd();
            int reply = ftp.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                if (ftp.login( getUsername(), getPassword())){
                    if (binaryTransfer) {
                        ftp.setFileType(FTP.BINARY_FILE_TYPE);
                    }
                    ftp.enterLocalPassiveMode();// should probably come from the setup dialog
                    boolean ftpOK=false;
                    if (isUpload()) {
                        String contents=getLocalFileContents();
                        if (contents.length() > 0){
                            byte[] bytes = contents.getBytes(); // TODO - charset?
                            input = new ByteArrayInputStream(bytes);
                            res.setBytes((long)bytes.length);
                        } else {
                            File infile = new File(local);
                            res.setBytes(infile.length());
                            fileIS = new FileInputStream(infile); // NOSONAR False positive, fileIS is closed in finally and not overwritten
                            input = new BufferedInputStream(fileIS);
                        }
                        ftpOK = ftp.storeFile(remote, input);
                    } else {
                        final boolean saveResponse = isSaveResponse();
                        ByteArrayOutputStream baos=null; // No need to close this
                        OutputStream target=null; 
                        OutputStream output = null;
                        try {
                            if (saveResponse){
                                baos  = new ByteArrayOutputStream();
                                target=baos;
                            }
                            if (local.length()>0){
                                output=new FileOutputStream(local); // NOSONAR False positive, the output is closed in finally and not overwritten
                                if (target==null) {
                                    target=output;
                                } else {
                                    target = new TeeOutputStream(output,baos);
                                }
                            }
                            if (target == null){
                                target=new NullOutputStream();
                            }
                            input = ftp.retrieveFileStream(remote);
                            if (input == null){// Could not access file or other error
                                res.setResponseCode(Integer.toString(ftp.getReplyCode()));
                                res.setResponseMessage(ftp.getReplyString());
                            } else {
                                long bytes = IOUtils.copy(input,target);
                                ftpOK = bytes > 0;
                                if (saveResponse) {
                                    saveResponse(res, binaryTransfer, baos);
                                } else {
                                    res.setBytes(bytes);
                                }
                            }
                        } finally {
                            IOUtils.closeQuietly(target);
                            IOUtils.closeQuietly(output);
                        }
                    }

                    if (ftpOK) {
                        res.setResponseCodeOK();
                        res.setResponseMessageOK();
                        res.setSuccessful(true);
                    } else {
                        res.setResponseCode(Integer.toString(ftp.getReplyCode()));
                        res.setResponseMessage(ftp.getReplyString());
                    }
                } else {
                    res.setResponseCode(Integer.toString(ftp.getReplyCode()));
                    res.setResponseMessage(ftp.getReplyString());
                }
            } else {
                res.setResponseCode("501"); // TODO
                res.setResponseMessage("Could not connect");
                res.setResponseMessage(ftp.getReplyString());
            }
        } catch (IOException ex) {
            res.setResponseCode("000"); // TODO
            res.setResponseMessage(ex.toString());
        } finally {
            savedClient = null;
            if (ftp.isConnected()) {
                try {
                    ftp.logout();
                } catch (IOException ignored) {
                }
                try {
                    ftp.disconnect();
                } catch (IOException ignored) {
                }
            }
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(fileIS);
        }

        res.sampleEnd();
        return res;
    }

    private void saveResponse(SampleResult res, boolean binaryTransfer, ByteArrayOutputStream baos) {
        res.setResponseData(baos.toByteArray());
        if (!binaryTransfer) {
            res.setDataType(SampleResult.TEXT);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean interrupt() {
        FTPClient client = savedClient;
        if (client != null) {
            savedClient = null;
            try {
                client.abort();
            } catch (IOException ignored) {
            }
            try {
                client.disconnect();
            } catch (IOException ignored) {
            }
        }
        return client != null;
    }
    
    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }
}
