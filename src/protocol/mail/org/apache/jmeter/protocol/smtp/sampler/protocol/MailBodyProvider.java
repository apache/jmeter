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


package org.apache.jmeter.protocol.smtp.sampler.protocol;

/**
 * Class to build and store message body. Does nothing more than handle the (text-)data to be included in the message body.
 */
public class MailBodyProvider {

    private String mailBody;

    /**
     * Standard-Constructor
     */
    public MailBodyProvider() {
        mailBody = ">>>This is just a blind text<<<";
    }

    /**
     * Returns mail-body as a String
     * @return Mail-body
     */
    public String getMailBody() {
        return mailBody;
    }

    /**
     * Sets string to be used as mailbody
     * @param mailBodyText Text to be set as mailbody
     */
    public void setBody(String mailBodyText) {
        mailBody  = mailBodyText;
    }
}
