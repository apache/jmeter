/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.smtp.sampler.protocol;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SendMailCommandTest {

    private static final String NON_ASCII_FILE_NAME = "\u0442\u0435\u043a\u0441\u0442.txt"; // Russian for "text"

    @TempDir
    File tempDir;

    @Test
    void testNonAsciiAttachmentFileNameIsEncodedPerRfc2231() throws Exception {
        SendMailCommand sendMailCommand = createSendMailCommandWithAttachment(NON_ASCII_FILE_NAME);
        String rawMessage = writeMessageToString(sendMailCommand.prepareMessage());
        // The file name must be encoded according to RFC 2231, so that the
        // recipients see the original file name (see issue #6652)
        assertTrue(
                rawMessage.contains("filename*=UTF-8''%D1%82%D0%B5%D0%BA%D1%81%D1%82.txt"),
                "filename* parameter with RFC 2231 encoded file name expected in:\n" + rawMessage);
        // The mangled name must not appear anywhere
        assertFalse(rawMessage.contains("B5:AB"), "mangled file name found in:\n" + rawMessage);
    }

    @Test
    void testAsciiAttachmentFileNameIsNotEncoded() throws Exception {
        SendMailCommand sendMailCommand = createSendMailCommandWithAttachment("attachment.txt");
        String rawMessage = writeMessageToString(sendMailCommand.prepareMessage());
        assertTrue(
                rawMessage.contains("filename=attachment.txt"),
                "plain ASCII file name expected in:\n" + rawMessage);
        assertFalse(rawMessage.contains("filename*="), "unexpected RFC 2231 encoding in:\n" + rawMessage);
    }

    private SendMailCommand createSendMailCommandWithAttachment(String attachmentName) throws Exception {
        File attachment = new File(tempDir, attachmentName);
        Files.writeString(attachment.toPath(), "attachment content", StandardCharsets.UTF_8);

        SendMailCommand sendMailCommand = new SendMailCommand();
        sendMailCommand.setSmtpServer("localhost");
        sendMailCommand.setSmtpPort("25");
        sendMailCommand.setConnectionTimeOut("1000");
        sendMailCommand.setTimeOut("1000");
        sendMailCommand.setSender("from@example.com");
        sendMailCommand.setReceiverTo(Collections.singletonList(new InternetAddress("to@example.com")));
        sendMailCommand.setSubject("attachment file name test");
        sendMailCommand.setMailBody("body");
        sendMailCommand.addAttachment(attachment);
        return sendMailCommand;
    }

    private static String writeMessageToString(Message message) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            message.writeTo(outputStream);
        }
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
