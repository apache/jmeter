/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.protocol.http.config.gui;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;


/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class MultipartUrlConfigGui extends UrlConfigGui implements ActionListener
{

    private JTextField filenameField;
    private JTextField paramNameField;
    private JTextField mimetypeField;

    private static String FILENAME = "filename";
    private static String BROWSE = "browse";
    private static String PARAMNAME = "paramname";
    private static String MIMETYPE = "mimetype";

    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public MultipartUrlConfigGui()
    {
        super();
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public TestElement createTestElement()
    {
        TestElement ce = super.createTestElement();

        configureTestElement(ce);
        ce.setProperty(HTTPSampler.MIMETYPE, mimetypeField.getText());
        ce.setProperty(HTTPSampler.FILE_NAME, filenameField.getText());
        ce.setProperty(HTTPSampler.FILE_FIELD, paramNameField.getText());
        return ce;
    }

    public void configureSampler(HTTPSampler sampler)
    {
        sampler.setMimetype(mimetypeField.getText());
        sampler.setFileField(paramNameField.getText());
        sampler.setFilename(filenameField.getText());
        super.configureSampler(sampler);
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param el  !ToDo (Parameter description)
     ***************************************/
    public void configure(TestElement el)
    {
        super.configure(el);
        mimetypeField.setText(el.getPropertyAsString(HTTPSampler.MIMETYPE));
        filenameField.setText(el.getPropertyAsString(HTTPSampler.FILE_NAME));
        paramNameField.setText(el.getPropertyAsString(HTTPSampler.FILE_FIELD));
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("url_multipart_config_title");
    }

    /****************************************
     * !ToDo (Method description)
     ***************************************/
    public void updateGui()
    {}

    /****************************************
     * !ToDo (Method description)
     *
     *@param e  !ToDo (Parameter description)
     ***************************************/
    public void actionPerformed(ActionEvent e)
    {
        String name = e.getActionCommand();

        if (name.equals(BROWSE))
        {
            JFileChooser chooser = FileDialoger.promptToOpenFile();

            if (chooser == null)
            {
                return;
            }
            File file = chooser.getSelectedFile();

            if (file != null)
            {
                filenameField.setText(file.getPath());
            }
        }
    }

    /****************************************
     * !ToDo (Method description)
     ***************************************/
    protected void init()
    {
        this.setLayout(new BorderLayout());

        // WEB SERVER PANEL
        VerticalPanel webServerPanel = new VerticalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server")));
        webServerPanel.add(getDomainPanel());
        webServerPanel.add(getPortPanel());

        // WEB REQUEST PANEL
        JPanel webRequestPanel = new JPanel();
        webRequestPanel.setLayout(new BoxLayout(webRequestPanel, BoxLayout.Y_AXIS));
        webRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_request")));
        
        webRequestPanel.add(getProtocolAndMethodPanel());
        webRequestPanel.add(getPathPanel());
        webRequestPanel.add(getParameterPanel());
        webRequestPanel.add(getFilePanel());
        
        this.add(webServerPanel, BorderLayout.NORTH);
        this.add(webRequestPanel, BorderLayout.CENTER);
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    protected JPanel getFilePanel()
    {
        JPanel filePanel = new VerticalPanel();
        filePanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("send_file")));

        filePanel.add(createFilenamePanel());
        filePanel.add(createFileParamNamePanel());
        filePanel.add(createFileMimeTypePanel());

        return filePanel;
    }

    private JPanel createFileMimeTypePanel() {
        mimetypeField = new JTextField(15);
        mimetypeField.setName(MIMETYPE);
        
        JLabel mimetypeLabel = new JLabel(JMeterUtils.getResString("send_file_mime_label"));
        mimetypeLabel.setLabelFor(mimetypeField);
        
        JPanel mimePanel = new JPanel(new BorderLayout(5, 0));
        mimePanel.add(mimetypeLabel, BorderLayout.WEST);
        mimePanel.add(mimetypeField, BorderLayout.CENTER);
        return mimePanel;
    }

    private JPanel createFileParamNamePanel() {
        paramNameField = new JTextField(15);
        paramNameField.setName(PARAMNAME);

        JLabel paramNameLabel = new JLabel(JMeterUtils.getResString("send_file_param_name_label"));
        paramNameLabel.setLabelFor(paramNameField);
        

        JPanel paramNamePanel = new JPanel(new BorderLayout(5, 0));        
        paramNamePanel.add(paramNameLabel, BorderLayout.WEST);
        paramNamePanel.add(paramNameField, BorderLayout.CENTER);
        return paramNamePanel;
    }

    private JPanel createFilenamePanel() {
        filenameField = new JTextField(15);
        filenameField.setName(FILENAME);

        JLabel filenameLabel = new JLabel(JMeterUtils.getResString("send_file_filename_label"));
        filenameLabel.setLabelFor(filenameField);        
        
        JButton browseFileButton = new JButton(JMeterUtils.getResString("send_file_browse"));
        browseFileButton.setActionCommand(BROWSE);
        browseFileButton.addActionListener(this);
        

        JPanel filenamePanel = new JPanel(new BorderLayout(5, 0));        
        filenamePanel.add(filenameLabel, BorderLayout.WEST);
        filenamePanel.add(filenameField, BorderLayout.CENTER);
        filenamePanel.add(browseFileButton, BorderLayout.EAST);
        return filenamePanel;
    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.config.gui.UrlConfigGui#clear()
     */
    public void clear()
    {
        // TODO Auto-generated method stub
        super.clear();
        filenameField.setText("");
        mimetypeField.setText("");
        paramNameField.setText("");
    }

}
