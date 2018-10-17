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

package org.apache.jmeter.test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface ResourceLocator {

    public static String getResource(Object instance, String path) {
        return getResource(instance.getClass(), path);
    }

    public static String getResource(Class<?> basetype, String path) {
        Path nioPath = getResourcePath(basetype, path);
        return nioPath.toString();
    }

    public static Path getResourcePath(Object instance, String path) {
        return getResourcePath(instance.getClass(), path);
    }

    public static Path getResourcePath(Class<?> basetype, String path) {
        URL url = basetype.getResource(path);
        if (url == null) {
            return null;
        }
        Path nioPath;
        try {
            nioPath = Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        return nioPath;
    }
}
