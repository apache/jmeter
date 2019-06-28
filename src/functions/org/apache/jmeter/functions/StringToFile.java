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
package org.apache.jmeter.functions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileToString Function to read a complete file into a String.
 *
 * Parameters:
 * - file name
 * - append (true/false)
 * - file encoding (optional)
 *
 * Returns: true if ok , false if an error occured
 *
 * @since 5.2
 */
public class StringToFile extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(StringToFile.class);
    private static final List<String> desc = new LinkedList<>();
    private static final String KEY = "__StringToFile";//$NON-NLS-1$
    private static final ConcurrentHashMap<String, Lock> lockMap = new ConcurrentHashMap<>();
    static {
        desc.add(JMeterUtils.getResString("string_to_file_pathname"));
        desc.add(JMeterUtils.getResString("string_to_file_content"));//$NON-NLS-1$
        desc.add(JMeterUtils.getResString("string_to_file_way_to_write"));//$NON-NLS-1$
        desc.add(JMeterUtils.getResString("string_to_file_encoding"));//$NON-NLS-1$
    }
    private Object[] values;

    public StringToFile() {
        super();
    }

    /**
     * Write to file
     * @return boolean true if success , false otherwise
     * @throws IOException
     */
    private boolean writeToFile() throws IOException {
        String fileName = ((CompoundVariable) values[0]).execute().trim();
        String content = ((CompoundVariable) values[1]).execute();
        boolean append = true;
        if (values.length >= 3) {
            append = Boolean.parseBoolean(((CompoundVariable) values[2]).execute().toLowerCase().trim());
        }
        Charset charset = StandardCharsets.UTF_8;
        if (values.length == 4) {
            String charsetParamValue = ((CompoundVariable) values[3]).execute();
            if (StringUtils.isNotEmpty(charsetParamValue)) {
                charset = Charset.forName(charsetParamValue);
            }
        }

        if (fileName.isEmpty()) {
            log.error("File name '{}' is empty", fileName);
            return false;
        }
        log.debug("Writing {} to file {} with charset {} and append {}", content, fileName, charset, append);

        Lock localLock = new ReentrantLock();
        Lock lock = lockMap.putIfAbsent(fileName, localLock);
        try {
            if (lock == null) {
                localLock.lock();
            } else {
                lock.lock();
            }
            File file = new File(fileName);
            File fileParent = file.getParentFile();
            if (fileParent == null || (fileParent.exists() && fileParent.isDirectory() && fileParent.canWrite())) {
                FileUtils.writeStringToFile(file, content, charset, append);
            } else {
                log.error("The parent file of {} doesn't exist or is not writable", file);
                return false;
            }
        } finally {
            if (lock == null) {
                localLock.unlock();
            } else {
                lock.unlock();
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        boolean executionResult;
        try {
            executionResult = this.writeToFile();
        } catch (UnsupportedCharsetException ue) { // NOSONAR
            executionResult = false;
            log.error("The encoding of file is not supported", ue);
        } catch (IllegalCharsetNameException ie) { // NOSONAR
            executionResult = false;
            log.error("The encoding of file contains illegal characters", ie);
        } catch (IOException e) { // NOSONAR
            executionResult = false;
            log.error("IOException occurred", e);
        }
        return String.valueOf(executionResult);
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 2, 4);
        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }
}
