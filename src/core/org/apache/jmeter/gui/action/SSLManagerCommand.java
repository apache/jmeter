/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SSLManager;

/**
 * SSL Manager Command.  The SSL Manager provides a mechanism to change your
 * client authentication if required by the server.  If you have JSSE 1.0.2
 * installed, you can select your client identity from a list of installed keys.
 * You can also change your keystore.  JSSE 1.0.2 allows you to export a PKCS#12
 * key from Netscape 4.04 or higher and use it in a read only format.  You must
 * supply a password that is greater than six characters due to limitations in
 * the keytool program--and possibly the rest of the system.
 *
 * <p>
 * By selecting a *.p12 file as your keystore (your PKCS#12) format file, you can
 * have a whopping one key keystore.  The advantage is that you can test a
 * connection using the assigned Certificate from a Certificate Authority.
 * </p>
 *
 * @author <a href="bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision$ $Date$
 */

public class SSLManagerCommand implements Command {
    private static Set commandSet;
    private JFileChooser keyStoreChooser;

    static {
        HashSet commands = new HashSet();
        commands.add("sslManager");
        SSLManagerCommand.commandSet = Collections.unmodifiableSet(commands);
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        System.setProperty("javax.net.ssl.debug", "all");
    }

    /**
     * Handle the "sslmanager" action by displaying the "SSL CLient Manager"
     * dialog box.  The Dialog Box is NOT modal, because those should be avoided
     * if at all possible.
     */
    public void doAction(ActionEvent e) {
        if (e.getActionCommand().equals("sslManager")) {
            this.sslManager();
        }
    }

    /**
     * Provide the list of Action names that are available in this command.
     */
    public Set getActionNames() {
        return SSLManagerCommand.commandSet;
    }

    /**
     * Called by sslManager button. Raises sslManager dialog.  Currently the sslManager box has
     * the product image and the copyright notice.  The dialog box is centered
     * over the MainFrame.
     */
    private void sslManager() {
        SSLManager.reset();

        keyStoreChooser = new JFileChooser(JMeterUtils.getJMeterProperties().getProperty("user.dir"));
        keyStoreChooser.addChoosableFileFilter(new AcceptPKCS12FileFilter());
        keyStoreChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retVal = keyStoreChooser.showOpenDialog(GuiPackage.getInstance().getMainFrame());

        if (JFileChooser.APPROVE_OPTION == retVal) {
            File selectedFile = keyStoreChooser.getSelectedFile();
            try {
                JMeterUtils.getJMeterProperties().setProperty("javax.net.ssl.keyStore", selectedFile.getCanonicalPath());
            } catch (Exception e) {
            }
        }

        keyStoreChooser = null;
        SSLManager.getInstance();
    }

    /**
     * Internal class to add a PKCS12 file format filter for JFileChooser.
     */
    static private class AcceptPKCS12FileFilter extends FileFilter {
        /**
         * Get the description that shows up in JFileChooser filter menu.
         *
         * @return description
         */
        public String getDescription() {
            return JMeterUtils.getResString("pkcs12_desc");
        }

        /**
         * Tests to see if the file ends with "*.p12" or "*.P12".
         *
         * @param testfile file to test
         * @return         true if file is accepted, false otherwise
         */
        public boolean accept(File testFile) {
            return testFile.isDirectory() ||
                   testFile.getName().endsWith(".p12") ||
                   testFile.getName().endsWith(".P12");
        }
    }
}
