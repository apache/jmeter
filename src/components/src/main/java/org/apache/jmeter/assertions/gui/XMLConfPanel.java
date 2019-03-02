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

package org.apache.jmeter.assertions.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.assertions.XPathAssertion;
import org.apache.jmeter.extractor.XPathExtractor;
import org.apache.jmeter.util.JMeterUtils;

public class XMLConfPanel extends JPanel {
    private static final long serialVersionUID = 240L;

    private JCheckBox validate;
    private JCheckBox tolerant;
    private JCheckBox whitespace;
    private JCheckBox namespace;

    private JCheckBox quiet; // Should Tidy be quiet?

    private JCheckBox reportErrors; // Report Tidy errors as Assertion failure?

    private JCheckBox showWarnings; // Show Tidy warnings ?

    private JCheckBox downloadDTDs; // Should we download external DTDs?

    /**
     *
     */
    public XMLConfPanel() {
        super();
        init();
    }

    private void init() { // WARNING: called from ctor so must not be overridden
                          // (i.e. must be private or final)
        quiet = new JCheckBox(JMeterUtils.getResString("xpath_tidy_quiet"), true);//$NON-NLS-1$
        reportErrors = new JCheckBox(JMeterUtils.getResString("xpath_tidy_report_errors"), true);//$NON-NLS-1$
        showWarnings = new JCheckBox(JMeterUtils.getResString("xpath_tidy_show_warnings"), true);//$NON-NLS-1$
        namespace = new JCheckBox(JMeterUtils.getResString("xml_namespace_button")); //$NON-NLS-1$
        whitespace = new JCheckBox(JMeterUtils.getResString("xml_whitespace_button")); //$NON-NLS-1$
        validate = new JCheckBox(JMeterUtils.getResString("xml_validate_button")); //$NON-NLS-1$
        tolerant = new JCheckBox(JMeterUtils.getResString("xml_tolerant_button")); //$NON-NLS-1$
        tolerant.addActionListener(e -> tolerant());
        downloadDTDs = new JCheckBox(JMeterUtils.getResString("xml_download_dtds")); //$NON-NLS-1$
        Box tidyOptions = Box.createHorizontalBox();
        tidyOptions.setBorder(BorderFactory.createEtchedBorder());
        tidyOptions.add(tolerant);
        tidyOptions.add(quiet);
        tidyOptions.add(reportErrors);
        tidyOptions.add(showWarnings);

        Box untidyOptions = Box.createHorizontalBox();
        untidyOptions.setBorder(BorderFactory.createEtchedBorder());
        untidyOptions.add(namespace);
        untidyOptions.add(validate);
        untidyOptions.add(whitespace);
        untidyOptions.add(downloadDTDs);

        Box options = Box.createVerticalBox();
        options.add(tidyOptions);
        options.add(untidyOptions);
        add(options);
        setDefaultValues();
    }

    public void setDefaultValues() {
        whitespace.setSelected(false);
        validate.setSelected(false);
        tolerant.setSelected(false);
        namespace.setSelected(false);
        quiet.setSelected(true);
        reportErrors.setSelected(false);
        showWarnings.setSelected(false);
        downloadDTDs.setSelected(false);
        tolerant();
    }

    // Process tolerant settings
    private void tolerant() {
        final boolean isTolerant = tolerant.isSelected();
        // Non-Tidy options
        validate.setEnabled(!isTolerant);
        whitespace.setEnabled(!isTolerant);
        namespace.setEnabled(!isTolerant);
        downloadDTDs.setEnabled(!isTolerant);
        // Tidy options
        quiet.setEnabled(isTolerant);
        reportErrors.setEnabled(isTolerant);
        showWarnings.setEnabled(isTolerant);
    }

    // Called by XPathAssertionGui
    public void modifyTestElement(XPathAssertion assertion) {
        assertion.setValidating(validate.isSelected());
        assertion.setWhitespace(whitespace.isSelected());
        assertion.setTolerant(tolerant.isSelected());
        assertion.setNamespace(namespace.isSelected());
        assertion.setShowWarnings(showWarnings.isSelected());
        assertion.setReportErrors(reportErrors.isSelected());
        assertion.setQuiet(quiet.isSelected());
        assertion.setDownloadDTDs(downloadDTDs.isSelected());
    }

    // Called by XPathExtractorGui
    public void modifyTestElement(XPathExtractor extractor) {
        extractor.setValidating(validate.isSelected());
        extractor.setWhitespace(whitespace.isSelected());
        extractor.setTolerant(tolerant.isSelected());
        extractor.setNameSpace(namespace.isSelected());
        extractor.setShowWarnings(showWarnings.isSelected());
        extractor.setReportErrors(reportErrors.isSelected());
        extractor.setQuiet(quiet.isSelected());
        extractor.setDownloadDTDs(downloadDTDs.isSelected());
    }

    // Called by XPathAssertionGui
    public void configure(XPathAssertion assertion) {
        whitespace.setSelected(assertion.isWhitespace());
        validate.setSelected(assertion.isValidating());
        tolerant.setSelected(assertion.isTolerant());
        namespace.setSelected(assertion.isNamespace());
        quiet.setSelected(assertion.isQuiet());
        showWarnings.setSelected(assertion.showWarnings());
        reportErrors.setSelected(assertion.reportErrors());
        downloadDTDs.setSelected(assertion.isDownloadDTDs());
        tolerant();
    }

    // Called by XPathExtractorGui
    public void configure(XPathExtractor extractor) {
        whitespace.setSelected(extractor.isWhitespace());
        validate.setSelected(extractor.isValidating());
        tolerant.setSelected(extractor.isTolerant());
        namespace.setSelected(extractor.useNameSpace());
        quiet.setSelected(extractor.isQuiet());
        showWarnings.setSelected(extractor.showWarnings());
        reportErrors.setSelected(extractor.reportErrors());
        downloadDTDs.setSelected(extractor.isDownloadDTDs());
        tolerant();
    }
}
