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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

/**
 * A sampler which understands FTP file requests.
 * 
 */
public class FTPSampler extends AbstractSampler {
	public final static String SERVER = "FTPSampler.server"; // $NON-NLS-1$

	// N.B. Originally there was only one filename, and only get(RETR) was supported
	// To maintain backwards compatibility, the property name needs to remain the same
	public final static String REMOTE_FILENAME = "FTPSampler.filename"; // $NON-NLS-1$
	
	public final static String LOCAL_FILENAME = "FTPSampler.localfilename"; // $NON-NLS-1$

	// Use binary mode file transfer?
	public final static String BINARY_MODE = "FTPSampler.binarymode"; // $NON-NLS-1$

	// Are we uploading?
	public final static String UPLOAD_FILE = "FTPSampler.upload"; // $NON-NLS-1$
	
	// Should the file data be saved in the response?
	public final static String SAVE_RESPONSE = "FTPSampler.saveresponse"; // $NON-NLS-1$

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

	public String getRemoteFilename() {
		return getPropertyAsString(REMOTE_FILENAME);
	}

	public String getLocalFilename() {
		return getPropertyAsString(LOCAL_FILENAME);
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
		return ("ftp://" + getServer() + "/" + getRemoteFilename() // $NON-NLS-1$ $NON-NLS-2$
				+ (isBinaryMode() ? " (Binary) " : " (Ascii) ")    // $NON-NLS-1$ $NON-NLS-2$
				+ (isUpload() ? " <- " : " -> ")                   // $NON-NLS-1$ $NON-NLS-2$
				+ getLocalFilename());
	}

	public SampleResult sample(Entry e) {
		SampleResult res = new SampleResult();
		res.setSuccessful(false);
		String remote = getRemoteFilename();
		String local = getLocalFilename();
		boolean binaryTransfer = isBinaryMode();
		res.setSampleLabel(getName());
        res.setSamplerData(getLabel());
        InputStream input = null;
        OutputStream output = null;

        res.sampleStart();
        FTPClient ftp = new FTPClient();
		try {
			ftp.connect(getServer());
			int reply = ftp.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply))
            {
	            if (ftp.login( getUsername(), getPassword())){
	                if (binaryTransfer) {
	                    ftp.setFileType(FTP.BINARY_FILE_TYPE);
	                }
					ftp.enterLocalPassiveMode();// should probably come from the setup dialog
					boolean ftpOK=false;
		            if (isUpload()) {
		            	File infile = new File(local);
		                input = new FileInputStream(infile);
		                ftpOK = ftp.storeFile(remote, input);		                
		                res.setBytes((int)infile.length());
		            } else {
		                final boolean saveResponse = isSaveResponse();
		            	ByteArrayOutputStream baos=null; // No need to close this
		            	OutputStream target=null; // No need to close this
		            	if (saveResponse){
		            		baos  = new ByteArrayOutputStream();
		            		target=baos;
		            	}
		            	if (local.length()>0){
		            		output=new FileOutputStream(local);
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
		                long bytes = IOUtils.copy(input,target);
		                ftpOK = bytes > 0;
						if (saveResponse){
							res.setResponseData(baos.toByteArray());
							if (!binaryTransfer) {
							    res.setDataType(SampleResult.TEXT);
							}
		                } else {
		                	res.setBytes((int) bytes);
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
            	//res.setResponseCode(Integer.toString(ftp.getReplyCode()));
            	res.setResponseMessage(ftp.getReplyString());
            }
		} catch (IOException ex) {
        	res.setResponseCode("000"); // TODO
            res.setResponseMessage(ex.toString());
        } finally {
            if (ftp != null && ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ignored) {
                }
            }
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }

		res.sampleEnd();
		return res;
	}
}
