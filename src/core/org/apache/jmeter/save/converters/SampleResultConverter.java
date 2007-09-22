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

package org.apache.jmeter.save.converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.util.Converter;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * XStream Converter for the SampleResult class
 */
public class SampleResultConverter extends AbstractCollectionConverter {
	//private static final Logger log = LoggingManager.getLoggerForClass();
 
    private static final String JAVA_LANG_STRING = "java.lang.String"; //$NON-NLS-1$
    private static final String ATT_CLASS = "class"; //$NON-NLS-1$

    // Element tags. Must be unique. Keep sorted.
    protected static final String TAG_COOKIES           = "cookies";          //$NON-NLS-1$
    protected static final String TAG_METHOD            = "method";           //$NON-NLS-1$
    protected static final String TAG_QUERY_STRING      = "queryString";      //$NON-NLS-1$
    protected static final String TAG_REDIRECT_LOCATION = "redirectLocation"; //$NON-NLS-1$
    protected static final String TAG_REQUEST_HEADER    = "requestHeader";    //$NON-NLS-1$

	//NOT USED protected   static final String TAG_URL               = "requestUrl";       //$NON-NLS-1$

    protected static final String TAG_RESPONSE_DATA     = "responseData";     //$NON-NLS-1$
    protected static final String TAG_RESPONSE_HEADER   = "responseHeader";   //$NON-NLS-1$
    protected static final String TAG_SAMPLER_DATA      = "samplerData";      //$NON-NLS-1$
    protected static final String TAG_RESPONSE_FILE     = "responseFile";     //$NON-NLS-1$

    // samplerData attributes. Must be unique. Keep sorted by string value.
    private static final String ATT_BYTES             = "by"; //$NON-NLS-1$
    private static final String ATT_DATA_ENCODING     = "de"; //$NON-NLS-1$
    private static final String ATT_DATA_TYPE         = "dt"; //$NON-NLS-1$
    private static final String ATT_LABEL             = "lb"; //$NON-NLS-1$
    private static final String ATT_LATENCY           = "lt"; //$NON-NLS-1$

    private static final String ATT_ALL_THRDS         = "na"; //$NON-NLS-1$
    private static final String ATT_GRP_THRDS         = "ng"; //$NON-NLS-1$

    // N.B. Originally the response code was saved with the code "rs" 
    // but retrieved with the code "rc". Changed to always use "rc", but
    // allow for "rs" when restoring values.
    private static final String ATT_RESPONSE_CODE     = "rc"; //$NON-NLS-1$
    private static final String ATT_RESPONSE_MESSAGE  = "rm"; //$NON-NLS-1$
    private static final String ATT_RESPONSE_CODE_OLD = "rs"; //$NON-NLS-1$
    
    private static final String ATT_SUCCESS           = "s";  //$NON-NLS-1$
    private static final String ATT_SAMPLE_COUNT      = "sc"; //$NON-NLS-1$
    private static final String ATT_TIME              = "t";  //$NON-NLS-1$
    private static final String ATT_TIME_STAMP        = "ts"; //$NON-NLS-1$
    private static final String ATT_THREADNAME        = "tn"; //$NON-NLS-1$

	/**
	 * Returns the converter version; used to check for possible
	 * incompatibilities
	 */
	public static String getVersion() {
		return "$Revision$"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
	 */
	public boolean canConvert(Class arg0) {
		return SampleResult.class.equals(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object,
	 *      com.thoughtworks.xstream.io.HierarchicalStreamWriter,
	 *      com.thoughtworks.xstream.converters.MarshallingContext)
	 */
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		SampleResult res = (SampleResult) obj;
		SampleSaveConfiguration save = res.getSaveConfig();
		setAttributes(writer, context, res, save);
		saveAssertions(writer, context, res, save);
		saveSubResults(writer, context, res, save);
		saveResponseHeaders(writer, context, res, save);
		saveRequestHeaders(writer, context, res, save);
		saveResponseData(writer, context, res, save);
		saveSamplerData(writer, context, res, save);
	}

	/**
	 * @param writer
	 * @param res
	 * @param save
	 */
	protected void saveSamplerData(HierarchicalStreamWriter writer, MarshallingContext context, SampleResult res,
			SampleSaveConfiguration save) {
		if (save.saveSamplerData(res)) {
			writeString(writer, TAG_SAMPLER_DATA, res.getSamplerData());
		}
        if (save.saveUrl()) {
            writeItem(res.getURL(), context, writer);
        }
	}

	/**
	 * @param writer
	 * @param res
	 * @param save
	 */
	protected void saveResponseData(HierarchicalStreamWriter writer, MarshallingContext context, SampleResult res,
			SampleSaveConfiguration save) {
		if (save.saveResponseData(res)) {
			writer.startNode(TAG_RESPONSE_DATA);
            writer.addAttribute(ATT_CLASS, JAVA_LANG_STRING);
			try {
                if (SampleResult.TEXT.equals(res.getDataType())){
    				writer.setValue(new String(res.getResponseData(), res.getDataEncoding()));
                }
                // Otherwise don't save anything - no point
			} catch (UnsupportedEncodingException e) {
				writer.setValue("Unsupported encoding in response data, can't record.");
			}
			writer.endNode();
		}
        if (save.saveFileName()){
            writer.startNode(TAG_RESPONSE_FILE);
            writer.addAttribute(ATT_CLASS, JAVA_LANG_STRING);
            writer.setValue(res.getResultFileName());
            writer.endNode();            
        }
	}

	/**
	 * @param writer
	 * @param res
	 * @param save
	 */
	protected void saveRequestHeaders(HierarchicalStreamWriter writer, MarshallingContext context, SampleResult res,
			SampleSaveConfiguration save) {
		if (save.saveRequestHeaders()) {
			writeString(writer, TAG_REQUEST_HEADER, res.getRequestHeaders());
		}
	}

	/**
	 * @param writer
	 * @param res
	 * @param save
	 */
	protected void saveResponseHeaders(HierarchicalStreamWriter writer, MarshallingContext context, SampleResult res,
			SampleSaveConfiguration save) {
		if (save.saveResponseHeaders()) {
			writeString(writer, TAG_RESPONSE_HEADER, res.getResponseHeaders());
		}
	}

	/**
	 * @param writer
	 * @param context
	 * @param res
	 * @param save
	 */
	protected void saveSubResults(HierarchicalStreamWriter writer, MarshallingContext context, SampleResult res,
			SampleSaveConfiguration save) {
		if (save.saveSubresults()) {
			SampleResult[] subResults = res.getSubResults();
			for (int i = 0; i < subResults.length; i++) {
				subResults[i].setSaveConfig(save);
				writeItem(subResults[i], context, writer);
			}
		}
	}

	/**
	 * @param writer
	 * @param context
	 * @param res
	 * @param save
	 */
	protected void saveAssertions(HierarchicalStreamWriter writer, MarshallingContext context, SampleResult res,
			SampleSaveConfiguration save) {
		if (save.saveAssertions()) {
			AssertionResult[] assertionResults = res.getAssertionResults();
			for (int i = 0; i < assertionResults.length; i++) {
				writeItem(assertionResults[i], context, writer);
			}
		}
	}

	/**
	 * @param writer
	 * @param res
	 * @param save
	 */
	protected void setAttributes(HierarchicalStreamWriter writer, MarshallingContext context, SampleResult res,
			SampleSaveConfiguration save) {
		if (save.saveTime())
			writer.addAttribute(ATT_TIME, Long.toString(res.getTime()));
		if (save.saveLatency())
			writer.addAttribute(ATT_LATENCY, Long.toString(res.getLatency()));
		if (save.saveTimestamp())
			writer.addAttribute(ATT_TIME_STAMP, Long.toString(res.getTimeStamp()));
		if (save.saveSuccess())
			writer.addAttribute(ATT_SUCCESS,
					Boolean.toString(res.isSuccessful()));
		if (save.saveLabel())
			writer.addAttribute(ATT_LABEL, ConversionHelp.encode(res.getSampleLabel()));
		if (save.saveCode())
			writer.addAttribute(ATT_RESPONSE_CODE, ConversionHelp.encode(res.getResponseCode()));
		if (save.saveMessage())
			writer.addAttribute(ATT_RESPONSE_MESSAGE, ConversionHelp.encode(res.getResponseMessage()));
		if (save.saveThreadName())
			writer.addAttribute(ATT_THREADNAME, ConversionHelp.encode(res.getThreadName()));
		if (save.saveDataType())
			writer.addAttribute(ATT_DATA_TYPE, ConversionHelp.encode(res.getDataType()));
		if (save.saveEncoding())
			writer.addAttribute(ATT_DATA_ENCODING, ConversionHelp.encode(res.getDataEncoding()));
		if (save.saveBytes())
			writer.addAttribute(ATT_BYTES, String.valueOf(res.getBytes()));
        if (save.saveSampleCount()){
        	writer.addAttribute(ATT_SAMPLE_COUNT, String.valueOf(res.getSampleCount()));
        }
        if (save.saveThreadCounts()){// These cannot be restored
        	org.apache.jmeter.threads.ThreadGroup 
        	threadGroup=JMeterContextService.getContext().getThreadGroup();
        	int numThreads =0;
        	if (threadGroup != null) { // can be null for remote testing
        	    numThreads = threadGroup.getNumberOfThreads();
        	}
            writer.addAttribute(ATT_GRP_THRDS,
                    String.valueOf(numThreads));
            writer.addAttribute(ATT_ALL_THRDS,
                    String.valueOf(JMeterContextService.getNumberOfThreads()));
        }
	}

	/**
	 * @param writer
	 * @param tag
	 * @param value
	 */
	protected void writeString(HierarchicalStreamWriter writer, String tag, String value) {
		if (value != null) {
			writer.startNode(tag);
			writer.addAttribute(ATT_CLASS, JAVA_LANG_STRING);
			writer.setValue(value);
			writer.endNode();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader,
	 *      com.thoughtworks.xstream.converters.UnmarshallingContext)
	 */
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		SampleResult res = (SampleResult) createCollection(context.getRequiredType());
		retrieveAttributes(reader, context, res);
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			Object subItem = readItem(reader, context, res);
			retrieveItem(reader, context, res, subItem);
			reader.moveUp();
		}

        // If we have a file, but no data, then read the file
        String resultFileName = res.getResultFileName();
        if (resultFileName.length()>0 
        &&  res.getResponseData().length == 0) {
            readFile(resultFileName,res);
        }
		return res;
	}

	/**
     * 
	 * @param reader
	 * @param context
	 * @param res
     * @return true if the item was processed (for HTTPResultConverter)
	 */
	protected boolean retrieveItem(HierarchicalStreamReader reader, UnmarshallingContext context, SampleResult res,
			Object subItem) {
		String nodeName = reader.getNodeName();
		if (subItem instanceof AssertionResult) {
			res.addAssertionResult((AssertionResult) subItem);
		} else if (subItem instanceof SampleResult) {
			res.storeSubResult((SampleResult) subItem);
		} else if (nodeName.equals(TAG_RESPONSE_HEADER)) {
			res.setResponseHeaders((String) subItem);
		} else if (nodeName.equals(TAG_REQUEST_HEADER)) {
			res.setRequestHeaders((String) subItem);
		} else if (nodeName.equals(TAG_RESPONSE_DATA)) {
			try {
				res.setResponseData(((String) subItem).getBytes(res.getDataEncoding()));
			} catch (UnsupportedEncodingException e) {
				res.setResponseData(("Can't support the char set: " + res.getDataEncoding()).getBytes());
			}
		} else if (nodeName.equals(TAG_SAMPLER_DATA)) {
			res.setSamplerData((String) subItem);
        } else if (nodeName.equals(TAG_RESPONSE_FILE)) {
            res.setResultFileName((String) subItem);
		// Don't try restoring the URL TODO: wy not?
		} else {
			return false;
		}
		return true;
	}

	/**
	 * @param reader
	 * @param res
	 */
	protected void retrieveAttributes(HierarchicalStreamReader reader, UnmarshallingContext context, SampleResult res) {
		res.setSampleLabel(ConversionHelp.decode(reader.getAttribute(ATT_LABEL)));
		res.setDataEncoding(ConversionHelp.decode(reader.getAttribute(ATT_DATA_ENCODING)));
		res.setDataType(ConversionHelp.decode(reader.getAttribute(ATT_DATA_TYPE)));
        String oldrc=reader.getAttribute(ATT_RESPONSE_CODE_OLD);
        if (oldrc!=null) {
            res.setResponseCode(ConversionHelp.decode(oldrc));
        } else {
            res.setResponseCode(ConversionHelp.decode(reader.getAttribute(ATT_RESPONSE_CODE)));
        }
		res.setResponseMessage(ConversionHelp.decode(reader.getAttribute(ATT_RESPONSE_MESSAGE)));
		res.setSuccessful(Converter.getBoolean(reader.getAttribute(ATT_SUCCESS), true));
		res.setThreadName(ConversionHelp.decode(reader.getAttribute(ATT_THREADNAME)));
		res.setStampAndTime(Converter.getLong(reader.getAttribute(ATT_TIME_STAMP)),
				Converter.getLong(reader.getAttribute(ATT_TIME)));
		res.setLatency(Converter.getLong(reader.getAttribute(ATT_LATENCY)));
		res.setBytes(Converter.getInt(reader.getAttribute(ATT_BYTES)));
		res.setSampleCount(Converter.getInt(reader.getAttribute(ATT_SAMPLE_COUNT),1)); // default is 1
        // ATT_GRP_THRDS and ATT_ALL_THRDS are write only
	}

    protected void readFile(String resultFileName, SampleResult res) {
        File in = null;
        FileInputStream fis = null;
        try {
            in = new File(resultFileName);
            fis = new FileInputStream(in);
            ByteArrayOutputStream outstream = new ByteArrayOutputStream(res.getBytes());
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                outstream.write(buffer, 0, len);
            }
            outstream.close();
            res.setResponseData(outstream.toByteArray());
        } catch (FileNotFoundException e) {
            //log.warn(e.getLocalizedMessage());
        } catch (IOException e) {
            //log.warn(e.getLocalizedMessage());
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
            }
        }
    }


	/**
	 * @param arg0
	 */
	public SampleResultConverter(Mapper arg0) {
		super(arg0);
	}
}