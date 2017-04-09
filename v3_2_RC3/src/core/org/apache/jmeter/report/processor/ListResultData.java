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
package org.apache.jmeter.report.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The class ListResultData provides a list of results from sample processing.
 * 
 * @since 3.0
 */
public class ListResultData implements ResultData, Iterable<ResultData> {

    /** The items. */
    private List<ResultData> items = new ArrayList<>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.ResultData#accept(org.apache.jmeter
     * .report.processor.ResultDataVisitor)
     */
    @Override
    public <T> T accept(ResultDataVisitor<T> visitor) {
        return visitor.visitListResult(this);
    }

    /**
     * Adds the result at the end of the list.
     *
     * @param result
     *            the result
     * @return true, if the result is added
     */
    public boolean addResult(ResultData result) {
        return items.add(result);
    }

    /**
     * Removes the result at the specified index.
     *
     * @param index
     *            the index of the result in the list
     * @return the removed result data
     */
    public ResultData removeResult(int index) {
        return items.remove(index);
    }

    /**
     * Gets the stored item at the specified index.
     *
     * @param index
     *            the index
     * @return the result data
     */
    public ResultData get(int index) {
        return items.get(index);
    }

    /**
     * Gets the size of the list.
     *
     * @return the size of the list
     */
    public int getSize() {
        return items.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<ResultData> iterator() {
        return items.iterator();
    }
}
