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
 */

package org.apache.jmeter.protocol.jms.sampler.render;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jorphan.io.TextFile;

import com.github.benmanes.caffeine.cache.Cache;

class TextMessageRenderer implements MessageRenderer<String> {

    @Override
    public String getValueFromText(String text) {
        return text;
    }

    @Override
    public String getValueFromFile(String filename, String encoding, boolean hasVariable, Cache<Object,Object> cache) {
        String text = (String) cache.get(new FileKey(filename, encoding), key -> getContent((FileKey)key));
        if (hasVariable) {
            text = new CompoundVariable(text).execute();
        }

        return text;
    }

    String getContent(FileKey key) {
        return new TextFile(key.getFilename(), key.getEncoding()).getText();
    }


}
