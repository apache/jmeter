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

package org.apache.jmeter.examples.testbeans.example3;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;

/**
 * This TestBean is just an example of the use of different TestBean types.
 */
public class Example3 extends AbstractSampler implements TestBean {

    private static final long serialVersionUID = 240L;

    private boolean mybool;
    private Boolean myBoolean1, myBoolean2;
    private int myInt;
    private Integer myInteger1, myInteger2;
    private long mylong;
    private Long myLong1, myLong2;
    private String myString1, myString2;
    private File myFile1;
    private String myFile2;

    @Override
    public SampleResult sample(Entry ignored) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.sampleStart();
        StringBuilder bld = new StringBuilder();
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                String name = field.getName();
                if (name.startsWith("my")) {
                    Object value = field.get(this);
                    bld.append(name).append('=');
                    bld.append(value);
                    bld.append(" (");
                    bld.append(field.getType().getCanonicalName());
                    bld.append(")\n");
                }
            } catch (IllegalAccessException e) {
                bld.append(e.toString());
            }
        }
        res.setResponseData(bld.toString(), null);
        res.setDataType(SampleResult.TEXT);
        res.sampleEnd();
        res.setSuccessful(true);
        return res;
    }

    public boolean isMybool() {
        return mybool;
    }
    public void setMybool(boolean mybool) {
        this.mybool = mybool;
    }
    public Boolean getMyBoolean1() {
        return myBoolean1;
    }
    public void setMyBoolean1(Boolean myBoolean1) {
        this.myBoolean1 = myBoolean1;
    }
    public Boolean getMyBoolean2() {
        return myBoolean2;
    }
    public void setMyBoolean2(Boolean myBoolean2) {
        this.myBoolean2 = myBoolean2;
    }
    public int getMyInt() {
        return myInt;
    }
    public void setMyInt(int myInt) {
        this.myInt = myInt;
    }
    public Integer getMyInteger1() {
        return myInteger1;
    }
    public void setMyInteger1(Integer myInteger1) {
        this.myInteger1 = myInteger1;
    }
    public Integer getMyInteger2() {
        return myInteger2;
    }
    public void setMyInteger2(Integer myInteger2) {
        this.myInteger2 = myInteger2;
    }
    public long getMylong() {
        return mylong;
    }
    public void setMylong(long mylong) {
        this.mylong = mylong;
    }
    public Long getMyLong1() {
        return myLong1;
    }
    public void setMyLong1(Long myLong1) {
        this.myLong1 = myLong1;
    }
    public Long getMyLong2() {
        return myLong2;
    }
    public void setMyLong2(Long myLong2) {
        this.myLong2 = myLong2;
    }
    public String getMyString1() {
        return myString1;
    }
    public void setMyString1(String myString1) {
        this.myString1 = myString1;
    }
    public String getMyString2() {
        return myString2;
    }
    public void setMyString2(String myString2) {
        this.myString2 = myString2;
    }

    public File getMyFile1() {
        return myFile1;
    }

    public void setMyFile1(File myFile) {
        this.myFile1 = myFile;
    }

    public String getMyFile2() {
        return myFile2;
    }

    public void setMyFile2(String myFile) {
        this.myFile2 = myFile;
    }

}
