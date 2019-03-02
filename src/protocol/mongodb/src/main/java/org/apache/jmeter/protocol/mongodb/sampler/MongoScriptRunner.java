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

package org.apache.jmeter.protocol.mongodb.sampler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;

/**
 */
public class MongoScriptRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoScriptRunner.class);

    public MongoScriptRunner() {
        super();
    }

    /**
     * Evaluate a script on the database
     *
     * @param db
     *            database connection to use
     * @param script
     *            script to evaluate on the database
     * @return result of evaluation on the database
     * @throws Exception
     *             when evaluation on the database fails
     */
    public Object evaluate(DB db, String script)
        throws Exception {

        if(log.isDebugEnabled()) {
            log.debug("database: " + db.getName()+", script: " + script);
        }

        db.requestStart();
        try {
            db.requestEnsureConnection();

            Object result = db.eval(script);

            if(log.isDebugEnabled()) {
                log.debug("Result : " + result);
            }
            return result;
        } finally {
            db.requestDone();
        }
    }
}
