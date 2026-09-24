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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SendMailCommandTest {

    // Russian for "text"
    private static final String NON_ASCII_WORD = "\u0442\u0435\u043a\u0441\u0442";
    private static final String NON_ASCII_FILE_NAME = NON_ASCII_WORD + ".txt";
    private static final String ENCODED_NON_ASCII_WORD = "%D1%82%D0%B5%D0%BA%D1%81%D1%82";

    @TempDir
    File tempDir;

    @Test
    void nonAsciiAttachmentFileNameIsEncodedAsUtf8() throws Exception {
        byte[] rawMessage = createMessageWithAttachment(NON_ASCII_FILE_NAME);
        String message = new String(rawMessage, StandardCharsets.UTF_8);
        // The file name is carried by both the filename parameter of Content-Disposition
        // and the name parameter of Content-Type, and both must be encoded as UTF-8
        // according to RFC 2231, so recipients see the original name on every platform
        assertTrue(
                message.contains("filename*=UTF-8''" + ENCODED_NON_ASCII_WORD + ".txt"),
                "RFC 2231 encoded filename parameter expected in:\n" + message);
        assertTrue(
                message.contains("name*=UTF-8''" + ENCODED_NON_ASCII_WORD + ".txt"),
                "RFC 2231 encoded name parameter expected in:\n" + message);
        // Parsing the message back must return the original name, no matter how
        // the name would have been mangled
        assertEquals(NON_ASCII_FILE_NAME, parseAttachmentFileName(rawMessage), "decoded attachment file name");
    }

    @Test
    void asciiAttachmentFileNameIsNotEncoded() throws Exception {
        byte[] rawMessage = createMessageWithAttachment("attachment.txt");
        String message = new String(rawMessage, StandardCharsets.UTF_8);
        assertTrue(
                message.contains("filename=attachment.txt"),
                "plain filename parameter expected in:\n" + message);
        assertTrue(
                message.contains("name=attachment.txt"),
                "plain name parameter expected in:\n" + message);
        assertFalse(message.contains("filename*="), "unexpected RFC 2231 encoding in:\n" + message);
        assertFalse(message.contains("name*="), "unexpected RFC 2231 encoding in:\n" + message);
        assertEquals("attachment.txt", parseAttachmentFileName(rawMessage), "decoded attachment file name");
    }

    @Test
    void longAsciiAttachmentFileNameIsSplitIntoContinuations() throws Exception {
        String fileName = "a".repeat(80) + ".txt";
        byte[] rawMessage = createMessageWithAttachment(fileName);
        String message = new String(rawMessage, StandardCharsets.UTF_8);
        // javax.mail splits parameters longer than 60 characters into
        // RFC 2231 continuations, and the parts must reassemble into the name
        assertTrue(message.contains("filename*0="), "continuation filename*0 expected in:\n" + message);
        assertTrue(message.contains("filename*1="), "continuation filename*1 expected in:\n" + message);
        assertTrue(message.contains("name*0="), "continuation name*0 expected in:\n" + message);
        assertEquals(fileName, parseAttachmentFileName(rawMessage), "decoded attachment file name");
    }

    @Test
    void longNonAsciiAttachmentFileNameIsEncodedAsSingleParameter() throws Exception {
        // Long in characters, yet within the 255 byte file name limit of common file systems
        String fileName = NON_ASCII_WORD.repeat(20) + ".txt";
        byte[] rawMessage = createMessageWithAttachment(fileName);
        String message = new String(rawMessage, StandardCharsets.UTF_8);
        // Unlike plain values, encoded values are never split, they stay a
        // single filename* parameter regardless of their length
        assertTrue(message.contains("filename*=UTF-8''"), "single encoded filename parameter expected in:\n" + message);
        assertFalse(message.contains("filename*0"), "unexpected continuation in:\n" + message);
        assertEquals(fileName, parseAttachmentFileName(rawMessage), "decoded attachment file name");
    }

    private byte[] createMessageWithAttachment(String attachmentName) throws Exception {
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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        sendMailCommand.prepareMessage().writeTo(outputStream);
        return outputStream.toByteArray();
    }

    private static String parseAttachmentFileName(byte[] rawMessage) throws Exception {
        MimeMessage parsed = new MimeMessage(null, new ByteArrayInputStream(rawMessage));
        Multipart multipart = (Multipart) parsed.getContent();
        BodyPart attachment = multipart.getBodyPart(1);
        return attachment.getFileName();
    }
}
