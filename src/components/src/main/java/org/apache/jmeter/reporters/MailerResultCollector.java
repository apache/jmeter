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

package org.apache.jmeter.reporters;

import java.io.Serializable;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.testelement.property.TestElementProperty;


public class MailerResultCollector extends ResultCollector implements Serializable {
    private static final long serialVersionUID = 240L;

    public static final String MAILER_MODEL = "MailerResultCollector.mailer_model"; //$NON-NLS-1$

    public MailerResultCollector() {
        super();
        setProperty(new TestElementProperty(MAILER_MODEL, new MailerModel()));
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        super.clear();
        setProperty(new TestElementProperty(MAILER_MODEL, new MailerModel()));
    }

    /** {@inheritDoc} */
    @Override
    public void sampleOccurred(SampleEvent e) {
        super.sampleOccurred(e); // sends the result to the visualiser
        getMailerModel().add(e.getResult(), true); // updates the model used for sending e-mails
    }

    public MailerModel getMailerModel() {
        return (MailerModel) getProperty(MAILER_MODEL).getObjectValue();
    }
}
