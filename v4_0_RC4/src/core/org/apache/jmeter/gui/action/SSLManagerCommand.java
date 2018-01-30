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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SSLManager;

//
/**
 * SSL Manager Command. The SSL Manager provides a mechanism to change your
 * client authentication if required by the server. If you have JSSE 1.0.2
 * installed, you can select your client identity from a list of installed keys.
 * You can also change your keystore. JSSE 1.0.2 allows you to export a PKCS#12
 * key from Netscape 4.04 or higher and use it in a read only format. You must
 * supply a password that is greater than six characters due to limitations in
 * the keytool program--and possibly the rest of the system.
 * <p>
 * By selecting a *.p12 file as your keystore (your PKCS#12) format file, you
 * can have a whopping one key keystore. The advantage is that you can test a
 * connection using the assigned Certificate from a Certificate Authority.
 * </p>
 * TODO ?
 * N.B. The present implementation does not seem to allow selection of keys,
 * it only allows a change of keystore at run-time, or to provide one if not
 * already defined via the property.
 *
 */
public class SSLManagerCommand extends AbstractAction {
    private static final Set<String> commandSet;
    static {
        Set<String> commands = new HashSet<>();
        commands.add(ActionNames.SSL_MANAGER);
        commandSet = Collections.unmodifiableSet(commands);
    }

    /**
     * Handle the "sslmanager" action by displaying the "SSL CLient Manager"
     * dialog box. The Dialog Box is NOT modal, because those should be avoided
     * if at all possible.
     */
    @Override
    public void doAction(ActionEvent e) {
        if (e.getActionCommand().equals(ActionNames.SSL_MANAGER)) {
            this.sslManager();
        }
    }

    /**
     * Provide the list of Action names that are available in this command.
     */
    @Override
    public Set<String> getActionNames() {
        return SSLManagerCommand.commandSet;
    }

    /**
     * Called by sslManager button. Raises sslManager dialog.
     * I.e. a FileChooser for PCSI12 (.p12|.P12) files.
     */
    private void sslManager() {
        SSLManager.reset();

        JFileChooser keyStoreChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
        keyStoreChooser.addChoosableFileFilter(new AcceptPKCS12FileFilter());
        keyStoreChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retVal = keyStoreChooser.showOpenDialog(GuiPackage.getInstance().getMainFrame());

        if (JFileChooser.APPROVE_OPTION == retVal) {
            File selectedFile = keyStoreChooser.getSelectedFile();
            try {
                System.setProperty(SSLManager.JAVAX_NET_SSL_KEY_STORE, selectedFile.getCanonicalPath());
            } catch (IOException e) {
                //Ignored
            }
        }

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
        @Override
        public String getDescription() {
            return JMeterUtils.getResString("pkcs12_desc"); //$NON-NLS-1$
        }

        /**
         * Tests to see if the file ends with "*.p12" or "*.P12".
         *
         * @param testFile
         *            file to test
         * @return true if file is accepted, false otherwise
         */
        @Override
        public boolean accept(File testFile) {
            return testFile.isDirectory()
            || testFile.getName().endsWith(".p12")  //$NON-NLS-1$
            || testFile.getName().endsWith(".P12"); //$NON-NLS-1$
        }
    }
}
