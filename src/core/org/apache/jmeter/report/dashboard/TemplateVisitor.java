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
package org.apache.jmeter.report.dashboard;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FilenameUtils;
import org.apache.jmeter.report.core.DataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * The class TemplateVisitor visits files in a template directory to copy
 * regular files and process templated ones.
 *
 * @since 3.0
 */
public class TemplateVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateVisitor.class);
    public static final String TEMPLATED_FILE_EXT = "fmkr";

    private final Path source;
    private final Path target;
    private final Configuration configuration;
    private final DataContext data;

    /**
     * Instantiates a new template visitor.
     *
     * @param source
     *            the source directory
     * @param target
     *            the target directory
     * @param configuration
     *            the freemarker configuration
     * @param data
     *            the data to inject
     */
    public TemplateVisitor(Path source, Path target,
            Configuration configuration, DataContext data) {
        this.source = source;
        this.target = target;
        this.configuration = configuration;
        this.data = data;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object,
     * java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs)
            throws IOException {
        // Copy directory
        Path newDir = target.resolve(source.relativize(file));
        try {
            Files.copy(file, newDir);
        } catch (FileAlreadyExistsException ex) {
            LOGGER.info("Copying folder from '{}' to '{}', got message:{}, found non empty folder with following content {}, will be ignored",
                    file, newDir, newDir.toFile().listFiles());
        }
        return FileVisitResult.CONTINUE;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
     * java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {

        // Depending on file extension, copy or process file
        String extension = FilenameUtils.getExtension(file.toString());
        if (TEMPLATED_FILE_EXT.equalsIgnoreCase(extension)) {
            // Process template file
            String templatePath = source.relativize(file).toString();
            Template template = configuration.getTemplate(templatePath);
            Path newPath = target.resolve(FilenameUtils
                    .removeExtension(templatePath));
            try (FileOutputStream stream = new FileOutputStream(newPath.toString());
                    Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
                    BufferedWriter bufferedWriter = new BufferedWriter(writer)){
                template.process(data, bufferedWriter);
            } catch (TemplateException ex) {
                throw new IOException(ex);
            }

        } else {
            // Copy regular file
            Path newFile = target.resolve(source.relativize(file));
            Files.copy(file, newFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return FileVisitResult.CONTINUE;
    }
}
