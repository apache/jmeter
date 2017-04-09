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

package org.apache.jmeter.protocol.mongodb.mongo;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 */
public class EvalResultHandler {

    //This can lead to code smell, meh! Do we care
    public String handle(Object o) {
        if(o == null) {
            return "ok";
        }

        if(o instanceof Double) {
            return this.handle((Double)o);
        }
        else if(o instanceof Integer) {
            return this.handle((Integer)o);
        }
        else if(o instanceof String) {
            return this.handle((String)o);
        }
        else if(o instanceof DBObject) {
            return this.handle((DBObject)o);
        }
        else {
            return "return type not handled";
        }
    }

    public String handle(Integer o) {
        return o.toString();
    }

    public String handle(String o) {
        return o;
    }

    public String handle(Double o) {
        return o.toString();
    }


    public String handle(DBObject o) {
        return JSON.serialize(o);
    }
}
