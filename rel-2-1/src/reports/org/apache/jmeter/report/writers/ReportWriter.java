//$Header$
/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.jmeter.report.writers;

import org.apache.jmeter.testelement.TestElement;

/**
 * @author Peter Lin
 *
 * ReportWriter defines the basic operations of a report writer. A report
 * plan may have multiple report writers. it might be nice to have a pdf
 * writer in the future.
 */
public interface ReportWriter {
    ReportSummary writeReport(TestElement element);
    String getTargetDirectory();
    void setTargetDirectory(String directory);
}
